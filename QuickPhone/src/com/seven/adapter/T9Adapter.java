package com.seven.adapter;

import java.util.ArrayList;
import java.util.List;

import com.seven.bean.ContactBean;
import com.seven.data.Constant;
import com.seven.quickphone.R;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class T9Adapter extends BaseAdapter  {

	private LayoutInflater mInflater;
	private List<ContactBean> list;
	private Context context;
    public static String filterNum;
	public T9Adapter(Context context) {     
		mInflater = LayoutInflater.from(context); 
		this.list = new ArrayList<ContactBean>(Constant.contactList);
		this.context=context;
	}   

	public void add(ContactBean bean) {
		list.add(bean);
	}
	public void remove(int position){
		list.remove(position);
	}
	public int getCount() {  
		return list.size();     
	}            
	public ContactBean getItem(int position) {    
		return list.get(position);     
	}          
	public long getItemId(int position) {
		return 0;   
	}    
	public void updateListView(List<ContactBean> list){
		this.list = list;
		notifyDataSetChanged();
	}
	public View getView(int position, View convertView, ViewGroup parent) {   

		ViewHolder holder = null;
		final ContactBean mContent=list.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.call_t9_list_item, parent, false);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);  
			holder.pinyin = (TextView) convertView.findViewById(R.id.pinyin);  
			holder.number = (TextView) convertView.findViewById(R.id.number);  
			holder.tvCall=(TextView)convertView.findViewById(R.id.call_btn);
			holder.tvSMS=(TextView)convertView.findViewById(R.id.sms_btn);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		//µç»°
		holder.tvCall.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						//Log.v("test", "call:"+mContent.getDisplayName());
						if(mContent.getPhoneNum().size()==1)
							Constant.callContact(context, mContent.getPhoneNum().get(0));
						else
						{
							Constant.showMutipleDialog(context,mContent.getPhoneNum(),1);
							
						}
					}
				});
				//¶ÌÐÅ
		holder.tvSMS.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						//Log.v("test", "sms:"+mContent.getDisplayName());
						if(mContent.getPhoneNum().size()==1)
							Constant.smsContact(context, mContent.getPhoneNum().get(0), "");
						else
						{
							Constant.showMutipleDialog(context,mContent.getPhoneNum(),2);
							
						}
							
					}
				});
		holder.name.setText(mContent.getDisplayName());
		String formattedNumber = mContent.getPinyin().toLowerCase();
		String t="";int len=mContent.getPhoneNum().size();
		for(int i=0;i<len-1;i++)
			t+=mContent.getPhoneNum().get(i)+"<br/>";
		t+=mContent.getPhoneNum().get(len-1);
		holder.number.setText(Html.fromHtml(t.replace(filterNum, "<font color='#cc0000'>" + filterNum + "</font>")));
		for (int i = 0; i < filterNum.length(); i++) {
			char c = filterNum.charAt(i);
			if (TextUtils.isDigitsOnly(String.valueOf(c)))
			{
				char[] zms = Constant.digit(c);
				if (zms != null) 
				{
					for (char c1 : zms)
					{
						formattedNumber = formattedNumber.replaceAll(String.valueOf(c1), "%%" + String.valueOf(c1) + "@@");
					}
					
				}
			}
		}
		formattedNumber = formattedNumber.replaceAll("%%", "<font color='#cc0000'>");
		formattedNumber = formattedNumber.replaceAll("@@", "</font>");
	//	Log.v("test", formattedNumber);
		holder.pinyin.setText(Html.fromHtml(formattedNumber));
		convertView.setTag(holder);
		return convertView;
	}   

	public final class ViewHolder {   
		public TextView name;        
		public TextView pinyin;        
		public TextView number;    
		public TextView tvCall;
		public TextView tvSMS;
	}

}
