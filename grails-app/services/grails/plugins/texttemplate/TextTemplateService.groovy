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
import org.springframework.core.io.ByteArrayResource
import org.springframework.web.context.support.WebApplicationContextUtils
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils
import java.text.Normalizer

class TextTemplateService {

    def grailsApplication
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
        concatenate(getContentList(name, contentType, language))
    }

    protected String concatenate(List<TextContent> result) {
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

    protected List<TextContent> getContentList(String name, String contentType = null, String language = null) {
        if (!name) {
            throw new IllegalArgumentException("Mandatory parameter [name] is missing")
        }
        def (templateName, contentName) = getNamePair(name)
        if (!templateName) {
            throw new IllegalArgumentException("Invalid template name [$name]")
        }
        def tenant = currentTenant?.get()
        def now = new Date()
        def result = findContent(tenant, templateName, contentName, contentType, language, now)
        if (!result) {
            if (language) {
                // Try without language.
                result = findContent(tenant, templateName, contentName, contentType, null, now)
            }
            if (!result) {
                def defaultTenant = grailsApplication.config.textTemplate.defaultTenant
                if (defaultTenant && defaultTenant != tenant) {
                    result = findContent(defaultTenant, templateName, contentName, contentType, language, now)
                    if (language) {
                        // Try without language.
                        result = findContent(defaultTenant, templateName, contentName, contentType, null, now)
                    }
                }
            }
        }
        result
    }

    private List<TextContent> findContent(Number tenant, String templateName, String contentName, String contentType, String language, Date now) {
        if (log.isDebugEnabled()) {
            log.debug "findContent(tenant=$tenant, template=$templateName, content=$contentName, type=$contentType, lang=$language, date=$now)"
        }
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
            //cache true
        }
        if (log.isDebugEnabled()) {
            if (result) {
                log.debug "Found content: ${StringUtils.abbreviate(result.head().text, 40)}"
            } else {
                log.debug "No content found"
            }
        }
        return result
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
            //cache true
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
            //cache true
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
            //cache true
        }
    }

    void setStatusDisabled(String name) {
        def tmpl = template(name)
        if (tmpl) {
            tmpl.status = TextTemplate.STATUS_DISABLED
            groovyPagesTemplateEngine.clearPageCache()
            log.debug "Disabled template [$name]"
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }

    void setStatusDraft(String name) {
        def tmpl = template(name)
        if (tmpl) {
            tmpl.status = TextTemplate.STATUS_DRAFT
            groovyPagesTemplateEngine.clearPageCache()
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }


    void setStatusPublished(String name) {
        def tmpl = template(name)
        if (tmpl) {
            tmpl.status = TextTemplate.STATUS_PUBLISHED
            log.debug "Published template [$name]"
            groovyPagesTemplateEngine.clearPageCache()
        } else {
            throw new IllegalArgumentException("Template not found: $name")
        }
    }

    void setVisible(String name, Date visibleFrom, Date visibleTo) {
        def tmpl = template(name)
        if (tmpl) {
            tmpl.visibleFrom = visibleFrom
            tmpl.visibleTo = visibleTo
            groovyPagesTemplateEngine.clearPageCache()
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
            eq('template.id', textTemplate.ident())
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
        groovyPagesTemplateEngine.clearPageCache()
        log.debug("Created/updated text template [$name]")
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
        def rval = false
        if (textTemplate) {
            textTemplate.delete()
            log.debug("Deleted text template [$name]")
            rval = true
        }
        groovyPagesTemplateEngine.clearPageCache()
        return rval
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
            log.debug("Deleted text content [$name] contentType=$contentType language=$language")
        }
        groovyPagesTemplateEngine.clearPageCache()
        return !result.isEmpty()
    }

    String applyTemplate(String templateContent, Map binding = [:], boolean cache = false) {
        def out = new StringWriter()
        if (templateContent) {
            def requestAttributes = RequestContextHolder.getRequestAttributes()
            boolean unbindRequest = false
            def language = (binding.language ?: binding.lang)
            try {
                // outside of an executing request, establish a mock version
                if (!requestAttributes) {
                    def servletContext = ServletContextHolder.getServletContext()
                    def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
                    requestAttributes = grails.util.GrailsWebUtil.bindMockWebRequest(applicationContext)
                    unbindRequest = true
                    log.debug "Not in web request, created mock request: $requestAttributes"
                    def locale = binding.locale ?: language
                    if (locale) {
                        def request = requestAttributes.getCurrentRequest()
                        if (!(locale instanceof Locale)) {
                            locale = new Locale(* locale.toString().split('_'))
                        }
                        request.addPreferredLocale(locale)
                        if (!language) {
                            language = locale.toString()
                        }
                    }
                }
                def name = templateContent.encodeAsMD5()
                if (language) {
                    name = (name + '_' + language)
                }
                def tenant = currentTenant?.get()
                if (tenant) {
                    name = 't' + tenant + '-' + name
                }
                println name
                def res = new ByteArrayResource(templateContent.getBytes("UTF-8"), name)
                groovyPagesTemplateEngine.createTemplate(res, cache).make(binding).writeTo(out)
            } finally {
                if (unbindRequest) {
                    RequestContextHolder.setRequestAttributes(null)
                }
            }
        }
        return out.toString()
    }

    String applyTemplate(String templateName, String contentType, Map binding = [:]) {
        def out = new StringWriter()
        try {
            applyTemplate(out, templateName, contentType, binding)
        } catch (Exception e) {
            log.error("Failed to apply template [$templateName] to binding $binding", e)
            out << e.message
        }
        return out.toString()
    }

    void applyTemplate(Writer out, String templateName, String contentType, Map binding = [:], boolean cache = false) {
        String language = binding.language ?: binding.lang
        def templateContent = content(templateName, contentType, language)
        if (templateContent) {
            def requestAttributes = RequestContextHolder.getRequestAttributes()
            def locale = binding.locale ?: language
            if (locale) {
                if (!(locale instanceof Locale)) {
                    locale = new Locale(* locale.toString().split('_'))
                }
            }
            boolean unbindRequest = false
            try {
                // outside of an executing request, establish a mock version
                if (!requestAttributes) {
                    def servletContext = ServletContextHolder.getServletContext()
                    def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
                    requestAttributes = grails.util.GrailsWebUtil.bindMockWebRequest(applicationContext)
                    unbindRequest = true
                    log.debug "Not in web request, created mock request: $requestAttributes"
                    if (locale) {
                        requestAttributes.getCurrentRequest().addPreferredLocale(locale)
                    }
                } else if (locale && (locale != requestAttributes.getCurrentRequest().getLocale())) {
                    requestAttributes.getCurrentRequest().addPreferredLocale(locale)
                }
                def name = templateName + '-' + contentType.replace('/', '-')
                if (language) {
                    name = (name + '_' + language)
                }
                def tenant = currentTenant?.get()
                if (tenant) {
                    name = 't' + tenant + '-' + name
                }
                println name
                def res = new ByteArrayResource(templateContent.getBytes('UTF-8'), name)
                groovyPagesTemplateEngine.createTemplate(res, cache).make(binding).writeTo(out)
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

    // Normalize to "Normalization Form Canonical Decomposition" (NFD)
    private String normalizeUnicode(String str) {
        Normalizer.Form form = Normalizer.Form.NFC
        Normalizer.isNormalized(str, form) ? str : Normalizer.normalize(str, form)
    }

    void addContentFromFile(File file) {
        def dir = file.parent
        def filename = normalizeUnicode(file.name)
        def ext = FilenameUtils.getExtension(filename)
        def language = StringUtils.substringAfterLast(dir, File.pathSeparator)
        def config = grailsApplication.config.textTemplate
        def contentType = config.contentType[ext]
        if (!contentType) {
            contentType = URLConnection.getFileNameMap().getContentTypeFor(filename)
            if (!contentType) {
                switch (ext) {
                    case 'html':
                    case 'htm':
                    case 'ftl':
                        contentType = 'text/html'
                        break
                    case 'txt':
                    case 'ini':
                        contentType = 'text/plain'
                        break
                    case 'xml':
                        contentType = 'text/xml'
                        break
                    case 'json':
                        contentType = 'application/json'
                        break
                    default:
                        contentType = 'application/octet-stream'
                        break
                }
            }
        }
        def (templateName, contentName) = getNamePair(filename)
        def tenant = config.defaultTenant
        def prevTenant = currentTenant.get()
        try {
            if (tenant) {
                currentTenant.set(tenant)
            }
            def exists = TextContent.createCriteria().count {
                template {
                    eq('name', templateName)
                    if (tenant != null) {
                        eq('tenantId', tenant)
                    } else {
                        isNull('tenantId')
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
                //cache true
            }
            if (!exists) {
                createContent(filename, contentType, file.text, language)
            }
        } finally {
            currentTenant.set(prevTenant)
        }
    }
}
