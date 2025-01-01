package ngo.nabarun.tools.deployment_tools.auth0;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.auth0.exception.Auth0Exception;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ngo.nabarun.tools.deployment_tools.auth0.models.Auth0Config;
import ngo.nabarun.tools.deployment_tools.auth0.service.Auth0SyncService;

@ShellComponent
@ShellCommandGroup("Auth0")
public class Auth0Commands {
	
	@Autowired
	private Auth0SyncService SyncService;
	
	
    @ShellMethod(key = {"auth0-sync"})
    public void SyncAuth0Tenants(
    		@ShellOption({"-c", "--config"}) String config, 
    		@ShellOption({"-s", "--source"}) String sourceTenant,
    		@ShellOption({"-d", "--dest"}) String destTenant

    		) throws Auth0Exception, JsonMappingException, JsonProcessingException {
    	System.out.println("-----------------------------");
    	System.out.println("LOGIN - Auth0");
    	System.out.println("-----------------------------");
    	Auth0Login(config,sourceTenant,destTenant);
    	System.out.println("-----------------------------");
    	System.out.println("SYNC - RESOURCE SERVER");
    	System.out.println("-----------------------------");
    	Auth0SyncResourceServers();
    	System.out.println("-----------------------------");
    	System.out.println("SYNC - ROLES & PERMISSIONS");
    	System.out.println("-----------------------------");
    	Auth0SyncRolesAndPermissions();
    }
    
    @ShellMethod(key = {"auth0-login"})
    public void Auth0Login(
    		@ShellOption({"-c", "--config"}) String config, 
    		@ShellOption({"-s", "--source"}) String sourceTenant,
    		@ShellOption({"-d", "--dest"}) String destTenant

    		) throws Auth0Exception, JsonMappingException, JsonProcessingException {
    	ObjectMapper objectMapper= new ObjectMapper();
    	Auth0Config[] configList =objectMapper.readValue(config,Auth0Config[].class);
    	SyncService.Initialize(configList, sourceTenant, destTenant);
    }
    
    @ShellMethod(key = {"auth0-sync-roles"})
    public void Auth0SyncRolesAndPermissions() throws Auth0Exception {
    	SyncService.SyncRolesAndPermissions();
    }
    
    @ShellMethod(key = {"auth0-sync-resource-servers"})
    public void Auth0SyncResourceServers() throws Auth0Exception {
    	SyncService.SyncResourceServersAndScopes();
    }
   
}
