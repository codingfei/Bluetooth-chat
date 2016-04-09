package com.ckt.yzf.bluetoothchat.sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.ckt.yzf.bluetoothchat.R;

import java.util.HashMap;


public class SoundEffect implements SoundPool.OnLoadCompleteListener {
	private static SoundEffect mSound;
	private SoundPool mSoundPool;
	private int mLoadNum = 0;
	private HashMap<Integer, Integer> mSoundMap;
	
	public static SoundEffect getInstance(Context context){
		if(mSound == null){
			mSound = new SoundEffect(context);
		}
		return mSound;
	}
	
	private SoundEffect(Context context){
		mSoundMap = new HashMap<Integer, Integer>();
		// SoundPool(int maxStreams, int streamType, int srcQuality)
		mSoundPool= new SoundPool(2, AudioManager.STREAM_SYSTEM, 0);
		// load(Context context, int resId, int priority)
		mSoundMap.put(0, mSoundPool.load(context, R.raw.send, 1));
		mSoundMap.put(1, mSoundPool.load(context, R.raw.recv, 1));
		mSoundMap.put(2, mSoundPool.load(context, R.raw.error, 1));
		mSoundMap.put(3, mSoundPool.load(context, R.raw.play_completed, 1));
		mSoundPool.setOnLoadCompleteListener(this);
	}

	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
		mLoadNum ++;
	}
	
	/**
	 * 0：send sound
	 * 1: recv sound
	 * 2: error sound
	 * 3: play sound
	 * @param idx
	 */
	public void play(int idx){
		if(idx > mSoundMap.size() || idx < 0)
			return;
		if(mLoadNum < 4)
			return;
		// play(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate)
		mSoundPool.play(mSoundMap.get(idx), 1, 1, 0, 0, 1);
	}
}
