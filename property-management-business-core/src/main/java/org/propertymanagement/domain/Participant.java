package org.propertymanagement.domain;

import java.util.Arrays;
import java.util.Objects;

public record Participant(
        NeighbourgId id,
        ParticipantRole role,
        Name name,
        PhoneNumber phoneNumber,
        Email email) {

    public enum ParticipantRole {
        ADMINISTRATOR,
        PRESIDENT,
        VICEPRESIDENT,
        COMMUNITY_MEMBER;

        public static ParticipantRole getRoleFrom(String from) {
            return Arrays.stream(ParticipantRole.values())
                    .filter(type -> type.name().equalsIgnoreCase(from))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown participant role: " + from));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
