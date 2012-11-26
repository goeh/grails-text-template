<%@ page import="grails.plugins.texttemplate.TextTemplate" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'textTemplate.label', default: 'Template')}"/>
    <title><g:message code="textTemplate.edit.title" args="[entityName, textTemplate.name]"/></title>
    <% if (textContent.contentType == 'text/html') { %>
    <ckeditor:resources/>
    <r:script>
        $(document).ready(function() {
            var editor = CKEDITOR.replace('template-content',
            {
                width : '98.3%',
                height : '250px',
                resize_enabled : true,
                startupFocus : false,
                skin : 'kama',
                toolbar :
                [
                    ['Styles','Format','Font','FontSize'],
                    ['Source'],
                    '/',
                    ['Bold','Italic','Underline','Strike','TextColor','BGColor','RemoveFormat'],
                    ['Paste','PasteText','PasteFromWord'],
                    ['JustifyLeft','JustifyCenter','JustifyRight'],
                    ['NumberedList','BulletedList','-','Outdent','Indent'],
                    ['Image', 'Link','Unlink'],
                    ['Table','HorizontalRule']
                ]
            });
        });
    </r:script>
    <% } %>
</head>

<body>

<header class="page-header">
    <h1>
        <g:if test="${textTemplate.id}">
            <g:message code="textTemplate.edit.title" default="Edit {0} - {1}" args="[entityName, textTemplate.name]"/>
        </g:if>
        <g:else>
            <g:message code="textTemplate.create.title" default="Create {0}" args="[entityName, textTemplate.name]"/>
        </g:else>
    </h1>
</header>

<g:hasErrors bean="${textTemplate}">
    <bootstrap:alert class="alert-error">
        <ul>
            <g:eachError bean="${textTemplate}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </bootstrap:alert>
</g:hasErrors>

<g:hasErrors bean="${textContent}">
    <bootstrap:alert class="alert-error">
        <ul>
            <g:eachError bean="${textContent}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </bootstrap:alert>
</g:hasErrors>

<g:form action="edit">
    <g:hiddenField name="id" value="${textTemplate.id}"/>
    <g:hiddenField name="content" value="${textContent.id}"/>

    <div class="row-fluid">
        <div class="span3">

            <f:with bean="${textTemplate}">
                <f:field property="name" input-class="span12" input-autofocus=""/>
                <f:field property="master" input-class="span12"/>
                <f:field property="status">
                    <g:select name="status" from="${TextTemplate.STATUSES}" value="${textTemplate.status}"
                              valueMessagePrefix="textTemplate.status" class="span12"/>
                </f:field>
                <f:field property="visibleFrom">
                    <g:textField name="visibleFrom" class="span7 date"
                                 value="${formatDate(type:'date', date:textTemplate.visibleFrom)}"/>
                </f:field>
                <f:field property="visibleTo">
                    <g:textField name="visibleTo" class="span7 date"
                                 value="${formatDate(type:'date', date:textTemplate.visibleTo)}"/>
                </f:field>
            </f:with>

            <ul id="textContent-list" class="nav nav-list">
                <li class="nav-header"><g:message code="textTemplate.content.label" default="Template Content"/></li>
                <g:each in="${textTemplate.content}" var="content">
                    <li class="${content.id == textContent?.id ? 'active' : ''}">
                        <g:link action="edit" params="${[id:textTemplate.id, content:content.id]}">
                            ${content.name?.encodeAsHTML()}
                            <g:if test="${content.language}">
                                (${content.language.encodeAsHTML()})
                            </g:if>
                        </g:link>
                    </li>
                </g:each>
            </ul>

        </div>

        <div class="span9">
            <div class="row-fluid">
                <f:with bean="${textContent}">
                    <div class="span4">
                        <f:field property="name" prefix="textContent." input-class="span12"/>
                    </div>

                    <div class="span4">
                        <f:field property="contentType" prefix="textContent." input-class="span12"/>
                    </div>

                    <div class="span4">
                        <f:field property="language" prefix="textContent." input-class="span12"/>
                    </div>
                </f:with>
            </div>

            <div class="row-fluid">
                <textarea id="template-content" name="textContent.text" cols="70" rows="18"
                          class="span12">${textContent.text}</textarea>
            </div>

        </div>

    </div>

    <div class="row-fluid">
        <div class="span3">
            <div class="form-actions">
                <g:link action="edit" class="btn btn-success">
                    <i class="icon-plus icon-white"></i>
                    <g:message code="textTemplate.button.create.label" default="New Template"/>
                </g:link>
                <g:if test="${textTemplate?.id}">
                    <button type="submit" name="_action_deleteTemplate" class="btn btn-danger"
                            onclick="return confirm('${message(code:'textTemplate.button.delete.confirm.message', default:'Are you sure you want to delete the template and all its content?', args:[textTemplate.name])}')">
                        <i class="icon-trash icon-white"></i>
                        <g:message code="textTemplate.button.delete.label" default="Delete Template"/>
                    </button>
                </g:if>
            </div>
        </div>

        <div class="span9">
            <div class="form-actions">
                <g:if test="${textContent}">
                    <button type="submit" name="_action_edit" class="btn btn-primary">
                        <i class="icon-ok icon-white"></i>
                        <g:message code="textContent.button.save.label" default="Save"/>
                    </button>
                </g:if>
                <g:if test="${params.boolean('add')}">
                    <g:link action="edit" id="${textTemplate.id}" class="btn">
                        <i class="icon-remove"></i>
                        <g:message code="textContent.button.cancel.label" default="Cancel"/>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link action="create" id="${textTemplate.id}" class="btn btn-success">
                        <i class="icon-plus icon-white"></i>
                        <g:message code="textContent.button.create.label" default="New Content"/>
                    </g:link>
                </g:else>
                <g:if test="${textContent?.id}">
                    <button type="submit" name="_action_deleteContent" class="btn btn-danger"
                            onclick="return confirm('${message(code:'textContent.button.delete.confirm.message', default:'Are you sure you want to delete this content?', args:[textContent.name])}')">
                        <i class="icon-trash icon-white"></i>
                        <g:message code="textContent.button.delete.label" default="Delete Content"/>
                    </button>
                </g:if>
                <g:link action="list" class="btn btn-info">
                    <i class="icon-list-alt icon-white"></i>
                    <g:message code="textTemplate.button.list.label" default="Templates"/>
                </g:link>
            </div>
        </div>
    </div>

</g:form>

</body>
</html>
