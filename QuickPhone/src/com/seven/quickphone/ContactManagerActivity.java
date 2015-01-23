package com.seven.quickphone;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.seven.adapter.CharacterParser;
import com.seven.adapter.ContactManageSortAdapter;
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
import android.os.Bundle;
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

public class ContactManagerActivity extends Activity implements OnClickListener{

	private Button mBtnSave;
	private Button mBtnBack;
	private List<ContactBean> list=Constant.contactList;
	TextView title_key;
	LinearLayout titleLayout;
	private PinnedHeaderListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private ContactManageSortAdapter adapter;
	private CharacterParser characterParser;
	private PinyinComparator pinyinComparator;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_manager);
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
    	TextView title=(TextView)findViewById(R.id.cmd_title);
    	if(Constant.cmd==0)
    		title.setText("添加联系人");
    	if(Constant.cmd==1)
    		title.setText("删除联系人");
    	if(Constant.cmd==2)
    		title.setText("选择联系人");
    	if(Constant.cmd==3)
    	{
    		title.setText("添加"+Constant.choosedNum);//用于添加现有联系人
    		mBtnSave.setVisibility(View.GONE);
    	}
    }
    private void initData() {
		//实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
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
				ContactBean cb=adapter.getDatas().get(position);
				if(adapter.getSelectedItems().containsKey(cb.getContactId())&&
						adapter.getSelectedItems().get(cb.getContactId()))
				{
					adapter.getSelectedItems().put(cb.getContactId(),false);
				}
				else
				{
					adapter.getSelectedItems().put(cb.getContactId(),true );
				}
				adapter.notifyDataSetChanged();
				if(Constant.cmd==3)
				{
					Log.v("test", "编辑联系人");
					Constant.editContact(ContactManagerActivity.this, cb);
					finish();
				}
			}
		});
		adapter = new ContactManageSortAdapter(this, list);
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
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_save:
			if(Constant.cmd==0)
				save();
			if(Constant.cmd==1)
				del();
			if(Constant.cmd==2)
				addSMS();
			finish();
			break;
		case R.id.btn_back:
			finish();
			break;
		}
	}
	public void addSMS()
	{
		Log.v("test", "需要增加的人");
		Constant.selected_contactList.clear();
		for(int i=0,len=Constant.contactList.size();i<len;i++)
		{
			if(adapter.getSelectedItems().containsKey(Constant.contactList.get(i).getContactId())&&adapter.getSelectedItems().get(Constant.contactList.get(i).getContactId()))
			{
				Log.v("test", "选中:"+Constant.contactList.get(i).getDisplayName());
				Constant.selected_contactList.add(new ContactBean(Constant.contactList.get(i)));
			}
		}
		if(NewSMSActivity.context!=null)
			NewSMSActivity.context.updateSelected();
	}
	public void del()
	{
		Log.v("test", "开始删除");
		for(int i=0,len=Constant.contactList.size();i<len;i++)
		{
			if(adapter.getSelectedItems().containsKey(Constant.contactList.get(i).getContactId())&&adapter.getSelectedItems().get(Constant.contactList.get(i).getContactId()))
			{
				Constant.delContact(this, Constant.contactList.get(i).getContactId());
			}
		}
	}
	public void save()
	{
		long groupId = Constant.createGroupId;
		ContentValues values = new ContentValues();
		for(int i=0,len=Constant.contactList.size();i<len;i++)
		{
			if(adapter.getSelectedItems().containsKey(Constant.contactList.get(i).getContactId())&&adapter.getSelectedItems().get(Constant.contactList.get(i).getContactId()))
			{
			
				values.clear();
				values.put(Data.RAW_CONTACT_ID,
						Constant.queryForRawContactId(getContentResolver(), Constant.contactList.get(i).getContactId()));
				values.put(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
				values.put(GroupMembership.GROUP_ROW_ID, groupId);
				getContentResolver().insert(Data.CONTENT_URI, values);
			}
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
	@Override
    public boolean onTouchEvent(MotionEvent event) {
    	InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
    	im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    	return super.onTouchEvent(event);
    }
   }