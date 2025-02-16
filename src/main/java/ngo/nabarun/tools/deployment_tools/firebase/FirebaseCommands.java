package ngo.nabarun.tools.deployment_tools.firebase;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.fasterxml.jackson.databind.ObjectMapper;

import ngo.nabarun.tools.config.DopplerProject;
import ngo.nabarun.tools.deployment_tools.firebase.service.DeployRemoteConfigService;

@ShellComponent
@ShellCommandGroup("Firebase")
public class FirebaseCommands {
	
    @ShellMethod(key = {"firebase-sync-remoteconfigs"},value = "Sync Remote configs between firebase env.")
    public void SyncAuth0Tenants(
    		@ShellOption({"-c", "--config"}) String config, 
    		@ShellOption({"-s", "--source"}) String sourceTenant,
    		@ShellOption({"-d", "--dest"}) String destTenant

    		) throws Exception {
    	ObjectMapper objectMapper= new ObjectMapper();
    	DopplerProject project =objectMapper.readValue(config,DopplerProject.class);

    	System.out.println("-----------------------------");
    	System.out.println("LOGIN - Firebase");
    	System.out.println("-----------------------------");
		DeployRemoteConfigService rc= new DeployRemoteConfigService(project,sourceTenant,destTenant);
    	System.out.println("-----------------------------");
    	System.out.println("SYNC - REMOTE CONFIGS");
    	System.out.println("-----------------------------");
    	rc.SyncRemoteConfig();
    }
}
