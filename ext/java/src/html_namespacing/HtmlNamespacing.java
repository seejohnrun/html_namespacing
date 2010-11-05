/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package html_namespacing;

import java.util.HashSet;
import java.util.Set;
import org.jruby.anno.JRubyMethod;
import org.jruby.anno.JRubyModule;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 * @author johncrepezzi
 */
@JRubyModule(name = "HtmlNamespacing")
public class HtmlNamespacing {

    @JRubyMethod(name = "add_namespace_to_html", module = true, required = 2)
    public static IRubyObject addNamespaceToHtml(ThreadContext context, IRubyObject self, IRubyObject rhtml, IRubyObject rnamespace) {

        // if null, return what we have
        if (rhtml.isNil()) return rhtml;
        if (rnamespace.isNil()) return rhtml;

        String html = rhtml.asJavaString();
        String namespace = rnamespace.asJavaString();

        // Get the result and return it to Ruby
        try {
            String result = HtmlNamespacing.namespace(html, namespace);
            return context.getRuntime().newString(result);
        } catch (IllegalArgumentException ex) {
            throw context.getRuntime().newArgumentError(ex.getMessage());
        } catch (Throwable t) {
            throw context.getRuntime().newRuntimeError("Unknown html_namespacing error: " + t.getMessage());
        }
    }

    private enum ParseType {
        PARSE_NORMAL,
        PARSE_OPEN_TAG,
        PARSE_OPEN_TAG_NAME,
        PARSE_OPEN_TAG_ATTRIBUTE_NAME,
        PARSE_OPEN_TAG_ATTRIBUTE_VALUE,
        PARSE_OPEN_TAG_ATTRIBUTE_EQUALS,
        PARSE_CLOSE_TAG,
        PARSE_EMPTY_TAG, /* detected while we're inside PARSE_OPEN_TAG */
        PARSE_COMMENT,
        PARSE_XML_DECL,
        PARSE_DOCTYPE,
        PARSE_CDATA
    };

    private static final Set<String> ignoreTags;
    static {
        ignoreTags = new HashSet<String>();
        ignoreTags.add("html");
        ignoreTags.add("head");
        ignoreTags.add("base");
        ignoreTags.add("meta");
        ignoreTags.add("title");
        ignoreTags.add("link");
        ignoreTags.add("script");
        ignoreTags.add("noscript");
        ignoreTags.add("style");
    }

    private static boolean shouldIgnoreTag(String tag) {
        return ignoreTags.contains(tag);
    }

    private static boolean characterIsWhitespace(char c) {
        return c == 0x20 || c == 0x09 || c == 0x0d || c == 0x0a;
    }

    private static boolean atPlaceWithString(String html, int pos, String find) {
        if (pos < 0) return false;
        int findLength = find.length();
        if (html.length() - pos < findLength) return false;
        return html.substring(pos, pos + findLength).equals(find);
    }

    private static char character(String html, int pos) {
        if (pos < 0 || pos >= html.length()) return 0;
        return html.charAt(pos);
    }

    private static int positionOfNext(String html, int pos, String chars) {
        int htmlLength = html.length(); // @TODO centralize this
        while (chars.indexOf(html.charAt(pos)) == -1) {
            pos ++;
            if (pos >= htmlLength) return -1; // not found
        }
        return pos;
    }

