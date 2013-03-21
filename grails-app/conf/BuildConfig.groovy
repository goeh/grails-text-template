grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn"
    repositories {
        grailsHome()
        grailsCentral()
    }
    dependencies {
    }
    plugins {
        build(":tomcat:$grailsVersion",
              ":release:2.2.1") {
            export = false
        }
        runtime(":hibernate:$grailsVersion") {
            export = false
        }
    }
}
