package com.seven.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.seven.bean.ContactBean;
import com.seven.bean.SMSBean;
import com.seven.data.Constant;
import com.seven.quickphone.MainActivity;
import com.seven.quickphone.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SMSAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<SMSBean> list;
	private Context context;
	private Date d;
	private SimpleDateFormat sdf;
	
	public SMSAdapter(Context context) {     
		mInflater = LayoutInflater.from(context); 
		this.list = new ArrayList<SMSBean>();
		this.context=context;
		this.d=new Date();
		this.sdf=new SimpleDateFormat("MM/dd HH:mm");
	}   

	public void assignment(List<SMSBean> list){
		this.list = list;
	}
	public void add(SMSBean bean) {
		list.add(bean);
	}
	public void remove(int position){
		list.remove(position);
	}
	public int getCount() {  
		return list.size();     
	}            
	public SMSBean getItem(int position) {    
		return list.get(position);     
	}          
	public long getItemId(int position) {
		return 0;   
	}           
	public View getView(final int position, View convertView, ViewGroup parent) {   

		ViewHolder holder = null;
		final SMSBean sb=list.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.sms_list_item, parent, false);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);  
			holder.count = (TextView) convertView.findViewById(R.id.count);  
			holder.date = (TextView) convertView.findViewById(R.id.date);  
			holder.content = (TextView) convertView.findViewById(R.id.content); 
			holder.newTag= (TextView) convertView.findViewById(R.id.newTag); 
			holder.del= (Button) convertView.findViewById(R.id.btn_delSMS); 
			holder.qcb=(ImageView) convertView.findViewById(R.id.qcb);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		if(Constant.isDel==1)
		{
			holder.del.setVisibility(View.VISIBLE);
			holder.date.setVisibility(View.GONE);
		}else
		{
			holder.del.setVisibility(View.GONE);
			holder.date.setVisibility(View.VISIBLE);
		}
		holder.del.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Constant.delSMS(context, sb.getThread_id());
				MainActivity.instance.updateSMS();
				//if(position<MainActivity.instance.smsListview.getCount())
					//MainActivity.instance.smsListview.setSelection(position);
			}
		});
		if(sb.getRead().equals("1"))
			holder.newTag.setVisibility(View.INVISIBLE);
		else
			holder.newTag.setVisibility(View.VISIBLE);
		String name="";
		int len=sb.getAddress().size();
		for(int i=0;i<len-1;i++)
		{
			name+=Constant.findNameByPhone(Constant.contactList,sb.getAddress().get(i))+"|";
		}
		name+=Constant.findNameByPhone(Constant.contactList,list.get(position).getAddress().get(len-1));
		holder.name.setText(name.substring(0, Math.min(16, name.length())));
		holder.count.setText("(" + list.get(position).getMsg_count() + ")");
		//head
		for(int i=0;i<len;i++)
		{
			 ContactBean cb=Constant.findContactByPhone(sb.getAddress().get(i));
	  	     if(cb.getPhotoId()!=0)
	  	     {
	  	    	holder.qcb.setImageBitmap(Constant.getHeadBitMap(context.getContentResolver(), cb.getContactId()));
	  	     }else
	  	     {
	  	    	holder.qcb.setImageResource(R.drawable.icon_contact);
	 	  	    
	   	     }
		}
		this.d.setTime(list.get(position).getDate());
		holder.date.setText(this.sdf.format(d));
		if(sb.getMsg_snippet()!=null)
			holder.content.setText(sb.getMsg_snippet().substring(0, Math.min(6,sb.getMsg_snippet().length() ))+"..");

		convertView.setTag(holder);
		return convertView;
	}   

	public final class ViewHolder {   
		public TextView name;        
		public TextView count;        
		public TextView date;        
		public TextView content,newTag;     
		public ImageView qcb;
		public Button del;
		
	}
}
