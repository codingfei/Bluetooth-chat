package com.ckt.yzf.bluetoothchat.notify;

public class NotifyMessage {
	/*public void notifyMessage(){
		//消息通知栏
        //定义NotificationManager
        String ns = Context.NOTIFICATION_SERVICE;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        //定义通知栏展现的内容信息

        int icon = R.drawable.icon;

        CharSequence tickerText = "我的通知栏标题";

        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);

      

        //定义下拉通知栏时要展现的内容信息

        Context context = getApplicationContext();

        CharSequence contentTitle = "我的通知栏标展开标题";

        CharSequence contentText = "我的通知栏展开详细内容";

        Intent notificationIntent = new Intent(this, BootStartDemo.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText,contentIntent); 

        //用mNotificationManager的notify方法通知用户生成标题栏消息通知

        mNotificationManager.notify(1, notification);
	}*/
}
