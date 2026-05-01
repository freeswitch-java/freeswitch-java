package com.freeswitchjava.esl.spring.annotation;

import com.freeswitchjava.esl.spring.FreeswitchEslAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables FreeSWITCH ESL auto-configuration explicitly.
 *
 * <p>Not required when using Spring Boot — auto-configuration activates automatically
 * when {@code freeswitch.esl.host} is set. Use this annotation in plain Spring
 * (non-Boot) applications or when you need explicit control over activation.
 *
 * <pre>{@code
 * @Configuration
 * @EnableFreeswitchEsl
 * public class AppConfig { }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(FreeswitchEslAutoConfiguration.class)
public @interface EnableFreeswitchEsl {}
