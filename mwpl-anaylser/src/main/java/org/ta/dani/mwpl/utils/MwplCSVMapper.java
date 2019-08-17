package org.ta.dani.mwpl.utils;

import org.apache.commons.lang3.StringUtils;
import org.ta.dani.mwpl.nse.model.CombinedVolAndOI;

public class MwplCSVMapper {

    public static CombinedVolAndOI mapToCombinedVolAndOI(String[] data) {
        CombinedVolAndOI combinedVolAndOI = new CombinedVolAndOI();
        combinedVolAndOI.setDate(data[0]);
        combinedVolAndOI.setISIN(data[1]);
        combinedVolAndOI.setScriptName(data[2]);
        combinedVolAndOI.setNseSymbol(data[3]);
        if (StringUtils.isNumeric(data[4])) {
            combinedVolAndOI.setMwpl(Long.valueOf(data[4]));
        }
        if (StringUtils.isNumeric(data[5])) {
            combinedVolAndOI.setOpenInterest(Long.valueOf(data[5]));
        }
        if (StringUtils.isNumeric(data[6])) {
            combinedVolAndOI.setLimitForNextDay(Long.valueOf(data[6]));
        }
        if (combinedVolAndOI.getMwpl() == null || combinedVolAndOI.getOpenInterest() == null
                || combinedVolAndOI.getLimitForNextDay() == null) {
            combinedVolAndOI.setNoFreshPositions(true);
        }
        if (!combinedVolAndOI.isNoFreshPositions()) {
            combinedVolAndOI.setPercentageOfOpenContracts((combinedVolAndOI.getLimitForNextDay() / Double.valueOf(combinedVolAndOI.getMwpl())) * 100.0);
        }
        return combinedVolAndOI;
    }
}
