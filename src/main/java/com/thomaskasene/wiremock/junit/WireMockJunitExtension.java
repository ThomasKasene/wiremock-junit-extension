package com.thomaskasene.wiremock.junit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
class WireMockJunitExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private WireMockStubsConfiguration configuration;
    private WireMockServer server;
    private Map<Class<? extends WireMockStub>, WireMockStub> stubs;
    private Integer storedPort;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        WireMockStubs annotation = findAnnotation(context);

        ExtensionContext.Store store = context.getRoot().getStore(ExtensionContext.Namespace.create(getClass()));
        String storedPortKey = annotation.toString() + " port";
        storedPort = store.get(storedPortKey, Integer.class);

        createConfiguration(annotation.configClass());
        createServer();
        startServer();

        store.put(storedPortKey, server.port());

        createStubs(annotation.value());
        for (WireMockStub wireMockStub : stubs.values()) {
            wireMockStub.beforeAll();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        injectStubsIntoTestClass(context.getRequiredTestInstance(), context.getRequiredTestInstance().getClass());

        configuration.resetBeforeEachTest(stubs.values());

        for (WireMockStub wireMockStub : stubs.values()) {
            wireMockStub.beforeEach();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        for (WireMockStub wireMockStub : stubs.values()) {
            wireMockStub.afterEach();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        for (WireMockStub wireMockStub : stubs.values()) {
            wireMockStub.afterAll();
        }

        stopServer();
    }

    private WireMockStubs findAnnotation(ExtensionContext context) {
        WireMockStubs annotation = context.getTestClass().get().getAnnotation(WireMockStubs.class);

        if (annotation != null) {
            return annotation;
        } else {
            throw new RuntimeException("Failed to find the @" + WireMockStubs.class.getSimpleName() + "-annotation" +
                    " for the current test context");
        }
    }

    private void createConfiguration(Class<? extends WireMockStubsConfiguration> configClass) {
        try {
            configuration = configClass.getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to create the configuration " + configClass.getSimpleName() +
                    ". Does it have a public default constructor?", exception);
        }
    }

    private void createServer() {
        if (server == null) {
            if (storedPort == null) {
                server = new WireMockServer(configuration.getWireMockConfiguration());
            } else {
                WireMockConfiguration wireMockConfiguration = configuration.getWireMockConfiguration();
                wireMockConfiguration.port(storedPort);
                server = new WireMockServer(wireMockConfiguration);
            }
        }
    }

    private void startServer() {
        if (!server.isRunning()) {
            server.start();
            WireMock.configureFor(new WireMock(server));
            exposePortInSystemProperties();

            log.info("Started the WireMock server on port {}", server.port());
        }
    }

    private void exposePortInSystemProperties() {
        if (configuration.getPortPropertyName() != null) {
            System.setProperty(configuration.getPortPropertyName(), String.valueOf(server.port()));
        }
    }

    private void createStubs(Class<? extends WireMockStub>[] stubClasses) {
        stubs = new LinkedHashMap<>(stubClasses.length);

        for (Class<? extends WireMockStub> stubClass : stubClasses) {
            WireMockStub stub = createStub(stubClass);
            stubs.put(stubClass, stub);
        }
    }

    private WireMockStub createStub(Class<? extends WireMockStub> stubClass) {
        try {
            return stubClass.getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to create the stub " + stubClass.getSimpleName() + ". Does it have" +
                    " a public default constructor?", exception);
        }
    }

    private void injectStubsIntoTestClass(Object testInstance, Class<?> hierarchyLevelType) {
        for (Field field : hierarchyLevelType.getDeclaredFields()) {
            if (stubs.containsKey(field.getType())) {
                injectStubIntoTestClass(testInstance, hierarchyLevelType, field);
            }
        }

        if (hierarchyLevelType.getDeclaredAnnotation(WireMockStubs.class) == null) {
            injectStubsIntoTestClass(testInstance, hierarchyLevelType.getSuperclass());
        }
    }

    private void injectStubIntoTestClass(Object testInstance, Class<?> hierarchyLevelType, Field field) {
        WireMockStub stub = stubs.get(field.getType());
        boolean wasPreviouslyAccessible = field.canAccess(testInstance);
        field.setAccessible(true);
        try {
            field.set(testInstance, stub);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Failed to assign stub instance " + stub + " to the field "
                    + hierarchyLevelType.getSimpleName() + "." + field.getName(), exception);
        }
        field.setAccessible(wasPreviouslyAccessible);
    }

    private void stopServer() {
        if (server.isRunning()) {
            int port = server.port();
            server.stop();
            WireMock.configure();

            log.info("Stopped the WireMock server that was running on port {}", port);
        }
    }
}
