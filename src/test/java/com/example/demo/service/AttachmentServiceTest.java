package com.example.demo.service;

import com.example.demo.model.MessageAttachment;
import com.example.demo.repository.MessageAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private MessageAttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attachmentService, "uploadDir", "test-uploads");
        ReflectionTestUtils.setField(attachmentService, "maxFileSize", 10485760L);
    }

    @Test
    void testUploadFile_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> {
            attachmentService.uploadFile(file, "conv1", "msg1", "user1", "User One");
        });
    }

    @Test
    void testUploadFile_FileTooLarge() {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile("file", "large.pdf", "application/pdf", largeContent);

        assertThrows(IllegalArgumentException.class, () -> {
            attachmentService.uploadFile(file, "conv1", "msg1", "user1", "User One");
        });
    }

    @Test
    void testUploadFile_InvalidExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "test.exe", "application/exe", "content".getBytes());

        assertThrows(IllegalArgumentException.class, () -> {
            attachmentService.uploadFile(file, "conv1", "msg1", "user1", "User One");
        });
    }

    @Test
    void testGetMessageAttachments() {
        List<MessageAttachment> attachments = new ArrayList<>();
        MessageAttachment att1 = new MessageAttachment("file1.pdf", "application/pdf", 1024L, "user1");
        attachments.add(att1);

        when(attachmentRepository.findByMessageIdAndDeletedFalse("msg1")).thenReturn(attachments);

        List<MessageAttachment> result = attachmentService.getMessageAttachments("msg1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(attachmentRepository).findByMessageIdAndDeletedFalse("msg1");
    }

    @Test
    void testGetConversationAttachments() {
        List<MessageAttachment> attachments = new ArrayList<>();
        when(attachmentRepository.findByConversationIdAndDeletedFalseOrderByUploadedAtDesc("conv1"))
            .thenReturn(attachments);

        List<MessageAttachment> result = attachmentService.getConversationAttachments("conv1");

        assertNotNull(result);
        verify(attachmentRepository).findByConversationIdAndDeletedFalseOrderByUploadedAtDesc("conv1");
    }

    @Test
    void testDeleteAttachment_NotFound() {
        when(attachmentRepository.findById("att1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            attachmentService.deleteAttachment("att1", "user1");
        });
    }

    @Test
    void testDeleteAttachment_Unauthorized() {
        MessageAttachment attachment = new MessageAttachment("file.pdf", "application/pdf", 1024L, "user2");
        attachment.setId("att1");
        when(attachmentRepository.findById("att1")).thenReturn(Optional.of(attachment));

        assertThrows(RuntimeException.class, () -> {
            attachmentService.deleteAttachment("att1", "user1");
        });
    }

    @Test
    void testGetTotalStorageUsed() {
        List<MessageAttachment> attachments = new ArrayList<>();
        MessageAttachment att1 = new MessageAttachment("file1.pdf", "application/pdf", 1024L, "user1");
        MessageAttachment att2 = new MessageAttachment("file2.pdf", "application/pdf", 2048L, "user1");
        attachments.add(att1);
        attachments.add(att2);

        when(attachmentRepository.findByDeletedFalse()).thenReturn(attachments);

        long totalStorage = attachmentService.getTotalStorageUsed();

        assertEquals(3072L, totalStorage);
    }

    @Test
    void testLoadFileAsResource_NotFound() {
        when(attachmentRepository.findById("att1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            attachmentService.loadFileAsResource("att1");
        });
    }

    @Test
    void testLoadFileAsResource_Deleted() {
        MessageAttachment attachment = new MessageAttachment("file.pdf", "application/pdf", 1024L, "user1");
        attachment.setId("att1");
        attachment.setDeleted(true);
        when(attachmentRepository.findById("att1")).thenReturn(Optional.of(attachment));

        assertThrows(RuntimeException.class, () -> {
            attachmentService.loadFileAsResource("att1");
        });
    }
}
