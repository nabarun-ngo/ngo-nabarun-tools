package ngo.nabarun.tools.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ngo.nabarun.tools.config.DopplerProject.Environment;


public class DopplerPropertySource {

	private String projectName;
	private String configName;
	private String serviceToken;

	public DopplerPropertySource(String projectName, String configName, String serviceToken) {
		this.projectName = projectName;
		this.configName = configName.toLowerCase();
		this.serviceToken = serviceToken;
	}
	
	public DopplerPropertySource(DopplerProject project, String environment) {
		this.projectName=project.getProjectName();
		Environment env=project.getEnvironments().stream().filter(f-> environment.equalsIgnoreCase(f.getEnvName())).findFirst().get();
		this.configName=env.getEnvName();
		this.serviceToken=env.getToken();
	}

	public Map<String, Object> loadProperties() throws Exception {
		Map<String, Object> propertySource = new HashMap<>();

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			HttpGet httpget = new HttpGet("https://api.doppler.com/v3/configs/config/secrets?project=" + projectName
					+ "&config=" + configName + "&include_dynamic_secrets=false&include_managed_secrets=false");

			httpget.setHeader(HttpHeaders.ACCEPT, "application/json");
			httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken);

			ResponseHandler<String> responseHandler = response -> {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					HttpEntity entity = response.getEntity();
					throw new ClientProtocolException(
							"Unexpected response status: " + status + " -> " + EntityUtils.toString(entity));
				}
			};

			String responseBody = httpclient.execute(httpget, responseHandler);
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode respNode = objectMapper.readTree(responseBody);
			JsonNode secretsNode = respNode.get("secrets");
			Iterator<Map.Entry<String, JsonNode>> fields = secretsNode.fields();
			//System.out.println("Proterties:");
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				String key = field.getKey();
				String rawValue = field.getValue().get("raw").asText();
				//String rawVisibility = field.getValue().get("rawVisibility").asText();
				propertySource.put(key, rawValue);
				//System.out.println(key + " : " + (rawVisibility.equals("restricted") ? "***" : rawValue));
			}
		}
		return propertySource;
	}

}
