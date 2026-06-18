package com.appverse.pattern.observer;
 
import com.appverse.entity.App;
import com.appverse.entity.AppStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
import java.time.LocalDateTime;
 
/**
* Observer Pattern — Concrete Observer 2.
* Logs all app status changes to audit trail.
* Every status transition is recorded with timestamp.
*/
@Component
@Slf4j
public class AuditLogObserver implements AppStatusObserver {
 
    @Override
    public void onStatusChanged(App app, AppStatus oldStatus, AppStatus newStatus) {
        log.info("AUDIT LOG | AppId={} | Name='{}' | Developer='{}' | " +
                 "StatusChange: {} → {} | Timestamp={}",
                app.getAppId(),
                app.getName(),
                app.getDeveloper().getUsername(),
                oldStatus,
                newStatus,
                LocalDateTime.now()
        );
    }
}