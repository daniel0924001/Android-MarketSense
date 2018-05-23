package com.idroi.marketsense.data;

import com.idroi.marketsense.Logging.MSLog;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by daniel.hsieh on 2018/5/12.
 */

public class Event {

    private static final String USER_ID = "user_id";
    private static final String EVENT = "event";
    private static final String EVENT_CONTENT = "event_content";
    private static final String EVENT_DETAIL = "event_detail";
    private static final String EVENT_TYPE = "event_type";
    private static final String EVENT_VALUE = "event_value";
    private static final String EVENT_TARGET = "event_target";
    private static final String EVENT_CREATED_TS = "created_ts";

    public static final String EVENT_VOTING = "voting";
    public static final String EVENT_TARGET_STOCK = "stock";
    public static final String EVENT_TARGET_NEWS = "news";

    private String mId;
    private String mEvent;
    private String mEventContent;
    private String mEventType;
    private Object mEventValue;
    private String mEventDetail;
    private String mEventTarget;
    private String mEventCreatedTs;

    public Event() {
    }

    public Event setId(String id) {
        mId = id;
        return this;
    }

    public Event setEvent(String event) {
        mEvent = event;
        return this;
    }

    public Event setEventContent(String content) {
        mEventContent = content;
        return this;
    }

    public Event setEventType(String type) {
        mEventType = type;
        return this;
    }

    public Event setEventValue(Object value) {
        mEventValue = value;
        return this;
    }

    public Event setEventDetail(String detail) {
        mEventDetail = detail;
        return this;
    }

    public Event setEventTarget(String target) {
        mEventTarget = target;
        return this;
    }

    public Event setEventCreatedTs(String timestamp) {
        mEventCreatedTs = timestamp;
        return this;
    }

    public String getEvent() {
        return mEvent;
    }

    public String getEventContent() {
        return mEventContent;
    }

    public String getEventType() {
        return mEventType;
    }

    public Object getEventValue() {
        return mEventValue;
    }

    public String getEventDetail() {
        return mEventDetail;
    }

    public String getEventTarget() {
        return mEventTarget;
    }

    public String getEventCreatedTs() {
        return mEventCreatedTs;
    }

    @Override
    public String toString() {
        return String.format(
                "Event: %s\nEventContent: %s\nEventType: %s\nEventDetail: %s\nEventTarget: %s\nEventCreatedTs: %s",
                mEvent, mEventContent, mEventType, mEventDetail, mEventTarget, mEventCreatedTs);
    }

    public static Event JsonObjectToEvent(JSONObject jsonObject) {
        Event event = new Event();
        Iterator<String> iterator = jsonObject.keys();
        while(iterator.hasNext()) {
            String key = iterator.next();
            try {
                switch (key) {
                    case USER_ID:
                        event.setId(jsonObject.optString(key));
                        break;
                    case EVENT:
                        event.setEvent(jsonObject.optString(EVENT));
                        break;
                    case EVENT_CONTENT:
                        event.setEventContent(jsonObject.optString(EVENT_CONTENT));
                        break;
                    case EVENT_TYPE:
                        event.setEventType(jsonObject.optString(EVENT_TYPE));
                        break;
                    case EVENT_VALUE:
                        event.setEventValue(jsonObject.opt(EVENT_VALUE));
                        break;
                    case EVENT_TARGET:
                        event.setEventTarget(jsonObject.optString(EVENT_TARGET));
                        break;
                    case EVENT_DETAIL:
                        event.setEventDetail(jsonObject.optString(EVENT_DETAIL));
                        break;
                    case EVENT_CREATED_TS:
                        event.setEventCreatedTs(jsonObject.optString(EVENT_CREATED_TS));
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                MSLog.e(e.toString());
            }
        }
        return event;
    }
}
