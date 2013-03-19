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

class TextTemplateServiceTests extends GroovyTestCase {

    def grailsApplication
    def textTemplateService
    def currentTenant

    void testCreateContent() {
        def content = textTemplateService.createContent("test-integration-foo", "text/plain", "Hello World")
        assert content != null
        assert content.name == 'plain'
        assert content.text == 'Hello World'
        assert content.language == null
        assert content.contentType == 'text/plain'
    }

    void testGetContent() {
        textTemplateService.createContent("test-integration-foo", "text/plain", "Hello World")
        textTemplateService.createContent("test-integration-foo", "text/html", "<h1>Hello World</h1>")
        textTemplateService.createContent("test-integration-foo", "text/xml", '<?xml version="1.0" encoding="UTF-8"?>\n<messages><msg>Hello World</msg></messages>')
        textTemplateService.createContent("test-integration-foo", "application/json", '{"message" : "Hello World"}')

        assert textTemplateService.text("test-integration-foo") == 'Hello World'
        assert textTemplateService.html("test-integration-foo") == '<h1>Hello World</h1>'
        assert textTemplateService.xml("test-integration-foo").msg[0] == 'Hello World'
        assert textTemplateService.json("test-integration-foo").message == 'Hello World'
    }

    void testAnyType() {
        textTemplateService.createContent("test-integration-1", "text/html", "<h1>Hello World</h1>")

        assert textTemplateService.content("test-integration-1") == '<h1>Hello World</h1>'
        assert textTemplateService.content("test-integration-1", "text/plain") == null

        textTemplateService.createContent("test-integration-1", "text/plain", "Hello World")

        assert textTemplateService.content("test-integration-1", "text/plain") == "Hello World"

        // Multiple content gets concatenated
        assert textTemplateService.content("test-integration-1") == '<h1>Hello World</h1>Hello World'

        textTemplateService.deleteContent("test-integration-1", "text/html")

        // Now we only have text/plain left.
        assert textTemplateService.content("test-integration-1") == 'Hello World'

        textTemplateService.deleteTemplate("test-integration-1")
        // Now we don't have anything left.
        assert textTemplateService.content("test-integration-1") == null
    }

    void testStatus() {
        def NAME = "test-integration-foo"
        textTemplateService.createContent(NAME, "text/plain", "Hello World")
        assert textTemplateService.text(NAME) == 'Hello World'

        // Setting status to draft should make the content disappear.
        textTemplateService.setStatusDraft(NAME)
        assert textTemplateService.text(NAME) == null

        // Set is to published and it should be visible again
        textTemplateService.setStatusPublished(NAME)
        assert textTemplateService.text(NAME) == 'Hello World'

        // Setting status to disabled should make the content disappear.
        textTemplateService.setStatusDisabled(NAME)
        assert textTemplateService.text(NAME) == null

    }

    void testVisibility() {
        def NAME = "test-integration-foo"
        textTemplateService.createContent(NAME, "text/plain", "Hello World")
        assert textTemplateService.text(NAME) == 'Hello World'

        // Text should not be visible until tomorrow.
        textTemplateService.setVisible(NAME, new Date() + 1, null)
        assert textTemplateService.text(NAME) == null

        // Make is visible again.
        textTemplateService.setVisible(NAME, new Date() - 1, new Date() + 1)
        assert textTemplateService.text(NAME) == 'Hello World'

        // Text is not visible anymore, it was hidden yesterday.
        textTemplateService.setVisible(NAME, null, new Date() - 1)
        assert textTemplateService.text(NAME) == null
    }

    void testListTemplates() {
        textTemplateService.createContent("test-integration-first", "text/plain", "First")
        textTemplateService.createContent("test-integration-second", "text/plain", "Second")
        textTemplateService.createContent("test-integration-third", "text/plain", "Third")
        textTemplateService.createContent("test-unit-simple", "text/plain", "Test")

        def result = textTemplateService.getTemplateNames("test-integration")
        assert result.size() == 3
    }

    void testApplyTemplate() {
        textTemplateService.createContent("test-integration-apply", "text/plain", "Hello \${arg}")
        textTemplateService.createContent("test-integration-apply", "text/html", "<h1>Hello \${arg}</h1>")
        assert textTemplateService.applyTemplate("test-integration-apply", "text/plain", [arg: "World!"]) == "Hello World!"
        assert textTemplateService.applyTemplate("test-integration-apply", "text/html", [arg: "World!"]) == "<h1>Hello World!</h1>"
        assert textTemplateService.applyTemplate("test-integration-apply", "text/xml", [arg: "World!"]) == ''

        def content = textTemplateService.content("test-integration-apply", "text/plain")
        assert content != null
        assert textTemplateService.applyTemplate(content, [arg: "Grails!"]) == "Hello Grails!"
    }

