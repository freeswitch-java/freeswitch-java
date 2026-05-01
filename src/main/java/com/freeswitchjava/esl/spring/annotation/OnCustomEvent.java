package com.freeswitchjava.esl.spring.annotation;

import java.lang.annotation.*;

/**
 * Handles FreeSWITCH {@code CUSTOM} events matching a specific {@code Event-Subclass}.
 *
 * <pre>{@code
 * @OnCustomEvent("conference::maintenance")
 * public void onConference(EslEvent event) {
 *     String action = event.getHeader("Action");
 * }
 *
 * @OnCustomEvent("sofia::register")
 * public void onRegister(EslEvent event) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnCustomEvent {
    /** The {@code Event-Subclass} value to match (e.g. {@code "conference::maintenance"}). */
    String value();
}
