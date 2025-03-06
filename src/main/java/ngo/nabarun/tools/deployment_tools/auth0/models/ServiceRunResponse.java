package ngo.nabarun.tools.deployment_tools.auth0.models;

import java.util.List;

import lombok.Data;

@Data
public class ServiceRunResponse {
	private LogList responsePayload;
	
	@Data
	public static class LogList{
		private List<String> logs;
	}
}
