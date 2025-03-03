package ngo.nabarun.tools.deployment_tools.auth0.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.RolesFilter;
import com.auth0.json.mgmt.Permission;
import com.auth0.json.mgmt.ResourceServer;
import com.auth0.json.mgmt.Role;
import com.auth0.json.mgmt.Scope;
import com.auth0.json.mgmt.users.User;

import ngo.nabarun.tools.config.Constants;
import ngo.nabarun.tools.config.DopplerPropertySource;
import ngo.nabarun.tools.helper.RolePermissionExtractor;
import ngo.nabarun.tools.util.ExcelUtil;

@Component
public class Auth0DataService extends Auth0BaseService {

	private ManagementAPI client;
	private Map<String, Object> config;

	public void Initialize(String project, Map<String, String> target) throws Exception {
		Assert.notNull(target, "Dest cannot be null or empty");
		String targetTenant = target.get(Constants.doppler_env_name);
		String targetToken = target.get(Constants.doppler_env_token);
		this.config = new DopplerPropertySource(project, targetTenant, targetToken).loadProperties();
		this.client = InitManagementAPI(config);
	}

	public void ImportPermissionsToResourceServer(File inputFile) throws Exception {
		String identifier = config.get(Constants.AUTH0_RESOURCE_API_AUDIENCE).toString();
		RolePermissionExtractor extractor = new RolePermissionExtractor(inputFile, "Auth0_Role_Permission_Mapping");

		ResourceServer resourceServer = client.resourceServers().get(identifier).execute();
		ResourceServer rserver = new ResourceServer();
		List<String> new_scopes = new ArrayList<>();
		List<String> old_scopes = resourceServer.getScopes().stream().map(m -> m.getValue()).toList();

		List<Scope> scopes = new ArrayList<>();

		for (Map.Entry<String, String> entry : extractor.getPermissionDescriptions().entrySet()) {
			Scope scope = new Scope(entry.getKey());
			scope.setDescription(entry.getValue());
			scopes.add(scope);
			new_scopes.add(entry.getKey());
		}

		rserver.setScopes(scopes);

		if (!new_scopes.equals(old_scopes)) {
			List<String> updated_scopes = client.resourceServers().update(resourceServer.getId(), rserver).execute()
					.getScopes().stream().map(m -> m.getValue()).toList();
			System.out.println("Scope updated to Resource server " + updated_scopes);
		} else {
			System.out.println("No Scope updated to Resource server");
		}

	}

