package com.appverse.pattern.observer;
 
import com.appverse.entity.App;
import com.appverse.entity.AppStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
/**
 * Observer Pattern — Concrete Observer 1.
 * Notifies the developer when their app status changes.
 * In production this would send an email or push notification.
 */
@Component
@Slf4j
public class DeveloperNotificationObserver implements AppStatusObserver {
 
    @Override
    public void onStatusChanged(App app, AppStatus oldStatus, AppStatus newStatus) {
        String developerEmail = app.getDeveloper().getEmail();
        String appName = app.getName();
 
        if (newStatus == AppStatus.APPROVED) {
            log.info("NOTIFICATION → Developer {}: Your app '{}' has been APPROVED and is now live!",
                    developerEmail, appName);
            // TODO: Send email via JavaMailSender
            // emailService.sendAppApprovalEmail(developerEmail, appName);
 
        } else if (newStatus == AppStatus.REJECTED) {
            log.info("NOTIFICATION → Developer {}: Your app '{}' has been REJECTED.",
                    developerEmail, appName);
            // TODO: Send rejection email with feedback
        } else if (newStatus == AppStatus.SUSPENDED) {
            log.info("NOTIFICATION → Developer {}: Your app '{}' has been SUSPENDED.",
                    developerEmail, appName);
        }
    }
}
 