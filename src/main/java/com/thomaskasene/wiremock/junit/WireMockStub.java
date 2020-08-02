package com.thomaskasene.wiremock.junit;

/**
 * <p>This interface can be implemented and passed to the {@link WireMockStubs}-annotation to define stubs that can be
 * integrated with the JUnit lifecycle. Furthermore, any instances may be injected into the test class if their types
 * match any of the declared fields.
 *
 * <p>For example, given the following implementation:
 * <pre>
 * public class NameRegistryStub implements WireMockStub {
 *     public void addNames(String... names) {
 *         // ...
 *     }
 * }
 * </pre>
 * It can be injected into the test class and used like this:
 * <pre>
 * &#64;WireMockStubs(NameRegistryStub.class)
 * public class MyTest {
 *
 *     private NameRegistryStub nameRegistryStub;
 *
 *     &#64;Test
 *     void myStubTest() {
 *         nameRegistryStub.addNames("Doe, John");
 *     }
 * }
 * </pre>
 *
 */
public interface WireMockStub {

    default void reset() throws Exception {
    }

    default void beforeAll() throws Exception {
    }

    default void beforeEach() throws Exception {
    }

    default void afterEach() throws Exception {
    }

    default void afterAll() throws Exception {
    }
}
