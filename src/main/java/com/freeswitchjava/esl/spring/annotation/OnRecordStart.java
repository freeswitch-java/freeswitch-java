package com.freeswitchjava.esl.spring.annotation;

import com.freeswitchjava.esl.event.EventName;

import java.lang.annotation.*;

/** Handles {@code RECORD_START} events. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnEslEvent(EventName.RECORD_START)
public @interface OnRecordStart {}
