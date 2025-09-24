package com.example.demo.service;

import com.example.demo.dto.CalendarEventDTO;
import com.example.demo.model.CalendarEvent;
import com.example.demo.repository.CalendarEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CalendarEventService {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    public List<CalendarEventDTO> getAllEvents() {
        try {
            return calendarEventRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur dans getAllEvents: " + e.getMessage());
            return List.of();
        }
    }

    public Optional<CalendarEventDTO> getEventById(String id) {
        try {
            return calendarEventRepository.findById(id).map(this::convertToDTO);
        } catch (Exception e) {
            System.err.println("Erreur dans getEventById: " + e.getMessage());
            return Optional.empty();
        }
    }

    public CalendarEventDTO createEvent(CalendarEventDTO eventDTO) {
        try {
            CalendarEvent event = convertToEntity(eventDTO);
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());
            
            CalendarEvent savedEvent = calendarEventRepository.save(event);
            return convertToDTO(savedEvent);
        } catch (Exception e) {
            System.err.println("Erreur dans createEvent: " + e.getMessage());
            return null;
        }
    }

    public Optional<CalendarEventDTO> updateEvent(String id, CalendarEventDTO eventDTO) {
        try {
            return calendarEventRepository.findById(id).map(existingEvent -> {
                existingEvent.setTitle(eventDTO.getTitle());
                existingEvent.setDescription(eventDTO.getDescription());
                existingEvent.setDate(eventDTO.getStart()); // CalendarEvent utilise date, DTO utilise start
                existingEvent.setType(eventDTO.getType());
                existingEvent.setStatus(eventDTO.getStatus());
                existingEvent.setUpdatedAt(LocalDateTime.now());
                
                CalendarEvent updatedEvent = calendarEventRepository.save(existingEvent);
                return convertToDTO(updatedEvent);
            });
        } catch (Exception e) {
            System.err.println("Erreur dans updateEvent: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean deleteEvent(String id) {
        try {
            if (calendarEventRepository.existsById(id)) {
                calendarEventRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Erreur dans deleteEvent: " + e.getMessage());
            return false;
        }
    }

    private CalendarEventDTO convertToDTO(CalendarEvent event) {
        CalendarEventDTO dto = new CalendarEventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStart(event.getDate()); // Mapper date -> start
        dto.setEnd(event.getDate());   // Par défaut, end = start
        dto.setType(event.getType());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        dto.setCreatedBy(event.getCreatedBy());
        dto.setColor(event.getColor());
        return dto;
    }

    private CalendarEvent convertToEntity(CalendarEventDTO dto) {
        CalendarEvent event = new CalendarEvent();
        event.setId(dto.getId());
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getStart()); // Mapper start -> date
        event.setType(dto.getType());
        event.setStatus(dto.getStatus());
        event.setCreatedBy(dto.getCreatedBy());
        event.setColor(dto.getColor());
        return event;
    }

    public List<CalendarEventDTO> getEventsByUser(String userId) {
        try {
            return calendarEventRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur dans getEventsByUser: " + e.getMessage());
            return List.of();
        }
    }

    public List<CalendarEventDTO> getEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        try {
            return calendarEventRepository.findByDateBetween(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur dans getEventsByDateRange: " + e.getMessage());
            return List.of();
        }
    }

    public List<CalendarEventDTO> getEventsByType(String type) {
        try {
            // Simulation en attendant la méthode repository
            return calendarEventRepository.findAll().stream()
                .filter(event -> type.equals(event.getType()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur dans getEventsByType: " + e.getMessage());
            return List.of();
        }
    }
}
