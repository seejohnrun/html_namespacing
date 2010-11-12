/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package html_namespacing;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.runtime.load.BasicLibraryService;

/**
 *
 * @author johncrepezzi
 */
public class HtmlNamespacingService implements BasicLibraryService {

    @Override
    public boolean basicLoad(Ruby runtime) {
        RubyModule module = runtime.defineModule("HtmlNamespacing");
        module.defineAnnotatedMethods(HtmlNamespacing.class);
        return true;
    }

}
