/* A representative of a ptolemy model

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy.fsm;

import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

//////////////////////////////////////////////////////////////////////////
//// FSMEffigyFactory
/**
A factory for creating an effigy for a Ptolemy II FSM model.

@author Steve Neuendorffer
@version $Id$
*/
public class FSMEffigyFactory extends EffigyFactory {
    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public FSMEffigyFactory(CompositeEntity container, String name)
	throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }
    
    /** Create a new effigy in the given directory.  This class
     *  overrides the base class to create a new Ptolemy Effigy
     *  whose model is set to an FSMActor.
     */
    public Effigy createEffigy(ModelDirectory directory) 
	throws NameDuplicationException, IllegalActionException {
	PtolemyEffigy effigy = 
	    new PtolemyEffigy(directory, 
			      directory.uniqueName("effigy"));
	effigy.setModel(new FSMActor(new Workspace()));
	return effigy;
    }		
}

