package com.seven.quickphone;


import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seven.adapter.ChatMsgViewAdapter;
import com.seven.bean.*;
import com.seven.data.Constant;
import com.seven.data.Threads;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewSMSActivity extends Activity implements OnClickListener{

	private Button mBtnSend;
	private Button mBtnBack;
	private EditText mEditTextContent,etMess;
	private LinearLayout ll;
	private ListView mListView;
	private ChatMsgViewAdapter mAdapter;
	private MyViewGroup mvg;
	public static NewSMSActivity context=null;
	private SimpleDateFormat sdf= new SimpleDateFormat("MM-dd HH:mm");;
	private List<ChatMsgBean> mDataArrays = new ArrayList<ChatMsgBean>();
	private HashMap<String, Boolean> phoneHash=new HashMap<String, Boolean>();
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_new);
        //启动activity时不自动弹出软键盘
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
        Constant.selected_contactList=new ArrayList<ContactBean>();
        initView();
        context=this;
    }
    
    
    public void updateSelected()
    {
    	for(ContactBean cb:Constant.selected_contactList)
    	{
    		for(String num:cb.getPhoneNum())
    		{
    			phoneHash.put(num, true);
    			Log.v("test", "add:"+num);
    			createView(num);
				autoHeight(mvg.getChildAt(mvg.getChildCount()-1));
    		}
    		
    	}
    }
    public void initView()
    {
    	mListView = (ListView) findViewById(R.id.listview);
    	mAdapter=new ChatMsgViewAdapter(this, mDataArrays);
    	mListView.setAdapter(mAdapter);
		mListView.setSelection(mListView.getCount()-1);
    	mBtnSend = (Button) findViewById(R.id.btn_send);
    	mBtnSend.setOnClickListener(this);
    	mBtnBack = (Button) findViewById(R.id.btn_back);
    	mBtnBack.setOnClickListener(this);
    	mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
    	ll = (LinearLayout) findViewById(R.id.contactSearch);
		/**********************************************************************************************/
		mvg = new MyViewGroup(NewSMSActivity.this);   
		mvg.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 70));
		etMess = new EditText(NewSMSActivity.this);
		etMess.setHint("直接输入号码");
		etMess.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
		etMess.setSelection(etMess.getText().length());
		etMess.setGravity(Gravity.CENTER_VERTICAL);
		etMess.setMinWidth(100);
		etMess.setHeight(60);
		etMess.setTag("edit");
		etMess.getBackground().setAlpha(0);
		etMess.setId(100010);
		etMess.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				if(isNum(s.toString())){
					if(s.length() >= 1){
						boolean bool = false;
						//length() == 15直接生成按钮
						if(s.length() >= 15){
							bool = true;
						}
						//生成Button
						if(bool){
							createView(s.toString());
							phoneHash.put(s.toString(), true);
							Log.v("test", "add:"+s.toString());
							etMess.setText("");
						}
						//检测输入框数据是否已经换行
						final View child = mvg.getChildAt(mvg.getChildCount()-1);
						autoHeight(child);
					}
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			public void afterTextChanged(Editable s) {
			}
		});
		mvg.addView(etMess);
		ll.addView(mvg);
		etMess.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					if(isNum(etMess.getText().toString().trim())&&etMess.getText().toString().trim().length()>0){
						createView(etMess.getText().toString().trim());
						phoneHash.put(etMess.getText().toString().trim(), true);
						Log.v("test", "add:"+etMess.getText().toString().trim());
						etMess.setText("");
					}else{
						etMess.setText("");
					}
				}
			}
		});
		etMess.setOnKeyListener(new View.OnKeyListener() { 
			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent arg2) {
				if(keyCode == KeyEvent.KEYCODE_DEL&&arg2.getAction()==KeyEvent.ACTION_DOWN) {
		
					String phone=(String)mvg.getChildAt(mvg.getChildCount() -2).getTag();
					phoneHash.put(phone, false);
					Log.v("test", "del:"+phone);
					mvg.removeViewAt(mvg.getChildCount() - 2);
					autoHeight(mvg.getChildAt(mvg.getChildCount() -1));
				} 
		        return true; 
			} 
		});
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
		mEditTextContent.setText("");
	    Set<String> addr=new HashSet<String>();
	    Iterator iter = phoneHash.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String)entry.getKey();
			Boolean val = (Boolean)entry.getValue();
			if(val)
			{
				addr.add(key);  
				Log.v("test", "sms::"+key);
			}
		}
	   addSendSMS(Constant.myPhone,s);
       long id=Threads.getOrCreateThreadId(NewSMSActivity.this, addr);//通过这个方法往SMs中插入数据时 threads表也会更新
       sendSMS(addr,s,id);
	}
  public void sendSMS(final Set<String> phone,final String body,final long threadId){
	    final SmsManager msg = SmsManager.getDefault();
	    Intent send = new Intent(MainActivity.SEND_SMS_ACTION);
	    // 短信发送广播
	    final PendingIntent sendPI = PendingIntent.getBroadcast(
                    this, 0, send, 0);
	    Intent delive = new Intent(MainActivity.DELIVERY_SMS_ACTION);
	    // 发送结果广播 
	    final PendingIntent deliverPI = PendingIntent.getBroadcast(
                    this, 0, delive, 0);
	    new Handler() {
		}.postDelayed(new Runnable() {
			public void run() {
				//将数据插入数据库
		        ContentValues cv = new ContentValues();
		        for(String pno:phone )
		        {
		            msg.sendTextMessage(pno, null, body, sendPI, deliverPI);
		            cv.put("thread_id", threadId);
		            cv.put("date", System.currentTimeMillis());
		            cv.put("body", body);
		            cv.put("read", 0);
		            cv.put("type", 2);
		            cv.put("address", pno);
		            getContentResolver().insert(Uri.parse("content://sms/"), cv);
		            
		        }        
		        Constant.main.updateSMS();
			}
		}, 200);
	          
    
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
	private void createView(String num) {

		if(num.trim().equals("")||num==null)
			return ;
		String name=Constant.findNameByPhone(Constant.contactList, num);
		name=name.substring(0, Math.min(6,name.length()));
		TextView t = new TextView(this);
		t.setText(name);
		t.setTextColor(Color.BLACK);
		t.setBackgroundResource(R.drawable.bg_sms_contact_btn);
		t.setGravity(Gravity.CENTER);
		t.setHeight(60);
		t.setPadding(2, 0, 2, 0);
		t.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mvg.removeView(v);
				String phone=(String)v.getTag();
				phoneHash.put(phone, false);
				Log.v("test", "del:"+phone);
				autoHeight(mvg.getChildAt(mvg.getChildCount() -1));
				
			}
		});
		t.setTag(num);
		mvg.addView(t, mvg.getChildCount() - 1);
		
	}
	
	private void autoHeight(final View child) {
		if (child != null){
			new Handler() {
			}.postDelayed(new Runnable() {
				public void run() {
					if (child.getBottom() > mvg.getBottom() || mvg.getBottom() - child.getBottom() >= child.getHeight()) {
						LayoutParams l = mvg.getLayoutParams();
						l.height = child.getBottom();
						mvg.setLayoutParams(l);
					}
				}
			}, 500);
		}
	}

	private boolean isNum(String str){
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}
	public void add_Contact(View v)
	{
		Constant.cmd=2;
    	Intent intent = new Intent (NewSMSActivity.this,ContactManagerActivity.class);			
		startActivity(intent);	
	}
}
class MyViewGroup extends ViewGroup {

