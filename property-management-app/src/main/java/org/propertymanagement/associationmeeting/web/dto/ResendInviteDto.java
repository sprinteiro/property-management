package org.propertymanagement.associationmeeting.web.dto;

import lombok.Data;

@Data
public class ResendInviteDto {
    private Long communityId;
    private String trackerId;
    private RESEND_ACTION action;

    public enum RESEND_ACTION {
        FOR_APPROVAL,
        TO_PARTICIPANTS
    }
}
