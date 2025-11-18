package com.example.demo.service;

import com.example.demo.dto.CalendarEventDTO;
import com.example.demo.model.CalendarEvent;
import com.example.demo.repository.CalendarEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarEventServiceTest {

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @InjectMocks
    private CalendarEventService calendarEventService;

    private CalendarEvent mockEvent;
    private CalendarEventDTO mockEventDTO;

    @BeforeEach
    void setUp() {
        mockEvent = new CalendarEvent();
        mockEvent.setId("event1");
        mockEvent.setTitle("Test Event");
        mockEvent.setDescription("Test Description");
        mockEvent.setDate(LocalDateTime.now());
        mockEvent.setType("convention");
        mockEvent.setStatus("pending");
        mockEvent.setColor("#2196f3");

        mockEventDTO = new CalendarEventDTO();
        mockEventDTO.setId("event1");
        mockEventDTO.setTitle("Test Event");
        mockEventDTO.setDescription("Test Description");
        mockEventDTO.setStart(LocalDateTime.now());
        mockEventDTO.setEnd(LocalDateTime.now());
        mockEventDTO.setType("convention");
        mockEventDTO.setStatus("pending");
    }

    @Test
    void testGetAllEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        events.add(mockEvent);
        when(calendarEventRepository.findAll()).thenReturn(events);

        List<CalendarEventDTO> result = calendarEventService.getAllEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(calendarEventRepository).findAll();
    }

    @Test
    void testGetEventById() {
        when(calendarEventRepository.findById("event1")).thenReturn(Optional.of(mockEvent));

        Optional<CalendarEventDTO> result = calendarEventService.getEventById("event1");

        assertTrue(result.isPresent());
        assertEquals("Test Event", result.get().getTitle());
        verify(calendarEventRepository).findById("event1");
    }

    @Test
    void testCreateEvent() {
        when(calendarEventRepository.save(any(CalendarEvent.class))).thenReturn(mockEvent);

        CalendarEventDTO result = calendarEventService.createEvent(mockEventDTO);

        assertNotNull(result);
        assertEquals("Test Event", result.getTitle());
        verify(calendarEventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void testUpdateEvent() {
        when(calendarEventRepository.findById("event1")).thenReturn(Optional.of(mockEvent));
        when(calendarEventRepository.save(any(CalendarEvent.class))).thenReturn(mockEvent);

        Optional<CalendarEventDTO> result = calendarEventService.updateEvent("event1", mockEventDTO);

        assertTrue(result.isPresent());
        verify(calendarEventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void testDeleteEvent_Success() {
        when(calendarEventRepository.existsById("event1")).thenReturn(true);
        doNothing().when(calendarEventRepository).deleteById("event1");

        boolean result = calendarEventService.deleteEvent("event1");

        assertTrue(result);
        verify(calendarEventRepository).deleteById("event1");
    }

    @Test
    void testDeleteEvent_NotFound() {
        when(calendarEventRepository.existsById("event1")).thenReturn(false);

        boolean result = calendarEventService.deleteEvent("event1");

        assertFalse(result);
        verify(calendarEventRepository, never()).deleteById(anyString());
    }

    @Test
    void testGetEventsByUser() {
        List<CalendarEvent> events = new ArrayList<>();
        events.add(mockEvent);
        when(calendarEventRepository.findByUserId("user1")).thenReturn(events);

        List<CalendarEventDTO> result = calendarEventService.getEventsByUser("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(calendarEventRepository).findByUserId("user1");
    }

    @Test
    void testGetEventsByDateRange() {
        List<CalendarEvent> events = new ArrayList<>();
        events.add(mockEvent);
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        when(calendarEventRepository.findByDateBetween(start, end)).thenReturn(events);

        List<CalendarEventDTO> result = calendarEventService.getEventsByDateRange(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(calendarEventRepository).findByDateBetween(start, end);
    }

    @Test
    void testGetEventsByType() {
        List<CalendarEvent> events = new ArrayList<>();
        events.add(mockEvent);
        when(calendarEventRepository.findAll()).thenReturn(events);

        List<CalendarEventDTO> result = calendarEventService.getEventsByType("convention");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
