package com.dogukantez.soaptask.service;

import com.dogukantez.soaptask.client.ViesClient;
import com.dogukantez.soaptask.dto.Decision;
import com.dogukantez.soaptask.dto.InvoiceValidationResponse;
import com.dogukantez.soaptask.model.Invoice;
import com.dogukantez.soaptask.model.VatValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceValidationService {

    private final ViesClient viesClient;

    public InvoiceValidationResponse validate(Invoice invoice) {

        log.info("Invoice validation starting for: {}", invoice.getInvoiceNumber());

        VatValidationResult seller = viesClient.checkVat(invoice.getSellerCountryCode(), invoice.getSellerVatNumber());

        VatValidationResult buyer = viesClient.checkVat(invoice.getBuyerCountryCode(), invoice.getBuyerVatNumber());

        Decision decision = (seller.isValid() && buyer.isValid())
                ? Decision.ISSUABLE
                : Decision.NOT_ISSUABLE;

        String reason = buildReason(seller, buyer);

        log.info("Invoice {} conclusion: {}{}",
                invoice.getInvoiceNumber(), decision, reason == null ? "" : " - " + reason);

        return InvoiceValidationResponse.builder()
                .invoiceNumber(invoice.getInvoiceNumber())
                .decision(decision)
                .reason(reason)
                .seller(seller)
                .buyer(buyer)
                .build();
    }

    private String buildReason(VatValidationResult seller, VatValidationResult buyer) {
        if (!seller.isValid() && !buyer.isValid()) {
            return "Invoice cannot be issued: Seller and buyer VAT numbers are invalid.";
        }
        if (!seller.isValid()) {
            return "Invoice cannot be issued: seller's VAT number is invalid.";
        }
        if (!buyer.isValid()) {
            return "Invoice cannot be issued: buyer's VAT number is invalid.";
        }
        return null;
    }
}
