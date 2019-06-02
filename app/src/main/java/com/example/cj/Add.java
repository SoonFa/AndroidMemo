package com.example.cj;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Add extends Fragment {
    public Add(){}
    private MyDBOpenHelper dbOpenHelper;
    private Button btnAdd, btnCancel;
    private EditText remindTitleEdit, dateEdit, timeEdit, remindTextEdit;
    private Calendar createDate,remindDate;
    private SimpleDateFormat dateFormatter,timeFormatter;
    private int notificationID=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add, container, false);
        btnAdd = (Button) rootView.findViewById(R.id.btnAdd);
        btnCancel = (Button) rootView.findViewById(R.id.btnAddCancel);
        // taskID = (TextView) rootView.findViewById(R.id.tvTaskID);
        remindTitleEdit = (EditText) rootView.findViewById(R.id.etAddTask);
        dateEdit = (EditText) rootView.findViewById(R.id.etAddDate);
        timeEdit = (EditText) rootView.findViewById(R.id.etAddTime);
        remindTextEdit = (EditText) rootView.findViewById(R.id.etAddRemark);

        dbOpenHelper=new MyDBOpenHelper(getActivity().getApplicationContext());
        //taskID.setText("ID: "+"0088");

        dateFormatter = new SimpleDateFormat ("yyyy年MM月dd日");
        timeFormatter = new SimpleDateFormat ("HH:mm:ss");
        createDate=Calendar.getInstance();
        createDate.setTimeInMillis(System.currentTimeMillis());
        remindDate=Calendar.getInstance();//新版本推荐使用Calendar，不用Date
        remindDate.setTimeInMillis(remindDate.getTimeInMillis()+1000*60);
        dateEdit.setText(dateFormatter.format(new Date(remindDate.getTimeInMillis())));
        timeEdit.setText(timeFormatter.format(new Date(remindDate.getTimeInMillis())));

        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        remindDate.set(year,month,day);
                        dateEdit.setText(dateFormatter.format(new Date(remindDate.getTimeInMillis())));
                    }
                },remindDate.get(Calendar.YEAR),remindDate.get(Calendar.MONTH),remindDate.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        timeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(getActivity(),new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        remindDate.set(remindDate.get(Calendar.YEAR),remindDate.get(Calendar.MONTH),remindDate.get(Calendar.DAY_OF_MONTH),hourOfDay,minute);
                        timeEdit.setText(timeFormatter.format(new Date(remindDate.getTimeInMillis())));
                        Log.e("待办事项-设置提醒时间",remindDate.getTime().toString());
                    }
                },remindDate.get(Calendar.HOUR_OF_DAY),remindDate.get(Calendar.MINUTE),true)
                        .show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RemindList())
                        .commit();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 从编辑框中获得相应的属性值
                SimpleDateFormat longDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SQLiteDatabase dbWriter=dbOpenHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("remindTitle", remindTitleEdit.getText().toString());
                cv.put("createDate",longDateFormatter.format(new Date(System.currentTimeMillis())));
                cv.put("modifyDate",longDateFormatter.format(new Date(System.currentTimeMillis())));
                cv.put("remindDate",longDateFormatter.format(remindDate.getTimeInMillis()));
                cv.put("remindText",remindTextEdit.getText().toString());

                dbWriter.insert("tb_ToDoItem",null, cv);
                dbWriter.close();
//                StartNotification.startTimeService(remindDate.getTimeInMillis()-System.currentTimeMillis(),
//                        remindTitleEdit.getText().toString(),remindTextEdit.getText().toString(),getActivity().getApplicationContext());
                startTimeService(remindDate.getTimeInMillis()-System.currentTimeMillis(),
                        remindTitleEdit.getText().toString(),remindTextEdit.getText().toString());

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RemindList())
                        .commit();

            }
        });


        return rootView;
    }

    private void startTimeService(Long time,String title,String text){
        int notificationID;
        SQLiteDatabase dbRead=(new MyDBOpenHelper(getActivity().getApplicationContext())).getReadableDatabase();
        Intent intent=new Intent(getActivity().getApplicationContext(),TimeService.class);
        Cursor result=dbRead.query("tb_Remind",new String[]{"notificationID"},null,null,null,null,null,null);
        if (result.moveToFirst()){
            notificationID=result.getInt(0);
            //Log.e("待办小助手-AddNewFragment的提示","-读取的notificationID--"+notificationID);
            SQLiteDatabase dbWriter=(new MyDBOpenHelper(getActivity().getApplicationContext())).getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("notificationID", notificationID+1);
            dbWriter.update("tb_Remind", cv,null, null);  //为了显示多条Notification，每次通知完，通知ID递增一下，避免消息覆盖掉
            dbWriter.close();
        }else {
            notificationID=0;
            Log.e("待办小助手-AddNewFragment的提示", "错误：无法获取数据库中的notificationID值！！");
        }
        dbRead.close();
        intent.putExtra("time", time);
        intent.putExtra("title",title);
        intent.putExtra("text",text);
        intent.putExtra("notificationID",notificationID);    //传递参数
        getActivity().startService(intent);  //启动Service
    }

}

