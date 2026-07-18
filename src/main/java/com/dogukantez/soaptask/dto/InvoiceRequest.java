package com.dogukantez.soaptask.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceRequest {

    @NotBlank
    private String invoiceNumber;

    @NotBlank
    private String sellerCountryCode;

    @NotBlank
    private String sellerVatNumber;

    @NotBlank
    private String buyerCountryCode;

    @NotBlank
    private String buyerVatNumber;
}
