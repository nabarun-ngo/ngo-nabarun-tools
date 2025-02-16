package ngo.nabarun.tools.config;
import java.util.List;

public class DopplerProject {
    private String projectName;
    private List<Environment> environments;

    public DopplerProject() {}

    public DopplerProject(String projectName, List<Environment> environments) {
        this.projectName = projectName;
        this.environments = environments;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }
    
    public static class Environment {
        private String envName;
        private String token;

        public Environment() {}

        public Environment(String envName, String token) {
            this.envName = envName;
            this.token = token;
        }

        public String getEnvName() {
            return envName;
        }

        public void setEnvName(String envName) {
            this.envName = envName;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}


