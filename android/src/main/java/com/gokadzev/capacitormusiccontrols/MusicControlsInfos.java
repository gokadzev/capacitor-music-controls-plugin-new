package com.gokadzev.capacitormusiccontrols;

import com.getcapacitor.JSObject;
import org.json.JSONException;

public class MusicControlsInfos {

    public String artist;
    public String album;
    public String track;
    public String ticker;
    public String cover;
    public boolean isPlaying;
    public boolean hasPrev;
    public boolean hasNext;
    public boolean hasClose;
    public boolean dismissable;
    public String playIcon;
    public String pauseIcon;
    public String prevIcon;
    public String nextIcon;
    public String closeIcon;
    public String notificationIcon;
    public int iconsColor;
    public long duration;

    public MusicControlsInfos(JSObject args) throws JSONException {
        final JSObject params = args;

        this.track = params.getString("track", "");
        this.artist = params.getString("artist", "");
        this.album = params.getString("album", "");
        this.ticker = params.getString("ticker", "");
        this.cover = params.getString("cover", "");
        this.isPlaying = params.getBoolean("isPlaying", false);
        this.hasPrev = params.getBoolean("hasPrev", true);
        this.hasNext = params.getBoolean("hasNext", true);
        this.hasClose = params.getBoolean("hasClose", false);
        this.dismissable = params.getBoolean("dismissable", false);
        this.playIcon = params.getString("playIcon", "media_play");
        this.pauseIcon = params.getString("pauseIcon", "media_pause");
        this.prevIcon = params.getString("prevIcon", "media_prev");
        this.nextIcon = params.getString("nextIcon", "media_next");
        this.closeIcon = params.getString("closeIcon", "media_close");
        this.notificationIcon = params.getString("notificationIcon", "notification");
        this.iconsColor = params.getInteger("iconsColor", 0);
        this.duration = params.getLong("duration");
    }
}
