/* A merge actor for the Continuous domain.

 Copyright (c) 1997-2013 The Regents of the University of California.
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
package ptolemy.domains.continuous.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ContinuousMerge

/**
 A merge actor for the Continuous domain. This port has a multiport
 input and on each firing, sends the input from channel 0 to the
 output, if the input at channel 0 is present. Otherwise, it sends
 the input from channel 1, if it is present. It continues to search
 the inputs in channel order until it finds one that is present
 or it runs out of input channels. In the latter case, the output
 will be absent.
 <p>
 By default, this actor discards data on all channels after
 the first channel that has present data. If you set the <i>discard</i>
 parameter to false, however, then it will collect all present
 data in queue and output it in the next microstep until all
 present inputs have been produced on the output.
 Note that setting this to false creates a risk of a chattering
 Zeno system if a continuous signal is provided as input.
 <p>
 Note that this actor can merge continuous signals with discrete
 ones, but the resulting signal may not be piecewise continuous.
 This will be a bit odd. It is most useful to merge discrete signals
 or signals that are piecewise continuous.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ContinuousMerge extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name. Create ports and make the input port a multiport.
     *  @param container The container.
     *  @param name The name.
     *  @exception NameDuplicationException If an actor
     *   with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public ContinuousMerge(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setMultiport(true);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-10,20 10,10 10,-10, -10,-20\" "
                + "style=\"fill:green\"/>\n" + "</svg>\n");
        
        discard = new Parameter(this, "discard");
        discard.setExpression("true");
        discard.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Indicator of whether to discard present inputs on channel numbers
     *  higher than the first one. This is a boolean that defaults to true.
     */
    public Parameter discard;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the input channels in order until either a present input
     *  is found or we run out of channels. In the former case, the first
     *  encountered input value is produced on the output. In the latter
     *  case, the output will be absent.
     *  @exception IllegalActionException If there is no director, or
     *   the input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        boolean discardValue = ((BooleanToken)discard.getToken()).booleanValue();
        boolean outputProduced = false;
        if (_pending != null && _pending.size() > 0) {
        	output.send(0, _pending.remove(0));
        	outputProduced = true;
        }
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
            	if (!outputProduced) {
            		output.send(0, input.get(i));
            		outputProduced = true;
            	}
                if (discardValue) {
                	return;
                } else {
                	if (_pending == null) {
                		_pending = new LinkedList<Token>();
                	}
                	_pending.add(input.get(i));
                }
            }
        }
    }
    
    /** Initialize this actor by clearing memory of any pending outputs.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
    	super.initialize();
    	if (_pending != null) {
    		_pending.clear();
    	}
    }
    
    /** If there are any pending outputs, then request a refiring at the
     *  current time.
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        if (_pending != null && _pending.size() > 0) {
        	getDirector().fireAtCurrentTime(this);
        }
        return super.postfire();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Queue of pending output events. */
    private List<Token> _pending;
}
