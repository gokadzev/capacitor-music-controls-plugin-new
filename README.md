# Capacitor Music Controls Plugin New

Music controls for Capacitor 7 applications with full Android 15 compatibility. Display a 'media' notification with play/pause, previous, next buttons, allowing the user to control the play. Handles headset events (plug, unplug, headset button) on Android.


### Permission Handling (Android 13+)
For Android 13+, you must request notification permissions at runtime:

```typescript
// Check permissions
const permissions = await CapacitorMusicControls.checkPermissions();
if (permissions.notifications !== 'granted') {
  // Request permissions
  await CapacitorMusicControls.requestPermissions();
}

// Then create music controls
await CapacitorMusicControls.create({...});
```

### Available Permission Methods
```typescript
// Check current permission status
CapacitorMusicControls.checkPermissions(): Promise<PermissionStatus>

// Request permissions from user
CapacitorMusicControls.requestPermissions(): Promise<PermissionStatus>
```

## Supported platforms

- **Android** (API 21+, targeting API 35)
- **iOS** (13.0+)


## Installation

```bash
npm install https://github.com/gokadzev/capacitor-music-controls-plugin-new.git
npx cap sync
```

## iOS Setup

Run:
```bash
npx cap sync ios
```

## Android Setup

After you install the plugin, locate your MainActivity.java (can be found in /android/app/src/main/java/path/to/my/app/MainActivity.java)

Import this path:
```java
import com.gokadzev.capacitormusiccontrols.CapacitorMusicControls;
```

Add class inside bridge activity:
```java
add(CapacitorMusicControls.class);
```

Example:
```java
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.gokadzev.capacitormusiccontrols.CapacitorMusicControls;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerPlugin(CapacitorMusicControls.class);
    }
}
```

### Required Permissions
The plugin requires these permissions in your AndroidManifest.xml (already included in the plugin):
```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

Finally, run:
```bash
npx cap sync android
```


## Usage

### Basic Setup

First, check and request permissions (required for Android 13+):

```typescript
import { CapacitorMusicControls } from "capacitor-music-controls-plugin-new";

// Check permissions first (Android 13+)
const permissions = await CapacitorMusicControls.checkPermissions();
if (permissions.notifications !== 'granted') {
  await CapacitorMusicControls.requestPermissions();
}
```


### Creating Music Controls

Create the media controls with all available options:

```typescript
CapacitorMusicControls.create({
	track: 'Time is Running Out',		// optional, default : ''
	artist: 'Muse',						// optional, default : ''
	album: 'Absolution',     // optional, default: ''
 	cover: 'albums/absolution.jpg',		// optional, default : nothing
	// cover can be a local path (use fullpath 'file:///storage/emulated/...', or only 'my_image.jpg' if my_image.jpg is in the www folder of your app)
	//			 or a remote url ('http://...', 'https://...', 'ftp://...')

	// hide previous/next/close buttons:
	hasPrev: false,		// show previous button, optional, default: true
	hasNext: false,		// show next button, optional, default: true
	hasClose: true,		// show close button, optional, default: false


	duration : 0, // (in seconds) required


	// iOS only, optional
	elapsed: 10, // optional, default: 0
  	hasSkipForward: true, //optional, default: false. true value overrides hasNext.
  	hasSkipBackward: true, //optional, default: false. true value overrides hasPrev.
  	skipForwardInterval: 15, //optional. default: 15.
	skipBackwardInterval : 15, //optional. default: 15.
	hasScrubbing: false, //optional. default to false. Enable scrubbing from control center progress bar

  	// Android only, optional
  	isPlaying: true,							// optional, default : true
  	dismissable: true,							// optional, default : false
	// text displayed in the status bar when the notification (and the ticker) are updated
	ticker: 'Now playing "Time is Running Out"',
	//All icons default to their built-in android equivalents
	//The supplied drawable name, e.g. 'media_play', is the name of a drawable found under android/res/drawable* folders
	playIcon: 'media_play',
	pauseIcon: 'media_pause',
	prevIcon: 'media_prev',
	nextIcon: 'media_next',
	closeIcon: 'media_close',
	notificationIcon: 'notification',
	iconsColor: 0xffffffff // controller icons color, default: white (url for more colors: https://developer.android.com/reference/android/graphics/Color#constants_1 )

}).then(()=>{
	console.log('Music controls created successfully');
})
.catch(e=>{
	console.log(e);
});
```

### Updating Playback State

Update whether the music is playing true/false, as well as the time elapsed (seconds):

```typescript

