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
        grailsCentral()
        //mavenCentral()
        //mavenLocal()
    }
    dependencies {
    }
    plugins {
        build(":tomcat:$grailsVersion",
              ":release:2.0.4") {
            export = false
        }
        runtime(":hibernate:$grailsVersion") {
            export = false
        }
        runtime ":resources:1.2.RC2"
        runtime ":fields:1.3"
        runtime ":jquery:1.8.0"
        runtime ":ckeditor:3.6.3.0"
        runtime(":twitter-bootstrap:latest.integration") {
            excludes 'resources'
        }
    }
}
