package gov.nasa.jpl.labcas.data_access_api.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;

/**
 * Service to execute CRUD operations for user data versus the LabCAS Solr index.
 *
 */
public interface UserDataService {
	
	/**
	 * Method to create a new user data record in Solr.
	 * 
	 * @param httpRequest
	 * @param requestContext
	 * @param headers
	 * @param content
	 */
	public void create(HttpServletRequest httpRequest, ContainerRequestContext requestContext, 
			HttpHeaders headers, String content) throws Exception;

}
