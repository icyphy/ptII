/* An attribute that displays documentation.

Copyright (c) 2003-2004 The Regents of the University of California.
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

PT_COPYRIGHT_VERSION_3
COPYRIGHTENDKEY
*/

package ptolemy.vergil.kernel.attributes;

import java.awt.Frame;
import java.net.URL;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.BrowserLauncher;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;

/**
   This attribute is a visible attribute that displays documentation when
   configured by double clicking on it or by invoking Configure in the context
   menu.
   <p>
   The source of the documentation is a file that is specified by the
   _documentation FileParameter. To create the FileParameter for a model
   <li> Right-mouse click on the background and select Configure.
   <li> Select Add.
   <li> use _documentation for the Name:
   <li> use the file name with the documentation for the Default Value:
   <li> use ptolemy.data.expr.FileParameter for the Class:
   <li> Click OK.
   <p>
   The DocumentationAttribute attribute can be found under more Utilities -&gt;
   Decorative -&gt; Documentation.

   @author Rowland R Johnson
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (rowland)
   @Pt.AcceptedRating Red (rowland)
*/
public class DocumentationAttribute extends Attribute {

    /** Construct am icon with the attached this attached.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DocumentationAttribute(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText(
            "_iconDescription",
            "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"130\" height=\"40\" "
                + "style=\"fill:yellow\"/>"
                + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "Double click to see\ndocumentation.</text></svg>");
        new SingletonAttribute(this, "_hideName");
        new DocumentationAttributeFactory(this, "_editorFactory");
        _docLocation = new Parameter(this, "docLocation", new StringToken());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class DocumentationAttributeFactory extends EditorFactory {

        public DocumentationAttributeFactory(NamedObj _container, String name)
            throws IllegalActionException, NameDuplicationException {
            super(_container, name);
        }

        /** Create an editor for configuring the specified object with the
         *  specified parent window.
         *  @param object The object to configure.
         *  @param parent The parent window, or null if there is none.
         */
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Configuration configuration =
                    ((TableauFrame) parent).getConfiguration();
                Tableau _tableau = ((TableauFrame) parent).getTableau();
                TypedCompositeActor model =
                    (
                        (TypedCompositeActor) (((PtolemyEffigy) (_tableau
                            .getContainer()))
                        .getModel()));

                FileParameter docAttribute =
                    (FileParameter) model.getAttribute(
                        "_documentation",
                        FileParameter.class);

                if (docAttribute != null) {
                    URL doc =
                        MoMLApplication.specToURL(docAttribute.getExpression());
                    configuration.openModel(doc, doc, doc.toExternalForm());
                }
            } catch (Exception ex) {
                throw new InternalErrorException(
                    object,
                    ex,
                    "Cannot access Documentation");
            }
        }
    }

    private Parameter _docLocation = null;
}
