package com.dogukantez.soaptask.service;

import com.dogukantez.soaptask.client.ViesClient;
import com.dogukantez.soaptask.dto.Decision;
import com.dogukantez.soaptask.dto.InvoiceValidationResponse;
import com.dogukantez.soaptask.exception.ViesTemporaryException;
import com.dogukantez.soaptask.model.Invoice;
import com.dogukantez.soaptask.model.VatValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceValidationServiceTest {

    @Mock
    private ViesClient viesClient;

    @InjectMocks
    private InvoiceValidationService invoiceValidationService;

    @Test
    void shouldAllowInvoiceWhenBothPartiesAreValid() {
        when(viesClient.checkVat("DE", "111111111")).thenReturn(result(true, "Seller"));
        when(viesClient.checkVat("FR", "222222222")).thenReturn(result(true, "Buyer"));

        InvoiceValidationResponse response = invoiceValidationService.validate(invoice());

        assertThat(response.getDecision()).isEqualTo(Decision.ISSUABLE);
        assertThat(response.getReason()).isNull();
        assertThat(response.getSeller().getName()).isEqualTo("Seller");
        assertThat(response.getBuyer().getName()).isEqualTo("Buyer");
    }

    @Test
    void shouldRejectInvoiceWhenBuyerIsInvalid() {
        when(viesClient.checkVat("DE", "111111111")).thenReturn(result(true, "Seller"));
        when(viesClient.checkVat("FR", "222222222")).thenReturn(result(false, null));

        InvoiceValidationResponse response = invoiceValidationService.validate(invoice());

        assertThat(response.getDecision()).isEqualTo(Decision.NOT_ISSUABLE);
        assertThat(response.getReason()).contains("buyer");
    }

    @Test
    void shouldRejectInvoiceWhenSellerIsInvalid() {
        when(viesClient.checkVat("DE", "111111111")).thenReturn(result(false, null));
        when(viesClient.checkVat("FR", "222222222")).thenReturn(result(true, "Buyer"));

        InvoiceValidationResponse response = invoiceValidationService.validate(invoice());

        assertThat(response.getDecision()).isEqualTo(Decision.NOT_ISSUABLE);
        assertThat(response.getReason()).contains("seller");
    }

    @Test
    void shouldRejectInvoiceWhenBothPartiesAreInvalid() {
        when(viesClient.checkVat(anyString(), anyString())).thenReturn(result(false, null));

        InvoiceValidationResponse response = invoiceValidationService.validate(invoice());

        assertThat(response.getDecision()).isEqualTo(Decision.NOT_ISSUABLE);
        assertThat(response.getReason()).contains("Seller and buyer");
    }

    // satıcı geçersiz olsa bile alıcı sorgusu atlanmıyor, kullanıcı tek seferde iki tarafın da durumunu görsün diye
    @Test
    void shouldQueryBothPartiesEvenWhenSellerIsInvalid() {
        when(viesClient.checkVat(anyString(), anyString())).thenReturn(result(false, null));

        invoiceValidationService.validate(invoice());

        verify(viesClient).checkVat("DE", "111111111");
        verify(viesClient).checkVat("FR", "222222222");
        verifyNoMoreInteractions(viesClient);
    }

    // teknik hata iş sonucuna dönüştürülmüyor, üst katmana olduğu gibi geçiyor
    // VIES'e ulaşılamaması bir karar değil. Servis bu hatayı
// yakalayıp NOT_ISSUABLE'a çevirmemeli, exception yukarı çıkmalı.
    @Test
    void shouldPropagateTechnicalErrors() {
        when(viesClient.checkVat("DE", "111111111"))
                .thenThrow(new ViesTemporaryException("VIES unavailable", null));

        assertThatThrownBy(() -> invoiceValidationService.validate(invoice()))
                .isInstanceOf(ViesTemporaryException.class);
    }

    private Invoice invoice() {
        return Invoice.builder()
                .invoiceNumber("INV-009")
                .sellerCountryCode("DE")
                .sellerVatNumber("111111111")
                .buyerCountryCode("FR")
                .buyerVatNumber("222222222")
                .build();
    }

    private VatValidationResult result(boolean valid, String name) {
        return VatValidationResult.builder()
                .valid(valid)
                .name(name)
                .requestDate(LocalDate.now())
                .build();
    }
}