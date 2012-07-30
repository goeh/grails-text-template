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

package grails.plugins.texttemplate

import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.web.context.support.WebApplicationContextUtils
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.web.context.request.RequestContextHolder

class TextTemplateService {

    def groovyPagesTemplateEngine
    def currentTenant

    LinkGenerator grailsLinkGenerator

    String text(String name, String language = null) {
        content(name, 'text/plain', language)
    }

    String html(String name, String language = null) {
        content(name, 'text/html', language)
    }

    def xml(String name, String language = null) {
        String s = content(name, 'text/xml', language)
        s ? new XmlSlurper().parseText(s) : null
    }

    def json(String name, String language = null) {
        String s = content(name, 'application/json', language)
        s ? new JsonSlurper().parseText(s) : null
    }

    private List getNamePair(String name) {
        def idx = name.lastIndexOf('.')
        return idx == -1 ? [name, null] : [name.substring(0, idx), name.substring(idx + 1)]
    }

    String content(String name, String contentType = null, String language = null) {
        if (!name) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        def (templateName, contentName) = getNamePair(name)
        if (!templateName) {
            throw new IllegalArgumentException("Invalid template name [$name]")
        }
        def tenant = currentTenant?.get()
        def now = new Date()
        def result = TextContent.createCriteria().list {
            template {
                eq('status', TextTemplate.STATUS_PUBLISHED)
                eq('name', templateName)
                if (tenant != null) {
                    eq('tenantId', tenant)
                } else {
                    isNull('tenantId')
                }
                or {
                    le('visibleFrom', now)
                    isNull('visibleFrom')
                }
                or {
                    ge('visibleTo', now)
                    isNull('visibleTo')
                }
            }
            if (contentName) {
                eq('name', contentName)
            }
            if (contentType) {
                eq('contentType', contentType)
            }
            if (language) {
                eq('language', language)
            } else {
                isNull('language')
            }
            cache true
        }
        if (result) {
            def s = new StringBuilder()
            for (c in result) {
                def text = c.text
                if (text) {
                    s << text
                }
            }
            return s.toString()
        }
        return null // Returning null means template was not found
    }

    private String wildcard(String q) {
        q = q.toLowerCase()
        if (q.contains('*')) {
            return q.replace('*', '%')
        } else if (q[0] == '=') { // Exact match.
            return q[1..-1]
        } else { // Starts with is default.
            return q + '%'
        }
    }


    List<String> getTemplateNames(String beginsWith) {
        def tenant = currentTenant?.get()
        TextTemplate.createCriteria().list([sort: 'name', order: 'asc']) {
            projections {
                property('name')
            }
            if (beginsWith) {
                ilike('name', wildcard(beginsWith))
            }
            if (tenant != null) {
                eq('tenantId', tenant)
            } else {
                isNull('tenantId')
            }
            cache true
        }
    }

    List<String> getContentNames(String templateName) {
        def tenant = currentTenant?.get()
        TextContent.createCriteria().list([sort: 'name', order: 'asc']) {
            projections {
                property('name')
            }
            template {
                eq('name', templateName)
                if (tenant != null) {
                    eq('tenantId', tenant)
                } else {
                    isNull('tenantId')
                }
            }
            cache true
        }
    }

    TextTemplate template(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        def tenant = currentTenant?.get()
        TextTemplate.createCriteria().get {
            eq('name', name)
            if (tenant != null) {
                eq('tenantId', tenant)
            } else {
                isNull('tenantId')
            }
            cache true
        }
    }

