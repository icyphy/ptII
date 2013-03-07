/* Main class for the Eclipse plugin.

 Copyright (c) 2005-2013 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.backtrack.eclipse.plugin;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ptolemy.backtrack.eclipse.plugin.console.OutputConsole;

///////////////////////////////////////////////////////////////////
//// EclipsePlugin
/**
 Main class for the Eclipse plugin. This class initializes the Eclipse plugin.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class EclipsePlugin extends AbstractUIPlugin {

    /** Construct a plugin object. Within one Eclipse process, there should be
     *  only one such plugin object
     */
    public EclipsePlugin() {
        super();
        _plugin = this;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the console for this plugin.
     *
     *  @return The console.
     */
    public OutputConsole getConsole() {
        return _console;
    }

    /** Return the only instance of this plugin.
     *
     *  @return The instance of this plugin created in the current Eclipse
     *  process.
     */
    public static EclipsePlugin getDefault() {
        return _plugin;
    }

    /** Return an image descriptor for the image file at the given plugin
     *  relative path.
     *
     *  @param path The path to the image descriptor.
     *  @return The image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("ptolemy.backtrack",
                path);
    }

    /** Return the plugin's resource bundle.
     *
     *  @return The resource bundle for this plugin, or null if none.
     */
    public ResourceBundle getResourceBundle() {
        try {
            if (_resourceBundle == null) {
                _resourceBundle = ResourceBundle
                        .getBundle("test.TestPluginResources");
            }
        } catch (MissingResourceException x) {
            _resourceBundle = null;
        }

        return _resourceBundle;
    }

    /** Return the string from the plugin's resource bundle, or the key itself
     *  if not found.
     *
     *  @param key The key of the resource string.
     *  @return The resource string, or the key itself.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = EclipsePlugin.getDefault().getResourceBundle();

        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /** Return the current Eclipse process's standard display. This standard
     *  display can be used to synchronize user interface operations.
     *
     *  @return The standard display.
     */
    public static Display getStandardDisplay() {
        Display display = Display.getCurrent();

        if (display == null) {
            display = Display.getDefault();
        }

        return display;
    }

    /** Start the plugin. This method is called upon plugin activation.
     *
     *  @param context The context where this plugin is started.
     *  @exception Exception If the start method of the superclass throws an
     *  Exception.
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        _console = new OutputConsole();
    }

    /** Stop the plugin. This method is called when the plugin is stopped. After
     *  the plugin is stopped, it cannot be started again, unless a new instance
     *  of plugin is created.
     *
     *  @param context The context where this plugin has been started.
     *  @exception Exception If the stop method of the superclass throws an
     *  Exception.
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        _plugin = null;
        _resourceBundle = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The console for this plugin.
     */
    private OutputConsole _console;

    /** The globally unique plugin object.
     */
    private static EclipsePlugin _plugin;

    /** The resource bundle.
     */
    private ResourceBundle _resourceBundle;
}
