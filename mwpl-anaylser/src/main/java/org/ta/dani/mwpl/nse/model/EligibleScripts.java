package org.ta.dani.mwpl.nse.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Data
@Document("eligible_scripts")
public class EligibleScripts {
    @Id
    private String id;
    @Field("eligible_month")
    private Month month;
    @Field("script_name")
    private String scriptName;
    @Field("nse_symbol")
    private String nseSymbol;
    @Field("eligible_on_date")
    private String eligibleOnDate;
    @Field("eligibility_details")
    private List<EligibilityDetail> eligibilityDetails;

    public void addEligibilityDetail(EligibilityDetail eligibilityDetail) {
        if(this.eligibilityDetails == null) {
            this.eligibilityDetails = new ArrayList<>();
        }
        this.eligibilityDetails.add(eligibilityDetail);
    }
}
