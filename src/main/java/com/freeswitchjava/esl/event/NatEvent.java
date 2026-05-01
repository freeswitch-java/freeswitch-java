package com.freeswitchjava.esl.event;

import com.freeswitchjava.esl.model.EslEvent;

import java.util.Map;

/**
 * Fired when a NAT mapping is added or removed.
 * Event-Name: {@code NAT}
 */
public final class NatEvent extends EslEvent {

    public NatEvent(Map<String, String> headers, String body) {
        super(headers, body);
    }

    /** NAT type / action: {@code "add"} or {@code "del"}. */
    public String getNatType() {
        return getHeader("nat-type");
    }

    /** Protocol: {@code "tcp"} or {@code "udp"}. */
    public String getProto() {
        return getHeader("proto");
    }

    /** External port. */
    public String getExternalPort() {
        return getHeader("external-port");
    }

    /** Internal port. */
    public String getInternalPort() {
        return getHeader("internal-port");
    }
}
