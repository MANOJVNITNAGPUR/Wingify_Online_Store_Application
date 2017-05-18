package org.wingify.beans;

public enum ResponseCode {
	
	SUCCESS(200,"Successful"), 
	FAILURE(400,"Technical Issue."),
    INTERNAL_SERVER_ERROR(500,"Internal server error."),
    LOGIN_BAD_REQUEST(400,"Username or password is empty"),
    INAVLID_USER(400,"Invalid username or password"),
	USER_EXISTS(400,"User already register with this username"),
	USER_LOGIN_EXISTS(201,"User already logged in"),
	NO_PRODUCT_AVAILABLE(201,"No products available"),
	USER_NOT_LOG_IN(400, "User not log in"),
	VENDOR_ERROR(400,"Unauthorized- Only vendor has access to Add/Update/Delete product details"),
	UNAUTHORIZED_ACCESS(400,"Unauthorized access"),
	SECURE_TOKEN_EMPTY(400,"Authentication token can not be empty");
    
	private int code;
	private String defaultMessage;
	private ResponseCode(int statusCode, String message){
		this.code=statusCode;
		this.defaultMessage=message;
	}
	public int getCode() {
		return code;
	}
	public String getDefaultMessage() {
		return defaultMessage;
	}

}
