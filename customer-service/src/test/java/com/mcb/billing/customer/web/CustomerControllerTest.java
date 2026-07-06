package com.mcb.billing.customer.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcb.billing.customer.exception.CustomerNotFoundException;
import com.mcb.billing.customer.exception.DuplicateEmailException;
import com.mcb.billing.customer.service.CustomerService;
import com.mcb.billing.customer.web.dto.CreateCustomerRequestDTO;
import com.mcb.billing.customer.web.dto.CustomerResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link CustomerController}. Loads only the MVC slice and
 * mocks {@link CustomerService}, so we test request/response handling, status
 * codes, JSON mapping, validation and the {@code @RestControllerAdvice} in isolation.
 */
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CustomerService customerService;

    private CustomerResponseDTO sampleResponse() {
        return new CustomerResponseDTO("C-00001", "Acme GmbH", "Max Mustermann", "acme@example.com",
                "030-1234", "Hauptstr 1", "10115", "Berlin", "DE", "DE123456789",
                true, Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
    }

    private CreateCustomerRequestDTO validRequest() {
        return new CreateCustomerRequestDTO("Acme GmbH", "acme@example.com",
                null, null, null, null, null, null, null);
    }

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(customerService.createCustomer(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/customers/C-00001"))
                .andExpect(jsonPath("$.customerNumber").value("C-00001"))
                .andExpect(jsonPath("$.companyName").value("Acme GmbH"));
    }

    @Test
    void createWithInvalidBodyReturns400WithFieldErrors() throws Exception {
        String badJson = "{\"companyName\":\"\",\"email\":\"not-an-email\"}";

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors.companyName").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void getReturns200() throws Exception {
        when(customerService.getByCustomerNumber("C-00001")).thenReturn(sampleResponse());

        mockMvc.perform(get("/customers/C-00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value("C-00001"));
    }

    @Test
    void getUnknownReturns404() throws Exception {
        when(customerService.getByCustomerNumber("C-99999"))
                .thenThrow(new CustomerNotFoundException("C-99999"));

        mockMvc.perform(get("/customers/C-99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Customer not found"));
    }

    @Test
    void createDuplicateEmailReturns409() throws Exception {
        when(customerService.createCustomer(any()))
                .thenThrow(new DuplicateEmailException("acme@example.com"));

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Email already in use"));
    }

    @Test
    void listAllReturns200() throws Exception {
        when(customerService.listAllCustomers()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerNumber").value("C-00001"));
    }

    @Test
    void deactivateReturns204() throws Exception {
        mockMvc.perform(delete("/customers/C-00001"))
                .andExpect(status().isNoContent());

        verify(customerService).deactivateCustomer("C-00001");
    }
}
