package io.openvidu.server.cdr;

import org.json.simple.JSONObject;

import io.openvidu.server.core.MediaOptions;
import io.openvidu.server.core.Participant;

public class CDREvent implements Comparable<CDREvent> {

	static final String SESSION_CREATED = "sessionCreated";
	static final String SESSION_DESTROYED = "sessionDestroyed";
	static final String PARTICIPANT_JOINED = "participantJoined";
	static final String PARTICIPANT_LEFT = "participantLeft";
	static final String CONNECTION_CREATED = "webrtcConnectionCreated";
	static final String CONNECTION_DESTROYED = "webrtcConnectionDestroyed";

	protected String eventName;
	protected String sessionId;
	private Participant participant;
	private MediaOptions mediaOptions;
	private String receivingFrom;
	private Long startTime;
	private Integer duration;
	protected Long timeStamp;

	public CDREvent(String eventName, CDREvent event) {
		this(eventName, event.participant, event.sessionId, event.mediaOptions, event.receivingFrom, event.startTime);
		this.duration = (int) (this.timeStamp - this.startTime / 1000);
	}

	public CDREvent(String eventName, String sessionId) {
		this.eventName = eventName;
		if ((sessionId.indexOf('/')) != -1) {
			this.sessionId = sessionId.substring(sessionId.lastIndexOf('/') + 1, sessionId.length());
		} else {
			this.sessionId = sessionId;
		}
		this.timeStamp = System.currentTimeMillis();
		this.startTime = this.timeStamp;
	}

	public CDREvent(String eventName, Participant participant, String sessionId) {
		this(eventName, sessionId);
		this.participant = participant;
		this.startTime = this.timeStamp;
	}

	public CDREvent(String eventName, Participant participant, String sessionId, MediaOptions mediaOptions,
			String receivingFrom, Long startTime) {
		this(eventName, sessionId);
		this.participant = participant;
		this.mediaOptions = mediaOptions;
		this.receivingFrom = receivingFrom;
		this.startTime = startTime;
	}

	public MediaOptions getMediaOptions() {
		return mediaOptions;
	}

	public String getParticipantPublicId() {
		return this.participant.getParticipantPublicId();
	}

	public String getReceivingFrom() {
		return this.receivingFrom;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String toString() {
		JSONObject json = new JSONObject();
		json.put("sessionId", this.sessionId);
		json.put("timestamp", this.timeStamp);

		if (this.participant != null) {
			json.put("participantId", this.participant.getParticipantPublicId());
		}
		if (this.mediaOptions != null) {
			json.put("connection", this.receivingFrom != null ? "INBOUND" : "OUTBOUND");
			json.put("audioEnabled", this.mediaOptions.audioActive);
			json.put("videoEnabled", this.mediaOptions.videoActive);
			if (this.mediaOptions.videoActive) {
				json.put("videoSource", this.mediaOptions.typeOfVideo);
			}
			if (this.receivingFrom != null) {
				json.put("receivingFrom", this.receivingFrom);
			}
		}
		if (this.duration != null) {
			json.put("startTime", this.startTime);
			json.put("endTime", this.timeStamp);
			json.put("duration", (this.timeStamp - this.startTime) / 1000);
		}

		JSONObject root = new JSONObject();
		root.put(this.eventName, json);

		return root.toString();
	}

	@Override
	public int compareTo(CDREvent other) {
		if (this.participant.equals(other.participant)) {
			if (this.receivingFrom != null && other.receivingFrom != null) {
				if (this.receivingFrom.equals(other.receivingFrom)) {
					return 0;
				} else {
					return 1;
				}
			} else {
				if (this.receivingFrom == null && other.receivingFrom == null) {
					return 0;
				} else {
					return 1;
				}
			}
		}
		return 1;
	}

}
