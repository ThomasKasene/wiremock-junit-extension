package com.thomaskasene.wiremock.junit;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

@WireMockStubs
@DisplayName("When @WireMockStubs is present on method-level test instances")
@TestInstance(PER_METHOD)
public class WireMockStubsWithTestInstancePerMethodTest {

    @Test
    @DisplayName("the WireMock server is available")
    void serverIsAvailable() {
        assertDoesNotThrow(() -> WireMock.stubFor(get("/test")));
    }
}
