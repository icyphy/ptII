/* An icon editor for Ptolemy II models.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import java.awt.Color;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.LibraryAttribute;

//////////////////////////////////////////////////////////////////////////
//// EditIconTableau
/**
This is an icon editor for ptolemy models.  It constructs an instance
of EditIconFrame, which contains an editor pane based on diva.

@see EditIconFrame
@author  Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class EditIconTableau extends Tableau {

    /** Create a tableau in the specified workspace.
     *  @param workspace The workspace.
     */
    public EditIconTableau(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    /** Create a tableau with the specified container and name, with
     *  no specified default library.
     *  @param container The container.
     *  @param name The name.
     */
    public EditIconTableau(PtolemyEffigy container,
            String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null);
    }

    /** Create a tableau with the specified container, name, and
     *  default library.
     *  @param container The container.
     *  @param name The name.
     *  @param defaultLibrary The default library, or null to not specify one.
     */
    public EditIconTableau(
            PtolemyEffigy container,
            String name,
            LibraryAttribute defaultLibrary)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        if (container instanceof PtolemyEffigy) {
            NamedObj model = container.getModel();
            if (model == null) {
                return;
            }
            if (model instanceof EditorIcon) {
                EditorIcon entity = (EditorIcon)model;

                EditIconFrame frame = new EditIconFrame(
                        entity, this, defaultLibrary);
                setFrame(frame);
                frame.setBackground(BACKGROUND_COLOR);
                return;
            } else {
                throw new IllegalActionException(this,
                        "Cannot edit an icon "
                        + "that is not an instance of EditorIcon.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The background color.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** A factory that creates icon editing tableaux for Ptolemy models.
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
         *  @return A new EditIconTableau, if the effigy is a
         *  PtolemyEffigy, or null otherwise.
         *  @exception Exception If an exception occurs when creating the
         *  tableau.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a graphTableau.
                EditIconTableau tableau =
                    (EditIconTableau)effigy.getEntity("editIconTableau");
                if (tableau == null) {
                    // Check to see whether this factory contains a
                    // default library.
                    // FIXME: It probabaly should!
                    LibraryAttribute library = (LibraryAttribute)getAttribute(
                            "_library", LibraryAttribute.class);
                    tableau = new EditIconTableau(
                            (PtolemyEffigy)effigy, "editIconTableau", library);
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
