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
                height : '200px',
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
        <div class="span2">

            <f:with bean="${textTemplate}">
                <f:field property="name" input-class="span2"/>
                <f:field property="master" input-class="span2"/>
                <f:field property="status">
                    <g:select name="status" from="${TextTemplate.STATUSES}" value="${textTemplate.status}"
                              valueMessagePrefix="textTemplate.status" class="span2"/>
                </f:field>
                <f:field property="visibleFrom">
                    <g:textField name="visibleFrom" class="span2"
                                 value="${formatDate(type:'date', date:textTemplate.visibleFrom)}"/>
                </f:field>
                <f:field property="visibleTo">
                    <g:textField name="visibleTo" class="span2"
                                 value="${formatDate(type:'date', date:textTemplate.visibleTo)}"/>
                </f:field>
            </f:with>

        </div>

        <div class="span8">
            <div class="row-fluid">
                <f:with bean="${textContent}">
                    <div class="span4">
                        <f:field property="name" prefix="textContent." input-class="span2"/>
                    </div>

                    <div class="span4">
                        <f:field property="contentType" prefix="textContent." input-class="span2"/>
                    </div>

                    <div class="span4">
                        <f:field property="language" prefix="textContent." input-class="span2"/>
                    </div>
                </f:with>
            </div>

            <div class="row-fluid">
                <textarea id="template-content" name="textContent.text" cols="50" rows="10"
                          class="span12">${textContent.text}</textarea>
            </div>

        </div>

        <div class="span2">
            <ul class="nav nav-list">
                <li class="nav-header">Template Content</li>
                <g:each in="${textTemplate.content}" var="content">
                    <li><g:link action="edit" params="${[id:textTemplate.id, content:content.id]}">
                        ${content.name?.encodeAsHTML()}
                        <g:if test="${content.language}">
                            (${content.language.encodeAsHTML()})
                        </g:if>
                    </g:link></li>
                </g:each>
            </ul>

        </div>
    </div>

    <div class="row-fluid">
        <div class="span2">
            <div class="form-actions">
                <g:link action="edit" class="btn btn-success">
                    <i class="icon-plus icon-white"></i>
                    <g:message code="textTemplate.button.create.label" default="New Template"/>
                </g:link>
                <g:if test="${textTemplate?.id}">
                    <button type="submit" name="_action_deleteTemplate" class="btn btn-danger"
                            onclick="return confirm('${message(code:'textTemplate.button.delete.confirm.message', default:'Are you sure you want to delete the template and all its content?')}')">
                        <i class="icon-trash icon-white"></i>
                        <g:message code="textTemplate.button.delete.label" default="Delete Template"/>
                    </button>
                </g:if>
            </div>
        </div>

        <div class="span8">
            <div class="form-actions">
                <g:if test="${textContent}">
                    <button type="submit" name="_action_edit" class="btn btn-primary">
                        <i class="icon-ok icon-white"></i>
                        <g:message code="textContent.button.save.label" default="Save"/>
                    </button>
                </g:if>
                <g:link action="edit" id="${textTemplate.id}" class="btn btn-success">
                    <i class="icon-plus icon-white"></i>
                    <g:message code="textContent.button.create.label" default="New Content"/>
                </g:link>
                <g:if test="${textContent?.id}">
                    <button type="submit" name="_action_deleteContent" class="btn btn-danger"
                            onclick="return confirm('${message(code:'textContent.button.delete.confirm.message', default:'Are you sure you want to delete this content?')}')">
                        <i class="icon-trash icon-white"></i>
                        <g:message code="textContent.button.delete.label" default="Delete Content"/>
                    </button>
                </g:if>
            </div>
        </div>

        <div class="span2">
        <p>&nbsp;</p>
        </div>
    </div>

</g:form>

</body>
</html>
