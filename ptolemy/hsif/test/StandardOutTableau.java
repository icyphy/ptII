/* A tableau that displays its contents to Standard Out

Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.hsif.test;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// StandardOutTableau

/**
   A tableau that writes its contents to standard out.

   @author  Christopher Hylands
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (cxh)
   @see Effigy
*/
public class StandardOutTableau extends Tableau {
    /** Construct a new tableau for the model represented by the given effigy.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public StandardOutTableau(StandardOutEffigy container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make the tableau editable or uneditable.  Notice that this does
     *  not change whether the effigy is modifiable, so other tableaux
     *  on the same effigy may still modify the associated file.
     *  @param flag False to make the tableau uneditable.
     */
    public void setEditable(boolean flag) {
        super.setEditable(flag);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates a standard output writer tableaux for
     *  Ptolemy models.
     */
    public static class Factory extends TableauFactory {
        /** Create a factory with the given name and container.
         *  @param container The container entity.
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

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** If the specified effigy is a StandardOutEffigy and it
         *  already contains a tableau named
         *  "standardOutTableau", then return that tableau; otherwise, create
         *  a new instance of StandardOutTableau in the effigy
         *  specified, and name it "standardOutTableau" and return that tableau.
         *  If the specified effigy is not an instance of StandardOutEffigy,
         *  but contains an instance of StandardOutEffigy, then open a tableau
         *  for that effigy.  If it is a PtolemyEffigy, then create a
         *  text effigy with the MoML representation of the model.
         *  Finally, if is not a StandardOutEffigy or a PtolemyEffigy,
         *  and it does not contain a StandardOutEffigy, then attempt to
         *  open its URL and display its date by creating a text effigy,
         *  which will then be contained by the specified effigy. If all
         *  of this fails, then do not create a tableau and return null.
         *  It is the responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The effigy.
         *  @return A text editor tableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof StandardOutEffigy) {
                // First see whether the effigy already contains a
                // StandardOutTableau with the appropriate name.
                StandardOutTableau tableau = (StandardOutTableau) effigy
                                .getEntity("standardOutTableau");

                if (tableau == null) {
                    tableau = new StandardOutTableau((StandardOutEffigy) effigy,
                            "standardOutTableau");
                }

                tableau.setEditable(effigy.isModifiable());
                return tableau;
            } else {
                return null;
            }
        }
    }
}
