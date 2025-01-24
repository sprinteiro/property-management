package org.propertymanagement.associationmeeting.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.web.dto.MeetingApprovalRequestDto;
import org.propertymanagement.associationmeeting.web.dto.MeetingRequestDto;
import org.propertymanagement.associationmeeting.web.dto.MeetingStatusDto;
import org.propertymanagement.associationmeeting.web.dto.ResendInviteDto;
import org.propertymanagement.domain.*;
import org.propertymanagement.domain.ResendMeetingInviteRequest.ResendType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(path = "/communities", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class MeetingController {
    private final MeetingScheduler meetingScheduler;

    /*
        curl -v -H "Content-Type: application/json" -X GET http://localhost:8288/communities/1/trackers/48584f75-5021-47c2-9c86-7bc6880a3cd1
        curl -v --user louis:louis -H "Content-Type: application/json" -X GET http://localhost:8288/communities/1/trackers/48584f75-5021-47c2-9c86-7bc6880a3cd1
     */
    @GetMapping(path = "/{communityId}/trackers/{trackerId}")
    public ResponseEntity<MeetingStatusDto> meetingStatus(@PathVariable Long communityId, @PathVariable String trackerId) {
        log.info("About to check meeting status for trackerId={}", trackerId);
        MeetingInvite invite = meetingScheduler.fecthMeetingInvite(new CommunityId(communityId), new TrackerId(UUID.fromString(trackerId)));
        MeetingStatusDto dto = MeetingStatusDto.builder()
                .status(invite.getApprovalDateTime() == null ? MeetingStatusDto.TrackingStatus.MEETING_SCHEDULE_REQUESTED :
                        MeetingStatusDto.TrackingStatus.MEETING_SCHEDULE_APPROVED
                )
                .date(invite.getDate().value())
                .time(invite.getTime().value())
                .approvalDateTime(invite.getApprovalDateTime())
                .build();
        return ResponseEntity.ok(dto);
    }

    /*
        curl -v -X POST 'http://localhost:8288/communities/1/meetings' -H "Content-Type: application/json" -d '{"date":"01/12/2024", "time":"19:00"}'
        curl -v --user admin:admin -H "Content-Type: application/json" -X POST 'http://localhost:8288/communities/1/meetings' -d '{"date":"01/12/2024", "time":"19:00"}'
     */
    @PostMapping(path = "/{communityId}/meetings")
    public ResponseEntity<MeetingStatusDto> newMeeting(@PathVariable Long communityId, @Valid @RequestBody MeetingRequestDto meetingRequest) {
        log.info("Received new meeting. CommunityId={} Request={}", communityId, meetingRequest);
        MeetingInvite meetingInvite = new MeetingInvite(
                new CommunityId(communityId),
                new MeetingDate(meetingRequest.getDate()),
                new MeetingTime(meetingRequest.getTime()));

        meetingInvite = meetingScheduler.newMeeting(meetingInvite);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/communities/" + meetingInvite.getCommunityId().value() + "/trackers/{trackerId}")
                .buildAndExpand(meetingInvite.getTrackerId().value())
                .toUri();
        return ResponseEntity
                .created(uri)
                .body(MeetingStatusDto.builder()
                        .status(MeetingStatusDto.TrackingStatus.MEETING_SCHEDULE_REQUESTED)
                        .description("Association meeting creation has been requested.")
                        .trackerId(meetingInvite.getTrackerId().toString())
                        .build()
                );
    }

    /*
        curl -v -H "Content-Type: application/json" -X POST 'http://localhost:8288/communities/1/trackers/3fe5ba9a-d073-44cb-a2c6-c15a6afae40e' -d '{"approverId":"1"}'
        curl -v --user president:president -H "Content-Type: application/json" -X POST 'http://localhost:8288/communities/1/trackers/3fe5ba9a-d073-44cb-a2c6-c15a6afae40e' -d '{"approverId":"1"}'
     */
    @PostMapping(path = "/{communityId}/trackers/{trackerId}")
    public ResponseEntity<MeetingStatusDto> approveMeeting(@PathVariable Long communityId, @PathVariable String trackerId, @Valid @RequestBody MeetingApprovalRequestDto approvalRequest) {
        log.info("Received new meeting approval. CommunityId={} Request={}", communityId, approvalRequest);
        ApprovalMeetingInvite approval = new ApprovalMeetingInvite(
                new CommunityId(communityId),
                new TrackerId(UUID.fromString(trackerId)),
                new NeighbourgId(approvalRequest.getApproverId()));

        meetingScheduler.fetchMeetingInviteAndNotifyScheduledMeetingForApproval(approval);

        return ResponseEntity
                .ok()
                .body(MeetingStatusDto.builder()
                        .description("Association meeting approval has been requested.").build());
    }

    /*
        curl -v -H "Content-Type: application/json" -X POST 'http://localhost:8288/communities/resendinvite' -d '{"communityId":"1", "trackerId":"62a60a71-fb86-49b3-af0d-ed796020d9df", "action":"FOR_APPROVAL"}'
        curl -v -H "Content-Type: application/json" -X POST 'http://localhost:8288/communities/resendinvite' -d '{"communityId":"1", "trackerId":"62a60a71-fb86-49b3-af0d-ed796020d9df", "action":"TO_PARTICIPANTS"}'
        curl -v --user admin:admin -H "Content-Type: application/json" -X POST 'http://localhost:8288/communities/resendinvite' -d '{"communityId":"1", "trackerId":"62a60a71-fb86-49b3-af0d-ed796020d9df", "action":"TO_PARTICIPANTS"}'
     */
    @PostMapping(path = "/resendinvite")
    public ResponseEntity<MeetingStatusDto> resendMeetingInvite(@Valid @RequestBody ResendInviteDto resendInvite) {
        Long communityId = resendInvite.getCommunityId();
        String trackerId = resendInvite.getTrackerId();
        log.info("Received resend invite request for {}. CommunityId={} TrackingId={}", resendInvite.getAction(), communityId, trackerId);

        meetingScheduler.resendMeetingInvite(
                new ResendMeetingInviteRequest(
                        new CommunityId(communityId), new TrackerId(UUID.fromString(trackerId)),
                        ResendType.getTypeFrom(resendInvite.getAction().toString())));

        return ResponseEntity
                .ok(MeetingStatusDto.builder()
                .description("Association meeting resend invite has been successful.").build());
    }

    @GetMapping(path = "/lookup", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> lookup() {
        return ResponseEntity.ok("Lookup successful!");
    }

}

