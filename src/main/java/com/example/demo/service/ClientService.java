package com.example.demo.service;

import com.example.demo.model.Client;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * Crée ou récupère un client avec des credentials automatiques
     */
    public Client createOrGetClientWithCredentials(String email, String name, String commercialId) {
        // Vérifier si le client existe déjà
        Optional<Client> existingClient = clientRepository.findByEmail(email);
        
        if (existingClient.isPresent()) {
            Client client = existingClient.get();
            // Si le client existe mais n'a pas de mot de passe, en générer un nouveau
            if (client.getPassword() == null || client.getPassword().isEmpty()) {
                String newPassword = generateSecurePassword();
                client.setPassword(passwordEncoder.encode(newPassword));
                client.setForcePasswordChange(true);
                clientRepository.save(client);
                
                // TODO: Envoyer les nouveaux credentials par email
                System.out.println("Nouveaux credentials générés pour " + email + ": " + newPassword);
            }
            return client;
        }

        // Créer un nouveau client
        Client newClient = new Client();
        newClient.setEmail(email);
        newClient.setName(name);
        newClient.setCreatedBy(commercialId);
        newClient.setActive(true);
        newClient.setForcePasswordChange(true);
        
        // Générer un mot de passe sécurisé
        String password = generateSecurePassword();
        newClient.setPassword(passwordEncoder.encode(password));
        
        // Sauvegarder le client
        Client savedClient = clientRepository.save(newClient);
        
        // TODO: Envoyer les credentials par email
        System.out.println("Credentials générés pour " + email + ": " + password);
        
        System.out.println("✅ Client créé avec succès: " + email);
        System.out.println("🔑 Mot de passe généré: " + password);
        
        return savedClient;
    }

    /**
     * Authentifie un client
     */
    public Optional<Client> authenticateClient(String email, String password) {
        Optional<Client> clientOpt = clientRepository.findByEmailAndActive(email, true);
        
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            if (passwordEncoder.matches(password, client.getPassword())) {
                // Mettre à jour les informations de connexion
                client.setLastLoginAt(LocalDateTime.now());
                clientRepository.save(client);
                return Optional.of(client);
            }
        }
        
        return Optional.empty();
    }

    /**
     * Change le mot de passe d'un client
     */
    public boolean changeClientPassword(String email, String oldPassword, String newPassword) {
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            if (passwordEncoder.matches(oldPassword, client.getPassword())) {
                client.setPassword(passwordEncoder.encode(newPassword));
                client.setForcePasswordChange(false);
                client.setPasswordChangedAt(LocalDateTime.now());
                clientRepository.save(client);
                return true;
            }
        }
        
        return false;
    }

    /**
     * Ajoute une facture à un client
     */
    public void addInvoiceToClient(String clientEmail, String invoiceId) {
        Optional<Client> clientOpt = clientRepository.findByEmail(clientEmail);
        
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            List<String> invoiceIds = client.getInvoiceIds();
            if (invoiceIds == null) {
                invoiceIds = new ArrayList<>();
            }
            if (!invoiceIds.contains(invoiceId)) {
                invoiceIds.add(invoiceId);
                client.setInvoiceIds(invoiceIds);
                clientRepository.save(client);
            }
        }
    }

    /**
     * Récupère tous les clients actifs
     */
    public List<Client> getAllActiveClients() {
        return clientRepository.findByActive(true);
    }

    /**
     * Récupère un client par email
     */
    public Optional<Client> getClientByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    /**
     * Désactive un client
     */
    public boolean deactivateClient(String email) {
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            client.setActive(false);
            clientRepository.save(client);
            return true;
        }
        
        return false;
    }

    /**
     * Réactive un client
     */
    public boolean reactivateClient(String email) {
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            client.setActive(true);
            clientRepository.save(client);
            return true;
        }
        
        return false;
    }

    /**
     * Génère un nouveau mot de passe pour un client
     */
    public boolean resetClientPassword(String email) {
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            String newPassword = generateSecurePassword();
            client.setPassword(passwordEncoder.encode(newPassword));
            client.setForcePasswordChange(true);
            clientRepository.save(client);
            
            // TODO: Envoyer le nouveau mot de passe par email
            System.out.println("Nouveau mot de passe généré pour " + email + ": " + newPassword);
            
            return true;
        }
        
        return false;
    }

    /**
     * Générer un mot de passe sécurisé
     */
    private String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
} 