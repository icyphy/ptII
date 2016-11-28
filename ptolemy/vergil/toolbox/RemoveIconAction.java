/* Action to remove a custom icon.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.gui.Configuration;
import ptolemy.gui.Top;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.IconLoader;
import ptolemy.moml.MoMLParser;
import ptolemy.util.FileUtilities;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;

///////////////////////////////////////////////////////////////////
//// ConfigureAction

/** Action to remove a custom icon.
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
@SuppressWarnings("serial")
public class RemoveIconAction extends FigureAction {
    public RemoveIconAction() {
        super("Remove Custom Icon");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Process the remove icon command.
     *  @param e The event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Determine which entity was selected for the look inside action.
        super.actionPerformed(e);

        final NamedObj object = getTarget();

        // If the source of the event was a button, then super.actionPerformed(e)
        // will return null.  There are other reasons super.actionPerformed(e)
        // will return null as well.
        if (object != null) {
            // In theory, there should be only one.
            // But just in case, we remove all.
            Iterator icons = object.attributeList(EditorIcon.class).iterator();

            while (icons.hasNext()) {
                EditorIcon icon = (EditorIcon) icons.next();

                // An XMLIcon is not a custom icon, so don't remove it.
                if (!(icon instanceof XMLIcon)) {
                    final String iconName = icon.getName();
                    // FIXME: No undo!
                    ChangeRequest request = new ChangeRequest(this, "Remove Custom Icon") {
                        @Override
                        protected void _execute() throws Exception {
                            Attribute attribute = object.getAttribute(iconName);
                            if (attribute != null) {
                                attribute.setContainer(null);
                            }
                            // Restore the default icon.
                            // FIXME: Could be an XML icon.
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    IconLoader iconLoader = MoMLParser.getIconLoader();
                                    String className = object.getClassName();
                                    if (iconLoader != null) {
                                        try {
                                            iconLoader.loadIconForClass(className, object);
                                        } catch (Exception e) {
                                            // Ignore. Not much we can do here anyway.
                                            System.err.println(
                                                    "WARNING: Failed to load icon for class "
                                                            + className + ": " + e);
                                        }
                                    } else {
                                        // This is similar to MoMLParser._loadIconForClass, but
                                        // it seems there is no way to reuse that here.
                                        String fileName = "$CLASSPATH/" + className.replace('.', '/') + "Icon.xml";
                                        MoMLParser newParser = new MoMLParser(object.workspace());
                                        newParser.setContext(object);
                                        // Initiate tracking of objects created during the parse.
                                        newParser.clearTopObjectsList();
                                        try {
                                            URL url = FileUtilities.nameToURL(fileName, null, object.getClass().getClassLoader());
                                            newParser.parse(url, url);
                                            // Have to mark the contents derived objects, so that
                                            // the icon is not exported with the MoML export.
                                            List<NamedObj> icons = newParser.topObjectsCreated();
                                            if (icons != null) {
                                                Iterator objects = icons.iterator();

                                                while (objects.hasNext()) {
                                                    NamedObj newObject = (NamedObj) objects.next();
                                                    newObject.setDerivedLevel(1);
                                                    _markContentsDerived(newObject, 1);
                                                }
                                            }
                                        } catch (Exception e) {
                                            // Ignore. Not much we can do here anyway.
                                            System.err.println(
                                                    "WARNING: Failed to load icon for class "
                                                            + className + ": " + e);
                                        }
                                    }
                                }
                            };
                            Top.deferIfNecessary(runnable);
                        }
                    };
                    object.requestChange(request);
                }
            }
        }
    }

    /** Specify the configuration.
     *  In this action, this method does nothing.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        // Do nothing.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // NOTE: The following method is largely duplicated from MoMLParser,
    // but exposing that method is not a good idea.

    /** Mark the contents as being derived objects at a depth
     *  one greater than the depth argument, and then recursively
     *  mark their contents derived.
     *  This makes them not export MoML, and prohibits name and
     *  container changes. Normally, the argument is an Entity,
     *  but this method will accept any NamedObj.
     *  This method also adds all (deeply) contained instances
     *  of Settable to the _paramsToParse list, which ensures
     *  that they will be validated.
     *  @param object The instance that is defined by a class.
     *  @param depth The depth (normally 0).
     */
    private void _markContentsDerived(NamedObj object, int depth) {
        // NOTE: It is necessary to mark objects deeply contained
        // so that we can disable deletion and name changes.
        // While we are at it, we add any
        // deeply contained Settables to the _paramsToParse list.
        Iterator objects = object.lazyContainedObjectsIterator();

        while (objects.hasNext()) {
            NamedObj containedObject = (NamedObj) objects.next();
            containedObject.setDerivedLevel(depth + 1);
            _markContentsDerived(containedObject, depth + 1);
        }
    }
}
