package com.appverse.pattern.observer;
 
import com.appverse.entity.App;
import com.appverse.entity.AppStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
import java.util.List;
 
/**
* Observer Pattern — Subject (Publisher).
* Maintains a list of observers and notifies all of them
* when an app's status changes.
*
* Usage in AppService:
*   publisher.notifyObservers(app, oldStatus, newStatus);
*/
@Component
@RequiredArgsConstructor
@Slf4j
public class AppStatusEventPublisher {
 
    private final List<AppStatusObserver> observers;
 
    /**
     * Notify all registered observers about a status change.
     *
     * @param app       the app whose status changed
     * @param oldStatus previous status
     * @param newStatus new status
     */
    public void notifyObservers(App app, AppStatus oldStatus, AppStatus newStatus) {
        log.debug("Notifying {} observers for app status change: {} → {}",
                observers.size(), oldStatus, newStatus);
 
        for (AppStatusObserver observer : observers) {
            try {
                observer.onStatusChanged(app, oldStatus, newStatus);
            } catch (Exception e) {
                log.error("Observer {} failed: {}", observer.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}