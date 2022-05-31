package com.gokadzev.capacitormusiccontrols;

import com.getcapacitor.JSObject;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;



import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import android.app.Activity;

import android.os.SystemClock;

import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.app.PendingIntent;
import org.json.JSONException;

@CapacitorPlugin(name = "CapacitorMusicControls")
public class CapacitorMusicControls extends Plugin {

	private static final String TAG = "CapacitorMusicControls";
	private final int notificationID=7824;

	private MediaSessionCompat mediaSessionCompat;
	private MusicControlsBroadcastReceiver mMessageReceiver;
	private MusicControlsNotification notification;
	private MediaSessionCallback mMediaSessionCallback;
	private PendingIntent mediaButtonPendingIntent;

	@PluginMethod()
	public void create(PluginCall call) {
		JSObject options = call.getData();

		this.initialize();
		this.updateMetadata(options);
		call.resolve();
	}

	@PluginMethod()
	public void updateMetadata(PluginCall call) {
		JSObject options = call.getData();

		this.updateMetadata(options);
		call.resolve();
	}

	@Override
	protected void handleOnDestroy() {
		final Activity activity = getActivity();
		final Context context=activity.getApplicationContext();

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

		} catch(IllegalArgumentException e) {
		}

		this.unregisterMediaButtonEvent();
	}

	@PluginMethod()
	public void destroy(PluginCall call) {
		final Activity activity = getActivity();
		final Context context=activity.getApplicationContext();

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

		} catch(IllegalArgumentException e) {
		}

		this.unregisterMediaButtonEvent();
		call.resolve();
	}

	@PluginMethod()
	public void updateIsPlaying(PluginCall call) {
		JSObject options = call.getData();

		try{
			final boolean isPlaying = options.getBoolean("isPlaying");
			this.notification.updateIsPlaying(isPlaying);

			if(isPlaying)
				setMediaPlaybackStateNew(PlaybackStateCompat.STATE_PLAYING);
			else
				setMediaPlaybackStateNew(PlaybackStateCompat.STATE_PAUSED);

			call.resolve();
		} catch(JSONException e){
			System.out.println("toString(): "  + e.toString());
			System.out.println("getMessage(): " + e.getMessage());
			System.out.println("StackTrace: ");
			e.printStackTrace();
			call.reject("error in updateIsPlaying");
		}
	}

	@PluginMethod()
	public void updateState(PluginCall call) {
		JSObject params = call.getData();

		try {
			final boolean isPlaying = params.getBoolean("isPlaying");
			final long elapsed = (params.getLong("elapsed") * 1000);;
			this.notification.updateIsPlaying(isPlaying);

			if(isPlaying)
				this.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING, elapsed);
			else
				this.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED, elapsed);

			call.resolve();
		} catch(JSONException e){
			call.reject("error in updateState");
		}
	}

	@PluginMethod()
	public void updateDismissable(PluginCall call) {
		JSObject params = call.getData();

		try{
			final boolean dismissable = params.getBoolean("dismissable");
			this.notification.updateDismissable(dismissable);
			call.resolve();
		} catch(JSONException e){
			call.reject("error in updateDismissable");
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
			this.mediaButtonPendingIntent = PendingIntent.getBroadcast(context, 0, headsetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		} catch (Exception e) {
			this.mediaButtonPendingIntent = null;
		}

		// create a media session
		this.mediaSessionCompat = new MediaSessionCompat(context, "capacitor-music-controls-media-session", null, this.mediaButtonPendingIntent);
		this.mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		// create a notification
		this.notification = new MusicControlsNotification(activity, this.notificationID, this.mediaSessionCompat.getSessionToken());

		// register the headset button event receiver
		this.registerMediaButtonEvent();
	}

	private boolean updateMetadata(JSObject options) {
		try {
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
			return false;
		}
	}

	private void registerBroadcaster() {
		final Context context = getActivity().getApplicationContext();

		context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-previous"));
		context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-pause"));
		context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-play"));
		context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-next"));
		context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-media-button"));
		context.registerReceiver(this.mMessageReceiver, new IntentFilter("music-controls-destroy"));
		context.registerReceiver(this.mMessageReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
	}

	private void setMediaPlaybackState(int state, long elapsed) {
		PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
		if( state == PlaybackStateCompat.STATE_PLAYING ) {
			playbackstateBuilder.setActions(
					PlaybackStateCompat.ACTION_PLAY_PAUSE |
							PlaybackStateCompat.ACTION_PAUSE |
							PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
							PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
							PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
							PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
			);
			playbackstateBuilder.setState(state, elapsed, 1.0f, SystemClock.elapsedRealtime());
		} else {
			playbackstateBuilder.setActions(
					PlaybackStateCompat.ACTION_PLAY_PAUSE |
							PlaybackStateCompat.ACTION_PLAY |
							PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
							PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
							PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
							PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
			);
			playbackstateBuilder.setState(state, elapsed, 0, SystemClock.elapsedRealtime());
		}
		this.mediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
	}

	private void setMediaPlaybackStateNew(int state) {
		PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
		if( state == PlaybackStateCompat.STATE_PLAYING ) {
			playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
					PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
					PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH);
			playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
		} else {
			playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
					PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
					PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH);
			playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
		}
		this.mediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
	}
	
}