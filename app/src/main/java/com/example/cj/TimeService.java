package com.example.cj;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

public class TimeService extends Service {

    private Timer timer;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer(true);// 创建Timer对象
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String notificationTitle = intent.getStringExtra("title");
        final String notificationText = intent.getStringExtra("text");
        final int notificationID=intent.getIntExtra("notificationID",0);
        Long waitTime=intent.getLongExtra("time",0);
        Log.e("待办小助手--接收到的time",waitTime+"");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e("待办小助手-TimeService的提示", "定时时间到，timer的 run()启动");
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);// 获得通知管理器
                Notification.Builder myBuilder=new Notification.Builder(TimeService.this);
                myBuilder.setSmallIcon(R.mipmap.ic_warning)
                        .setContentTitle(getText(R.string.notification_title)+notificationTitle)   // 定义通知的标题
                        .setContentText(notificationText)    // 定义通知的内容
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND)   //定义默认铃声
                        .setTicker(getText(R.string.notification_ticker));        // 定义一闪而过的提醒文字
                Notification notification=myBuilder.build();     // 创建通知 ，至少:minSdkVersion="16"
                notification.flags = Notification.FLAG_AUTO_CANCEL;   //点击通知后，该通知即消失
                manager.notify(notificationID, notification);// 显示通知，加ID是为了显示多条Notification，每次通知完，通知ID递增一下，避免消息覆盖掉
            }
        }, waitTime);
        return super.onStartCommand(intent, flags, startId);
    }
}


