package com.seven.bean;

import java.util.List;

public class SMSBean {

	private String thread_id;
	private String msg_count;
	private String msg_snippet,read;
	private List<String> address;
	public List<String> getAddress() {
		return address;
	}
	public void setAddress(List<String> address) {
		this.address = address;
	}
	private Long date;
	
	public SMSBean(String threadId ,Long date,String msgCount,List<String> address, String msgSnippet,String read) {
		thread_id = threadId;
		msg_count = msgCount;
		msg_snippet = msgSnippet;
		this.address=address;
		this.date=date;
		this.read=read;
	}
	public SMSBean() {
	}
	
	public Long getDate() {
		return date;
	}
	public void setDate(Long date) {
		this.date = date;
	}
	public String getRead() {
		return read;
	}
	public void setRead(String read) {
		this.read = read;
	}
	public String getThread_id() {
		return thread_id;
	}
	public void setThread_id(String threadId) {
		thread_id = threadId;
	}
	public String getMsg_count() {
		return msg_count;
	}
	public void setMsg_count(String msgCount) {
		msg_count = msgCount;
	}
	public String getMsg_snippet() {
		return msg_snippet;
	}
	public void setMsg_snippet(String msgSnippet) {
		msg_snippet = msgSnippet;
	}
}
