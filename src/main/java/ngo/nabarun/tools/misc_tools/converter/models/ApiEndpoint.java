package ngo.nabarun.tools.misc_tools.converter.models;

import java.util.List;

public class ApiEndpoint {
	private String path;
    private String method;
    private String summary;
    private String description;
    private List<String> tags;
    private List<String> authorityScopes; // Updated to store multiple scopes

    // Constructor
    public ApiEndpoint(String path, String method, String summary, String description, List<String> tags, List<String> authorityScopes) {
        this.path = path;
        this.method = method;
        this.summary = summary;
        this.description = description;
        this.tags = tags;
        this.authorityScopes = authorityScopes;
    }

    // Getters and Setters
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getAuthorityScopes() {
        return authorityScopes;
    }

    public void setAuthorityScopes(List<String> authorityScopes) {
        this.authorityScopes = authorityScopes;
    }

    @Override
    public String toString() {
        return "ApiEndpoint{" +
                "path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                ", authorityScopes=" + authorityScopes +
                '}';
    }
}