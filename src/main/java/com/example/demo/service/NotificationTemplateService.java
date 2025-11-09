package com.example.demo.service;

import com.example.demo.model.NotificationTemplate;
import com.example.demo.repository.NotificationTemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Locale;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTemplateService {

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    private JsonNode emailTemplates;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Map<String, String> SUPPORTED_LANGUAGES = new HashMap<>();
    static {
        SUPPORTED_LANGUAGES.put("fr", "Français");
        SUPPORTED_LANGUAGES.put("en", "English");
        SUPPORTED_LANGUAGES.put("ar", "العربية");
    }

    /**
     * Templates par défaut
     */
    private static final Map<String, NotificationTemplate> DEFAULT_TEMPLATES = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        try (InputStream is = resourceLoader.getResource("classpath:templates/email/notification-templates.json").getInputStream()) {
            emailTemplates = mapper.readTree(is);
        }
    }

    /**
     * Récupère un template selon les préférences de l'utilisateur
     */
    public String getPersonalizedTemplate(String type, String language, String channel, Map<String, Object> variables) {
        if (!emailTemplates.has(type)) {
            return getDefaultTemplate(type, variables);
        }

        // Langue par défaut si non supportée
        if (!SUPPORTED_LANGUAGES.containsKey(language)) {
            language = "fr";
        }

        JsonNode templateNode = emailTemplates.get(type).get(language).get(channel);
        if (templateNode == null) {
            return getDefaultTemplate(type, variables);
        }

        String template;
        if (channel.equals("email")) {
            template = templateNode.get("body").asText();
        } else {
            template = templateNode.get("content").asText();
        }

        return replaceVariables(template, variables, language);
    }

    /**
     * Récupère le sujet de l'email selon la langue
     */
    public String getEmailSubject(String type, String language, Map<String, Object> variables) {
        if (!emailTemplates.has(type)) {
            return DEFAULT_TEMPLATES.get(type).getSubject();
        }

        if (!SUPPORTED_LANGUAGES.containsKey(language)) {
            language = "fr";
        }

        JsonNode templateNode = emailTemplates.get(type).get(language).get("email");
        if (templateNode == null || !templateNode.has("subject")) {
            return DEFAULT_TEMPLATES.get(type).getSubject();
        }

        return replaceVariables(templateNode.get("subject").asText(), variables, language);
    }

    /**
     * Remplace les variables dans le template
     */
    private String replaceVariables(String template, Map<String, Object> variables, String language) {
        String result = template;
        
        // Formattage selon la locale
        Locale locale = new Locale(language);
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale);

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            Object value = entry.getValue();
            
            // Formattage spécial selon le type de variable
            String formattedValue;
            if (value instanceof Number) {
                formattedValue = numberFormat.format(value);
            } else if (value instanceof java.time.temporal.Temporal) {
                formattedValue = dateFormat.format((java.time.temporal.Temporal)value);
            } else {
                formattedValue = value.toString();
            }
            
            result = result.replace(key, formattedValue);
        }
        
        return result;
    }

    /**
     * Récupère le template par défaut et remplace les variables
     */
    private String getDefaultTemplate(String type, Map<String, Object> variables) {
        NotificationTemplate template = DEFAULT_TEMPLATES.get(type);
        if (template == null) {
            throw new IllegalArgumentException("Template inconnu: " + type);
        }
        return replaceVariables(template.getContent(), variables, "fr");
    }

    static {
        // Template pour rappel de facture
        NotificationTemplate invoiceReminder = new NotificationTemplate();
        invoiceReminder.setId("invoice_reminder");
        invoiceReminder.setName("Rappel de facture");
        invoiceReminder.setDescription("Rappel avant échéance de facture");
        invoiceReminder.setType("EMAIL");
        invoiceReminder.setSubject("Facture en attente – {NomClient}");
        invoiceReminder.setContent(
            "Bonjour {NomCommercial},\n\n" +
            "La facture N°{NumeroFacture} pour le client {NomClient} d'un montant de {Montant}€ arrive à échéance le {DateEcheance}.\n\n" +
            "Merci de suivre cette facture dans l'application.\n\n" +
            "Cordialement,\n" +
            "Système de notifications"
        );
        invoiceReminder.setVariables(Arrays.asList("NomCommercial", "NumeroFacture", "NomClient", "Montant", "DateEcheance"));
        invoiceReminder.setActive(true);
        DEFAULT_TEMPLATES.put("invoice_reminder", invoiceReminder);

        // Template pour facture en retard
        NotificationTemplate invoiceOverdue = new NotificationTemplate();
        invoiceOverdue.setId("invoice_overdue");
        invoiceOverdue.setName("Facture en retard");
        invoiceOverdue.setDescription("Notification de facture en retard");
        invoiceOverdue.setType("EMAIL");
        invoiceOverdue.setSubject("URGENT - Facture en retard – {NomClient}");
        invoiceOverdue.setContent(
            "Bonjour {NomCommercial},\n\n" +
            "ATTENTION : La facture N°{NumeroFacture} pour le client {NomClient} d'un montant de {Montant}€ est en retard de {JoursRetard} jour(s).\n\n" +
            "Date d'échéance : {DateEcheance}\n" +
            "Merci de régulariser cette situation dans les plus brefs délais.\n\n" +
            "Cordialement,\n" +
            "Système de notifications"
        );
        invoiceOverdue.setVariables(Arrays.asList("NomCommercial", "NumeroFacture", "NomClient", "Montant", "JoursRetard", "DateEcheance"));
        invoiceOverdue.setActive(true);
        DEFAULT_TEMPLATES.put("invoice_overdue", invoiceOverdue);

        // Template pour confirmation de paiement
        NotificationTemplate paymentConfirmation = new NotificationTemplate();
        paymentConfirmation.setId("payment_confirmation");
        paymentConfirmation.setName("Confirmation de paiement");
        paymentConfirmation.setDescription("Confirmation après paiement de facture");
        paymentConfirmation.setType("EMAIL");
        paymentConfirmation.setSubject("Confirmation de paiement – {NumeroFacture}");
        paymentConfirmation.setContent(
            "Bonjour {NomCommercial},\n\n" +
            "Nous confirmons la réception du paiement de {Montant}€ pour la facture N°{NumeroFacture} du client {NomClient}.\n\n" +
            "Date de paiement : {DatePaiement}\n" +
            "Merci pour votre suivi.\n\n" +
            "Cystème de notifications"
        );
        paymentConfirmation.setVariables(Arrays.asList("NomCommercial", "NumeroFacture", "NomClient", "Montant", "DatePaiement"));
        paymentConfirmation.setActive(true);
        DEFAULT_TEMPLATES.put("payment_confirmation", paymentConfirmation);

        // Template pour convention proche de fin
        NotificationTemplate conventionDeadline = new NotificationTemplate();
        conventionDeadline.setId("convention_deadline");
        conventionDeadline.setName("Convention proche de fin");
        conventionDeadline.setDescription("Alerte convention proche de fin");
        conventionDeadline.setType("EMAIL");
        conventionDeadline.setSubject("Convention proche de fin – {NomClient}");
        conventionDeadline.setContent(
            "Bonjour {NomCommercial},\n\n" +
            "La convention {ReferenceConvention} pour le client {NomClient} se termine dans {JoursRestants} jour(s).\n\n" +
            "Date de fin : {DateFin}\n" +
            "Statut actuel : {StatutConvention}\n\n" +
            "Veuillez vérifier si une reconduction est nécessaire.\n\n" +
            "Cordialement,\n" +
            "Système de notifications"
        );
        conventionDeadline.setVariables(Arrays.asList("NomCommercial", "ReferenceConvention", "NomClient", "JoursRestants", "DateFin", "StatutConvention"));
        conventionDeadline.setActive(true);
        DEFAULT_TEMPLATES.put("convention_deadline", conventionDeadline);

        // Template SMS pour urgences
        NotificationTemplate smsUrgent = new NotificationTemplate();
        smsUrgent.setId("sms_urgent");
        smsUrgent.setName("SMS Urgent");
        smsUrgent.setDescription("SMS pour notifications urgentes");
        smsUrgent.setType("SMS");
        smsUrgent.setSubject(""); // Pas de sujet pour SMS
        smsUrgent.setContent("URGENT: {TypeNotification} - {Details} - {DateEcheance}");
        smsUrgent.setVariables(Arrays.asList("TypeNotification", "Details", "DateEcheance"));
        smsUrgent.setActive(true);
        DEFAULT_TEMPLATES.put("sms_urgent", smsUrgent);
    }

    /**
     * Initialiser les templates par défaut
     */
    public void initializeDefaultTemplates() {
        System.out.println("🔧 [TEMPLATES] Initialisation des templates par défaut...");
        
        for (NotificationTemplate template : DEFAULT_TEMPLATES.values()) {
            if (!templateRepository.existsById(template.getId())) {
                templateRepository.save(template);
                System.out.println("✅ [TEMPLATES] Template créé: " + template.getName());
            }
        }
        
        System.out.println("✅ [TEMPLATES] Initialisation terminée");
    }

    /**
     * Récupérer un template par ID
     */
    public Optional<NotificationTemplate> getTemplate(String templateId) {
        return templateRepository.findById(templateId);
    }

    /**
     * Récupérer tous les templates actifs
     */
    public List<NotificationTemplate> getAllActiveTemplates() {
        return templateRepository.findByActiveTrue();
    }

    /**
     * Récupérer les templates par type
     */
    public List<NotificationTemplate> getTemplatesByType(String type) {
        return templateRepository.findByTypeAndActiveTrue(type);
    }

    /**
     * Créer ou mettre à jour un template
     */
    public NotificationTemplate saveTemplate(NotificationTemplate template) {
        template.setUpdatedAt(new Date());
        return templateRepository.save(template);
    }

    /**
     * Supprimer un template
     */
    public void deleteTemplate(String templateId) {
        templateRepository.deleteById(templateId);
    }

    /**
     * Rendre un template inactif
     */
    public void deactivateTemplate(String templateId) {
        Optional<NotificationTemplate> templateOpt = templateRepository.findById(templateId);
        if (templateOpt.isPresent()) {
            NotificationTemplate template = templateOpt.get();
            template.setActive(false);
            template.setUpdatedAt(new Date());
            templateRepository.save(template);
        }
    }

    /**
     * Générer le contenu d'un message à partir d'un template
     */
    public String generateMessage(String templateId, Map<String, String> variables) {
        Optional<NotificationTemplate> templateOpt = getTemplate(templateId);
        if (templateOpt.isEmpty()) {
            throw new IllegalArgumentException("Template non trouvé: " + templateId);
        }

        NotificationTemplate template = templateOpt.get();
        return replaceVariables(template.getContent(), variables);
    }

    /**
     * Générer le sujet d'un email à partir d'un template
     */
    public String generateSubject(String templateId, Map<String, String> variables) {
        Optional<NotificationTemplate> templateOpt = getTemplate(templateId);
        if (templateOpt.isEmpty()) {
            throw new IllegalArgumentException("Template non trouvé: " + templateId);
        }

        NotificationTemplate template = templateOpt.get();
        return replaceVariables(template.getSubject(), variables);
    }

    /**
     * Remplacer les variables dans un texte
     */
    private String replaceVariables(String text, Map<String, String> variables) {
        if (text == null || variables == null) {
            return text;
        }

        String result = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * Extraire les variables d'un template
     */
    public List<String> extractVariables(String templateId) {
        Optional<NotificationTemplate> templateOpt = getTemplate(templateId);
        if (templateOpt.isEmpty()) {
            return new ArrayList<>();
        }

        NotificationTemplate template = templateOpt.get();
        List<String> variables = new ArrayList<>();
        
        // Extraire les variables du contenu
        variables.addAll(extractVariablesFromText(template.getContent()));
        
        // Extraire les variables du sujet
        variables.addAll(extractVariablesFromText(template.getSubject()));
        
        // Supprimer les doublons
        return new ArrayList<>(new HashSet<>(variables));
    }

    /**
     * Extraire les variables d'un texte
     */
    private List<String> extractVariablesFromText(String text) {
        List<String> variables = new ArrayList<>();
        if (text == null) {
            return variables;
        }

        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return variables;
    }

    /**
     * Valider un template
     */
    public boolean validateTemplate(NotificationTemplate template) {
        if (template == null) {
            return false;
        }

        // Vérifier les champs obligatoires
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            return false;
        }

        if (template.getType() == null || template.getType().trim().isEmpty()) {
            return false;
        }

        if (template.getContent() == null || template.getContent().trim().isEmpty()) {
            return false;
        }

        // Vérifier que le type est valide
        if (!Arrays.asList("EMAIL", "SMS", "PUSH").contains(template.getType().toUpperCase())) {
            return false;
        }

        // Vérifier que les variables sont cohérentes
        List<String> contentVariables = extractVariablesFromText(template.getContent());
        List<String> subjectVariables = extractVariablesFromText(template.getSubject());
        
        if (template.getVariables() != null) {
            Set<String> declaredVariables = new HashSet<>(template.getVariables());
            Set<String> usedVariables = new HashSet<>();
            usedVariables.addAll(contentVariables);
            usedVariables.addAll(subjectVariables);
            
            // Vérifier que toutes les variables utilisées sont déclarées
            for (String usedVar : usedVariables) {
                if (!declaredVariables.contains(usedVar)) {
                    System.out.println("⚠️ [TEMPLATES] Variable utilisée mais non déclarée: " + usedVar);
                }
            }
        }

        return true;
    }

    /**
     * Créer des variables par défaut pour une facture
     */
    public Map<String, String> createInvoiceVariables(Object invoice, Object convention, Object user) {
        Map<String, String> variables = new HashMap<>();
        
        try {
            // Variables de la facture
            if (invoice != null) {
                variables.put("NumeroFacture", getFieldValue(invoice, "reference"));
                variables.put("Montant", getFieldValue(invoice, "amount"));
                variables.put("DateEcheance", formatDate(getFieldValue(invoice, "dueDate")));
                variables.put("DatePaiement", formatDate(getFieldValue(invoice, "paymentDate")));
            }
            
            // Variables de la convention
            if (convention != null) {
                variables.put("ReferenceConvention", getFieldValue(convention, "reference"));
                variables.put("NomClient", getFieldValue(convention, "clientName"));
                variables.put("DateFin", formatDate(getFieldValue(convention, "endDate")));
                variables.put("StatutConvention", getFieldValue(convention, "status"));
            }
            
            // Variables de l'utilisateur
            if (user != null) {
                variables.put("NomCommercial", getFieldValue(user, "username"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ [TEMPLATES] Erreur création variables: " + e.getMessage());
        }
        
        return variables;
    }

    /**
     * Obtenir la valeur d'un champ d'un objet
     */
    private String getFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Formater une date
     */
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }
}











