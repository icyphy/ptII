/* A service for managing the availabled directors.

 Copyright (c) 2000 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy;

// FIXME: Trim this
import diva.gui.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.gui.style.EditableChoiceStyle;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.gui.MessageHandler;
import ptolemy.vergil.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.dde.kernel.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.dt.kernel.*;
import ptolemy.domains.giotto.kernel.GiottoDirector;
import ptolemy.domains.rtp.kernel.*;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// DirectorService
/**
A service that manages the director of the current document.  It provides
a user interface, etc for viewing and setting the director if the current
document is a ptolemy document.

@author Steve Neuendorffer
@version $Id$
*/
public class DirectorService extends AbstractService {
    /**
     * Create a new service and register it with the given application.
     * Add any user interface pieces to the given toolbar.
     */
    public DirectorService(VergilApplication application, JToolBar toolbar) {
	_setApplication(application);
	application.addService(this);

	_directorModel = new DefaultComboBoxModel();
	try {
	    // FIXME MoMLize
	    Director dir;
	    dir = new SDFDirector();
	    dir.setName("SDF");
	    addDirector(dir);
	    dir = new DTDirector();
	    dir.setName("DT");
	    addDirector(dir);
	    dir = new PNDirector();
	    dir.setName("PN");
	    addDirector(dir);
	    dir = new DEDirector();
	    dir.setName("DE");
	    addDirector(dir);
	    dir = new CSPDirector();
	    dir.setName("CSP");
	    addDirector(dir);
	    dir = new DDEDirector();
	    dir.setName("DDE");
	    addDirector(dir);
	    dir = new FSMDirector();
	    dir.setName("FSM");
	    addDirector(dir);

	    dir = new CTMixedSignalDirector();	    
	    dir.setName("CT");
	    Parameter solver;
	    solver = (Parameter)dir.getAttribute("ODESolver");
	    EditableChoiceStyle style;
	    style = new EditableChoiceStyle(solver, "style");
	    new Parameter(style, "choice0", new StringToken(
		"ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver"));
	    new Parameter(style, "choice1", new StringToken(
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver"));
	    new Parameter(style, "choice2", new StringToken(
	        "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver"));

	    solver = (Parameter)dir.getAttribute("breakpointODESolver");
	    style = new EditableChoiceStyle(solver, "style");
	    new Parameter(style, "choice0", new StringToken(
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver"));
	    new Parameter(style, "choice1", new StringToken(
		"ptolemy.domains.ct.kernel.solver.BackwardEulerSolver"));
	    new Parameter(style, "choice2", new StringToken(
		"ptolemy.domains.ct.kernel.solver.ImpulseBESolver"));
            addDirector(dir);

            dir = new CTEmbeddedDirector();	    
	    dir.setName("CTEmbedded");
	    //Parameter solver;
	    solver = (Parameter)dir.getAttribute("ODESolver");
	    //EditableChoiceStyle style;
	    style = new EditableChoiceStyle(solver, "style");
	    new Parameter(style, "choice0", new StringToken(
		"ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver"));
	    new Parameter(style, "choice1", new StringToken(
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver"));
	    new Parameter(style, "choice2", new StringToken(
	        "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver"));

	    solver = (Parameter)dir.getAttribute("breakpointODESolver");
	    style = new EditableChoiceStyle(solver, "style");
	    new Parameter(style, "choice0", new StringToken(
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver"));
	    new Parameter(style, "choice1", new StringToken(
		"ptolemy.domains.ct.kernel.solver.BackwardEulerSolver"));
	    new Parameter(style, "choice2", new StringToken(
		"ptolemy.domains.ct.kernel.solver.ImpulseBESolver"));
	    addDirector(dir);

	    dir = new GiottoDirector();
	    dir.setName("Giotto");
	    addDirector(dir);
            dir = new RTPDirector();
	    dir.setName("RTP");
	    addDirector(dir);
	}
	catch (Exception ex) {
	    MessageHandler.error("Director combobox creation failed", ex);
	}
	//FIXME find these names somehow.
	_directorComboBox = new JComboBox(_directorModel);
	_directorComboBox.setRenderer(new PtolemyListCellRenderer());
	_directorComboBox.setMaximumSize(_directorComboBox.getMinimumSize());
        _directorComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
		    // When a director is selected, update the 
		    // director of the model in the current document.
		    final Director director = (Director) e.getItem();
		    PtolemyDocument d = (PtolemyDocument)
			getApplication().getCurrentView().getDocument();
		    if(d == null) return;
		    CompositeEntity entity = d.getModel();
		    if(entity instanceof CompositeActor) {
			final CompositeActor actor = (CompositeActor) entity;
			final Director oldDirector = actor.getDirector();
                        if((oldDirector == null) || (director.getClass()
                                != oldDirector.getClass())) {
                            actor.requestChange(new ChangeRequest(
                                   this, "Set Director") {
                                protected void _execute() throws Exception {
                                    Director clone = (Director)
                                            director.clone(actor.workspace());
                                    actor.setDirector(clone);
                                }
                            });
                        }					      
		    }
                }
            }
        });
        toolbar.add(_directorComboBox);

	ListDataListener ldl = new ListDataListener() {
	    public void contentsChanged(ListDataEvent event) {		
		// When the current document is changed, set the 
		// director menu to whatever director is currently associated
		// with the model in the document.
		View v = getApplication().getCurrentView();
		
		if(v == null) {
		    _directorModel.setSelectedItem(null);
		    return;
		}
		PtolemyDocument d = (PtolemyDocument)v.getDocument();
                CompositeEntity entity = d.getModel();
                if(!(entity instanceof CompositeActor)) {
                    _directorModel.setSelectedItem(null);
                    return;
                }
		CompositeActor actor = (CompositeActor)entity;
		Director director = actor.getDirector();
		if(director == null) {
		    _directorModel.setSelectedItem(null);
		    return;
		}
		Director foundDirector = null;
		for(int i = 0; foundDirector == null && 
			i < _directorModel.getSize(); i++) {
		    if(director.getClass().isInstance(_directorModel.getElementAt(i))) {
		    	foundDirector = 
		    	(Director)_directorModel.getElementAt(i);
		    }
		}
		_directorModel.setSelectedItem(foundDirector);
	    }
	    public void intervalAdded(ListDataEvent event) {
		contentsChanged(event);
	    }
	    public void intervalRemoved(ListDataEvent event) {
		contentsChanged(event);
	    }
	};
	application.addViewListener(ldl);
    }

    /**
     * Add the given director to the list of directors maintained by
     * this service.
     */
    public void addDirector(Director director) {
	_directorModel.addElement(director);
    }

    /**
     * Return a list of directors that have been registered with this service.
     */
    public List directorList() {
	return null;
    }

    /**
     * Remove the given director from the list of directors maintained by
     * this service.
     */
    public void removeDirector(Director director) {
	_directorModel.removeElement(director);
    }

    // The director selection combobox
    private JComboBox _directorComboBox;

    // The list of directors.
    private DefaultComboBoxModel _directorModel;
}
