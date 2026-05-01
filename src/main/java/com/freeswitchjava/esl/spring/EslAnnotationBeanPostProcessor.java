package com.freeswitchjava.esl.spring;

import com.freeswitchjava.esl.event.EventName;
import com.freeswitchjava.esl.inbound.InboundClient;
import com.freeswitchjava.esl.model.EslEvent;
import com.freeswitchjava.esl.spring.annotation.OnCustomEvent;
import com.freeswitchjava.esl.spring.annotation.OnEslEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.reflect.Method;

/**
 * Scans every Spring bean for methods annotated with {@link OnEslEvent} (directly or as a
 * meta-annotation such as {@code @OnChannelAnswer}) and {@link OnCustomEvent}, then registers
 * a listener on the {@link InboundClient} for each.
 *
 * <p>Registered automatically by {@link FreeswitchEslAutoConfiguration}. No user action needed.
 *
 * <h2>Supported method signatures</h2>
 * <ul>
 *   <li>{@code void onAnswer(ChannelAnswerEvent event)} — receives the specific event subtype</li>
 *   <li>{@code void onAny(EslEvent event)} — receives the base event type</li>
 *   <li>{@code void onHeartbeat()} — no parameter</li>
 * </ul>
 */
public class EslAnnotationBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(EslAnnotationBeanPostProcessor.class);

    private final InboundClient client;

    public EslAnnotationBeanPostProcessor(InboundClient client) {
        this.client = client;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);
            MergedAnnotations merged = MergedAnnotations.from(method,
                    MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);

            // ── @OnEslEvent (direct or as meta-annotation on typed aliases) ──────
            if (merged.isPresent(OnEslEvent.class)) {
                MergedAnnotation<OnEslEvent> ann = merged.get(OnEslEvent.class);
                EventName[] eventNames = ann.getEnumArray("value", EventName.class);
                for (EventName eventName : eventNames) {
                    registerMethodListener(bean, method, eventName, beanName);
                }
            }

            // ── @OnCustomEvent ────────────────────────────────────────────────────
            if (merged.isPresent(OnCustomEvent.class)) {
                String subclass = merged.get(OnCustomEvent.class).getString("value");
                registerCustomMethodListener(bean, method, subclass, beanName);
            }
        }

        return bean;
    }

    private void registerMethodListener(Object bean, Method method, EventName eventName, String beanName) {
        log.debug("[ESL] Registering @OnEslEvent({}) → {}.{}()",
                eventName, bean.getClass().getSimpleName(), method.getName());

        client.addEventListener(eventName, event -> invoke(bean, method, event));
    }

    private void registerCustomMethodListener(Object bean, Method method, String subclass, String beanName) {
        log.debug("[ESL] Registering @OnCustomEvent(\"{}\") → {}.{}()",
                subclass, bean.getClass().getSimpleName(), method.getName());

        // CUSTOM events arrive under EventName.CUSTOM; filter by subclass in the listener
        client.addEventListener(EventName.CUSTOM, event -> {
            if (subclass.equals(event.getEventSubclass())) {
                invoke(bean, method, event);
            }
        });
    }

    private void invoke(Object bean, Method method, EslEvent event) {
        try {
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 0) {
                method.invoke(bean);
            } else {
                // Pass event if the parameter type is assignable — allows typed subclasses
                if (params[0].isAssignableFrom(event.getClass())) {
                    method.invoke(bean, event);
                } else {
                    // Parameter type doesn't match this event's concrete class —
                    // pass the base EslEvent if the parameter accepts it
                    if (params[0].isAssignableFrom(EslEvent.class)) {
                        method.invoke(bean, event);
                    } else {
                        log.warn("[ESL] Method {}.{}() expects {} but received {} — skipping invocation",
                                bean.getClass().getSimpleName(), method.getName(),
                                params[0].getSimpleName(), event.getClass().getSimpleName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[ESL] Error invoking {}.{}() for event [{}] — {}",
                    bean.getClass().getSimpleName(), method.getName(),
                    event.getEventName(), e.getMessage(), e);
        }
    }
}
