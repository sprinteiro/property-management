package org.propertymanagement.associationmeeting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Association Meeting API")
                        .version("1.0")
                        .description("API for managing neighbourhood association meetings"))
                .schemaRequirement("basicAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic"))
                .schema("MeetingRequestDto", new Schema()
                        .addProperty("date", new Schema()
                                .type("string")
                                .description("The date of the meeting in dd/MM/yyyy format")
                                .example("01/12/2025"))
                        .addProperty("time", new Schema()
                                .type("string")
                                .description("The time of the meeting in HH:mm format")
                                .example("19:00")))
                .schema("MeetingStatusDto", new Schema()
                        .addProperty("date", new Schema()
                                .type("string")
                                .description("The date of the meeting in dd/MM/yyyy format")
                                .example("01/12/2024"))
                        .addProperty("time", new Schema()
                                .type("string")
                                .description("The time of the meeting in HH:mm format")
                                .example("19:00"))
                        .addProperty("status", new Schema()
                                .type("TrackingStatus")
                                .description("Current meeting status (MEETING_SCHEDULE_REQUESTED, MEETING_SCHEDULE_APPROVED)")
                                .example("48584f75-5021-47c2-9c86-7bc6880a3cd1"))
                        .addProperty("approvalDateTime", new Schema()
                                .type("string")
                                .description("Date and time of when the meeting was approved")
                                .example("12/12/2025 19:00"))
                        .addProperty("description", new Schema()
                                .type("string")
                                .description("Goal/Description of the meeting.")
                                .example("Yearly meeting"))
                        .addProperty("trackerId", new Schema()
                                .type("string")
                                .description("Tracker identifier for the meeeting")
                                .example("trackers/4e002c5c-9c78-4179-82ae-f1672e2a6c25")))
                .schema("MeetingApprovalRequestDto", new Schema()
                        .addProperty("approverId", new Schema()
                                .type("long")
                                .description("Valid approver identifier for the registered meeting that belongs to a community.")
                                .example("1")))
                .schema("ResendInviteDto", new Schema()
                        .addProperty("communityId", new Schema()
                                .type("long")
                                .description("Community identifier.")
                                .example("1"))
                        .addProperty("trackerId", new Schema()
                                .type("string")
                                .description("Tracker identifier for the meeeting")
                                .example("trackers/4e002c5c-9c78-4179-82ae-f1672e2a6c25"))
                        .addProperty("action", new Schema()
                                .type("RESEND_ACTION")
                                .description("Resend action type (FOR_APPROVAL, TO_PARTICIPANTS)")
                                .example("FOR_APPROVAL")))
                ;
    }
}
