package com.seven.data;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.seven.adapter.CallLogAdapter;
import com.seven.adapter.RexseeSMS;
import com.seven.bean.CallLogBean;
import com.seven.bean.ContactBean;
import com.seven.bean.GroupBean;
import com.seven.bean.SMSBean;
import com.seven.quickphone.ChatActivity;
import com.seven.quickphone.MainActivity;
import com.seven.quickphone.R;

public class Constant {

	public static List<ContactBean> contactList=null,grouped_contactList=null;
    public static List<ContactBean>	selected_contactList=null;
	public static long createGroupId=-1; 
	public static boolean joined=true;
	public static boolean isPullState=false;
	public static int cmd=0;// 0 联系人分组  1删除联系人
	public static int isDel=0;//0不显示删除按钮  1显示删除按钮
	public static String choosedNum;
	public static SMSBean currentSMSBean=new SMSBean();
	public static String myPhone="我",myPhoneNUM="10010";
	public static String preDate=null;
	public static MainActivity main=null;
	public static ChatActivity ca=null;
	public static class ViewHolder {
		public TextView tvKey;
		public TextView tvTitle;
		public TextView tvPhone;
		public ImageView tvCall,tvSMS,tvDetail,headId;
		public LinearLayout sortKeyLayout,moreLayout,nameLayout;
	}
	/**
	 * 得到头像的Bitmap
	 * @param c
	 * @param contactId
	 * @return
	 */
	public static Bitmap getHeadBitMap(ContentResolver c,Long contactId)
	{
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
		InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(c, uri); 
		return BitmapFactory.decodeStream(input);
	}
	/**
	 * 清空通话记录
	 */
	public static void clearCallLog(ContentResolver cr)
	{
		cr.delete(CallLog.Calls.CONTENT_URI, null, null);  
		main.updateCallLog();

	}
	/**
	 * 初始化通话记录
	 * @param cr
	 * @param list
	 */
	public static void initCallLog(ContentResolver cr)
	{
		
		Uri uri = CallLog.Calls.CONTENT_URI;
		String[] projection = { 
				CallLog.Calls.DATE,
				CallLog.Calls.NUMBER,
				CallLog.Calls.TYPE,
				CallLog.Calls.CACHED_NAME,
				CallLog.Calls._ID
		}; // 查询的列
		(new CallLogAsyncQueryHandler(cr)).startQuery(0, null, uri, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);  
	
	}
	private static class CallLogAsyncQueryHandler extends AsyncQueryHandler {

