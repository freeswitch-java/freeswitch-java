package com.freeswitchjava.esl.spring.annotation;

import com.freeswitchjava.esl.event.EventName;

import java.lang.annotation.*;

/** Handles {@code CHANNEL_BRIDGE} events. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnEslEvent(EventName.CHANNEL_BRIDGE)
public @interface OnChannelBridge {}
