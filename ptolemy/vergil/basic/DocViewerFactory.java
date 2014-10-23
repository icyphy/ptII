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
package ptolemy.vergil.basic;

import java.awt.Frame;
import java.util.List;

import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.DocEffigy;
import ptolemy.vergil.actor.DocTableau;

///////////////////////////////////////////////////////////////////
//// DocViewerFactory

/**
 An attribute that creates an editor to open a doc viewer for
 its container's container. The usage for this is to put it in
 a visible attribute. When the user double clicks on the visible
 attribute or selects Configure from the context menu, this class
 will open documentation for the container's container. If the
 container's container doesn't have any documentation, then
 the user sees a dialog with instructions on how to create the
 documentation.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class DocViewerFactory extends EditorFactory {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DocViewerFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a doc viewer for the specified object with the
     *  specified parent window.
     *  @param object The object to configure, which is required to
     *   an instance of DocAttribute.
     *  @param parent The parent window, which is required to be an
     *   instance of TableauFrame.
     */
    @Override
    public void createEditor(NamedObj object, Frame parent) {
        NamedObj container = object.getContainer();
        if (container != null) {
            List docAttributes = container.attributeList(DocAttribute.class);
            // DocAttribute is singleton, so there should be at most one.
            if (docAttributes.size() < 1) {
                MessageHandler
                .message("To create documentation, right click on the background "
                        + "and select 'Documentation->Customize Documentation'");
                return;
            }
            DocAttribute doc = (DocAttribute) docAttributes.get(0);
            if (!(parent instanceof TableauFrame)) {
                MessageHandler.error("Cannot display documentation!");
            } else {
                Effigy effigy = ((TableauFrame) parent).getEffigy();
                try {
                    DocEffigy newEffigy = new DocEffigy(
                            (CompositeEntity) effigy.getContainer(), effigy
                            .getContainer().uniqueName("parentClass"));
                    newEffigy.setDocAttribute(doc);
                    DocTableau tableau = new DocTableau(newEffigy, "docTableau");
                    tableau.show();
                } catch (KernelException e) {
                    MessageHandler.error("Error opening documentation", e);
                }
            }
            return;
        }
        MessageHandler.error("Need a container to document.");
    }
}
