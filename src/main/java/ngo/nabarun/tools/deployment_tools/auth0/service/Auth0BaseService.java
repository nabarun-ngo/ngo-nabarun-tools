package ngo.nabarun.tools.deployment_tools.auth0.service;

import java.util.Map;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;

import ngo.nabarun.tools.config.Constants;

public class Auth0BaseService {
	
	public ManagementAPI InitManagementAPI(Map<String, Object> config) throws Auth0Exception {
		String domain = config.get(Constants.AUTH0_DOMAIN).toString();
		String clientId = config.get(Constants.AUTH0_MANAGEMENT_CLIENT_ID).toString();
		String clientSecret = config.get(Constants.AUTH0_MANAGEMENT_CLIENT_SECRET).toString();
		String audience = config.get(Constants.AUTH0_MANAGEMENT_API_AUDIENCE).toString();
		AuthAPI authAPI = new AuthAPI(domain, clientId,clientSecret);
		TokenHolder tokenHolder= authAPI.requestToken(audience).execute();
		return new ManagementAPI(domain, tokenHolder.getAccessToken());
	}
}
