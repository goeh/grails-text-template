package grails.plugins.texttemplate


import grails.test.GroovyPagesTestCase

/**
 * Test the TextTemplateTagLib.
 */
class TextTemplateTagLibTests extends GroovyPagesTestCase {

    def grailsApplication
    def textTemplateService
    def currentTenant

    protected void setUp() {
        super.setUp()
        currentTenant.set(null)
    }

    void testTextTag() {
        textTemplateService.createContent("test-integration-hello", "text/plain", "Hello World")

        assert applyTemplate('<tt:text name="test-integration-hello"/>') == 'Hello World'
    }

    void testHtmlTag() {
        textTemplateService.createContent("test-integration-hello", "text/html", "<h1>Hello World</h1>")

        assert applyTemplate('<tt:html name="test-integration-hello"/>') == '<h1>Hello World</h1>'
    }

    void testNonExistingTemplate() {
        assert applyTemplate('<tt:text name="this-template-does-not-exist">Default Text</tt:text>') == 'Default Text'
        assert applyTemplate('<tt:html name="this-template-does-not-exist"><h1>Default HTML</h1></tt:html>') == '<h1>Default HTML</h1>'
    }

    void testEmptyTemplate() {
        // An empty template gives the same result as non-existing template.
        textTemplateService.createContent("test-integration-empty", "text/plain", "")
        assert applyTemplate('<tt:text name="test-integration-empty">EMPTY</tt:text>') == 'EMPTY'

        // A single whitespace is enough to make the template non-empty, and thus render it's content.
        textTemplateService.createContent("test-integration-empty", "text/plain", " ")
        assert applyTemplate('<tt:text name="test-integration-empty">ERROR</tt:text>') == ' '
    }

    void testAnyType() {
        textTemplateService.createContent("test-integration-any", "text/html", "<h1>Hello World</h1>")

        assert applyTemplate('<tt:content name="test-integration-any">FOO</tt:content>') == '<h1>Hello World</h1>'
        assert applyTemplate('<tt:content name="test-integration-any" contentType="text/plain">FOO</tt:content>') == 'FOO'

        textTemplateService.createContent("test-integration-any", "text/plain", "Hello World")

        assert applyTemplate('<tt:content name="test-integration-any" contentType="text/plain">FOO</tt:content>') == 'Hello World'
        // Multiple content gets concatenated
        assert applyTemplate('<tt:content name="test-integration-any">FOO</tt:content>') == '<h1>Hello World</h1>Hello World'

        textTemplateService.deleteContent("test-integration-any", "text/html")

        // Now we only have text/plain left.
        assert applyTemplate('<tt:content name="test-integration-any">FOO</tt:content>') == 'Hello World'

        textTemplateService.deleteTemplate("test-integration-any")
        // Now we don't have anything left.
        assert applyTemplate('<tt:content name="test-integration-any">FOO</tt:content>') == 'FOO'
    }

    void testGspTag() {
        textTemplateService.createContent("test-integration-gsp", "text/html", "<g:link controller=\"foo\" action=\"show\" id=\"\${bean.id}\">\${bean.name}</g:link>")

        assert applyTemplate('<tt:html name="test-integration-gsp"/>', [bean:[id:42, name:'Grails']]) == '<a href="/foo/show/42">Grails</a>'
    }

    void testIterate() {
        textTemplateService.createContent("test-integration-first", "text/plain", "First")
        textTemplateService.createContent("test-integration-second", "text/plain", "Second")
        textTemplateService.createContent("test-integration-third", "text/plain", "Third")
        textTemplateService.createContent("test-unit-simple", "text/plain", "Test")

        assert applyTemplate('<tt:eachTemplate name="test-integration">\${name}</tt:eachTemplate>') == 'test-integration-firsttest-integration-secondtest-integration-third'
    }

    void testComposite() {
        textTemplateService.createContent("test-integration.first", "text/plain", "First")
        textTemplateService.createContent("test-integration.second", "text/plain", "Second")
        textTemplateService.createContent("test-integration.third", "text/plain", "Third")

        assert applyTemplate('<tt:content name="test-integration">FOO</tt:content>') == 'FirstSecondThird'
    }

    void testMultiTenancy() {
        currentTenant.set(1)
        textTemplateService.createContent("tenant", "text/plain", "Hello Tenant 1")
        currentTenant.set(2)
        textTemplateService.createContent("tenant", "text/plain", "Hello Tenant 2")
        currentTenant.set(3)
        textTemplateService.createContent("tenant", "text/plain", "Hello Tenant 3")

        currentTenant.set(1)
        assert applyTemplate('<tt:content name="tenant">FOO</tt:content>') == "Hello Tenant 1"

        currentTenant.set(2)
        assert applyTemplate('<tt:content name="tenant">FOO</tt:content>') == "Hello Tenant 2"

        currentTenant.set(3)
        assert applyTemplate('<tt:content name="tenant">FOO</tt:content>') == "Hello Tenant 3"

        grailsApplication.config.textTemplate.defaultTenant = 1

        currentTenant.set(9)
        assert applyTemplate('<tt:content name="tenant">FOO</tt:content>') == "Hello Tenant 1"

        currentTenant.set(null)
        assert applyTemplate('<tt:content name="tenant">FOO</tt:content>') == "Hello Tenant 1"

        grailsApplication.config.textTemplate.defaultTenant = null

        currentTenant.set(9)
        assert applyTemplate('<tt:content name="tenant">FOO</tt:content>') == "FOO"

        currentTenant.set(null)
        assert applyTemplate('<tt:content name="tenant">FOO</tt:content>') == "FOO"
    }
}
