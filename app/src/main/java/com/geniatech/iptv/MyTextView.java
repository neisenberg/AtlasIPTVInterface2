package com.geniatech.iptv;

import com.example.natha_000.atlasiptvinterface.R;
import com.example.natha_000.atlasiptvinterface.R.color;
import android.content.res.Resources;
import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.content.res.*;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.TextView;

@SuppressLint("ResourceAsColor")
public class MyTextView extends TextView implements OnFocusChangeListener {

	private static final String TAG = "MyTextView";

	private Typeface textType;

	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		Log.d(TAG, "MyTextView()");
        System.out.println("Nathan: Stuff..." + getContext().getAssets());
		textType = Typeface.createFromAsset(getContext().getAssets(), "fonts/arial.ttf");
		this.setTypeface(textType);
		this.setOnFocusChangeListener(this);
		//this.setShadowLayer(4.0f, 3.0f, 3.0f, Color.BLACK);

	}

	@SuppressLint("ResourceAsColor")
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		Log.d(TAG, "onFocusChange()===========");
		if (hasFocus)
			this.setTextColor(R.color.bule);
		else
			this.setTextColor(R.color.white);
	}

}
