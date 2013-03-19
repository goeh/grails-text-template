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

import org.springframework.dao.DataIntegrityViolationException

/**
 * Administration of text templates.
 */
class TextTemplateController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 930,
                    title: 'textTemplate.list.label',
                    action: 'index'
            ],
            [group: 'textTemplate',
                    order: 20,
                    title: 'textTemplate.create.label',
                    action: 'create',
                    isVisible: { actionName != 'create' }
            ],
            [group: 'textTemplate',
                    order: 30,
                    title: 'textTemplate.list.label',
                    action: 'list',
                    isVisible: { actionName != 'list' }
            ]
    ]

    def currentTenant
    def textTemplateService

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 20, 100)
        def tenant = currentTenant?.get()
        def result = TextTemplate.createCriteria().list(params) {
            if (tenant) {
                eq('tenantId', tenant)
            } else {
                isNull('tenantId')
            }
        }
        [templateList: result, totalCount: result.totalCount]
    }

    def create() {
        redirect(action: 'edit', params: [id: params.id, add: true])
    }

    def edit(Long id, Long content) {
        def tenant = currentTenant?.get()
        def textTemplate
        def textContent
        if (id) {
            textTemplate = TextTemplate.findByIdAndTenantId(id, tenant)
            if (textTemplate) {
                textContent = textTemplate.content?.find { it }
            } else {
                flash.error = message(code: 'textTemplate.not.found.message', args: [message(code: 'textTemplate.label', default: 'Template'), id])
                redirect action: 'list'
                return
            }
        } else {
            textTemplate = new TextTemplate(tenantId: tenant)
        }

        if (content) {
            textContent = TextContent.get(content)
            if (textContent == null || textContent.template != textTemplate) {
                flash.error = message(code: 'textContent.not.found.message', args: [message(code: 'textContent.label', default: 'Content'), content])
                redirect action: 'edit', id: id
                return
            }
        }

        switch (request.method) {
            case 'GET':
                if (params.boolean('add') || !textContent) {
                    textContent = new TextContent(template: textTemplate)
                }
                return [textTemplate: textTemplate, textContent: textContent]
            case 'POST':

                bindData(textTemplate, params, [include: ['name', 'status', 'visibleFrom', 'visibleTo', 'master']])

                if (!content) {
                    textContent = new TextContent(template: textTemplate)
                    textTemplate.addToContent(textContent)
                }

                bindData(textContent, params, [include: ['name', 'language', 'contentType', 'text']], 'textContent')

                if (textTemplate.validate() && textContent.validate()) {
                    textTemplate.save(failOnError: true, flush: true)
                    flash.success = message(code: 'textTemplate.updated.message', args: [message(code: 'textTemplate.label', default: 'Template'), textTemplate.toString()])
                    redirect action: "edit", id: textTemplate.id
                } else {
                    render view: 'edit', model: [textTemplate: textTemplate, textContent: textContent]
                    return
                }
                break
        }
    }

    def deleteTemplate(Long id) {
        def tenant = currentTenant?.get()
        def textTemplate = TextTemplate.findByIdAndTenantId(id, tenant)
        if (!textTemplate) {
            flash.error = message(code: 'textTemplate.not.found.message', args: [message(code: 'textTemplate.label', default: 'Template'), id])
            redirect action: 'list'
            return
        }

        try {
            def tombstone = textTemplate.toString()
            textTemplate.delete(flush: true)
            flash.warning = message(code: 'textTemplate.deleted.message', args: [message(code: 'textTemplate.label', default: 'Template'), tombstone])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'textTemplate.not.deleted.message', args: [message(code: 'textTemplate.label', default: 'Template'), id])
            redirect action: 'edit', id: id
        }
    }

    def deleteContent(Long id, Long content) {
        def tenant = currentTenant?.get()
        def textTemplate = TextTemplate.findByIdAndTenantId(id, tenant)
        if (!textTemplate) {
            flash.error = message(code: 'textTemplate.not.found.message', args: [message(code: 'textTemplate.label', default: 'Template'), id])
            redirect action: 'list'
            return
        }
        def textContent = TextContent.get(content)
        if (textContent == null || textContent.template != textTemplate) {
            flash.error = message(code: 'textTemplate.not.found.message', args: [message(code: 'textTemplate.label', default: 'Template'), content])
            redirect action: 'edit', id: id
            return
        }

        try {
            def tombstone = textContent.toString()
            textTemplate.removeFromContent(textContent)
            textContent.delete(flush: true)
            textTemplate.save()
            flash.warning = message(code: 'textTemplate.deleted.message', args: [message(code: 'textTemplate.label', default: 'Template'), tombstone])
            redirect action: 'edit', id: id
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'textTemplate.not.deleted.message', args: [message(code: 'textTemplate.label', default: 'Template'), id])
            redirect action: 'edit', params: [id: id, content: content]
        }
    }

    def test() {
        if(! textTemplateService.content("functional-test", "text/html")) {
            textTemplateService.createContent("functional-test", "text/html",
                    "<g:link controller=\"textTemplate\" action=\"test\">Functional test</g:link> at \${date}")
        }

        [date: new Date()]
    }
}