//Update only playing status

CapacitorMusicControls.updateIsPlaying({ isPlaying: true }).then(()=>{
	console.log('Playing status updated');
})
.catch(e=>{
	console.log(e);
});

//or just

CapacitorMusicControls.updateIsPlaying({ isPlaying: true });


//Update as playing status as elapsed time

CapacitorMusicControls.updateState({
	elapsed: timeElapsed, // affects iOS Only
	isPlaying: true // affects Android only
}).then(()=>{
	console.log('State updated');
})
.catch(e=>{
	console.log(e);
});

```

### Listening for Events

Listen for events and pass them to your handler function:

```typescript
CapacitorMusicControls.addListener('controlsNotification', (info: any) => {
    console.log('controlsNotification was fired');
    console.log(info);
    handleControlsEvent(info);
});
```



### Example Event Handler

```typescript
function handleControlsEvent(action) {

	console.log("hello from handleControlsEvent")
	const message = action.message;

	console.log("message: " + message)

	switch(message) {
		case 'music-controls-next':
			// next
			break;
		case 'music-controls-previous':
			// previous
			break;
		case 'music-controls-pause':
			// paused
			break;
		case 'music-controls-play':
			// resumed
			break;
		case 'music-controls-destroy':
			// controls were destroyed
			break;

		// External controls (iOS only)
		case 'music-controls-toggle-play-pause' :
			// do something
			break;
		case 'music-controls-skip-to':
			// do something
			break;
		case 'music-controls-skip-forward':
			// Do something
			break;
		case 'music-controls-skip-backward':
			// Do something
			break;

		// Headset events (Android only)
		// All media button events are listed below
		case 'music-controls-media-button' :
			// Do something
			break;
		case 'music-controls-headset-unplugged':
			// Do something
			break;
		case 'music-controls-headset-plugged':
			// Do something
			break;
		default:
			break;
	}
}
```

### Destroying Music Controls

```typescript
// Clean up music controls when done
await CapacitorMusicControls.destroy();
```

## API Reference

### Permission Methods

#### checkPermissions()
```typescript
checkPermissions(): Promise<PermissionStatus>
```
Returns the current permission status for notifications.

#### requestPermissions()
```typescript
requestPermissions(): Promise<PermissionStatus>
```
Requests notification permissions from the user (Android 13+ only).

### PermissionStatus Interface
```typescript
interface PermissionStatus {
  notifications: 'granted' | 'denied' | 'prompt';
}
```

## Migration Guide

### From v1.x to v2.x

If you're upgrading from version 1.x, note these major breaking changes:

1. **Capacitor 7 Required**: You must upgrade your app to Capacitor 7
2. **Permission Handling**: You must now check and request permissions for Android 13+
3. **Target SDK**: Plugin now targets Android 15 (API 35)
4. **Dependencies**: Requires Java 17 and updated build tools
5. **iOS Minimum Version**: Updated to iOS 13.0+

#### Update Dependencies
```bash
# Update your Capacitor dependencies first
npm install @capacitor/core@7 @capacitor/cli@7 @capacitor/android@7 @capacitor/ios@7

# Then update the plugin
npm install https://github.com/gokadzev/capacitor-music-controls-plugin-new.git

# Sync with native platforms
npx cap sync
```

#### Code Changes
```typescript
// OLD (v1.x) - Direct creation
await CapacitorMusicControls.create({...});

// NEW (v2.x) - Check permissions first
const permissions = await CapacitorMusicControls.checkPermissions();
if (permissions.notifications !== 'granted') {
  await CapacitorMusicControls.requestPermissions();
}
await CapacitorMusicControls.create({...});
```

---
Contributors:

<a href = "https://github.com/gokadzev/capacitor-music-controls-plugin-new/graphs/contributors">
  <img src = "https://contrib.rocks/image?repo=gokadzev/capacitor-music-controls-plugin-new"/>
</a>
