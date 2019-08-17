package org.ta.dani.mwpl.nse.helper;

import com.sendgrid.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ta.dani.mwpl.utils.MWPLUtils;

import java.io.IOException;
import java.time.LocalDate;

@Component
public class EmailHelper {

    Logger logger = LoggerFactory.getLogger(EmailHelper.class);

    @Value("${sendgrid.apiKey}")
    private String apiKey;

    @Value("${ta.notifications.fromEmail}")
    private String fromEmailAddr;

    @Value("${ta.notifications.mwpl.datastored.templateId}")
    private String templateId;

    @Value("${ta.notifications.mwpl.datastored.emailSubject}")
    private String dataStoredEmailSub;

    @Value("${ta.notifications.mwpl.datastored.emailContent}")
    private String dataStoredEmailCont;

    @Value("${ta.notifications.toEmail}")
    private String toEmailAddr;

    @Value("${ta.notifications.mwpl.nodata.emailSubject}")
    private String noDataEmailSub;

    @Value("${ta.notifications.mwpl.nodata.emailContent}")
    private String noDataEmailCont;

    public boolean sendEmailForMwplData(LocalDate processedDate, boolean isSaved) {
        Email from = new Email(fromEmailAddr);
        Email to = new Email(toEmailAddr);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        String processedDateStr = MWPLUtils.localDateToString(processedDate, "dd-MMM-yyyy");
        String emailSubject, emailContent;
        if (isSaved) {
            emailSubject = dataStoredEmailSub.replace("{{processedDate}}", processedDateStr);
            emailContent = dataStoredEmailCont.replace("{{processedDate}}", processedDateStr);
        } else {
            emailSubject = noDataEmailSub.replace("{{attemptedDate}}", processedDateStr);
            emailContent = noDataEmailCont.replace("{{attemptedDate}}", processedDateStr);
        }
        personalization.addDynamicTemplateData("emailContent", emailContent);
        personalization.addDynamicTemplateData("emailSubject", emailSubject);
        personalization.addTo(to);
        mail.addPersonalization(personalization);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info("Email successfully processed with response status code: " + response.getStatusCode());
        } catch (IOException ex) {
            logger.error("Error sending notification email for " + processedDate, ex);
            return false;
        } catch (Exception e) {
            logger.error("Error sending notification email for " + processedDate, e);
            return false;
        }
        return true;
    }

}
