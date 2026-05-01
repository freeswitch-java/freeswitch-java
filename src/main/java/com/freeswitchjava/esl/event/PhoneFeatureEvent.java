package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired for phone feature events (e.g. call waiting, do-not-disturb).
 * Event-Name: {@code PHONE_FEATURE} or {@code PHONE_FEATURE_SUBSCRIBE}
 */
public class PhoneFeatureEvent extends EslEvent {

    public PhoneFeatureEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** SIP user associated with the feature event. */
    public String getSipUser() {
        return getHeader("sip-user");
    }

    /** Feature name, e.g. {@code "call-waiting"}, {@code "dnd"}. */
    public String getFeatureName() {
        return getHeader("feature-name");
    }

    /** Feature status: {@code "on"} or {@code "off"}. */
    public String getFeatureStatus() {
        return getHeader("feature-status");
    }
}
