package ngo.nabarun.tools.deployment_tools.auth0.models;

import lombok.Data;

@Data
public class Auth0Config {
	private String tenantName;
	private String domain;
	private String clientId;
	private String clientSecret;
	private String audience;
}
