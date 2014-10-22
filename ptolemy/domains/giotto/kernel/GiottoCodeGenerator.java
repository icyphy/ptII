/* An attribute that manages generation of Giotto code.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.giotto.kernel;

import java.awt.Frame;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// GiottoCodeGenerator

/**
 This attribute is a visible attribute that when configured (by double
 clicking on it or by invoking Configure in the context menu) it generates
 Giotto code and displays it a text editor.  It is up to the user to save
 the Giotto code in an appropriate file, if necessary.

 <p>The Giotto Code Generator has been changed from the earlier generator
 implemented by Haiyang and Steve in the following respect :-

 <p>Any and all unconnected ports are ignored. This includes :
 <ol>
 <li> Removal of its mention in the output drivers
 <li> Removal of its mention in task (...) output (...)
 <li> Removal of driver code for tasks without inputs
 </ol>

 @author Edward A. Lee, Steve Neuendorffer, Haiyang Zheng, Christopher Brooks
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class GiottoCodeGenerator extends Attribute {
    /** Construct a factory with the default workspace and "" as name.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GiottoCodeGenerator() throws IllegalActionException,
            NameDuplicationException {
        super();
        _init();
    }

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GiottoCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate Giotto code for the given Giotto model.
     *  @param model The given Giotto model.
     *  @return The Giotto code.
     *  @exception IllegalActionException If code can not be generated.
     */
    public String generateGiottoCode(TypedCompositeActor model)
            throws IllegalActionException {
        return GiottoCodeGeneratorUtilities.generateGiottoCode(model);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method to instantiate the Editor Factory class called from the
     *  constructor. The reason for having this is that it can be
     *  overridden by subclasses
     *  @exception IllegalActionException If the editor factory can not be
     *  created.
     *  @exception NameDuplicationException If there is already another editor
     *  factory with the same name.
     */
    protected void _instantiateEditorFactoryClass()
            throws IllegalActionException, NameDuplicationException {
        new GiottoEditorFactory(this, "_editorFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");

        _instantiateEditorFactoryClass();

        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An attribute that can create an Giotto code editor for a Giotto model.
     */
    protected class GiottoEditorFactory extends EditorFactory {
        /** Constructs a Giotto EditorFactory object for a Giotto model.
         *
         *  @param container The container, which is a Giotto model.
         *  @param name The name for this attribute.
         *  @exception IllegalActionException If the factory is not of an
         *  acceptable attribute for the container.
         *  @exception NameDuplicationException If the name coincides with
         *  an attribute already in the container.
         */
        public GiottoEditorFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create an editor for configuring the specified object with the
         *  specified parent window.
         *  @param object The object to configure.
         *  @param parent The parent window, or null if there is none.
         */
        @Override
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Configuration configuration = ((TableauFrame) parent)
                        .getConfiguration();

                // NamedObj container = (NamedObj)object.getContainer();
                TypedCompositeActor model = (TypedCompositeActor) GiottoCodeGenerator.this
                        .getContainer();

                // Preinitialize and resolve types.
                CompositeActor toplevel = (CompositeActor) model.toplevel();
                Manager manager = toplevel.getManager();

                if (manager == null) {
                    manager = new Manager(toplevel.workspace(), "manager");
                    toplevel.setManager(manager);
                }

                manager.preinitializeAndResolveTypes();

                TextEffigy codeEffigy = TextEffigy
                        .newTextEffigy(configuration.getDirectory(),
                                generateGiottoCode(model));
                codeEffigy.setModified(true);
                configuration.createPrimaryTableau(codeEffigy);

                // end the model execution.
                manager.stop();
                manager.wrapup();
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                        "Cannot generate code. Perhaps outside Vergil?");
            }
        }
    }
}
