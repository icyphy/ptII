/* A tableau representing a plot window.

 Copyright (c) 2000-2003 The Regents of the University of California.
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
@ProposedRating Red (rowland@eecs.berkeley.edu)
@AcceptedRating
*/

package ptolemy.actor.gui;

import java.awt.Frame;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JFrame;

import ptolemy.gui.MessageHandler;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DialogTableau
/**
A tableau representing a Dialog in a toplevel window.
<p>
The code herein supports a different way of doing dialogs. In particular, the
dialog is not a JDialog, but a JFrame. In particular, dialogs under this new
system will be non-modal. DialogTableau is just like all the other XXXTableau
classes except that the Frame associated with DialogTableau is not an extension
of TableauFrame, and, ultimately, the Top class. The reason being that Top
adorns the GUI manifestation with the normal status bar which isn't appropriate
for a dialog.
<p>
Presently, the only dialog that is supported under the new system is
PortConfigurerDialog.
There can be any number of instances of this class in an effigy, however, there
can only be one each for the model represented by the effigy, and one each of
the actors that are part of the model.

@author  Rowland R Johnson
@version
@since Ptolemy II 3.1
@see Effigy
*/
public class DialogTableau extends Tableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  Use setFrame() to specify the Dialog after construction.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public DialogTableau(Effigy container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container.workspace());
        if (!(container instanceof PtolemyEffigy)) {
            throw new IllegalActionException(
                this,
                "Effigy for Dialog must be an instance of " + "PtolemyEffigy.");
        }
        setName(name);
        setTitle(name);
        setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a tableau for the specified dialog. First look to see if a dialog
     *  already exists. If so, then just return that one.
     * @param parent The Frame parent
     * @param configuration The configuration
     * @param effigy The Effigy containg the model that needs a dialog
     * @param dialogClass The Dialog class to create.
     * @param target The entity that needs the Dialog
     * @return
     */
    public static DialogTableau createDialog(
        Frame parent,
        Configuration configuration,
        Effigy effigy,
        Class dialogClass,
        Entity target) {
        if ((effigy instanceof PtolemyEffigy)
            && dialogClass == PortConfigurerDialog.class) {
            // First see whether the effigy already contains a
            // PortConfigurerDialog on this entity.
            Iterator dialogs =
                effigy.entityList(DialogTableau.class).iterator();
            while (dialogs.hasNext()) {
                DialogTableau existingDialog = (DialogTableau) dialogs.next();
                JFrame dialogJFrame = existingDialog.getFrame();
                if (dialogJFrame instanceof PortConfigurerDialog) {
                    PortConfigurerDialog pcd =
                        (PortConfigurerDialog) dialogJFrame;
                    if (pcd.getTarget() == target) {
                        return existingDialog;
                    }
                }
            }
            // A DialogTableau doesn't exist, so create one.
            DialogTableau newDialog;
            try {
                newDialog =
                    new DialogTableau(
                        effigy,
                        "Configure ports for " + target.getFullName());
                PortConfigurerDialog pcd =
                    new PortConfigurerDialog(
                        newDialog,
                        parent,
                        target,
                        configuration);
                newDialog.setFrame(pcd);
                return newDialog;
            } catch (Exception ex) {
                MessageHandler.error(
                    "Failed to create a DialogTableau for "
                        + target.getFullName());
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** A factory that creates dialog tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {

        /** Create an factory with the given name and container.
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

        /** Create a tableau in the default workspace with no name for the
         *  given Effigy.  The tableau will created with a new unique name
         *  in the given model effigy.  If this factory cannot create a tableau
         *  for the given effigy (perhaps because the effigy is not of the
         *  appropriate subclass) then return null.
         *  It is the responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The model effigy.
         *  @return A new ActorGraphTableau, if the effigy is a
         *  PtolemyEffigy, or null otherwise.
         *  @exception Exception If an exception occurs when creating the
         *  tableau.
         */
        public DialogTableau createDialog(
            Effigy effigy,
            Class dialogClass,
            Entity target)
            throws Exception {
            if ((effigy instanceof PtolemyEffigy)
                && dialogClass == PortConfigurerDialog.class) {
                // First see whether the effigy already contains a
                // PortConfigurerDialog on this target.
                Iterator dialogs =
                    effigy.entityList(PortConfigurerDialog.class).iterator();
                while (dialogs.hasNext()) {
                    PortConfigurerDialog pcd =
                        (PortConfigurerDialog) dialogs.next();
                    if (pcd.getTarget() == target) {
                        return pcd.getDialogTableau();
                    }
                }
                DialogTableau dialog = new DialogTableau(effigy, "XXX");
                return dialog;
            } else {
                return null;
            }
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    ////                         public varialbles                 ////

}