	public void AlocatePermissionsToRole(File inputFile) throws Exception {
		List<Role> roleList = client.roles().list(null).execute().getItems();
		RolePermissionExtractor extractor = new RolePermissionExtractor(inputFile, "Auth0_Role_Permission_Mapping");
		String identifier = config.get(Constants.AUTH0_RESOURCE_API_AUDIENCE).toString();

		for (Role role : roleList) {

			try {
				List<String> newPermissions =extractor.getRolePermissionMap().get(role.getName());

				List<String> oldPermissions = client.roles().listPermissions(role.getId(), null).execute().getItems()
						.stream().map(m -> m.getName()).toList();

				List<Permission> permissionToAdd = getAddedItems(oldPermissions, newPermissions).stream().map(m -> {
					Permission permission = new Permission();
					permission.setName(m);
					permission.setResourceServerId(identifier);
					System.out.println("Permission '" + m + "' will be added.");
					return permission;
				}).toList();

				if (!permissionToAdd.isEmpty()) {
					client.roles().addPermissions(role.getId(), permissionToAdd).execute();
					System.out.println("Permissions added to '" + role.getName() + "' Role.");
				} else {
					System.out.println("Old Permissions : [" + String.join(",", oldPermissions) + "]");
					System.out.println("New Permissions : [" + String.join(",", newPermissions) + "]");
					System.out.println("No Permissions to add for '" + role.getName() + "' Role.");
				}

				List<Permission> permissionToRemove = getRemovedItems(oldPermissions, newPermissions).stream()
						.map(m -> {
							Permission permission = new Permission();
							permission.setName(m);
							permission.setResourceServerId(identifier);
							System.out.println("Permission '" + m + "' will be removed.");
							return permission;
						}).toList();

				if (!permissionToRemove.isEmpty()) {
					client.roles().removePermissions(role.getId(), permissionToRemove).execute();
					System.out.println("Permissions removed from " + role.getName() + " Role.");
				} else {
					System.out.println("Old Permissions : [" + String.join(",", oldPermissions) + "]");
					System.out.println("New Permissions : [" + String.join(",", newPermissions) + "]");
					System.out.println("No Permissions to remove from '" + role.getName() + "' Role.");
				}
				System.out.println("--------------------------------------------------");
				Thread.sleep(3000);

			} catch (Exception e) {
				System.out.println("Exception occured while adding permission to role " + role.getName() + ". Message: "
						+ e.getMessage());
				e.printStackTrace();
			}
		}

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

	public void ImportUsersAndRoles(File inputFile, boolean removeFirst) throws Exception {
		List<List<String>> mappingSheet = ExcelUtil.readExcelWorkBook(inputFile).get("Auth0_Test_Users");
		List<Role> allRoles = client.roles().list(new RolesFilter()).execute().getItems();
		List<User> userList = client.users().list(null).execute().getItems();

		if (removeFirst) {
			System.out.println("-----------------------------");
			for (User user : userList) {
				try {
					client.users().delete(user.getId()).execute();
					System.out.println("Successfully deleted user " + user.getName() + ". UserId is " + user.getId());
					Thread.sleep(2000);
				} catch (Exception e) {
					System.out.println("Failed to delete user " + user.getName() + ". UserId is " + user.getId()
							+ " Message: " + e.getMessage());
				}

			}
			System.out.println("-----------------------------");
			userList = client.users().list(null).execute().getItems();
		}
		System.out.println("-----------------------------");

		for (int i = 1; i < mappingSheet.size(); i++) {
			List<String> userInfo = mappingSheet.get(i);
			String firstName = userInfo.get(0);
			String lastName = userInfo.get(1);
			String email = userInfo.get(2);
			String password = userInfo.get(4);
			String[] roles = userInfo.get(3).split(",");
			Optional<User> existingUser = userList.stream().filter(f -> f.getEmail().equals(email)).findFirst();

			if (!existingUser.isPresent()) {
				System.out.println("-----------------------------");
				boolean userCreated = false;

				User user = new User("Username-Password-Authentication");
				user.setEmail(email);
				user.setEmailVerified(true);
				user.setGivenName(firstName);
				user.setFamilyName(lastName);
				user.setName(firstName + " " + lastName);
				user.setPassword(password.toCharArray());
				try {
					user = client.users().create(user).execute();
					System.out.println("Successfully created user " + user.getName() + ". UserId is " + user.getId());
					Thread.sleep(2000);
					userCreated = true;
				} catch (Exception e) {
					System.out.println("Failed create user " + user.getName() + " Message: " + e.getMessage());
				}

				if (userCreated) {
					for (String role : roles) {
						try {
							List<String> roleIds = allRoles.stream().filter(f -> f.getName().equalsIgnoreCase(role))
									.map(m -> m.getId()).collect(Collectors.toList());
							client.users().addRoles(user.getId(), roleIds).execute();
							System.out.println("Successfully added role " + role + " to user " + user.getName()
									+ ". UserId is " + user.getId());
						} catch (Exception e) {
							System.out.println("Failed add role " + role + " to user " + user.getName() + " Message: "
									+ e.getMessage());
						}
					}
				}
				System.out.println("-----------------------------");
			} else {
				System.out.println("User already exists with email " + email);
			}
		}
		System.out.println("-----------------------------");
	}

	public void SyncUserDetailBetweenAuth0AndApp() {
		Object APP_URL = config.get(Constants.APP_URL);
		if (APP_URL == null) {
			System.out.println("[ERROR] APP_URL is found null. Please manually Sync users in DB.");
			return;
		}

		String baseApiUrl = APP_URL.toString();
		String apiKey = config.get(Constants.APP_ACCESS_TOKEN).toString();

		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost httpPost = new HttpPost(baseApiUrl + "/api/admin/service/run");

			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("X-Api-Key", apiKey);
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setHeader("Correlation-Id", UUID.randomUUID().toString());

			String jsonBody = "{ \"name\": \"SYNC_USERS\", \"parameters\": { \"sync_role\": \"Y\" } }";
			httpPost.setEntity(new StringEntity(jsonBody));

			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();

			if (responseEntity != null) {
				String responseBody = EntityUtils.toString(responseEntity);
				System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
				System.out.println("Response Body: " + responseBody);
			}

		} catch (Exception e) {
			System.out.println("Exception occured while Syncing user : " + e.getMessage());
		}
	}

}
