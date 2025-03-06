package ngo.nabarun.tools.deployment_tools.auth0.models;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ServiceRunResponse {
	private LogList responsePayload;
	private final String info;
	private Date timestamp;
	private final String traceId;
	private int status;
	private List<String> messages;
	private final String version;
	
	@Data
	public static class LogList{
		private List<String> logs;
	}
}
