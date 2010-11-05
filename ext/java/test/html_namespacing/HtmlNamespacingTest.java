/*
    John Crepezzi (c) 2009
    <john@crepezzi.com>
*/
package html_namespacing;

import html_namespacing.HtmlNamespacing;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author based
 */
public class HtmlNamespacingTest {

    // For convenience
    private String testExact(String html, String namespace, String expectation) {
        String result = HtmlNamespacing.namespace(html, namespace);
        assertEquals(expectation, result);
        return result;
    }

    private void testError(String html, String namespace) {
        try {
            HtmlNamespacing.namespace(html, namespace);
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void nilHtml() {
        this.testExact(null, "X", null);
    }

    @Test
    public void nilNamespace() {
        this.testExact("<div>hello</div>", null, "<div>hello</div>");
    }

    @Test
    public void nilEverything() {
        this.testExact(null, null, null);
    }

    @Test
    public void testRegularTag() {
        this.testExact("<div>hello</div>", "X", "<div class=\"X\">hello</div>");
    }

    @Test
    public void testEmptyTag() {
        this.testExact("<div/>", "X", "<div class=\"X\"/>");
    }

    @Test
    public void testEmptyTagWithSpace() {
        this.testExact("<div />", "X", "<div class=\"X\" />");
    }

    @Test
    public void testNestedTag() {
        this.testExact("<div><div>hello</div></div>", "X", "<div class=\"X\"><div>hello</div></div>");
    }

    @Test
    public void testTwoTags() {
        this.testExact("<div>hello</div><div>goodbye</div>", "X", "<div class=\"X\">hello</div><div class=\"X\">goodbye</div>");
    }

    @Test
    public void testExistingClassDoubleQuotes() {
        this.testExact("<div class=\"foo\">bar</div>", "baz", "<div class=\"foo baz\">bar</div>");
    }

    @Test
    public void testExistingClassSingleQuotes() {
        this.testExact("<div class='foo'>bar</div>", "baz", "<div class='foo baz'>bar</div>");
    }

    @Test
    public void testOtherAttributesIgnored() {
        this.testExact("<div id=\"id\" class=\"foo\" style=\"display:none;\">bar</div>", "baz",
                "<div id=\"id\" class=\"foo baz\" style=\"display:none;\">bar</div>");
    }

    @Test
    public void worksWithUTF8() {
        this.testExact("<div class=\"ᛗ\">᛬</div>", "ᚾ", "<div class=\"ᛗ ᚾ\">᛬</div>");
    }

    @Test
    public void emptyTagWithExistingClass() {
        this.testExact("<span class=\"foo\"/>", "bar", "<span class=\"foo bar\"/>");
    }

    @Test
    public void worksWithNewlines() {
        this.testExact("<div\n\nclass\n\n=\n\n'foo'\n\n>bar</div>", "baz", "<div\n\nclass\n\n=\n\n'foo baz'\n\n>bar</div>");
    }

    @Test
    public void worksWithEscapedAttributes() {
        this.testExact("<div title=\"John's House\" class=\"foo\">bar</div>", "baz",
                "<div title=\"John's House\" class=\"foo baz\">bar</div>");
    }

    @Test
    public void ignoresXmlProlog() {
        this.testExact("<?xml version=\"1.0\"?><div>foo</div>", "X", "<?xml version=\"1.0\"?><div class=\"X\">foo</div>");
    }

    @Test
    public void ignoresDoctype() {
        this.testExact("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><div>foo</div>", "X",
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><div class=\"X\">foo</div>");
    }

    @Test
    public void ignoresCDATA() {
        this.testExact("<![CDATA[ignore <div class=\"foo\">]] </div>]]><div>foo</div>", "X",
                "<![CDATA[ignore <div class=\"foo\">]] </div>]]><div class=\"X\">foo</div>");
    }

    @Test
    public void ignoresComments() {
        this.testExact("<!-- blah <div class=\"foo\">foo</div> - <span/>--><div>foo</div>", "X",
                "<!-- blah <div class=\"foo\">foo</div> - <span/>--><div class=\"X\">foo</div>");
    }

    @Test
    public void detectsCdataEndDelimeterCorrectly() {
        this.testExact("<![CDATA[ ]>< ]]>", "X", "<![CDATA[ ]>< ]]>");
    }

    @Test
    public void detectsCommentEndDelimeterCorrectly() {
        this.testExact("<!-- ->< -->", "X", "<!-- ->< -->");
    }

    @Test
    public void testThingsAreIgnored() {
        String[] things = { "html", "head", "base", "meta", "title", "link", "script", "noscript", "style" };
        for (String thing : things) {
            String testStr = "<" + thing + ">foo</" + thing + ">";
            this.testExact(testStr, "X", testStr);
        }
    }

    // DESIGNED TO FAIL
    /////////////////////

    @Test
    public void testUnclosedTag() {
        this.testError("<div>foo", "X");
    }

    @Test
    public void testClosingTag() {
        this.testError("foo</div>", "X");
    }

    @Test
    public void wrongAttributeSyntax() {
        this.testError("<div foo=bar>foo</div>", "X");
    }

    @Test
    public void missingClosingAngle() {
        this.testError("<div foo=\"bar\">foo</div", "X");
    }

    @Test
    public void badEndOfString() {
        this.testError("<div foo=\"x", "X");
    }

    @Test
    public void endOfStringDuringDoctype() {
        this.testError("<!DOCTYPE", "X");
    }

}