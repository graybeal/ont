package org.mmisw.ont.client;


/**
 * Info about the result of a log-in operation.
 * 
 * @author Carlos Rueda
 */
public class SignInResult  {

	private String sessionId;
	private String userId;
	private String userName;
	private String userRole;

	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;		
	}
	

	/**
	 * @return true if the role of this user indicates administrator
	 */
	public boolean isAdministrator() {
		return (userRole != null && userRole.toLowerCase().indexOf("administrator") >= 0 ) ;
	}
	/**
	 * @param userRole the userRole to set
	 */
	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
	
	public String getUserRole() {
		return userRole;
	}
	public String toString() {
		return "SignInResult{userId=" +userId+", userName=" +userName+", sessionId=" +sessionId+", role=" +userRole+ "}";
	}

}
