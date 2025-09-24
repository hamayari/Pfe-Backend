package com.example.demo.service;

import com.example.demo.dto.CalendarFiltersDTO;
import com.example.demo.dto.CalendarStatsDTO;
import com.example.demo.model.CalendarEvent;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.CalendarEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

        // Services supprimés car non utilisés dans cette implémentation simplifiée
    // @Autowired
    // private ConventionService conventionService;
    
    // @Autowired
    // private InvoiceService invoiceService;

    // Récupérer tous les événements avec filtres
    public List<CalendarEvent> getCalendarEvents(CalendarFiltersDTO filters) {
        if (filters == null) {
            return calendarEventRepository.findAll();
        }

        return calendarEventRepository.findEventsWithFilters(
                filters.getEventTypes(),
                filters.getStatuses(),
                filters.getUserId(),
                filters.getStartDate() != null ? filters.getStartDate().atStartOfDay() : null,
                filters.getEndDate() != null ? filters.getEndDate().atTime(23, 59, 59) : null
        );
    }

    // Récupérer les événements pour une date spécifique
    public List<CalendarEvent> getEventsForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return calendarEventRepository.findEventsForDate(startOfDay, endOfDay);
    }

    // Récupérer les événements pour une semaine
    public List<CalendarEvent> getEventsForWeek(LocalDate startDate) {
        LocalDateTime startOfWeek = startDate.atStartOfDay();
        LocalDateTime endOfWeek = startDate.plusDays(6).atTime(LocalTime.MAX);
        return calendarEventRepository.findEventsForWeek(startOfWeek, endOfWeek);
    }

    // Récupérer les événements pour un mois
    public List<CalendarEvent> getEventsForMonth(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);
        
        return calendarEventRepository.findEventsForMonth(startDateTime, endDateTime);
    }

    // Créer un événement personnalisé
    public CalendarEvent createCustomEvent(CalendarEvent event) {
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        return calendarEventRepository.save(event);
    }

    // Mettre à jour un événement
    public CalendarEvent updateEvent(String eventId, CalendarEvent event) {
        CalendarEvent existingEvent = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé: " + eventId));
        
        existingEvent.setTitle(event.getTitle());
        existingEvent.setDate(event.getDate());
        existingEvent.setType(event.getType());
        existingEvent.setStatus(event.getStatus());
        existingEvent.setColor(event.getColor());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setUpdatedAt(LocalDateTime.now());
        
        return calendarEventRepository.save(existingEvent);
    }

    // Supprimer un événement
    public void deleteEvent(String eventId) {
        calendarEventRepository.deleteById(eventId);
    }

    // Obtenir les statistiques du calendrier
    public CalendarStatsDTO getCalendarStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<CalendarEvent> events = calendarEventRepository.findByDateBetween(startDate, endDate);
        
        CalendarStatsDTO stats = new CalendarStatsDTO();
        stats.setTotalEvents((long) events.size());
        stats.setPendingEvents((long) events.stream().filter(e -> "pending".equals(e.getStatus())).count());
        stats.setOverdueEvents((long) events.stream().filter(e -> "overdue".equals(e.getStatus())).count());
        stats.setCompletedEvents((long) events.stream().filter(e -> "completed".equals(e.getStatus())).count());
        
        // Répartition par type
        Map<String, Long> eventsByType = events.stream()
                .collect(Collectors.groupingBy(CalendarEvent::getType, Collectors.counting()));
        stats.setEventsByType(eventsByType);
        
        // Répartition par statut
        Map<String, Long> eventsByStatus = events.stream()
                .collect(Collectors.groupingBy(CalendarEvent::getStatus, Collectors.counting()));
        stats.setEventsByStatus(eventsByStatus);
        
        // Répartition par jour
        Map<String, Long> eventsByDay = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDate().toLocalDate().toString(),
                        Collectors.counting()
                ));
        stats.setEventsByDay(eventsByDay);
        
        return stats;
    }

    // Exporter le calendrier
    public byte[] exportCalendar(String format, CalendarFiltersDTO filters) {
        List<CalendarEvent> events = getCalendarEvents(filters);
        
        switch (format.toLowerCase()) {
            case "ics":
                return exportToICS(events);
            case "pdf":
                return exportToPDF(events);
            case "excel":
                return exportToExcel(events);
            default:
                throw new IllegalArgumentException("Format non supporté: " + format);
        }
    }

    // Générer les événements depuis les conventions
    public List<CalendarEvent> generateEventsFromConventions(List<Convention> conventions) {
        List<CalendarEvent> events = new ArrayList<>();
        
        for (Convention convention : conventions) {
            // Événement de création de convention
            CalendarEvent creationEvent = new CalendarEvent();
            creationEvent.setTitle("Convention: " + convention.getTitle());
            creationEvent.setDate(convention.getCreatedAt() != null ? convention.getCreatedAt().atStartOfDay() : LocalDateTime.now());
            creationEvent.setType("convention");
            creationEvent.setStatus(getConventionStatus(convention));
            creationEvent.setDescription("Convention " + convention.getReference() + " - " + convention.getClient());
            creationEvent.setColor("#2196f3");
            creationEvent.setUserId(convention.getCreatedBy());
            
            events.add(creationEvent);
            
            // Échéances de la convention
            if (convention.getEcheances() != null) {
                for (int i = 0; i < convention.getEcheances().size(); i++) {
                    LocalDateTime echeanceDate = convention.getEcheances().get(i).atStartOfDay();
                    boolean isOverdue = echeanceDate.isBefore(LocalDateTime.now());
                    
                    CalendarEvent echeanceEvent = new CalendarEvent();
                    echeanceEvent.setTitle("Échéance " + convention.getReference());
                    echeanceEvent.setDate(echeanceDate);
                    echeanceEvent.setType("echeance");
                    echeanceEvent.setStatus(isOverdue ? "overdue" : "pending");
                    echeanceEvent.setDescription("Échéance #" + (i + 1) + " - Montant: " + convention.getAmount() + "€");
                    echeanceEvent.setColor(isOverdue ? "#f44336" : "#ff9800");
                    echeanceEvent.setUserId(convention.getCreatedBy());
                    
                    events.add(echeanceEvent);
                }
            }
            
            // Date de fin de convention
            if (convention.getEndDate() != null) {
                CalendarEvent endEvent = new CalendarEvent();
                endEvent.setTitle("Fin Convention: " + convention.getReference());
                endEvent.setDate(convention.getEndDate().atStartOfDay());
                endEvent.setType("convention");
                endEvent.setStatus(convention.getEndDate().atStartOfDay().isBefore(LocalDateTime.now()) ? "completed" : "pending");
                endEvent.setDescription("Fin de la convention " + convention.getReference());
                endEvent.setColor("#4caf50");
                endEvent.setUserId(convention.getCreatedBy());
                
                events.add(endEvent);
            }
        }
        
        return events;
    }

    // Générer les événements depuis les factures
    public List<CalendarEvent> generateEventsFromInvoices(List<Invoice> invoices) {
        List<CalendarEvent> events = new ArrayList<>();
        
        for (Invoice invoice : invoices) {
            LocalDateTime dueDate = invoice.getDueDate().atStartOfDay();
            boolean isOverdue = dueDate.isBefore(LocalDateTime.now());
            
            CalendarEvent invoiceEvent = new CalendarEvent();
            invoiceEvent.setTitle("Facture: " + invoice.getInvoiceNumber());
            invoiceEvent.setDate(dueDate);
            invoiceEvent.setType("facture");
            invoiceEvent.setStatus(invoice.getStatus().equals("PAID") ? "completed" : 
                    (isOverdue ? "overdue" : "pending"));
            invoiceEvent.setDescription("Facture " + invoice.getInvoiceNumber() + " - Montant: " + invoice.getAmount() + "€");
            invoiceEvent.setColor(invoice.getStatus().equals("PAID") ? "#4caf50" : 
                    (isOverdue ? "#f44336" : "#ff9800"));
            invoiceEvent.setUserId(invoice.getCreatedBy());
            
            events.add(invoiceEvent);
        }
        
        return events;
    }

    // Méthodes utilitaires privées
    private String getConventionStatus(Convention convention) {
        if ("COMPLETED".equals(convention.getStatus())) {
            return "completed";
        }
        
        if (convention.getEndDate() != null && convention.getEndDate().isBefore(LocalDate.now())) {
            return "overdue";
        }
        
        return "pending";
    }

    private byte[] exportToICS(List<CalendarEvent> events) {
        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//Calendar Export//FR\r\n");
        
        for (CalendarEvent event : events) {
            ics.append("BEGIN:VEVENT\r\n");
            ics.append("UID:").append(event.getId()).append("\r\n");
            ics.append("DTSTART:").append(event.getDate().toString().replace("T", "").replace(":", "")).append("Z\r\n");
            ics.append("SUMMARY:").append(event.getTitle()).append("\r\n");
            ics.append("DESCRIPTION:").append(event.getDescription() != null ? event.getDescription() : "").append("\r\n");
            ics.append("END:VEVENT\r\n");
        }
        
        ics.append("END:VCALENDAR\r\n");
        return ics.toString().getBytes();
    }

    private byte[] exportToPDF(List<CalendarEvent> events) {
        // Implémentation simplifiée - retourne un PDF basique
        String pdfContent = "Calendrier des échéances\n\n";
        for (CalendarEvent event : events) {
            pdfContent += event.getDate().toLocalDate() + " - " + event.getTitle() + "\n";
        }
        return pdfContent.getBytes();
    }

    private byte[] exportToExcel(List<CalendarEvent> events) {
        // Implémentation simplifiée - retourne un CSV
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Titre,Type,Statut,Description\n");
        
        for (CalendarEvent event : events) {
            csv.append(event.getDate().toLocalDate())
               .append(",")
               .append("\"").append(event.getTitle()).append("\"")
               .append(",")
               .append(event.getType())
               .append(",")
               .append(event.getStatus())
               .append(",")
               .append("\"").append(event.getDescription() != null ? event.getDescription() : "").append("\"")
               .append("\n");
        }
        
        return csv.toString().getBytes();
    }
} 