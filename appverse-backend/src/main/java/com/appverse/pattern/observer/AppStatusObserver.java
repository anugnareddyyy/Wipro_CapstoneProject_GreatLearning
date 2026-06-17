package com.appverse.pattern.observer;
 
import com.appverse.entity.App;
import com.appverse.entity.AppStatus;
 
/**
* Observer Pattern — Observer interface.
* All classes that need to react to app status changes
* must implement this interface.
*
* Concrete observers:
* - DeveloperNotificationObserver (notify developer)
* - AnalyticsObserver (update analytics on approval)
* - AuditLogObserver (log all status changes)
*/
public interface AppStatusObserver {
    void onStatusChanged(App app, AppStatus oldStatus, AppStatus newStatus);
}