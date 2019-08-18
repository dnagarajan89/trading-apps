package org.ta.dani.mwpl.nse.respository;

import org.springframework.data.repository.CrudRepository;
import org.ta.dani.mwpl.nse.model.EligibleScripts;

import java.time.Month;
import java.util.List;

public interface EligibleScriptsRepository extends CrudRepository<EligibleScripts, String> {

    List<EligibleScripts> findByMonth(Month month);

    EligibleScripts findByMonthAndNseSymbol(Month month, String nseSymbol);

    void deleteByEligibleOnDate(String date);
}
