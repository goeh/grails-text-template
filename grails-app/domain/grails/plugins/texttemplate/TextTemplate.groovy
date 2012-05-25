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

import org.apache.commons.lang.StringUtils

class TextTemplate {

    // Status.
    public static final int STATUS_DISABLED = -1
    public static final int STATUS_DRAFT = 0
    public static final int STATUS_PUBLISHED = 1
    public static final List STATUSES = [STATUS_DISABLED, STATUS_DRAFT, STATUS_PUBLISHED].asImmutable()

    Long tenantId
    String name
    int status
    Date visibleFrom
    Date visibleTo

    static hasMany = [content:TextContent]

    static constraints = {
        tenantId(nullable:true)
        name(maxSize:80, blank:false, unique:'tenantId')
        status(inList: STATUSES)
    }

    static mapping = {
        content cascade: 'all'
    }

    static transients = ['summary']

    // Wed Dec 31 12:00:00 GMT 2031
    private static final long FAR_AWAY = 1956488400000L

    def beforeValidate() {
        if (!visibleFrom) {
            visibleFrom = new Date()
        }
        if (!visibleTo) {
            visibleTo = new Date(FAR_AWAY)
        }
    }

    @Override
    public String toString() {
        return name
    }

    /**
     * Returns a summary of template content.
     * First 50 characters of content are returned.
     * @return summary of template content
     */
    public String getSummary() {
        def text = content?.find{it}?.toString()
        if(text) {
            return StringUtils.abbreviate(text, 50)
        }
        return ''
    }

}
