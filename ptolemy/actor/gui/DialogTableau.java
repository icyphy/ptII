/* A tableau representing a plot window.

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
import java.util.Iterator;

import javax.swing.JFrame;

import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

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
    public DialogTableau(Effigy container, String name, String title)
        throws IllegalActionException, NameDuplicationException {
        super(container.workspace());
        if (!(container instanceof PtolemyEffigy)) {
            throw new IllegalActionException(
                this,
                "Effigy for Dialog must be an instance of " + "PtolemyEffigy.");
        }
        setName(name);
        setTitle(title);
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
        if (dialogClass == PortConfigurerDialog.class) {
            if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a
                // PortConfigurerDialog on this entity.
                Iterator dialogs =
                    effigy.entityList(DialogTableau.class).iterator();
                while (dialogs.hasNext()) {
                    DialogTableau existingDialog =
                        (DialogTableau) dialogs.next();
                    if (existingDialog.hasTarget(target)) {
                        return existingDialog;
                    }
                }
            }
            NamedObj container = (NamedObj) (effigy.getContainer());
            if ((container != null) && (container instanceof PtolemyEffigy)) {
                // First see whether the effigy already contains a
                // PortConfigurerDialog on this entity.
                Iterator dialogs =
                    ((PtolemyEffigy) container)
                        .entityList(DialogTableau.class)
                        .iterator();
                while (dialogs.hasNext()) {
                    DialogTableau existingDialog =
                        (DialogTableau) dialogs.next();
                    if (existingDialog.hasTarget(target)) {
                        return existingDialog;
                    }
                }
            }
        }

        // A DialogTableau doesn't exist, so create one.
        DialogTableau newDialog;
        try {
            newDialog =
                new DialogTableau(
                    effigy,
                    effigy.uniqueName("dialog"),
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
                "Failed to create a DialogTableau for " + target.getFullName());
        }
        return null;
    }

    public boolean hasTarget(Entity target) {
        JFrame dialogJFrame = getFrame();
        if (dialogJFrame instanceof PortConfigurerDialog) {
            PortConfigurerDialog pcd = (PortConfigurerDialog) dialogJFrame;
            if (pcd.getTarget() == target) {
                return true;
            }
        }
        return false;
    } ///////////////////////////////////////////////////////////////////
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
