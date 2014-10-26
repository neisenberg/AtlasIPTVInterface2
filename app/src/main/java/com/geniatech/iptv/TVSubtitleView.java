package com.geniatech.iptv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author geniatech
 * @version 1.0
 * TVSubtitleView Provide digital / analog TV caption and text information support.
 * Currently supports DVB subtitle, DTV/ATV teletext, ATSC/NTSC closed caption.
 */
public class TVSubtitleView extends View {
	private static final String TAG="TVSubtitleView";

	private static Object lock = new Object();
	private static final int BUFFER_W = 1920;
	private static final int BUFFER_H = 1080;

	private static final int MODE_NONE=0;
	private static final int MODE_DTV_TT=1;
	private static final int MODE_DTV_CC=2;
	private static final int MODE_DVB_SUB=3;
	private static final int MODE_ATV_TT=4;
	private static final int MODE_ATV_CC=5;

	private static final int PLAY_NONE= 0;
	private static final int PLAY_SUB = 1;
	private static final int PLAY_TT  = 2;

	public static final int COLOR_RED=0;
	public static final int COLOR_GREEN=1;
	public static final int COLOR_YELLOW=2;
	public static final int COLOR_BLUE=3;

	private static int init_count=0;

	private native int native_sub_init();
	private native int native_sub_destroy();
	private native int native_sub_lock();
	private native int native_sub_unlock();
	private native int native_sub_clear();
	private native int native_sub_start_dvb_sub(int dmx_id, int pid, int page_id, int anc_page_id);
	private native int native_sub_start_dtv_tt(int dmx_id, int region_id, int pid, int page, int sub_page, boolean is_sub);
	private native int native_sub_stop_dvb_sub();
	private native int native_sub_stop_dtv_tt();
	private native int native_sub_tt_goto(int page);
	private native int native_sub_tt_color_link(int color);
	private native int native_sub_tt_home_link();
	private native int native_sub_tt_next(int dir);
	private native int native_sub_tt_set_search_pattern(String pattern, boolean casefold);
	private native int native_sub_tt_search_next(int dir);
	protected native int native_get_subtitle_picture_width();
	protected native int native_get_subtitle_picture_height();
	private native int native_sub_start_atsc_cc(int caption, int fg_color, int fg_opacity, int bg_color, int bg_opacity, int font_style, int font_size);
	private native int native_sub_stop_atsc_cc();
	private native int native_sub_set_active(boolean active);

	/*static{
		System.loadLibrary("am_adp");
		System.loadLibrary("am_mw");
		System.loadLibrary("zvbi");
		System.loadLibrary("jnitvsubtitle");
	}*/

	/**
	 * DVB subtitle 参数
	 */
	static public class DVBSubParams{
		private int dmx_id;
		private int pid;
		private int composition_page_id;
		private int ancillary_page_id;

		/**
		 * 创建DVB subtitle参数
		 * @param dmx_id 接收使用demux设备的ID
		 * @param pid subtitle流的PID
		 * @param page_id 字幕的page_id
		 * @param anc_page_id 字幕的ancillary_page_id
		 */
		public DVBSubParams(int dmx_id, int pid, int page_id, int anc_page_id){
			this.dmx_id              = dmx_id;
			this.pid                 = pid;
			this.composition_page_id = page_id;
			this.ancillary_page_id   = anc_page_id;
		}
	}

	/**
	 * 数字电视teletext图文参数
	 */
	static public class DTVTTParams{
		private int dmx_id;
		private int pid;
		private int page_no;
		private int sub_page_no;
		private int region_id;

		/**
		 * 创建数字电视teletext图文参数
		 * @param dmx_id 接收使用demux设备的ID
		 * @param pid 图文信息流的PID
		 * @param page_no 要显示页号
		 * @param sub_page_no 要显示的子页号
		 */
		public DTVTTParams(int dmx_id, int pid, int page_no, int sub_page_no, int region_id){
			this.dmx_id      = dmx_id;
			this.pid         = pid;
			this.page_no     = page_no;
			this.sub_page_no = sub_page_no;
			this.region_id   = region_id;
		}
	}

	static public class ATVTTParams{
	}

