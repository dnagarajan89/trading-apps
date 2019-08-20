package org.ta.dani.mwpl.nse.respository;

import java.time.Month;

import org.springframework.data.repository.CrudRepository;
import org.ta.dani.mwpl.nse.model.StockPriceTracker;

public interface StockPriceTrackerRepository extends CrudRepository<StockPriceTracker, String> {

    StockPriceTracker findByMonthAndDateOfRecordAndNseSymbol(Month month, String dateOfRecord, String nseSymbol);
}
