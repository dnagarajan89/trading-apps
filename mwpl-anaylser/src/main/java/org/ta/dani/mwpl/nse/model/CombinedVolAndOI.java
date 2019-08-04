package org.ta.dani.mwpl.nse.model;

import java.time.LocalDate;

import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity;
import org.springframework.cloud.gcp.data.datastore.core.mapping.Field;
import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
@Entity
public class CombinedVolAndOI {
	@Id
	private Long id;
	private LocalDate date;
	@Field(name = "isin")
	private String ISIN;
	@Field(name = "script_name")
	private String scriptName;
	@Field(name = "nse_symbol")
	private String nseSymbol;
	@Field(name = "mwpl")
	private Long mwpl;
	@Field(name = "open_interest")
	private Long openInterest;
	@Field(name = "limit_for_next_day")
	private Long limitForNextDay;
	@Field(name = "is_fresh_positions_available")
	private boolean noFreshPositions;
}
