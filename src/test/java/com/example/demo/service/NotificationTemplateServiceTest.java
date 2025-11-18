package com.example.demo.service;

import com.example.demo.model.NotificationTemplate;
import com.example.demo.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private NotificationTemplateService templateService;

    private NotificationTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testTemplate = new NotificationTemplate();
        testTemplate.setId("test_template");
        testTemplate.setName("Test Template");
        testTemplate.setType("EMAIL");
        testTemplate.setSubject("Test Subject {Variable1}");
        testTemplate.setContent("Hello {Variable1}, your {Variable2} is ready.");
        testTemplate.setVariables(Arrays.asList("Variable1", "Variable2"));
        testTemplate.setActive(true);
    }

    @Test
    void testGetTemplate() {
        when(templateRepository.findById("test_template")).thenReturn(Optional.of(testTemplate));

        Optional<NotificationTemplate> result = templateService.getTemplate("test_template");

        assertTrue(result.isPresent());
        assertEquals("Test Template", result.get().getName());
        verify(templateRepository).findById("test_template");
    }

    @Test
    void testGetAllActiveTemplates() {
        when(templateRepository.findByActiveTrue()).thenReturn(Arrays.asList(testTemplate));

        List<NotificationTemplate> result = templateService.getAllActiveTemplates();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(templateRepository).findByActiveTrue();
    }

    @Test
    void testGetTemplatesByType() {
        when(templateRepository.findByTypeAndActiveTrue("EMAIL")).thenReturn(Arrays.asList(testTemplate));

        List<NotificationTemplate> result = templateService.getTemplatesByType("EMAIL");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(templateRepository).findByTypeAndActiveTrue("EMAIL");
    }

    @Test
    void testSaveTemplate() {
        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(testTemplate);

        NotificationTemplate result = templateService.saveTemplate(testTemplate);

        assertNotNull(result);
        assertNotNull(result.getUpdatedAt());
        verify(templateRepository).save(testTemplate);
    }

    @Test
    void testDeleteTemplate() {
        doNothing().when(templateRepository).deleteById("test_template");

        templateService.deleteTemplate("test_template");

        verify(templateRepository).deleteById("test_template");
    }

    @Test
    void testDeactivateTemplate() {
        when(templateRepository.findById("test_template")).thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(testTemplate);

        templateService.deactivateTemplate("test_template");

        assertFalse(testTemplate.isActive());
        verify(templateRepository).save(testTemplate);
    }

    @Test
    void testGenerateMessage() {
        when(templateRepository.findById("test_template")).thenReturn(Optional.of(testTemplate));

        Map<String, String> variables = new HashMap<>();
        variables.put("Variable1", "John");
        variables.put("Variable2", "order");

        String result = templateService.generateMessage("test_template", variables);

        assertNotNull(result);
        assertTrue(result.contains("John"));
        assertTrue(result.contains("order"));
    }

    @Test
    void testGenerateSubject() {
        when(templateRepository.findById("test_template")).thenReturn(Optional.of(testTemplate));

        Map<String, String> variables = new HashMap<>();
        variables.put("Variable1", "John");

        String result = templateService.generateSubject("test_template", variables);

        assertNotNull(result);
        assertTrue(result.contains("John"));
    }

    @Test
    void testExtractVariables() {
        when(templateRepository.findById("test_template")).thenReturn(Optional.of(testTemplate));

        List<String> result = templateService.extractVariables("test_template");

        assertNotNull(result);
        assertTrue(result.contains("Variable1"));
        assertTrue(result.contains("Variable2"));
    }

    @Test
    void testValidateTemplate_Valid() {
        boolean result = templateService.validateTemplate(testTemplate);

        assertTrue(result);
    }

    @Test
    void testValidateTemplate_NullTemplate() {
        boolean result = templateService.validateTemplate(null);

        assertFalse(result);
    }

    @Test
    void testValidateTemplate_EmptyName() {
        testTemplate.setName("");

        boolean result = templateService.validateTemplate(testTemplate);

        assertFalse(result);
    }

    @Test
    void testValidateTemplate_InvalidType() {
        testTemplate.setType("INVALID");

        boolean result = templateService.validateTemplate(testTemplate);

        assertFalse(result);
    }

    @Test
    void testInitializeDefaultTemplates() {
        when(templateRepository.existsById(anyString())).thenReturn(false);
        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(testTemplate);

        templateService.initializeDefaultTemplates();

        verify(templateRepository, atLeastOnce()).save(any(NotificationTemplate.class));
    }

    @Test
    void testCreateInvoiceVariables() {
        Object invoice = new Object();
        Object convention = new Object();
        Object user = new Object();

        Map<String, String> result = templateService.createInvoiceVariables(invoice, convention, user);

        assertNotNull(result);
    }

    @Test
    void testGenerateMessage_TemplateNotFound() {
        when(templateRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            templateService.generateMessage("nonexistent", new HashMap<>());
        });
    }

    @Test
    void testGenerateSubject_TemplateNotFound() {
        when(templateRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            templateService.generateSubject("nonexistent", new HashMap<>());
        });
    }

    @Test
    void testExtractVariables_TemplateNotFound() {
        when(templateRepository.findById("nonexistent")).thenReturn(Optional.empty());

        List<String> result = templateService.extractVariables("nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
