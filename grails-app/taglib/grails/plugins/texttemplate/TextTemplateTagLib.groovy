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

import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.codehaus.groovy.grails.compiler.web.pages.GroovyPageClassLoader
import org.springframework.core.io.ByteArrayResource

class TextTemplateTagLib {

    static namespace = "tt"

    def textTemplateService
    def currentTenant
    def grailsApplication

    private GroovyPagesTemplateEngine getEngine(request) {
        def groovyPagesTemplateEngine = request.getAttribute('TT_GROOVY_PAGES_TEMPLATE_ENGINE')
        if (!groovyPagesTemplateEngine) {
            synchronized (request) {
                groovyPagesTemplateEngine = new GroovyPagesTemplateEngine(servletContext)
                groovyPagesTemplateEngine.setClassLoader(new GroovyPageClassLoader(grailsApplication.getClassLoader()))
                request.setAttribute('TT_GROOVY_PAGES_TEMPLATE_ENGINE', groovyPagesTemplateEngine)
            }
        }
        return groovyPagesTemplateEngine
    }

    /**
     * Render text/plain content of a text template.
     * @attr name REQUIRED name of template
     * @attr lang preferred content language
     * @attr raw set to true to render the content un-parsed, default it will be parsed with GroovyPagesTemplateEngine
     * @attr model optional model sent to the template engine along with pageScope
     */
    def text = { attrs, body ->
        if (!attrs.name) {
            throwTagError("Tag [content] is missing required attribute [name]")
        }
        def s = textTemplateService.text(attrs.name, attrs.lang)
        if (s) {
            if (attrs.raw) {
                out << s
            } else {
                def name = attrs.name + '-text-plain'
                if (attrs.lang) {
                    name = (name + '_' + attrs.lang)
                }
                def tenant = currentTenant?.get()
                if (tenant) {
                    name = 't' + tenant + '-' + name
                }
                def res = new ByteArrayResource(s.getBytes("UTF-8"), name)
                def model = attrs.model ?: [:]
                getEngine(request).createTemplate(res, attrs.cache != null ? attrs.cache : false).make(pageScope.variables + model).writeTo(out)
            }
        } else {
            out << body()
        }
    }

    /**
     * Render text/html content of a text template.
     * @attr name REQUIRED name of template
     * @attr lang preferred content language
     * @attr raw set to true to render the content un-parsed, default it will be parsed with GroovyPagesTemplateEngine
     * @attr model optional model sent to the template engine along with pageScope
     */
    def html = { attrs, body ->
        if (!attrs.name) {
            throwTagError("Tag [content] is missing required attribute [name]")
        }
        def s = textTemplateService.html(attrs.name, attrs.lang)
        if (s) {
            if (attrs.raw) {
                out << s
            } else {
                if (params.ttDebug) {
                    out << "\n<!-- TEMPLATE ${attrs.name} START -->\n<span style=\"font-size:8px;\" title=\"${attrs.name}\">#</span>\n"
                }
                def name = attrs.name + '-text-html'
                if (attrs.lang) {
                    name = (name + '_' + attrs.lang)
                }
                def tenant = currentTenant?.get()
                if (tenant) {
                    name = 't' + tenant + '-' + name
                }
                def res = new ByteArrayResource(s.getBytes("UTF-8"), name)
                def model = attrs.model ?: [:]
                def engine = getEngine(request)
                //out << res.getDescription() + ' ' + engine.toString()
                engine.createTemplate(res, attrs.cache != null ? Boolean.valueOf(attrs.cache) : true).make(pageScope.variables + model).writeTo(out)
                if (params.ttDebug) {
                    out << "\n<!-- TEMPLATE ${attrs.name} END -->\n"
                }
            }
        } else {
            if (params.ttDebug) {
                out << "\n<!-- TEMPLATE ${attrs.name} NOT FOUND -->\n"
            }
            out << body()
        }
    }

    /**
     * Render content of a text template.
     * @attr name REQUIRED name of template
     * @attr contentType MIME content type, is not specified first content found is returned
     * @attr lang preferred content language
     * @attr raw set to true to render the content un-parsed, default it will be parsed with GroovyPagesTemplateEngine
     * @attr model optional model sent to the template engine along with pageScope
     */
    def content = { attrs, body ->
        if (!attrs.name) {
            throwTagError("Tag [content] is missing required attribute [name]")
        }
        def name = attrs.name

        def s
        if (attrs.contentType) {
            s = textTemplateService.content(attrs.name, attrs.contentType, attrs.lang)
            name = name + '-' + attrs.contentType.replace('/', '-')
        } else {
            def t = textTemplateService.template(attrs.name)
            if (t) {
                def c = t.content?.find { it }
                if (c) {
                    s = c.text
                    name = name + '-' + c.contentType.replace('/', '-')
                }
            }
        }
        if (s) {
            if (attrs.raw) {
                out << s
            } else {
                if (attrs.lang) {
                    name = (name + '_' + attrs.lang)
                }
                def tenant = currentTenant?.get()
                if (tenant) {
                    name = 't' + tenant + '-' + name
                }
                def res = new ByteArrayResource(s.getBytes("UTF-8"), name)
                def model = attrs.model ?: [:]
                getEngine(request).createTemplate(res, attrs.cache != null ? attrs.cache : false).make(pageScope.variables + model).writeTo(out)
            }
        } else {
            out << body()
        }
    }

    /**
     * Iterate over a list of text templates and render tag body for each template.
     * @attr name REQUIRED beginning of template name (query uses "WHERE name LIKE ...%")
     */
    def eachTemplate = { attrs, body ->
        if (!attrs.name) {
            throwTagError("Tag [content] is missing required attribute [name]")
        }
        def names = textTemplateService.getTemplateNames(attrs.name)
        for (name in names) {
            def t = textTemplateService.template(name)
            def map = [name: t.name, status: t.status, visibleFrom: t.visibleFrom, visibleTo: t.visibleTo, master: t.master?.name]
            map.content = t.content?.collect { [name: it.name, contentType: it.contentType, language: it.language] }
            out << body(map)
        }
    }
}
