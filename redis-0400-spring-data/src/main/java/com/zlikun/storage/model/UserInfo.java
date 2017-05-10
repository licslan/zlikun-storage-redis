package com.zlikun.storage.model;

import java.io.Serializable;
import java.util.Date;

public class UserInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long userId ;
	private String name ;
	private Gender gender ;
	private Date birthday ;
	private String email ;
	private Integer status ;

	public Long getUserId() {
		return userId;
	}

	public UserInfo setUserId(Long userId) {
		this.userId = userId;
		return this ;
	}

	public String getName() {
		return name;
	}

	public UserInfo setName(String name) {
		this.name = name;
		return this ;
	}

	public Gender getGender() {
		return gender;
	}

	public UserInfo setGender(Gender gender) {
		this.gender = gender;
		return this ;
	}

	public Date getBirthday() {
		return birthday;
	}

	public UserInfo setBirthday(Date birthday) {
		this.birthday = birthday;
		return this ;
	}

	public String getEmail() {
		return email;
	}

	public UserInfo setEmail(String email) {
		this.email = email;
		return this ;
	}

	public Integer getStatus() {
		return status;
	}

	public UserInfo setStatus(Integer status) {
		this.status = status;
		return this ;
	}
	
}
