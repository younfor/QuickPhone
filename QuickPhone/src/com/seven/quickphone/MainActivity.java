package com.seven.quickphone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seven.adapter.CallLogAdapter;
import com.seven.adapter.CharacterParser;
import com.seven.adapter.PinyinComparator;
import com.seven.adapter.RexseeSMS;
import com.seven.adapter.SMSAdapter;
import com.seven.adapter.SortAdapter;
import com.seven.adapter.T9Adapter;
import com.seven.bean.CallLogBean;
import com.seven.bean.ContactBean;
import com.seven.bean.GroupBean;
import com.seven.bean.SMSBean;
import com.seven.data.Constant;
import com.seven.view.PinnedHeaderListView;
import com.seven.view.RefreshableView;
import com.seven.view.RefreshableView.PullToRefreshListener;
import com.seven.view.SideBar;
import com.seven.view.SideBar.OnTouchingLetterChangedListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.game.UMGameAgent;
import com.wandoujia.ads.sdk.Ads;
import com.wandoujia.ads.sdk.loader.Fetcher;
import com.wandoujia.ads.sdk.widget.AdBanner;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnClickListener {

	public AdBanner adBanner;
	public static MainActivity instance = null;
	RefreshableView refreshableView;
	private ViewPager mTabPager;
	private ImageView mTabImg;// 动画图片
	private ImageView mTab1, mTab2, mTab3, mTab4;
	private TextView dialtitle;
	private int zero = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int one;// 单个水平动画位移
	private int two;
	private int three;
	public EditText et;
	MyAsyncQueryHandler asyncQuery;
	private Map<Integer, ContactBean> contactIdMap = null;
	private List<ContactBean> list;
	public View view1, view2, view3, view4;
	// list
	TextView title_key;
	LinearLayout titleLayout;
	private PinnedHeaderListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	public SortAdapter adapter;
	private CharacterParser characterParser;
	private List<ContactBean> SourceData;
	private PinyinComparator pinyinComparator;
	// dial
	private LinearLayout bohaopan;
	private LinearLayout keyboard_show_ll;
	private RelativeLayout main_bottom, main_title;
	private Button keyboard_show, cancel;
	public ListView callLogListview, callContactListview;
	public List<CallLogBean> callLoglist;
	private Button phone_view;
	private Button delete;
	private T9Adapter t9adapter;
	public CallLogAdapter callLogadapter;
	// sms
	public ListView smsListview;
	private SMSAdapter smsadapter;
	RexseeSMS rsms;
	public static String SEND_SMS_ACTION = "发送短信";
	public static String DELIVERY_SMS_ACTION = "接收短信";
	// 广播消息类型
	public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	BroadcastReceiver sendMessage = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(context, "短信发送成功", Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(context, "短信发送失败", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	BroadcastReceiver receiveMessage = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "对方接收成功", Toast.LENGTH_SHORT).show();
		}
	};
	BroadcastReceiver SMSBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 先判断广播消息
			String action = intent.getAction();
			if (SMS_RECEIVED_ACTION.equals(action)) {
				SmsMessage msg = null;
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					Object[] pdusObj = (Object[]) bundle.get("pdus");
					for (Object p : pdusObj) {
						msg = SmsMessage.createFromPdu((byte[]) p);
						String msgTxt = msg.getMessageBody();// 得到消息的内容
						// Date date = new Date(msg.getTimestampMillis());// 时间
						// SimpleDateFormat format = new
						// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						// String receiveTime = format.format(date);
						// String senderNumber = msg.getOriginatingAddress();
						Toast.makeText(
								context,
								"新短信:"
										+ msgTxt.substring(0,
												Math.min(10, msgTxt.length())),
								Toast.LENGTH_LONG).show();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								if (Constant.ca != null)
									Constant.ca.initData();
								if (Constant.main != null)
									Constant.main.updateSMS();
							}
						}, 3000);
					}
				}
				return;
			}
		}
	};

	@Override
	protected void onResume() {
		registerReceiver(sendMessage, new IntentFilter(SEND_SMS_ACTION));
		registerReceiver(receiveMessage, new IntentFilter(DELIVERY_SMS_ACTION));
		registerReceiver(SMSBroadcastReceiver, new IntentFilter(
				"android.provider.Telephony.SMS_RECEIVED"));
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	public void help(View v) {
		new AlertDialog.Builder(MainActivity.this).setTitle("CopyRight")
				.setMessage("cq361106306@qq.com").show()
				.setCanceledOnTouchOutside(true);
	}

	public void exitAll(View v) {
		finish();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(sendMessage);
		unregisterReceiver(receiveMessage);
		unregisterReceiver(SMSBroadcastReceiver);
		super.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 启动activity时不自动弹出软键盘
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// ad
		// MobclickAgent.setDebugMode(true);
		AD.ma = this;
		// Init AdsSdk.
		try {
			Ads.init(this, AD.ADS_APP_ID, AD.ADS_SECRET_KEY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Ads.preLoad(this, Fetcher.AdFormat.banner, AD.TAG_INTERSTITIAL_BANNER);
		instance = this;
		Constant.main = this;
		mTabPager = (ViewPager) findViewById(R.id.tabpager);
		mTabPager.setOnPageChangeListener(new MyOnPageChangeListener());

		mTab1 = (ImageView) findViewById(R.id.img_dial);
		mTab2 = (ImageView) findViewById(R.id.img_address);
		mTab3 = (ImageView) findViewById(R.id.img_find);
		mTab4 = (ImageView) findViewById(R.id.img_settings);
		mTabImg = (ImageView) findViewById(R.id.img_tab_now);
		dialtitle = (TextView) findViewById(R.id.dialTitle);
		main_bottom = (RelativeLayout) findViewById(R.id.main_bottom);
		Display currDisplay = getWindowManager().getDefaultDisplay();// 获取屏幕当前分辨率
		int displayWidth = currDisplay.getWidth();
		one = displayWidth / 4; // 设置水平动画平移大小
		two = one * 2;
		three = one * 3;
		// 将要分页显示的View装入数组中
		LayoutInflater mLi = LayoutInflater.from(this);
		view1 = mLi.inflate(R.layout.main_tab_call, null);
		view2 = mLi.inflate(R.layout.main_tab_contact, null);
		view3 = mLi.inflate(R.layout.main_tab_sms, null);
		view4 = mLi.inflate(R.layout.main_tab_settings, null);
		// 每个页面的view数据
		final ArrayList<View> views = new ArrayList<View>();
		views.add(view1);
		views.add(view2);
		views.add(view3);
		views.add(view4);

		// 填充ViewPager的数据适配器
		PagerAdapter mPagerAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return views.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager) container).removeView(views.get(position));
			}

			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager) container).addView(views.get(position));
				return views.get(position);
			}
		};
		mTabPager.setAdapter(mPagerAdapter);
		// mTabPager.setCurrentItem(1);
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		//
		// testAddContact();
		// contact
		//test();
		init();
	}

	// tap监听
	public void tapClickListener(View v) {
		int i = 0;
		switch (v.getId()) {
		case R.id.tap1:
			i = 0;
			break;
		case R.id.tap2:
			i = 1;
			break;
		case R.id.tap3:
			i = 2;
			break;
		case R.id.tap4:
			i = 3;
			break;
		}
		mTabPager.setCurrentItem(i);
	}

	// 设置标题栏右侧按钮的作用
	public void btnmainright(View v) {
		Intent intent = new Intent(MainActivity.this, GroupDialogActivity.class);
		startActivity(intent);

	}

	// 查询联系人
	private void init() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY }; // 查询的列
		asyncQuery.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询

	}

	// 电话查询类
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		/**
		 * 查询结束的回调函数
		 */
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			ContactBean cb = null;
			if (list == null)
				list = new ArrayList<ContactBean>();
			else
				list.clear();
			if (cursor != null && cursor.getCount() > 0) {
				contactIdMap = new HashMap<Integer, ContactBean>();
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = cursor.getString(3);
					int contactId = cursor.getInt(4);
					Long photoId = cursor.getLong(5);
					String lookUpKey = cursor.getString(6);
					// Log.v("test", name+":"+number);
					if (!contactIdMap.containsKey(contactId)) {
						cb = new ContactBean();
						cb.setDisplayName(name);
						cb.setPhoneNum(number);
						cb.setSortKey(sortKey);
						cb.setContactId(contactId);
						cb.setPhotoId(photoId);
						cb.setLookUpKey(lookUpKey);
						list.add(cb);
						contactIdMap.put(contactId, cb);
					} else
						cb.setPhoneNum(number);
				}
			}
			if (adapter == null) {
				initContactListViews(list);
				initCallLog();
				initSms();
			} else {// update
					// 填充数据
				filledData(list);
				Collections.sort(list, pinyinComparator);
				adapter.updateListView(list);
			}
		}

	}

	private void initContactListViews(List<ContactBean> list) {

		// 下拉刷新
		refreshableView = (RefreshableView) view2
				.findViewById(R.id.refreshable_view);
		refreshableView.setOnRefreshListener(new PullToRefreshListener() {
			@Override
			public void onRefresh() {
				init();
				refreshableView.finishRefreshing();
			}
		}, 0);
		main_title = (RelativeLayout) view2.findViewById(R.id.title);
		cancel = (Button) view2.findViewById(R.id.cancel);
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		sideBar = (SideBar) view2.findViewById(R.id.sidrbar);
		dialog = (TextView) view2.findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}

			}
		});

		sortListView = (PinnedHeaderListView) view2
				.findViewById(R.id.contact_listview);
		sortListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ContactBean cb = adapter.getDatas().get(position);
				if (adapter.getSelects().containsKey(cb.getContactId())
						&& adapter.getSelects().get(cb.getContactId())) {
					adapter.getSelects().put(cb.getContactId(), false);
				} else {
					adapter.getSelects().put(cb.getContactId(), true);
				}
				adapter.notifyDataSetChanged();

			}
		});
		sortListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				if (!Constant.isPullState)
					showContactDialog(adapter.getDatas().get(position));
				return true;
			}
		});
		// 填充数据
		filledData(list);
		Collections.sort(list, pinyinComparator);
		adapter = new SortAdapter(this, list);
		sortListView.setAdapter(adapter);
		sortListView.setOnScrollListener(adapter);
		sortListView.setPinnedHeaderView(getLayoutInflater().inflate(
				R.layout.main_contact_title, sortListView, false));
		EditText editSearch = (EditText) view2
				.findViewById(R.id.contactETsearch);
		editSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (main_bottom.getVisibility() == View.VISIBLE) {
					main_bottom.setVisibility(View.GONE);
					main_title.setVisibility(View.GONE);
					cancel.setVisibility(View.VISIBLE);
					// Log.v("test", "gone3");
				}
			}
		});
		editSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					main_bottom.setVisibility(View.GONE);
					main_title.setVisibility(View.GONE);
					cancel.setVisibility(View.VISIBLE);
					// Log.v("test", "gone3");
				}

			}
		});
		editSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
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
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) v.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
				main_bottom.setVisibility(View.VISIBLE);
				main_title.setVisibility(View.VISIBLE);
				cancel.setVisibility(View.GONE);

			}
		});
	}

	// 弹出群组
	public void showGroupDialog(final ContactBean cb) {
		final List<GroupBean> grouplist = Constant
				.queryGroup(MainActivity.this);
		grouplist.remove(grouplist.size() - 1);
		final ArrayList<Map<String, String>> mData = new ArrayList<Map<String, String>>();
		final AlertDialog myDialog = new AlertDialog.Builder(this).create();
		myDialog.show();
		myDialog.setTitle("选择群组");
		myDialog.getWindow().setContentView(R.layout.main_mydialog);
		ListView mylist = (ListView) myDialog.getWindow().findViewById(
				R.id.dialog_list);
		for (int i = 0; i < grouplist.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("title", grouplist.get(i).getName());
			mData.add(item);
		}
		SimpleAdapter a = new SimpleAdapter(this, mData,
				R.layout.contact_top_right_list, new String[] { "title" },
				new int[] { R.id.groupName });
		mylist.setAdapter(a);
		myDialog.setCanceledOnTouchOutside(true);
		mylist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String title = mData.get(position).get("title");
				// Log.v("test", "改变群组");
				List<ContactBean> templist = new ArrayList<ContactBean>();
				templist.add(new ContactBean(cb));
				Constant.addContactToGroup(MainActivity.this, templist,
						grouplist.get(position).getId());
				myDialog.dismiss();
			}
		});
	}

	// 弹出管理
	public void showContactDialog(final ContactBean cb) {
		int len = cb.getPhoneNum().size();
		String[] arg = new String[2 * len + 4];
		for (int i = 0; i < len * 2; i += 2) {
			String p1 = cb.getPhoneNum().get(i / 2), p2 = cb.getPhoneNum().get(
					(i + 1) / 2);
			arg[i] = new String("电话:"
					+ p1.substring(0, Math.min(6, p1.length())) + "**");
			arg[i + 1] = new String("短信:"
					+ p2.substring(0, Math.min(6, p2.length())) + "**");
		}
		arg[len * 2] = new String("查看联系人");
		arg[len * 2 + 1] = new String("删除联系人");
		arg[len * 2 + 2] = new String("编辑联系人");
		arg[len * 2 + 3] = new String("快速分组");
		final ArrayList<Map<String, String>> mData = new ArrayList<Map<String, String>>();
		final AlertDialog myDialog = new AlertDialog.Builder(this).create();
		myDialog.show();
		myDialog.setTitle(cb.getDisplayName());
		myDialog.getWindow().setContentView(R.layout.main_mydialog);
		ListView mylist = (ListView) myDialog.getWindow().findViewById(
				R.id.dialog_list);
		for (int i = 0; i < 2 * len + 4; i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("title", arg[i]);
			mData.add(item);
		}
		SimpleAdapter a = new SimpleAdapter(this, mData,
				R.layout.contact_top_right_list, new String[] { "title" },
				new int[] { R.id.groupName });
		mylist.setAdapter(a);
		myDialog.setCanceledOnTouchOutside(true);
		mylist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String title = mData.get(position).get("title");
				// Log.v("test", title);
				if (title.indexOf("电话") > -1) {
					Constant.callContact(MainActivity.this, cb.getPhoneNum()
							.get(position / 2));
				} else if (title.indexOf("短信") > -1) {
					Constant.smsContact(MainActivity.this, cb.getPhoneNum()
							.get((position - 1) / 2), "");
				} else if (title.indexOf("查看联系人") > -1) {
					Constant.showContact(MainActivity.this, cb.getContactId());
				} else if (title.indexOf("删除联系人") > -1) {
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("确定删除联系人吗?")
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface arg0, int arg1) {
											Constant.delContact(
													MainActivity.this,
													cb.getContactId());
											myDialog.dismiss();
										}
									}).setNegativeButton("取消", null).show();

				} else if (title.indexOf("编辑联系人") > -1) {
					Constant.cmd = -1;
					Constant.editContact(MainActivity.this, cb);
				} else if (title.indexOf("快速分组") > -1) {
					myDialog.dismiss();
					showGroupDialog(cb);

				}

			}

		});

	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private void filledData(List<ContactBean> list) {
		for (int i = 0, len = list.size(); i < len; i++) {
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(
					list.get(i).getDisplayName()).trim();
			// Log.v("test", pinyin);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			list.get(i).setPinyin(pinyin);
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				list.get(i).setSortKey(sortString.toUpperCase());
			} else {
				list.get(i).setSortKey("#");
			}
			list.get(i).setFormattedNumber(
					Constant.getNameNum(list.get(i).getPinyin()));
		}
		Constant.contactList = list;
	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		boolean isContain=false;
		List<ContactBean> filterDateList = new ArrayList<ContactBean>();
		List<ContactBean> list = this.list;
		if (Constant.grouped_contactList != null)
			list = Constant.grouped_contactList;
		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = list;
		} else {
			filterDateList.clear();
			for (ContactBean sortModel : list) {
				String name = sortModel.getDisplayName();
				String pinyin = sortModel.getPinyin();
				List<String> num=sortModel.getPhoneNum();
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
				if (name.indexOf(filterStr.toString()) != -1
						|| pinyin.startsWith(filterStr.toString())) {
					filterDateList.add(sortModel);
				} else if (Constant.containAB(pinyin.toLowerCase(), filterStr
						.toString().toLowerCase())) {
					filterDateList.add(sortModel);
				}
			}
		}
		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}

	/*
	 * 页卡切换监听(原作者:D.Winter)
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			switch (arg0) {
			case 0:
				Constant.initCallLog(getContentResolver());
				;
				mTab1.setImageDrawable(getResources().getDrawable(
						R.drawable.tab_dial_selected));
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, 0, 0, 0);
					mTab2.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_contact_normal));
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, 0, 0, 0);
					mTab3.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_voice_normal));
				} else if (currIndex == 3) {
					animation = new TranslateAnimation(three, 0, 0, 0);
					mTab4.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_settings_normal));
				}
				break;
			case 1:

				mTab2.setImageDrawable(getResources().getDrawable(
						R.drawable.tab_contact_selected));
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, one, 0, 0);
					mTab1.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_dial_normal));
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, one, 0, 0);
					mTab3.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_voice_normal));
				} else if (currIndex == 3) {
					animation = new TranslateAnimation(three, one, 0, 0);
					mTab4.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_settings_normal));
				}
				break;
			case 2:
				mTab3.setImageDrawable(getResources().getDrawable(
						R.drawable.tab_voice_selected));
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, two, 0, 0);
					mTab1.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_dial_normal));
				} else if (currIndex == 1) {
					animation = new TranslateAnimation(one, two, 0, 0);
					mTab2.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_contact_normal));
				} else if (currIndex == 3) {
					animation = new TranslateAnimation(three, two, 0, 0);
					mTab4.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_settings_normal));
				}
				break;
			case 3:
				AD.showWandoujia();
				mTab4.setImageDrawable(getResources().getDrawable(
						R.drawable.tab_settings_pressed));
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, three, 0, 0);
					mTab1.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_dial_normal));
				} else if (currIndex == 1) {
					animation = new TranslateAnimation(one, three, 0, 0);
					mTab2.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_contact_normal));
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, three, 0, 0);
					mTab3.setImageDrawable(getResources().getDrawable(
							R.drawable.tab_voice_normal));
				}
				break;
			}
			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(150);
			mTabImg.startAnimation(animation);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	// 联系人管理
	public void btn_contact_manager(View v) {
		Constant.cmd = 1;
		// Log.v("test", "联系人删除");
		Intent intent = new Intent(MainActivity.this,
				ContactManagerActivity.class);
		startActivity(intent);
	}

	// 分组管理
	public void btn_group_manager(View v) {
		// Log.v("test", "分组管理");
		Intent intent = new Intent(MainActivity.this,
				GroupManagerActivity.class);
		startActivity(intent);
	}

	// View1-添加联系人
	public void btn_view1_addContact(View v) {
		AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
				.setTitle("添加" + phone_view.getText().toString() + "到:")
				.setPositiveButton("新建联系人",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								Constant.showNewContact(MainActivity.this,
										phone_view.getText().toString());
							}
						})
				.setNegativeButton("现有联系人",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								// 弹出联系人窗口选择
								// Log.v("test", "现有联系人");
								Constant.cmd = 3;// 选择联系人
								Constant.choosedNum = phone_view.getText()
										.toString();
								Intent intent = new Intent(MainActivity.this,
										ContactManagerActivity.class);
								startActivity(intent);
							}
						}).show();
		alert.setCanceledOnTouchOutside(true);
	}

	// View2-添加联系人
	public void add_contact(View v) {
		Constant.showNewContact(MainActivity.this, "");
	}

	// View1-清空记录
	public void btn_clearCallLog(View v) {
		// Log.v("test", "清空");
		new AlertDialog.Builder(MainActivity.this).setTitle("确定清空记录吗?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						Constant.clearCallLog(getContentResolver());
					}
				}).setNegativeButton("取消", null).show();

	}

	// View3-短信
	public void btn_editSMS(View v) {
		// Log.v("test", "编辑短信");
		Button btn = (Button) v;
		if (Constant.isDel == 0) {
			btn.setText(" 返回  ");
		} else {
			btn.setText(" 编辑  ");
		}
		Constant.isDel = (Constant.isDel + 1) % 2;
		smsadapter.notifyDataSetChanged();
	}

	// clearCallLog
	public void updateCallLog() {
		Constant.initCallLog(getContentResolver());
	}

	// initCallLog
	public void initCallLog() {
		bohaopan = (LinearLayout) view1.findViewById(R.id.bohaopan);
		keyboard_show_ll = (LinearLayout) view1
				.findViewById(R.id.keyboard_show_ll);
		keyboard_show = (Button) view1.findViewById(R.id.keyboard_show);
		callLogListview = (ListView) view1.findViewById(R.id.call_log_list);
		// Log.v("test", "call");
		Constant.initCallLog(getContentResolver());

		callLogListview.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Log.v("test", "滚动了");
				if (bohaopan.getVisibility() == View.VISIBLE) {
					bohaopan.setVisibility(View.GONE);
					keyboard_show_ll.setVisibility(View.VISIBLE);
				}
				return false;
			}
		});
		for (int i = 0; i < 12; i++)
			view1.findViewById(R.id.dialNum1 + i).setOnClickListener(this);
		phone_view = (Button) view1.findViewById(R.id.phone_view);
		phone_view.setOnClickListener(this);
		callContactListview = (ListView) view1
				.findViewById(R.id.call_contact_list);
		t9adapter = new T9Adapter(MainActivity.this);
		callContactListview.setAdapter(t9adapter);
		callContactListview.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					if (bohaopan.getVisibility() == View.VISIBLE) {
						bohaopan.setVisibility(View.GONE);
						keyboard_show_ll.setVisibility(View.VISIBLE);
					}
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		callContactListview.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Log.v("test", "滚动了");
				if (bohaopan.getVisibility() == View.VISIBLE) {
					bohaopan.setVisibility(View.GONE);
					keyboard_show_ll.setVisibility(View.VISIBLE);
				}
				return false;
			}
		});
		phone_view.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (null == Constant.contactList
						|| Constant.contactList.size() < 1
						|| "".equals(s.toString())) {
					callContactListview.setVisibility(View.INVISIBLE);
					callLogListview.setVisibility(View.VISIBLE);
				} else {
					T9Adapter.filterNum = s.toString();
					callLogListview.setVisibility(View.INVISIBLE);
					callContactListview.setVisibility(View.VISIBLE);
					// 更新
					t9adapter.updateListView(Constant.filterData(s.toString()));

				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		delete = (Button) view1.findViewById(R.id.delete);
		delete.setOnClickListener(this);
		delete.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				phone_view.setText("");
				return false;
			}
		});
		keyboard_show.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialPadShow();
			}
		});
	}

	public void dialPadShow() {
		if (bohaopan.getVisibility() == View.VISIBLE) {
			bohaopan.setVisibility(View.GONE);
			keyboard_show_ll.setVisibility(View.VISIBLE);
		} else {
			bohaopan.setVisibility(View.VISIBLE);
			keyboard_show_ll.setVisibility(View.INVISIBLE);
		}
	}

	private void input(String str) {
		String p = phone_view.getText().toString();
		phone_view.setText(p + str);
	}

	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.dialNum0:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum1:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum2:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum3:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum4:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum5:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum6:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum7:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}

			break;
		case R.id.dialNum8:
			if (phone_view.getText().length() < 12) {

				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum9:
			if (phone_view.getText().length() < 12) {

				input(v.getTag().toString());
			}
			break;
		case R.id.dialx:
			if (phone_view.getText().length() < 12) {

				input(v.getTag().toString());
			}
			break;
		case R.id.dialj:
			if (phone_view.getText().length() < 12) {

				input(v.getTag().toString());
			}
			break;
		case R.id.delete:
			String p = phone_view.getText().toString();
			if (p.length() > 0) {
				phone_view.setText(p.substring(0, p.length() - 1));
			}
			break;
		case R.id.phone_view:
			if (phone_view.getText().toString().length() >= 3) {
				Constant.callContact(MainActivity.this, phone_view.getText()
						.toString());
			}
			break;
		default:
			break;
		}
	}

	// sms
	public void initSms() {

		// Log.v("test", "init sms");
		smsListview = (ListView) view3.findViewById(R.id.sms_listview);
		smsadapter = new SMSAdapter(this);

		rsms = new RexseeSMS(this);
		List<SMSBean> list_mmt = rsms.getThreads();
		smsadapter.assignment(list_mmt);
		smsListview.setAdapter(smsadapter);
		smsListview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Constant.currentSMSBean = smsadapter.getItem(position);
				Intent intent = new Intent(MainActivity.this,
						ChatActivity.class);
				startActivity(intent);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Constant.setReadStage(MainActivity.this,
								Constant.currentSMSBean.getThread_id());
						updateSMS();
					}
				}, 1000);

			}
		});
	}

	public void updateSMS() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				smsadapter.assignment(rsms.getThreads());
				smsadapter.notifyDataSetChanged();
			}
		}, 100);

	}

	public void addGroupSMS(View v) {
		Intent intent = new Intent(MainActivity.this, NewSMSActivity.class);
		startActivity(intent);
	}

	/*public void test()
	{
		for(int i=0;i<700;i++)
			AddContact("测试"+i, "100"+i);
	}
	// 往数据库中新增联系人

	public void AddContact(String name, String number)

	{

		ContentValues values = new ContentValues();

		// 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId

		Uri rawContactUri = getContentResolver().insert(
				RawContacts.CONTENT_URI, values);

		long rawContactId = ContentUris.parseId(rawContactUri);

		// 往data表插入姓名数据

		values.clear();

		values.put(Data.RAW_CONTACT_ID, rawContactId);

		values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);// 内容类型

		values.put(StructuredName.GIVEN_NAME, name);

		getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

		// 往data表插入电话数据

		values.clear();

		values.put(Data.RAW_CONTACT_ID, rawContactId);

		values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);

		values.put(Phone.NUMBER, number);

		values.put(Phone.TYPE, Phone.TYPE_MOBILE);

		getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

	}*/
    @Override
    public void onBackPressed() {
    	moveTaskToBack(false);
	/*new AlertDialog.Builder(this).setTitle("确认退出吗？")
		.setIcon(android.R.drawable.ic_dialog_info)
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			// 点击“确认”后的操作
		    	
			//finish();
 
		    }
		})
		.setNegativeButton("返回", new DialogInterface.OnClickListener() {
 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			// 点击“返回”后的操作,这里不设置没有任何操作
		    }
		}).show();*/
	// super.onBackPressed();
    }

}
