package org.mmisw.ont.client;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.client.util.XmlAccessor;


/** 
 * A helper to create/update a user account.
 * 
 * @author Carlos Rueda
 */
class UserAccountManager {
	/** for the REST call */
	private static final String USERS     = "/users";

	private final Log log = LogFactory.getLog(UserAccountManager.class);
	
	private String userId;
	private Map<String,String> values;
	
	
	/**
	 * Constructor.
	 * @param create true to create new account; false to update existing account.
	 * @param values
	 */
	UserAccountManager(Map<String,String> values) {
		this.values = values;
		this.userId = values.get("id");
	}
	
	
	/** makes the request and return the response from the server 
	 * @throws Exception */
	private String doPost() throws Exception {
		String applicationid = "4ea81d74-8960-4525-810b-fa1baab576ff";
		log.info("applicationid=" +applicationid);

		String aquaportalRestUrl = OntClientUtil.getAquaportalRestUrl();
		String restUrl = aquaportalRestUrl + USERS;
		
		values.put("applicationid", applicationid);

		
		if ( userId != null ) {
			values.put("method", "PUT");
			restUrl += "/" + userId;
		}
		restUrl += "?&applicationid=" +applicationid;
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for ( String key : values.keySet() ) {
			String value = values.get(key);
			if ( value != null ) {
				pairs.add(new NameValuePair(key, value));
			}
		}
		NameValuePair[] data = pairs.toArray(new NameValuePair[pairs.size()]);

		PostMethod method = new PostMethod(restUrl);
		method.setRequestBody(data);

		log.info("preparing to call REST URL = " +restUrl);
		

		try {
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			log.info("Executing " +method.getName()+ " ...");

			int status = client.executeMethod(method);
			
			String msg = method.getResponseBodyAsString();
			
			if (status == HttpStatus.SC_OK) {
				log.info("creation/update complete, response=[" + msg + "]");
			} 
			else {
				String statusText = HttpStatus.getStatusText(status);
				log.info("creation/update failed, status text=" + statusText);
				log.info("Authentication failed, response=" + msg);
				if ( msg == null ) {
					msg = statusText;
				}
			}
			
			return msg;
		} 
		finally {
			method.releaseConnection();
		}
	}
	
	/**
	 * @return
	 * @throws Exception 
	 */
	SignInResult doIt() throws Exception  {
		String response = doPost();

		response = response.replaceAll("\\s+", " ");
		log.info("----response=" +response);
		
	
		XmlAccessor xa = new XmlAccessor(response);
		
		if ( xa.containsTag("error") ) {
			throw new Exception("Could not create/update account");
		}
		
		if ( xa.containsTag("errorCode") ) {
			if ( response.contains("Duplicate") ) {
				throw new Exception("Please try a different username");
			}
			else {
				throw new Exception("Could not create/update account. Please try again later.");
			}
		}
		
		// Assign appropriate values to loginResult object
		String sessionId = xa.getString("success/sessionId");
		String id = xa.getString("success/data/user/id");
		String username = xa.getString("success/data/user/username");
		String role = xa.getString("success/data/user/roles/string");
//		String email = xa.getString("success/data/user/email");
//		String firstname = xa.getString("success/data/user/firstname");
//		String lastname = xa.getString("success/data/user/lastname");
//		String dateCreate = xa.getString("success/data/user/accessDate");

		// During account update, the roles are not reported (<roles/>); so, do not
		// check if role is emty.
		if ( sessionId == null || sessionId.trim().length() == 0
		||   id == null || id.trim().length() == 0
		||   username == null || username.trim().length() == 0
//		||   role == null || role.trim().length() == 0
		) {
			String error;
			if ( ! xa.containsTag("success") ) {
				// unexpected response.
				error = "Unexpected: server did not respond with a success nor an error message. Please try again later.";
			}
			else {
				error = "Could not parse response from registry server. Please try again later. response=" +response;
			}
			log.error(error);
			log.error("sessionId=" +sessionId+ ", id=" +id+ ", username=" +username);
			throw new Exception(error);
		}
		SignInResult signInResult = new SignInResult();
		signInResult.setSessionId(sessionId);
		signInResult.setUserId(id);
		signInResult.setUserName(username);
		signInResult.setUserRole(role);

		return signInResult;
		
	}
}
