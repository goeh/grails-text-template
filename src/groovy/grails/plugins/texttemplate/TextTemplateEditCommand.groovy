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

import grails.validation.Validateable

/**
 * Command object used when creating/editing templates.
 */
@Validateable
class TextTemplateEditCommand {
    Long id
    String name
    String language
    String contentType
    int status
    Date visibleFrom
    Date visibleTo
    String text

    static constraints = {
        id(nullable:true)
        name(maxSize: 80, blank: false)
        status(inList: TextTemplate.STATUSES)
        language(maxSize: 5, nullable: true)
        contentType(maxSize: 100, blank: false, inList: TextContent.MIME_TYPES)
        text(maxSize: 102400, nullable: true)
    }

    String toString() {
        name.toString()
    }
}
