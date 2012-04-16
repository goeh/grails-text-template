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

    def text = {attrs, body ->
        def s = textTemplateService.text(attrs.name, attrs.lang, attrs.tenant)
        if(s) {
            if(attrs.raw) {
                out << s
            } else {
                groovyPagesTemplateEngine.createTemplate(s, "${attrs.name}-text-plain").make(pageScope.variables).writeTo(out)
            }
        } else {
            out << body()
        }
    }

    def html = {attrs, body ->
        def s = textTemplateService.html(attrs.name, attrs.lang, attrs.tenant)
        if(s) {
            if(attrs.raw) {
                out << s
            } else {
                groovyPagesTemplateEngine.createTemplate(s, "${attrs.name}-text-html").make(pageScope.variables).writeTo(out)
            }
        } else {
            out << body()
        }
    }

    def content = {attrs, body ->
        def contentType = attrs.contentType
        def s
        if(contentType) {
            s = textTemplateService.content(attrs.name, contentType, attrs.lang, attrs.tenant)
        } else {
            for(type in ['text/html', 'text/plain']) {
                s = textTemplateService.content(attrs.name, type, attrs.lang, attrs.tenant)
                if(s) {
                    contentType = type
                    break
                }
            }
        }

        if(s) {
            if(attrs.raw) {
                out << s
            } else {
                groovyPagesTemplateEngine.createTemplate(s, "${attrs.name}-${contentType.replace('/', '-')}").make(pageScope.variables).writeTo(out)
            }
        } else {
            out << body()
        }
    }

    def eachTemplate = {attrs, body ->
        def names = textTemplateService.getTemplateNames(attrs.name, attrs.tenant)
        for(name in names) {
            def t = textTemplateService.template(name, attrs.tenant)
            def map = [name:t.name, status:t.status, visibleFrom:t.visibleFrom, visibleTo:t.visibleTo]
            out << body(map)
        }
    }
}
