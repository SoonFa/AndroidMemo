package com.example.cj;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
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
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Update extends Fragment {
    public Update() {
    }

    private SQLiteDatabase dbRead;
    private MyDBOpenHelper dbOpenHelper;
    private Button btnUpdate, btnCancel;
    private EditText taskEdit, dateEdit, timeEdit, remarkEdit;
    private TextView taskID;
    private Date remindDate=new Date(System.currentTimeMillis());
    private Calendar newRemindDate=Calendar.getInstance();   //新版本推荐使用Calendar，不用Date

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.update, container, false);
        btnUpdate = (Button) rootView.findViewById(R.id.btnUpdate);
        btnCancel = (Button) rootView.findViewById(R.id.btnUpdateCancel);
        taskID = (TextView) rootView.findViewById(R.id.tvTaskID);
        taskEdit = (EditText) rootView.findViewById(R.id.etUpdateTask);
        dateEdit = (EditText) rootView.findViewById(R.id.etUpdateDate);
        timeEdit = (EditText) rootView.findViewById(R.id.etUpdateTime);
        remarkEdit = (EditText) rootView.findViewById(R.id.etUpdateRemark);

        dbOpenHelper=new MyDBOpenHelper(getActivity().getApplicationContext());
        final String updateID = getArguments().getString("taskID");
        dbRead = dbOpenHelper.getReadableDatabase();
        Cursor result = dbRead.query("tb_ToDoItem", null, "_id=?", new String[]{updateID}, null,null,null,null);
        result.moveToFirst();
        taskEdit.setText(result.getString(1));
        remarkEdit.setText(result.getString(4));

        final SimpleDateFormat dateFormatter = new SimpleDateFormat ("yyyy年MM月dd日");
        final SimpleDateFormat timeFormatter = new SimpleDateFormat ("HH:mm:ss");
        final SimpleDateFormat longDateFormatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        dateEdit.setText(dateFormatter.format(remindDate));
        timeEdit.setText(timeFormatter.format(remindDate));
        remarkEdit.setText(result.getString(4));
        newRemindDate.setTime(remindDate);
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        newRemindDate.set(year,month,day);
                        dateEdit.setText(dateFormatter.format(new Date(newRemindDate.getTimeInMillis())));
                    }
                },newRemindDate.get(Calendar.YEAR),newRemindDate.get(Calendar.MONTH),newRemindDate.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
        timeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(getActivity(),new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        newRemindDate.set(newRemindDate.get(Calendar.YEAR),newRemindDate.get(Calendar.MONTH),newRemindDate.get(Calendar.DAY_OF_MONTH),hourOfDay,minute);
                        timeEdit.setText(timeFormatter.format(new Date(newRemindDate.getTimeInMillis())));
                    }
                },newRemindDate.get(Calendar.HOUR_OF_DAY),newRemindDate.get(Calendar.MINUTE),true)
                        .show();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getFragmentManager().popBackStack();//回退到上一个界面
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SQLiteDatabase dbWriter=dbOpenHelper.getReadableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("remindTitle", taskEdit.getText().toString());
                cv.put("modifyDate",longDateFormatter.format(System.currentTimeMillis()));
                cv.put("remindDate",longDateFormatter.format(newRemindDate.getTimeInMillis()));
                cv.put("remindText",remarkEdit.getText().toString());
                dbWriter.update("tb_ToDoItem", cv, "_id=?", new String[]{updateID});
                dbWriter.close();
                StartNotification.startTimeService(newRemindDate.getTimeInMillis()-System.currentTimeMillis(),
                        taskEdit.getText().toString(),remarkEdit.getText().toString(),getActivity().getApplicationContext());
                getFragmentManager().popBackStack();
            }
        });
        return rootView;
    }

}

