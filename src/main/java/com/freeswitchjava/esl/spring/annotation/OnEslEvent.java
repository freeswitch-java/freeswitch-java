package com.freeswitchjava.esl.spring.annotation;

import com.freeswitchjava.esl.event.EventName;

import java.lang.annotation.*;

/**
 * Marks a method as a FreeSWITCH ESL event handler for one or more event types.
 *
 * <p>The annotated method may have zero or one parameter. If one parameter is declared,
 * it must be assignable from the event type (e.g., {@code EslEvent} or a specific subclass
 * like {@code ChannelAnswerEvent}).
 *
 * <pre>{@code
 * @OnEslEvent(EventName.CHANNEL_ANSWER)
 * public void onAnswer(ChannelAnswerEvent event) { ... }
 *
 * @OnEslEvent({EventName.CHANNEL_ANSWER, EventName.CHANNEL_HANGUP})
 * public void onCallEvent(EslEvent event) { ... }
 * }</pre>
 *
 * <p>Prefer the typed convenience aliases ({@link OnChannelAnswer}, {@link OnChannelHangup},
 * {@link OnDtmf}, etc.) over this annotation for common events.
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnEslEvent {
    EventName[] value();
}
