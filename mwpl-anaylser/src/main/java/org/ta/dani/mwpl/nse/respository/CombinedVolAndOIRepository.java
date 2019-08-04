package org.ta.dani.mwpl.nse.respository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository;
import org.ta.dani.mwpl.nse.model.CombinedVolAndOI;

public interface CombinedVolAndOIRepository extends DatastoreRepository<CombinedVolAndOI, Long> {

	void deleteByDate(LocalDate date);

	List<CombinedVolAndOI> findByDate(LocalDate date);

}
