package ngo.nabarun.tools.deployment_tools.auth0.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.ResourceServersFilter;
import com.auth0.client.mgmt.filter.RolesFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.Permission;
import com.auth0.json.mgmt.ResourceServer;
import com.auth0.json.mgmt.Role;
import ngo.nabarun.tools.deployment_tools.auth0.models.Auth0Config;

@Component
public class Auth0SyncService {

	private ManagementAPI sourceClient;
	private ManagementAPI targetClient;

	public void Initialize(Auth0Config[] config,String source,String dest) throws Auth0Exception {
		System.out.println("Source Tenant = "+source);
		System.out.println("Destination Tenant = "+dest);

		Auth0Config sourceCred=List.of(config).stream().filter(f->f.getTenantName().equalsIgnoreCase(source)).findFirst().orElseThrow();
		Auth0Config destCred=List.of(config).stream().filter(f->f.getTenantName().equalsIgnoreCase(dest)).findFirst().orElseThrow();

		this.sourceClient = new ManagementAPI(sourceCred.getDomain(), GetToken(sourceCred).getAccessToken());
		this.targetClient = new ManagementAPI(destCred.getDomain(), GetToken(destCred).getAccessToken());
	}
	
	private TokenHolder GetToken(Auth0Config cred) throws Auth0Exception {
		AuthAPI authAPI = new AuthAPI(cred.getDomain(), cred.getClientId(), cred.getClientSecret());
		return authAPI.requestToken(cred.getAudience()).execute();
	}

	public void SyncRolesAndPermissions() {
		try {
			List<Role> sourceRoles = sourceClient.roles().list(new RolesFilter()).execute().getItems();
			List<Role> targetRoles = targetClient.roles().list(new RolesFilter()).execute().getItems();

			for (Role sourceRole : sourceRoles) {
				Optional<Role> existingRoleOpt = targetRoles.stream()
						.filter(role -> role.getName().equals(sourceRole.getName())).findFirst();

				Role targetRole;
				if (existingRoleOpt.isPresent()) {
					System.out.println("Role already exists in target tenant: " + sourceRole.getName());
					targetRole = existingRoleOpt.get(); // Use the existing role
				} else {
					Role newRole = new Role();
					newRole.setName(sourceRole.getName());
					newRole.setDescription(sourceRole.getDescription());
					targetRole = targetClient.roles().create(newRole).execute();
					System.out.println("Created role: " + sourceRole.getName() + " in target tenant.");
				}

				SyncPermissions(sourceRole, targetRole);
				Thread.sleep(2000);
			}
		} catch (Exception e) {
			System.err.println("Error syncing roles and permissions: " + e.getMessage());
		}
	}

	public void SyncPermissions(Role sourceRole, Role targetRole) {
		try {
			// Fetch permissions for source and target roles
			List<Permission> sourcePermissions = sourceClient.roles().listPermissions(sourceRole.getId(), null)
					.execute().getItems();

			List<Permission> targetPermissions = targetClient.roles().listPermissions(targetRole.getId(), null)
					.execute().getItems();

			// Find permissions to add (present in source, missing in target)
			List<Permission> permissionsToAdd = sourcePermissions.stream()
					.filter(sp -> targetPermissions.stream().noneMatch(tp -> tp.getName().equals(sp.getName())))
					.map(m->{
						m.setDescription(null);
						m.setResourceServerName(null);
						return m;
					})
					.collect(Collectors.toList());

			// Find permissions to remove (present in target, missing in source)
			List<Permission> permissionsToRemove = targetPermissions.stream()
					.filter(tp -> sourcePermissions.stream().noneMatch(sp -> sp.getName().equals(tp.getName())))
					.map(m->{
						m.setDescription(null);
						m.setResourceServerName(null);
						return m;
					}).collect(Collectors.toList());

			boolean sync = false;
			// Remove outdated permissions from the target role
			if (!permissionsToRemove.isEmpty()) {
				targetClient.roles().removePermissions(targetRole.getId(), permissionsToRemove).execute();
				List<String> permission_remove=permissionsToRemove.stream().map(m->m.getName()).toList();
				System.out.println("Removed outdated permissions for role: " + sourceRole.getName()+"\nPermissions: "+permission_remove);
				sync=true;
			}

			// Add new permissions to the target role
			if (!permissionsToAdd.isEmpty()) {
				targetClient.roles().addPermissions(targetRole.getId(), permissionsToAdd).execute();
				List<String> permission_add=permissionsToAdd.stream().map(m->m.getName()).toList();
				System.out.println("Added new permissions for role: " + sourceRole.getName()+"\nPermissions: "+permission_add);
				sync=true;
			}

			if(sync) {
				System.out.println("Permissions synced for role: " + sourceRole.getName());
			}else {
				System.out.println("Nothing to Sync for role: " + sourceRole.getName());
			}
		} catch (Exception e) {
			System.err.println("Error syncing permissions for role " + sourceRole.getName() + ": " + e.getMessage());
		}
	}

	public void SyncResourceServersAndScopes() {
		try {
			List<ResourceServer> sourceResourceServers = sourceClient.resourceServers().list(new ResourceServersFilter()).execute().getItems();

			List<ResourceServer> targetResourceServers = targetClient.resourceServers().list(new ResourceServersFilter()).execute().getItems();

			for (ResourceServer sourceServer : sourceResourceServers) {
				
				if(sourceServer.isSystem() != Boolean.TRUE) {
					Optional<ResourceServer> existingServerOpt = targetResourceServers.stream()
							.filter(server -> server.getIdentifier().equals(sourceServer.getIdentifier())).findFirst();
					
					if (existingServerOpt.isPresent()) {
						System.out.println("Resource server exists in target tenant: " + sourceServer.getName());
						ResourceServer targetServer = existingServerOpt.get();
						ResourceServer targetServerUpdateServer= new ResourceServer();
						boolean isUpdated = false;
	
						if (!targetServer.getName().equals(sourceServer.getName())) {
							targetServerUpdateServer.setName(sourceServer.getName());
							System.out.println("Resource server name will be updated to "+targetServer.getName());
							isUpdated = true;
						}
						
						List<String> scopes1=sourceServer.getScopes().stream().map(m->m.getValue()).toList();
						List<String> scopes2=targetServer.getScopes().stream().map(m->m.getValue()).toList();
						
						if (!scopes1.equals(scopes2)) {
							targetServerUpdateServer.setScopes(sourceServer.getScopes());
							List<String> scopes=targetServer.getScopes().stream().map(m->m.getValue()).toList();
							System.out.println("Resource server scope will be updated to "+String.join(",", scopes));
							isUpdated = true;
						}
	
						if (isUpdated) {
							targetClient.resourceServers().update(targetServer.getId(), targetServerUpdateServer).execute();
							System.out.println("Updated resource server: " + sourceServer.getName());
						}else {
							System.out.println("Nothing to update in resource server: " + sourceServer.getName());
						}
					} else {
						System.out.println("Cloning resource server "+sourceServer.getName());
						ResourceServer newResourceServer = sourceServer;
						newResourceServer.setId(null);
						newResourceServer=targetClient.resourceServers().create(newResourceServer).execute();
						System.out.println("Created new resource server: " + newResourceServer.getName());
					}
					Thread.sleep(2000);
				}
				
			}
		} catch (Exception e) {
			System.err.println("Error syncing resource servers and scopes: " + e.getMessage());
		}
	}

}
