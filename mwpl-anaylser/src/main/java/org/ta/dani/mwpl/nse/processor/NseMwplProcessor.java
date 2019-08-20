package org.ta.dani.mwpl.nse.processor;

import java.io.*;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ta.dani.mwpl.excepion.DateAlreadyProcessedException;
import org.ta.dani.mwpl.excepion.MwplProcessException;
import org.ta.dani.mwpl.nse.helper.EmailHelper;
import org.ta.dani.mwpl.nse.helper.MwplDataHelper;
import org.ta.dani.mwpl.nse.helper.StockPriceHelper;
import org.ta.dani.mwpl.nse.model.CombinedVolAndOI;
import org.ta.dani.mwpl.nse.respository.CombinedVolAndOIRepository;
import org.ta.dani.mwpl.nse.respository.EligibleScriptsRepository;
import org.ta.dani.mwpl.nse.respository.MwplDataStoreTrackerRepository;

import static java.util.Comparator.comparing;
import static org.ta.dani.mwpl.utils.MWPLUtils.dervieMwplDate;
import static org.ta.dani.mwpl.utils.MWPLUtils.localDateToString;

@Component
public class NseMwplProcessor {

    @Autowired
    private CombinedVolAndOIRepository combinedVolAndOIRepository;

    @Value("${ta.mwpl.nse.combined_vol_and_oi.url}")
    private String combinedVolAndOIUrl;

    @Value("${ta.mwpl.nse.combined_vol_and_oi.localFilePath}")
    private String localFilePath;

    @Value("${ta.mwpl.nse.combined_vol_and_oi.urlDateFormat}")
    private String urlDateFormat;

    @Value("${ta.mwpl.nse.combined_vol_and_oi.dbDateFormat}")
    private String dbDateFormat;

    @Autowired
    EmailHelper emailHelper;

    @Autowired
    MwplDataHelper mwplDataHelper;

    @Autowired
    StockPriceHelper stockPriceHelper;

    private static Logger logger = LoggerFactory.getLogger(NseMwplProcessor.class);

    public List<CombinedVolAndOI> processMwpl(LocalDate dateToProcess, boolean deleteEntriesIfExists)
            throws DateAlreadyProcessedException {
        logger.info("Starting MWPL processing");
        String urlDateString = null;
        if (dateToProcess != null) {
            urlDateString = localDateToString(dateToProcess, urlDateFormat);
        } else {
            dateToProcess = dervieMwplDate(dateToProcess);
            urlDateString = localDateToString(dateToProcess, urlDateFormat);
        }

        logger.info("Processing MWPL data for " + dateToProcess);
        String url = combinedVolAndOIUrl + urlDateString + ".zip";
        String filename = "combinedoi_" + urlDateString + ".zip";
        String filePath = localFilePath + urlDateString;
        logger.info("Url: " + url);
        logger.info("File name: " + filename);
        logger.info("File path: " + filePath);
        if (deleteEntriesIfExists) {
            mwplDataHelper.cleanMWPLDataIfExists(filePath, dateToProcess);
        } else {
            if (mwplDataHelper.isDataExistsInDbFor(dateToProcess)) {
                // Send email for already processed.
                throw new DateAlreadyProcessedException("Already processed MWPL for date : " + dateToProcess);
            }
            if (new File(filePath).exists()) {
                mwplDataHelper.cleanMWPLDataIfExists(filePath, null);
            }
        }
        try {
            downloadFile(url, filePath, filename);
        } catch (Exception e) {
            emailHelper.sendEmailForMwplData(dateToProcess, false);
        }
        String unzipUrl = filePath + File.separator + "unzipped";
        String zipFile = filePath + File.separator + filename;
        String csvFile = unZipAndGetCsvFileName(zipFile, unzipUrl);
        String csvFilePath = unzipUrl + File.separator + csvFile;

        List<CombinedVolAndOI> combinedVolAndOIs = mwplDataHelper.processMWPLDataAndSave(csvFilePath, dateToProcess);
        mwplDataHelper.cleanMWPLDataIfExists(filePath, null);
        return combinedVolAndOIs;
    }


    public void downloadFile(String url, String filePath, String filename) throws FileNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(filePath + File.separator + filename)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                throw (FileNotFoundException) e;
            }
            logger.error("Error downloading the file" + url, e);
            throw new MwplProcessException("Error downloading the file " + url, e);
        } catch (Exception e) {
            logger.error("Error downloading the file" + url, e);
            throw new MwplProcessException("Error downloading the file " + url, e);
        }
    }

    public String unZipAndGetCsvFileName(String zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
        String xmlFile = null;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                logger.info("Unzipping file : " + newFile.getAbsoluteFile());
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (fileName.endsWith(".csv")) {
                    xmlFile = fileName;
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException e) {
            logger.error("Error when unziping " + zipFile, e);
            throw new MwplProcessException("Error when unziping " + zipFile, e);
        } catch (Exception e) {
            logger.error("Error when unziping " + zipFile, e);
            throw new MwplProcessException("Error when unziping " + zipFile, e);
        }
        return xmlFile;
    }

    public List<CombinedVolAndOI> readMwplData(LocalDate date) {
        if (date == null) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            int weekendOffset = 0;
            if (yesterday.getDayOfWeek() == DayOfWeek.SUNDAY) {
                weekendOffset = 2;
            } else if (yesterday.getDayOfWeek() == DayOfWeek.SATURDAY) {
                weekendOffset = 1;
            }
            if (weekendOffset > 0) {
                date = yesterday.minusDays(weekendOffset);
            }
        }
        List<CombinedVolAndOI> combinedVolAndOIs = combinedVolAndOIRepository.findByDate(localDateToString(date, dbDateFormat).toUpperCase());
        Collections.sort(combinedVolAndOIs, comparing(CombinedVolAndOI::getNseSymbol));
        return combinedVolAndOIs;
    }

    public void processStockPriceForEligibleScripts() {
        stockPriceHelper.processAndSave();
    }

}
