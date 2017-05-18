package org.wingify.logger;

import org.slf4j.Logger;

public class LoggerFactory {

	public static Logger getLogger(){
		String name = new java.lang.Exception().getStackTrace()[1].getClassName();
		return org.slf4j.LoggerFactory.getLogger(name);
	}
}
