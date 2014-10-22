/* An object that can create a tableau for a Ptolemy II model.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.util.Iterator;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PtolemyTableauFactory

/**
 This is an intermediate container tableau factory that is designed to contain
 all tableau factories in a configuration that are capable of displaying a
 Ptolemy II model.  This class sets up the effigy with a set of available
 views.  Tableaux can use that to set up a View menu which offers alternative
 views besides the default view. Subclasses of this class will usually
 be inner classes of a Tableau, and will create the Tableau.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see Configuration
 @see Effigy
 @see Tableau
 */
public class PtolemyTableauFactory extends TableauFactory {
    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public PtolemyTableauFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a tableau for the specified effigy. The tableau will
     *  created with a new unique name with the specified effigy as its
     *  container.  If the effigy is not an instance of PtolemyEffigy,
     *  then return null.  Otherwise, set up the list of alternative
     *  views in the PtolemyEffigy and then delegate to the first
     *  contained factory that can display the model.
     *  @param effigy The model effigy.
     *  @return A tableau for the effigy, or null if one cannot be created.
     *  @exception Exception If the factory should be able to create a
     *   Tableau for the effigy, but something goes wrong.
     */
    @Override
    public Tableau createTableau(Effigy effigy) throws Exception {
        if (!(effigy instanceof PtolemyEffigy)) {
            return null;
        }

        // Indicate to the effigy that this factory contains effigies
        // offering multiple views of the effigy data.
        effigy.setTableauFactory(this);

        // Delegate to the first contained effigy to open a view.
        Tableau tableau = null;
        Iterator<TableauFactory> factories = attributeList(TableauFactory.class)
                .iterator();

        if (factories.hasNext()) {
            TableauFactory factory = factories.next();
            tableau = factory.createTableau(effigy);
            if (tableau != null) {
                factory._configureTableau(tableau);
            }
        }

        return tableau;
    }
}
