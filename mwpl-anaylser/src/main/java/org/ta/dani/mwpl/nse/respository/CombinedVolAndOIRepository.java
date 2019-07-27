package org.ta.dani.mwpl.nse.respository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.ta.dani.mwpl.nse.model.CombinedVolAndOI;

public interface CombinedVolAndOIRepository extends CrudRepository<CombinedVolAndOI, String> {

	void deleteByDate(String date);

	List<CombinedVolAndOI> findByDate(String date);

}
