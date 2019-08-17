package org.ta.dani.mwpl.nse.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Document("combined_vol_and_oi")
public class CombinedVolAndOI {
	@Id
	private String id;
	@Field
	private String date;
	@Field("isin")
	private String ISIN;
	@Field("script_name")
	private String scriptName;
	@Field("nse_symbol")
	@Indexed
	private String nseSymbol;
	@Field("mwpl")
	private Long mwpl;
	@Field("open_interest")
	private Long openInterest;
	@Field("limit_for_next_day")
	private Long limitForNextDay;
	@Field("is_no_fresh_positions")
	private boolean noFreshPositions;
	@Field("open_contracts_percentage")
	private Double percentageOfOpenContracts;
}
