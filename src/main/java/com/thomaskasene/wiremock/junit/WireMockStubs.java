package com.thomaskasene.wiremock.junit;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * <p>Used to set up a WireMock server before all tests in the annotated class runs.
 *
 * <p>Important: This annotation uses the {@link ExtendWith}-annotation from JUnit to start the server bootstrapping,
 * which means that the order of this annotation in relation to other {@link ExtendWith}-annotations is important to
 * get right, if those other JUnit extensions rely on the WireMock server being up and running.
 *
 * <p>For example, the following cases are not equal, and the second might fail if the Spring context attempts to make
 * any HTTP-requests during context startup:
 * <pre>
 * &#64;WireMockStubs
 * &#64;ExtendWith(SpringExtension.class)
 * public class MyTest {
 *     // ...
 * }
 *
 * &#64;ExtendWith(SpringExtension.class)
 * &#64;WireMockStubs
 * public class MyTest {
 *     // ...
 * }
 * </pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@ExtendWith(WireMockJunitExtension.class)
public @interface WireMockStubs {

    /**
     * A list of {@link WireMockStub} implementations that will be instantiated during annotation processing. See
     * {@link WireMockStub} for more details on how to use these.
     *
     * @return A list of the {@link WireMockStub} implementations to use.
     */
    Class<? extends WireMockStub>[] value() default {};

    /**
     * The {@link WireMockStubsConfiguration} interface can be overridden to provide a custom configuration for the
     * WireMock server that's being created.
     *
     * @return The {@link WireMockStubsConfiguration} implementation to use.
     */
    Class<? extends WireMockStubsConfiguration> configClass() default DefaultWireMockStubsConfiguration.class;
}
