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

    void testIterate() {
        textTemplateService.createContent("test.integration.first", "text/plain", "First")
        textTemplateService.createContent("test.integration.second", "text/plain", "Second")
        textTemplateService.createContent("test.integration.third", "text/plain", "Third")
        textTemplateService.createContent("test.unit.simple", "text/plain", "Test")

        assert applyTemplate('<tt:eachTemplate name="test.integration">\${name}</tt:eachTemplate>') == 'test.integration.firsttest.integration.secondtest.integration.third'
    }
}
