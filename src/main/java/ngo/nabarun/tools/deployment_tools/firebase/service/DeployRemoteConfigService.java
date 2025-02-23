package ngo.nabarun.tools.deployment_tools.firebase.service;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.springframework.util.Assert;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.Parameter;
import com.google.firebase.remoteconfig.Template;

import ngo.nabarun.tools.config.Constants;
import ngo.nabarun.tools.config.DopplerPropertySource;


public class DeployRemoteConfigService {
	
	private Map<String, Object> sourceConfig;
	private Map<String, Object> destConfig;
	private FirebaseApp sourceApp;
	private FirebaseApp destApp;

	public DeployRemoteConfigService(String project,Map<String,String> source,Map<String,String> dest) throws Exception {
		Assert.notNull(source, "Source cannot be null or empty");
		Assert.notNull(dest, "Dest cannot be null or empty");
		String sourceTenant=source.get(Constants.doppler_env_name);
		String sourceToken=source.get(Constants.doppler_env_token);
		
		String destTenant=dest.get(Constants.doppler_env_name);
		String destToken=dest.get(Constants.doppler_env_token);
		
		System.out.println("Source Tenant = "+sourceTenant);
		System.out.println("Destination Tenant = "+destTenant);
		
		this.sourceConfig = new DopplerPropertySource(project, sourceTenant,sourceToken).loadProperties();
		this.destConfig = new DopplerPropertySource(project, destTenant,destToken).loadProperties();
		
		String sourceCred= String.valueOf(sourceConfig.get(Constants.FIREBASE_CREDENTIAL));		
		FirebaseOptions sourceOptions = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(sourceCred.getBytes())))
				.build();
		sourceApp=FirebaseApp.initializeApp(sourceOptions,sourceTenant);
        
		String destCred= String.valueOf(destConfig.get(Constants.FIREBASE_CREDENTIAL));
		FirebaseOptions destOptions = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(destCred.getBytes())))
				.build();
		destApp=FirebaseApp.initializeApp(destOptions,destTenant);

	}
	
	
	public void SyncRemoteConfig() throws Exception {
		Map<String, Parameter> parameters = FirebaseRemoteConfig.getInstance(sourceApp).getTemplate().getParameters();
		FirebaseRemoteConfig destTemplate=FirebaseRemoteConfig.getInstance(destApp);
		Template template=destTemplate.getTemplate();
		template.setParameters(parameters);
		destTemplate.validateTemplate(template);
		template=destTemplate.publishTemplate(template);
		System.out.println("Sync successful. Etag "+ template.getETag());
	}
	
}
