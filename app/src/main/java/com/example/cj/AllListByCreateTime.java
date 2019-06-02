package com.example.cj;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class AllListByCreateTime extends Fragment {

    public AllListByCreateTime() {
    }

    private SQLiteDatabase dbRead;
    private MyDBOpenHelper dbOpenHelper;
    private ListView ListTask;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.all_list, container, false);
        ListTask = rootView.findViewById(R.id.listAllToDo);
        dbOpenHelper = new MyDBOpenHelper(getActivity().getApplicationContext());
        dbRead= dbOpenHelper.getReadableDatabase();
        readToDoList();
        return rootView;
    }
    protected void readToDoList(){
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日 HH:mm:ss ");
        ArrayList taskList = new ArrayList<HashMap<String,String>>();
        Cursor result=dbRead.query("tb_ToDoItem",new String[]{"_id","remindTitle","createDate","modifyDate","remindText","remindDate","haveDo"}, null,null,null,null,"createDate",null);
        if (result.getCount() == 0 ){
            Toast.makeText(getActivity().getApplicationContext(),"数据库中无数据！", Toast.LENGTH_SHORT).show();
            return;
        }else {
            while(result.moveToNext()){
                HashMap<String,String> temp = new HashMap<String,String>();
                temp.put("_id", String.valueOf(result.getInt(0)));
                temp.put("remindTitle", result.getString (1));
                temp.put("createDate", "创建时间："+result.getString (2));
                temp.put("modifyDate", "最后修改时间："+result.getString (3));
                temp.put("remindText", "备注：" + result.getString(4));
                temp.put("remindDate", "设定的办理时间："+result.getString (5));
                temp.put("haveDo", result.getInt(6)==0?"×未处理":"√已处理");
                taskList.add(temp);
            }
        }

        final SimpleAdapter listViewAdapter = new SimpleAdapter(getActivity(), taskList,R.layout.all_list_item,
                new String[] {"remindTitle","createDate", "remindDate","haveDo","remindText"},
                new int[]{R.id.listitem_task,R.id.listitem_createDate,R.id.listitem_remindDate,R.id.listitem_haveDo,R.id.listitem_remark} );
        ListTask.setAdapter(listViewAdapter);//将查询到的结果显示到ListView控件中
        ListTask.setOnItemClickListener(new AdapterView.OnItemClickListener() {//单击修改列表项
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
                String remindTitle=temp.get("remindTitle");
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

        ListTask.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){//长按删除列表项

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final View itemView=view;
                final int itemPosition=position;
                HashMap<String,String> temp = (HashMap<String,String>)listViewAdapter.getItem(position);
                final String taskID=temp.get("_id");
                new AlertDialog.Builder(getActivity())
                        .setTitle("警告")
                        .setMessage("您要删除这条待办事项吗?" )
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                SQLiteDatabase dbWriter = dbOpenHelper.getWritableDatabase();
                                dbWriter.delete("tb_ToDoItem", "_id=?", new String[]{taskID});
                                readToDoList();
                            }

                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        })
                        .create()
                        .show();
                return true;
            }
        });
    }
}



