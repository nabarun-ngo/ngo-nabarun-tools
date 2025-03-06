package ngo.nabarun.tools.deployment_tools.auth0;

import java.io.File;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import ngo.nabarun.tools.config.Constants;
import ngo.nabarun.tools.deployment_tools.auth0.service.Auth0DataService;
import ngo.nabarun.tools.deployment_tools.auth0.service.Auth0SyncService;

@ShellComponent
@ShellCommandGroup("Auth0")
public class Auth0Commands {
	
	@Autowired
	private Auth0SyncService SyncService;
	
	@Autowired
	private Auth0DataService DataService;
	
    @ShellMethod(key = {"auth0-sync-tenants"},value = "Sync Resource servers, Roles and permissions between auth0 tenants")
    public void SyncAuth0Tenants(
    		@ShellOption({"-s", "--source"}) String sourceTenant,
    		@ShellOption({"-d", "--dest"}) String destTenant,
    		@ShellOption({"-p", "--project"}) String project,
    		@ShellOption({"-st", "--sourceToken"}) String sourceToken, 
    		@ShellOption({"-dt", "--destToken"}) String destToken
    		) throws Exception {
    	System.out.println("-----------------------------");
    	System.out.println("LOGIN - Auth0");
    	System.out.println("-----------------------------");
    	
    	Map<String, String> source = Map.of(Constants.doppler_env_name,sourceTenant,Constants.doppler_env_token,sourceToken);
    	Map<String, String> dest = Map.of(Constants.doppler_env_name,destTenant,Constants.doppler_env_token,destToken);
    	SyncService.Initialize(project, source, dest);
    	
    	System.out.println("-----------------------------");
    	System.out.println("SYNC - RESOURCE SERVER");
    	System.out.println("-----------------------------");
    	SyncService.SyncResourceServersAndScopes();
    	System.out.println("-----------------------------");
    	System.out.println("SYNC - ROLES & PERMISSIONS");
    	System.out.println("-----------------------------");
    	SyncService.SyncRolesAndPermissions();
    }
    
    
    @ShellMethod(key = {"auth0-import-users"},value = "Import Test users to tenant")
    public void Auth0InportUsers(
    		@ShellOption(value={"-d", "--dest"}) String destTenant,
    		@ShellOption({"-p", "--project"}) String project,
    		@ShellOption({"-dt", "--destToken"}) String destToken,
    		@ShellOption({"-i", "--input"}) String input,
    		@ShellOption({"-r", "--removeFirst"}) boolean removeFirst
    		) throws Exception {
    	System.out.println(destTenant);
    	System.out.println("-----------------------------");
    	System.out.println("LOGIN - Auth0");
    	System.out.println("-----------------------------");
    	
    	Map<String, String> dest = Map.of(Constants.doppler_env_name,destTenant,Constants.doppler_env_token,destToken);
    	DataService.Initialize(project, dest);

    	System.out.println("-----------------------------");
    	System.out.println("IMPORT - Users and Roles to "+destTenant+" tenant.");
    	System.out.println("-----------------------------");
    	DataService.ImportUsersAndRoles(new File(input),removeFirst);
    	System.out.println("-----------------------------");
    	
    	System.out.println("-----------------------------");
    	System.out.println("SYNC - Syncing Users and Roles with APP.");
    	System.out.println("-----------------------------");
    	DataService.syncUserDetailBetweenAuth0AndApp();
    	System.out.println("-----------------------------");
    	
    }
    
    @ShellMethod(key = {"auth0-import-data"},value = "Import permisssions to Resource servers, Import permissions to Roles from excel.")
    public void Auth0InportData(
    		@ShellOption(value={"-d", "--dest"}) String destTenant,
    		@ShellOption({"-p", "--project"}) String project,
    		@ShellOption({"-dt", "--destToken"}) String destToken,
    		@ShellOption({"-i", "--input"}) String input
    		) throws Exception {
    	System.out.println(destTenant);
    	System.out.println("-----------------------------");
    	System.out.println("LOGIN - Auth0");
    	System.out.println("-----------------------------");
    	Map<String, String> dest = Map.of(Constants.doppler_env_name,destTenant,Constants.doppler_env_token,destToken);
    	DataService.Initialize(project, dest);
    	System.out.println("-----------------------------");
    	System.out.println("IMPORT - RESOURCE SERVER SCOPE");
    	System.out.println("-----------------------------");
    	DataService.ImportPermissionsToResourceServer(new File(input));
    	System.out.println("-----------------------------");
    	System.out.println("IMPORT - PERMISSION & ROLE");
    	System.out.println("-----------------------------");
    	DataService.AlocatePermissionsToRole(new File(input));

    }
    
}
