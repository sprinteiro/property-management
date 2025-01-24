package org.propertymanagement.associationmeeting.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.config.WebConfig;
import org.propertymanagement.associationmeeting.config.WebSecurityConfig;
import org.propertymanagement.associationmeeting.web.dto.MeetingApprovalRequestDto;
import org.propertymanagement.associationmeeting.web.dto.MeetingRequestDto;
import org.propertymanagement.associationmeeting.web.dto.ResendInviteDto;
import org.propertymanagement.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.propertymanagement.associationmeeting.web.dto.MeetingStatusDto.TrackingStatus.MEETING_SCHEDULE_APPROVED;
import static org.propertymanagement.associationmeeting.web.dto.MeetingStatusDto.TrackingStatus.MEETING_SCHEDULE_REQUESTED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        properties = {
                "logging.level.org.springframework.web=INFO",
                "logging.level.org.springframework.web.servlet.mvc.method.annotation=INFO"
        })
@ContextConfiguration(classes = {
        WebConfig.class,
        WebSecurityConfig.class
})
public class MeetingControllerTest {
    private static final String ROOT_PATH = "/communities";
    private static final CommunityId COMMUNITY_ID = new CommunityId(1L);
    private static final NeighbourgId PRESIDENT_ID = new NeighbourgId(2L);
    private static final MeetingDate MEETING_DATE = new MeetingDate("01/12/2024");
    private static final MeetingTime MEETING_TIME = new MeetingTime("19:00");
    private static final String APPROVAL_MEETING_DATE = "29/11/2024";
    private static final String APPROVAL_MEETING_TIME = "10:00";
    private static final String APPROVAL_DATE_TIME = APPROVAL_MEETING_DATE + " " + APPROVAL_MEETING_TIME;
    private static final TrackerId TRACKER_ID = new TrackerId(UUID.randomUUID());
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MeetingScheduler meetingScheduler;


    @Test
    void meetingStatusOk() throws Exception {
        MeetingInvite meetingInvite = new MeetingInvite(MEETING_DATE, MEETING_TIME, COMMUNITY_ID, PRESIDENT_ID, APPROVAL_DATE_TIME, TRACKER_ID, null);
        given(meetingScheduler.fecthMeetingInvite(any(CommunityId.class), any(TrackerId.class))).willReturn(meetingInvite);

        mockMvc.perform(get(ROOT_PATH + "/{communityId}/trackers/{trackerId}", COMMUNITY_ID.value(), TRACKER_ID.value())
                        .with(httpBasic("mark", "mark"))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("date").value(MEETING_DATE.value()))
                .andExpect(jsonPath("time").value(MEETING_TIME.value()))
                .andExpect(jsonPath("status").value(MEETING_SCHEDULE_APPROVED.name()))
                .andExpect(jsonPath("approvalDateTime").value(APPROVAL_DATE_TIME));
        verify(meetingScheduler).fecthMeetingInvite(any(CommunityId.class), any(TrackerId.class));
    }

    @Test
    void newMeetingCreated() throws Exception {
        MeetingInvite meetingInvite = new MeetingInvite(COMMUNITY_ID, new MeetingDate("date"), new MeetingTime("time"));
        meetingInvite.setCorrelationId("correlationId".getBytes(UTF_8));
        meetingInvite.setTrackerId(TRACKER_ID);

        MeetingRequestDto meetingRequestDto = new MeetingRequestDto();
        meetingRequestDto.setTime(meetingInvite.getTime().value());
        meetingRequestDto.setDate(meetingInvite.getDate().value());

        given(meetingScheduler.newMeeting(any(MeetingInvite.class))).willReturn(meetingInvite);

        mockMvc.perform(post(ROOT_PATH + "/{communityId}/meetings", COMMUNITY_ID.value())
                        .with(httpBasic("admin", "admin"))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(meetingRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header()
                        .string("Location", org.hamcrest.Matchers.containsString("/trackers/" + TRACKER_ID.value().toString())))
                .andExpect(jsonPath("status").value(MEETING_SCHEDULE_REQUESTED.name()))
                .andExpect(jsonPath("description").value(org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("trackerId").value(TRACKER_ID.toString()))
        ;
        verify(meetingScheduler).newMeeting(any(MeetingInvite.class));
    }

    @Test
    void approveMeetingOk() throws Exception {
        MeetingApprovalRequestDto requestDto = new MeetingApprovalRequestDto();
        requestDto.setApproverId(PRESIDENT_ID.value());

        mockMvc.perform(post(ROOT_PATH + "/{communityId}/trackers/{trackerId}", COMMUNITY_ID.value(), TRACKER_ID.toString())
                        .with(httpBasic("president", "president"))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("description").value(org.hamcrest.Matchers.notNullValue()));
        verify(meetingScheduler).fetchMeetingInviteAndNotifyScheduledMeetingForApproval(any(ApprovalMeetingInvite.class));

    }

    @Test
    void resendMeetingInviteOk() throws Exception {
        ResendInviteDto requestDto = new ResendInviteDto();
        requestDto.setCommunityId(PRESIDENT_ID.value());
        requestDto.setTrackerId(TRACKER_ID.toString());
        requestDto.setAction(ResendInviteDto.RESEND_ACTION.FOR_APPROVAL);

        mockMvc.perform(post(ROOT_PATH + "/resendinvite")
                        .with(httpBasic("admin", "admin"))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("description").value(org.hamcrest.Matchers.notNullValue()));
        verify(meetingScheduler).resendMeetingInvite(any(ResendMeetingInviteRequest.class));
    }

    private String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
