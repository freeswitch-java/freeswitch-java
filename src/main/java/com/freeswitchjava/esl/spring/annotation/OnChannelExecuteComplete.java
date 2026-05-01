package com.freeswitchjava.esl.spring.annotation;

import com.freeswitchjava.esl.event.EventName;

import java.lang.annotation.*;

/** Handles {@code CHANNEL_EXECUTE_COMPLETE} events. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnEslEvent(EventName.CHANNEL_EXECUTE_COMPLETE)
public @interface OnChannelExecuteComplete {}
