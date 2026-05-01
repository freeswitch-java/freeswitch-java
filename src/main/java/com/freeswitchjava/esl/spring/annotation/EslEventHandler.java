package com.freeswitchjava.esl.spring.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Stereotype annotation marking a class as a FreeSWITCH ESL event handler.
 *
 * <p>Equivalent to {@code @Component} but communicates intent. Any method annotated
 * with {@link OnEslEvent}, {@link OnChannelAnswer}, {@link OnChannelHangup}, etc.
 * inside this class will be registered automatically.
 *
 * <pre>{@code
 * @EslEventHandler
 * public class CallHandler {
 *
 *     @OnChannelAnswer
 *     public void onAnswer(ChannelAnswerEvent event) { ... }
 *
 *     @OnChannelHangup
 *     public void onHangup(ChannelHangupEvent event) { ... }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface EslEventHandler {
    String value() default "";
}
