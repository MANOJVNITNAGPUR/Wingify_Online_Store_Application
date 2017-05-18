package org.wingify.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.wingify.beans.CommonResponse;
import org.wingify.beans.DeleteProduct;
import org.wingify.beans.ProductDetails;
import org.wingify.beans.ResponseCode;
import org.wingify.beans.SsoToken;
import org.wingify.beans.User;
import org.wingify.beans.UserDetails;
import org.wingify.datalayer.ProductDaoUtil;
import org.wingify.datalayer.UserDaoUtil;
import org.wingify.logger.LoggerFactory;
import org.wingify.util.CommonResponseStatus;

@Path("/")
public class WingifyService {

	private static final Logger logger = LoggerFactory.getLogger();
	UserDaoUtil userUtil = new UserDaoUtil();
	ProductDaoUtil productUtil = new ProductDaoUtil();

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String checkService() {
		return "Wingify online store service is running ...";
	}

	@POST
	@Path("/registeruser")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonResponse getUsers(@FormParam("userName") String userName,
			@FormParam("password") String password,
			@FormParam("isVendor") boolean isVendor) {
		logger.info("WingifyService: Register user started");
		CommonResponse response = new CommonResponse();
		if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)) {
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.LOGIN_BAD_REQUEST));
		} else {
			User user = new User();
			user.setUserName(userName);
			user.setPassword(password);
			user.setVendor(isVendor);
			userUtil.registerUser(user, response);
		}
		logger.info("WingifyService: Register user ended");
		return response;
	}

	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonResponse doLogin(@FormParam("userName") String userName,
			@FormParam("password") String password) {
		logger.info("WingifyService: Do User login");
		CommonResponse response = new CommonResponse();
		if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)) {
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.LOGIN_BAD_REQUEST));
		} else {
			User user = new User();
			user.setUserName(userName);
			user.setPassword(password);
			userUtil.loginUser(user, response);
		}
		logger.info("WingifyService: User login ended");
		return response;
	}

	@POST
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonResponse doLogin(SsoToken token) {
		logger.info("WingifyService: User logout started");
		CommonResponse response = new CommonResponse();
		if (StringUtils.isEmpty(token.getSsoToken())) {
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SECURE_TOKEN_EMPTY));
		} else {
			userUtil.logoutUser(token.getSsoToken(), response);
		}
		logger.info("WingifyService: User logout ended");
		return response;
	}

	@POST
	@Path("/adduserdetails")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CommonResponse addUserDetails(UserDetails userDetails) {
		logger.info("WingifyService: Add user details started");
		CommonResponse response = new CommonResponse();
		if (StringUtils.isEmpty(userDetails.getSsoToken())) {
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SECURE_TOKEN_EMPTY));
		} else if (StringUtils.isEmpty(userDetails.getFirstName())|| StringUtils.isEmpty(userDetails.getLastName())) {
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"User first name or last name can not be empty"));
		} else {
			userUtil.addUserDetails(userDetails, response);
		}
		logger.info("WingifyService: Add user details ended");
		return response;

	}
	
	@POST
	@Path("/updateuserdetails")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CommonResponse updateUserDetails(UserDetails userDetails) {
		logger.info("WingifyService: Update user details started");
		CommonResponse response = new CommonResponse();
		if (StringUtils.isEmpty(userDetails.getSsoToken())) {
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SECURE_TOKEN_EMPTY));
		} else {
			userUtil.updateUserDetails(userDetails, response);
		}
		logger.info("WingifyService: Update user details ended");
		return response;

	}
	
	@POST
	@Path("/addnewproduct")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CommonResponse addNewProduct(ProductDetails product){
		logger.info("WingifyService: Add new product call stared");
		CommonResponse response = new CommonResponse();
		if (StringUtils.isEmpty(product.getSsoToken())) {
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SECURE_TOKEN_EMPTY));
		}else if(StringUtils.isEmpty(product.getProductName())){
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"Product name can not be empty"));
		}else{
			productUtil.addNewProduct(product, response);
		}
		logger.info("WingifyService: Add new product call ended");
		return response;
	}
	
	@POST
	@Path("/updateproductdetails")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CommonResponse updateProductDetails(ProductDetails product){
		logger.info("WingifyService: Update product details call stared");
		CommonResponse response = new CommonResponse();
		if (StringUtils.isEmpty(product.getSsoToken())) {
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SECURE_TOKEN_EMPTY));
		}else if(product.getProductId() == 0){
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"Product id can not be empty"));
		}else{
			productUtil.updateProductDetails(product, response);
		}
		logger.info("WingifyService: Update product details call ended");
		return response;
	}
	
	@POST
	@Path("/deleteproduct")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CommonResponse deleteProduct(DeleteProduct product){
		logger.info("WingifyService: Delete product details call stared");
		CommonResponse response = new CommonResponse();
		if (StringUtils.isEmpty(product.getSsoToken())) {
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SECURE_TOKEN_EMPTY));
		}else if(product.getProductId() == 0){
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"Product id can not be empty"));
		}else{
			productUtil.deleteProduct(product, response);
		}
		logger.info("WingifyService: Delete product details call ended");
		return response;
	}
	
	@GET
	@Path("/getproducts")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonResponse getProducts(@QueryParam("pageNo") int pageNo, @QueryParam("pageSize")int pageSize){
		logger.info("WingifyService: Delete product details call stared");
		CommonResponse response = new CommonResponse();
		 if(pageNo == 0 || pageSize == 0){
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"Page number or Page size can not be empty"));
		}else{
			productUtil.getProducts(pageNo, pageSize, response);
		}
		logger.info("WingifyService: Delete product details call ended");
		return response;
	}
	
}
