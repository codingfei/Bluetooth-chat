package com.ckt.yzf.bluetoothchat.filetransfer;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.ckt.yzf.bluetoothchat.protocol.DataProtocol;
import com.ckt.yzf.bluetoothchat.protocol.Message;
import com.ckt.yzf.bluetoothchat.task.Task;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


public class FileTransfer {
	/*
	 * 从服务器中下载APK
	 */
	public static boolean sendFile(Context context, String path, OutputStream out) {
		File file = new File(path);
		if (!file.exists()) {
			Toast.makeText(context, "文件不存在", Toast.LENGTH_LONG).show();
			return false;
		}
		Activity act = (Activity)context;
		Handler handler = new Handler(act.getMainLooper());
		android.os.Message msg = handler.obtainMessage();
		msg.what = Task.TASK_PROGRESS;
		msg.arg1 = (int) file.length();
		msg.arg2 = 0;
		handler.sendMessage(msg);
		byte[] data;
		try {
			data = DataProtocol.packFile(file);
		} catch (UnsupportedEncodingException e) {
			return false;
		}
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}
		BufferedOutputStream bos = new BufferedOutputStream(out);
		try {
			bos.write(data);
		} catch (IOException e) {
			try {
				fis.close();
				bos.close();
			} catch (Exception e1) {
			}
			return false;
		}
		int total = 0;
		int len;
		byte[] buffer = new byte[1024];
		while (true) {
			try {
				len = fis.read(buffer);
				if (len < 0)
					break;
				total += len;
				//------------------------------------------------
				msg = handler.obtainMessage();
				msg.what = Task.TASK_PROGRESS;
				msg.arg1 = (int) file.length();
				msg.arg2 = total;
				handler.sendMessage(msg);
				//------------------------------------------------
				bos.write(buffer, 0, len);
			} catch (IOException e) {
				return false;
			}
		}
		try {
			fis.close();
			bos.close();
		} catch (IOException e1) {
		}
		return true;
	}

	/**
	 * 接收文件
	 * 
	 * @throws Exception
	 */
	public static File writeFile(Message msg, BufferedInputStream bis,
			Activity activity) throws Exception {
		if (msg.total <= 0 || msg.fileName == null)
			return null;

		// 如果相等的话表示当前的sdcard挂载在手机上并且是可用的
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/BTChat", msg.fileName);
			FileOutputStream fos = new FileOutputStream(file);

			Task.CallBack cb = (Task.CallBack) activity;
			byte[] buffer = new byte[1024];
			int len;
			int total = 0;
			while ((len = bis.read(buffer)) != -1) {
				Task task = new Task(activity, Task.TASK_RECV_FILE, null);
				Message message = new Message();
				message.type = msg.type;
				message.total = msg.total;
				message.fileName = msg.fileName;
				message.remoteDevName = msg.remoteDevName;
				fos.write(buffer, 0, len);
				total += len;
				message.length = total;
				// 获取当前下载量
				task.mResult = msg;
				cb.onTaskFinished(task);
			}
			fos.close();
			bis.close();
			if (total != msg.total)
				return null;
			else
				return file;
		} else {
			return null;
		}
	}

}
