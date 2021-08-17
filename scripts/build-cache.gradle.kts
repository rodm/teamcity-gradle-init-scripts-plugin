
val buildCacheUrl = System.getProperty("build.cache.url")

gradle.settingsEvaluated {
    buildCache {
        local {
            isEnabled = false
        }

        remote<HttpBuildCache> {
            url = uri(buildCacheUrl)
            isAllowInsecureProtocol = true
            isEnabled = true
            isPush = true
        }
    }
}
