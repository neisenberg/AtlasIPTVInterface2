package com.example.natha_000.atlasiptvinterface;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.geniatech.iptv.MainActivity;
import com.geniatech.iptv.player;
import com.geniatech.iptv.SubtitleStreamInfo;
import com.geniatech.iptv.TVSubtitleView;
import java.util.ArrayList;
import java.util.List;

public class launcher extends Activity {

    public Context launcher;

    private static final String TAG = "MainActivity";
    private player mPlayer;
    private static int VIDEO_HOLE = 257;

    private Typeface textType;
    private SurfaceView mSurfaceView = null;
    private TVSubtitleView subtitleView;
    private Dialog settingDialog;
    private LinearLayout infoLayout;
    private static final int SHOW_PANLE = 0X009;
    private static final int SHOW_UDP_STATUS = 0X011;
    public String Thumbs[];
    private ArrayList<String> pathList = new ArrayList<String>();
    private static int AudioStream = 0;

    //XML Channel Guide
    static final String URL = "http://testarea.atlasiptv.com/channels.xml";
    static final String KEY_ITEM = "channel"; // parent node
    static final String KEY_ID = "id";
    static final String KEY_CODE = "code";
    static final String KEY_NAME = "name";
    static final String KEY_URL = "url";

    private Toast toast0;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case SHOW_PANLE:
                    infoLayout.setVisibility(View.GONE);
                    //settingPanel();
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

public Integer[] mThumbIds = {
        R.drawable.abc, R.drawable.abcf,
        R.drawable.ae, R.drawable.aljazz,
        R.drawable.angelone, R.drawable.angeltwo,
        R.drawable.animalplanet, R.drawable.babyfirst
};
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        IPTVHandler iptvlist = new IPTVHandler();
        List<IPTVChannels> iptvchannels = iptvlist.getAllChannels();


        for (IPTVChannels cn : iptvchannels) {
            String log = "Id: "+cn.getID()+" ,Name: " + cn.getName() + " ,URL: " + cn.getUrl();
            // Writing Contacts to log
            Log.d("Nathan: ", log);

        }

        setContentView(R.layout.activity_launcher);
        GridView gridview = (GridView) findViewById(R.id.gridview);
        //gridview.setAdapter(new ImageAdapter(this, ));

        gridview.setAdapter(new ImageAdapter(this, mThumbIds));
        //gridview.imageView.setImageResource(launcher.mThumbIds[position]);

        mPlayer = new player();
        toast0 = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_LONG);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(launcher.this, "" + position, Toast.LENGTH_SHORT).show();

                setContentView(R.layout.main);
                findView();
                initData();
                new Thread(run).start();
                String path = "udp://239.0.0.17:10005";
                mPlayer.play(path);
                //settingDialog.cancel();
                //mPlayer.SetVideoSize(50, 50, 200, 150);
            }
        });
    }

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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String udp = getResources().getString(R.string.udp_head);
        int retry = 2;
        boolean ret = false;
        String path = "239.0.0.48:10005";
        // TODO Auto-generated method stub
        System.out.println(event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                //settingPanel();
                break;
            case KeyEvent.KEYCODE_PAGE_UP:
                if (Global.channel+1 < Global.channels.length) {
                    Global.channel = Global.channel + 1;
                } else {
                    Global.channel = 0;
                    System.out.println("Nathan: Array exhausted, wrapping");
                }
                System.out.println("NATHAN: Changing channel up to " + Global.channels[Global.channel][1] + " at " + Global.channels[Global.channel][0]);

                //toast0.setDuration(Toast.LENGTH_LONG);
                toast0.setText(Global.channels[Global.channel][1]);
                toast0.show();

                if (mPlayer.isPlaying()) {
                    mPlayer.stop();
                    //mPlayer.finalize();
                   // mPlayer = new player();
                    //while (!ret&&(retry>0)&&(mPlayer!=null)) {
                     //   retry -= 1;
                    //    ret = mPlayer.configVMServer("public2.verimatrix.com", 12686, "/data/data/com.geniatech.iptv/verimatrix.store");
                   // }
                }
                path = Global.channels[Global.channel][0];
                if (!path.contains(udp)) {
                    path = udp + path;
                }
                mPlayer.play(path);
                break;
            case KeyEvent.KEYCODE_PAGE_DOWN:
                if (Global.channel-1 >= 0) {
                    Global.channel = Global.channel - 1;
                } else {
                    Global.channel = Global.channels.length - 1;
                    System.out.println("Nathan: Array exhausted, wrapping");
                }
                System.out.println("NATHAN: Changing channel down to " + Global.channels[Global.channel]);

                if (mPlayer.isPlaying()) {
                    mPlayer.stop();
                    //mPlayer.finalize();
                    mPlayer = new player();
                }
                while (!ret&&(retry>0)&&(mPlayer!=null)) {
                    retry -= 1;
                    ret = mPlayer.configVMServer("public2.verimatrix.com", 12686, "/data/data/com.geniatech.iptv/verimatrix.store");
                }

                path = Global.channels[Global.channel][0];
                if (!path.contains(udp)) {
                    path = udp + path;
                }
                mPlayer.play(path);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}


