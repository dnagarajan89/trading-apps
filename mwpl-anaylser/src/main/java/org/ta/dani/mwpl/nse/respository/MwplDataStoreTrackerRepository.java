package org.ta.dani.mwpl.nse.respository;

import org.springframework.data.repository.CrudRepository;
import org.ta.dani.mwpl.nse.model.MwplDataStoreTracker;

import java.time.Month;
import java.util.List;

public interface MwplDataStoreTrackerRepository extends CrudRepository<MwplDataStoreTracker, String> {

    List<MwplDataStoreTracker> findByMonthAndIsMwplAvailableOrderByMwplDateDesc(Month month, boolean isMwplAvailable);

    void deleteByMwplDateInString(String date);
}