		public CallLogAsyncQueryHandler(ContentResolver cr) {
			super(cr);
			if(main.callLoglist==null)
			{
				main.callLoglist = new ArrayList<CallLogBean>();
				Log.v("test", "null list");
			}
			else
			{
				main.callLoglist.clear();
				Log.v("test", "clear list");
			}
			
		}
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			
			if (cursor != null && cursor.getCount() > 0) {
				Log.v("test", "cursor>0");
				SimpleDateFormat sfd = new SimpleDateFormat("MM-dd hh:mm");
				Date date;
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					date = new Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
					String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
					int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
					String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));//缓存的名称与电话号码，如果它的存在
					int id = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID));
					CallLogBean clb = new CallLogBean();
					clb.setId(id);
					clb.setNumber(number);
					clb.setName(cachedName);
					if(null == cachedName || "".equals(cachedName)){
						clb.setName(number);
					}
					clb.setType(type);
					clb.setDate(sfd.format(date));
					main.callLoglist.add(clb);
				}
			}
			if(main.callLogadapter==null)
			{
				Log.v("test", "init log");
				main.callLogadapter=new CallLogAdapter(main, main.callLoglist);
				main.callLogListview.setAdapter(main.callLogadapter);
			}else
			{
				Log.v("test", "notice fy list");
				main.callLogadapter.notifyDataSetChanged();
			}
		}

	}
	/**
	 * a是否包含b
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean containAB(String a,String b)
	{
		int i=0,len1=a.length(),j=0,len2=b.length();
		// a=chenqi, b=cq   
		while(i<len1 && j<len2)
		{
			if(a.charAt(i)==b.charAt(j)){i++;j++;}
			else i++;
		}
		if(j==len2)
			return true;
		else
			return false;
	}
	/**
	 * 打电话
	 * @param c
	 * @param number
	 */
	public static void callContact(Context c,String number)
	{
	   if(!number.equals(""))
	   {
		   Intent phoneIntent = new Intent("android.intent.action.CALL",
		   Uri.parse("tel:" + number));
	       c.startActivity(phoneIntent);
	   }
	}
	/**
	 * 查询会话ID
	 * @param c
	 * @param adddress
	 * @return
	 */
	public static String getSMSThreadId(Context c,String adddress){
		return RexseeSMS.getUniqueTheadId(c, adddress);
	}
	/**
	 * 改变短信未读状态
	 * @param c
	 * @param thread_id
	 */
	public static void setReadStage(Context c,String thread_id)
	{
		 ContentValues values = new ContentValues();  
         values.put("read", "1");        //修改短信为已读模式  
		 c.getContentResolver().update(Uri.parse("content://sms/"), values, "thread_id=?", new String[]{thread_id});
	}
	/**
	 * 删除短信会话
	 * @param c
	 * @param thread_id
	 */
	public static void delSMS(Context c,String thread_id)
	{
	     String uri = "content://sms/conversations/" + thread_id;
	     c.getContentResolver().delete(Uri.parse(uri), null, null);   
	}
	/**
	 * 发短信
	 * @param c
	 * @param number
	 * @param threadId
	 */
	public static void smsContact(Context c,String number,String threadId)
	{
		SMSBean sb=new SMSBean();
		List<String> num=new ArrayList<String>();
		num.add(number);
		sb.setAddress(num);
		sb.setThread_id(getSMSThreadId( c,number));
		Constant.currentSMSBean=sb;
		Intent intent = new Intent (c,ChatActivity.class);			
		c.startActivity(intent);	
	}
	public static void sendMessage(Context c,String number,String text)
	{
		SmsManager sms = SmsManager.getDefault();
        Intent sendIntent = new Intent(MainActivity.SEND_SMS_ACTION);
        PendingIntent sendPI = PendingIntent.getBroadcast(c, 0, sendIntent, 0);
        Intent deliveryIntent = new Intent(MainActivity.DELIVERY_SMS_ACTION);
        PendingIntent deliveryPI = PendingIntent.getBroadcast(c, 0, deliveryIntent, 0);
        sms.sendTextMessage(number, null, text, sendPI, deliveryPI);
	}
	/**
	 * 编辑联系人
	 */
	public static void editContact(Context c,ContactBean cb)
	{
		Long rawId=queryForRawContactId(c.getContentResolver(), cb.getContactId());
		Intent intent = new Intent(Intent.ACTION_EDIT, Uri
                 .withAppendedPath(
                 ContactsContract.RawContacts.CONTENT_URI,
                 rawId+""));
		if(Constant.cmd==3)
		{
			intent.putExtra(ContactsContract.Intents.Insert.PHONE, Constant.choosedNum);
			intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,cb.getPhoneNum().size()+1);
		}
		c.startActivity(intent);
	}
	/**
	 * 联系人详情
	 * @param c
	 * @param contactId
	 */
	public static void showContact(Context c,Long contactId)
	{
		if(contactId==null)
			return;
		Intent it=new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId));
		c.startActivity(it);
	}
	/**
	 * 添加联系人
	 * @param c
	 * @param m
	 */
	public static void showNewContact(Context c,String num)
	{
		Intent it = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
    	//it.putExtra(ContactsContract.Intents.Insert.NAME, "zhangsan");
    	it.putExtra(ContactsContract.Intents.Insert.PHONE, num);
    	it.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,1);
    	c.startActivity(it);
	}
	/**
	 * 删除联系人
	 * @param c
	 * @param contactId
	 */
	public static void delContact(Context c,long contactId)
	{
		String where = ContactsContract.Data._ID  + " =?";
		String[] whereparams = new String[]{String.valueOf(contactId)};
		c.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, where, whereparams);
	}
	/**
	 * 查询所有分组
	 */
	public static List<GroupBean> queryGroup(Context c){

		List<GroupBean> list=new ArrayList<GroupBean>();
		GroupBean cg_all;
		Cursor cur = c.getContentResolver().query(Groups.CONTENT_URI, null, Groups.DELETED + " = 0", null, null); 
		while (cur.moveToNext()) { 
			if(null!=cur.getString(cur.getColumnIndex(Groups.TITLE))&&(!"".equals(cur.getString(cur.getColumnIndex(Groups.TITLE))))){
				GroupBean cg=new GroupBean();
				cg.setId(cur.getInt(cur.getColumnIndex(Groups._ID)));
				Cursor groupCursor = c.getContentResolver().query(Data.CONTENT_URI, null,
						Data.MIMETYPE + " = ?" + " AND "+ GroupMembership.GROUP_ROW_ID + " = ?", 
						new String[] {GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(cg.getId())}, null);
				cg.setCount(groupCursor.getCount());
				cg.setName(cur.getString(cur.getColumnIndex(Groups.TITLE)));
				list.add(cg);
			}
		}   
		cg_all=new GroupBean();
		cg_all.setId(-1);
		cg_all.setName("新建分组");
		list.add(cg_all);
		cur.close();
		return list;
	}
	/**
	 * 删除分组
	 */
	public static void delGroup(Context c,long groupId)
	{
		// 真正意义上的删除  
		Uri uri = ContentUris.withAppendedId(ContactsContract.Groups.CONTENT_URI, groupId);  
		Uri.Builder b = uri.buildUpon();  
		b.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true");  
		uri = b.build();  
		c.getContentResolver().delete(uri, null, null);  
	}
	/**
	 * 修改分组名字
	 */
	public static void modifyGroup(Context c,long groupId,String title)
	{
		ContentValues values = new ContentValues();  
		values.put(ContactsContract.Groups.TITLE, title);  
		c.getContentResolver().update(ContactsContract.Groups.CONTENT_URI,   
		    values,   
		    ContactsContract.Groups._ID + " = " + groupId,   
		    null);  
	}
	/**
	 * 得到指定分组的联系人
	 */
	public static List<ContactBean> getContactFromGroup(Context c,long groupId)
	{
		// 查询Data中与该group相关的信息
		String groupSelection = Data.MIMETYPE + " = ?" + " AND "
				+ GroupMembership.GROUP_ROW_ID + " = ?";
		String[] groupSelectionArgs = new String[] {
				GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId) };
		Cursor groupCursor = c.getContentResolver().query(Data.CONTENT_URI, null,
				groupSelection, groupSelectionArgs, null);
		int count = groupCursor.getCount();
		long rawContactId;
		long contactId;
		HashMap<Long, Boolean> hasContact=new HashMap<Long, Boolean>();
		for (int i = 0; i < count; i++) {
			groupCursor.moveToPosition(i);
			rawContactId = groupCursor.getLong(groupCursor
					.getColumnIndex(GroupMembership.RAW_CONTACT_ID));
			contactId = queryForContactId(c.getContentResolver(), rawContactId);
			if(!hasContact.containsKey(contactId))
			{
				hasContact.put(contactId, true);
			}
		}
		groupCursor.close();
		ArrayList<ContactBean> gcontactList=new ArrayList<ContactBean>();
		for(ContactBean cb:Constant.contactList)
		{
			if(hasContact.containsKey(cb.getContactId()))
				gcontactList.add(new ContactBean(cb));
		}
		return gcontactList;
	}
	/**
	 * 向分组批量添加联系人
	 */
	public static void addContactToGroup(Context c,List<ContactBean> list,long groupId)
	{
		ContentValues values = new ContentValues();
		for(int i=0,len=list.size();i<len;i++)
		{
			values.clear();
			values.put(Data.RAW_CONTACT_ID,
					queryForRawContactId(c.getContentResolver(), list.get(i).getContactId()));
			values.put(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
			values.put(GroupMembership.GROUP_ROW_ID, groupId);
			c.getContentResolver().insert(Data.CONTENT_URI, values);
		}
	}
	/**
	 * 从分组中删除联系人
	 */
	public static void delContactFromGroup(Context c,long contactId,long groupId)
	{
		Uri.Builder b = Data.CONTENT_URI.buildUpon();
		b.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true");
		c.getContentResolver().delete(b.build(), 
				Data.RAW_CONTACT_ID + "=" + queryForRawContactId(c.getContentResolver(), contactId) + " AND " +
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " =" 
				+ groupId, null);
	}
	/**
	 * 查询RawContacts中_id等于rawContactId的记录的contact_id字段的值
	 */
	public static long queryForContactId(ContentResolver cr, long rawContactId) {
		Cursor contactIdCursor = null;
		long contactId = -1;
		try {
			contactIdCursor = cr.query(RawContacts.CONTENT_URI,
					new String[] { RawContacts.CONTACT_ID }, RawContacts._ID
							+ "=" + rawContactId, null, null);
			if (contactIdCursor != null && contactIdCursor.moveToFirst()) {
				contactId = contactIdCursor.getLong(0);
			}
		} finally {
			if (contactIdCursor != null) {
				contactIdCursor.close();
			}
		}
		return contactId;
	}
	/**
	* @return 根据参数中的contactId,查询在raw_contacts表中谁属于参数中的contactId
	*/
	public static long queryForRawContactId(ContentResolver cr, long contactId) {
		Cursor rawContactIdCursor = null;
		long rawContactId = -1;
		try {
			rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
					new String[] { RawContacts._ID }, RawContacts.CONTACT_ID
							+ "=" + contactId, null, null);
			if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
				// Just return the first one.
				rawContactId = rawContactIdCursor.getLong(0);
			}
		} finally {
			if (rawContactIdCursor != null) {
				rawContactIdCursor.close();
			}
		}
		return rawContactId;
	}
	public static void remove(List<ContactBean> a,ContactBean b)
	{
		ContactBean r=null;
		for(ContactBean c:a)
		{
			if(c.getContactId()==b.getContactId())
				r=c;
		}
		if(r!=null)
			a.remove(r);
	}
	public static void removeAll(List<ContactBean> a,List<ContactBean> b)
	{
		List<ContactBean> temp=new ArrayList<ContactBean>();
		HashMap<Long, Boolean> h2=new HashMap<Long, Boolean>();
		for(ContactBean c2:b)
		{
			if(!h2.containsKey(c2.getContactId()))
				h2.put(c2.getContactId(), true);
		}
		for(ContactBean c3:a)
		{
			if(h2.containsKey(c3.getContactId()))
				temp.add(c3);
		}
		a.removeAll(temp);
	}
	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * @param filterStr
	 */
	public static List<ContactBean> filterData(String filterStr){
		List<ContactBean> filterDateList = new ArrayList<ContactBean>();
		List<ContactBean> list=contactList;
		boolean isContain=false;
		if(TextUtils.isEmpty(filterStr)){
			filterDateList = list;
		}else{
			filterDateList.clear();
			for(ContactBean sortModel : list){
				List<String> num = sortModel.getPhoneNum();
				String pinyin=sortModel.getPinyin();
				isContain=false;
				for(int i=0,len=num.size();i<len;i++)
				{
					if(num.get(i).indexOf(filterStr)!=-1)
					{
						filterDateList.add(sortModel);
						isContain=true;
					}
				}
				if(!isContain)
				{
					if( Constant.containAB(sortModel.getFormattedNumber(), filterStr))
					{
						filterDateList.add(sortModel);
					}
				}
			}
		}
		return filterDateList;
		// 根据a-z进行排序
		//Collections.sort(filterDateList, pinyinComparator);
		//adapter.updateListView(filterDateList);
	}
	
	public static char[] digit(char digit) {
			char[] cs = null;
			switch (digit) {
			case '0':
				cs = new char[] {'#','#','#'};
				break;
			case '1':
				break;
			case '2':
				cs = new char[] { 'a', 'b', 'c' };
				break;
			case '3':
				cs = new char[] { 'd', 'e', 'f' };
				break;
			case '4':
				cs = new char[] { 'g', 'h', 'i' };
				break;
			case '5':
				cs = new char[] { 'j', 'k', 'l' };
				break;
			case '6':
				cs = new char[] { 'm', 'n', 'o' };
				break;
			case '7':
				cs = new char[] { 'p', 'q', 'r', 's' };
				break;
			case '8':
				cs = new char[] { 't', 'u', 'v' };
				break;
			case '9':
				cs = new char[] { 'w', 'x', 'y', 'z' };
				break;
			}
			return cs;
	}
	/**
	 * T9算法
	 * @param name
	 * @return
	 */
	public static String getNameNum(String name) {
		if (name != null && name.length() != 0) {
			int len = name.length();
			char[] nums = new char[len];
			for (int i = 0; i < len; i++)
			{
				nums[i] = getOneNumFromAlpha(name.toLowerCase().charAt(i));
			}
			return new String(nums);
		}
		return "";
	}

	/**
	 * T9算法
	 * @param firstAlpha
	 * @return
	 */
	private static char getOneNumFromAlpha(char firstAlpha) {
		switch (firstAlpha) {
		case 'a':
		case 'b':
		case 'c':
			return '2';
		case 'd':
		case 'e':
		case 'f':
			return '3';
		case 'g':
		case 'h':
		case 'i':
			return '4';
		case 'j':
		case 'k':
		case 'l':
			return '5';
		case 'm':
		case 'n':
		case 'o':
			return '6';
		case 'p':
		case 'q':
		case 'r':
		case 's':
			return '7';
		case 't':
		case 'u':
		case 'v':
			return '8';
		case 'w':
		case 'x':
		case 'y':
		case 'z':
			return '9';
		default:
			return firstAlpha;
		}
	}
	/**	去掉+86和-
	 * 
	 */
	public static String filterNum(String s)
	{
		String r=s.substring(s.indexOf("+86")>-1?s.indexOf("+86")+3:0);
		return r.replace("-", " ").trim();
	}
	public static ContactBean findContactByPhone(String phone)
	{
		for(ContactBean c:Constant.contactList)
		{
			
			for(String s:c.getPhoneNum())
			{
				if(filterNum(phone).equals(filterNum(s)))
					return c;
					
			}
		}
		ContactBean cb=new ContactBean();
		cb.setPhotoId(Long.parseLong("0"));
		return cb;
	}
	/**
	 * 通过电话找到Id
	 * @param list
	 * @param phone
	 * @return
	 */
	public static Long findContactIdByPhone(List<ContactBean>  list,String phone)
	{
		String name="";
		for(ContactBean c:list)
		{
			
			for(String s:c.getPhoneNum())
			{
				//Log.v("test", s+":"+phone);
				if(filterNum(phone).equals(filterNum(s)))
					return c.getContactId();
					
			}
		}
		return null;
	}
	/**
	 * 通过电话号码获得HeadId
	 *
	 */
	public static Long findHeadIdByPhone(List<ContactBean>  list,String phone)
	{
		for(ContactBean c:list)
		{
			
			for(String s:c.getPhoneNum())
			{
				//Log.v("test", s+":"+phone);
				if(filterNum(phone).equals(filterNum(s)))
					return c.getPhotoId();
					
			}
		}
		return null;
	}
	/**
	 * 通过电话号码获得名字
	 * @param list
	 * @param phone
	 * @return
	 */
	public static String findNameByPhone(List<ContactBean>  list,String phone)
	{
		if(phone==null) return "";
		String name="";
		for(ContactBean c:list)
		{
			
			for(String s:c.getPhoneNum())
			{
				//Log.v("test", s+":"+phone);
				if(filterNum(phone).equals(filterNum(s)))
					name+=c.getDisplayName()+" ";
					
			}
		}
		if(!name.trim().equals(""))
		{
			return name.substring(0, Math.min(8, name.length()));
		}
		return phone;
	}
	//多个电话或者短信的时候弹出
	public static void showMutipleDialog(final Context c,final List<String> num,final int cmd){
		
		ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
		AlertDialog myDialog = new AlertDialog.Builder(c).create();  
		myDialog.show();
        myDialog.getWindow().setContentView(R.layout.main_mydialog);  
        ListView mylist=(ListView)myDialog.getWindow().findViewById(R.id.dialog_list);
        for (int i = 0; i < num.size(); i++) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("title", num.get(i));
			mData.add(item);
		}
        SimpleAdapter a = new SimpleAdapter(c, mData, R.layout.contact_top_right_list,
				new String[] { "title" }, new int[] { R.id.groupName });
		mylist.setAdapter(a);
		myDialog.setCanceledOnTouchOutside(true);
		mylist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if(cmd==1)//电话
				{
					Constant.callContact(c, num.get(position));
				}
				if(cmd==2)//短信
				{
					Constant.smsContact(c, num.get(position), "");
				}
				
			}
		});
        
	}
}
