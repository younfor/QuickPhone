package com.seven.quickphone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.seven.bean.ContactBean;
import com.seven.bean.GroupBean;
import com.seven.data.Constant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class GroupDialogActivity extends Activity {
	// private MyDialog dialog;
	private ListView mListView = null;
	private ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
	SimpleAdapter adapter;
	List<GroupBean> list;
	EditText newGroupText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_top_right_dialog);
		mListView = (ListView) findViewById(R.id.main_dialog_listview);
		list=Constant.queryGroup(GroupDialogActivity.this);
		GroupBean gb=new GroupBean();
		gb.setName("全部联系人");
		list.add(0, gb);
		int lengh = list.size();
		for (int i = 0; i < lengh; i++) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("title", list.get(i).getName());
			mData.add(item);
		}
		adapter = new SimpleAdapter(this, mData, R.layout.contact_top_right_list,
				new String[] { "title" }, new int[] { R.id.groupName });
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListenerImpl());
	}

	private class OnItemClickListenerImpl implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(position==0)
			{
				if(MainActivity.instance!=null)
				{
					MainActivity.instance.adapter.updateListView(Constant.contactList);
					Constant.grouped_contactList=Constant.contactList;
				}
			}
			if(position>0&&position<list.size()-1)
			{
				GroupBean cg_all=list.get(position);
				Log.v("test", "groupID:"+cg_all.getId());
				// 查询Data中与该group相关的信息
				String groupSelection = Data.MIMETYPE + " = ?" + " AND "
						+ GroupMembership.GROUP_ROW_ID + " = ?";
				String[] groupSelectionArgs = new String[] {
						GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(cg_all.getId()) };
				Cursor groupCursor = getContentResolver().query(Data.CONTENT_URI, null,
						groupSelection, groupSelectionArgs, null);
				int count = groupCursor.getCount();
				Log.v("test","count:"+count);
				long rawContactId;
				long contactId;
				HashMap<Long, Boolean> hasContact=new HashMap<Long, Boolean>();
				for (int i = 0; i < count; i++) {
					groupCursor.moveToPosition(i);
					rawContactId = groupCursor.getLong(groupCursor
							.getColumnIndex(GroupMembership.RAW_CONTACT_ID));
					contactId = Constant.queryForContactId(getContentResolver(), rawContactId);
					Log.v("test","contactId:"+contactId);
					if(!hasContact.containsKey(contactId))
					{
						Log.v("test","add:"+contactId);
						hasContact.put(contactId, true);
					}
				}
				groupCursor.close();
				ArrayList<ContactBean> grouped_contactList=new ArrayList<ContactBean>();
				for(ContactBean c:Constant.contactList)
				{
					if(hasContact.containsKey(c.getContactId()))
						grouped_contactList.add(c);
					else
						Log.v("test","is?:"+c.getContactId());
				}
				if(MainActivity.instance!=null)
				{
					MainActivity.instance.adapter.updateListView(grouped_contactList);
					Constant.grouped_contactList=grouped_contactList;
				}
				Log.v("test", "分组大小:"+grouped_contactList.size());
			}
			//新建分组
			if(position==list.size()-1)
			{
				newGroupText=new EditText(GroupDialogActivity.this);
				new AlertDialog.Builder(GroupDialogActivity.this)
				.setTitle("请输入")
				.setView(newGroupText)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface arg0, int arg1) {
						if(!newGroupText.getText().toString().trim().equals(""))
						{
							newGroupActivity();
							//创建分组
							ContentValues valuess = new ContentValues();  
					        valuess.put(Groups.TITLE, newGroupText.getText().toString().trim());  
					        Constant.createGroupId=ContentUris.parseId(getContentResolver().insert(Groups.CONTENT_URI, valuess));
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
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}
    public void newGroupActivity() {  
    	Constant.cmd=0;
    	finish();
    	Intent intent = new Intent (GroupDialogActivity.this,ContactManagerActivity.class);			
		startActivity(intent);	
		
      } 
}
