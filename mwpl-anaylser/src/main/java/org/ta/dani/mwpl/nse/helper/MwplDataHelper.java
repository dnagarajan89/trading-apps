package org.ta.dani.mwpl.nse.helper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ta.dani.mwpl.excepion.MwplProcessException;
import org.ta.dani.mwpl.nse.model.*;
import org.ta.dani.mwpl.nse.respository.CombinedVolAndOIRepository;
import org.ta.dani.mwpl.nse.respository.EligibleScriptsRepository;
import org.ta.dani.mwpl.nse.respository.MwplDataStoreTrackerRepository;
import org.ta.dani.mwpl.nse.respository.StockPriceTrackerRepository;
import org.ta.dani.mwpl.utils.MWPLUtils;
import org.ta.dani.mwpl.utils.MwplCSVMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections.CollectionUtils.*;
import static org.ta.dani.mwpl.utils.MWPLUtils.localDateToString;
import static org.ta.dani.mwpl.utils.MWPLUtils.stringToLocalDate;

@Component
public class MwplDataHelper {

    static final String getQuoteURL = "https://www.nseindia.com/live_market/dynaContent/live_watch/get_quote/GetQuote.jsp?";

    Logger logger = LoggerFactory.getLogger(MwplDataHelper.class);

    @Autowired
    private CombinedVolAndOIRepository combinedVolAndOIRepository;

    @Autowired
    private MwplDataStoreTrackerRepository mwplDataStoreTrackerRepository;

    @Autowired
    private EligibleScriptsRepository eligibleScriptsRepository;

    @Autowired
    private StockPriceTrackerRepository stockPriceTrackerRepository;

    @Autowired
    private EmailHelper emailHelper;

    @Value("${ta.mwpl.nse.combined_vol_and_oi.dbDateFormat}")
    private String dbDatePattern;

    @Value("${ta.mwpl.nse.combined_vol_and_oi.percentageDiff}")
    private Double percentageDiff;

    public List<CombinedVolAndOI> readMWPLDataFromCsv(String csvFilePath) {
        try (Stream<String> csvStream = Files.lines(Paths.get(csvFilePath))) {
            return csvStream.skip(1).map(line -> line.split(",")).map(MwplCSVMapper::mapToCombinedVolAndOI).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error reading MWPL data from CSV", e);
            throw new MwplProcessException("Error reading MWPL data from CSV " + csvFilePath, e);
        }
    }

    @Transactional
    public List<CombinedVolAndOI> processMWPLDataAndSave(String csvFilePath, LocalDate date) {
        List<CombinedVolAndOI> combinedVolAndOIs = readMWPLDataFromCsv(csvFilePath);
        combinedVolAndOIs = (List<CombinedVolAndOI>) combinedVolAndOIRepository.saveAll(combinedVolAndOIs);

        MwplDataStoreTracker mwplDataStoreTracker = new MwplDataStoreTracker();
        mwplDataStoreTracker.setMonth(date.getMonth());
        mwplDataStoreTracker.setMwplDateInString(localDateToString(date, dbDatePattern));
        mwplDataStoreTracker.setMwplDate(date);
        mwplDataStoreTracker.setMwplAvailable(isNotEmpty(combinedVolAndOIs));
        mwplDataStoreTracker.setUpdatedTime(LocalDateTime.now());
        mwplDataStoreTrackerRepository.save(mwplDataStoreTracker);

        if (mwplDataStoreTracker.isMwplAvailable()) {
            List<MwplDataStoreTracker> mwplDataStoreTrackers = mwplDataStoreTrackerRepository.findByMonthAndIsMwplAvailableOrderByMwplDateDesc(date.getMonth(), true);
            if (CollectionUtils.isNotEmpty(mwplDataStoreTrackers) && mwplDataStoreTrackers.size() > 1) {
                MwplDataStoreTracker secondRecord = mwplDataStoreTrackers.get(1);
                final String compareToDate = secondRecord.getMwplDateInString();
                combinedVolAndOIs.stream().filter(data -> !data.isNoFreshPositions()).forEach(data -> {
                    CombinedVolAndOI combinedVolAndOIToCompare = combinedVolAndOIRepository.findByNseSymbolAndDate(data.getNseSymbol(), compareToDate.toUpperCase());
                    Double percentageDiff = combinedVolAndOIToCompare.getPercentageOfOpenContracts() - data.getPercentageOfOpenContracts();
                    EligibleScripts eligibleScripts = null;
                    if (percentageDiff.compareTo(this.percentageDiff) >= 0) {
                        eligibleScripts = eligibleScriptsRepository.findByMonthAndNseSymbol(date.getMonth(), data.getNseSymbol());
                        if (eligibleScripts == null) {
                            eligibleScripts = new EligibleScripts();
                            eligibleScripts.setMonth(date.getMonth());
                            eligibleScripts.setNseSymbol(data.getNseSymbol());
                            eligibleScripts.setScriptName(data.getScriptName());
                            eligibleScripts.setEligibleOnDate(localDateToString(date, dbDatePattern));
                        }
                        EligibilityDetail eligibilityDetail = new EligibilityDetail();
                        eligibilityDetail.setComparedToDate(compareToDate);
                        eligibilityDetail.setComparedWithDate(localDateToString(date, dbDatePattern));
                        eligibilityDetail.setPercentageDifference(percentageDiff);
                        eligibleScripts.addEligibilityDetail(eligibilityDetail);
                        eligibleScriptsRepository.save(eligibleScripts);
                    }
                });
            }
        }
        emailHelper.sendEmailForMwplData(date, true);
        return combinedVolAndOIs;
    }

    public void cleanMWPLDataIfExists(String filePath, LocalDate deletionDataDate) {
        if (StringUtils.isNotBlank(filePath)) {
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    String errorMsg = "Error deleting directory " + filePath;
                    logger.error(errorMsg, e);
                    throw new MwplProcessException(errorMsg, e);
                }
            }
        }
        if (deletionDataDate != null) {
            String deletionDateInStr = localDateToString(deletionDataDate, dbDatePattern);
            List<CombinedVolAndOI> existingEntries = combinedVolAndOIRepository.findByDate(deletionDateInStr.toUpperCase());
            if (isNotEmpty(existingEntries)) {
                combinedVolAndOIRepository.deleteByDate(deletionDateInStr.toUpperCase());
                mwplDataStoreTrackerRepository.deleteByMwplDateInString(deletionDateInStr);
                eligibleScriptsRepository.deleteByEligibleOnDate(deletionDateInStr);
            }
        }
    }

    public boolean isDataExistsInDbFor(LocalDate date) {
        return isNotEmpty(combinedVolAndOIRepository.findByDate(localDateToString(date, dbDatePattern).toUpperCase()));
    }
}
