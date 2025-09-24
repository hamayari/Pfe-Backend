package com.example.demo.repository;

import com.example.demo.model.PaymentProof;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PaymentProofRepository extends MongoRepository<PaymentProof, String> {
    List<PaymentProof> findByInvoiceId(String invoiceId);
    List<PaymentProof> findByClientId(String clientId);
    List<PaymentProof> findByStatus(String status);
    List<PaymentProof> findByInvoiceIdAndStatus(String invoiceId, String status);
    List<PaymentProof> findByClientIdAndStatus(String clientId, String status);
} 