package com.gokadzev.capacitormusiccontrols;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.gokadzev.capacitormusiccontrols.capacitormusiccontrolsplugin.R;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class MusicControlsNotification {

    private static final String TAG = "CMCNotification";

    private Activity cordovaActivity;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private int notificationID;
    private MusicControlsInfos infos;
    private Bitmap bitmapCover;
    private String CHANNEL_ID;
    private MediaSessionCompat.Token token;

    public WeakReference<CMCNotifyKiller> killer_service;

    // Public Constructor
    public MusicControlsNotification(Activity cordovaActivity, int id, MediaSessionCompat.Token token) {
        this.CHANNEL_ID = "capacitor-music-channel-id";
        this.notificationID = id;
        this.cordovaActivity = cordovaActivity;
        Context context = cordovaActivity.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //MediaSessionCompat mediaSession = new MediaSessionCompat(context, "session tag");
        this.token = token; //mediaSession.getSessionToken();

        // use channelid for Oreo and higher
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = "capacitor-music-controls-plugin";
            // The user-visible description of the channel.
            String description = "capacitor-music-controls-plugin notification";

            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel mChannel = new NotificationChannel(this.CHANNEL_ID, name, importance);

            // Configure the notification channel.
            mChannel.setDescription(description);

            this.notificationManager.createNotificationChannel(mChannel);
        }
    }

    // Show or update notification
    public void updateNotification(MusicControlsInfos newInfos) {
        Log.i(TAG, "updateNotification: infos: " + newInfos.toString());
        this.getBitmapCover(newInfos.cover);
        this.infos = newInfos;
        this.createBuilder();
        this.createNotification();
    }

    private void createNotification() {
        final Notification noti = this.notificationBuilder.build();
        if (killer_service != null) {
            killer_service.get().setNotification(noti);
        }
        this.notificationManager.notify(this.notificationID, noti);
    }

    public void setKillerService(CMCNotifyKiller s) {
        this.killer_service = new WeakReference<CMCNotifyKiller>(s);
    }

    private boolean hasNotification() {
        return this.killer_service != null && this.killer_service.get().getNotification() != null;
    }

    // Toggle the play/pause button
    public void updateIsPlaying(boolean isPlaying) {
        if (isPlaying == this.infos.isPlaying && this.hasNotification()) {
            return; // Not recreate the notification with the same data
        }

        this.infos.isPlaying = isPlaying;
        this.createBuilder();
        this.createNotification();
    }

    // Toggle the dismissable status
    public void updateDismissable(boolean dismissable) {
        if (dismissable == this.infos.dismissable && hasNotification()) {
            return; // Not recreate the notification with the same data
        }
        this.infos.dismissable = dismissable;
        this.createBuilder();
        this.createNotification();
    }

    // Toggle the dismissable and play/pause status
    public void updateIsPlayingDismissable(boolean isPlaying, boolean dismissable) {
        if (dismissable == this.infos.dismissable && isPlaying == this.infos.isPlaying && hasNotification()) {
            return; // Not recreate the notification with the same data
        }
        this.infos.isPlaying = isPlaying;
        this.infos.dismissable = dismissable;
        this.createBuilder();
        this.createNotification();
    }

    // Get image from url
    private void getBitmapCover(String coverURL) {
        try {
            if (coverURL.matches("^(https?|ftp)://.*$")) // Remote image
            this.bitmapCover = getBitmapFromURL(coverURL); else {
                // Local image
                this.bitmapCover = getBitmapFromLocal(coverURL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // get Local image
    private Bitmap getBitmapFromLocal(String localURL) {
        try {
            Uri uri = Uri.parse(localURL);
            File file = new File(uri.getPath());
            FileInputStream fileStream = new FileInputStream(file);
            BufferedInputStream buf = new BufferedInputStream(fileStream);
            Bitmap myBitmap = BitmapFactory.decodeStream(buf);
            buf.close();
            return myBitmap;
        } catch (Exception ex) {
            try {
                InputStream fileStream = cordovaActivity.getAssets().open("public/" + localURL);
                BufferedInputStream buf = new BufferedInputStream(fileStream);
                Bitmap myBitmap = BitmapFactory.decodeStream(buf);
                buf.close();
                return myBitmap;
            } catch (Exception ex2) {
                ex.printStackTrace();
                ex2.printStackTrace();
                return null;
            }
        }
    }

    // get Remote image
    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void createBuilder() {
        Context context = cordovaActivity.getApplicationContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        // use channelid for Oreo and higher
        if (Build.VERSION.SDK_INT >= 26) {
            builder.setChannelId(this.CHANNEL_ID);
        }

        //Configure builder
        builder.setContentTitle(this.infos.track);
        if (!this.infos.artist.isEmpty()) {
            builder.setContentText(this.infos.artist);
        }
        builder.setWhen(0);

        // set if the notification can be destroyed by swiping
        if (this.infos.dismissable) {
            builder.setOngoing(false);
            Intent dismissIntent = new Intent("music-controls-destroy");
            PendingIntent dismissPendingIntent;
            if (Build.VERSION.SDK_INT >= 23) {
                dismissPendingIntent = PendingIntent.getBroadcast(context, 1, dismissIntent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                dismissPendingIntent = PendingIntent.getBroadcast(context, 1, dismissIntent, 0);
            }

            builder.setDeleteIntent(dismissPendingIntent);
        } else {
            builder.setOngoing(true);
        }
        if (!this.infos.ticker.isEmpty()) {
            builder.setTicker(this.infos.ticker);
        }

        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        //Set SmallIcon
        boolean usePlayingIcon = this.infos.notificationIcon.isEmpty();
        if (!usePlayingIcon) {
            int resId = this.getResourceId(this.infos.notificationIcon, 0);
            usePlayingIcon = resId == 0;
            if (!usePlayingIcon) {
                builder.setSmallIcon(resId);
            }
        }

        if (usePlayingIcon) {
            if (this.infos.isPlaying) {
                builder.setSmallIcon(this.getResourceId(this.infos.playIcon, R.drawable.cmc_play));
            } else {
                builder.setSmallIcon(this.getResourceId(this.infos.pauseIcon, R.drawable.cmc_pause));
            }
        }

        //Set LargeIcon
        if (!this.infos.cover.isEmpty() && this.bitmapCover != null) {
            builder.setLargeIcon(this.bitmapCover);
        }

        //Open app if tapped
        Intent resultIntent = new Intent(context, cordovaActivity.getClass());
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent resultPendingIntent;
        if (Build.VERSION.SDK_INT >= 31) {
            resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_MUTABLE);
        } else {
            resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
        }
        builder.setContentIntent(resultPendingIntent);

        //Controls
        int nbControls = 0;
        /* Previous  */
        if (this.infos.hasPrev) {
            nbControls++;
            Intent previousIntent = new Intent("music-controls-previous");
            PendingIntent previousPendingIntent;
            if (Build.VERSION.SDK_INT >= 31) {
                previousPendingIntent = PendingIntent.getBroadcast(context, 1, previousIntent, PendingIntent.FLAG_MUTABLE);
            } else {
                previousPendingIntent = PendingIntent.getBroadcast(context, 1, previousIntent, 0);
            }
            builder.addAction(createAction(this.infos.prevIcon, R.drawable.cmc_skip_previous, previousPendingIntent));
        }
        if (this.infos.isPlaying) {
            /* Pause  */
            nbControls++;
            Intent pauseIntent = new Intent("music-controls-pause");
            PendingIntent pausePendingIntent;
            if (Build.VERSION.SDK_INT >= 31) {
                pausePendingIntent = PendingIntent.getBroadcast(context, 1, pauseIntent, PendingIntent.FLAG_MUTABLE);
            } else {
                pausePendingIntent = PendingIntent.getBroadcast(context, 1, pauseIntent, 0);
            }
            builder.addAction(createAction(this.infos.pauseIcon, R.drawable.cmc_pause, pausePendingIntent));
        } else {
            /* Play  */
            nbControls++;
            Intent playIntent = new Intent("music-controls-play");
            PendingIntent playPendingIntent;
            if (Build.VERSION.SDK_INT >= 31) {
                playPendingIntent = PendingIntent.getBroadcast(context, 1, playIntent, PendingIntent.FLAG_MUTABLE);
            } else {
                playPendingIntent = PendingIntent.getBroadcast(context, 1, playIntent, 0);
            }
            builder.addAction(createAction(this.infos.playIcon, R.drawable.cmc_play, playPendingIntent));
        }
        /* Next */
        if (this.infos.hasNext) {
            nbControls++;
            Intent nextIntent = new Intent("music-controls-next");
            PendingIntent nextPendingIntent;
            if (Build.VERSION.SDK_INT >= 31) {
                nextPendingIntent = PendingIntent.getBroadcast(context, 1, nextIntent, PendingIntent.FLAG_MUTABLE);
            } else {
                nextPendingIntent = PendingIntent.getBroadcast(context, 1, nextIntent, 0);
            }
            builder.addAction(createAction(this.infos.nextIcon, R.drawable.cmc_skip_next, nextPendingIntent));
        }
        /* Close */
        if (this.infos.hasClose) {
            nbControls++;
            Intent destroyIntent = new Intent("music-controls-destroy");
            PendingIntent destroyPendingIntent;
            if (Build.VERSION.SDK_INT >= 31) {
                destroyPendingIntent = PendingIntent.getBroadcast(context, 1, destroyIntent, PendingIntent.FLAG_MUTABLE);
            } else {
                destroyPendingIntent = PendingIntent.getBroadcast(context, 1, destroyIntent, 0);
            }
            builder.addAction(createAction(this.infos.closeIcon, R.drawable.cmc_stop, destroyPendingIntent));
        }

        int[] args = new int[nbControls];
        for (int i = 0; i < nbControls; ++i) {
            args[i] = i;
        }
        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle();
        mediaStyle.setMediaSession(this.token);
        mediaStyle.setShowActionsInCompactView(args);
        builder.setStyle(mediaStyle);

        //If 8.0 >= use colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (this.infos.iconsColor != 0) {
                builder.setColorized(true);
                builder.setColor(this.infos.iconsColor);
            }
        }

        this.notificationBuilder = builder;
    }

    private NotificationCompat.Action createAction(String drawableRes, int fallbackRes, PendingIntent intent) {
        int icon = getResourceId(drawableRes, fallbackRes);
        return new NotificationCompat.Action(icon, "", intent);
    }

    private int getResourceId(String name, int fallback) {
        try {
            if (name.isEmpty()) {
                return fallback;
            }

            int resId = this.cordovaActivity.getResources().getIdentifier(name, "drawable", this.cordovaActivity.getPackageName());
            return resId == 0 ? fallback : resId;
        } catch (Exception ex) {
            return fallback;
        }
    }

    public void destroy() {
        Log.i(TAG, "Destroying notification");
        if (this.killer_service != null) {
            this.killer_service.get().setNotification(null);
        }
        this.notificationManager.cancel(this.notificationID);
        Log.i(TAG, "Notification destroyed");
    }
}
