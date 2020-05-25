package com.forezp.util;

import org.springframework.http.HttpStatus;

public class Notice {
	HttpStatus state;
	String notice;
	Object data;

	public Notice() {

	}

	public Notice(HttpStatus state, String notice) {
		this.state = state;
		this.notice = notice;
	}

	public Notice(HttpStatus state, Object data, String notice) {
		this.state = state;
		this.data = data;
		this.notice = notice;
	}

    public HttpStatus getState() {
		return state;
	}

	public void setState(HttpStatus state) {
		this.state = state;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
