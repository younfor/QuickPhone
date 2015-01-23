package com.seven.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import com.seven.bean.ContactBean;
import com.seven.data.Constant;
import com.seven.quickphone.R;
import com.seven.view.PinnedHeaderListView.PinnedHeaderAdapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class SortAdapter extends BaseAdapter implements SectionIndexer,PinnedHeaderAdapter,OnScrollListener{
	private List<ContactBean> list = null;
	private Context mContext;
	private HashMap<Long,Boolean> selects=new HashMap<Long,Boolean>();
	private ArrayList<String> sections=new ArrayList<String>();
	public SortAdapter(Context mContext, List<ContactBean> list) {
		this.mContext = mContext;
		this.list = list;
		this.setSections(list);
		
	}
	public HashMap<Long,Boolean> getSelects()
	{
		return selects;
	}
	public List<ContactBean> getDatas()
	{
		return list;
	}
	public void setSections(List<ContactBean> list)
	{
		sections.clear();
		for(ContactBean c:list)
		{
			if(!sections.contains(c.getSortKey()))
				sections.add(c.getSortKey());
		}
	}
	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * @param list
	 */
	public void updateListView(List<ContactBean> list){
		this.list = list;
		this.setSections(list);
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		Constant.ViewHolder viewHolder = null;
		final ContactBean mContent = list.get(position);
		if (view == null) {
			viewHolder = new Constant.ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.main_contact_listitem, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.listview_title);
			viewHolder.tvKey=(TextView) view.findViewById(R.id.sort_key);
			viewHolder.tvPhone=(TextView) view.findViewById(R.id.listview_phone);
			viewHolder.sortKeyLayout = (LinearLayout) view.findViewById(R.id.sort_key_layout);
			viewHolder.moreLayout=(LinearLayout) view.findViewById(R.id.more_layout);
			viewHolder.nameLayout=(LinearLayout) view.findViewById(R.id.name_layout);
			viewHolder.tvCall=(ImageView)view.findViewById(R.id.item_call);
			viewHolder.tvSMS=(ImageView)view.findViewById(R.id.item_message);
			viewHolder.tvDetail=(ImageView)view.findViewById(R.id.item_detail);
			viewHolder.headId=(ImageView)view.findViewById(R.id.headId);
			view.setTag(viewHolder);
		} else {
			viewHolder = (Constant.ViewHolder) view.getTag(); 
		}
		//事件
		if(selects.containsKey(mContent.getContactId())&&selects.get(mContent.getContactId()))
		{
			viewHolder.moreLayout.setVisibility(View.VISIBLE);
		}else
			viewHolder.moreLayout.setVisibility(View.GONE);
		//电话
		viewHolder.tvCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Log.v("test", "call:"+mContent.getDisplayName());
				if(mContent.getPhoneNum().size()==1)
					Constant.callContact(mContext, mContent.getPhoneNum().get(0));
				else
				{
					Constant.showMutipleDialog(mContext,mContent.getPhoneNum(),1);
					
				}
			}
		});
		//短信
		viewHolder.tvSMS.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Log.v("test", "sms:"+mContent.getDisplayName());
				if(mContent.getPhoneNum().size()==1)
					Constant.smsContact(mContext, mContent.getPhoneNum().get(0), "");
				else
				{
					Constant.showMutipleDialog(mContext,mContent.getPhoneNum(),2);
				}
					
			}
		});
		//详细
		viewHolder.tvDetail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Log.v("test", "detail:"+mContent.getDisplayName());
				Constant.showContact(mContext, mContent.getContactId());
			}
		});
		//根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);
		viewHolder.tvTitle.setText(mContent.getDisplayName().substring(0, Math.min(16, mContent.getDisplayName().length())));
		String t="";int len=mContent.getPhoneNum().size();
		for(int i=0;i<len-1;i++)
			t+=mContent.getPhoneNum().get(i)+"\n";
		t+=mContent.getPhoneNum().get(len-1);
		viewHolder.tvPhone.setText(t);
		if (position == getPositionForSection(section)) {  
			viewHolder.tvKey.setText(mContent.getSortKey());  
			viewHolder.sortKeyLayout.setVisibility(View.VISIBLE);  
        } else {  
        
        	viewHolder.sortKeyLayout.setVisibility(View.GONE);  
        }  
		//head
		
		 Long pid=mContent.getPhotoId();
  	     if(pid!=0)
  	     {
  	    	viewHolder.headId.setImageBitmap(Constant.getHeadBitMap(mContext.getContentResolver(),mContent.getContactId()));
  	     }else
  	     {
  	    	viewHolder.headId.setImageResource(R.drawable.icon_contact);
 	  	    
   	     }
		
		return view;

	}
	


	

	public int getNextSection(int section)
	{
		for(int i=0,len=sections.size();i<len;i++)
		{
			if(sections.get(i).charAt(0)==section &&i+1<len)
				return  sections.get(i+1).charAt(0);
		}
		return -1;
	}
	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		if(list.size()==0)
			return -1;
		return list.get(position).getSortKey().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortKey();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String  sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int arg2, int arg3) {
		if (view instanceof com.seven.view.PinnedHeaderListView) {
			((com.seven.view.PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}
		
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPinnedHeaderState(int position) {
			int realPosition = position;
			if (realPosition < 0) 
			{
				return PINNED_HEADER_GONE;
			}
			int section = getSectionForPosition(realPosition);// 得到此item所在的分组位置
			int nextSectionPosition = getPositionForSection(getNextSection(section));// 得到下一个分组的位置
			//当前position正好是当前分组的最后一个position，也就是下一个分组的第一个position的前面一个
			if (nextSectionPosition != -1
			&& realPosition == nextSectionPosition - 1) {
			return PINNED_HEADER_PUSHED_UP;
			}
			return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
		int realPosition = position;
		int section = getSectionForPosition(realPosition);
		String title = "";
		if(section>-1)
			title=""+(char)section;
		((TextView) header.findViewById(R.id.sort_keyTitle)).setText(title);
		
	}
}