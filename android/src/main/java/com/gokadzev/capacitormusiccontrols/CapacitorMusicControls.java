package com.gokadzev.capacitormusiccontrols;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONException;

@CapacitorPlugin(name = "CapacitorMusicControls")
public class CapacitorMusicControls extends Plugin {

    private static final String TAG = "CapacitorMusicControls";
    private final int notificationID = 7824;

    private MediaSessionCompat mediaSessionCompat;
    private MusicControlsBroadcastReceiver mMessageReceiver;
    private MusicControlsNotification notification;
    private MediaSessionCallback mMediaSessionCallback;
    private PendingIntent mediaButtonPendingIntent;

    @PluginMethod
    public void create(PluginCall call) {
        try {
            // Check for notification permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                    call.reject("POST_NOTIFICATIONS permission is required for Android 13+");
                    return;
                }
            }

            JSObject options = call.getData();

            this.initialize();
            boolean metadataSuccess = this.updateMetadata(options);
            
            if (!metadataSuccess) {
                call.reject("Failed to initialize music controls: metadata update failed");
                return;
            }
            
            call.resolve();
        } catch (Exception e) {
            System.out.println("Error in create method: " + e.toString());
            e.printStackTrace();
            call.reject("Failed to create music controls: " + e.getMessage());
        }
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        JSObject result = new JSObject();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasNotificationPermission = ContextCompat.checkSelfPermission(getContext(), 
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            result.put("notifications", hasNotificationPermission ? "granted" : "denied");
        } else {
            result.put("notifications", "granted");
        }
        
        call.resolve(result);
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), 
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
        call.resolve();
    }

    @PluginMethod
    public void updateMetadata(PluginCall call) {
        JSObject options = call.getData();

        try {
            boolean success = this.updateMetadata(options);
            if (success) {
                call.resolve();
            } else {
                call.reject("Failed to update metadata");
            }
        } catch (Exception e) {
            System.out.println("Unexpected error in updateMetadata method: " + e.toString());
            e.printStackTrace();
            call.reject("Unexpected error in updateMetadata: " + e.getMessage());
        }
    }

    @Override
    protected void handleOnDestroy() {
        final Activity activity = getActivity();
        final Context context = activity.getApplicationContext();

        this.destroyPlayerNotification();

        try {
            context.unregisterReceiver(this.mMessageReceiver);

            this.mediaSessionCompat.setActive(false);
            this.mediaSessionCompat.setMetadata(null);
            this.mediaSessionCompat.setPlaybackState(null);

            this.mMessageReceiver = null;
            this.notification = null;
            this.mediaSessionCompat = null;
            this.mediaButtonPendingIntent = null;
            this.mMediaSessionCallback = null;
        } catch (IllegalArgumentException e) {}

        this.unregisterMediaButtonEvent();
    }

    @PluginMethod
    public void destroy(PluginCall call) {
        final Activity activity = getActivity();
        final Context context = activity.getApplicationContext();

        this.destroyPlayerNotification();

        try {
            context.unregisterReceiver(this.mMessageReceiver);

            this.mediaSessionCompat.setActive(false);
            this.mediaSessionCompat.setMetadata(null);
            this.mediaSessionCompat.setPlaybackState(null);

            this.mMessageReceiver = null;
            this.notification = null;
            this.mediaSessionCompat = null;
            this.mediaButtonPendingIntent = null;
            this.mMediaSessionCallback = null;
        } catch (IllegalArgumentException e) {}

        this.unregisterMediaButtonEvent();
        call.resolve();
    }

    @PluginMethod
    public void updateIsPlaying(PluginCall call) {
        JSObject options = call.getData();

        try {
            final boolean isPlaying = options.getBoolean("isPlaying");
            
            // Check if notification is initialized
            if (this.notification == null) {
                call.reject("Music controls not initialized. Call create() first.");
                return;
            }
            
            this.notification.updateIsPlaying(isPlaying);

            if (isPlaying) setMediaPlaybackStateNew(PlaybackStateCompat.STATE_PLAYING); else setMediaPlaybackStateNew(
                PlaybackStateCompat.STATE_PAUSED
            );

            call.resolve();
        } catch (JSONException e) {
            System.out.println("toString(): " + e.toString());
            System.out.println("getMessage(): " + e.getMessage());
            System.out.println("StackTrace: ");
            e.printStackTrace();
            call.reject("error in updateIsPlaying");
        } catch (Exception e) {
            System.out.println("Unexpected error in updateIsPlaying: " + e.toString());
            e.printStackTrace();
            call.reject("Unexpected error in updateIsPlaying: " + e.getMessage());
        }
    }

    @PluginMethod
    public void updateState(PluginCall call) {
        JSObject params = call.getData();

        try {
            // Check if notification is initialized
            if (this.notification == null) {
                call.reject("Music controls not initialized. Call create() first.");
                return;
            }
            
            final boolean isPlaying = params.getBoolean("isPlaying");
            final long elapsed = (params.getLong("elapsed") * 1000);
            this.notification.updateIsPlaying(isPlaying);
            if (isPlaying) this.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING, elapsed); else this.setMediaPlaybackState(
                    PlaybackStateCompat.STATE_PAUSED,
                    elapsed
                );

            call.resolve();
        } catch (JSONException e) {
            call.reject("error in updateState: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error in updateState: " + e.toString());
            e.printStackTrace();
            call.reject("Unexpected error in updateState: " + e.getMessage());
        }
    }

    @PluginMethod
    public void updateDismissable(PluginCall call) {
        JSObject params = call.getData();

        try {
            // Check if notification is initialized
            if (this.notification == null) {
                call.reject("Music controls not initialized. Call create() first.");
                return;
            }
            
            final boolean dismissable = params.getBoolean("dismissable");
            this.notification.updateDismissable(dismissable);
            call.resolve();
        } catch (JSONException e) {
            call.reject("error in updateDismissable: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error in updateDismissable: " + e.toString());
            e.printStackTrace();
            call.reject("Unexpected error in updateDismissable: " + e.getMessage());
        }
    }

    public void controlsNotification(JSObject ret) {
        notifyListeners("controlsNotification", ret);
    }

    public void registerMediaButtonEvent() {
        if (this.mediaSessionCompat != null) {
            this.mediaSessionCompat.setMediaButtonReceiver(this.mediaButtonPendingIntent);
        }
    }

    public void unregisterMediaButtonEvent() {
        if (this.mediaSessionCompat != null) {
            this.mediaSessionCompat.setMediaButtonReceiver(null);
        }
    }

    public void destroyPlayerNotification() {
        if (this.notification != null) {
            this.notification.destroy();
            this.notification = null;
        }
    }

    private void initialize() {
        final Activity activity = getActivity();
        final Context context = activity.getApplicationContext();

        // create a message receiver
        this.mMessageReceiver = new MusicControlsBroadcastReceiver(this);
        this.registerBroadcaster();

        // get ready for headset events
        try {
            Intent headsetIntent = new Intent("music-controls-media-button");
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            this.mediaButtonPendingIntent = PendingIntent.getBroadcast(context, 0, headsetIntent, flags);
        } catch (Exception e) {
            this.mediaButtonPendingIntent = null;
        }

        // create a media session
        this.mediaSessionCompat =
            new MediaSessionCompat(context, "capacitor-music-controls-media-session", null, this.mediaButtonPendingIntent);
        this.mediaSessionCompat.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            );

        // create a notification
        this.notification = new MusicControlsNotification(activity, this.notificationID, this.mediaSessionCompat.getSessionToken());

        // register the headset button event receiver
        this.registerMediaButtonEvent();
    }

    private boolean updateMetadata(JSObject options) {
        try {
            // Check if notification is initialized
            if (this.notification == null) {
                System.out.println("Warning: updateMetadata called but notification is null");
                return false;
            }
            
            this.mediaSessionCompat.setActive(false);
            this.mediaSessionCompat.setMetadata(null);
            this.mediaSessionCompat.setPlaybackState(null);

            final MusicControlsInfos infos = new MusicControlsInfos(options);
            final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

            this.notification.updateNotification(infos);

            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, infos.track);
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, infos.artist);
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, infos.album);
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, infos.duration * 1000);
            this.mediaSessionCompat.setMetadata(metadataBuilder.build());

            this.mediaSessionCompat.setActive(true);
            return true;
        } catch (JSONException e) {
            System.out.println("JSONException in updateMetadata: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("Unexpected error in updateMetadata: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    private void registerBroadcaster() {
        final Context context = getActivity().getApplicationContext();

        // For Android 14+ (API 34+), we need to specify receiver export flags
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Use RECEIVER_NOT_EXPORTED for our custom actions as they are internal to our app
            int flags = Context.RECEIVER_NOT_EXPORTED;
            
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-previous"), flags);
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-pause"), flags);
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-play"), flags);
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-next"), flags);
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-media-button"), flags);
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-destroy"), flags);
            
            // Use RECEIVER_EXPORTED for system broadcasts like headset plug
            context.registerReceiver(this.mMessageReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG), Context.RECEIVER_EXPORTED);
        } else {
            // Use legacy method for older Android versions
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-previous"));
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-pause"));
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-play"));
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-next"));
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-media-button"));
            context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-destroy"));
            context.registerReceiver(this.mMessageReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        }
    }

    private void setMediaPlaybackState(int state, long elapsed) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackstateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SEEK_TO |
                PlaybackStateCompat.ACTION_SET_RATING |
                PlaybackStateCompat.ACTION_FAST_FORWARD |
                PlaybackStateCompat.ACTION_REWIND
            );
            playbackstateBuilder.setState(state, elapsed, 1.0f, SystemClock.elapsedRealtime());
        } else {
            playbackstateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SEEK_TO |
                PlaybackStateCompat.ACTION_SET_RATING |
                PlaybackStateCompat.ACTION_FAST_FORWARD |
                PlaybackStateCompat.ACTION_REWIND
            );
            playbackstateBuilder.setState(state, elapsed, 0, SystemClock.elapsedRealtime());
        }
        this.mediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private void setMediaPlaybackStateNew(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackstateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SEEK_TO |
                PlaybackStateCompat.ACTION_SET_RATING |
                PlaybackStateCompat.ACTION_FAST_FORWARD |
                PlaybackStateCompat.ACTION_REWIND
            );
            playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
        } else {
            playbackstateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SEEK_TO |
                PlaybackStateCompat.ACTION_SET_RATING |
                PlaybackStateCompat.ACTION_FAST_FORWARD |
                PlaybackStateCompat.ACTION_REWIND
            );
            playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        }
        this.mediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }
}
