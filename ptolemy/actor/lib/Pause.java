/* An actor that pauses a model executing when it receives a true token.

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
package ptolemy.actor.lib;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// Stop

/** An actor that pauses execution of a model when it receives a true
 token on any input channel. This is accomplished by calling 
 pause() on the manager.


 @author Patricia Derler
 @version $Id: Stop.java 67499 2013-09-14 16:12:58Z cxh $
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (pd)
 @Pt.AcceptedRating Red (pd)
 */
public class Pause extends Sink {
	
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Pause(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.BOOLEAN);

        // Icon is a stop sign.
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-8,-19 8,-19 19,-8 19,8 8,19 "
                + "-8,19 -19,8 -19,-8\" " + "style=\"fill:cyan\"/>\n"
                + "<text x=\"-15\" y=\"4\""
                + "style=\"font-size:11; fill:black; font-family:SansSerif\">"
                + "PAUSE</text>\n" + "</svg>\n");

        // Hide the name because the name is in the icon.
        _hideName = new SingletonParameter(this, "_hideName");
        _hideName.setToken(BooleanToken.TRUE);
        _hideName.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The parameter that hides the name of the actor.  The default
     * value is true.
     */
    public SingletonParameter _hideName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input channel that has a token,
     *  and if any token is true, call pause() on the manager.
     *  If nothing at all is connected to the input port, then
     *  do nothing.
     *  @exception IllegalActionException If there is no director or
     *   if there is no manager, or if the container is not a
     *   CompositeActor.
     *  @return False if this actor is trying to pause execution if any
     *  token is true or if the input is not connected.  If this actor
     *  is not trying to pause execution, then return the value
     *  returned by super.postfire().
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = false;

        if (!input.isOutsideConnected()) {
            result = true;
        }

        // NOTE: We need to consume data on all channels that have data.
        // If we don't then DE will go into an infinite loop.
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                if (((BooleanToken) input.get(i)).booleanValue()) {
                    result = true;
                }
            }
        }

        if (result) {
            getManager().pause();
            
            
        }

        // If this actor is not trying to stop execution, then return
        // the value returned by super.postfire().
        boolean superResults = super.postfire();
        if (!result) {
            return superResults;
        }

        return !result;
    }
}
