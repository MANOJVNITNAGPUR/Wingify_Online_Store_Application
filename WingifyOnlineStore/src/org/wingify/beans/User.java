package org.wingify.beans;

import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

public class User {
	
	private String userName;
	private String password;
	private boolean isVendor;
	
	public User() {
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = getHashedPassword(password);
	}


	public boolean isVendor() {
		return isVendor;
	}


	public void setVendor(boolean isVendor) {
		this.isVendor = isVendor;
	}

	
	public String getHashedPassword(String password){
		String myHash = "";
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
		    md.update(password.getBytes());
		    byte[] digest = md.digest();
		    myHash = DatatypeConverter
		      .printHexBinary(digest).toUpperCase();
		    
		}catch(Exception ex){
			
		}
		return myHash;
	}

}