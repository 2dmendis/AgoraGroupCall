package com.airsealand.agoragroupcall;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Rational;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.Constants;
import android.app.PictureInPictureParams;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<newRemoteVideo> remoteUsers = new ArrayList<>();



    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;


    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    private RtcEngine mRtcEngine;
    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserJoined(final int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);

            remoteUsers.add(new newRemoteVideo(uid));
            RecyclerView myrv = (RecyclerView) findViewById(R.id.recyclerview_id);
            RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(this, remoteUsers);
            myrv.setLayoutManager(new GridLayoutManager(this,3));
            myrv.setAdapter(myAdapter);
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();

        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }


    @Override
             protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initAgoraEngineAndJoinChannel();
        }


    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    REQUESTED_PERMISSIONS,
                    requestCode);
            return false;
        }
        return true;
    }


    private void setupVideoProfile() {
         mRtcEngine.enableVideo();
         mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P,false);
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeRtcEngine();
        setupVideoProfile();
        setupLocalVideo();
        joinChannel();
        enableVideo();
        setChannelProfile();
    }

    private void initializeRtcEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id),mRtcEventHandler);


        } catch (Exception e) {
            Log.e("AGORA",e.getMessage());
        }
    }

    private void enableVideo() {
        mRtcEngine.enableVideo();

    }
//
//    private void setVideoEncoderConfiguration() {
//        VideoEncoderConfiguration.ORIENTATION_MODE
//                orientationMode =
//                VideoEncoderConfiguration\].ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;
//
//        VideoEncoderConfiguration.VideoDimensions dimensions = new VideoEncoderConfiguration.VideoDimensions(360, 640);
//
//        VideoEncoderConfiguration videoEncoderConfiguration = new VideoEncoderConfiguration(dimensions, frameRate, bitrate, orientationMode);
//
//        mRtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration);
//    }

    private void setChannelProfile() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
    }

    private void joinChannel() {
         mRtcEngine.joinChannel(null, "test", null,0);
    }

    private void setupLocalVideo(){
         FrameLayout container = findViewById(R.id.local_video_view_container);

         SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
         surfaceView.setZOrderMediaOverlay(true);

         container.addView(surfaceView);
         mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView,VideoCanvas.RENDER_MODE_ADAPTIVE,0));

//         setupRemoteVideo(int uid = 0);

    }
    public void onLocalVideoMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalVideoStream(iv.isSelected());

        FrameLayout container = findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
        surfaceView.setZOrderMediaOverlay(!iv.isSelected());
        surfaceView.setVisibility(iv.isSelected() ? View.GONE : View.VISIBLE);
    }

    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    public void onEncCallClicked(View view) {
        finish();
    }

    private void setupRemoteVideo(int uid) {

        FrameLayout container = findViewById(R.id.remote_video_view_container);

        if (container.getChildCount() >= 1) {
            return;
        }
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());

        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView,VideoCanvas.RENDER_MODE_ADAPTIVE,uid));

        surfaceView.setTag(uid);

        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
        tipMsg.setVisibility(View.GONE);


    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }
    private void onRemoteUserLeft() {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        container.removeAllViews();

        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk);
        tipMsg.setVisibility(View.VISIBLE);
    }



}
