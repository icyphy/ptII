/* A tableau representing a dialog window.

 Copyright (c) 2003 The Regents of the University of California.
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
import java.lang.reflect.Constructor;
import java.util.Iterator;

import javax.swing.JFrame;

import ptolemy.util.MessageHandler;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DialogTableau
/**
A tableau representing a Dialog in a toplevel window.
<p>
DialogTableau is just like all the other XXXTableau classes except that the
Frame associated with DialogTableau is not an extension of TableauFrame, and,
ultimately, the Top class. The reason being that Top adorns the GUI
manifestation with the normal status bar which isn't appropriate for a dialog.
In addition, the created dialog is not a JDialog, but a JFrame. And, the created
dialog is non-modal.
<p>
There can be any number of instances of this class in an effigy, however, there
can only be one each for the model represented by the effigy, and one each of
the actors that are part of the model.

@author  Rowland R Johnson
@version $Id$
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
        if (!(container instanceof Effigy)) {
            throw new IllegalActionException(
                this,
                "Effigy for Dialog must be an instance of " + "Effigy.");
        }
        setName(name);
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
     * @return DialogTableau
     */
    public static DialogTableau createDialog(
        Frame parent,
        Configuration configuration,
        Effigy effigy,
        Class dialogClass,
        Entity target) {

        if (PtolemyDialog.class.isAssignableFrom(dialogClass)) {
            // First see whether the effigy already contains a dialog of
            // dialogClas on this entity.
            if (effigy instanceof HTMLEffigy) {
                Iterator dialogs =
                    effigy.entityList(DialogTableau.class).iterator();
                while (dialogs.hasNext()) {
                    DialogTableau dialogTableau =
                        (DialogTableau) dialogs.next();
                    PtolemyDialog existingDialog =
                        ((PtolemyDialog) (dialogTableau.getFrame()));
                    if ((existingDialog.getClass() == dialogClass)
                        && (dialogTableau.hasTarget(target))) {
                        return dialogTableau;
                    }
                }
            }
            // Now, do the same test on the container of the effigy.
            NamedObj container = (NamedObj) (effigy.getContainer());
            if ((container != null) && (container instanceof PtolemyEffigy)) {
                Iterator dialogs =
                    ((PtolemyEffigy) container)
                        .entityList(DialogTableau.class)
                        .iterator();
                while (dialogs.hasNext()) {
                    DialogTableau dialogTableau =
                        (DialogTableau) dialogs.next();
                    PtolemyDialog existingDialog =
                        ((PtolemyDialog) (dialogTableau.getFrame()));
                    if ((existingDialog.getClass() == dialogClass)
                        && (dialogTableau.hasTarget(target))) {
                        return dialogTableau;
                    }
                }
            }
        }

        // A DialogTableau doesn't exist, so create one.
        DialogTableau newDialogTableau;
        try {
            newDialogTableau =
                new DialogTableau(effigy, effigy.uniqueName("dialog"));
            PtolemyDialog dialog = null;
            Constructor[] constructors = dialogClass.getConstructors();
            Constructor constructor = null;
            for (int i = 0; i < constructors.length; i++) {
                Class pType[] = constructors[i].getParameterTypes();
                if (pType.length == 4
                    && pType[0] == DialogTableau.class
                    && pType[1] == Frame.class
                    && pType[2] == Entity.class
                    && pType[3] == Configuration.class) {
                    constructor = constructors[i];
                    break;
                }
            }
            if (constructor != null) {
                Object args[] = new Object[4];
                args[0] = newDialogTableau;
                args[1] = parent;
                args[2] = target;
                args[3] = configuration;
                dialog = (PtolemyDialog) constructor.newInstance(args);
            }
            if (dialog == null) {
                throw new KernelException(
                    target,
                    null,
                    "Can't create a " + dialogClass);
            }

            newDialogTableau.setFrame(dialog);
            return newDialogTableau;
        } catch (Exception ex) {
            MessageHandler.error(
                "Failed to create a DialogTableau for " + target.getFullName());
        }
        return null;
    }

    /** Get the target associated with this DialogTableau. Actually, the target
     *  is associated with the frame that is associated with this DialogTableau.
     * @return target The Entity that this DialogTableau is associated with.
     */
    public Entity getTarget() {
        JFrame dialogJFrame = getFrame();
        if (dialogJFrame instanceof PtolemyDialog) {
            PtolemyDialog dialog = (PtolemyDialog) dialogJFrame;
            return dialog.getTarget();
        }
        return null;
    }

    /** Determines if a particular Entity is associated with this DialogTableau
     * @param entity Entity that test is performed on.,
     * @return true/false True if entity is associated with this DialogTableau.
     */
    public boolean hasTarget(Entity entity) {
        Entity target = getTarget();
        if (target != null && target == entity)
            return true;
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    ////                         public varialbles                 ////
}
