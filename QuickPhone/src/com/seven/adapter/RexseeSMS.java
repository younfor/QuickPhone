package com.seven.adapter;

import java.util.ArrayList;
import java.util.List;

import com.seven.bean.SMSBean;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class RexseeSMS {  

	public static final String CONTENT_URI_SMS = "content://sms";  
	public static final String CONTENT_URI_SMS_INBOX = "content://sms/inbox";  
	public static final String CONTENT_URI_SMS_SENT = "content://sms/sent";  
	public static final String CONTENT_URI_SMS_CONVERSATIONS = "content://sms/conversations";  

	public RexseeSMS(Context mContext) {
		this.mContext=mContext;
	}
	private Context mContext;  
	public List<SMSBean> getThreads() {  
		Cursor cursor = null;  
		ContentResolver contentResolver = mContext.getContentResolver();  
		List<SMSBean> list=new ArrayList<SMSBean>();
		try {  
			
			cursor = contentResolver.query(Uri.parse("content://sms/"), new String[]{"* from threads order by date desc --"}, null, null, null);  
			
			if (cursor == null || cursor.getCount() == 0) 
			{
				Log.v("test", "null");
				return list;  
			}
			Log.v("test", "count:"+cursor.getCount());
			for (int i = 0; i < cursor.getCount(); i++) {  
				cursor.moveToPosition(i);  
				SMSBean mmt=new SMSBean(
			cursor.getString(0),
			cursor.getLong(1),
			cursor.getString(2),
			getCanonicalAddresses(cursor.getString(3)),
			cursor.getString(4),
			cursor.getString(6)
			);
				list.add(mmt);
			}  
			return list;  
		} catch (Exception e) {  
			Log.v("test", "bug");
			return list;  
		}  
	}  
	public List<String> getCanonicalAddresses(String recipient_ids){  
        String[] ids=recipient_ids.split(" ");  
        System.out.println("recipient_ids:"+recipient_ids);  
        List<String> addressResult=new ArrayList<String>();  
        for (int i = 0; i < ids.length; i++) {  
            Cursor cur=mContext.getContentResolver().query(Uri.parse("content://sms/"),   
                    new String[]{" * from canonical_addresses where _id="+ids[i].toString()+" --"}, null,null,null);  
            if(cur.getCount()>0){  
                while (cur.moveToNext()){  
                    String _id=cur.getString(cur.getColumnIndex("_id"));  
                    String address=cur.getString(cur.getColumnIndex("address"));  
                    addressResult.add(address);
                }  
            }  
        }  
        return addressResult;  
    }  
	public static String getUniqueTheadId(Context c,String address)
	{
		String sql="t._id from threads t,canonical_addresses c where c._id=t.recipient_ids "+
             "and c.address='"+address+"' --";
		Cursor cur=c.getContentResolver().query(Uri.parse("content://sms/"),   
                new String[]{sql}, null,null,null);  
		Log.v("test", "start threadid");
		if(cur.getCount()>0)
		{
			cur.moveToNext();
			Log.v("test",cur.getString(0));
			return cur.getString(0);  
		}else
		{
			Log.v("test","blank");
			return "";
		}
        
	}
} 
