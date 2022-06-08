package br.com.alurafood.payments.service;

import br.com.alurafood.payments.dto.PaymentDto;
import br.com.alurafood.payments.http.PedidoClient;
import br.com.alurafood.payments.model.Payment;
import br.com.alurafood.payments.model.Status;
import br.com.alurafood.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    private final PedidoClient pedidoClient;

    public Page<PaymentDto> getAll(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(payment -> modelMapper.map(payment, PaymentDto.class));
    }

    public PaymentDto getById(Long id) {
        return paymentRepository
                .findById(id)
                .map(payment -> modelMapper.map(payment, PaymentDto.class))
                .orElseThrow(EntityNotFoundException::new);
    }

    public PaymentDto create(PaymentDto paymentDto) {
        Payment payment = modelMapper.map(paymentDto, Payment.class);
        return modelMapper.map(paymentRepository.save(payment), PaymentDto.class);
    }

    public PaymentDto update(Long id, PaymentDto paymentDto) {
        Payment payment = modelMapper.map(paymentDto, Payment.class);
        payment.setId(id);
        return modelMapper.map(paymentRepository.save(payment), PaymentDto.class);
    }

    public void delete(Long id) {
        paymentRepository.deleteById(id);
    }

    public void confirmPayment(Long id) {
        Payment payment = confirmPayment(id, Status.CONFIRMED);
        pedidoClient.updatePayment(payment.getInvoiceId());
    }

    public Payment confirmPayment(Long id, Status status) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);

        if (optionalPayment.isEmpty()) {
            throw new EntityNotFoundException();
        }
        Payment payment = optionalPayment.get();
        payment.setStatus(status);
        paymentRepository.save(payment);
        return payment;
    }
}
