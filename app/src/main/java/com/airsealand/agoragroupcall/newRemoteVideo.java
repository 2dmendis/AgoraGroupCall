package com.airsealand.agoragroupcall;

import android.view.SurfaceView;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.Constants;

public class newRemoteVideo {

    private int uid;

    public newRemoteVideo() {

    }

    public newRemoteVideo(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setUpNewView(){
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView,VideoCanvas.RENDER_MODE_ADAPTIVE,uid));
    }
}
