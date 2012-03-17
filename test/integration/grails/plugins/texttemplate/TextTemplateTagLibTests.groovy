package grails.plugins.texttemplate


import grails.test.GroovyPagesTestCase

/**
 * Test the SelectionRepoTagLib.
 */
class TextTemplateTagLibTests extends GroovyPagesTestCase {

    def textTemplateService

    void testTagLibIterate() {
        textTemplateService.createContent("test.integration.hello", "text/plain", "Hello World")

        assert applyTemplate('<tt:text name="test.integration.hello"/>') == 'Hello World'
    }
}
