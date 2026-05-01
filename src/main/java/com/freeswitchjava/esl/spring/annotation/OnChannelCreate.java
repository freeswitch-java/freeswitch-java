package com.freeswitchjava.esl.spring.annotation;

import com.freeswitchjava.esl.event.EventName;

import java.lang.annotation.*;

/** Handles {@code CHANNEL_CREATE} events. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnEslEvent(EventName.CHANNEL_CREATE)
public @interface OnChannelCreate {}
