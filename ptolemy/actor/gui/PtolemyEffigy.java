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

package ptolemy.actor.gui;

import ptolemy.kernel.CompositeEntity;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.*;
import ptolemy.gui.MessageHandler;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

//////////////////////////////////////////////////////////////////////////
//// PtolemyEffigy
/**
An effigy for a Ptolemy II model.

@author Steve Neuendorffer
@version $Id$
*/
public class PtolemyEffigy extends Effigy implements ChangeListener {

    /** Create a new proxy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this proxy.
     */
    public PtolemyEffigy(Workspace workspace) {
	super(workspace);
    }

    /** Create a new proxy in the given directory with the given name.
     *  @param container The directory that contains this proxy.
     *  @param name The name of this proxy.
     */
    public PtolemyEffigy(ModelDirectory container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** React to the fact that a change has been successfully executed.
     *  This method does nothing.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {}

    /** React to the fact that a change has triggered an error by
     *  reporting the error in a top-level dialog.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Do not report if it has already been reported.
        if (!change.isErrorReported()) {
            change.setErrorReported(true);
            MessageHandler.error("Change failed", exception);
        }
    }

    /** Return the ptolemy model proxied by this object.
     *  @return The model, or null if none has been set.
     */
    public NamedObj getModel() {
	return _model;
    }

    /** Set the ptolemy model that this is an effigy of.
     *  Register with that model as a change listener.
     *  @param model The model.
     */
    public void setModel(NamedObj model) {
        if (_model != null) {
            _model.removeChangeListener(this);
        }
        _model = model;
        if (model != null) {
            _model.addChangeListener(this);
        }
    }

    /** A factory for creating new ptolemy effigies.
     */
    public static class Factory extends EffigyFactory {
	/** Create a factory with the given name and container.
	 *  @param container The container.
	 *  @param name The name.
	 *  @exception IllegalActionException If the container is incompatible
	 *   with this entity.
	 *  @exception NameDuplicationException If the name coincides with
	 *   an entity already in the container.
	 */
	public Factory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	    super(container, name);
	}

	/** Create a new effigy in the given directory.  This class
	 *  overrides the base class to create a new Ptolemy Effigy
	 *  whose model is set to a TypedCompositeActor.
	 */
	public Effigy createEffigy(ModelDirectory directory) 
	    throws NameDuplicationException, IllegalActionException {
	    PtolemyEffigy effigy = new PtolemyEffigy(directory, 
				     directory.uniqueName("effigy"));
	    effigy.setModel(new TypedCompositeActor(new Workspace()));
	    return effigy;
	}		
    }
	
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The model associated with this proxy.
    private NamedObj _model;
}

