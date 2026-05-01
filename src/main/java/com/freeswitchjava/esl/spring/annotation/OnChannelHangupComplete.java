package com.freeswitchjava.esl.spring.annotation;

import com.freeswitchjava.esl.event.EventName;

import java.lang.annotation.*;

/** Handles {@code CHANNEL_HANGUP_COMPLETE} events. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnEslEvent(EventName.CHANNEL_HANGUP_COMPLETE)
public @interface OnChannelHangupComplete {}
