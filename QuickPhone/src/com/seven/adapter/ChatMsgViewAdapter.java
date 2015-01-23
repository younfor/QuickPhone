
package com.seven.adapter;

import android.R.integer;
import android.content.Context;
import android.database.DataSetObserver;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.seven.bean.ChatMsgBean;
import com.seven.bean.ContactBean;
import com.seven.data.Constant;
import com.seven.quickphone.ChatActivity;
import com.seven.quickphone.R;

public class ChatMsgViewAdapter extends BaseAdapter {
	
	public static interface IMsgViewType
	{
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}
	public HashMap<String, Boolean> dateHash=new HashMap<String, Boolean>();
    private static final String TAG = ChatMsgViewAdapter.class.getSimpleName();

    private List<ChatMsgBean> coll;

    private Context ctx;
    
    private LayoutInflater mInflater;

    public ChatMsgViewAdapter(Context context, List<ChatMsgBean> coll) {
        ctx = context;
        this.coll = coll;
        mInflater = LayoutInflater.from(context);
        initHash(1);
    }
    public void initHash(int clear)
    {
		if(clear==1)
			dateHash.clear();
    	for(int i=0,len=coll.size();i<len;i++)
    	{
    		if(i==0)
    		{
    			dateHash.put(coll.get(i).getId(), true);
    			//Log.v("test", "id:"+coll.get(i).getId()+":true");
    		}
    		else
    		{
    			try {
    				long a=ChatActivity.sdf.parse(coll.get(i).getDate()).getTime();
    				long b=ChatActivity.sdf.parse(coll.get(i-1).getDate()).getTime();
    				if(Math.abs(a-b)>1000*60*5)
    				{
    					dateHash.put(coll.get(i).getId(), true);
    				//	Log.v("test", "id:"+coll.get(i).getId()+":true");
    				}
    				else
    				{
    					dateHash.put(coll.get(i).getId(), false);
    					//Log.v("test", "id:"+coll.get(i).getId()+":false");
    				}
    				
    			} catch (ParseException e) {
    				
    				e.printStackTrace();
    			}
    		}
    	}
    }
    public void updateView(List<ChatMsgBean> coll)
    {
    	 this.coll = coll;
    	 initHash(0);
    	 notifyDataSetChanged();
    }
    public int getCount() {
        return coll.size();
    }

    public Object getItem(int position) {
        return coll.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    


	public int getItemViewType(int position) {
	 	ChatMsgBean entity = coll.get(position);
	 	
	 	if (entity.getMsgType())
	 	{
	 		return IMsgViewType.IMVT_COM_MSG;
	 	}else{
	 		return IMsgViewType.IMVT_TO_MSG;
	 	}
	 	
	}


	public int getViewTypeCount() {
	
		return 2;
	}
	
	
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	final ChatMsgBean entity = coll.get(position);
    	final boolean isComMsg = entity.getMsgType();
    		
    	ViewHolder viewHolder = null;	
	    if (convertView == null)
	    {
	    	  if (isComMsg)
			  {
				  convertView = mInflater.inflate(R.layout.sms_item_msg_text_left, null);
			  }else{
				  convertView = mInflater.inflate(R.layout.sms_item_msg_text_right, null);
			  }

	    	  viewHolder = new ViewHolder();
			  viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
			  viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tv_username);
			  viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
			  viewHolder.isComMsg = isComMsg;
			  viewHolder.tv_userhead=(ImageView) convertView.findViewById(R.id.iv_userhead);
			  convertView.setTag(viewHolder);
	    }else{
	        viewHolder = (ViewHolder) convertView.getTag();
	    }
	
	    
	    //time
	    if(dateHash.containsKey(entity.getId())&&dateHash.get(entity.getId()))
	    {
	    	viewHolder.tvSendTime.setVisibility(View.VISIBLE);
	    	viewHolder.tvSendTime.setText(entity.getDate());
	    }
	    else
	    	viewHolder.tvSendTime.setVisibility(View.INVISIBLE);
	     if (isComMsg)
		  {
	    	 viewHolder.tvUserName.setText((Constant.findNameByPhone(Constant.contactList,entity.getName())));
	  	     ContactBean cb=Constant.findContactByPhone(entity.getName());
	  	     if(cb.getPhotoId()!=0)
	  	     {
	  	    	viewHolder.tv_userhead.setImageBitmap(Constant.getHeadBitMap(ctx.getContentResolver(), cb.getContactId()));
	  	     }else
	  	     {
	  	    	viewHolder.tv_userhead.setImageResource(R.drawable.icon_contact);
		  	    
	  	     }
		  }else{
			  ContactBean cb=Constant.findContactByPhone(Constant.myPhoneNUM);
			  if(cb.getPhotoId()!=0)
			 {
	  	    	viewHolder.tv_userhead.setImageBitmap(Constant.getHeadBitMap(ctx.getContentResolver(), cb.getContactId()));
	  	     }else
	  	     {
	  	    	viewHolder.tv_userhead.setImageResource(R.drawable.icon_contact);
		  	    
	  	     }
			  viewHolder.tvUserName.setText(Constant.myPhone);
			   
		  }
	    viewHolder.tvContent.setText(entity.getText());
	    
	    return convertView;
    }
    

    static class ViewHolder { 
        public TextView tvSendTime;
        public TextView tvUserName;
        public TextView tvContent;
        public ImageView tv_userhead;
        public boolean isComMsg = true;
    }


}
