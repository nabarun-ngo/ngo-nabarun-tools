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
import ngo.nabarun.tools.config.DopplerProject;
import ngo.nabarun.tools.config.DopplerPropertySource;


public class DeployRemoteConfigService {
	
	private Map<String, Object> sourceConfig;
	private Map<String, Object> destConfig;
	private FirebaseApp sourceApp;
	private FirebaseApp destApp;

	public DeployRemoteConfigService(DopplerProject config,String source,String dest) throws Exception {
		Assert.notNull(source, "Source cannot be null or empty");
		Assert.notNull(dest, "Dest cannot be null or empty");

		System.out.println("Source = "+source);
		System.out.println("Destination = "+dest);
		
		this.sourceConfig = new DopplerPropertySource(config, source).loadProperties();
		this.destConfig = new DopplerPropertySource(config, dest).loadProperties();
		
		String sourceCred= String.valueOf(sourceConfig.get(Constants.FIREBASE_CREDENTIAL));		
		FirebaseOptions sourceOptions = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(sourceCred.getBytes())))
				.build();
		sourceApp=FirebaseApp.initializeApp(sourceOptions,source);
        
		String destCred= String.valueOf(destConfig.get(Constants.FIREBASE_CREDENTIAL));
		FirebaseOptions destOptions = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(destCred.getBytes())))
				.build();
		destApp=FirebaseApp.initializeApp(destOptions,dest);

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
