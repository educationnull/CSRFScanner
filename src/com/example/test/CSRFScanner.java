package com.example.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.jwebunit.junit.WebTester;

public class CSRFScanner {
	
	private WebTester tester;
	
	private String baseURL, scanURL, logoutURL;
	private String csrfTokenId, csrfErrorResponse;
	
	public void log(String s) {System.out.println(s); }

    @Before
    public void prepare() {
    	baseURL = "http://www.example.com";
    	scanURL = baseURL + "/pageWithForm/";
    	logoutURL = baseURL + "/logout";
    	
    	// id of hidden field carrying anti-csrf token
    	csrfTokenId = "csrfToken";
    	
	// This unique string is included in error response when server side csrf token validation fails
	// Note: It is not a good idea to explicitly show that it is a CSRF token validation error, 
    	csrfErrorResponse = "CSRFTokenValidationError";
    	
    	tester = new WebTester();
    }
    
    public void login() {
    	//get the login page 
        tester.beginAt(baseURL);
        // Set the username
        tester.setTextField("userName", "<username>");
        // set the password
        tester.setTextField("passWord", "<password>");
        // Submit login. The button name here is "login"
        tester.submit("login");
        // On login, get the page of interest to scan for csrf vulnerability
        tester.gotoPage(scanURL);
    }
    
    public void logout() {
    	tester.gotoPage(logoutURL);
    }

    @Test
    public void scan() {
    	testTokenExists();
    	testTokenHasValue();
    	testValidToken();
    	testInvalidToken();
    }
    
    /**
     * Check if csrf token exists in the form
     */
	 private void testTokenExists() {	  
		  login();
		  boolean hasCsrfToken = tester.hasElementById(csrfTokenId);
		  logout();
		  Assert.assertTrue("No Anti-CSRF token in the form", hasCsrfToken);
	 }

	 /** 
	  * Check if the token is populated
	  */
	 private void testTokenHasValue() {	  
		  login();
		  String tokenVal = tester.getElementById(csrfTokenId).getAttribute("value");
		  logout();
		  Assert.assertFalse("Anti-CSRF token has no value", tokenVal.isEmpty());
	 }
	 
	 /** 
	  * Submit the page without making any changes.
	  * Whether there is server side validation or not, we do not know what response to expect at this point.
	  * All we know is that we should NOT get the error response corresponding to CSRF token mismatch.
	  */
	 private void testValidToken() {
		  login();
		  tester.submit();
		  boolean hasErrorResponse = tester.getPageSource().contains(csrfErrorResponse);
		  logout();
		  Assert.assertFalse("Anti-CSRF Token Mismatch Error response on valid token", hasErrorResponse);
	 }
	 
	 /**
	  * In this test we modify the CSRF token and submit the page
	  * We must get the unique string specifically configured to indicate the server side validation error.
	  * If not, that indicates there is no server side validation of CSRF token.
	  */
	 private void testInvalidToken() {
		  login();
		  tester.setHiddenField(csrfTokenId, "XXX");
		  tester.submit();
		  boolean hasErrorResponse = tester.getPageSource().contains(csrfErrorResponse);
		  logout();
		  Assert.assertTrue("\n     No Anti-CSRF Token Mismatch Error response on Invalid token\n", 
			+ "    Possible cause: Server side CSRF token validation is not enabled", hasErrorResponse);		  
	 }
}
