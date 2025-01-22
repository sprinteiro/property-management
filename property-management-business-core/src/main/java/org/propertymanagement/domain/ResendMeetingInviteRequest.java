package org.propertymanagement.domain;

import java.util.Arrays;

public record ResendMeetingInviteRequest(CommunityId communityId, TrackerId trackerId, ResendType type) {

    public enum ResendType {
        FOR_APPROVAL,
        TO_PARTICIPANTS;

        public static ResendType getTypeFrom(String from) {
            return Arrays.stream(ResendType.values())
                    .filter(type -> type.name().equalsIgnoreCase(from))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown Resend_Type: " + from));
        }
    }
}
