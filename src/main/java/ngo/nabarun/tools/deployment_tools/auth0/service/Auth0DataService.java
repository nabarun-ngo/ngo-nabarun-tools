package ngo.nabarun.tools.deployment_tools.auth0.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.Permission;
import com.auth0.json.mgmt.ResourceServer;
import com.auth0.json.mgmt.Role;
import com.auth0.json.mgmt.Scope;
import ngo.nabarun.tools.deployment_tools.auth0.models.Auth0Config;
import ngo.nabarun.tools.util.ExcelUtil;

@Component
public class Auth0DataService extends Auth0BaseService {
	private static Map<String,Integer> RoleMapping=new HashMap<String, Integer>();

	static{
		RoleMapping.put("MEMBER", 2);
		RoleMapping.put("PRESIDENT", 3);
		RoleMapping.put("VICE_PRESIDENT", 4);
		RoleMapping.put("SECRETARY", 5);
		RoleMapping.put("ASST_SECRETARY", 6);
		RoleMapping.put("GROUP_COORDINATOR", 7);
		RoleMapping.put("ASST_GROUP_COORDINATOR", 8);
		RoleMapping.put("TREASURER", 9);
		RoleMapping.put("CASHIER", 10);
		RoleMapping.put("ASSISTANT_CASHIER", 11);
		RoleMapping.put("COMMUNITY_MANAGER", 12);
		RoleMapping.put("ASST_COMMUNITY_MANAGER", 13);
		RoleMapping.put("TECHNICAL_SPECIALIST", 14);
	}

	private ManagementAPI client;

	public void Initialize(Auth0Config[] configList, String destTenant) throws Auth0Exception {
		Assert.notNull(destTenant, "Dest cannot be null or empty");
		this.client = InitManagementAPI(configList, destTenant);
	}

	public void ImportPermissionsToResourceServer(File inputFile,String identifier) throws Exception {
		List<List<String>> sheetdata = ExcelUtil.readExcelWorkBook(inputFile).get("Auth0_Role_Permission_Mapping");
		ResourceServer resourceServer = client.resourceServers().get(identifier).execute();
		ResourceServer rserver = new ResourceServer();
		List<String> new_scopes= new ArrayList<>();
		List<String> old_scopes= resourceServer.getScopes().stream().map(m->m.getValue()).toList();

		List<Scope> scopes= new ArrayList<>();
		
		for(int i=1;i<sheetdata.size();i++) {
			if(sheetdata.get(i).size() >= 2) {
				Scope scope= new Scope(sheetdata.get(i).get(0));
				scope.setDescription(sheetdata.get(i).get(1));
				scopes.add(scope);
				new_scopes.add(scope.getValue());
			}
		}
		rserver.setScopes(scopes);
		
		if(!new_scopes.equals(old_scopes)) {
			client.resourceServers().update(resourceServer.getId(), rserver);
			System.out.println("Scope updated to Resource server "+new_scopes);
		}else {
			System.out.println("No Scope updated to Resource server");
		}
		
	}

	public void AlocatePermissionsToRole(File inputFile,String identifier) throws Exception {
		List<Role> roleList = client.roles().list(null).execute().getItems();
		List<List<String>> mappingSheet = ExcelUtil.readExcelWorkBook(inputFile).get("Auth0_Role_Permission_Mapping");

		for (Role role : roleList) {
			
			List<String> newPermissions=retrieveNewPermissions(mappingSheet,role.getName());

			List<String> oldPermissions = client.roles().listPermissions(role.getId(), null).execute().getItems()
					.stream().map(m -> m.getName()).toList();

			List<Permission> permissionToAdd = getAddedItems(oldPermissions, newPermissions).stream().map(m -> {
				Permission permission = new Permission();
				permission.setName(m);
				permission.setResourceServerId(identifier);
				System.out.println("Permission '"+m+"' will be added.");
				return permission;
			}).toList();

			if (!permissionToAdd.isEmpty()) {
				client.roles().addPermissions(role.getId(), permissionToAdd);
				System.out.println("Permissions added to '"+role.getName()+"' Role.");
			}else {
				System.out.println("No Permissions to add for '"+role.getName()+"' Role.");
			}

			List<Permission> permissionToRemove = getRemovedItems(oldPermissions, newPermissions).stream().map(m -> {
				Permission permission = new Permission();
				permission.setName(m);
				permission.setResourceServerId(identifier);
				System.out.println("Permission '"+m+"' will be removed.");
				return permission;
			}).toList();

			if (!permissionToRemove.isEmpty()) {
				client.roles().removePermissions(role.getId(), permissionToRemove);
				System.out.println("Permissions removed from "+role.getName()+" Role.");
			}else {
				System.out.println("No Permissions to remove from '"+role.getName()+"' Role.");
			}
			System.out.println("--------------------------------------------------");
			Thread.sleep(3000);
		}

	}
	

	private List<String> retrieveNewPermissions(List<List<String>> sheetdata, String name) {
		List<String> permissions= new ArrayList<>();
		for(List<String> data :sheetdata) {
			if(data.size() >= 15 && data.get(RoleMapping.get(name)).equals("Y")) {
				permissions.add(data.get(0));
			}
		}
		return permissions;		
	}

	private static List<String> getAddedItems(List<String> oldList, List<String> newList) {
		List<String> added = new ArrayList<>(newList);
		added.removeAll(oldList); 
		return added;
	}

	private static List<String> getRemovedItems(List<String> oldList, List<String> newList) {
		List<String> removed = new ArrayList<>(oldList);
		removed.removeAll(newList); 
		return removed;
	}

}
