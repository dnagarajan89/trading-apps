package org.ta.dani.mwpl.nse.respository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.ta.dani.mwpl.nse.model.CombinedVolAndOI;

public interface CombinedVolAndOIRepository extends CrudRepository<CombinedVolAndOI, String> {

	void deleteByDate(String date);

	List<CombinedVolAndOI> findByDate(String date);

	CombinedVolAndOI findByNseSymbolAndDate(String nseSymbol, String date);
}
