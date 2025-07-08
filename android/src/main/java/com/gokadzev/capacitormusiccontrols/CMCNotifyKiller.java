package com.gokadzev.capacitormusiccontrols;

import static android.os.PowerManager.PARTIAL_WAKE_LOCK;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import java.lang.ref.WeakReference;

public class CMCNotifyKiller extends Service {

    private static final String TAG = "cmcapp:CMCNotifyKiller";

    private static int NOTIFICATION_ID;
    private NotificationManager mNM;
    private final IBinder mBinder = new KillBinder(this);

    // Partial wake lock to prevent the app from going to sleep when locked
    private PowerManager.WakeLock wakeLock;

    private WeakReference<Notification> notification;

    private boolean foregroundStarted = false;

    private Activity activity;
    private ServiceConnection connection;
    private boolean bounded;

    public CMCNotifyKiller setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public CMCNotifyKiller setConnection(ServiceConnection connection) {
        this.connection = connection;
        return this;
    }

    public CMCNotifyKiller setBounded(boolean bounded) {
        this.bounded = bounded;
        return this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        this.NOTIFICATION_ID = intent.getIntExtra("notificationID", 1);
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    public void setNotification(Notification n) {
        Log.i(TAG, "setNotification");
        if (notification != null) {
            if (n == null) {
                sleepWell(true);
            }
            notification = null;
        }
        if (n != null) {
            notification = new WeakReference<Notification>(n);
            keepAwake(wakeLock == null);
        }
    }

    public Notification getNotification() {
        return notification != null ? notification.get() : null;
    }

    /**
     * Put the service in a foreground state to prevent app from being killed
     * by the OS.
     */
    private void keepAwake(boolean do_wakelock) {
        if (notification != null && notification.get() != null && !foregroundStarted) {
            Log.i(TAG, "Starting ForegroundService");
            
            // For Android 14+ (API 34+), specify the foreground service type
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    startForeground(this.NOTIFICATION_ID, notification.get(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to start foreground service with media playback type", e);
                    startForeground(this.NOTIFICATION_ID, notification.get());
                }
            } else {
                startForeground(this.NOTIFICATION_ID, notification.get());
            }
            foregroundStarted = true;
        }

        if (do_wakelock) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

            wakeLock = pm.newWakeLock(PARTIAL_WAKE_LOCK, TAG);

            Log.i(TAG, "Acquiring LOCK");
            wakeLock.acquire();
            if (wakeLock.isHeld()) {
                Log.i(TAG, "wakeLock acquired");
            } else {
                Log.e(TAG, "wakeLock not acquired yet");
            }
        }
    }

    /**
     * Shared manager for the notification service.
     */
    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNM.cancel(NOTIFICATION_ID);
    }

    /**
     * Stop background mode.
     */
    private void sleepWell(boolean do_wakelock) {
        Log.i(TAG, "Stopping WakeLock");
        if (foregroundStarted) {
            Log.i(TAG, "Stopping ForegroundService");
            stopForeground(true);
            foregroundStarted = false;
            Log.i(TAG, "ForegroundService stopped");
        }
        mNM.cancel(NOTIFICATION_ID);

        if (wakeLock != null && do_wakelock) {
            if (wakeLock.isHeld()) {
                try {
                    wakeLock.release();
                    Log.i(TAG, "wakeLock released");
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            } else {
                Log.i(TAG, "wakeLock not held");
            }
            wakeLock = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sleepWell(true);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        sleepWell(true);
        if (bounded) {
            activity.unbindService(connection);
        }
    }
}
