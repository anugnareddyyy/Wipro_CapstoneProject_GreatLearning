//package com.appverse;
//
//import io.swagger.v3.oas.annotations.OpenAPIDefinition;
//import io.swagger.v3.oas.annotations.info.Contact;
//import io.swagger.v3.oas.annotations.info.Info;
//import io.swagger.v3.oas.annotations.info.License;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AppVerse AI - Smart App Marketplace & Analytics Platform
 *
 * Main entry point for the Spring Boot application.
 * This application provides REST APIs for:
 * - Authentication & Role-based Authorization (JWT)
 * - App Marketplace with Search & Filters
 * - AI Recommendation Engine
 * - Developer Console & Analytics
 * - AI Review Sentiment Analysis
 * - Admin Management Dashboard
 *
 * @author Anugna Reddy
 * @version 1.0.0
 */
//@SpringBootApplication
//@OpenAPIDefinition(
//    info = @Info(
//        title = "AppVerse AI API",
//        version = "1.0.0",
//        description = "Smart App Marketplace & Analytics Platform REST API Documentation",
//        contact = @Contact(name = "AppVerse Team", email = "support@appverse.ai"),
//        license = @License(name = "Apache 2.0")
//    )
//)
//public class AppVerseApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(AppVerseApplication.class, args);
//    }
//}

package com.appverse;
 
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
 
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "AppVerse AI API",
        version = "1.0.0",
        description = "Smart App Marketplace & Analytics Platform"
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class AppVerseApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppVerseApplication.class, args);
    }
}
