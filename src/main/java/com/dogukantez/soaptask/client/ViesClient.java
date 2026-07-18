package com.dogukantez.soaptask.client;

import com.dogukantez.soaptask.exception.*;
import com.dogukantez.soaptask.generated.CheckVat;
import com.dogukantez.soaptask.generated.CheckVatResponse;
import com.dogukantez.soaptask.model.VatValidationResult;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViesClient {

    private final WebServiceTemplate viesWebServiceTemplate;

    public VatValidationResult checkVat(String countryCode, String vatNumber) {

        CheckVat request = new CheckVat();
        request.setCountryCode(countryCode);
        request.setVatNumber(vatNumber);

        log.debug("VIES query sending: {}{}", countryCode, vatNumber);

        try {
            CheckVatResponse response =
                    (CheckVatResponse) viesWebServiceTemplate.marshalSendAndReceive(request);
            return toResult(response);

        } catch (SoapFaultClientException e) {
            throw mapFault(e);

        } catch (WebServiceIOException e) {
            // bağlantı kurulamadı veya cevap zamanında gelmediyse
            log.warn("VIES access error: {}", e.getMessage());
            throw new ViesTemporaryException("VIES service couldn't reach", e);

        } catch (WebServiceClientException e) {
            // XSD doğrulama hatası ve diğer client hataları
            log.error("VIES technical fault", e);
            throw new ViesTechnicalException("VIES call failed", e);
        }
    }

    private ViesException mapFault(SoapFaultClientException e) {
        ViesFaultCode code = ViesFaultCode.from(e.getFaultStringOrReason());
        log.warn("VIES SOAP fault: {}", code);

        if (code == ViesFaultCode.INVALID_INPUT) {
            return new ViesInvalidInputException("VIES refused input: " + code, e);
        }
        if (code.isTemporary()) {
            return new ViesTemporaryException("VIES temporary unavaible: " + code, e);
        }
        return new ViesTechnicalException("Unexpected VIES fault: " + code, e);
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
                : LocalDate.of(calendar.getYear(), calendar.getMonth(), calendar.getDay());
    }
}