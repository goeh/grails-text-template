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

    def text = {attrs, body ->
        out << (textTemplateService.text(attrs.name, attrs.lang, attrs.tenant) ?: body())
    }

    def html = {attrs, body ->
        out << (textTemplateService.html(attrs.name, attrs.lang, attrs.tenant) ?: body())
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
