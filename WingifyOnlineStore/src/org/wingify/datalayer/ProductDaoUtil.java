package org.wingify.datalayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.wingify.beans.CommonResponse;
import org.wingify.beans.DeleteProduct;
import org.wingify.beans.Product;
import org.wingify.beans.ProductDetails;
import org.wingify.beans.ProductList;
import org.wingify.beans.ResponseCode;
import org.wingify.logger.LoggerFactory;
import org.wingify.util.CommonResponseStatus;

public class ProductDaoUtil {

	private static final Logger logger = LoggerFactory.getLogger();
	
	public static final String GET_USER_DETAILS_QUERY = "select user.id, if (user.is_vendor IS NULL,'false',if(user.is_vendor = 0 ,'false','true')) as is_vendor from user_session session inner join user on session.user_id = user.id and session.sso_token =?";
	public static final String GET_USER_NAME_QUERY = "select details.user_firstname, details.user_lastname from user_details details where details.user_id = ?";
	public static final String ADD_NEW_PRODUCT_QUERY = "INSERT INTO `product_details` (`product_name`, `product_description`, `product_price`, `product_stock`, `product_category`, `product_supplier_id`, `product_supplier_name`, `is_active`, `created_on`, `last_updated_on`) VALUES (?, ?, ?, ?,?, ?, ?, ?, ?, ?)";
	public static final String GET_PRODUCT_DETAILS_QUERY = "select details.product_supplier_id from product_details details where details.product_name = ? or details.id = ?";
	public static final String DELETE_PRODUCT_QUERY = "DELETE from product_details where id= ?";
	public static final String UPDATE_PRODUCT_DETAILS_QUERY = "UPDATE `product_details` SET `product_name`= ? , `product_description`=?, `product_price`=?, `product_stock`=?, `product_category`=?, `is_active`=?, `last_updated_on`=?  WHERE  `id`=?";
	public static final String GET_PRODUCT_LIST_QUERY = "SELECT `id`, `product_name`, `product_description`, `product_price`, `product_stock`, `product_category`, `product_supplier_name`, `is_active` FROM `product_details` order by id LIMIT ? OFFSET ?";
	public static final String GET_TOTAL_PRODUCT_COUNT = "select Count(*) as total_products from product_details";
	
