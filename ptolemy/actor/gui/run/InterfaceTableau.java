/* A tableau that creates a run control panel for a ptolemy model.

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
package ptolemy.actor.gui.run;

import java.util.List;

import javax.swing.JFrame;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// InterfaceTableau

/**
 A tableau that creates a new run control panel for a ptolemy model.
 This panel has controls for parameters of the top-level entity
 and its director, if any, a set of buttons to control execution
 of the model, and a panel displaying the placeable entities within
 the model.

 FIXME: Customization

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class InterfaceTableau extends Tableau {
    /** Create a new run control panel for the model with the given
     *  effigy.  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public InterfaceTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        NamedObj model = container.getModel();

        if (!(model instanceof CompositeActor)) {
            throw new IllegalActionException(this,
                    "Cannot run a model that is not a CompositeActor."
                            + " It is: " + model);
        }

        _manager = ((CompositeActor) model).getManager();
        // Create a manager if necessary.
        if (_manager == null) {
            try {
                _manager = new Manager(model.workspace(), "manager");
                ((CompositeActor) model).setManager(_manager);
            } catch (IllegalActionException ex) {
                throw new IllegalActionException(
                        this,
                        ex,
                        "Failed to set manager.  This can occur if "
                                + "you try to run a non-toplevel model that "
                                + "is a component of a toplevel model.  "
                                + "The solution is invoke View -> Run while in a "
                                + "toplevel window.");
            }
        }

        JFrame frame = new CustomizableRunFrame((CompositeActor) model, this);
        setFrame(frame);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The manager. */
    private Manager _manager;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates run control panel tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** If the specified effigy already contains a tableau named
         *  "runTableau", then return that tableau; otherwise, create
         *  a new instance of RunTableau for the effigy, and
         *  name it "runTableau".  If the specified effigy is not an
         *  instance of PtolemyEffigy, then do not create a tableau
         *  and return null. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *
         *  @param effigy The model effigy.
         *  @return A new run tableau if the effigy is a PtolemyEffigy,
         *    or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains an InterfaceTableau.
                List<InterfaceTableau> list = effigy
                        .entityList(InterfaceTableau.class);
                InterfaceTableau tableau;
                if (list.size() > 0) {
                    // Return the last one (most recently created) in the list.
                    tableau = list.get(list.size() - 1);
                } else {
                    tableau = new InterfaceTableau((PtolemyEffigy) effigy,
                            effigy.uniqueName("interfaceTableau"));
                }
                // Don't call show() here, it is called for us in
                // TableauFrame.ViewMenuListener.actionPerformed()
                return tableau;
            } else {
                return null;
            }
        }
    }
}
