/* An execution listener that suspends execution based on breakpoints.

 Copyright (c) 1999-2002 SUPELEC and The Regents of the University of
 California.  All rights reserved.  Permission is hereby granted,
 without written agreement and without license or royalty fees, to
 use, copy, modify, and distribute this software and its documentation
 for any purpose, provided that the above copyright notice and the
 following two paragraphs appear in all copies of this software.

 IN NO EVENT SHALL SUPELEC OR THE UNIVERSITY OF CALIFORNIA BE LIABLE
 TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR
 CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 DOCUMENTATION, EVEN IF SUPELEC OR THE UNIVERSITY OF CALIFORNIA HAVE
 BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND SUPELEC SPECIFICALLY DISCLAIM ANY
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA AND SUPELEC HAVE NO OBLIGATION TO PROVIDE MAINTENANCE,
 SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (celaine@eecs.berkeley.edu)
@AcceptedRating Red (celaine@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptdb;

import diva.canvas.Figure;

import ptolemy.actor.FiringEvent;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.kernel.DebugRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

////////////////////////////////////////////////////////////////////////
//// DebugController
/**
An execution listener that suspends execution based on breakpoints.

@author Elaine Cheong
@version $Id$
*/
public class DebugController implements DebugListener {

    /**
     * Construct a new debug controller.
     * @param object The object that the listener should break on.
     * @param graphController The graph controller of the object.
     * FIXME: What exactly is the graph controller???
     */
    public DebugController(NamedObj object,
            BasicGraphController graphController) {
        _object = object;
        _graphController = graphController;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Ignore string messages.
     */
    public void message(String string) {
    }

    /**
     * Ignore debug events that aren't firing events.
     * @param debugEvent The debug event.
     * @see ptolemy.vergil.actor.ActorViewerGraphController#event
     */
    public void event(DebugEvent debugEvent) {
	if (debugEvent instanceof FiringEvent) {
	    FiringEvent event = (FiringEvent) debugEvent;

            if (event.getActor() == _object) {
                // Highlight the actor that we are breaking on.

                NamedObj objToHighlight = _object;                

                // If the object is not contained by the associated
                // composite, then find an object above it in the hierarchy
                // that is.
                // FIXME: Not sure if this implementation is right...

                AbstractBasicGraphModel graphModel =
                    (AbstractBasicGraphModel)_graphController.getGraphModel();
                NamedObj toplevel = graphModel.getPtolemyModel();
                
                // FIXME: why is this null check needed?
                while (objToHighlight != null
                        && objToHighlight.getContainer() != toplevel) {
                    objToHighlight = (NamedObj)objToHighlight.getContainer();
                }
                if (objToHighlight == null) {
                    return;
                }
                Object location = objToHighlight.getAttribute("_location");
                if (location != null) {
                    Figure figure = _graphController.getFigure(location);
                    if (figure != null) {
                        if (_debugRenderer == null) {
                            _debugRenderer = new DebugRenderer();
                        }
                        if (event.getType() == FiringEvent.BEFORE_PREFIRE) {
                            _debugRenderer.renderSelected(figure);
                            _debugRendered = figure;
                            System.out.println("DebugController: prefire " + event.getActor().toString());
                        } else if (event.getType() == FiringEvent.BEFORE_FIRE) {
                            System.out.println("DebugController: fire");
                        } else if (event.getType() == FiringEvent.BEFORE_POSTFIRE) {
                            System.out.println("DebugController: postfire");
                        } else if (event.getType() == FiringEvent.AFTER_POSTFIRE) {
                            System.out.println("DebugController: postpostfire");

                    
                            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                            try {
                                console.readLine();
                                if (_debugRendered != null) {
                                    _debugRenderer.renderDeselected(_debugRendered);
                                }
                            } catch (IOException e) {
                                //do nothing
                            }
                            
                        }
                    }
                }
            }
        }
    }
            

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Listen to this object
    private NamedObj _object;

    private DebugRenderer _debugRenderer = null;
    private Figure _debugRendered = null;

    private BasicGraphController _graphController = null;

}
