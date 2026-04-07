package org.propertymanagement.associationmeeting.exception;

import org.propertymanagement.util.JsonUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.config.WebConfig;
import org.propertymanagement.associationmeeting.config.WebSecurityConfig;
import org.propertymanagement.associationmeeting.web.dto.MeetingRequestDto;
import org.propertymanagement.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.propertymanagement.associationmeeting.exception.MeetingScheduleException.LogLevel.ERROR;
import static org.propertymanagement.associationmeeting.web.exception.ExceptionHandlerController.API_ERROR_UNABLE_TO_PROCESS_THE_REQUEST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        properties = {
                "logging.level.org.springframework.web=INFO",
                "logging.level.org.springframework.web.servlet.mvc.method.annotation=INFO"
        })
@ContextConfiguration(classes = {
        WebConfig.class,
        WebSecurityConfig.class
})
public class ExceptionHandlerControllerTest {
    private static final String ROOT_PATH = "/communities";
    private static final CommunityId COMMUNITY_ID = new CommunityId(1L);
    private static final TrackerId TRACKER_ID = new TrackerId(UUID.randomUUID());
    public static final String TECHNICAL_ERROR_MESSAGE = "Technical error message";
    public static final String API_ERROR_MESSAGE = "API error message";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MeetingScheduler meetingScheduler;


    @Test
    void newMeetingBadRequestAsMeetingScheduleException() throws Exception {
        MeetingInvite meetingInvite = MeetingInvite.create(COMMUNITY_ID, new MeetingDate("date"), new MeetingTime("time"), null)
                .withTracker(TRACKER_ID);
        MeetingRequestDto meetingRequestDto = newMeetingRequestDto(meetingInvite);

        String apiErrorMessage = API_ERROR_MESSAGE;
        given(meetingScheduler.newMeeting(any(MeetingInvite.class)))
                .willThrow(new MeetingScheduleException(TECHNICAL_ERROR_MESSAGE, apiErrorMessage, ERROR));

        mockMvc.perform(post(ROOT_PATH + "/{communityId}/meetings", COMMUNITY_ID.value())
                        .with(httpBasic("admin", "admin"))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(JsonUtil.asJsonString(meetingRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(Matchers.is(BAD_REQUEST.value())))
                .andExpect(jsonPath("$.instance").value(org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.timestamp").value(org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.detail").value(Matchers.is(apiErrorMessage)))
        ;
        verify(meetingScheduler).newMeeting(any(MeetingInvite.class));
    }

    @Test
    void newMeetingBadRequestAsInvalidMeetingInviteException() throws Exception {
        MeetingInvite meetingInvite = MeetingInvite.create(COMMUNITY_ID, new MeetingDate("date"), new MeetingTime("time"), null)
                .withTracker(TRACKER_ID);
        MeetingRequestDto meetingRequestDto = newMeetingRequestDto(meetingInvite);

        given(meetingScheduler.newMeeting(any(MeetingInvite.class)))
                .willThrow(new InvalidMeetingInviteException(TECHNICAL_ERROR_MESSAGE, API_ERROR_MESSAGE));

        mockMvc.perform(post(ROOT_PATH + "/{communityId}/meetings", COMMUNITY_ID.value())
                        .with(httpBasic("admin", "admin"))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(JsonUtil.asJsonString(meetingRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(Matchers.is(BAD_REQUEST.value())))
                .andExpect(jsonPath("$.instance").value(org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.timestamp").value(org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.detail").value(Matchers.is(API_ERROR_MESSAGE)))
        ;
        verify(meetingScheduler).newMeeting(any(MeetingInvite.class));
    }

    @Test
    void newMeetingBadRequestAsRuntimeException() throws Exception {
        MeetingInvite meetingInvite = MeetingInvite.create(COMMUNITY_ID, new MeetingDate("date"), new MeetingTime("time"), null)
                .withTracker(TRACKER_ID);
        MeetingRequestDto meetingRequestDto = newMeetingRequestDto(meetingInvite);

        given(meetingScheduler.newMeeting(any(MeetingInvite.class)))
                .willThrow(new RuntimeException(API_ERROR_MESSAGE));

        mockMvc.perform(post(ROOT_PATH + "/{communityId}/meetings", COMMUNITY_ID.value())
                        .with(httpBasic("admin", "admin"))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(JsonUtil.asJsonString(meetingRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(Matchers.is(BAD_REQUEST.value())))
                .andExpect(jsonPath("$.instance").value(org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.timestamp").value(org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.detail").value(Matchers.is(API_ERROR_UNABLE_TO_PROCESS_THE_REQUEST)))
        ;
        verify(meetingScheduler).newMeeting(any(MeetingInvite.class));
    }

    private MeetingRequestDto newMeetingRequestDto(MeetingInvite meetingInvite) {
        var meetingRequestDto = new MeetingRequestDto();
        meetingRequestDto.setTime(meetingInvite.time().value());
        meetingRequestDto.setDate(meetingInvite.date().value());
        return meetingRequestDto;
    }


    @Test
    void newMeetingBadRequestAsMissingDateTimeFields() throws Exception {
        mockMvc.perform(post(ROOT_PATH + "/{communityId}/meetings", COMMUNITY_ID.value())
                        .with(httpBasic("admin", "admin"))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(JsonUtil.asJsonString(new MeetingRequestDto())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(Matchers.is(BAD_REQUEST.value())))
                .andExpect(jsonPath("$.instance").value(org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.timestamp").value(org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.detail").value(Matchers.is(API_ERROR_UNABLE_TO_PROCESS_THE_REQUEST)))
        ;
        verifyNoInteractions(meetingScheduler);
    }
}
