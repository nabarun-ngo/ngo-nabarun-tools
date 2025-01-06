package ngo.nabarun.tools.deployment_tools.auth0.service;

import java.util.List;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;

import ngo.nabarun.tools.deployment_tools.auth0.models.Auth0Config;

public class Auth0BaseService {
	
	private Auth0Config cred;

	public ManagementAPI InitManagementAPI(Auth0Config[] config,String name) throws Auth0Exception {
		this.cred=List.of(config).stream().filter(f->f.getTenantName().equalsIgnoreCase(name)).findFirst().orElseThrow();
		return new ManagementAPI(cred.getDomain(), GetToken(cred).getAccessToken());
	}
	
	private TokenHolder GetToken(Auth0Config cred) throws Auth0Exception {
		AuthAPI authAPI = new AuthAPI(cred.getDomain(), cred.getClientId(), cred.getClientSecret());
		return authAPI.requestToken(cred.getAudience()).execute();
	}
}