	private final static int VIEW_MARGIN = 2;
	private int maxWidth = 0;
	private int maxHeight = 60;

	public MyViewGroup(Context context) {
		super(context);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		for (int index = 0; index < getChildCount(); index++) {
			final View child = getChildAt(index);
			child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {

		final int count = getChildCount();
		int row = 0;// which row lay you view relative to parent
		int lengthX = arg1;    // right position of child relative to parent
		int lengthY = arg2;    // bottom position of child relative to parent
		for(int i = 0 ; i < count ; i++){

			final View child = this.getChildAt(i);
			int width = child.getMeasuredWidth();
			//            int height = child.getMeasuredHeight();
			int height = maxHeight; //限制子节点的高度
			lengthX += width + VIEW_MARGIN;

			lengthY = row * (height + VIEW_MARGIN) + VIEW_MARGIN + height + arg2;
			if(width + VIEW_MARGIN > maxWidth){
				maxWidth = width + VIEW_MARGIN;
			}

			if(lengthX > arg3){
				lengthX = width + VIEW_MARGIN + arg1;
				row ++;
				lengthY = row * (height + VIEW_MARGIN) + VIEW_MARGIN + height + arg2;

			}
			child.layout(lengthX - width, lengthY - height, lengthX, lengthY);
		}
	}

}
