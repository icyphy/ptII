/** A graph editor for Ptolemy II models, including the JNI code generator.


 Copyright (c) 1998-2001 The Regents of the University of California.
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


@ProposedRating Red (vincent.arnould@thalesgroup.com)
@AcceptedRating Red (vincent.arnould@thalesgroup.com)
*/

package jni;

import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.actor.ActorGraphTableau;

import java.awt.Color;


//////////////////////////////////////////////////////////////////////////
//// GraphTableau
/**
This is a graph editor for ptolemy models.  It constructs an instance
of ThalesGraphFrame, which contains an editor pane based on diva.

@see ThalesGraphFrame
@see GraphFrame
@author  Steve Neuendorffer
@version $Id$
 * @modelguid {17B1FA87-F877-4EC7-83DD-2977E223764D}
*/
public class ThalesGraphTableau extends ActorGraphTableau {

    /**
     * @modelguid {4BF431E5-F505-43C3-85F1-4EF60FF1E566}
     */
    public ThalesGraphTableau(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }


    /** @modelguid {210B7DAD-02B5-4096-9806-AC9148090BAD} */
    public ThalesGraphTableau(PtolemyEffigy container,
            String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        if(container instanceof PtolemyEffigy) {
            NamedObj model = container.getModel();
            if(model == null) {
                return;
            }
            if (!(model instanceof CompositeEntity)) {
                throw new IllegalActionException(this,
                        "Cannot graphically edit a model "
                        + "that is not a CompositeEntity. Model is a "
                        + model);
            }
            CompositeEntity entity = (CompositeEntity)model;

            ThalesGraphFrame frame = new ThalesGraphFrame(entity, this);
            setFrame(frame);
            frame.setBackground(BACKGROUND_COLOR);
            frame.setVisible(true);
        }
    }

   ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** A factory that creates graph editing tableaux for Ptolemy models.
     * @modelguid {E7634A22-EF13-4A8F-8B5A-E6D899403976}
     */
    public static class Factory extends TableauFactory {

        /** Create an factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         * @modelguid {C1DA2593-D008-4C56-BA6F-4A0907E7385F}
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
         *  @return A new ThalesGraphTableau, if the effigy is a
         *  PtolemyEffigy, or null otherwise.
         *  @exception Exception If an exception occurs when creating the
         *  tableau.
         * @modelguid {B084377C-A065-4503-80E9-3D33194885C3}
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if(effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a RunTableau.
                ThalesGraphTableau tableau =
                    (ThalesGraphTableau)effigy.getEntity("graphTableau");
                if (tableau == null) {
                    tableau = new ThalesGraphTableau(
                            (PtolemyEffigy)effigy, "graphTableau");
                }
                // Don't call show() here, it is called for us in
                // TableauFrame.ViewMenuListener.actionPerformed()
                return tableau;
            } else {
                return null;
            }
        }
    }

     ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The background color.
    /** @modelguid {F40C89BB-670F-485F-931E-AED8CAC4D97D} */
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);


}
