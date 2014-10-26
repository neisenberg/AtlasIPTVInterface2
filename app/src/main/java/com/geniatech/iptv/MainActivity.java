package com.geniatech.iptv;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.content.res.Resources;
import com.example.natha_000.atlasiptvinterface.R;
import android.graphics.Typeface;
import android.content.Context;
import android.content.res.*;

/**
 * 
 * @author geniatech
 * @version 1.00 
 * 
 */
@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private player mPlayer;
	private static int VIDEO_HOLE = 257;

	public Typeface textType;
	private SurfaceView mSurfaceView = null;
	private TVSubtitleView subtitleView;
	private Dialog settingDialog;
	private LinearLayout infoLayout;
	private static final int SHOW_PANLE = 0X009;
	private static final int SHOW_UDP_STATUS = 0X011;

	private ArrayList<String> pathList = new ArrayList<String>();
	private static int AudioStream = 0;

	// Loading so file according to the following order
	
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case SHOW_PANLE:
				infoLayout.setVisibility(View.GONE);
				settingPanel();
				break;

			case SHOW_UDP_STATUS:
				// getServerStatu();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
        textType = Typeface.createFromAsset(this.getAssets(), "fonts/arial.ttf");
		super.onCreate(savedInstanceState);
		mPlayer = new player();
		setContentView(R.layout.main);
		findView();
		initData();
		new Thread(run).start();

		System.out.println("onCreate over ================");
	}

	@SuppressLint("SdCardPath")
	Runnable run = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int retry = 2;
			boolean ret = false;
			if (true) {
				while (!ret&&(retry>0)&&(mPlayer!=null)) {
					retry -= 1;
					ret = mPlayer.configVMServer("public2.verimatrix.com", 12686, "/data/data/com.geniatech.iptv/verimatrix.store");
				}
			}
			mHandler.sendEmptyMessage(SHOW_PANLE);
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mPlayer.stop();
		mPlayer.finalize();
		super.onDestroy();
	}

	@SuppressLint("ResourceAsColor")
	private void findView() {
		textType = Typeface.createFromAsset(this.getAssets(), "fonts/arial.ttf");
		mSurfaceView = (SurfaceView) findViewById(R.id.videoView1);
		subtitleView = (TVSubtitleView) findViewById(R.id.subtitle_view);
		infoLayout = (LinearLayout) findViewById(R.id.show_layout);
	}

	private void initData() {
		mSurfaceView.getHolder().addCallback(surfaceHolderCallback);
		mSurfaceView.getHolder().setFormat(VIDEO_HOLE);
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		System.out.println(event);
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			settingPanel();
			break;
		case KeyEvent.KEYCODE_BACK:
			if (settingDialog != null && settingDialog.isShowing()) {
				settingDialog.cancel();
				settingDialog = null;
			} else {
				mPlayer.stop();
			}
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			Log.d(TAG, "surfaceChanged");
			try {
				initSurface(holder);

			} catch (Exception e) {
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated");
			try {
				initSurface(holder);
			} catch (Exception e) {
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "surfaceDestroyed");
		}

		private void initSurface(SurfaceHolder h) {
			Canvas c = null;
			try {
				Log.d(TAG, "initSurface");
				c = h.lockCanvas();
			} finally {
				if (c != null)
					h.unlockCanvasAndPost(c);
			}
		}
	};

	@SuppressLint("DefaultLocale")
	private void getAllFiles(File root) {
		File files[] = root.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					getAllFiles(f);
				} else {
					try {
						String path = f.getPath();
						String type = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
						if (type.equals("ts") || type.equals("mpg")) {
							pathList.add(f.getPath());
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.i(TAG, e.getMessage());
					}

				}
			}
		}
	}


	TVSubtitleView.DVBSubParams subp;
	private boolean isPlaying = false;

	private int localUrl = 0;
	private int audio_target = 0;
	private int audio_lang = 0;
	private boolean isSubtitle = false;
	private String TmpUdp = "";

	private void settingPanel() {
		settingDialog = new Dialog(MainActivity.this, R.style.setting_dialog);
		settingDialog.setContentView(R.layout.setting_panel);
		final Spinner mLocalUrl = (Spinner) settingDialog.findViewById(R.id.local_path_spi);
		final EditText mUdpUrl = (EditText) settingDialog.findViewById(R.id.udp_path_edt);

		Button mStart = (Button) settingDialog.findViewById(R.id.player_btn);

		final Spinner mAudioStream = (Spinner) settingDialog.findViewById(R.id.audio_spi);
		final Spinner mLanguage = (Spinner) settingDialog.findViewById(R.id.lang_spi);

		Button mAudioSet = (Button) settingDialog.findViewById(R.id.audio_set);
		Button mLangSet = (Button) settingDialog.findViewById(R.id.lang_set);

		mUdpUrl.setTypeface(textType);
		mAudioSet.setTypeface(textType);
		mLangSet.setTypeface(textType);

		//mUdpUrl.setText("udp://239.192.1.1:1234");

		Log.i(TAG, "TmpUdp = " + TmpUdp);
		if (TmpUdp != null && !TmpUdp.equals("")) {
			mUdpUrl.setText(TmpUdp);
		}

		pathList.clear();
		getAllFiles(new File("/storage/"));
		final ArrayAdapter<String> _Adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_, pathList);
		_Adapter.setDropDownViewResource(R.layout.spinner_item);
		mLocalUrl.setAdapter(_Adapter);
		mLocalUrl.setSelection(localUrl);

		mStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mPlayer.isPlaying()) {
					mPlayer.stop();
				}
				String path = mUdpUrl.getText().toString();
				//TmpUdp = path;
				if (!path.equals("")) {
					String udp = getResources().getString(R.string.udp_head);
					if (!path.contains(udp)) {
						path = udp + path;
					}
					mPlayer.play(path);
					mHandler.sendEmptyMessage(SHOW_UDP_STATUS);
					settingDialog.cancel();
					settingDialog = null;
				} else if (pathList != null && pathList.size() > 0) {
					localUrl = mLocalUrl.getSelectedItemPosition();
					mPlayer.play(pathList.get(localUrl)); // cbc
					// false
					settingDialog.cancel();
					settingDialog = null;
				}
				if (false) {
					mPlayer.SetVideoSize(50, 50, 200, 150);
					try {
						Thread.sleep(5*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mPlayer.SetVideoSize(0, 0, 0, 0);
				}
			}
		});

		if (isPlaying) {
			ArrayList<String> audioName = new ArrayList<String>();
			final AudioStreamInfo[] audioList =mPlayer.get_audio_list();
			if (audioList != null) {
				for (int i = 0; i < audioList.length; i++) {
					audioName.add(audioList[i].lang);
				}
			}

			ArrayList<String> langName = new ArrayList<String>();
			final SubtitleStreamInfo[] subittle =mPlayer.get_subtitle_list();
			if (subittle != null) {
				for (int i = 0; i < subittle.length; i++) {
					langName.add(subittle[i].lang);
				}
			}

			final ArrayAdapter<String> audioAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_, audioName);
			audioAdapter.setDropDownViewResource(R.layout.spinner_item);
			mAudioStream.setAdapter(audioAdapter);

			final ArrayAdapter<String> LangAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_, langName);
			LangAdapter.setDropDownViewResource(R.layout.spinner_item);
			mLanguage.setAdapter(LangAdapter);

			if (audio_target > 0) {
				mAudioStream.setSelection(audio_target);
			}

			if (audio_lang > 0) {
				mLanguage.setSelection(audio_target);
			}

			mAudioSet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int pid = mAudioStream.getSelectedItemPosition();
					mPlayer.setAudioIndex(pid);
					audio_target = pid;
				}
			});

			mLangSet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					if (subp != null && subtitleView != null) {
						// subtitleView.clear();
						subtitleView.stop();
					}
					int index = mLanguage.getSelectedItemPosition();
					mPlayer.setSubtitleIndex(index);
					subp = new TVSubtitleView.DVBSubParams(0, subittle[index].pid, subittle[index].compositonPageId, subittle[index].ancillaryPageId);

					subtitleView.setMargin(100, 100, 100, 100);
					subtitleView.setActive(true);
					subtitleView.setSubParams(subp);
					subtitleView.startSub();
					subtitleView.show();

					audio_lang = index;
					// subtitleView.bringToFront();
				}
			});
		}

		settingDialog.show();
	}

}
