package com.dogukantez.soaptask.mapper;

import com.dogukantez.soaptask.dto.InvoiceRequest;
import com.dogukantez.soaptask.model.Invoice;

public final class InvoiceMapper {

    private InvoiceMapper() {
    }

    public static Invoice toInvoice(InvoiceRequest request) {
        return Invoice.builder()
                .invoiceNumber(request.getInvoiceNumber())
                .sellerCountryCode(request.getSellerCountryCode())
                .sellerVatNumber(request.getSellerVatNumber())
                .buyerCountryCode(request.getBuyerCountryCode())
                .buyerVatNumber(request.getBuyerVatNumber())
                .build();
    }
}