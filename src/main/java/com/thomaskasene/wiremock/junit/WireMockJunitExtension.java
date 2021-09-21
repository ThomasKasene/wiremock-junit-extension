package com.thomaskasene.wiremock.junit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Extension;
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

    @Override
    @SuppressWarnings("unchecked")
    public void beforeAll(ExtensionContext context) throws Exception {
        WireMockStubs annotation = findAnnotation(context);

        ExtensionContext.Store store = context.getRoot().getStore(ExtensionContext.Namespace.create(getClass()));

        String storedServerKey = annotation + " server";
        server = store.get(storedServerKey, WireMockServer.class);

        String storedStubsKey = annotation + " stubs";
        stubs = store.get(storedStubsKey, Map.class);

        createStubs(annotation.value());

        createConfiguration(annotation.configClass());
        createServer();
        startServer();

        store.put(storedServerKey, server);
        store.put(storedStubsKey, stubs);

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

    private void createStubs(Class<? extends WireMockStub>[] stubClasses) {
        if (stubs == null) {
            stubs = new LinkedHashMap<>(stubClasses.length);

            for (Class<? extends WireMockStub> stubClass : stubClasses) {
                WireMockStub stub = createStub(stubClass);
                stubs.put(stubClass, stub);
            }
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
            WireMockConfiguration wireMockConfiguration = configuration.getWireMockConfiguration();
            configureWithStubsAsExtensions(wireMockConfiguration);
            server = new WireMockServer(wireMockConfiguration);
        }
    }

    private void configureWithStubsAsExtensions(WireMockConfiguration wireMockConfiguration) {
        stubs.values().stream()
                .filter(wireMockStub -> wireMockStub instanceof Extension)
                .map(wireMockStub -> (Extension) wireMockStub)
                .forEach(wireMockConfiguration::extensions);
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
}
