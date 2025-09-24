package com.example.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Configuration pour Swagger/OpenAPI.
 * Cette classe configure l'interface Swagger UI et la documentation OpenAPI.
 */
@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
@OpenAPIDefinition
public class OpenApiConfig {

    @Value("${spring.application.name:API Application}")
    private String applicationName;

    @Bean
    @Primary
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
            .info(new Info()
                .title(applicationName + " - API Documentation")
                .description(
                    "<h3>Documentation complète de l'API</h3>" +
                    "<p>Cette documentation permet de tester tous les endpoints de l'application, y compris :</p>" +
                    "<ul>" +
                    "<li><strong>Authentification</strong> : inscription, connexion, gestion des rôles</li>" +
                    "<li><strong>Utilisateurs</strong> : gestion des profils et des permissions</li>" +
                    "<li><strong>Autres fonctionnalités</strong> : selon les modules de votre application</li>" +
                    "</ul>" +
                    "<p><strong>Comment utiliser cette documentation :</strong></p>" +
                    "<ol>" +
                    "<li>Pour les endpoints sécurisés, utilisez d'abord l'endpoint de connexion pour obtenir un token JWT</li>" +
                    "<li>Cliquez sur le bouton 'Authorize' en haut à droite et entrez votre token (sans le préfixe 'Bearer')</li>" +
                    "<li>Vous pouvez maintenant tester tous les endpoints sécurisés</li>" +
                    "</ol>"
                )
                .version("1.0.0")
                .contact(new Contact()
                    .name("Support Team")
                    .email("support@example.com")
                    .url("https://example.com"))
                .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .servers(List.of(
                new Server().url("/").description("Serveur local")
            ));
    }
}
