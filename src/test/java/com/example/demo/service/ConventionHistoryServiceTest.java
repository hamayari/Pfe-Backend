package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.ConventionHistory;
import com.example.demo.repository.ConventionHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConventionHistoryServiceTest {

    @Mock
    private ConventionHistoryRepository historyRepository;

    @InjectMocks
    private ConventionHistoryService conventionHistoryService;

    private Convention mockConvention;

    @BeforeEach
    void setUp() {
        mockConvention = new Convention();
        mockConvention.setId("conv1");
        mockConvention.setReference("REF001");
        mockConvention.setTitle("Test Convention");
        mockConvention.setStatus("ACTIVE");
        mockConvention.setAmount(BigDecimal.valueOf(1000));
        mockConvention.setStartDate(LocalDate.now());
        mockConvention.setEndDate(LocalDate.now().plusMonths(1));
    }

    @Test
    void testRecordCreate() {
        when(historyRepository.save(any(ConventionHistory.class))).thenReturn(new ConventionHistory());

        conventionHistoryService.recordCreate(mockConvention, "user1", "User One");

        verify(historyRepository).save(any(ConventionHistory.class));
    }

    @Test
    void testRecordUpdate() {
        Convention oldConvention = new Convention();
        oldConvention.setId("conv1");
        oldConvention.setReference("REF001");
        oldConvention.setTitle("Old Title");
        oldConvention.setAmount(BigDecimal.valueOf(500));

        Convention newConvention = new Convention();
        newConvention.setId("conv1");
        newConvention.setReference("REF001");
        newConvention.setTitle("New Title");
        newConvention.setAmount(BigDecimal.valueOf(1000));

        when(historyRepository.save(any(ConventionHistory.class))).thenReturn(new ConventionHistory());

        conventionHistoryService.recordUpdate(oldConvention, newConvention, "user1", "User One");

        verify(historyRepository, atLeastOnce()).save(any(ConventionHistory.class));
    }

    @Test
    void testRecordStatusChange() {
        when(historyRepository.save(any(ConventionHistory.class))).thenReturn(new ConventionHistory());

        conventionHistoryService.recordStatusChange(mockConvention, "DRAFT", "ACTIVE", "user1", "User One");

        verify(historyRepository).save(any(ConventionHistory.class));
    }

    @Test
    void testRecordDelete() {
        when(historyRepository.save(any(ConventionHistory.class))).thenReturn(new ConventionHistory());

        conventionHistoryService.recordDelete(mockConvention, "user1", "User One");

        verify(historyRepository).save(any(ConventionHistory.class));
    }

    @Test
    void testGetConventionHistory() {
        List<ConventionHistory> history = new ArrayList<>();
        when(historyRepository.findByConventionIdOrderByModifiedAtDesc("conv1")).thenReturn(history);

        List<ConventionHistory> result = conventionHistoryService.getConventionHistory("conv1");

        assertNotNull(result);
        verify(historyRepository).findByConventionIdOrderByModifiedAtDesc("conv1");
    }

    @Test
    void testGetHistoryByReference() {
        List<ConventionHistory> history = new ArrayList<>();
        when(historyRepository.findByConventionReferenceOrderByModifiedAtDesc("REF001")).thenReturn(history);

        List<ConventionHistory> result = conventionHistoryService.getHistoryByReference("REF001");

        assertNotNull(result);
        verify(historyRepository).findByConventionReferenceOrderByModifiedAtDesc("REF001");
    }

    @Test
    void testGetUserHistory() {
        List<ConventionHistory> history = new ArrayList<>();
        when(historyRepository.findByModifiedByOrderByModifiedAtDesc("user1")).thenReturn(history);

        List<ConventionHistory> result = conventionHistoryService.getUserHistory("user1");

        assertNotNull(result);
        verify(historyRepository).findByModifiedByOrderByModifiedAtDesc("user1");
    }

    @Test
    void testCountModifications() {
        when(historyRepository.countByConventionId("conv1")).thenReturn(5L);

        long count = conventionHistoryService.countModifications("conv1");

        assertEquals(5L, count);
        verify(historyRepository).countByConventionId("conv1");
    }
}
