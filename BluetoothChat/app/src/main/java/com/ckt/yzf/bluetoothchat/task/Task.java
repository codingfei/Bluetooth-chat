package com.ckt.yzf.bluetoothchat.task;

import android.app.Activity;

public class Task {
	public static final int TASK_START_ACCEPT = 1;
	public static final int TASK_START_CONN_THREAD = 2;
	public static final int TASK_SEND_MSG = 3;
	public static final int TASK_GET_REMOTE_STATE = 4;
	public static final int TASK_RECV_MSG = 5;
	public static final int TASK_RECV_FILE = 6;
	/** mParam[0]：path */
	public static final int TASK_SEND_FILE = 7;
	public static final int TASK_PROGRESS = 8;
	/**
	 * ÿ����Ҫ�ύ�����Acitivy��Ҫȥʵ�ָýӿ�
	 * @author Administrator
	 */
	public static interface CallBack{
		/**
		 * ���������ʱ��TaskService��ص�÷���֪ͨActiivty�����Ѿ����
		 * @param task
		 */
		public void onTaskFinished(Task task);
	}
	
	// ����ID
	private int mTaskID;
	// ����Ĳ����б�
	public Object[] mParams;
	// ������ɵĽ��
	public Object mResult;
	
	private Activity mContext;
	
	
	public Task(Activity context, int taskID, Object[] params){
		this.mContext = context;
		this.mTaskID = taskID;
		this.mParams = params;
		this.mResult = null;
	}
	
	public Activity getActivity(){
		return this.mContext;
	}
	
	public int getTaskID(){
		return mTaskID;
	}
}
