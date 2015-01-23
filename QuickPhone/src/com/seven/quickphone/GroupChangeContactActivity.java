package com.seven.quickphone;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.seven.adapter.CharacterParser;
import com.seven.adapter.ContactManageSortAdapter;
import com.seven.adapter.GroupChangeContactAdapter;
import com.seven.adapter.PinyinComparator;
import com.seven.adapter.SortAdapter;
import com.seven.bean.ContactBean;
import com.seven.data.Constant;
import com.seven.view.PinnedHeaderListView;
import com.seven.view.SideBar;
import com.seven.view.SideBar.OnTouchingLetterChangedListener;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;  
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class GroupChangeContactActivity extends Activity implements OnClickListener{

	private Button mBtnSave;
	private Button mBtnBack;
	private Button Isjoined,Nojoined;
	private List<ContactBean> list_out,list_in,list;
	public  static boolean isJoined=true; 
	TextView title_key;
	LinearLayout titleLayout;
	private PinnedHeaderListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private GroupChangeContactAdapter adapter;
	private PinyinComparator pinyinComparator;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gourp_manager_contactlist);
        //启动activity时不自动弹出软键盘 
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
        
        initView();
        initData();
    }
    public void initView()
    {
    	mBtnSave = (Button) findViewById(R.id.btn_save);
    	mBtnSave.setOnClickListener(this);
    	mBtnBack = (Button) findViewById(R.id.btn_back);
    	mBtnBack.setOnClickListener(this);
    	Isjoined = (Button) findViewById(R.id.btn_checked);
    	Isjoined.setOnClickListener(this);
    	Nojoined = (Button) findViewById(R.id.btn_no_checked);
    	Nojoined.setOnClickListener(this);
    }
    private void initData() {
    	//初始数据
    	list_in=new ArrayList<ContactBean>(Constant.grouped_contactList);
    	list_out=new ArrayList<ContactBean>(Constant.contactList);
    	Constant.removeAll(list_out, list_in);
    	list=list_in;
    	//显示人数
    	Isjoined.setText("已加入联系人("+list_in.size()+")");
    	Nojoined.setText("未加入联系人("+list_out.size()+")");
		//实例化汉字转拼音类
		pinyinComparator = new PinyinComparator();
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		//设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			
			@Override
			public void onTouchingLetterChanged(String s) {
				//该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					sortListView.setSelection(position);
				}
				
			}
		});
		sortListView = (PinnedHeaderListView) findViewById(R.id.contact_listview);
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				List<ContactBean> tlist=adapter.getDatas();
				ContactBean temp=tlist.get(position);
			    if(isJoined)
			    {//移除
			    	Constant.remove(list_in, temp);
			    	list_out.add(temp);
			    	Collections.sort(list_out, pinyinComparator);
			    	tlist=list_in;
			    	adapter.updateListView(tlist);
			    }else
			    {//移入
			    	Constant.remove(list_out, temp);
			    	list_in.add(temp);
			    	Collections.sort(list_in, pinyinComparator);
			    	tlist=list_out;
			    	adapter.updateListView(tlist);
			    }
			    //显示人数
		    	Isjoined.setText("已加入联系人("+list_in.size()+")");
		    	Nojoined.setText("未加入联系人("+list_out.size()+")");
			}
		});
		adapter = new GroupChangeContactAdapter(this, list);
		sortListView.setAdapter(adapter);
		sortListView.setOnScrollListener(adapter);
		sortListView.setPinnedHeaderView(getLayoutInflater().inflate(R.layout.main_contact_title, sortListView, false));
		EditText editSearch=(EditText)findViewById(R.id.contactETsearch);
		editSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filterData(s.toString());
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
	}
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.btn_save:
			save();
			finish();
			Intent intent = new Intent (GroupChangeContactActivity.this,GroupManagerActivity.class);			
			startActivity(intent);	
			break;
		case R.id.btn_back:
			finish();
			intent = new Intent (GroupChangeContactActivity.this,GroupManagerActivity.class);			
			startActivity(intent);	
			break;
		case R.id.btn_checked:
			isJoined=true;
			list=list_in;
			adapter.updateListView(list);
			break;
		case R.id.btn_no_checked:
			isJoined=false;
			list=list_out;
			adapter.updateListView(list);
			break;
		}
	}
	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * @param filterStr
	 */
	private void filterData(String filterStr){
		List<ContactBean> filterDateList = new ArrayList<ContactBean>();
		
		if(TextUtils.isEmpty(filterStr)){
			filterDateList = list;
		}else{
			filterDateList.clear();
			for(ContactBean sortModel : list){
				String name = sortModel.getDisplayName();
				String pinyin=sortModel.getPinyin();
				if(name.indexOf(filterStr.toString()) != -1 || pinyin.startsWith(filterStr.toString())){
					filterDateList.add(sortModel);
				}
				else if(Constant.containAB(pinyin.toLowerCase(), filterStr.toString().toLowerCase()))
				{
					filterDateList.add(sortModel);
				}
			}
		}
		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}
	
	public void save()
	{
		// old  12345    new 126
		long groupId = Constant.createGroupId;
		HashSet<Long> same=new HashSet<Long>();
		List<ContactBean> tempDel=new ArrayList<ContactBean>(Constant.grouped_contactList);
		List<ContactBean> tempAdd=new ArrayList<ContactBean>();
		for(ContactBean c1:tempDel)
		{
			for(ContactBean c2:list_in)
			{
				if(c1.getContactId()==c2.getContactId())
					same.add(c1.getContactId());
			}
		}
		//del
		for(ContactBean del:tempDel)
		{
			if(!same.contains(del.getContactId()))
				Constant.delContactFromGroup(this, del.getContactId(), groupId);
		}
		//add
		for(ContactBean add:list_in)
		{
			if(!same.contains(add.getContactId()))
				tempAdd.add(add);
		}
		Constant.addContactToGroup(this, tempAdd, groupId);
	}
	@Override
    public boolean onTouchEvent(MotionEvent event) {
    	InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
    	im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    	return super.onTouchEvent(event);
    }
   }