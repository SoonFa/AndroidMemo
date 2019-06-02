package com.example.cj;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private MyDBOpenHelper dbOpenHelper;
    private Calendar createDate,remindDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbOpenHelper=new MyDBOpenHelper(this);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new RemindList())
                    .commit();
        }
        this.setTitle("备忘录");
    }
    //添加menu菜单
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();  //获得menu容器
        inflater.inflate(R.menu.menu, menu);//用menu.xml填充menu容器
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_add:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Add())
                        .commit();
                return true;
            case R.id.menu_clear:    //删除全部待办事项
                showClearAll();
                return true;
            case R.id.menu_exit: //退出
                showExit();
                return true;
            case R.id.menu_test: //生成测试数据
                showTestData();
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RemindList())
                        .commit();
                return true;
            case R.id.menu_list_todo:     //按待办时间顺序列出全部待办事项
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AllListByToDoTime())
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.menu_list_create:     //按创建时间顺序列出全部待办事项
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AllListByCreateTime())
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.menu_today:  //列出今日提醒
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TodayList())
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.menu_first: //回到首页
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RemindList())
                        .commit();
                return true;
            case R.id.menu_undo: //列出未处理事项
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new UndoList())
                        .commit();
                return true;
            case R.id.menu_done: //列出未处理事项
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new DoneList())
                        .commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearAll(){
        new AlertDialog.Builder(MainActivity.this).setTitle("警告")
                .setMessage("数据删除之后将无法恢复！！\n您确定要删除全部事项吗?")
                .setNeutralButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SQLiteDatabase dbWriter=dbOpenHelper.getWritableDatabase();
                        dbWriter.delete("tb_ToDoItem",null,null);
                        Toast.makeText(getApplicationContext(), "数据已经全部删除！", Toast.LENGTH_SHORT).show();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new RemindList())
                                .commit();
                        dbWriter.close();
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }
    private void showExit(){
        AlertDialog.Builder exitAlert=new AlertDialog.Builder(MainActivity.this);
        exitAlert.setTitle("警告");
        exitAlert.setMessage("您确定要退出吗?");
        exitAlert.setNeutralButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                MainActivity.this.finish();
            }

        });
        exitAlert.setNegativeButton("取消", null);
        exitAlert.create();
        exitAlert.show();
    }
    private void showTestData() {
        MyDBOpenHelper dbOpenHelper=new MyDBOpenHelper(this);
        SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        remindDate=Calendar.getInstance();
        String yesterday=dateFormatter.format(new Date(remindDate.getTimeInMillis()-86400000));//昨天这个时候
        String today=dateFormatter.format(new Date(remindDate.getTimeInMillis()+60000));//今天一分钟后
        String today2=dateFormatter.format(new Date(remindDate.getTimeInMillis()+3600000));//今天今天一小时后
        String tomorrow=dateFormatter.format(new Date(remindDate.getTimeInMillis()+86400000));//明天这个时候
        String aftertomorrow1=dateFormatter.format(new Date(remindDate.getTimeInMillis()+86400000*2));//后天这个时候
        String aftertomorrow2=dateFormatter.format(new Date(remindDate.getTimeInMillis()+86400000*3));//大后天这个时候

        String sql_insert="insert into tb_ToDoItem(createDate,remindTitle,remindText,remindDate) " +
                "values ('"+yesterday+"','开会','评选优秀员工','"+today+"');";
        db.execSQL(sql_insert);
        StartNotification.startTimeService(new Long(60000),"下午2点开会","会议议题,评选优秀员工",this);

        sql_insert="insert into tb_ToDoItem(createDate,remindTitle,remindText,remindDate) " +
                "values ('"+yesterday+"','晚上吃饭','火锅','"+today2+"');";
        db.execSQL(sql_insert);
        StartNotification.startTimeService(new Long(1000*60*60),"晚上吃饭","火锅",this);

        sql_insert="insert into tb_ToDoItem(createDate,remindTitle,remindText,remindDate) " +
                "values ('"+yesterday+"','实验','操作系统','"+tomorrow+"');";
        db.execSQL(sql_insert);
        StartNotification.startTimeService(new Long(1000*60*60),"实验","操作系统",this);

        sql_insert="insert into tb_ToDoItem(createDate,remindTitle,remindText,remindDate) " +
                "values ('"+yesterday+"','画画','水彩','"+aftertomorrow1+"');";
        db.execSQL(sql_insert);
        StartNotification.startTimeService(new Long(1000*60*60),"画画","水彩",this);
        sql_insert="insert into tb_ToDoItem(createDate,remindTitle,remindText,remindDate) " +
                "values ('"+yesterday+"','实验报告','嵌入式计算器','"+aftertomorrow2+"');";
        db.execSQL(sql_insert);
        StartNotification.startTimeService(new Long(1000*60*60),"实验报告","计算器",this);
    }

}
