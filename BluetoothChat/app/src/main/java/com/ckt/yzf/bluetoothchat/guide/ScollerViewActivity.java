package com.ckt.yzf.bluetoothchat.guide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ckt.yzf.bluetoothchat.ChatActivity;
import com.ckt.yzf.bluetoothchat.R;
import com.lidroid.xutils.view.annotation.ViewInject;


public class ScollerViewActivity extends Activity implements
		OnScrollChangedListener {
	@ViewInject(R.id.ll_anim)
	private LinearLayout mLLAnim;
	private MyScrollView mSVmain;
	private int mScrollViewHeight;
	private int mStartAnimateTop;
	private boolean hasStart = false;
	@ViewInject(R.id.tvInNew)
	private TextView tvInNew;
	private SharedPreferences sharedPreferences;
	private boolean isFirst =  false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = getSharedPreferences("BlueChat", Context.MODE_PRIVATE);
		if (sharedPreferences.getBoolean("firststart", true)) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			//将登录标志位设置为false，下次登录时不在显示首次登录界面
			editor.putBoolean("firststart", false);
			editor.commit();
			setContentView(R.layout.activity_scrollview);
			com.lidroid.xutils.ViewUtils.inject(this);
			initView();
			setView();
			isFirst = true;
		}
		else
		{
			startActivity(new Intent(ScollerViewActivity.this,ChatActivity.class));
		}
	}

	private void initView() {
		mSVmain = (MyScrollView) findViewById(R.id.sv_main);
		tvInNew.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(ScollerViewActivity.this,ChatActivity.class));
				AnimationUtil.finishActivityAnimation(ScollerViewActivity.this);
			}
		});
	}

	private void setView() {
		mSVmain.setOnScrollChangedListener(this);
		mLLAnim.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		mScrollViewHeight = mSVmain.getHeight();
		mStartAnimateTop = mScrollViewHeight / 5 * 4;
	}

	@Override
	public void onScrollChanged(int top, int oldTop) {
		int animTop = mLLAnim.getTop() - top;
		if (top > oldTop) {
			if (animTop < mStartAnimateTop && !hasStart) {
				Animation anim = AnimationUtils
						.loadAnimation(this, R.anim.show);
				mLLAnim.setVisibility(View.VISIBLE);
				mLLAnim.startAnimation(anim);
				hasStart = true;
			}
		} else {
			if (animTop > mStartAnimateTop && hasStart) {
				Animation anim = AnimationUtils.loadAnimation(this,
						R.anim.close);
				mLLAnim.setVisibility(View.INVISIBLE);
				mLLAnim.startAnimation(anim);
				hasStart = false;
			}
		}
	}

}