    void setStatusDisabled(String name) {
        def tmpl = template(name)
        if (tmpl) {
            tmpl.status = TextTemplate.STATUS_DISABLED
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }

    void setStatusDraft(String name) {
        def tmpl = template(name)
        if (tmpl) {
            tmpl.status = TextTemplate.STATUS_DRAFT
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }


    void setStatusPublished(String name) {
        def tmpl = template(name)
        if (tmpl) {
            tmpl.status = TextTemplate.STATUS_PUBLISHED
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }

    void setVisible(String name, Date visibleFrom, Date visibleTo) {
        def tmpl = template(name)
        if (tmpl) {
            tmpl.visibleFrom = visibleFrom
            tmpl.visibleTo = visibleTo
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }

    def createContent(String name, String contentType, String text, String language = null) {
        if (name == null) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Mandatory parameter [contentType] is missing")
        }
        def (templateName, contentName) = getNamePair(name)
        def tenant = currentTenant?.get()
        // Find or create the template
        def textTemplate = TextTemplate.createCriteria().get {
            eq('name', templateName)
            if (tenant != null) {
                eq('tenantId', tenant)
            } else {
                isNull('tenantId')
            }
        } ?: new TextTemplate(status: TextTemplate.STATUS_PUBLISHED, name: templateName, tenantId: tenant)

        def textContent = textTemplate.id ? TextContent.createCriteria().get() {
            eq('template', textTemplate)
            eq('name', contentName)
            eq('contentType', contentType)
            if (language) {
                eq('language', language)
            }
        } : null

        if (textContent) {
            // Update existing content
            textContent.text = text
        } else {
            // Create new content
            textContent = new TextContent(template: textTemplate, name: contentName, language: language, contentType: contentType, text: text)
            if (textContent.validate()) {
                textTemplate.addToContent(textContent)
            } else {
                throw new RuntimeException(textContent.errors.toString())
            }
        }

        textTemplate.save(failOnError: true)
        return textContent
    }

    boolean deleteTemplate(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        def tenant = currentTenant?.get()
        // Find or create the template
        def textTemplate = TextTemplate.createCriteria().get {
            eq('name', name)
            if (tenant != null) {
                eq('tenantId', tenant)
            } else {
                isNull('tenantId')
            }
        }
        if (textTemplate) {
            textTemplate.delete()
            return true
        }
        return false
    }

    boolean deleteContent(String name, String contentType, String language = null) {
        if (name == null) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        def tenant = currentTenant?.get()
        def (templateName, contentName) = getNamePair(name)
        // Find or create the template
        def textTemplate = TextTemplate.createCriteria().get {
            eq('name', templateName)
            if (tenant != null) {
                eq('tenantId', tenant)
            } else {
                isNull('tenantId')
            }
        }
        if (!textTemplate) {
            return false
        }
        // Find or create the content.
        def result = TextContent.createCriteria().list {
            eq('template', textTemplate)
            if (contentName) {
                eq('name', contentName)
            }
            if (contentType) {
                eq('contentType', contentType)
            }
            if (language) {
                eq('language', language)
            } else {
                isNull('language')
            }
        }
        for (textContent in result) {
            textTemplate.removeFromContent(textContent)
            textContent.delete()
        }
        return !result.isEmpty()
    }

    String applyTemplate(String templateName, String contentType, Map binding) {
        def out = new StringWriter()
        try {
            applyTemplate(out, templateName, contentType, binding)
        } catch (Exception e) {
            log.error("Failed to apply template [$templateName] to binding $binding", e)
            out << e.message
        }
        return out.toString()
    }

    void applyTemplate(Writer out, String templateName, String contentType, Map binding) {
        String language = binding.language ?: binding.lang
        def templateContent = content(templateName, contentType, language)
        if (templateContent) {
            def requestAttributes = RequestContextHolder.getRequestAttributes()
            boolean unbindRequest = false
            try {
                // outside of an executing request, establish a mock version
                if (!requestAttributes) {
                    def servletContext = ServletContextHolder.getServletContext()
                    def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
                    requestAttributes = grails.util.GrailsWebUtil.bindMockWebRequest(applicationContext)
                    unbindRequest = true
                    log.info "Not in web request, created mock request: $requestAttributes"
                }
                groovyPagesTemplateEngine.createTemplate(templateContent, "${templateName}-${contentType.replace('/', '-')}").make(binding).writeTo(out)
            } finally {
                if (unbindRequest) {
                    RequestContextHolder.setRequestAttributes(null)
                }
            }
        }
    }

    /**
     * Create link to actions from outside a web request using Grails LinkGenerator.
     *
     * @param params see Grails link tag
     * @return the generated URL as String
     */
    String createLink(Map params) {
        grailsLinkGenerator.link(params)
    }
}
