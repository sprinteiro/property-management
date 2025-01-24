package org.propertymanagement.associationmeeting.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ResendInviteDto {
    @Positive(message = "Community identifier must be provided")
    private Long communityId;
    @NotBlank(message = "Tracker identifier must be provided")
    private String trackerId;
    @NotNull(message = "Resend action must be provided")
    private RESEND_ACTION action;

    public enum RESEND_ACTION {
        FOR_APPROVAL,
        TO_PARTICIPANTS
    }
}
