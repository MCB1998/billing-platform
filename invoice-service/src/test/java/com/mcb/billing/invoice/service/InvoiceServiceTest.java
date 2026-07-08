package com.mcb.billing.invoice.service;

import com.mcb.billing.invoice.client.CustomerClient;
import com.mcb.billing.invoice.client.CustomerDTO;
import com.mcb.billing.invoice.domain.InvoiceStatus;
import com.mcb.billing.invoice.exception.InactiveCustomerException;
import com.mcb.billing.invoice.exception.InvalidInvoiceStateException;
import com.mcb.billing.invoice.exception.InvoiceNotFoundException;
import com.mcb.billing.invoice.exception.UnknownCustomerException;
import com.mcb.billing.invoice.web.dto.CreateInvoiceItemDTO;
import com.mcb.billing.invoice.web.dto.CreateInvoiceRequestDTO;
import com.mcb.billing.invoice.web.dto.InvoiceResponseDTO;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Service tests running on H2. The customer-service is not started - its Feign
 * client is replaced with a Mockito mock, so we control what the "remote" returns.
 */
@SpringBootTest
class InvoiceServiceTest {

    @Autowired
    InvoiceService invoiceService;

    @MockBean
    CustomerClient customerClient;

    private CreateInvoiceRequestDTO request(String customerNumber) {
        return new CreateInvoiceRequestDTO(customerNumber, "EUR", LocalDate.now().plusDays(30),
                List.of(new CreateInvoiceItemDTO("Consulting", 3, new BigDecimal("100.00")),
                        new CreateInvoiceItemDTO("License", 1, new BigDecimal("49.99"))));
    }

    private static FeignException.NotFound feignNotFound() {
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost/customers/x",
                Map.of(), null, StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("Not Found", request, null, Map.of());
    }

    @Test
    void createsInvoiceForActiveCustomerWithComputedTotal() {
        when(customerClient.getCustomer("C-00001"))
                .thenReturn(new CustomerDTO("C-00001", "Acme GmbH", true));

        InvoiceResponseDTO created = invoiceService.createInvoice(request("C-00001"));

        assertThat(created.invoiceNumber()).matches("INV-\\d{5}");
        assertThat(created.status()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(created.totalAmount()).isEqualByComparingTo("349.99");
        assertThat(created.items()).hasSize(2);
    }

    @Test
    void rejectsUnknownCustomer() {
        when(customerClient.getCustomer("C-99999")).thenThrow(feignNotFound());

        assertThatThrownBy(() -> invoiceService.createInvoice(request("C-99999")))
                .isInstanceOf(UnknownCustomerException.class);
    }

    @Test
    void rejectsInactiveCustomer() {
        when(customerClient.getCustomer("C-00002"))
                .thenReturn(new CustomerDTO("C-00002", "Inactive GmbH", false));

        assertThatThrownBy(() -> invoiceService.createInvoice(request("C-00002")))
                .isInstanceOf(InactiveCustomerException.class);
    }

    @Test
    void issuesDraftAndRejectsSecondIssue() {
        when(customerClient.getCustomer("C-00003"))
                .thenReturn(new CustomerDTO("C-00003", "Acme GmbH", true));
        InvoiceResponseDTO created = invoiceService.createInvoice(request("C-00003"));

        InvoiceResponseDTO issued = invoiceService.issueInvoice(created.invoiceNumber());
        assertThat(issued.status()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(issued.issueDate()).isNotNull();

        assertThatThrownBy(() -> invoiceService.issueInvoice(created.invoiceNumber()))
                .isInstanceOf(InvalidInvoiceStateException.class);
    }

    @Test
    void getUnknownInvoiceThrowsNotFound() {
        assertThatThrownBy(() -> invoiceService.getByInvoiceNumber("INV-99999"))
                .isInstanceOf(InvoiceNotFoundException.class);
    }
}
