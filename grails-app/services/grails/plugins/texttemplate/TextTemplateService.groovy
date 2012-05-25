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
 * under the License.
 */

package grails.plugins.texttemplate

import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

class TextTemplateService {

    def groovyPagesTemplateEngine

    LinkGenerator grailsLinkGenerator

    String text(String name, String language = null, Long tenant = null) {
        content(name, 'text/plain', language, tenant)
    }

    String html(String name, String language = null, Long tenant = null) {
        content(name, 'text/html', language, tenant)
    }

    def xml(String name, String language = null, Long tenant = null) {
        String s = content(name, 'text/xml', language, tenant)
        s ? new XmlSlurper().parseText(s) : null
    }

    def json(String name, String language = null, Long tenant = null) {
        String s = content(name, 'application/json', language, tenant)
        s ? new JsonSlurper().parseText(s) : null
    }

    String content(String name, String contentType, String language = null, Long tenant = null) {
        if (name == null) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Mandatory parameter [contentType] is missing")
        }
        def now = new Date()
        def c = TextContent.createCriteria().get {
            template {
                eq('status', TextTemplate.STATUS_PUBLISHED)
                eq('name', name)
                if (tenant) {
                    eq('tenantId', tenant)
                } else {
                    isNull('tenantId')
                }
                le('visibleFrom', now)
                ge('visibleTo', now)
            }
            eq('contentType', contentType)
            if (language) {
                eq('language', language)
            } else {
                isNull('language')
            }
        }
        if (c) {
            // If template was found, we always return something != null
            return c.text ?: ''
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

    List<String> getTemplateNames(String beginsWith, Long tenant = null) {
        TextTemplate.createCriteria().list([sort: 'name', order: 'asc']) {
            if (beginsWith) {
                ilike('name', wildcard(beginsWith))
            }
            if (tenant) {
                eq('tenantId', tenant)
            } else {
                isNull('tenantId')
            }
        }.collect {it.name}
    }

    TextTemplate template(String name, Long tenant = null) {
        if (name == null) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        TextTemplate.createCriteria().get {
            eq('name', name)
            if (tenant) {
                eq('tenantId', tenant)
            } else {
                isNull('tenantId')
            }
        }
    }

    void setStatusDisabled(String name, Long tenant = null) {
        def tmpl = template(name, tenant)
        if (tmpl) {
            tmpl.status = TextTemplate.STATUS_DISABLED
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }

    void setStatusDraft(String name, Long tenant = null) {
        def tmpl = template(name, tenant)
        if (tmpl) {
            tmpl.status = TextTemplate.STATUS_DRAFT
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }

    void setStatusPublished(String name, Long tenant = null) {
        def tmpl = template(name, tenant)
        if (tmpl) {
            tmpl.status = TextTemplate.STATUS_PUBLISHED
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }

    void setVisible(String name, Date visibleFrom, Date visibleTo, Long tenant = null) {
        def tmpl = template(name, tenant)
        if (tmpl) {
            if (visibleFrom) {
                tmpl.visibleFrom = visibleFrom
            }
            if (visibleTo) {
                tmpl.visibleTo = visibleTo
            }
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }

    def createContent(String name, String contentType, String text, String language = null, Long tenant = null) {
        if (name == null) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Mandatory parameter [contentType] is missing")
        }
        // Find or create the template
        def textTemplate = TextTemplate.createCriteria().get {
            eq('name', name)
            if (tenant) {
                eq('tenantId', tenant)
            } else {
                isNull('tenantId')
            }
        } ?: new TextTemplate(status: TextTemplate.STATUS_PUBLISHED, name: name, tenantId: tenant)

        def textContent = textTemplate.content?.find{
            it.contentType == contentType && (language ? it.language == language : it.language == null)
        }

        if (textContent) {
            // Update existing content
            textContent.text = text
        } else {
            // Create new content
            textContent = new TextContent(language: language, contentType: contentType, text: text)
            textTemplate.addToContent(textContent)
        }

        textTemplate.save(failOnError: true)

        return textContent
    }

    boolean deleteTemplate(String name, Long tenant = null) {
        if (name == null) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        // Find or create the template
        def textTemplate = TextTemplate.createCriteria().get {
            eq('name', name)
            if (tenant) {
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

    boolean deleteContent(String name, String contentType, String language = null, Long tenant = null) {
        if (name == null) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Mandatory parameter [contentType] is missing")
        }
        // Find or create the template
        def textTemplate = TextTemplate.createCriteria().get {
            eq('name', name)
            if (tenant) {
                eq('tenantId', tenant)
            } else {
                isNull('tenantId')
            }
        }
        if (!textTemplate) {
            return false
        }
        // Find or create the content.
        def textContent = TextContent.createCriteria().get {
            eq('template', textTemplate)
            eq('contentType', contentType)
            if (language) {
                eq('language', language)
            } else {
                isNull('language')
            }
        }
        if (textContent) {
            textTemplate.removeFromContent(textContent)
            textContent.delete()
            return true
        }
        return false
    }

    String applyTemplate(String templateName, String contentType, Map binding) {
        def out = new StringWriter()
        try {
            applyTemplate(out, templateName, contentType, binding)
        } catch (Exception e) {
            out << e.message
        }
        return out.toString()
    }

    void applyTemplate(Writer out, String templateName, String contentType, Map binding) {
        Long tenant = binding.tenantId ?: binding.tenant
        String language = binding.language ?: binding.lang
        def templateContent = content(templateName, contentType, language, tenant)
        if (templateContent) {
            groovyPagesTemplateEngine.createTemplate(templateContent, "${templateName}-${contentType.replace('/', '-')}").make(binding).writeTo(out)
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
