package com.example.cj;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TodayList extends Fragment {
    public TodayList() {
    }
    private SQLiteDatabase dbRead;
    private MyDBOpenHelper dbOpenHelper;
    private ListView ListTask;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.today_list, container, false);
        ListTask = (ListView) rootView.findViewById(R.id.listTodayToDo);
        TextView tvToday= (TextView) rootView.findViewById(R.id.tvToday);
        SimpleDateFormat dateFormatter = new SimpleDateFormat ("yyyy年MM月dd日");
        tvToday.setText(dateFormatter.format(new Date(System.currentTimeMillis())));
        dbOpenHelper = new MyDBOpenHelper(getActivity().getApplicationContext());
        dbRead= dbOpenHelper.getReadableDatabase();
        readToDoList();
        return rootView;
    }
    protected void readToDoList(){
        SimpleDateFormat dayFormatter = new SimpleDateFormat ("yyyy-MM-dd");
        ArrayList taskList = new ArrayList<HashMap<String,String>>();
        Cursor result=dbRead.query("tb_ToDoItem",new String[]{
                        "_id","remindTitle","createDate","modifyDate","remindText","remindDate","haveDo"},
                null,null,null,null,"createDate",null);
        while(result.moveToNext()){
            if (result.getString (5).substring(0,10).compareTo(dayFormatter.format(new Date(System.currentTimeMillis())))==0){
                HashMap<String,String> temp = new HashMap<String,String>();
                temp.put("_id", String.valueOf(result.getInt(0)));
                temp.put("remindTitle", result.getString(1));
                temp.put("createDate", "创建时间：" + result.getString(2));
                temp.put("modifyDate", "最后修改时间："+result.getString (3));
                temp.put("remindText", "备注：" + result.getString(4));
                temp.put("remindDate", "时间："+result.getString(5));
                temp.put("haveDo", result.getInt(6)==0?"该事项未处理":"该事项已经处理");
                taskList.add(temp);
            }
        }
        final SimpleAdapter listViewAdapter = new SimpleAdapter(getActivity(), taskList,R.layout.today_list_item,
                new String[] {"remindDate", "remindTitle","remindText","haveDo"},
                new int[]{R.id.remind_listitem_remindDate,R.id.remind_listitem_taskTitle,R.id.remind_listitem_taskText,R.id.remind_listitem_haveDo} );
        ListTask.setAdapter(listViewAdapter);//将查询到的结果显示到ListView控件中
        ListTask.setOnItemClickListener(new AdapterView.OnItemClickListener() {//单击修改列表项
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                HashMap<String, String> temp = (HashMap<String, String>) listViewAdapter.getItem(position);
                final String taskID = temp.get("_id");
                Log.d("待办小助手--我的提示", taskID);
                Cursor result=dbRead.query("tb_ToDoItem",null,
                        "_id=? ",new String[]{taskID},null,null,null,null);
                result.moveToFirst();
                HashMap<String,String> findByID = new HashMap<String,String>();
                findByID.put("remindTitle", "标题："+result.getString (1)+"\n");
                findByID.put("createDate", "创建时间："+result.getString (2)+"\n");
                String ll=result.getString (3).equals(" ")?result.getString (2):result.getString (3);
                findByID.put("modifyDate", "最后修改："+ll+"\n");
                findByID.put("remindText", "备注：" + result.getString(4)+"\n");
                findByID.put("remindDate", "提醒时间："+result.getString (5)+"\n");
                findByID.put("haveDo", result.getInt(6)==0?"该事项未处理":"该事项已经处理");
                new AlertDialog.Builder(getActivity())
                        .setTitle("详细信息")
                        .setMessage(findByID.get("remindTitle")+findByID.get("createDate")+findByID.get("modifyDate")+findByID.get("remindText")+findByID.get("remindDate")+findByID.get("haveDo"))
                        .setNegativeButton("设为已处理", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                SQLiteDatabase dbWriter = dbOpenHelper.getWritableDatabase();
                                ContentValues cv = new ContentValues();
                                cv.put("haveDo",1);
                                dbWriter.update("tb_ToDoItem", cv,"_id=?", new String[]{taskID});
                                dbWriter.close();
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new TodayList())
                                        .commit();
                            }

                        })
                        .setNeutralButton("修改该项内容", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                SQLiteDatabase dbWriter = dbOpenHelper.getWritableDatabase();
                                final Bundle bundle = new Bundle();
                                bundle.putString("taskID", taskID);
                                Update update = new Update();
                                update.setArguments(bundle);
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, update)   //R.id.container是Fragment的容器
                                        .addToBackStack(null) //为了支持回退键
                                        .commit();
                            }

                        })
                        .setPositiveButton("关闭窗口", null)
                        .create()
                        .show();
            }
        });

        ListTask.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){//长按删除列表项

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> temp = (HashMap<String,String>)listViewAdapter.getItem(position);
                final String taskID=temp.get("_id");
                new AlertDialog.Builder(getActivity())
                        .setTitle("警告")
                        .setMessage("您要删除这条待办事项吗?" )
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                SQLiteDatabase dbWriter = dbOpenHelper.getWritableDatabase();
                                dbWriter.delete("tb_ToDoItem", "_id=?", new String[]{taskID});
                                dbWriter.close();
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new TodayList())
                                        .commit();
                            }

                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
                return true;
            }
        });

    }
}
