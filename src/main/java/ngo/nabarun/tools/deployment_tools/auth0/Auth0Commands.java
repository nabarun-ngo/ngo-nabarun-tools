package ngo.nabarun.tools.deployment_tools.auth0;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.auth0.exception.Auth0Exception;
import com.fasterxml.jackson.databind.ObjectMapper;

import ngo.nabarun.tools.config.DopplerProject;
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
    		@ShellOption({"-c", "--config"}) String config, 
    		@ShellOption({"-s", "--source"}) String sourceTenant,
    		@ShellOption({"-d", "--dest"}) String destTenant

    		) throws Exception {
    	System.out.println("-----------------------------");
    	System.out.println("LOGIN - Auth0");
    	System.out.println("-----------------------------");
    	Auth0Login(config,sourceTenant,destTenant);
    	System.out.println("-----------------------------");
    	System.out.println("SYNC - RESOURCE SERVER");
    	System.out.println("-----------------------------");
    	SyncService.SyncResourceServersAndScopes();
    	System.out.println("-----------------------------");
    	System.out.println("SYNC - ROLES & PERMISSIONS");
    	System.out.println("-----------------------------");
    	SyncService.SyncRolesAndPermissions();
    }
    
    @ShellMethod(key = {"auth0-login"},value = "Login to auth0 server tenants")
    public void Auth0Login(
    		@ShellOption({"-c", "--config"}) String config, 
    		@ShellOption(value={"-s", "--source"},defaultValue = "__NONE__") String sourceTenant,
    		@ShellOption(value={"-d", "--dest"}) String destTenant

    		) throws Exception {
    	ObjectMapper objectMapper= new ObjectMapper();
    	DopplerProject project =objectMapper.readValue(config,DopplerProject.class);
    	if(sourceTenant == null || sourceTenant.equals("__NONE__")) {
        	DataService.Initialize(project, destTenant);
    	}else {
        	SyncService.Initialize(project, sourceTenant, destTenant);
    	}
    }
    
    @ShellMethod(key = {"auth0-sync-roles"})
    public void Auth0SyncRolesAndPermissions() throws Auth0Exception {
    	SyncService.SyncRolesAndPermissions();
    }
    
    @ShellMethod(key = {"auth0-sync-resource-servers"})
    public void Auth0SyncResourceServers() throws Auth0Exception {
    	SyncService.SyncResourceServersAndScopes();
    }
    
    @ShellMethod(key = {"auth0-import-users"},value = "Import Test users to tenant")
    public void Auth0InportUsers(

    		@ShellOption({"-c", "--config"}) String config, 
    		@ShellOption(value={"-d", "--dest"}) String destTenant,
    		@ShellOption({"-i", "--input"}) String input,
    		@ShellOption({"-r", "--removeFirst"}) boolean removeFirst
    		) throws Exception {
    	System.out.println(destTenant);
    	System.out.println("-----------------------------");
    	System.out.println("LOGIN - Auth0");
    	System.out.println("-----------------------------");
    	Auth0Login(config,null,destTenant);
    	System.out.println("-----------------------------");
    	System.out.println("IMPORT - Users and Roles to "+destTenant+" tenant.");
    	System.out.println("-----------------------------");
    	DataService.ImportUsersAndRoles(new File(input),removeFirst);
    	System.out.println("-----------------------------");
    	
    }
    
    @ShellMethod(key = {"auth0-import-data"},value = "Import permisssions to Resource servers, Import permissions to Roles from excel.")
    public void Auth0InportData(

    		@ShellOption({"-c", "--config"}) String config, 
    		@ShellOption(value={"-d", "--dest"}) String destTenant,
    		@ShellOption({"-i", "--input"}) String input,
    		@ShellOption({"-r", "--rs"}) String rs
    		) throws Exception {
    	System.out.println(destTenant);
    	System.out.println("-----------------------------");
    	System.out.println("LOGIN - Auth0");
    	System.out.println("-----------------------------");
    	Auth0Login(config,null,destTenant);
    	System.out.println("-----------------------------");
    	System.out.println("IMPORT - RESOURCE SERVER SCOPE");
    	System.out.println("-----------------------------");
    	DataService.ImportPermissionsToResourceServer(new File(input),rs);
    	System.out.println("-----------------------------");
    	System.out.println("IMPORT - PERMISSION & ROLE");
    	System.out.println("-----------------------------");
    	DataService.AlocatePermissionsToRole(new File(input),rs);

    }
    
}
