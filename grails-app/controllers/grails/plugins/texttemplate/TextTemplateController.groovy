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
                    title: 'textTemplate.label',
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

    def textTemplateService
    def currentTenant

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        if (!params.max) params.max = 20
        def tenant = currentTenant?.get()
        def result = tenant ? TextTemplate.findAllByTenantId(tenant, params) : TextTemplate.list(params)
        [templateList: result]
    }

    def edit() {
        def tenant = currentTenant?.get()
        def textTemplate
        if (params.id) {
            textTemplate = TextTemplate.findByIdAndTenantId(params.id, tenant)
            if (!textTemplate) {
                flash.error = message(code: 'textTemplate.not.found.message', args: [message(code: 'textTemplate.label', default: 'Template'), params.id])
                redirect action: 'list'
                return
            }
        } else {
            textTemplate = new TextTemplate(tenantId:tenant)
        }

        def textContent
        if (params.content) {
            textContent = TextContent.get(params.content)
            if (textContent == null || textContent.template != textTemplate) {
                flash.error = message(code: 'textContent.not.found.message', args: [message(code: 'textContent.label', default: 'Content'), params.content])
                redirect action: 'edit', id: params.id
                return
            }
        } else {
            textContent = new TextContent(template:textTemplate)
        }

        bindData(textTemplate, params, [include: ['name', 'status', 'visibleFrom', 'visibleTo', 'master']])
        bindData(textContent, params, [include: ['name', 'language', 'contentType', 'text']], 'textContent')

        switch (request.method) {
            case 'GET':
                return [textTemplate: textTemplate, textContent: textContent]
            case 'POST':

                if (!textContent.id) {
                    textTemplate.addToContent(textContent)
                }

                if (textTemplate.validate() && textContent.validate()) {
                    textTemplate.save(failOnError: true, flush: true)
                    flash.success = message(code: 'textTemplate.updated.message', args: [message(code: 'textTemplate.label', default: 'Template'), textTemplate.toString()])
                    redirect action:"edit", id:textTemplate.id
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
            redirect action: 'edit', id:id
            return
        }

        try {
            def tombstone = textContent.toString()
            textTemplate.removeFromContent(textContent)
            textContent.delete(flush: true)
            textTemplate.save()
            flash.warning = message(code: 'textTemplate.deleted.message', args: [message(code: 'textTemplate.label', default: 'Template'), tombstone])
            redirect action: 'edit', id:id
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'textTemplate.not.deleted.message', args: [message(code: 'textTemplate.label', default: 'Template'), id])
            redirect action: 'edit', params: [id:id, content:content]
        }
    }
}
