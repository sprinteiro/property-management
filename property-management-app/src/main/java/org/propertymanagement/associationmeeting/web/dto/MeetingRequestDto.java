package org.propertymanagement.associationmeeting.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Represents a request to create a new meeting.
 */
public class MeetingRequestDto {

    /**
     * The date of the meeting in dd/MM/yyyy format.
     * Example: "01/12/2025"
     */
    @NotBlank
    private String date;
    /**
     * The time of the meeting in HH:mm format.
     * Example: "19:00"
     */
    @NotBlank
    private String time;

    public MeetingRequestDto() {}

    public MeetingRequestDto(String date, String time) {
        this.date = date;
        this.time = time;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingRequestDto that = (MeetingRequestDto) o;
        return Objects.equals(date, that.date) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time);
    }

    @Override
    public String toString() {
        return "MeetingRequestDto{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
