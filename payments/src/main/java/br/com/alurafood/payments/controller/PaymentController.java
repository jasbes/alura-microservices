package br.com.alurafood.payments.controller;

import br.com.alurafood.payments.dto.PaymentDto;
import br.com.alurafood.payments.model.Status;
import br.com.alurafood.payments.service.PaymentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.URI;

@RequiredArgsConstructor
@RequestMapping("/payments")
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public Page<PaymentDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return paymentService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentDto> create(@RequestBody PaymentDto paymentDto, UriComponentsBuilder uriComponentsBuilder) {
        PaymentDto payment = paymentService.create(paymentDto);
        URI uri = uriComponentsBuilder.path("/payments/{id}").buildAndExpand(payment.getId()).toUri();

        return ResponseEntity.created(uri).body(payment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentDto> update(@PathVariable Long id, @RequestBody PaymentDto paymentDto) {
        return ResponseEntity.ok(paymentService.update(id, paymentDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PaymentDto> delete(@PathVariable Long id) {
        paymentService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @CircuitBreaker(name = "updateInvoice", fallbackMethod = "confirmPaymentWithPendingIntegration")
    @PatchMapping("/{id}/confirm")
    public void confirmPayment(@PathVariable @NotNull Long id) {
        paymentService.confirmPayment(id);
    }

    public void confirmPaymentWithPendingIntegration(Long id, Exception e) {
        paymentService.confirmPayment(id, Status.CONFIRMED_NOT_INTEGRATED);
    }
}
