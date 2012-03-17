package grails.plugins.texttemplate


import grails.test.GroovyPagesTestCase

/**
 * Test the SelectionRepoTagLib.
 */
class TextTemplateTagLibTests extends GroovyPagesTestCase {

    def textTemplateService

    void testTextTag() {
        textTemplateService.createContent("test.integration.hello", "text/plain", "Hello World")

        assert applyTemplate('<tt:text name="test.integration.hello"/>') == 'Hello World'
    }

    void testHtmlTag() {
        textTemplateService.createContent("test.integration.hello", "text/html", "<h1>Hello World</h1>")

        assert applyTemplate('<tt:html name="test.integration.hello"/>') == '<h1>Hello World</h1>'
    }

    void testNonExistingTemplate() {
        assert applyTemplate('<tt:text name="this.template.does.not.exist">Default Text</tt:text>') == 'Default Text'
        assert applyTemplate('<tt:html name="this.template.does.not.exist"><h1>Default HTML</h1></tt:html>') == '<h1>Default HTML</h1>'
    }

    void testEmptyTemplate() {
        // An empty template gives the same result as non-existing template.
        textTemplateService.createContent("test.integration.empty", "text/plain", "")
        assert applyTemplate('<tt:text name="test.integration.empty">EMPTY</tt:text>') == 'EMPTY'

        // A single whitespace is enough to make the template non-empty, and thus render it's content.
        textTemplateService.createContent("test.integration.empty", "text/plain", " ")
        assert applyTemplate('<tt:text name="test.integration.empty">ERROR</tt:text>') == ' '
    }

    void testIterate() {
        textTemplateService.createContent("test.integration.first", "text/plain", "First")
        textTemplateService.createContent("test.integration.second", "text/plain", "Second")
        textTemplateService.createContent("test.integration.third", "text/plain", "Third")
        textTemplateService.createContent("test.unit.simple", "text/plain", "Test")

        assert applyTemplate('<tt:eachTemplate name="test.integration">\${name}</tt:eachTemplate>') == 'test.integration.firsttest.integration.secondtest.integration.third'
    }
}
