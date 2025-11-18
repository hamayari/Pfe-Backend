package com.example.demo.service;

import com.example.demo.dto.CalendarFiltersDTO;
import com.example.demo.dto.CalendarStatsDTO;
import com.example.demo.model.CalendarEvent;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.CalendarEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @InjectMocks
    private CalendarService calendarService;

    private CalendarEvent mockEvent;
    private Convention mockConvention;
    private Invoice mockInvoice;

    @BeforeEach
    void setUp() {
        mockEvent = new CalendarEvent();
        mockEvent.setId("event1");
        mockEvent.setTitle("Test Event");
        mockEvent.setDate(LocalDateTime.now());
        mockEvent.setType("convention");
        mockEvent.setStatus("pending");
        mockEvent.setColor("#2196f3");

        mockConvention = new Convention();
        mockConvention.setId("conv1");
        mockConvention.setReference("REF001");
        mockConvention.setTitle("Convention Test");
        mockConvention.setClient("Client Test");
        mockConvention.setAmount(BigDecimal.valueOf(1000));
        mockConvention.setCreatedBy("user1");
        mockConvention.setCreatedAt(LocalDate.now());
        mockConvention.setEndDate(LocalDate.now().plusMonths(1));
        mockConvention.setEcheances(Arrays.asList(LocalDate.now().plusDays(15), LocalDate.now().plusDays(30)));

        mockInvoice = new Invoice();
        mockInvoice.setId("inv1");
        mockInvoice.setInvoiceNumber("FACT001");
        mockInvoice.setAmount(BigDecimal.valueOf(500));
        mockInvoice.setDueDate(LocalDate.now().plusDays(30));
        mockInvoice.setStatus("PENDING");
        mockInvoice.setCreatedBy("user1");
    }

    @Test
    void testGetCalendarEvents_NoFilters() {
        List<CalendarEvent> events = Arrays.asList(mockEvent);
        when(calendarEventRepository.findAll()).thenReturn(events);

        List<CalendarEvent> result = calendarService.getCalendarEvents(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(calendarEventRepository).findAll();
    }

    @Test
    void testGetCalendarEvents_WithFilters() {
        CalendarFiltersDTO filters = new CalendarFiltersDTO();
        filters.setUserId("user1");
        filters.setStartDate(LocalDate.now());
        filters.setEndDate(LocalDate.now().plusDays(30));

        List<CalendarEvent> events = Arrays.asList(mockEvent);
        when(calendarEventRepository.findEventsWithFilters(any(), any(), anyString(), any(), any()))
            .thenReturn(events);

        List<CalendarEvent> result = calendarService.getCalendarEvents(filters);

        assertNotNull(result);
        verify(calendarEventRepository).findEventsWithFilters(any(), any(), anyString(), any(), any());
    }

    @Test
    void testGetEventsForDate() {
        LocalDate date = LocalDate.now();
        List<CalendarEvent> events = Arrays.asList(mockEvent);
        when(calendarEventRepository.findEventsForDate(any(), any())).thenReturn(events);

        List<CalendarEvent> result = calendarService.getEventsForDate(date);

        assertNotNull(result);
        verify(calendarEventRepository).findEventsForDate(any(), any());
    }

    @Test
    void testGetEventsForWeek() {
        LocalDate startDate = LocalDate.now();
        List<CalendarEvent> events = Arrays.asList(mockEvent);
        when(calendarEventRepository.findEventsForWeek(any(), any())).thenReturn(events);

        List<CalendarEvent> result = calendarService.getEventsForWeek(startDate);

        assertNotNull(result);
        verify(calendarEventRepository).findEventsForWeek(any(), any());
    }

    @Test
    void testGetEventsForMonth() {
        List<CalendarEvent> events = Arrays.asList(mockEvent);
        when(calendarEventRepository.findEventsForMonth(any(), any())).thenReturn(events);

        List<CalendarEvent> result = calendarService.getEventsForMonth(2025, 11);

        assertNotNull(result);
        verify(calendarEventRepository).findEventsForMonth(any(), any());
    }

    @Test
    void testCreateCustomEvent() {
        when(calendarEventRepository.save(any(CalendarEvent.class))).thenReturn(mockEvent);

        CalendarEvent result = calendarService.createCustomEvent(mockEvent);

        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        verify(calendarEventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void testUpdateEvent() {
        when(calendarEventRepository.findById("event1")).thenReturn(Optional.of(mockEvent));
        when(calendarEventRepository.save(any(CalendarEvent.class))).thenReturn(mockEvent);

        CalendarEvent updatedEvent = new CalendarEvent();
        updatedEvent.setTitle("Updated Title");
        updatedEvent.setDate(LocalDateTime.now().plusDays(1));

        CalendarEvent result = calendarService.updateEvent("event1", updatedEvent);

        assertNotNull(result);
        verify(calendarEventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void testDeleteEvent() {
        doNothing().when(calendarEventRepository).deleteById("event1");

        calendarService.deleteEvent("event1");

        verify(calendarEventRepository).deleteById("event1");
    }

    @Test
    void testGetCalendarStats() {
        List<CalendarEvent> events = new ArrayList<>();
        CalendarEvent event1 = new CalendarEvent();
        event1.setType("convention");
        event1.setStatus("pending");
        event1.setDate(LocalDateTime.now());
        events.add(event1);

        CalendarEvent event2 = new CalendarEvent();
        event2.setType("facture");
        event2.setStatus("completed");
        event2.setDate(LocalDateTime.now());
        events.add(event2);

        when(calendarEventRepository.findByDateBetween(any(), any())).thenReturn(events);

        CalendarStatsDTO stats = calendarService.getCalendarStats(
            LocalDateTime.now().minusDays(30), LocalDateTime.now());

        assertNotNull(stats);
        assertEquals(2L, stats.getTotalEvents());
        assertNotNull(stats.getEventsByType());
        assertNotNull(stats.getEventsByStatus());
    }

    @Test
    void testGenerateEventsFromConventions() {
        List<Convention> conventions = Arrays.asList(mockConvention);

        List<CalendarEvent> events = calendarService.generateEventsFromConventions(conventions);

        assertNotNull(events);
        assertTrue(events.size() > 0);
    }

    @Test
    void testGenerateEventsFromInvoices() {
        List<Invoice> invoices = Arrays.asList(mockInvoice);

        List<CalendarEvent> events = calendarService.generateEventsFromInvoices(invoices);

        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals("Facture: FACT001", events.get(0).getTitle());
    }

    @Test
    void testExportCalendar_ICS() {
        List<CalendarEvent> events = Arrays.asList(mockEvent);
        when(calendarEventRepository.findEventsWithFilters(any(), any(), any(), any(), any()))
            .thenReturn(events);

        byte[] result = calendarService.exportCalendar("ics", new CalendarFiltersDTO());

        assertNotNull(result);
        assertTrue(result.length > 0);
        String content = new String(result);
        assertTrue(content.contains("BEGIN:VCALENDAR"));
    }

    @Test
    void testExportCalendar_PDF() {
        List<CalendarEvent> events = Arrays.asList(mockEvent);
        when(calendarEventRepository.findEventsWithFilters(any(), any(), any(), any(), any()))
            .thenReturn(events);

        byte[] result = calendarService.exportCalendar("pdf", new CalendarFiltersDTO());

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportCalendar_Excel() {
        List<CalendarEvent> events = Arrays.asList(mockEvent);
        when(calendarEventRepository.findEventsWithFilters(any(), any(), any(), any(), any()))
            .thenReturn(events);

        byte[] result = calendarService.exportCalendar("excel", new CalendarFiltersDTO());

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportCalendar_InvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            calendarService.exportCalendar("invalid", new CalendarFiltersDTO());
        });
    }
}
