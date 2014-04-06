/* A tableau factory for SCR models.

 Copyright (c) 2003-2013 The Regents of the University of California.
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
package ptolemy.vergil.scr;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.FSMDirector;
import ptolemy.domains.scr.SCRModel;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SCRTableauFactory

/**
 A tableau factory that opens an editor on the contained controller
 rather than this composite actor.  This is triggered by look inside.

 @author Patricia Derler
 @version $Id: ModalTableauFactory.java 65763 2013-03-07 01:54:37Z cxh $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (pd)
 @Pt.AcceptedRating Red (pd)
 */
public class SCRTableauFactory extends TableauFactory {
	
    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public SCRTableauFactory(NamedObj container, String name)
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
    public Tableau createTableau(Effigy effigy) throws Exception {
        Configuration configuration = (Configuration) effigy.toplevel();
        SCRModel model = null;
        if (effigy instanceof PtolemyEffigy) {
        	PtolemyEffigy ptolemyEffigy = (PtolemyEffigy) effigy;
        	if (ptolemyEffigy.getModel() instanceof SCRModel) {
        		model = (SCRModel) ptolemyEffigy.getModel();
        	} else {
        		throw new IllegalActionException("SCRTableau cannot be created for models other than SCRModels");
        	}
        }
        FSMActor controller = ((FSMDirector) model.getDirector())
                .getController();
       
        Tableau tableau = configuration.openModel(controller);
        tableau.setContainer(effigy);
        return tableau;
    }
}
