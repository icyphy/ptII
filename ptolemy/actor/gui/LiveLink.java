/* An attribute that creates an editor to open a doc viewer on its container's container.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.Frame;
import java.net.URL;
import java.util.List;

import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.export.web.IconLink;

///////////////////////////////////////////////////////////////////
//// LiveLink

/**
 An attribute that provides a link to a specified URL.
 This can be contained by any Ptolemy II object, and when a
 user double clicks on the icon for that object, the result is to open
 the specified URL. To set the URL, you either Alt-double click
 on the container, right click on an icon
 for this parameter and select Configure,
 or right click on the container and select Configure.
 <p>
 A common way to use this attribute is to put
 a text annotation in a model with text something like
 "See also Foo", where "Foo" is the name of another
 related model. Drag an instance of this LiveLink attribute
 onto the text annotation. Alt double click (or right click
 and select Configure) on the text
 annotation to set the file name for the model Foo to
 link to a file or a URL to link to a web page.
 The file name can be relative to the location of
 the model containing the annotation. It can also
 have any of the forms supported by
 {@link FileParameter}. For example, a file name
 can begin with $PTII, indicating that the file
 is in the Ptolemy II installation tree.
 <p>
 The default URL is "http://ptolemy.org#in_browser", which is
 the home page of the Ptolemy Project with an additional
 annotation indicating that the page should be opened
 in a browser. The suffix "#in_browser" will always
 be interpreted this way. Without this suffix, Vergil
 will be used to open the URL. Note that Vergil's HTML
 viewer does not handle many modern pages well.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @see IconLink
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class LiveLink extends FileParameter implements Editable {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public LiveLink(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setExpression("http://ptolemy.org#in_browser");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an editor.
     *  This editor will have no parent window.
     */
    @Override
    public void createEditor() {
        throw new InternalErrorException(this, null,
                "createEditor() should not be called.");
    }

    /** Create an editor for configuring the specified object.
     *  This editor will have no parent window.
     *  @param object The object to configure.
     */
    @Override
    public void createEditor(NamedObj object) {
        createEditor(object, null);
    }

    /** Create a doc viewer for the specified object with the
     *  specified parent window.
     *  @param object The object to configure, which is required to
     *   an instance of DocAttribute.
     *  @param parent The parent window, which is required to be an
     *   instance of TableauFrame.
     */
    @Override
    public void createEditor(NamedObj object, Frame parent) {
        Configuration configuration = (Configuration) Configuration.findEffigy(
                object.getContainer()).toplevel();
        try {
            // To find a base for relative references, find
            // the URIAttribute contained by the toplevel.
            NamedObj toplevel = toplevel();
            URL base = null;
            List<URIAttribute> attributes = toplevel
                    .attributeList(URIAttribute.class);
            if (attributes != null && attributes.size() > 0) {
                base = attributes.get(0).getURL();
            }
            URL toOpen = asURL();
            configuration.openModel(base, toOpen, toOpen.toExternalForm());
        } catch (Exception e) {
            MessageHandler.error("Unable to open specified URI", e);
        }
    }
}
