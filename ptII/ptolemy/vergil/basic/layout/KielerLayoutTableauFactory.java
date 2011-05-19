/*  A top-level dialog window for controlling the Kieler graph layout algorithm.

 Copyright (c) 2010 The Regents of the University of California.
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
package ptolemy.vergil.basic.layout;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * A factory that creates a control panel to display Kieler layout controls.
 * @author Christopher Brooks
 * @version $Id: KielerLayoutTableauFactory.java 59265 2010-09-26 17:11:58Z cmot
 *          $
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class KielerLayoutTableauFactory extends TableauFactory {
    /**
     * Create a factory with the given name and container.
     * @param container The container.
     * @param name The name.
     * @exception IllegalActionException If the container is incompatible with
     *                this attribute.
     * @exception NameDuplicationException If the name coincides with an
     *                attribute already in the container.
     */
    public KielerLayoutTableauFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create a new instance of KielerLayoutTableau in the specified effigy. If
     * the specified effigy is not an instance of PtolemyEffigy, then do not
     * create a tableau and return null. It is the responsibility of callers of
     * this method to check the return value and call show().
     * 
     * @param effigy The model effigy.
     * @return A new control panel tableau if the effigy is a PtolemyEffigy, or
     *         null otherwise.
     * @exception Exception If the factory should be able to create a tableau
     *                for the effigy, but something goes wrong.
     */
    public Tableau createTableau(Effigy effigy) throws Exception {
        if (effigy instanceof PtolemyEffigy) {
            KielerLayoutTableau returnTableau = null;
            // First see whether the effigy already contains a tableau.
            returnTableau = (KielerLayoutTableau) effigy
                    .getEntity("KielerLayoutTableau");
            if (returnTableau == null) {
                returnTableau = new KielerLayoutTableau((PtolemyEffigy) effigy,
                        "KielerLayoutTableau");
            }
            return returnTableau;
        } else {
            return null;
        }
    }
}
