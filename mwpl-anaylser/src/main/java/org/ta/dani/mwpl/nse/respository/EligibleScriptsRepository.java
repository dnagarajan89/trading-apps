package org.ta.dani.mwpl.nse.respository;

import org.springframework.data.repository.CrudRepository;
import org.ta.dani.mwpl.nse.model.EligibleScripts;

import java.time.Month;

public interface EligibleScriptsRepository extends CrudRepository<EligibleScripts, String> {

    EligibleScripts findByMonthAndNseSymbol(Month month, String nseSymbol);
}
