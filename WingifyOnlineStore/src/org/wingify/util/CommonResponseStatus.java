package org.wingify.util;

import org.wingify.beans.ResponseCode;
import org.wingify.beans.ResponseStatus;

public class CommonResponseStatus extends ResponseStatus {

	private CommonResponseStatus() {
	}

	public static CommonResponseStatus getResponseStatus(ResponseCode responseCode) {
		return getResponseStatus(responseCode, responseCode.getDefaultMessage());
	}

	public static CommonResponseStatus getResponseStatus(ResponseCode responseCode, String custMessage) {
		CommonResponseStatus responseStatus = new CommonResponseStatus();
		responseStatus.setStatusCode(String.format("%02d", responseCode.getCode()));
		responseStatus.setResponseMessage(custMessage);
		return responseStatus;
	}

}
