package org.wingify.util;

import java.util.Calendar;
import java.util.UUID;

public  class TokenGenerator {

	public static String generateSSOToken(){
		return  UUID.randomUUID().toString()+Calendar.getInstance().getTimeInMillis();
	};
}
