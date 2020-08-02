# WireMock Extension for JUnit Jupiter

![Build master branch](https://github.com/ThomasKasene/wiremock-junit-extension/workflows/Build%20master%20branch/badge.svg?branch=master)

This module aims to provide a JUnit Jupiter extension that starts a WireMock server before the test suite starts
running, so that the rest of the test setup, such as Spring contexts as well as the tests themselves, will be able to
access it.

## Why not Spring Cloud Contract?

I've used it in the past, and while it's a good module it has a few problems that makes it difficult to use in certain
cases. The main problem is that it boots up the WireMock server as part of the Spring context setup, and there's no way
to be certain that it's running if a part of that setup needs to perform HTTP requests. A couple of real-world cases
are:
* Spring Cloud Vault attempts to fetch secrets from a remote secret service.
* Spring Security OAuth2 Resource Server attempts to configure itself via a OIDC Discovery-URL
  (`/.well-known/openid-configuration`).

## Prerequisites

* Your code base must be using Java 9 or later.
* You need to include JUnit Jupiter and WireMock to your dependencies as this module doesn't impose any transitive
 dependencies.

## Usage

If you use Maven, you should be able to use the following coordinates:

```xml
<dependency>
    <groupId>com.thomaskasene.wiremock</groupId>
    <artifactId>wiremock-junit-extension</artifactId>
    <version>${latest-release.version}</version>
</dependency>
```

Then you can simply add the `@WireMockStubs`-annotation to your test class:

```java
@WireMockStubs
public class MyWireMockTest {

    @Test
    void myFirstTest() {
        // ...
    }
}
```

The result is that a WireMock server will be started before the test context begins, and it will be torn down once the
test context finishes its work.

Additionally, the WireMock server's HTTP port will be exposed as a system property. This property's name is
`wiremock.server.port` by default to cater to existing Spring Cloud Contract-users, but it's possible to override it by
providing a custom implementation of `WireMockStubsConfiguration.getPortPropertyName`.

### The `WireMockStub`-interface

This is an optional feature of this tool. When implementations of this interface is registered with the `WireMockStubs`
annotation, an instance of each implementation is created and your test classes can have them automatically injected
into class-level variables. See the example snippet below, or the `WireMockStub`-class for more details.

```java
@WireMockStubs(NameRegistryStub.class)
public class MyExampleTest {
    private NameRegistryStub nameRegistryStub;

    @Test
    void myStubTest() {
        nameRegistryStub.addNames("Doe, John");
    }
}
```
