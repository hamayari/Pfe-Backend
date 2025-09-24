package com.example.demo.service;

import com.example.demo.model.MessageAttachment;
import com.example.demo.repository.MessageAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    @Autowired
    private MessageAttachmentRepository attachmentRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:10485760}") // 10MB par défaut
    private long maxFileSize;

    private final String[] allowedExtensions = {
        "jpg", "jpeg", "png", "gif", "bmp", "webp", // Images
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", // Documents
        "txt", "rtf", "csv", // Texte
        "zip", "rar", "7z", // Archives
        "mp3", "wav", "ogg", // Audio
        "mp4", "avi", "mov", "webm" // Vidéo
    };

    public MessageAttachment uploadFile(MultipartFile file, String conversationId, String messageId, String uploadedBy, String uploadedByName) throws IOException {
        // Validations
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Le fichier est trop volumineux (max: " + (maxFileSize / 1024 / 1024) + "MB)");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        
        if (!isAllowedExtension(fileExtension)) {
            throw new IllegalArgumentException("Type de fichier non autorisé: " + fileExtension);
        }

        // Générer un nom unique
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        
        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        
        // Sauvegarder le fichier
        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Créer l'entité attachment
        MessageAttachment attachment = new MessageAttachment(fileName, file.getContentType(), file.getSize(), uploadedBy);
        attachment.setOriginalFileName(originalFileName);
        attachment.setMessageId(messageId);
        attachment.setConversationId(conversationId);
        attachment.setUploadedByName(uploadedByName);
        attachment.setFilePath(targetLocation.toString());
        attachment.setDownloadUrl("/api/messaging/attachments/" + attachment.getId() + "/download");

        // Si c'est une image, générer une miniature
        if (attachment.isImage()) {
            try {
                generateThumbnail(targetLocation, attachment);
            } catch (Exception e) {
                System.err.println("Erreur génération miniature: " + e.getMessage());
            }
        }

        return attachmentRepository.save(attachment);
    }

    public Resource loadFileAsResource(String attachmentId) throws Exception {
        MessageAttachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Pièce jointe non trouvée"));

        if (attachment.isDeleted()) {
            throw new RuntimeException("Pièce jointe supprimée");
        }

        try {
            Path filePath = Paths.get(attachment.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier non trouvé: " + attachment.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Fichier non trouvé: " + attachment.getFileName(), ex);
        }
    }

    public List<MessageAttachment> getMessageAttachments(String messageId) {
        return attachmentRepository.findByMessageIdAndDeletedFalse(messageId);
    }

    public List<MessageAttachment> getConversationAttachments(String conversationId) {
        return attachmentRepository.findByConversationIdAndDeletedFalseOrderByUploadedAtDesc(conversationId);
    }

    public void deleteAttachment(String attachmentId, String userId) {
        MessageAttachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Pièce jointe non trouvée"));

        // Vérifier que l'utilisateur peut supprimer ce fichier
        if (!attachment.getUploadedBy().equals(userId)) {
            throw new RuntimeException("Vous ne pouvez pas supprimer cette pièce jointe");
        }

        attachment.setDeleted(true);
        attachment.setDeletedAt(LocalDateTime.now());
        attachmentRepository.save(attachment);

        // Optionnel : supprimer physiquement le fichier
        try {
            Files.deleteIfExists(Paths.get(attachment.getFilePath()));
            if (attachment.getThumbnailPath() != null) {
                Files.deleteIfExists(Paths.get(attachment.getThumbnailPath()));
            }
        } catch (IOException e) {
            System.err.println("Erreur suppression fichier physique: " + e.getMessage());
        }
    }

    private void generateThumbnail(Path imagePath, MessageAttachment attachment) throws IOException {
        BufferedImage originalImage = ImageIO.read(imagePath.toFile());
        if (originalImage == null) return;

        attachment.setImageWidth(originalImage.getWidth());
        attachment.setImageHeight(originalImage.getHeight());

        // Générer miniature (200x200 max)
        int thumbnailWidth = 200;
        int thumbnailHeight = 200;
        
        // Calculer les dimensions en conservant le ratio
        double ratio = Math.min((double) thumbnailWidth / originalImage.getWidth(), 
                               (double) thumbnailHeight / originalImage.getHeight());
        
        int scaledWidth = (int) (originalImage.getWidth() * ratio);
        int scaledHeight = (int) (originalImage.getHeight() * ratio);

        BufferedImage thumbnailImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnailImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        // Sauvegarder la miniature
        String thumbnailFileName = "thumb_" + attachment.getFileName();
        Path thumbnailPath = imagePath.getParent().resolve(thumbnailFileName);
        ImageIO.write(thumbnailImage, "jpg", thumbnailPath.toFile());
        
        attachment.setThumbnailPath(thumbnailPath.toString());
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        for (String allowed : allowedExtensions) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    public long getTotalStorageUsed() {
        return attachmentRepository.findByDeletedFalse().stream()
            .mapToLong(MessageAttachment::getFileSize)
            .sum();
    }

    public void cleanupOldDeletedFiles(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        List<MessageAttachment> oldDeleted = attachmentRepository.findByDeletedTrueAndDeletedAtBefore(cutoff);
        
        for (MessageAttachment attachment : oldDeleted) {
            try {
                Files.deleteIfExists(Paths.get(attachment.getFilePath()));
                if (attachment.getThumbnailPath() != null) {
                    Files.deleteIfExists(Paths.get(attachment.getThumbnailPath()));
                }
                attachmentRepository.delete(attachment);
            } catch (IOException e) {
                System.err.println("Erreur nettoyage fichier: " + e.getMessage());
            }
        }
    }
}





