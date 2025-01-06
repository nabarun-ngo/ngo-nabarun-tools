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
		Assert.isNull(destTenant, "Dest cannot be null or empty");
		this.client = InitManagementAPI(configList, destTenant);
	}

	public void ImportPermissionsToResourceServer(File inputFile,String identifier) throws Exception {
		List<List<String>> sheetdata = ExcelUtil.readExcelWorkBook(inputFile).get("Auth0_Role_Permission_Mapping");
		
		ResourceServer resourceServer = client.resourceServers().get(identifier).execute();
		ResourceServer rserver = new ResourceServer();
		List<Scope> scopes= new ArrayList<>();
		for(List<String> data :sheetdata) {
			Scope scope= new Scope(data.get(0));
			scope.setDescription(data.get(1));
			scopes.add(scope);
		}
		rserver.setScopes(scopes);
		client.resourceServers().update(resourceServer.getId(), rserver);
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
				System.out.println("Permission will be added " + m);
				return permission;
			}).toList();

			if (!permissionToAdd.isEmpty()) {
				client.roles().addPermissions(role.getId(), permissionToAdd);
			}

			List<Permission> permissionToRemove = getRemovedItems(oldPermissions, newPermissions).stream().map(m -> {
				Permission permission = new Permission();
				permission.setName(m);
				permission.setResourceServerId(identifier);
				System.out.println("Permission will be removed " + m);
				return permission;
			}).toList();

			if (!permissionToRemove.isEmpty()) {
				client.roles().removePermissions(role.getId(), permissionToRemove);
			}
		}

	}
	

	private List<String> retrieveNewPermissions(List<List<String>> sheetdata, String name) {
		List<String> permissions= new ArrayList<>();
		for(List<String> data :sheetdata) {
			if(data.get(RoleMapping.get(name)).equals("Y")) {
				permissions.add(data.get(0));
			}
		}
		return permissions;		
	}

	private static List<String> getAddedItems(List<String> oldList, List<String> newList) {
		List<String> added = new ArrayList<>(newList);
		added.removeAll(oldList); // Keep only items that are in newList but not in oldList
		return added;
	}

	private static List<String> getRemovedItems(List<String> oldList, List<String> newList) {
		List<String> removed = new ArrayList<>(oldList);
		removed.removeAll(newList); // Keep only items that are in oldList but not in newList
		return removed;
	}

}
