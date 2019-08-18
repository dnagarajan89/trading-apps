package org.ta.dani.mwpl.nse.respository;

import org.springframework.data.repository.CrudRepository;
import org.ta.dani.mwpl.nse.model.StockPriceTracker;

import java.time.Month;
import java.util.List;

public interface StockPriceTrackerRepository extends CrudRepository<StockPriceTracker, String> {

    List<StockPriceTracker> findByEligibleScriptId(String eligibleScriptId);

    StockPriceTracker findByMonthAndDateOfRecord(Month month, String dateOfRecord);
}
