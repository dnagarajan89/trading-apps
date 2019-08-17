package org.ta.dani.mwpl.nse.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Data
public class EligibilityDetail {
    @Id
    private String id;
    @Field("compared_with_date")
    private String comparedWithDate;
    @Field("compared_to_date")
    private String comparedToDate;
    @Field("percentage_difference")
    private Double percentageDifference;
}
