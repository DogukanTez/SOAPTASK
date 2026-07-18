package com.dogukantez.soaptask.client;

import com.dogukantez.soaptask.exception.*;
import com.dogukantez.soaptask.generated.CheckVat;
import com.dogukantez.soaptask.generated.CheckVatResponse;
import com.dogukantez.soaptask.model.VatValidationResult;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.support.MarshallingUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViesClient {

    private final WebServiceTemplate viesWebServiceTemplate;
    private final Jaxb2Marshaller viesMarshaller;

    public VatValidationResult checkVat(String countryCode, String vatNumber) {

        CheckVat request = new CheckVat();
        request.setCountryCode(countryCode);
        request.setVatNumber(vatNumber);

        log.debug("Sending VIES query: {} {}", countryCode, vatNumber);

        try {
            return viesWebServiceTemplate.sendAndReceive(
                    message -> MarshallingUtils.marshal(viesMarshaller, request, message),
                    this::extractResult);

        } catch (SoapFaultClientException e) {
            throw mapFault(ViesFaultCode.from(e.getFaultStringOrReason())); // framework faultu kendi yakalarsa buraya girer.

        } catch (WebServiceIOException e) {
            log.warn("VIES access error: {}", e.getMessage());
            throw new ViesTemporaryException("Could not reach VIES service", e);

        } catch (WebServiceClientException e) {
            log.error("VIES technical fault", e);
            throw new ViesTechnicalException("VIES call failed", e);
        }
    }

    private VatValidationResult extractResult(WebServiceMessage message) throws IOException {

        if (message instanceof SoapMessage soapMessage) {
            SoapBody body = soapMessage.getSoapBody();
            if (body.hasFault()) {
                SoapFault fault = body.getFault();
                throw mapFault(ViesFaultCode.from(fault.getFaultStringOrReason()));
            }
        }

        Object payload = MarshallingUtils.unmarshal(viesMarshaller, message);

        if (!(payload instanceof CheckVatResponse response)) {
            log.error("Unexpected VIES response type: {}",
                    payload == null ? "null" : payload.getClass().getName());
            throw new ViesTechnicalException("Unexpected response type from VIES", null);
        }

        return toResult(response);
    }

    private ViesException mapFault(ViesFaultCode code) {
        log.warn("VIES SOAP fault: {}", code);

        if (code == ViesFaultCode.INVALID_INPUT) {
            return new ViesInvalidInputException("VIES refused input: " + code, null);
        }
        if (code.isTemporary()) {
            return new ViesTemporaryException("VIES temporarily unavailable: " + code, null);
        }
        return new ViesTechnicalException("Unexpected VIES fault: " + code, null);
    }

    private VatValidationResult toResult(CheckVatResponse response) {
        return VatValidationResult.builder()
                .valid(response.isValid())
                .countryCode(response.getCountryCode())
                .vatNumber(response.getVatNumber())
                .name(unwrap(response.getName()))
                .address(unwrap(response.getAddress()))
                .requestDate(toLocalDate(response.getRequestDate()))
                .build();
    }

    private String unwrap(JAXBElement<String> element) {
        return element == null ? null : element.getValue();
    }

    private LocalDate toLocalDate(XMLGregorianCalendar calendar) {
        return calendar == null
                ? null
                : calendar.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }
}