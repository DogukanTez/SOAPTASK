package com.dogukantez.soaptask.dto;

import com.dogukantez.soaptask.model.VatValidationResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceValidationResponse {

    private String invoiceNumber;

    private Decision decision;

    private String reason;   // NOT_ISSUABLE değil ise null

    private VatValidationResult seller;
    private VatValidationResult buyer;

    public boolean isIssuable() {
        return decision == Decision.ISSUABLE;  // kolay kontrol için
    }
}