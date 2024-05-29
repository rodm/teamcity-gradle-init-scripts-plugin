
rootProject.name = "gradle-init-scripts"

includeBuild ("build-logic")

include ("common")
include ("agent")
include ("server")

rootProject.children.forEach { project ->
    project.name = "${rootProject.name}-${project.name}"
}
