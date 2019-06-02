package com.example.cj;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class StartNotification {

    public static void startTimeService(Long time,String title,String text,Context context){
        int notificationID;
        SQLiteDatabase dbRead=(new MyDBOpenHelper(context)).getReadableDatabase();
        Intent intent=new Intent(context,TimeService.class);
        Cursor result=dbRead.query("tb_Remind",new String[]{"notificationID"},null,null,null,null,null,null);
        if (result.moveToFirst()){
            notificationID=result.getInt(0);
            SQLiteDatabase dbWriter=(new MyDBOpenHelper(context)).getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("notificationID", notificationID+1);
            dbWriter.update("tb_Remind", cv,null, null);
            dbWriter.close();
        }else {
            notificationID=0;
        }
        dbRead.close();
        intent.putExtra("time", time);
        intent.putExtra("title",title);
        intent.putExtra("text",text);
        intent.putExtra("notificationID",notificationID);
        context.startService(intent);
    }


}
