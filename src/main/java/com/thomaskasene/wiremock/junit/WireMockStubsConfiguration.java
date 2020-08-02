package com.thomaskasene.wiremock.junit;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;

import java.util.Collection;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * <p>This interface is used to customize the WireMock server that's being created before a test class begins running
 * its tests.
 *
 * <h2>Usage:</h2>
 * <p>This interface needs to be implemented if there's a need to override the default settings for the WireMock server,
 * and then that class must be passed into the {@link WireMockStubs}-annotation on the test class that will use it.
 * Please note that the implementing class also needs to have a public default constructor.
 *
 * <p>Given the following implementation:
 * <pre>
 * public MyCustomConfig implements WireMockStubsConfiguration {
 *     // ...
 * }
 * </pre>
 * It can be used in the test class like:
 * <pre>
 * &#64;WireMockStubs(configClass = MyCustomConfig.class)
 * public class MyTest {
 *
 *     &#64;Test
 *     void myTestMethod() {
 *         // ...
 *     }
 * }
 * </pre>
 */
public interface WireMockStubsConfiguration {

    /**
     * Returns an instance of {@link WireMockConfiguration} which will be used to configure the WireMock server before
     * it's created.
     *
     * @return The {@link WireMockConfiguration} instance to use.
     */
    default WireMockConfiguration getWireMockConfiguration() {
        return wireMockConfig()
                .notifier(new Slf4jNotifier(true))
                .extensions(new ResponseTemplateTransformer(false))
                .dynamicPort();
    }

    /**
     * When the WireMock server starts, it starts on a port, and this port can be set in the system properties so that
     * other frameworks can access it. If this method returns {@code null}, the system property won't be set.
     *
     * @return The name of the system property that should contain the WireMock server's HTTP port.
     */
    default String getPortPropertyName() {
        return "wiremock.server.port";
    }

    /**
     * This method is run once before each test, and should generally be used to clear the WireMock context and any
     * {@link WireMockStub} implementations so they're ready for the next test.
     *
     * @param stubs A list of all the {{@link WireMockStub} instances that are available.
     *
     * @throws Exception If something goes wrong during the resetting. This will bubble up and get caught by JUnit,
     * which will stop the test.
     */
    default void resetBeforeEachTest(Collection<WireMockStub> stubs) throws Exception {
        WireMock.reset();

        for (WireMockStub stub : stubs) {
            stub.reset();
        }
    }
}
