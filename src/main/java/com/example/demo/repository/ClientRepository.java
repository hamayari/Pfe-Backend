package com.example.demo.repository;

import com.example.demo.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {
    Optional<Client> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Client> findByActive(boolean active);
    List<Client> findByInvoiceIdsContaining(String invoiceId);
    Optional<Client> findByEmailAndActive(String email, boolean active);
} 