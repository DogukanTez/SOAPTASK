package com.dogukantez.soaptask.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    private String invoiceNumber;

    private String sellerCountryCode;
    private String sellerVatNumber;

    private String buyerCountryCode;
    private String buyerVatNumber;
}