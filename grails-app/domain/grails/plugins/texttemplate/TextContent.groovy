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

import org.apache.commons.lang.StringUtils

class TextContent {

    public static final String TEXT_PLAIN = "text/plain"
    public static final String TEXT_HTML = "text/html"
    public static final String TEXT_XML = "text/xml"
    public static final String TEXT_JSON = "application/json"
    public static final List MIME_TYPES = [TEXT_PLAIN, TEXT_HTML, TEXT_XML, TEXT_JSON]

    String name
    String language
    String contentType
    String text

    static belongsTo = [template:TextTemplate]

    static constraints = {
        name(maxSize:80, blank:false)
        language(maxSize:5, nullable:true)
        contentType(maxSize:100, blank:false)
        text(maxSize:102400, nullable:true)
    }

    static mapping = {
        sort 'name'
        cache true
    }

    def beforeValidate() {
        if(! contentType) {
            contentType = TEXT_PLAIN
        }
        if(! name) {
            name = StringUtils.substringAfterLast(contentType, '/')
        }
    }

    String toString() {
        template?.name + '.' + name
    }
}
