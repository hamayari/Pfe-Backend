package com.example.demo.repository;

import com.example.demo.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    List<Invoice> findByConventionId(String conventionId);
    List<Invoice> findByStatus(String status);
    List<Invoice> findByDueDateBeforeAndStatus(LocalDate date, String status);
    boolean existsByReference(String reference);
    List<Invoice> findByCreatedBy(String userId);
    List<Invoice> findByCreatedByAndStatusAndDueDateBefore(String userId, String status, LocalDate date);
    List<Invoice> findByCreatedByAndDueDateBetween(String userId, LocalDate start, LocalDate end);
    List<Invoice> findByClientId(String clientId);
    
    // Méthodes pour le système de notifications
    List<Invoice> findByStatusAndDueDateBefore(String status, LocalDate date);
    List<Invoice> findByCreatedByAndDueDateAndStatus(String userId, LocalDate date, String status);
    long countByCreatedAtBetween(LocalDate start, LocalDate end);
    long countByStatusAndPaymentDateBetween(String status, LocalDate start, LocalDate end);
    long countByStatusAndDueDateBefore(String status, LocalDate date);
    List<Invoice> findByStatusAndPaymentDateBetween(String status, LocalDate start, LocalDate end);
    
    // Méthodes pour le scheduler de notifications
    List<Invoice> findByDueDate(LocalDate dueDate);
    List<Invoice> findByDueDateBeforeAndStatusNot(LocalDate date, String status);
}
