package com.dogukantez.soaptask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceRequest {

    @NotBlank
    private String invoiceNumber;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{2}$", message = "country code must be ISO format.") // iso format hataları için
    private String sellerCountryCode;

    @NotBlank
    @Pattern(regexp = "^[0-9A-Za-z+*.]{2,12}$", message = "Invalid KDV number format") // kdv paterni dokümanda belirtildiği üzere
    private String sellerVatNumber;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{2}$", message = "country code must be ISO format.")
    private String buyerCountryCode;

    @NotBlank
    @Pattern(regexp = "^[0-9A-Za-z+*.]{2,12}$", message = "Invalid KDV number format")
    private String buyerVatNumber;
}
