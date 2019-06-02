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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class RemindList extends Fragment {

    public RemindList() {
    }
    private SQLiteDatabase dbRead;
    private MyDBOpenHelper dbOpenHelper;
    private ListView listToDoToday,listToDoTomorrow,listToDoAfterTomorrow,listToDoAfterAll;
    private TextView tvToday,tvTomorrow,tvAfterTomorrow,tvAfterAll;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.remind_list, container, false);
        tvToday=(TextView)rootView.findViewById(R.id.tvToday);
        tvTomorrow=(TextView)rootView.findViewById(R.id.tvTomorrow);
        tvAfterTomorrow=(TextView)rootView.findViewById(R.id.tvAfterTomorrow);
        tvAfterAll=(TextView)rootView.findViewById(R.id.tvAfterAll);

        listToDoToday = (ListView) rootView.findViewById(R.id.listToDoToday);
        listToDoTomorrow = (ListView) rootView.findViewById(R.id.listToDoTomorrow);
        listToDoAfterTomorrow = (ListView) rootView.findViewById(R.id.listToDoAfterTomorrow);
        listToDoAfterAll = (ListView) rootView.findViewById(R.id.listToDoAfterAll);

        SimpleDateFormat dateFormatter = new SimpleDateFormat ("yyyy年MM月dd日");
        tvToday.setText(dateFormatter.format(new Date(System.currentTimeMillis())));//获取今天的日期
        tvTomorrow.setText(dateFormatter.format(new Date(System.currentTimeMillis()+86400000)));//获取明天的日期
        tvAfterTomorrow.setText(dateFormatter.format(new Date(System.currentTimeMillis()+86400000*2)));//获取后天的日期
        tvAfterAll.setText(dateFormatter.format(new Date(System.currentTimeMillis()+86400000*3))+"之后");

        dbOpenHelper = new MyDBOpenHelper(getActivity().getApplicationContext());
        dbRead= dbOpenHelper.getReadableDatabase();  //获得一个只读的SQLiteDatabase对象

        readToDoList(new Date(System.currentTimeMillis()),listToDoToday,0);
        readToDoList(new Date(System.currentTimeMillis()+86400000),listToDoTomorrow,0);
        readToDoList(new Date(System.currentTimeMillis()+86400000*2),listToDoAfterTomorrow,0);
        readToDoList(new Date(System.currentTimeMillis()),listToDoAfterAll,1);
        return rootView;
    }

    protected void readToDoList(Date toDoDay,ListView toDoList,int i){
        SimpleDateFormat dayFormatter = new SimpleDateFormat ("yyyy-MM-dd");
        ArrayList taskList = new ArrayList<HashMap<String,String>>();
        Cursor result=dbRead.query("tb_ToDoItem",new String[]{"_id","remindTitle","remindText","remindDate","haveDo"}, null,null,null,null,"remindDate",null);
        if(i==0){
            while(result.moveToNext()){
                if (result.getString (3).substring(0,10).compareTo(dayFormatter.format(toDoDay))==0){
                    HashMap<String,String> temp = new HashMap<String,String>();
                    temp.put("_id", String.valueOf(result.getInt(0)));
                    temp.put("remindTitle", result.getString (1));
                    temp.put("remindDate", "提醒时间："+result.getString (3).substring(11));
                    temp.put("remindText", "备注：" + result.getString(2));
                    temp.put("taskHaveDo", result.getInt(4)==0?"×未处理":"√已处理");
                    taskList.add(temp);
                }
            }
        }else if(i==1){ //显示三天之后的提醒项
            while(result.moveToNext()){
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date remindDay = null,today=null;
                try {
                    remindDay = dateFormatter.parse(result.getString(3));
                    today=dayFormatter.parse(dayFormatter.format(toDoDay));
                    if(((remindDay.getTime() - today.getTime())/(24*3600*1000)) >=3) { //三天之后
                        HashMap<String,String> temp = new HashMap<String,String>();
                        temp.put("_id", String.valueOf(result.getInt(0)));
                        temp.put("remindTitle", result.getString (1));
                        temp.put("remindDate", "提醒时间："+result.getString (3));
                        temp.put("remindText", "备注：" + result.getString(2));
                        temp.put("taskHaveDo", result.getInt(4)==0?"×未处理":"√已处理");
                        taskList.add(temp);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        final SimpleAdapter listViewAdapter = new SimpleAdapter(getActivity(), taskList,R.layout.remind_list_item, new String[] {"remindDate", "remindTitle","remindText","taskHaveDo"},new int[]{R.id.remind_listitem_remindDate,R.id.remind_listitem_taskTitle,R.id.remind_listitem_taskText,R.id.remind_listitem_haveDo} );
        toDoList.setAdapter(listViewAdapter);
        setListViewHeight(toDoList);
        toDoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                HashMap<String, String> temp = (HashMap<String, String>) listViewAdapter.getItem(position);
                final String taskID = temp.get("_id");    //获取点击的提醒项ID
                Cursor result=dbRead.query("tb_ToDoItem",null,"_id=? ",new String[]{taskID},null,null,null,null);
                result.moveToFirst();
                HashMap<String,String> itemFindByID = new HashMap<String,String>();
                itemFindByID.put("remindTitle", "标题："+result.getString (1)+"\n");
                itemFindByID.put("createDate", "创建时间："+result.getString (2)+"\n");
                String ll=result.getString (3).equals(" ")?result.getString (2):result.getString (3);
                itemFindByID.put("modifyDate", "最后修改："+ll+"\n");
                itemFindByID.put("remindText", "备注：" + result.getString(4)+"\n");
                itemFindByID.put("remindDate", "提醒时间："+result.getString (5)+"\n");
                itemFindByID.put("haveDo", result.getInt(6)==0?"×未处理":"√已处理");
                new AlertDialog.Builder(getActivity())
                        .setTitle("详细信息")
                        .setMessage(itemFindByID.get("remindTitle")+itemFindByID.get("createDate")+itemFindByID.get("modifyDate")+itemFindByID.get("remindText")+itemFindByID.get("remindDate")+itemFindByID.get("haveDo"))
                        .setNegativeButton("设为已处理", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                SQLiteDatabase dbWriter = dbOpenHelper.getWritableDatabase();
                                ContentValues cv = new ContentValues();
                                cv.put("haveDo",1);
                                dbWriter.update("tb_ToDoItem", cv,"_id=?", new String[]{taskID});
                                dbWriter.close();
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new RemindList())
                                        .commit();
                            }

                        })
                        .setNeutralButton("修改该项内容", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                final Bundle bundle = new Bundle();
                                bundle.putString("taskID", taskID);
                                Update updateFragment = new Update();
                                updateFragment.setArguments(bundle);
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, updateFragment)   //R.id.container是Fragment的容器
                                        .addToBackStack(null) //为了支持回退键
                                        .commit();
                            }

                        })
                        .setPositiveButton("关闭窗口", null)
                        .create()
                        .show();
            }
        });

        toDoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){//长按删除列表项

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> temp = (HashMap<String,String>)listViewAdapter.getItem(position);
                final String taskID=temp.get("_id");
                String remindTitle=temp.get("remindTitle");
                Log.d("待办小助手--我的提示（删除）",taskID+"---"+remindTitle+"---");
                new AlertDialog.Builder(getActivity())
                        .setTitle("警告")
                        .setMessage("您要删除这条待办事项吗?"+"\n\n待办事项标题："+ remindTitle)
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                SQLiteDatabase dbWriter = dbOpenHelper.getWritableDatabase();
                                dbWriter.delete("tb_ToDoItem", "_id=?", new String[]{taskID});
                                dbWriter.close();
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new RemindList())
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

    public static void setListViewHeight(ListView listview) {
        int totalHeight = 0;
        ListAdapter adapter= listview.getAdapter();
        if(null != adapter) {
            for (int i = 0; i <adapter.getCount(); i++) {
                View listItem = adapter.getView(i, null, listview);
                if (null != listItem) {
                    listItem.measure(0, 0);//注意listview子项必须为LinearLayout才能调用该方法
                    totalHeight += listItem.getMeasuredHeight();
                }
            }

            ViewGroup.LayoutParams params = listview.getLayoutParams();
            params.height = totalHeight + (listview.getDividerHeight() * (listview.getCount() - 1));
            listview.setLayoutParams(params);
        }
    }

}
