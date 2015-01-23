package com.seven.quickphone;


import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.seven.adapter.ChatMsgViewAdapter;
import com.seven.bean.ChatMsgBean;
import com.seven.data.Constant;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity implements OnClickListener{

	private Button mBtnSend;
	private Button mBtnBack;
	private EditText mEditTextContent;
	private ListView mListView;
	private TextView title;
	private ChatMsgViewAdapter mAdapter;
	public static SimpleDateFormat sdf= new SimpleDateFormat("MM-dd HH:mm");
	private List<ChatMsgBean> mDataArrays = new ArrayList<ChatMsgBean>();
	private String thread=Constant.currentSMSBean.getThread_id();
	private List<String> address=Constant.currentSMSBean.getAddress();
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms);
        Constant.ca=this;
        //启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
        initView();
        initData();
    }
    
    
    @Override
	protected void onDestroy() {
		Constant.ca=null;
		super.onDestroy();
	}


	public void initView()
    {
    	mListView = (ListView) findViewById(R.id.listview);
    	mBtnSend = (Button) findViewById(R.id.btn_send);
    	mBtnSend.setOnClickListener(this);
    	mBtnBack = (Button) findViewById(R.id.btn_back);
    	mBtnBack.setOnClickListener(this);
    	title=(TextView)findViewById(R.id.title);
    	String name="";
		int len=address.size();
		for(int i=0;i<len-1;i++)
		{
			name+=Constant.findNameByPhone(Constant.contactList,address.get(i))+"|";
		}
		name+=Constant.findNameByPhone(Constant.contactList,address.get(len-1));
		
    	title.setText(name.substring(0, Math.min(8, name.length())));
    	mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
    }
    
    public void initData()
    {
    	
    	thread=Constant.currentSMSBean.getThread_id();
		Uri uri = Uri.parse("content://sms"); 
		String[] projection = new String[] {
				"date",
				"address",
				"person",
				"body",
				"type",
				"_id"
		};// 查询的列
		//Log.v("test", "thread:"+thread);
		new MyAsyncQueryHandler(getContentResolver()).startQuery(
				0, null, uri, projection, "thread_id = " + thread, null,
				"date asc");
		 //Toast.makeText(ChatActivity.this, "测试:"+thread, Toast.LENGTH_LONG).show();
    }

    public void showDetail(View v)
    {
    	//Log.v("test", "showdetail");
    	if(address.size()==1)
    		Constant.showContact(ChatActivity.this, Constant.findContactByPhone(address.get(0)).getContactId());
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_send:
			send();
			break;
		case R.id.btn_back:
			finish();
			break;
		}
	}
	public void send()
	{
		String s=mEditTextContent.getText().toString();
		if(s.equals(""))
		{
			return ;
		}
		if(address.size()==1)
		{
			//插入数据库
			ContentValues values = new ContentValues();
			values.put("address", address.get(0));
			values.put("body", s);
			//addSendSMS(Constant.myPhone,s);
			new MyAsyncQueryHandler(getContentResolver()).startInsert(
					0, null, Uri.parse("content://sms/sent"), values);
			//真正的发送
			Constant.sendMessage(ChatActivity.this, address.get(0), s);
			
		}else if(address.size()>1)
		{
			//群发 循环单发
			//插入数据库群发会话
		}
		mEditTextContent.setText("");
	}
	public void addSendSMS(String address,String text)
	{
		ChatMsgBean c = new ChatMsgBean();
		c.setMsgType(false);
		c.setName(address);
		c.setDate(sdf.format(new Date(System.currentTimeMillis())));
		c.setText(text);
		mDataArrays.add(c);
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(mAdapter.getCount()-1);
	}
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			Constant.main.updateSMS();
			if(Constant.currentSMSBean.getThread_id().equals("")&&address.size()==1)
				Constant.currentSMSBean.setThread_id(
						Constant.getSMSThreadId(ChatActivity.this, Constant.currentSMSBean.getAddress().get(0)));
			initData();
		}


		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			
			if (cursor != null && cursor.getCount() > 0)
			{
				mDataArrays.clear();
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) 
				{
					cursor.moveToPosition(i);
					String date = sdf.format(new Date(cursor.getLong(cursor.getColumnIndex("date"))));
					ChatMsgBean c = new ChatMsgBean();
					if(cursor.getInt(cursor.getColumnIndex("type")) == 1)
						c.setMsgType(true);
					else
						c.setMsgType(false);
					c.setName(Constant.findNameByPhone(Constant.contactList, cursor.getString(cursor.getColumnIndex("address"))));
					c.setDate(date);
					c.setText(cursor.getString(cursor.getColumnIndex("body")));
					c.setId(cursor.getString(cursor.getColumnIndex("_id")));
					mDataArrays.add(c);
				}
				
			}
			if(mAdapter==null)
			{
				// Toast.makeText(ChatActivity.this, "测试:null"+mDataArrays.size(), Toast.LENGTH_LONG).show();
				mAdapter = new ChatMsgViewAdapter(ChatActivity.this, mDataArrays);
				mListView.setAdapter(mAdapter);
				mListView.setSelection(mListView.getCount()-1);
			}else
			{
				// Toast.makeText(ChatActivity.this, "测试:"+mDataArrays.size(), Toast.LENGTH_LONG).show();
				mAdapter.updateView(mDataArrays);
				mListView.setSelection(mListView.getCount()-1);
			}
			
		}
	}
	 
}