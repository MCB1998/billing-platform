package com.mcb.billing.invoice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcb.billing.invoice.domain.InvoiceStatus;
import com.mcb.billing.invoice.exception.InvalidInvoiceStateException;
import com.mcb.billing.invoice.exception.InvoiceNotFoundException;
import com.mcb.billing.invoice.exception.UnknownCustomerException;
import com.mcb.billing.invoice.service.InvoiceService;
import com.mcb.billing.invoice.web.dto.CreateInvoiceItemDTO;
import com.mcb.billing.invoice.web.dto.CreateInvoiceRequestDTO;
import com.mcb.billing.invoice.web.dto.InvoiceItemResponseDTO;
import com.mcb.billing.invoice.web.dto.InvoiceResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link InvoiceController}: request/response handling, status
 * codes, validation and the {@code @RestControllerAdvice}. The service is mocked.
 */
@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    InvoiceService invoiceService;

    private InvoiceResponseDTO sample(InvoiceStatus status) {
        return new InvoiceResponseDTO("INV-00001", "C-00001", status, "EUR",
                new BigDecimal("349.99"), null, LocalDate.now().plusDays(30),
                List.of(new InvoiceItemResponseDTO("Consulting", 3,
                        new BigDecimal("100.00"), new BigDecimal("300.00"))),
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
    }

    private CreateInvoiceRequestDTO validRequest() {
        return new CreateInvoiceRequestDTO("C-00001", "EUR", LocalDate.now().plusDays(30),
                List.of(new CreateInvoiceItemDTO("Consulting", 3, new BigDecimal("100.00"))));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(invoiceService.createInvoice(any())).thenReturn(sample(InvoiceStatus.DRAFT));

        mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/invoices/INV-00001"))
                .andExpect(jsonPath("$.invoiceNumber").value("INV-00001"))
                .andExpect(jsonPath("$.totalAmount").value(349.99));
    }

    @Test
    void createWithoutItemsReturns400() throws Exception {
        String badJson = "{\"customerNumber\":\"C-00001\",\"dueDate\":\""
                + LocalDate.now().plusDays(30) + "\",\"items\":[]}";

        mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.items").exists());
    }

    @Test
    void createUnknownCustomerReturns422() throws Exception {
        when(invoiceService.createInvoice(any()))
                .thenThrow(new UnknownCustomerException("C-99999"));

        mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Unknown customer"));
    }

    @Test
    void issueReturns200() throws Exception {
        when(invoiceService.issueInvoice("INV-00001")).thenReturn(sample(InvoiceStatus.ISSUED));

        mockMvc.perform(post("/invoices/INV-00001/issue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISSUED"));
    }

    @Test
    void issueInvalidStateReturns409() throws Exception {
        when(invoiceService.issueInvoice("INV-00001"))
                .thenThrow(new InvalidInvoiceStateException("Only a DRAFT invoice can be issued"));

        mockMvc.perform(post("/invoices/INV-00001/issue"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid invoice state"));
    }

    @Test
    void getUnknownReturns404() throws Exception {
        when(invoiceService.getByInvoiceNumber("INV-99999"))
                .thenThrow(new InvoiceNotFoundException("INV-99999"));

        mockMvc.perform(get("/invoices/INV-99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listReturns200() throws Exception {
        when(invoiceService.listInvoices()).thenReturn(List.of(sample(InvoiceStatus.DRAFT)));

        mockMvc.perform(get("/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceNumber").value("INV-00001"));
    }
}
