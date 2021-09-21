package com.thomaskasene.wiremock.junit;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When @WireMockStubs")
public class WireMockStubsTest {

    @WireMockStubs
    @NestedTestClass
    @DisplayName("with default properties is present")
    class WireMockStubsAnnotationPresentTest {

        @Test
        @DisplayName("the WireMock server is available")
        void serverIsAvailable() {
            assertDoesNotThrow(() -> WireMock.stubFor(get("/test")));
        }

        @Test
        @DisplayName("the wiremock.server.port system property is set to the running server's port")
        void wiremockServerPortProperty() {
            String propertyValue = System.getProperty("wiremock.server.port");

            assertNotNull(propertyValue);
        }
    }

    @WireMockStubs(configClass = CustomConfig.class)
    @NestedTestClass
    @DisplayName("with a configClass defined is present")
    class WireMockStubsAnnotationWithConfigClassPresentTest {

        @Test
        @DisplayName("the WireMock server is available")
        void serverIsAvailable() {
            assertDoesNotThrow(() -> WireMock.stubFor(get("/test")));
        }

        @Test
        @DisplayName("the custom system property is set to the running server's port")
        void wiremockServerPortProperty() {
            String propertyValue = System.getProperty("my.custom.property");

            assertNotNull(propertyValue);
        }

        @Test
        @DisplayName("the default server port system property is not defined")
        void defaultWiremockServerPortProperty() {
            String propertyValue = System.getProperty("wiremock.server.property");

            assertNull(propertyValue);
        }
    }

    @NestedTestClass
    @DisplayName("with default properties is present on a base class")
    class SubTest extends DefaultTestBase {

        @Test
        @DisplayName("the WireMock server is available")
        void serverIsAvailable() {
            assertDoesNotThrow(() -> WireMock.stubFor(get("/test")));
        }
    }

    @NestedTestClass
    @DisplayName("with a stub class defined is present on a base class")
    class WireMockStubsAnnotationWithStubClassPresentOnSuperClassTest extends StubInjectionPointTestBase {

        private TestStub stubFromThisClass;

        @Test
        @DisplayName("any fields matching the declared stub classes will be injected with the relevant stub instances")
        void injectedStub() {
            assertNotNull(stubFromBaseClass);
            assertNotNull(stubFromThisClass);
        }
    }

    @WireMockStubs(TestStub.class)
    @NestedTestClass
    @DisplayName("with a stub class defined is present")
    class WireMockStubsAnnotationWithStubClassPresentTest {

        private TestStub testStub;

        @Test
        @DisplayName("any fields matching the declared stub classes will be injected with the relevant stub instances")
        void injectedStub() {
            assertNotNull(testStub);
        }
    }

    @WireMockStubs
    class DefaultTestBase {
    }

    @WireMockStubs(TestStub.class)
    class StubInjectionPointTestBase {
        protected TestStub stubFromBaseClass;
    }

    public static class CustomConfig implements WireMockStubsConfiguration {
        @Override
        public String getPortPropertyName() {
            return "my.custom.property";
        }
    }

    public static class TestStub implements WireMockStub {
    }
}
