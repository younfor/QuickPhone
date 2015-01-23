package com.seven.bean;

import java.util.ArrayList;
import java.util.List;

import android.R.string;

public class ContactBean {

	private long contactId;
	private String displayName;
	private List<String> phoneNum=new ArrayList<String>();
	private String sortKey;
	private Long photoId;
	private String lookUpKey;
	private int selected = 0;
	private String formattedNumber;
	private String pinyin;
	
	public ContactBean(){}
	public ContactBean(String name,String num[])
	{
		this.setDisplayName(name);
		for(int i=0;i<num.length;i++)
			this.setPhoneNum(num[i]);
	}
	public ContactBean(ContactBean c)
	{
		this.contactId=c.contactId;
		this.displayName=c.displayName;
		this.phoneNum=c.phoneNum;
		this.sortKey=c.sortKey;
		this.photoId=c.photoId;
		this.formattedNumber=c.formattedNumber;
		this.selected=c.selected;
		this.pinyin=c.pinyin;
	}
	public long getContactId() {
		return contactId;
	}
	public void setContactId(long contactId) {
		this.contactId = contactId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public List<String> getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phonenum) {
		this.phoneNum.add(phonenum);
	}
	public String getSortKey() {
		return sortKey;
	}
	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}
	public Long getPhotoId() {
		return photoId;
	}
	public void setPhotoId(Long photoId) {
		this.photoId = photoId;
	}
	public String getLookUpKey() {
		return lookUpKey;
	}
	public void setLookUpKey(String lookUpKey) {
		this.lookUpKey = lookUpKey;
	}
	public int getSelected() {
		return selected;
	}
	public void setSelected(int selected) {
		this.selected = selected;
	}
	public String getFormattedNumber() {
		return formattedNumber;
	}
	public void setFormattedNumber(String formattedNumber) {
		this.formattedNumber = formattedNumber;
	}
	public String getPinyin() {
		return pinyin;
	}
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
}
