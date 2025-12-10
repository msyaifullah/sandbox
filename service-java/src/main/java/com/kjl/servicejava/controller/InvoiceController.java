package com.kjl.servicejava.controller;

import com.kjl.servicejava.model.InvoiceValidationResponse;
import com.kjl.servicejava.service.InvoiceValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InvoiceController {

    @Autowired
    private InvoiceValidationService invoiceValidationService;

    @PostMapping("/invoice/validate")
    public ResponseEntity<InvoiceValidationResponse> validateInvoice(@RequestBody String body) {
        InvoiceValidationResponse response = invoiceValidationService.validateInvoice(body);
        return ResponseEntity.ok(response);
    }
}
