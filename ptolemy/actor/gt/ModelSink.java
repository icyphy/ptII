/*  This actor opens a window to display the specified model and applies its inputs to the model.

 @Copyright (c) 1998-2007 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.gt;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.lib.Sink;
import ptolemy.data.ActorToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ModelDisplay

/**
 This actor opens a window to display the specified model.
 If inputs are provided, they are expected to be MoML strings
 that are to be applied to the model. This can be used, for
 example, to create animations.
 
 @author  Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
*/
public class ModelSink extends Sink  {
    
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModelSink(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        input.setTypeEquals(ActorToken.TYPE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Read the input, if there is any, and issue a change
     *  request to apply the MoML in the input to the displayed model.
     *  @exception IllegalActionException If there is an error reading
     *   the input.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.getWidth() > 0 && input.hasToken(0)) {
            if (_tableau != null) {
                _tableau.close();
                _tableau = null;
            }
            
            Entity model = ((ActorToken) input.get(0)).getEntity();
            Configuration configuration =
                (Configuration) Configuration.findEffigy(toplevel()).toplevel();
        	try {
        	    _tableau = configuration.openModel(model);
			} catch (NameDuplicationException e) {
				throw new IllegalActionException(this, e, "Cannot open model.");
			}
        }
    }
    
    public boolean prefire() throws IllegalActionException {
        return input.getWidth() > 0 && input.hasToken(0) && super.prefire();
    }
    
    private Tableau _tableau;
}
