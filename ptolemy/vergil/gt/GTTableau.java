/*

@Copyright (c) 2007-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.vergil.gt;

import java.awt.Color;

import ptolemy.actor.gt.FSMMatcher;
import ptolemy.actor.gt.GTCompositeActor;
import ptolemy.actor.gt.ModalModelMatcher;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.modal.CaseGraphTableau;

///////////////////////////////////////////////////////////////////
//// GTRuleGraphTableau

/** An editor tableau for graph transformation in Ptolemy II. FIXME: more

 @author  Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see CaseGraphTableau
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTTableau extends Tableau {

    /** Create a new case editor tableau with the specified container
     *  and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the model associated with
     *   the container effigy is not an instance of Case.
     *  @exception NameDuplicationException If the container already
     *   contains an object with the specified name.
     */
    public GTTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null);
    }

    /** Create a new case editor tableau with the specified container,
     *  name, and default library.
     *  @param container The container.
     *  @param name The name.
     *  @param defaultLibrary The default library, or null to not specify one.
     *  @exception IllegalActionException If the model associated with
     *   the container effigy is not an instance of Case.
     *  @exception NameDuplicationException If the container already
     *   contains an object with the specified name.
     */
    public GTTableau(PtolemyEffigy container, String name,
            LibraryAttribute defaultLibrary) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        NamedObj model = container.getModel();

        if (!(model instanceof GTCompositeActor || !model.attributeList(
                Factory.class).isEmpty())) {
            throw new IllegalActionException(this,
                    "Cannot edit a model that is not a GTCompositeActor.");
        }

        createFrame((CompositeEntity) model, defaultLibrary);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the graph frame that displays the model associated with
     *  this tableau together with the specified library.
     *  This method creates a GRRuleGraphFrame. If a subclass
     *  uses another frame, this method should be overridden to create
     *  that frame.
     *  @param model The Ptolemy II model to display in the graph frame.
     *  @param defaultLibrary The default library, or null to not specify
     *   one.
     */
    public void createFrame(CompositeEntity model,
            LibraryAttribute defaultLibrary) {
        if (!(model instanceof GTCompositeActor)
                && model.attributeList(Factory.class).isEmpty()) {
            throw new InternalErrorException(this, null, "Composite entity \""
                    + model.getFullName() + "\" is not an instance of "
                    + "GTCompositeActor.");
        }

        ExtendedGraphFrame frame = new TransformationEditor(model, this,
                defaultLibrary);
        try {
            setFrame(frame);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }

        frame.setBackground(BACKGROUND_COLOR);
        frame.pack();
        frame.centerOnScreen();
        frame.setVisible(true);
    }

    /** A factory that creates graph editing tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {

        /** Create an factory with the given name and container.
         *  @param container The container.
         *  @param name The name of the entity.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create an instance of GRRuleGraphTableau for the specified effigy,
         *  if it is an effigy for an instance of FSMActor.
         *  @param effigy The effigy for an FSMActor.
         *  @return A new GRTableau, if the effigy is a PtolemyEffigy
         *   that references an FSMActor, or null otherwise.
         *  @exception Exception If an exception occurs when creating the
         *   tableau.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (!(effigy instanceof PtolemyEffigy)) {
                return null;
            }

            ComponentEntity entity = effigy.getEntity("gtTableau");
            if (entity != null && entity instanceof GTTableau) {
                return (Tableau) entity;
            }

            NamedObj model = ((PtolemyEffigy) effigy).getModel();

            if (model instanceof GTCompositeActor || model != null
                    && !model.attributeList(Factory.class).isEmpty()) {
                LibraryAttribute library;
                if (model instanceof FSMMatcher) {
                    library = (LibraryAttribute) getAttribute("state",
                            LibraryAttribute.class);
                } else {
                    library = (LibraryAttribute) getAttribute("_library",
                            LibraryAttribute.class);
                }

                GTTableau tableau = new GTTableau((PtolemyEffigy) effigy,
                        effigy.uniqueName("gtTableau"), library);
                return tableau;
            } else {
                return null;
            }
        }

    }

    public static class ModalTableauFactory extends TableauFactory {

        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this entity.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public ModalTableauFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Create a tableau for the specified effigy, which is assumed to
         *  be an effigy for an instance of ModalModel.  This class
         *  defers to the configuration containing the specified effigy
         *  to open a tableau for the embedded controller.
         *  @param effigy The model effigy.
         *  @return A tableau for the effigy, or null if one cannot be created.
         *  @exception Exception If the factory should be able to create a
         *   Tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            Configuration configuration = (Configuration) effigy.toplevel();
            ModalModelMatcher model = (ModalModelMatcher) ((PtolemyEffigy) effigy)
                    .getModel();
            return configuration.openModel(model.getController());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Background color. */
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);
}
