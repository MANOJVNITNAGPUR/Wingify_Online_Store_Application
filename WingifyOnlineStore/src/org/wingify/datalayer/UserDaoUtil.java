package org.wingify.datalayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.wingify.beans.CommonResponse;
import org.wingify.beans.ResponseCode;
import org.wingify.beans.SsoToken;
import org.wingify.beans.User;
import org.wingify.beans.UserDetails;
import org.wingify.logger.LoggerFactory;
import org.wingify.util.CommonResponseStatus;
import org.wingify.util.TokenGenerator;


public class UserDaoUtil {

	private static final Logger logger = LoggerFactory.getLogger();	
	public static final String GET_USER_NAME_QUERY = "select details.user_firstname, details.user_lastname from user_details details where details.user_id = ?";
	public static final String USER_SESSION_CHECK_QUERY = "select session.sso_token from user_session session where session.user_id =? ";
	public static final String USER_FROM_SESSION_QUERY = "select session.user_id from user_session session where session.sso_token =?";
	public static final String USER_SESSION_INSERT_QUERY = "INSERT INTO `user_session` (`sso_token`, `user_id`) VALUES (?, ?)";
	public static final String USER_SESSION_DELETE_QUERY = "delete from user_session where sso_token =?";
	public static final String USER_CHECK_QUERY = "select id from user where user.user_name = ?";
	public static final String USER_CHECK_LOGIN_QUERY = "Select id from user where user.user_name = ? and user.user_password = ?";
	public static final String USER_CREATE_QUERY = "INSERT INTO user (`user_name`, `user_password`, `is_vendor`) VALUES (?,?,?)";
	public static final String USER_DETAILS_INSERT_QUERY = "INSERT INTO `user_details` (`user_firstname`, `user_lastname`, `user_address_line1`, `user_address_line2`, `user_city`, `user_zip_code`, `user_state`, `user_id`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String USER_DETAILS_UPDATE_QUERY = "UPDATE `user_details` SET `user_firstname`=?, `user_lastname`=?, `user_address_line1`=?, `user_address_line2`=?, `user_city`=?, `user_zip_code`=?, `user_state`=? WHERE  `user_id`=?";
	public static final String USER_DETAILS_GET_QUERY = "SELECT  `user_firstname`, `user_lastname`, `user_address_line1`, `user_address_line2`, `user_city`, `user_zip_code`, `user_state`, details.user_id FROM user_details details inner join user_session session on details.user_id = session.user_id and session.sso_token = ?";
	public void registerUser(User user,CommonResponse response){
		logger.info("UserUtil: register User started");
		
		try{
			PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(USER_CHECK_QUERY);
			statement.setString(1, user.getUserName());
			ResultSet result  = statement.executeQuery();
			if(result.next()){
				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.USER_EXISTS));
			}else{
				statement = MySQLConnect.getDbCon().conn.prepareStatement(USER_CREATE_QUERY);
				statement.setString(1, user.getUserName());
				statement.setString(2, user.getPassword());
				statement.setBoolean(3, user.isVendor());
				int success = statement.executeUpdate();
				if(success  > 0){
					response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SUCCESS,"User register successfully"));
				}else{
					response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
				}
			}
		}catch(SQLException ex){
			logger.error("SQL exception while creating user", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}catch(Exception ex){
			logger.error("Exception occurred while creating user", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}
		logger.info("UserUtil: Register User ended");
	}
    public void loginUser(User user, CommonResponse response){
    	logger.info("UserDaoUtil: Do User login");
    	try{
    		PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(USER_CHECK_LOGIN_QUERY);
    		statement.setString(1, user.getUserName());
    		statement.setString(2, user.getPassword());
    		ResultSet result  = statement.executeQuery();
    		if(result.next()){
    			SsoToken  token = new SsoToken();
    			String userId = result.getString("id");
    			statement = MySQLConnect.getDbCon().conn.prepareStatement(USER_SESSION_CHECK_QUERY);
    			statement.setInt(1, Integer.parseInt(userId));
    			result  = statement.executeQuery();
    			if(result.next()){
    				//User already logged in
    				String ssoToken = result.getString("sso_token");
    				token.setSsoToken(ssoToken);
    				response.setData(token);
    				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.USER_LOGIN_EXISTS));
    			}else{
    				//Create new user session
    				statement = MySQLConnect.getDbCon().conn.prepareStatement(USER_SESSION_INSERT_QUERY);
    				String ssoToken = TokenGenerator.generateSSOToken();
    				statement.setString(1,ssoToken );
    				statement.setInt(2, Integer.parseInt(userId));
    				int success = statement.executeUpdate();
    				if(success > 0 ){
    					token.setSsoToken(ssoToken);
    					response.setData(token);
    					response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SUCCESS,"Login successful"));
    				}else{
    					response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
    				}
    			}
    			
    		}else{
    			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INAVLID_USER));
    		}
    		
    	}catch(SQLException ex){
			logger.error("SQL exception while login", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}catch(Exception ex){
			logger.error("Exception occurred while login", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}
    	
    }
    public void logoutUser(String ssoToken, CommonResponse response){
    	try{
    		logger.info("UserDaoUtil: User logout Call started");
    		PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(USER_SESSION_DELETE_QUERY);
			statement.setString(1, ssoToken);
			int success = statement.executeUpdate();
			if(success > 0){
				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SUCCESS,"User logout successfully" ));
			}else{
				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.USER_NOT_LOG_IN));
			}
    		
    	}catch(SQLException ex){
			logger.error("SQL exception while logout", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}catch(Exception ex){
			logger.error("Exception occurred while logout", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}
		logger.info("UserDaoUtil: User logout Call ended");
    }
    
    public void addUserDetails(UserDetails userDetails,CommonResponse response){
    	logger.info("UserDaoUtil: addUserDetails call started");
    	try {
    		PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(USER_FROM_SESSION_QUERY);
    		statement.setString(1,userDetails.getSsoToken());
    		ResultSet result  = statement.executeQuery();
    		if(result.next()){
    			int userId = Integer.parseInt(result.getString("user_id"));
    			statement = MySQLConnect.getDbCon().conn.prepareStatement(GET_USER_NAME_QUERY);
    			statement.setInt(1, userId);
    			ResultSet userSearchResult = statement.executeQuery();
    			if(userSearchResult.next()){
    				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"User details already available"));
    			}else{
    				statement = MySQLConnect.getDbCon().conn.prepareStatement(USER_DETAILS_INSERT_QUERY);
        			statement.setString(1, userDetails.getFirstName());
        			statement.setString(2, userDetails.getLastName());
        			statement.setString(3, userDetails.getAddressLine1());
        			statement.setString(4, userDetails.getAddressLine2());
        			statement.setString(5, userDetails.getCity());
        			statement.setString(6, userDetails.getPinCode());
        			statement.setString(7, userDetails.getState());
        			statement.setInt(8, userId);
        			int success = statement.executeUpdate();
        			if(success > 0){
        				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SUCCESS,"User details added successfully"));
        			}else{
        				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
        			}
    			}
    			
    		}else{
    			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.USER_NOT_LOG_IN));
    		}
    	}catch(SQLException ex){
			logger.error("SQL exception while adding user details", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}catch(Exception ex){
			logger.error("Exception occurred while adding user details", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}
    	logger.info("UserDaoUtil: addUserDetails call ended");
    }
    
    public void updateUserDetails(UserDetails userDetails, CommonResponse response){
    	logger.info("UserDaoUtil: Update User details call started");
    	try{
    		PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(USER_DETAILS_GET_QUERY);
    		statement.setString(1,userDetails.getSsoToken());
    		ResultSet result  = statement.executeQuery();
    		if(result.next()){
    			statement = MySQLConnect.getDbCon().conn.prepareStatement(USER_DETAILS_UPDATE_QUERY);
    			statement.setString(1, StringUtils.isEmpty(userDetails.getFirstName())?result.getString("user_firstname") : userDetails.getFirstName());
    			statement.setString(2, StringUtils.isEmpty(userDetails.getLastName())?result.getString("user_lastname") : userDetails.getLastName());
    			statement.setString(3, userDetails.getAddressLine1());
    			statement.setString(4, userDetails.getAddressLine2());
    			statement.setString(5, userDetails.getCity());
    			statement.setString(6, userDetails.getPinCode());
    			statement.setString(7, userDetails.getState());
    			statement.setInt(8, Integer.parseInt(result.getString("user_id")));
    			int success = statement.executeUpdate();
    			if(success > 0 ){
    				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SUCCESS,"User details updated successfully"));
    			}else{
    				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"User details not available"));
    			}
    		}else{
    			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.USER_NOT_LOG_IN));
    		}
    	}catch(SQLException ex){
			logger.error("SQL exception whileupdating user details", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}catch(Exception ex){
			logger.error("Exception occurred while updating user details", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}
    	logger.info("UserDaoUtil: Update User details call ended");
    }
}
 