package ngo.nabarun.tools.deployment_tools.auth0;

import java.io.File;

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
import ngo.nabarun.tools.deployment_tools.auth0.service.Auth0DataService;
import ngo.nabarun.tools.deployment_tools.auth0.service.Auth0SyncService;

@ShellComponent
@ShellCommandGroup("Auth0")
public class Auth0Commands {
	
	@Autowired
	private Auth0SyncService SyncService;
	
	@Autowired
	private Auth0DataService DataService;
	
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
    	SyncService.SyncResourceServersAndScopes();
    	System.out.println("-----------------------------");
    	System.out.println("SYNC - ROLES & PERMISSIONS");
    	System.out.println("-----------------------------");
    	SyncService.SyncRolesAndPermissions();
    }
    
    @ShellMethod(key = {"auth0-login"})
    public void Auth0Login(
    		@ShellOption({"-c", "--config"}) String config, 
    		@ShellOption(value={"-s", "--source"},defaultValue = "__NONE__") String sourceTenant,
    		@ShellOption(value={"-d", "--dest"}) String destTenant

    		) throws Auth0Exception, JsonMappingException, JsonProcessingException {
    	ObjectMapper objectMapper= new ObjectMapper();
    	Auth0Config[] configList =objectMapper.readValue(config,Auth0Config[].class);
    	if(sourceTenant == null || sourceTenant.equals("__NONE__")) {
        	DataService.Initialize(configList, destTenant);
    	}else {
        	SyncService.Initialize(configList, sourceTenant, destTenant);
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
    
    @ShellMethod(key = {"auth0-import-data"})
    public void Auth0InportData(

    		@ShellOption({"-c", "--config"}) String config, 
    		@ShellOption(value={"-d", "--dest"}) String destTenant,
    		@ShellOption({"-i", "--input"}) String input,
    		@ShellOption({"-r", "--rs"}) String rs
    		) throws Exception {
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