    void testCreateLink() {
        assert textTemplateService.createLink(controller: "foo", action: "bar", id: 42, absolute: true) == "http://localhost/foo/bar/42"
        assert textTemplateService.createLink(controller: "foo", action: "bar", id: 42, absolute: false) == "/foo/bar/42"
    }

    void testMultipleContent() {
        textTemplateService.createContent("home.index.left", "text/html", "<h1>Welcome</h1>")
        textTemplateService.createContent("home.index.middle", "text/html", "<h1>Our Products</h1>")
        textTemplateService.createContent("home.index.right", "text/html", "<h1>Contact</h1>")
        assert textTemplateService.getTemplateNames("home.index").size() == 1
        assert textTemplateService.getContentNames("home.index").size() == 3
        assert textTemplateService.html("home.index.right") == '<h1>Contact</h1>'
    }

    void testMultiTenancy() {
        currentTenant.set(1)
        textTemplateService.createContent("tenant", "text/plain", "Hello Tenant 1")
        currentTenant.set(2)
        textTemplateService.createContent("tenant", "text/plain", "Hello Tenant 2")
        currentTenant.set(3)
        textTemplateService.createContent("tenant", "text/plain", "Hello Tenant 3")

        currentTenant.set(1)
        assert textTemplateService.text("tenant") == "Hello Tenant 1"

        currentTenant.set(2)
        assert textTemplateService.text("tenant") == "Hello Tenant 2"

        currentTenant.set(3)
        assert textTemplateService.text("tenant") == "Hello Tenant 3"

        grailsApplication.config.textTemplate.defaultTenant = 1

        currentTenant.set(9)
        assert textTemplateService.text("tenant") == "Hello Tenant 1"

        currentTenant.set(null)
        assert textTemplateService.text("tenant") == "Hello Tenant 1"


        grailsApplication.config.textTemplate.defaultTenant = null

        currentTenant.set(9)
        assert textTemplateService.text("tenant") == null

        currentTenant.set(null)
        assert textTemplateService.text("tenant") == null
    }

    void testLanguage() {
        textTemplateService.createContent("test-integration-lang", "text/plain", "Hello \${arg}", "en")
        textTemplateService.createContent("test-integration-lang", "text/plain", "Hej \${arg}", "sv")
        textTemplateService.createContent("test-integration-lang", "text/plain", "Hola \${arg}", "es")
        textTemplateService.createContent("test-integration-lang", "text/plain", "Hei \${arg}", "fi")
        textTemplateService.createContent("test-integration-lang", "text/plain", "Hallo \${arg}", "de")

        assert textTemplateService.applyTemplate("test-integration-lang", "text/plain", [arg: "Grails!", language: "en"]) == "Hello Grails!"
        assert textTemplateService.applyTemplate("test-integration-lang", "text/plain", [arg: "Grails!", language: "sv"]) == "Hej Grails!"
        assert textTemplateService.applyTemplate("test-integration-lang", "text/plain", [arg: "Grails!", language: "es"]) == "Hola Grails!"
        assert textTemplateService.applyTemplate("test-integration-lang", "text/plain", [arg: "Grails!", lang: "fi"]) == "Hei Grails!"
        assert textTemplateService.applyTemplate("test-integration-lang", "text/plain", [arg: "Grails!", lang: "de"]) == "Hallo Grails!"
    }

    void testLocale() {
        textTemplateService.createContent("test-integration-locale", "text/plain", 'Date: <g:formatDate type="date" style="long" date="\${date}"/>')
        def date = Date.parse("yyyy-MM-dd", "2012-11-02")
        assert textTemplateService.applyTemplate("test-integration-locale", "text/plain", [date: date, locale: "en_UK"]) == "Date: November 2, 2012"
        assert textTemplateService.applyTemplate("test-integration-locale", "text/plain", [date: date, locale: "sv_SE"]) == "Date: den 2 november 2012"
        assert textTemplateService.applyTemplate("test-integration-locale", "text/plain", [date: date, locale: "es_ES"]) == "Date: 2 de noviembre de 2012"
        assert textTemplateService.applyTemplate("test-integration-locale", "text/plain", [date: date, locale: new Locale("fi_FI")]) == "Date: November 2, 2012"
        assert textTemplateService.applyTemplate("test-integration-locale", "text/plain", [date: date, locale: new Locale("de_DE")]) == "Date: November 2, 2012"
    }

    void testTemplateNameWithAccents() {
        textTemplateService.createContent("räksmörgås", "text/plain", 'Chrimp Sandwich')
        assert textTemplateService.applyTemplate("räksmörgås", "text/plain") == 'Chrimp Sandwich'
    }
}
