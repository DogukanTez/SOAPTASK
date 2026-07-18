package com.dogukantez.soaptask.client;

import com.dogukantez.soaptask.exception.ViesInvalidInputException;
import com.dogukantez.soaptask.exception.ViesTechnicalException;
import com.dogukantez.soaptask.exception.ViesTemporaryException;
import com.dogukantez.soaptask.model.VatValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.test.client.MockWebServiceServer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.ws.test.client.RequestMatchers.payload;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;
import static org.springframework.ws.test.client.ResponseCreators.withServerOrReceiverFault;

@SpringBootTest
class ViesClientTest {

    private static final String NS = "urn:ec.europa.eu:taxud:vies:services:checkVat:types";

    @Autowired
    private ViesClient viesClient;

    @Autowired
    private WebServiceTemplate viesWebServiceTemplate;

    private MockWebServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockWebServiceServer.createServer(viesWebServiceTemplate);
    }

    @Test
    void shouldReturnResultForValidVatNumber() throws Exception {
        mockServer.expect(payload(anyCheckVat()))
                .andRespond(withPayload(resource("""
                        <checkVatResponse xmlns="%s">
                            <countryCode>DE</countryCode>
                            <vatNumber>129273398</vatNumber>
                            <requestDate>2026-07-18</requestDate>
                            <valid>true</valid>
                            <name>Test GmbH</name>
                            <address>Berlin</address>
                        </checkVatResponse>""".formatted(NS))));

        VatValidationResult result = viesClient.checkVat("DE", "129273398");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getCountryCode()).isEqualTo("DE");
        assertThat(result.getVatNumber()).isEqualTo("129273398");
        assertThat(result.getName()).isEqualTo("Test GmbH");
        assertThat(result.getAddress()).isEqualTo("Berlin");
        assertThat(result.getRequestDate()).isEqualTo(LocalDate.of(2026, 7, 18));
        mockServer.verify();
    }

    // valid false senaryosu teknik hata değil, normal iş akışı
    @Test
    void shouldNotThrowWhenVatNumberIsNotRegistered() throws Exception {
        mockServer.expect(payload(anyCheckVat()))
                .andRespond(withPayload(resource("""
                        <checkVatResponse xmlns="%s">
                            <countryCode>DE</countryCode>
                            <vatNumber>129273398</vatNumber>
                            <requestDate>2026-07-18</requestDate>
                            <valid>false</valid>
                        </checkVatResponse>""".formatted(NS))));

        VatValidationResult result = viesClient.checkVat("DE", "129273398");

        assertThat(result.isValid()).isFalse();
        // name ve address opsiyonel, gelmediğinde null kalmalı
        assertThat(result.getName()).isNull();
        assertThat(result.getAddress()).isNull();
        mockServer.verify();
    }

    // input hatası, tekrar denenmeyecek.
    @Test
    void shouldMapInvalidInputFaultToPermanentError() throws Exception {
        mockServer.expect(payload(anyCheckVat()))
                .andRespond(withServerOrReceiverFault("INVALID_INPUT", Locale.ENGLISH));

        assertThatThrownBy(() -> viesClient.checkVat("DE", "129273398"))
                .isInstanceOf(ViesInvalidInputException.class)
                .hasMessageContaining("INVALID_INPUT");
    }

    // Geçici hata retry tetiklemeli.
    @Test
    void shouldMapMemberStateUnavailableFaultToTemporaryError() throws Exception {
        mockServer.expect(payload(anyCheckVat()))
                .andRespond(withServerOrReceiverFault("MS_UNAVAILABLE", Locale.ENGLISH));

        assertThatThrownBy(() -> viesClient.checkVat("DE", "129273398"))
                .isInstanceOf(ViesTemporaryException.class)
                .hasMessageContaining("MS_UNAVAILABLE");
    }

    // XSD doğrulaması, zorunlu alanlar eksik
    @Test
    void shouldRejectResponseThatViolatesSchema() throws Exception {
        mockServer.expect(payload(anyCheckVat()))
                .andRespond(withPayload(resource("""
                        <checkVatResponse xmlns="%s">
                            <countryCode>DE</countryCode>
                        </checkVatResponse>""".formatted(NS))));

        assertThatThrownBy(() -> viesClient.checkVat("DE", "129273398"))
                .isInstanceOf(ViesTechnicalException.class);
    }

    private ByteArrayResource resource(String xml) {
        return new ByteArrayResource(xml.getBytes(StandardCharsets.UTF_8));
    }

    // payload eşleşmesi için sabit istek gövdesi
    private ByteArrayResource anyCheckVat() {
        return resource("""
                <checkVat xmlns="%s">
                    <countryCode>DE</countryCode>
                    <vatNumber>129273398</vatNumber>
                </checkVat>""".formatted(NS));
    }
}