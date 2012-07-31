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

class TextTemplateTagLib {

    static namespace = "tt"

    def textTemplateService
    def groovyPagesTemplateEngine
    def currentTenant

    def text = {attrs, body ->
        def bak = currentTenant.get()
        try {
            if (attrs.tenant != null) {
                currentTenant.set(attrs.int('tenant'))
            }
            def s = textTemplateService.text(attrs.name, attrs.lang)
            if (s) {
                if (attrs.raw) {
                    out << s
                } else {
                    groovyPagesTemplateEngine.createTemplate(s, "${attrs.name}-text").make(pageScope.variables).writeTo(out)
                }
            } else {
                out << body()
            }
        } finally {
            currentTenant.set(bak)
        }
    }

    def html = {attrs, body ->
        def bak = currentTenant.get()
        try {
            if (attrs.tenant != null) {
                currentTenant.set(attrs.int('tenant'))
            }
            def s = textTemplateService.html(attrs.name, attrs.lang)
            if (s) {
                if (attrs.raw) {
                    out << s
                } else {
                    groovyPagesTemplateEngine.createTemplate(s, "${attrs.name}-html").make(pageScope.variables).writeTo(out)
                }
            } else {
                out << body()
            }
        } finally {
            currentTenant.set(bak)
        }
    }

    def content = {attrs, body ->
        def bak = currentTenant.get()
        try {
            if (attrs.tenant != null) {
                currentTenant.set(attrs.int('tenant'))
            }
            def s = textTemplateService.content(attrs.name, attrs.contentType, attrs.lang)
            if (s) {
                if (attrs.raw) {
                    out << s
                } else {
                    groovyPagesTemplateEngine.createTemplate(s, "${attrs.name}").make(pageScope.variables).writeTo(out)
                }
            } else {
                out << body()
            }
        } finally {
            currentTenant.set(bak)
        }
    }

    def eachTemplate = {attrs, body ->
        def bak = currentTenant.get()
        try {
            if (attrs.tenant != null) {
                currentTenant.set(attrs.int('tenant'))
            }
            def names = textTemplateService.getTemplateNames(attrs.name)
            for (name in names) {
                def t = textTemplateService.template(name)
                def map = [name: t.name, status: t.status, visibleFrom: t.visibleFrom, visibleTo: t.visibleTo, master: t.master?.name]
                map.content = t.content?.collect {[name: it.name, contentType: it.contentType, language: it.language]}
                out << body(map)
            }
        } finally {
            currentTenant.set(bak)
        }
    }
}
