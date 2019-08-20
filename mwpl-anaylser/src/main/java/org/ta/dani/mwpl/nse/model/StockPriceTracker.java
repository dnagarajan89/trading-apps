package org.ta.dani.mwpl.nse.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

@Data
@Document("stock_price_tracker")
public class StockPriceTracker {
    @Id
    private String id;
    @Field("nse_symbol")
    private String nseSymbol;
    @Field("open_price")
    private Double openPrice;
    @Field("close_price")
    private Double closePrice;
    @Field("last_price")
    private Double lastPrice;
    @Field("month")
    private Month month;
    @Field("price_date")
    private LocalDate priceDate;
    @Field("date_of_record")
    private String dateOfRecord;
    @Field("updated_on")
    private LocalDateTime updatedOn;
}
