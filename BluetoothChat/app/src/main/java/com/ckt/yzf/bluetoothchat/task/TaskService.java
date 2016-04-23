package com.ckt.yzf.bluetoothchat.task;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ckt.yzf.bluetoothchat.UI.ChatListViewAdapter;
import com.ckt.yzf.bluetoothchat.filetransfer.FileTransfer;
import com.ckt.yzf.bluetoothchat.protocol.DataProtocol;
import com.ckt.yzf.bluetoothchat.protocol.Message;
import com.ckt.yzf.bluetoothchat.sound.SoundEffect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * 任务处理服务
 * @author Administrator
 *
 */
public class TaskService extends Service {
	private final String TAG = "TaskService";
	private TaskThread mThread;
	public static int unReadCount = 0;

	private BluetoothAdapter mBluetoothAdapter;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	
	private boolean isServerMode = true;
	
	private Activity mActivity;
		
	// 任务队列
	private static ArrayList<Task> mTaskList = new ArrayList<Task>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		mThread = new TaskThread();
		mThread.start();
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		if (mBluetoothAdapter == null) {     
			// Device does not support Bluetooth 
			Log.e(TAG, "Your device is not support Bluetooth!");
			return;
		}
	}

	public static void newTask(Task target){
		synchronized (mTaskList) {
			mTaskList.add(target);
		}
	}
	
	private class TaskThread extends Thread{
		private boolean isRun = true;
		private int mCount = 0;

		public void cancel(){
			isRun = false;
		}
		
		@Override
		public void run() {
			Task task;
			while(isRun){
				
				// 有任务
				if(mTaskList.size() > 0){
					synchronized (mTaskList) {
						// 获得任务
						task = mTaskList.get(0);
						mActivity = task.getActivity();
						doTask(task);
					}
				}else{
					try {
						Thread.sleep(200);
						mCount++;
					} catch (InterruptedException e) {
					}
					if(mCount >= 5){
						mCount = 0;
						newTask(new Task(mActivity, Task.TASK_GET_REMOTE_STATE, null));
					}
				}
			}
		}
		
	}
	
	private void doTask(Task task){
		switch(task.getTaskID()){
		case Task.TASK_GET_REMOTE_STATE:
			if(mAcceptThread != null && mAcceptThread.isAlive()){
				task.mResult = "等待连接...";
			}else if(mCommThread != null && mCommThread.isAlive()){
				if(unReadCount == 0){
					task.mResult = mCommThread.getRemoteName() + "[在线]";
				}else
				{
					task.mResult = mCommThread.getRemoteName() + "[未读  " + unReadCount + "]";
				}
			}else if(mConnectThread != null && mConnectThread.isAlive()){
				task.mResult = "正在连接：" + mConnectThread.getDevice().getName();
				SoundEffect.getInstance(TaskService.this).play(3);
			}else{
				task.mResult = "未知状态";
				SoundEffect.getInstance(TaskService.this).play(2);
				// 重新等待连接
				mAcceptThread = new AcceptThread();
				mAcceptThread.start();
				isServerMode = true;
			}
			break;
		case Task.TASK_START_ACCEPT:
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
			isServerMode = true;
			break;
		case Task.TASK_START_CONN_THREAD:
			if(task.mParams == null || task.mParams.length == 0){
				task.mResult = null;
				break;
			}
			BluetoothDevice remote = (BluetoothDevice) task.mParams[0];
			mConnectThread = new ConnectThread(remote);
			mConnectThread.start();
			isServerMode = false; 
			break;
		case Task.TASK_SEND_MSG:
			if(mCommThread == null || !mCommThread.isAlive()){
				Log.e(TAG, "mCommThread");
				task.mResult = null;
				break;
			}
			if(task.mParams == null || task.mParams.length == 0){
				Log.e(TAG, "task.mParams");
				task.mResult = null;
				break;
			}
			byte[] msg = null;
			try {
				msg = DataProtocol.packMsg((String) task.mParams[0]);
			} catch (UnsupportedEncodingException e) {
			}
			mCommThread.write(msg);
			task.mResult = (String) task.mParams[0];
			break;
			
		case Task.TASK_SEND_FILE:
			if(mCommThread != null && mCommThread.isAlive()){
				if(task.mParams[0] != null)
				if(FileTransfer.sendFile(mActivity, (String) task.mParams[0], mCommThread.getOutputStream()))
					task.mResult = "文件" + (String)task.mParams[0] + "发送成功!";
			}
			task.mResult = null;
			break;
		}

		Task.CallBack cb = (Task.CallBack) task.getActivity();
		
		cb.onTaskFinished(task);
		// 移除任务
		mTaskList.remove(task);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mThread.cancel();
	}
	
	private final String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
	/**
	 * 等待客户端连接线程
	 * @author Administrator
	 */
	private class AcceptThread extends Thread { 
		private final BluetoothServerSocket mmServerSocket; 
		private boolean isCancel = false;
		public AcceptThread() {
			Log.d(TAG, "AcceptThread");
			BluetoothServerSocket tmp = null;        
			try {
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MT_Chat_Room", UUID.fromString(UUID_STR));         
			} catch (IOException e) { }   
			mmServerSocket = tmp;     
		}
		public void run() {
			BluetoothSocket socket = null;  
			while (true) { 
				try {       
					// 阻塞等待
					if(mmServerSocket != null)
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					if(!isCancel){
						try {
							mmServerSocket.close();
						} catch (IOException e1) {
						}
						mAcceptThread = new AcceptThread();
						mAcceptThread.start();
						isServerMode = true;
					}
					break;
				}    
				if (socket != null) {                               
					manageConnectedSocket(socket); 
					try {
						mmServerSocket.close();
					} catch (IOException e) {
					}
					mAcceptThread = null;
					break;          
				}    
			}
		}
		
		public void cancel(){
			try {
				Log.d(TAG, "AcceptThread canceled");
				isCancel = true;
				isServerMode = false;
				mmServerSocket.close();
				mAcceptThread = null;
				if(mCommThread != null && mCommThread.isAlive()){
					mCommThread.cancel();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 作为客户端连接指定的蓝牙设备线程
	 * @author Administrator
	 */
	private class ConnectThread extends Thread {     
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		
		public ConnectThread(BluetoothDevice device) {
			
			Log.d(TAG, "ConnectThread");
			
			if(mAcceptThread != null && mAcceptThread.isAlive()){
				mAcceptThread.cancel();
			}
			
			if(mCommThread != null && mCommThread.isAlive()){
				mCommThread.cancel();
			}
			
			// Use a temporary object that is later assigned to mmSocket,         
			// because mmSocket is final    
			BluetoothSocket tmp = null;
			mmDevice = device;        
			try {
				tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(UUID_STR));       
			} catch (IOException e) {
				Log.d(TAG, "createRfcommSocketToServiceRecord error!");
			}       
			
			mmSocket = tmp;    
		}
		
		public BluetoothDevice getDevice(){
			return mmDevice;
		}
		
		public void run() { 
			// Cancel discovery because it will slow down the connection       
			mBluetoothAdapter.cancelDiscovery();
			try {            
				// Connect the device through the socket. This will block             
				// until it succeeds or throws an exception          
				mmSocket.connect();       
			} catch (IOException connectException) {   
					// Unable to connect; close the socket and get out   
				Log.e(TAG, "Connect server failed");
				try {                
					mmSocket.close();   
				} catch (IOException closeException) { }
				mAcceptThread = new AcceptThread();
				mAcceptThread.start();
				isServerMode = true;
				return;  
			}           // Do work to manage the connection (in a separate thread)   
			manageConnectedSocket(mmSocket);    
		}       
		
		public void cancel() {
			try {
				mmSocket.close();  
			} catch (IOException e) { } 
			mConnectThread = null;
		} 
	}
	
	private ConnectedThread mCommThread ;
	private void manageConnectedSocket(BluetoothSocket socket){
		// 启动子线程来维持连接
		mCommThread = new ConnectedThread(socket);
		mCommThread.start();
	}
	
	
	private class ConnectedThread extends Thread { 
		private final BluetoothSocket mmSocket;  
		private final InputStream mmInStream;    
		private final OutputStream mmOutStream;  
		private BufferedOutputStream mmBos;
		private byte[] buffer;
		
		public ConnectedThread(BluetoothSocket socket) {  
			Log.d(TAG, "ConnectedThread");
			mmSocket = socket;         
			InputStream tmpIn = null;    
			OutputStream tmpOut = null;          
			try {             
				tmpIn = socket.getInputStream(); 
				tmpOut = socket.getOutputStream();  
			} catch (IOException e) { }
			mmInStream = tmpIn;        
			mmOutStream = tmpOut;
			mmBos = new BufferedOutputStream(mmOutStream);
		}
		
		public OutputStream getOutputStream(){
			return mmOutStream;
		}
				
		public void write(byte[] msg){
			if(msg == null)
				return;
			try {
				mmBos.write(msg);
				mmBos.flush();
				System.out.println("Write:" + msg);
			} catch (IOException e) {
			}
		}
		
		public String getRemoteName(){
			return mmSocket.getRemoteDevice().getName();
		}
		
		public void cancel(){
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
			mCommThread = null;
		}
		
		public void run() {  
			try {
				write(DataProtocol.packMsg(mBluetoothAdapter.getName() + "已经上线\n"));
			} catch (UnsupportedEncodingException e2) {
			}
			int size;
			Message msg;
			buffer = new byte[1024];
		    Task task = new Task(mActivity, Task.TASK_RECV_MSG, null);
    		Task.CallBack cb = (Task.CallBack) mActivity;
    		BufferedInputStream bis = new BufferedInputStream(mmInStream);
    		//BufferedReader br = new BufferedReader(new InputStreamReader(mmInStream));
    		HashMap<String, Object> data;
		    while (true) {
		    	try {
		    		//msg = br.readLine();
		    		size = bis.read(buffer);
		    		msg = DataProtocol.unpackData(buffer);
		    		if(msg == null)
		    			continue;
		    		msg.remoteDevName = mmSocket.getRemoteDevice().getName();
		    		if(msg.type == DataProtocol.TYPE_FILE){
		    			// 先接收文件信息
		    			Task tsk = new Task(mActivity, Task.TASK_RECV_FILE, null);
		    			tsk.mResult = msg;
			    		cb.onTaskFinished(tsk);
		    			try {
		    				// 执行文件写入
							File f = FileTransfer.writeFile(msg, bis, mActivity);
							if(f == null){
								cb.onTaskFinished(new Task(mActivity, Task.TASK_RECV_FILE, null));
								continue;
							}
						} catch (Exception e) {
			    			continue;
						}
		    		}else if(msg.type == DataProtocol.TYPE_MSG){
			    		data = new HashMap<String, Object>();
			    		System.out.println("Read data.");
			    		data.put(ChatListViewAdapter.KEY_ROLE, ChatListViewAdapter.ROLE_TARGET);
			    		data.put(ChatListViewAdapter.KEY_NAME, msg.remoteDevName);
			    		data.put(ChatListViewAdapter.KEY_TEXT, msg.msg);
			    		task.mResult = data;
		    		}
		    		cb.onTaskFinished(task);
		    	} catch (IOException e) {
		    		try {
						mmSocket.close();
					} catch (IOException e1) {
					}
					mCommThread = null;
					if(isServerMode){
						TaskService.newTask(new Task(mActivity, Task.TASK_GET_REMOTE_STATE, null));
						SoundEffect.getInstance(TaskService.this).play(2);
						mAcceptThread = new AcceptThread();
						mAcceptThread.start();
					}
		    		break;
		    	}
		    }
		} 
	}
	
	//================================================================

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
