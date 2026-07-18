package com.dogukantez.soaptask.controller;

import com.dogukantez.soaptask.dto.InvoiceRequest;
import com.dogukantez.soaptask.dto.InvoiceValidationResponse;
import com.dogukantez.soaptask.mapper.InvoiceMapper;
import com.dogukantez.soaptask.service.InvoiceValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceValidationService invoiceValidationService;

    @PostMapping("/validate")
    public ResponseEntity<InvoiceValidationResponse> validate(
            @Valid @RequestBody InvoiceRequest request) {

        InvoiceValidationResponse response =
                invoiceValidationService.validate(InvoiceMapper.toInvoice(request));

        return ResponseEntity.ok(response);
    }
}