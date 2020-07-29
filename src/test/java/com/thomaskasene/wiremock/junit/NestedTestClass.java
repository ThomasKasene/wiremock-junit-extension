package com.thomaskasene.wiremock.junit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Nested
@TestInstance(PER_CLASS)
public @interface NestedTestClass {
}