	public void addNewProduct(ProductDetails product,CommonResponse response){
		logger.info("ProductDaoUtil: Add new product call started");
		try {
    		ResultSet result  = getUserDetails(product.getSsoToken());
    		if(result.next()){
    			boolean isVendor = Boolean.parseBoolean(result.getString("is_vendor"));
    			if(isVendor){
    				int userId = Integer.parseInt(result.getString("id"));
    				ResultSet productResult = getProductDetails(product.getProductName(), 0);
    				if(productResult.next()){
    					response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"Product already exists"));
    				}else{
    					PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(GET_USER_NAME_QUERY);
            			statement.setInt(1,userId);
                		 result  = statement.executeQuery();
                		 String vendorName = "";
                		 if(result.next()){
                			 vendorName = result.getString("user_firstname") + " " + result.getString("user_lastname");
                			 statement = MySQLConnect.getDbCon().conn.prepareStatement(ADD_NEW_PRODUCT_QUERY);
                    		 statement.setString(1, product.getProductName());
                    		 statement.setString(2, product.getProductDescription());
                    		 statement.setDouble(3, product.getProductPrice());
                    		 statement.setInt(4, product.getProductStock());
                    		 statement.setString(5, product.getProductCategory());
                    		 statement.setInt(6, userId);
                    		 statement.setString(7, vendorName);
                    		 statement.setBoolean(8, product.isActive());
                    		 statement.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
                    		 statement.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
                    		 int success = statement.executeUpdate();
                    		 if(success > 0){
                    			 response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SUCCESS,"Product added successfullly"));
                    		 }else{
                    			 response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
                    		 }
                		 }else{
                			 response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"Please update user profile as user details are not found"));
                		 }
                		 
    				}
    				
    			}else{
    				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.VENDOR_ERROR));
    			}
    			
    		}else{
    			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.USER_NOT_LOG_IN));
    		}
		}catch(SQLException ex){
			logger.error("SQL exception while adding new product", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}catch(Exception ex){
			logger.error("Exception occurred while adding new product", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}
		logger.info("ProductDaoUtil: Add new product call ended");
		
	}
	
	public void updateProductDetails(ProductDetails product,CommonResponse response){
		logger.info("ProductDaoUtil: Update product call started");
		try {
			ResultSet result = getUserDetails(product.getSsoToken());
			if(result.next()){
				int userId = Integer.parseInt(result.getString("id"));
				ResultSet productResultSet = getProductDetails("", product.getProductId());
				if(productResultSet.next()){
					int productSupplierId = Integer.parseInt(productResultSet.getString("product_supplier_id"));
					if(userId == productSupplierId){
						PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(UPDATE_PRODUCT_DETAILS_QUERY);
						 statement.setString(1, product.getProductName());
	            		 statement.setString(2, product.getProductDescription());
	            		 statement.setDouble(3, product.getProductPrice());
	            		 statement.setInt(4, product.getProductStock());
	            		 statement.setString(5, product.getProductCategory());
	            		 statement.setBoolean(6, product.isActive());
	            		 statement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
	            		 statement.setInt(8, product.getProductId());
						int success = statement.executeUpdate();
						if(success > 0){
							response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SUCCESS,"Product updated successfully"));
						}else{
							response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
						}
					}else{
						response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.UNAUTHORIZED_ACCESS));	
					}
				}else{
					response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"Product not exists"));	
				}
			}else{
				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.USER_NOT_LOG_IN));
			}
		}catch(SQLException ex){
			logger.error("SQL exception while updating product details", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}catch(Exception ex){
			logger.error("Exception occurred while updating product details", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}
		logger.info("ProductDaoUtil: Update product call ended");
	}
	
	public void deleteProduct (DeleteProduct product, CommonResponse response){
		
		logger.info("ProductDaoUtil: Delete product call started");
		try{
			ResultSet result = getUserDetails(product.getSsoToken());
			if(result.next()){
				int userId = Integer.parseInt(result.getString("id"));
				ResultSet productResultSet = getProductDetails("", product.getProductId());
				if(productResultSet.next()){
					int productSupplierId = Integer.parseInt(productResultSet.getString("product_supplier_id"));
					if(userId == productSupplierId){
						PreparedStatement statemennt = MySQLConnect.getDbCon().conn.prepareStatement(DELETE_PRODUCT_QUERY);
						statemennt.setInt(1, product.getProductId());
						int success = statemennt.executeUpdate();
						if(success > 0){
							response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SUCCESS,"Product deleted successfully"));
						}else{
							response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
						}
					}else{
						response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.UNAUTHORIZED_ACCESS));	
					}
				}else{
					response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.FAILURE,"Product not exists"));	
				}
			}else{
				response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.USER_NOT_LOG_IN));
			}
		}catch(SQLException ex){
			logger.error("SQL exception while deleting product", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}catch(Exception ex){
			logger.error("Exception occurred while deleting product", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}
		
		logger.info("ProductDaoUtil: Delete product call ended");
	}
	
	public void getProducts (int pageNo, int pageSize, CommonResponse response){
		logger.info("ProductDaoUtil: Get product list call started");
		try{
			PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(GET_PRODUCT_LIST_QUERY);
    		statement.setInt(1, pageSize);
    		statement.setInt(2, ((pageNo * pageSize)-pageSize));
    		ResultSet result = statement.executeQuery();
    		List<Product> productList = getProductList(result);
    		ProductList list = new ProductList();
    		list.setProductList(productList);
    		list.setTotalPages((getTotalProductsCount()+pageSize-1)/pageSize);
    		response.setData(list);
    		if(productList.size() > 0){
    			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.SUCCESS,"Product list retrived successfully"));
    		}else{
    			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.NO_PRODUCT_AVAILABLE));
    		}
		}catch(SQLException ex){
			logger.error("SQL exception while getting product list", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}catch(Exception ex){
			logger.error("Exception occurred while getting product list", ex);
			response.setStatus(CommonResponseStatus.getResponseStatus(ResponseCode.INTERNAL_SERVER_ERROR));
		}
		logger.info("ProductDaoUtil: Get product list ended");
	}
	
	
	/********************Private methods*********************************************************************************/
	private ResultSet getUserDetails (String ssoToken) throws SQLException{
			PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(GET_USER_DETAILS_QUERY);
    		statement.setString(1, ssoToken);
    		return statement.executeQuery();
	}
	
	private ResultSet getProductDetails (String productName, int productId)  throws SQLException{
		PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(GET_PRODUCT_DETAILS_QUERY);
		statement.setString(1, productName);
		statement.setInt(2, productId);
		return statement.executeQuery();
	}
	
	private List<Product> getProductList (ResultSet result) throws SQLException{
		 List<Product> productList = new ArrayList<Product>();
		 while(result.next()){
			 Product product = new Product();
			 productList.add(product);
			 product.setProductId(Integer.parseInt(result.getString("id")));
			 product.setProductName(result.getString("product_name"));
			 product.setProductDescription(result.getString("product_description"));
			 product.setProductPrice(Double.parseDouble(result.getString("product_price")));
			 product.setProductStock(Integer.parseInt(result.getString("product_stock")));
			 product.setActive(Boolean.parseBoolean(result.getString("is_active")));
			 product.setProductCategory(result.getString("product_category"));
			 product.setProductSupplierName(result.getString("product_supplier_name"));
			 
		 }
		 return productList;
	}
	
	private int getTotalProductsCount () throws SQLException{
		int totalProducts = 0;
		PreparedStatement statement = MySQLConnect.getDbCon().conn.prepareStatement(GET_TOTAL_PRODUCT_COUNT);
		ResultSet result = statement.executeQuery();
		if(result.next()){
			totalProducts = result.getInt("total_products");
		}
		return totalProducts;
	}
}