	static public class DTVCCParams{
		private int caption_mode;
		private int fg_color;
		private int fg_opacity;
		private int bg_color;
		private int bg_opacity;
		private int font_style;
		private int font_size;

		public DTVCCParams(int caption, int fg_color, int fg_opacity, 
			int bg_color, int bg_opacity, int font_style, int font_size){
			this.caption_mode = caption;
			this.fg_color = fg_color;
			this.fg_opacity = fg_opacity;
			this.bg_color = bg_color;
			this.bg_opacity = bg_opacity;
			this.font_style = font_style;
			this.font_size = font_size;
		}
	}

	static public class ATVCCParams{
	}
	@SuppressWarnings("unused")
	private class SubParams{
		int mode;
		DVBSubParams dvb_sub;
		DTVTTParams  dtv_tt;
		ATVTTParams  atv_tt;
		DTVCCParams  dtv_cc;
		ATVCCParams  atv_cc;

		private SubParams(){
			mode = MODE_NONE;
		}
	}

	@SuppressWarnings("unused")
	private class TTParams{
		int mode;
		DTVTTParams dtv_tt;
		ATVTTParams atv_tt;

		private TTParams(){
			mode = MODE_NONE;
		}
	}

	private int disp_left=0;
	private int disp_right=0;
	private int disp_top=0;
	private int disp_bottom=0;
	private boolean active=true;

	private static SubParams sub_params;
	private static TTParams  tt_params;
	private static int       play_mode=PLAY_NONE;
	private static boolean   visible;
	private static boolean   destroy;
	private static Bitmap bitmap = null;
	private static TVSubtitleView activeView=null;

	private void update() {
		Log.i(TAG, "update================");
		postInvalidate();
	}

	private void stopDecoder(){
		synchronized(lock){
		switch(play_mode){
			case PLAY_NONE:
				break;
			case PLAY_TT:
				switch(tt_params.mode){
					case MODE_DTV_TT:
						native_sub_stop_dtv_tt();
						break;
					default:
						break;
				}
				break;
			case PLAY_SUB:
				switch(sub_params.mode){
					case MODE_DTV_TT:
						native_sub_stop_dtv_tt();
						break;
					case MODE_DVB_SUB:
						native_sub_stop_dvb_sub();
						break;
					case MODE_DTV_CC:
						native_sub_stop_atsc_cc();
						break;
					default:
						break;
				}
				break;
		}

		play_mode = PLAY_NONE;
		}
	}

	private void init(){
		synchronized(lock){
		if(init_count == 0){
			play_mode  = PLAY_NONE;
			visible    = true;
			destroy    = false;
			tt_params  = new TTParams();
			sub_params = new SubParams();
			
			if(bitmap==null){
				bitmap = Bitmap.createBitmap(BUFFER_W, BUFFER_H, Bitmap.Config.ARGB_8888);
			}
			
			if(native_sub_init()<0){
			}
		}

		init_count++;
		}
	}

	/**
	 * 创建TVSubtitle控件
	 */
	public TVSubtitleView(Context context){
		super(context);
		init();
	}

