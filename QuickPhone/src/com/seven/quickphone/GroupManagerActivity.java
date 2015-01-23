package com.seven.quickphone;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seven.adapter.CharacterParser;
import com.seven.adapter.ContactManageSortAdapter;
import com.seven.adapter.PinyinComparator;
import com.seven.adapter.SortAdapter;
import com.seven.bean.ContactBean;
import com.seven.bean.GroupBean;
import com.seven.data.Constant;
import com.seven.view.PinnedHeaderListView;
import com.seven.view.SideBar;
import com.seven.view.SideBar.OnTouchingLetterChangedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;  
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class GroupManagerActivity extends Activity {

	private Button mBtnBack;
	private ListView mListView = null;
	private ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
	SimpleAdapter adapter;
	List<GroupBean> list;
	EditText newGroupText;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gourp_manager);
        mBtnBack = (Button) findViewById(R.id.btn_back);
    	mListView = (ListView) findViewById(R.id.group_listview);
        init();
    }
    public void updateView(int position,boolean modify)
    {
    	list=Constant.queryGroup(GroupManagerActivity.this);
    	if(modify)
    	{
    		Map<String, Object> m=mData.get(position);
    	    m.put("title", list.get(position).getName()+" ( "+list.get(position).getCount()+" ) ");
    	    Log.v("test", "ll："+m.get("title"));
    	}else
    		mData.remove(position);
    	adapter.notifyDataSetChanged();
    }
    public void init()
    {
		list=Constant.queryGroup(GroupManagerActivity.this);
		int lengh = list.size();
		for (int i = 0; i < lengh; i++) {
			Map<String, Object> item = new HashMap<String, Object>();
			if(i==lengh-1)
				item.put("title", list.get(i).getName());
			else
				item.put("title", list.get(i).getName()+" ( "+list.get(i).getCount()+" ) ");
			mData.add(item);
		}
		adapter = new SimpleAdapter(this, mData, R.layout.group_manager_listitem,
				new String[] { "title" }, new int[] { R.id.groupName });
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListenerImpl());
		mListView.setOnItemLongClickListener(new OnItemLongClickListenerImpl());
    }
    private class OnItemClickListenerImpl implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.v("test", "position:"+position);
			if(position<list.size()-1)
			{
				GroupBean cg_all=list.get(position);
				Log.v("test", "groupID:"+cg_all.getId());
				Constant.grouped_contactList=Constant.getContactFromGroup(GroupManagerActivity.this, cg_all.getId());
				Log.v("test", "分组大小:"+Constant.grouped_contactList.size());
				Constant.createGroupId=cg_all.getId();
				newGroupActivity();
			}
			//新建分组
			if(position==list.size()-1)
			{
				Log.v("test", "弹出对话框");
				newGroupText=new EditText(GroupManagerActivity.this);
				new AlertDialog.Builder(GroupManagerActivity.this)
				.setTitle("请输入")
				.setView(newGroupText)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface arg0, int arg1) {
						if(!newGroupText.getText().toString().trim().equals(""))
						{
							//创建分组
							ContentValues valuess = new ContentValues();  
					        valuess.put(Groups.TITLE, newGroupText.getText().toString().trim());  
					        Constant.createGroupId=ContentUris.parseId(getContentResolver().insert(Groups.CONTENT_URI, valuess));
					        if(Constant.grouped_contactList==null)
					        	Constant.grouped_contactList=new ArrayList<ContactBean>();
					        else
					        	Constant.grouped_contactList.clear();
					        newGroupActivity();
						}else
						{
							Toast.makeText(getApplicationContext(), "名称不能为空", Toast.LENGTH_LONG).show();
							Log.v("test", "空");		
						}
					}
				})
				.setNegativeButton("取消", null)
				.show();
			}

		}
	}
    private class OnItemLongClickListenerImpl implements OnItemLongClickListener{

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int position, long arg3) {
			if(position<list.size()-1)
				showContactDialog(list.get(position),position);
			return true;
		}
    	
    }
    
	public void newGroupActivity() {  
		finish();
    	Intent intent = new Intent (GroupManagerActivity.this,GroupChangeContactActivity.class);			
		startActivity(intent);	
		
      } 
	public void exit(View v)
	{
		finish();
	}
	//弹出管理
	public void showContactDialog(final GroupBean cb,final int position){
		final String[] arg = new String[] {"管理联系人","修改组名","删除分组" };

		new AlertDialog.Builder(this).setTitle(cb.getName()).setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					GroupBean cg_all=cb;
					Log.v("test", "groupID:"+cg_all.getId());
					Constant.grouped_contactList=Constant.getContactFromGroup(GroupManagerActivity.this, cg_all.getId());
					Log.v("test", "分组大小:"+Constant.grouped_contactList.size());
					Constant.createGroupId=cg_all.getId();
					newGroupActivity();
					break;
				case 1:
					modifyGroup(cb.getId(),position);
					break;
				case 2:
					Constant.delGroup(GroupManagerActivity.this,cb.getId());
					updateView(position, false);
					break;
				}
			}
		}).show();
	}
	
	//修改组名
	public void modifyGroup(final long groupId,final int position)
	{
		Log.v("test", "弹出对话框");
		newGroupText=new EditText(GroupManagerActivity.this);
		new AlertDialog.Builder(GroupManagerActivity.this)
		.setTitle("请输入")
		.setView(newGroupText)
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface arg0, int arg1) {
				if(!newGroupText.getText().toString().trim().equals(""))
				{
					Constant.modifyGroup(GroupManagerActivity.this, groupId, newGroupText.getText().toString().trim());
					updateView(position, true);
				}else
				{
					Toast.makeText(getApplicationContext(), "名称不能为空", Toast.LENGTH_LONG).show();
					Log.v("test", "空");		
				}
			}
		})
		.setNegativeButton("取消", null)
		.show();
	}
   }