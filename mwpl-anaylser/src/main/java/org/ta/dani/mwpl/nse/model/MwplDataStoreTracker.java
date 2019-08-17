package org.ta.dani.mwpl.nse.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

@Data
@Document("mwpl_data_store_tracker")
public class MwplDataStoreTracker {
    @Id
    private String id;
    @Indexed
    @Field("month_of_analysis")
    private Month month;
    @Field("mwpl_date_in_string")
    private String mwplDateInString;
    @Indexed
    @Field("mwpl_date")
    private LocalDate mwplDate;
    @Field("is_mwpl_available")
    private boolean isMwplAvailable;
    @Field("updated_time")
    private LocalDateTime updatedTime;
}
