package com.geniatech.iptv;

import android.util.Log;

public class player {
	private static native int jniCreateDrmPlayer(Object obj);

	private static native int jniDestroyDrmPlayer();

	//private static native int jniDrmPlayerStart(String url);
	
	private static native int jniDrmPlayerStart(String url, boolean isSoftMode);
	
	private static native int jniDrmPlayerConfigVMServer(String serverIP, int serverPort, String storeFilePath);
	
	private static native int jniDrmPlayerGetVMServerStatus();

	private static native int jniDrmPlayerStop();

	private static native int jniDrmPlayerGetStatus(); // return 1:playing

	private static native SubtitleStreamInfo[] jniDrmPlayerGetSubtitleInfo();

	private static native AudioStreamInfo[] jniDrmPlayerGetAudioInfo();

	private static native int jniDrmPlayerSetAudio(int index);

	private static native int jniDrmPlayerSetSubtitle(int index);
	
	private static native int jniDrmPlayerSetBuffer(int timeUs);//timeUs=-1,caching until get enough data for playing.
	
	private static native int jniDrmPlayerSetVideoSize(int x, int y, int dx, int dy);
	
	private boolean	mIsPlaying = false;
	
	public class PLAYER_STATUS {
		final static int ePLAYER_NO_ERROR = 0;
		final static int ePLAYER_DRM_DECRYPTION_ERROR = 1;
	};

	// Loading so file according to the following order
	static {
		System.loadLibrary("vmlogger");
		System.loadLibrary("vmclient");
		System.loadLibrary("zvbi");
		System.loadLibrary("drmplayer_jni");
	}
	
	public player() {
		jniCreateDrmPlayer(this);
	}
	
	protected void finalize() {
		if (isPlaying()) {
			stop();
		}
		jniDestroyDrmPlayer();
	}

	public boolean configVMServer(String serverAddr, int serverPort, String storeFilePath) {
        System.out.println("Nathan: Configuring VM Server..." + serverAddr + serverPort + storeFilePath);
		/*we just can get Verimatrix Server status when configing,
		there is no more method provide by vm to get server status*/
		return jniDrmPlayerConfigVMServer(serverAddr, serverPort, storeFilePath)==0;
		//jniDrmPlayerConfigVMServer("public2.verimatrix.com", 12686, "/data/data/com.geniatech.iptv")==0;
	}

	public boolean stop() {
		if (mIsPlaying){
			mIsPlaying = !(jniDrmPlayerStop()==0);
			return !mIsPlaying;
		}
		return true;
	}

	public boolean isPlaying() {
		return mIsPlaying;
	}

	public boolean play(String path) {
		//jniDrmPlayerSetBuffer(2*1000*1000);
		if (!mIsPlaying) {
			mIsPlaying = (jniDrmPlayerStart(path, true)==0);
			return mIsPlaying;
		}
		return false;
	}

	public AudioStreamInfo[] get_audio_list() {
		return isPlaying() ? jniDrmPlayerGetAudioInfo() : null;
	}

	public SubtitleStreamInfo[] get_subtitle_list() {
		return isPlaying() ? jniDrmPlayerGetSubtitleInfo() : null;
	}

	public boolean setAudioIndex(int index) {
		return jniDrmPlayerSetAudio(index)==0;
	}

	public boolean setSubtitleIndex(int index) {
		return jniDrmPlayerSetSubtitle(index)==0;
	}
	/*
	 	had better on conditional that:
		0<=x<=1280
		0<=y<=720
		1280>=dx>=200 
		720>=dy>=150
		4/3<=dx/dy<=16/9
		
		normal size:x=y=dx=dy=0
	*/
	public boolean SetVideoSize(int x, int y, int dx, int dy) {
		return jniDrmPlayerSetVideoSize(x, y, dx, dy)==0;
	}

	public void onEvent(int event){
		switch (event) {
			case PLAYER_STATUS.ePLAYER_DRM_DECRYPTION_ERROR: {
				Log.v("VM", "ePLAYER_DRM_DECRYPTION_ERROR ******************\n");
			}
			break;
			case PLAYER_STATUS.ePLAYER_NO_ERROR: {
				Log.v("VM", "ePLAYER_NO_ERROR ******************\n");
			}
			break;
		}
	}
}
