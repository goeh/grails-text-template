<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'textTemplate.label', default: 'Template')}"/>
    <title><g:message code="textTemplate.list.title" args="[entityName]"/></title>
</head>

<body>

<header class="page-header">
    <h1><g:message code="textTemplate.list.title" default="Templates" args="[entityName]"/></h1>
</header>

<table class="table table-striped">
    <thead>
    <tr>
        <g:sortableColumn property="name"
                          title="${message(code: 'textTemplate.name.label', default: 'Name')}"/>
        <g:sortableColumn property="status"
                          title="${message(code: 'textTemplate.status.label', default: 'Status')}"/>
        <g:sortableColumn property="visibleFrom"
                          title="${message(code: 'textTemplate.visibleFrom.label', default: 'visibleFrom')}"/>
        <g:sortableColumn property="visibleTo"
                          title="${message(code: 'textTemplate.visibleTo.label', default: 'visibleTo')}"/>
    </tr>
    </thead>
    <tbody>
    <g:each in="${templateList}" var="t">
        <tr>
            <td>
                <g:link action="edit" id="${t.id}">
                    ${t.name.encodeAsHTML()}
                </g:link>
            </td>
            <td>${message(code: 'textTemplate.status.' + t.status + '.label', default: t.status.toString())}</td>
            <td><g:formatDate type="date" date="${t.visibleFrom}"/></td>
            <td><g:formatDate type="date" date="${t.visibleTo}"/></td>
        </tr>
    </g:each>
    </tbody>
</table>

<g:paginate total="${totalCount}"/>

<div class="form-actions">
    <g:link action="edit" class="btn btn-success">
        <i class="icon-plus icon-white"></i>
        <g:message code="textTemplate.button.create.label" default="New Template"/>
    </g:link>
</div>

</body>
</html>
