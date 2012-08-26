/*
 * Copyright 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class TextTemplateGrailsPlugin {
    def version = "1.0-SNAPSHOT"
    def grailsVersion = "2.0 > *"
    def dependsOn = [:]
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "src/groovy/grails/plugins/texttemplate/TestCurrentTenant.groovy",
            "src/templates/text/**/*"
    ]
    def title = "Text Template Plugin"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
This plugin provides management and access to text templates
that can be used anywhere in your Grails application.
For example email templates, dynamic page content, etc.
An administration UI is provided where administrators can create and edit text templates.
'''
    def documentation = "http://grails.org/plugin/text-template"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-text-template/issues"]
    def scm = [url: "https://github.com/goeh/grails-text-template"]

    String watchedResources = "file:./src/templates/text/**/*.*".toString()

    def doWithApplicationContext = { applicationContext ->
        def templates
        if (application.warDeployed) {
            templates = applicationContext.getResources("**/WEB-INF/templates/text/**/*.*")?.toList()
        } else {
            templates = plugin.watchedResources
        }

        if (templates) {
            def textTemplateService = applicationContext.getBean("textTemplateService")
            for (resource in templates) {
                textTemplateService.addContentFromFile(resource.file)
            }
        }
    }

    def onChange = { event ->
        def context = event.ctx
        if (!context) {
            log.debug("Application context not found. Can't reload")
            return
        }
    }
}
