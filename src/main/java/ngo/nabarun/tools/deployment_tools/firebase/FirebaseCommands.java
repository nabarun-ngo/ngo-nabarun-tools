package ngo.nabarun.tools.deployment_tools.firebase;

import java.util.Map;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import ngo.nabarun.tools.config.Constants;
import ngo.nabarun.tools.deployment_tools.firebase.service.DeployRemoteConfigService;

@ShellComponent
@ShellCommandGroup("Firebase")
public class FirebaseCommands {
	
    @ShellMethod(key = {"firebase-sync-remoteconfigs"},value = "Sync Remote configs between firebase env.")
    public void SyncAuth0Tenants(
    		@ShellOption({"-s", "--source"}) String sourceTenant,
    		@ShellOption({"-d", "--dest"}) String destTenant,
    		@ShellOption({"-p", "--project"}) String project,
    		@ShellOption({"-st", "--sourceToken"}) String sourceToken, 
    		@ShellOption({"-dt", "--destToken"}) String destToken
    		) throws Exception {
   
    	System.out.println("-----------------------------");
    	System.out.println("LOGIN - Firebase");
    	System.out.println("-----------------------------");
    	Map<String, String> source = Map.of(Constants.doppler_env_name,sourceTenant,Constants.doppler_env_token,sourceToken);
    	Map<String, String> dest = Map.of(Constants.doppler_env_name,destTenant,Constants.doppler_env_token,destToken);
		DeployRemoteConfigService rc= new DeployRemoteConfigService(project,source,dest);
    	System.out.println("-----------------------------");
    	System.out.println("SYNC - REMOTE CONFIGS");
    	System.out.println("-----------------------------");
    	rc.SyncRemoteConfig();
    }
}
