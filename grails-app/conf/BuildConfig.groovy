grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.tomcat.jvmArgs = ["-Djava.awt.headless=true", "-Xms128m", "-Xmx128m", "-XX:PermSize=128m", "-XX:MaxPermSize=128m"]
grails.tomcat.nio = true

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
        //runtime ":resources:1.2.RC2"
        //runtime ":fields:1.3"
    }
}
