package com.example.cj;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class MyDBOpenHelper extends SQLiteOpenHelper {


    public MyDBOpenHelper(Context context) {
        //重写构造方法，创建一个名为DB_ToDoList的数据库
        super(context, "todoDatabase.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //重写onCreate()方法，创建数据表，其中 _id字段作为主键，自动增加
        String sql = "create table tb_ToDoItem(_id integer primary key autoincrement, " +
                "remindTitle text not null, " + //待办事项的标题文本
                "createDate text DEFAULT(' '), " +  //待办事项的创建日期和时间
                "modifyDate text DEFAULT(' '), " +  //最后修改日期和时间
                "remindText text DEFAULT(' '), " +    //待办事项的注释说明
                "remindDate text DEFAULT(' '), " +    //待办事项的提醒日期和时间
                "haveDo boolean DEFAULT(0));";      //是否已处理，默认值：false
        db.execSQL(sql);
        sql = "create table tb_Remind(_id integer primary key autoincrement, " + //主键，自动增加
                "remindID integer, notificationID integer);";      //用于保存状态栏提示Notification的ID
        db.execSQL(sql);
        db.execSQL("insert into tb_Remind(notificationID) values(0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("drop table if exists tb_ToDoItem");
        sqLiteDatabase.execSQL("drop table if exists tb_notificationID");
        onCreate(sqLiteDatabase);
    }
}
