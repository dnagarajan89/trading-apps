package org.ta.dani.mwpl.nse.helper;

import org.apache.commons.io.IOUtils;
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
import org.ta.dani.mwpl.nse.model.EligibleScripts;
import org.ta.dani.mwpl.nse.model.StockPriceTracker;
import org.ta.dani.mwpl.nse.respository.EligibleScriptsRepository;
import org.ta.dani.mwpl.nse.respository.StockPriceTrackerRepository;

import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.ta.dani.mwpl.utils.MWPLUtils.localDateToString;
import static org.ta.dani.mwpl.utils.MWPLUtils.stringToLocalDate;

@Component
public class StockPriceHelper {

    Logger logger = LoggerFactory.getLogger(StockPriceHelper.class);

    @Autowired
    StockPriceTrackerRepository stockPriceTrackerRepository;

    @Autowired
    EligibleScriptsRepository eligibleScriptsRepository;

    @Value("${ta.mwpl.nse.combined_vol_and_oi.dbDateFormat}")
    private String dbDatePattern;

    @Value("${ta.mwpl.nse.quote_url}")
    private String quoteURL;

    public void processAndSave() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Month month = yesterday.getMonth();
        eligibleScriptsRepository.findByMonth(month).stream().forEach(this::processStockPriceForEligibleScript);
    }

    public void processStockPriceForEligibleScript(EligibleScripts eligibleScripts) {
        JSONObject jsonObject = getStockQuote(eligibleScripts.getNseSymbol());
        if (jsonObject != null) {
            LocalDate priceDate = stringToLocalDate((String) jsonObject.get("secDate"), "dd-MMM-yyyy HH:mm:ss");
            StockPriceTracker stockPriceTracker = stockPriceTrackerRepository.findByMonthAndDateOfRecordAndNseSymbol(eligibleScripts.getMonth(), localDateToString(priceDate, dbDatePattern), eligibleScripts.getNseSymbol());
            if (stockPriceTracker == null) {
                stockPriceTracker = new StockPriceTracker();
                stockPriceTracker.setMonth(eligibleScripts.getMonth());
                stockPriceTracker.setNseSymbol(eligibleScripts.getNseSymbol());
                stockPriceTracker.setUpdatedOn(LocalDateTime.now());
                stockPriceTracker.setOpenPrice(Double.valueOf((String) jsonObject.get("open")));
                stockPriceTracker.setClosePrice(Double.valueOf((String) jsonObject.get("closePrice")));
                stockPriceTracker.setLastPrice(Double.valueOf((String) jsonObject.get("lastPrice")));
                stockPriceTracker.setPriceDate(priceDate);
                stockPriceTracker.setDateOfRecord(localDateToString(priceDate, dbDatePattern));
                stockPriceTrackerRepository.save(stockPriceTracker);
            }
        }
    }

    public JSONObject getStockQuote(String symbol) {
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            CloseableHttpResponse response = client.execute(new HttpGet(buildURLForQuote(symbol.toUpperCase(), 0, 0, 0)));
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                response.close();
                client.close();
                throw new HttpException("Unable to connect to NSE");
            }
            Element content = Jsoup.parse(IOUtils.toString(new InputStreamReader(response.getEntity().getContent(), "UTF-8"))).getElementById("responseDiv");
            JSONObject jsonResponse = (JSONObject) new JSONParser().parse(content.text());
            JSONArray dataArray = (JSONArray) jsonResponse.get("data");
            JSONObject data = (JSONObject) dataArray.get(0);
            return data;
        } catch (Exception e) {
            logger.error("Error getting stock quote from NSE for " + symbol, e);
            return null;
        }
    }

    private String buildURLForQuote(String quote, Integer illiquidValue, Integer smeFlag, Integer itpFlag) {
        return quoteURL + "symbol=" + URLEncoder.encode(quote) + "&illiquid=" + illiquidValue.toString() + "&smeFlag=" + smeFlag.toString() + "&itpFlag=" + itpFlag.toString();
    }
}