	/**
	 * 创建TVSubtitle控件
	 */
	public TVSubtitleView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	/**
	 * 创建TVSubtitle控件
	 */
	public TVSubtitleView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();
	}

	/**
	 *设定显示边缘空隙
	 *@param left 左边缘宽度
	 *@param top 顶部边缘高度
	 *@param right 右部边缘宽度
	 *@param bottom 底部边缘高度
	 */
	public void setMargin(int left, int top, int right, int bottom){
		disp_left   = left;
		disp_top    = top;
		disp_right  = right;
		disp_bottom = bottom;
	}

	/**
	 * 设定控件活跃状态
	 * @param active 活跃/不活跃
	 */
	public void setActive(boolean active){
		synchronized(lock){
		native_sub_set_active(active);
		this.active = active;
		if(active){
			activeView = this;
		/*}else if(activeView == this){
			activeView = null;*/
		}
		postInvalidate();
		}
	}

	/**
	 * 设定字幕参数
	 * @param params 字幕参数
	 */
	public void setSubParams(DVBSubParams params){
		synchronized(lock){
		sub_params.mode = MODE_DVB_SUB;
		sub_params.dvb_sub = params;

		if(play_mode==PLAY_SUB)
			startSub();
		}
	}

	/**
	 * 设定字幕参数
	 * @param params 字幕参数
	 */
	public void setSubParams(DTVTTParams params){
		synchronized(lock){
		sub_params.mode = MODE_DTV_TT;
		sub_params.dtv_tt = params;

		if(play_mode==PLAY_SUB)
			startSub();
		}
	}

	/**
	 * 设定close caption字幕参数
	 * @param params 字幕参数
	 */
	public void setSubParams(DTVCCParams params){
		synchronized(lock){
		sub_params.mode = MODE_DTV_CC;
		sub_params.dtv_cc= params;

		if(play_mode==PLAY_SUB)
			startSub();
		}
	}

	/**
	 * 设定图文参数
	 * @param params 字幕参数
	 */
	public void setTTParams(DTVTTParams params){
		synchronized(lock){
		tt_params.mode = MODE_DTV_TT;
		tt_params.dtv_tt = params;

		if(play_mode==PLAY_TT)
			startTT();
		}
	}

	/**
	 * 显示字幕/图文信息
	 */
	public void show(){
		if(visible)
			return;

		Log.d(TAG, "show");

		visible = true;
		update();
	}

	/**
	 * 隐藏字幕/图文信息
	 */
	public void hide(){
		if(!visible)
			return;

		Log.d(TAG, "hide");

		visible = false;
		update();
	}

	/**
	 * 开始图文信息解析
	 */
	public void startTT(){
		synchronized(lock){
		if(activeView != this)
			return;
		
		stopDecoder();

		if(tt_params.mode==MODE_NONE)
			return;

		int ret = 0;
		switch(tt_params.mode){
			case MODE_DTV_TT:
				ret = native_sub_start_dtv_tt(tt_params.dtv_tt.dmx_id,
						tt_params.dtv_tt.region_id,
						tt_params.dtv_tt.pid,
						tt_params.dtv_tt.page_no,
						tt_params.dtv_tt.sub_page_no,
						false);
				break;
			default:
				break;
		}

		if(ret >= 0)
			play_mode = PLAY_TT;
		}
	}

	/**
	 * 开始字幕信息解析
	 */
	public void startSub(){
		synchronized(lock){
		if(activeView != this)
			return;
		
		stopDecoder();

		if(sub_params.mode==MODE_NONE)
			return;

		int ret = 0;
		switch(sub_params.mode){
			case MODE_DVB_SUB:
				ret = native_sub_start_dvb_sub(sub_params.dvb_sub.dmx_id,
						sub_params.dvb_sub.pid,
						sub_params.dvb_sub.composition_page_id,
						sub_params.dvb_sub.ancillary_page_id);
				break;
			case MODE_DTV_TT:
				ret = native_sub_start_dtv_tt(sub_params.dtv_tt.dmx_id,
						sub_params.dtv_tt.region_id,
						sub_params.dtv_tt.pid,
						sub_params.dtv_tt.page_no,
						sub_params.dtv_tt.sub_page_no,
						true);
				break;
			case MODE_DTV_CC:
				ret = native_sub_start_atsc_cc(
					sub_params.dtv_cc.caption_mode, 
					sub_params.dtv_cc.fg_color,
					sub_params.dtv_cc.fg_opacity,
					sub_params.dtv_cc.bg_color,
					sub_params.dtv_cc.bg_opacity,
					sub_params.dtv_cc.font_style,
					sub_params.dtv_cc.font_size);
				break;
			default:
				break;
		}

		if(ret >= 0)
			play_mode = PLAY_SUB;
		}
	}

	/**
	 * 停止图文/字幕信息解析
	 */
	public void stop(){
		synchronized(lock){
		if(activeView != this)
			return;
		stopDecoder();
		}
	}

	/**
	 * 停止图文/字幕信息解析并清除缓存数据
	 */
	public void clear(){
		synchronized(lock){
		if(activeView != this)
			return;
		stopDecoder();
		native_sub_clear();
		tt_params.mode  = MODE_NONE;
		sub_params.mode = MODE_NONE;
		}
	}

	/**
	 * 在图文模式下进入下一页
	 */
	public void nextPage(){
		synchronized(lock){
		if(activeView != this)
			return;
		if(play_mode!=PLAY_TT)
			return;

		native_sub_tt_next(1);
		}
	}

	/**
	 * 在图文模式下进入上一页
	 */
	public void previousPage(){
		synchronized(lock){
		if(activeView != this)
			return;
		if(play_mode!=PLAY_TT)
			return;

		native_sub_tt_next(-1);
		}
	}

	/**
	 * 在图文模式下跳转到指定页
	 * @param page 要跳转到的页号
	 */
	public void gotoPage(int page){
		synchronized(lock){
		if(activeView != this)
			return;
		if(play_mode!=PLAY_TT)
			return;

		native_sub_tt_goto(page);
		}
	}

	/**
	 * 在图文模式下跳转到home页
	 */
	public void goHome(){
		synchronized(lock){
		if(activeView != this)
			return;
		if(play_mode!=PLAY_TT)
			return;

		native_sub_tt_home_link();
		}
	}

	/**
	 * 在图文模式下根据颜色跳转到指定链接
	 * @param color 颜色，COLOR_RED/COLOR_GREEN/COLOR_YELLOW/COLOR_BLUE
	 */
	public void colorLink(int color){
		synchronized(lock){
		if(activeView != this)
			return;
		if(play_mode!=PLAY_TT)
			return;

		native_sub_tt_color_link(color);
		}
	}

	/**
	 * 在图文模式下设定搜索字符串
	 * @param pattern 搜索匹配字符串
	 * @param casefold 是否区分大小写
	 */
	public void setSearchPattern(String pattern, boolean casefold){
		synchronized(lock){
		if(activeView != this)
			return;
		if(play_mode!=PLAY_TT)
			return;

		native_sub_tt_set_search_pattern(pattern, casefold);
		}
	}

	/**
	 * 搜索下一页
	 */
	public void searchNext(){
		synchronized(lock){
		if(activeView != this)
			return;
		if(play_mode!=PLAY_TT)
			return;

		native_sub_tt_search_next(1);
		}
	}

	/**
	 * 搜索上一页
	 */
	public void searchPrevious(){
		synchronized(lock){
		if(activeView != this)
			return;
		if(play_mode!=PLAY_TT)
			return;

		native_sub_tt_search_next(-1);
		}
	}

	int i = 0;
	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas){
		Log.i(TAG, "canvas=========================");
		synchronized(lock){
		Rect sr;
		Rect dr = new Rect(disp_left, disp_top, getWidth() - disp_right, getHeight()- disp_bottom);
		if(!active || !visible || (play_mode==PLAY_NONE)){
			return;
		}

		native_sub_lock();
		
		if(play_mode==PLAY_TT || sub_params.mode==MODE_DTV_TT || sub_params.mode==MODE_ATV_TT){
			sr = new Rect(0, 0, 12*41, 10*25);
		}else if (play_mode==PLAY_SUB){
			sr = new Rect(0, 0, native_get_subtitle_picture_width(), native_get_subtitle_picture_height());
		}else{
			sr = new Rect(0, 0, BUFFER_W, BUFFER_H);
		}
		//bitmap =getNewBitMap("Test font");
		canvas.drawBitmap(bitmap, sr, dr, new Paint());
		
		/*try {
			String fileName = "/storage/external_storage/sdcard1/img/000"+i+".bmp";
			File file = new File(fileName);
			FileOutputStream outStream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, outStream);
			outStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 i ++;*/

		native_sub_unlock();
		}
	}
	
	private Bitmap getNewBitMap(String text) {
        Bitmap newBitmap = Bitmap.createBitmap(120,150, Config.ARGB_4444);
        Canvas canvas = new Canvas(newBitmap);
        //canvas.drawBitmap(bmp, 0, 0, null);
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(16.0F);
        StaticLayout sl= new StaticLayout(text, textPaint, newBitmap.getWidth()-8, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas.translate(6, 40);
        sl.draw(canvas);
        return newBitmap;
    }

	public void dispose(){
		synchronized(lock){
		if(!destroy){
			init_count--;
			destroy = true;
			if(init_count == 0){
				stopDecoder();
				native_sub_clear();
				native_sub_destroy();
			}
		}
		}
	}

	protected void finalize() throws Throwable {
	//	dispose();
		super.finalize();
	}  
}

