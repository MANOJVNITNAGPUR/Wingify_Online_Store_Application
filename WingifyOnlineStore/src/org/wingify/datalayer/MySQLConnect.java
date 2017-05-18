package org.wingify.datalayer;

import java.io.InputStream;
import java.sql.DriverManager;
import java.util.Properties;

import org.slf4j.Logger;
import org.wingify.logger.LoggerFactory;

import com.mysql.jdbc.Connection;

public final class MySQLConnect {
	
	public Connection conn;
		
	private static final Logger logger = LoggerFactory.getLogger();
	
	private Properties dbProperties = readDBProperties();
	
	public static MySQLConnect db = getDbCon();
	
	private MySQLConnect(){
		try {
			String dbUrl= dbProperties.getProperty("jdbc.url");
	        String driver = dbProperties.getProperty("jdbc.driverClassName");
	        String userName = dbProperties.getProperty("jdbc.username");
	        String password = dbProperties.getProperty("jdbc.password");
	        Class.forName(driver).newInstance();
            this.conn = (Connection)DriverManager.getConnection(dbUrl,userName,password);
		}catch(Exception ex){
			logger.error("exception occurred while creating DB connection", ex);
		}
		
	}
	
	public static synchronized MySQLConnect getDbCon() {
        if ( db == null ) {
            db = new MySQLConnect();
        }
        return db;
 
    }
	
	public Properties readDBProperties(){
		logger.info("Reading database config properties");
		Properties prop = null;
		try{
			InputStream in = getClass().getClassLoader().getResourceAsStream("db-config.properties");;
			 prop = new Properties();
            prop.load(in);
            logger.info("Database config properties Loaded successfully");
		}catch(Exception ex){
			logger.error("Error occurred while readind database properties", ex);
		}
		return prop;
	}
}