    protected static String namespace(String html, String namespace) {

        if (html == null) return null;
        if (namespace == null) return html;

        ParseType state = ParseType.PARSE_NORMAL;
        int pos = 0;
        int openTagNameStart = 0;
        String openTag = null;
        int numberOfTagsOpen = 0;
        char openTagAttributeValue = 0;

        boolean openTagHadClassAttribute = false;
        boolean openTagAttributeIsClassAttribute = false;

        int copiedFrom = 0;
        StringBuilder result = new StringBuilder();

        boolean done = false;

        while(!done) {
            if (character(html,  pos) == 0) break;
            switch(state) {
                case PARSE_NORMAL:
                    if (html.charAt(pos) == '<') {
                        pos += 1;
                        if (atPlaceWithString(html, pos, "![CDATA[")) state = ParseType.PARSE_CDATA;
                        else if (atPlaceWithString(html, pos, "/")) state = ParseType.PARSE_CLOSE_TAG;
                        else if (atPlaceWithString(html, pos, "!--")) state = ParseType.PARSE_COMMENT;
                        else if (atPlaceWithString(html, pos, "!DOCTYPE")) state = ParseType.PARSE_DOCTYPE;
                        else if (atPlaceWithString(html, pos, "?xml")) state = ParseType.PARSE_XML_DECL;
                        else {
                            state = ParseType.PARSE_OPEN_TAG_NAME;
                            openTagNameStart = pos;
                        }
                    } else {
                        pos = html.indexOf('<', pos);
                    }
                    break;
                case PARSE_OPEN_TAG_NAME:
                    pos = positionOfNext(html, pos, " \t\n\r>/"); if (pos < 0) { done = true; break; }
                    openTag = html.substring(openTagNameStart, pos);
                    state = ParseType.PARSE_OPEN_TAG;
                    break;
                case PARSE_OPEN_TAG:
                    char c = html.charAt(pos);
                    if (c == '/' || c == '>') {
                        if (numberOfTagsOpen == 0 && !openTagHadClassAttribute && !shouldIgnoreTag(openTag)) {
                            result.append(html.substring(copiedFrom, pos));
                            copiedFrom = pos;

                            if (html.charAt(pos) == '/' && characterIsWhitespace(html.charAt(pos - 1))) {
                                result.append("class=\"");
                                result.append(namespace);
                                result.append("\" ");
                            } else {
                                result.append(" class=\"");
                                result.append(namespace);
                                result.append('"');
                            }
                        }

                        openTagHadClassAttribute = false;
                        openTagAttributeValue = 0;

                        if (html.charAt(pos) == '/') state = ParseType.PARSE_EMPTY_TAG;
                        else { numberOfTagsOpen ++; state = ParseType.PARSE_NORMAL; }
                        pos ++;
                    } else if (!characterIsWhitespace(html.charAt(pos))) {
                        if (atPlaceWithString(html, pos, "class")) {
                            openTagHadClassAttribute = true;
                            openTagAttributeIsClassAttribute = true;
                        }
                        else openTagAttributeIsClassAttribute = false;
                        state = ParseType.PARSE_OPEN_TAG_ATTRIBUTE_NAME;
                    } else {
                        pos ++;
                    }
                    break;
                case PARSE_OPEN_TAG_ATTRIBUTE_NAME:
                    pos = html.indexOf('=', pos);
                    pos ++;
                    state = ParseType.PARSE_OPEN_TAG_ATTRIBUTE_EQUALS;
                    break;
                case PARSE_OPEN_TAG_ATTRIBUTE_EQUALS:
                    pos = positionOfNext(html, pos, "\"'"); if (pos < 0) { done = true; break; }
                    openTagAttributeValue = html.charAt(pos);
                    state = ParseType.PARSE_OPEN_TAG_ATTRIBUTE_VALUE;
                    pos ++;
                    break;
                case PARSE_OPEN_TAG_ATTRIBUTE_VALUE:
                    pos = positionOfNext(html, pos, "\"'"); if (pos < 0) { done = true; break; }
                    boolean cancel = false;
                    while (!cancel && html.charAt(pos) != openTagAttributeValue) {
                        pos ++;
                        pos = positionOfNext(html, pos, "\"'"); if (pos < 0) { cancel = true; done = true; } //yuck
                    }
                    if (cancel) break;
                    if (openTagAttributeIsClassAttribute && numberOfTagsOpen == 0) {
                        result.append(html.substring(copiedFrom, pos)); copiedFrom = pos;
                        result.append(' ');
                        result.append(namespace);
                    }
                    openTagAttributeIsClassAttribute = false;
                    state = ParseType.PARSE_OPEN_TAG;
                    pos ++;
                    break;
                case PARSE_CLOSE_TAG:
                    pos = html.indexOf('>', pos); if (pos < 0) break;
                    numberOfTagsOpen --;
                    openTagAttributeValue = 0;
                    state = ParseType.PARSE_NORMAL;
                    pos ++;
                    break;
                case PARSE_EMPTY_TAG:
                case PARSE_XML_DECL:
                case PARSE_DOCTYPE:
                    pos = html.indexOf('>', pos); if (pos < 0) break;
                    pos ++;
                    state = ParseType.PARSE_NORMAL;
                    break;
                case PARSE_COMMENT:
                    pos ++;
                    pos = html.indexOf('-', pos); if (pos < 0) break;
                    if (atPlaceWithString(html, pos, "-->")) {
                        pos += 3;
                        state = ParseType.PARSE_NORMAL;
                    }
                    break;
                case PARSE_CDATA:
                    pos ++;
                    pos = html.indexOf("]", pos); if (pos < 0) break;
                    if (atPlaceWithString(html, pos, "]]>")) {
                        pos += 3;
                        state = ParseType.PARSE_NORMAL;
                    }
                    break;
                default:
                    done = true;
                    break;
            }
        }

        // copy the rest
        result.append(html.substring(copiedFrom, html.length()));

        if (state != ParseType.PARSE_NORMAL || numberOfTagsOpen != 0 || openTagAttributeIsClassAttribute || openTagAttributeValue != 0) {
            throw new IllegalArgumentException("Badly formatted HTML: " + html);
        }

        return result.toString();

    }

}
