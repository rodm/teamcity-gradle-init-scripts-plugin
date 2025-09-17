
class EnterpriseRepositoryPlugin: Plugin<Gradle> {

    private val ENTERPRISE_REPOSITORY_URL = System.getenv("REPO_URL")

    override fun apply(gradle: Gradle) {

        // ONLY USE ENTERPRISE REPO FOR DEPENDENCIES
        gradle.allprojects {

            repositories {
                // Remove all repositories not pointing to the enterprise repository url
                all {
                    if (this !is MavenArtifactRepository || url.toString() != ENTERPRISE_REPOSITORY_URL) {
                        logger.lifecycle("Repository ${(this as? MavenArtifactRepository)?.url ?: name} removed from project ${project.name}.")
                        remove(this)
                    }
                }

                // Add the enterprise repository
                add(maven {
                    name = "STANDARD_ENTERPRISE_REPO"
                    url = uri(ENTERPRISE_REPOSITORY_URL)
                    isAllowInsecureProtocol = ENTERPRISE_REPOSITORY_URL.startsWith("http://")
                })
                logger.lifecycle ("Repository ${ENTERPRISE_REPOSITORY_URL} added to project ${project.name}.")
            }
        }
    }
}

apply<EnterpriseRepositoryPlugin>()
