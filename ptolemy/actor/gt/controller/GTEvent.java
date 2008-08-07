/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import java.util.List;

import ptolemy.data.ActorToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ParserScope;
import ptolemy.domains.erg.kernel.ERGController;
import ptolemy.domains.erg.kernel.ERGDirector;
import ptolemy.domains.erg.kernel.Event;
import ptolemy.domains.erg.kernel.SchedulingRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// GTEvent

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTEvent extends Event {

    public GTEvent(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        fireOnInput.setVisibility(Settable.NONE);
        isInitialState.setVisibility(Settable.NONE);
        isFinalState.setVisibility(Settable.NONE);
    }

    protected BooleanToken _getMatchArgument(ArrayToken arguments)
    throws IllegalActionException {
        int length = parameters.getParameterNames().size();
        if (arguments.length() > length + 1) {
            return (BooleanToken) arguments.getElement(length + 1);
        } else {
            return null;
        }
    }

    protected ActorToken _getModelArgument(ArrayToken arguments)
    throws IllegalActionException {
        int length = parameters.getParameterNames().size();
        if (arguments.length() > length) {
            return (ActorToken) arguments.getElement(length);
        } else {
            return null;
        }
    }

    protected ArrayToken _getOutputArguments(ArrayToken arguments,
            ActorToken modelToken, BooleanToken matchToken)
            throws IllegalActionException {
        int length = parameters.getParameterNames().size();
        int newLength = length;
        if (matchToken != null) {
            newLength += 2;
        } else if (modelToken != null) {
            newLength ++;
        }
        Token[] tokens = new Token[newLength];
        System.arraycopy(arguments.arrayValue(), 0, tokens, 0, length);
        if (matchToken != null) {
            tokens[length++] = modelToken;
            tokens[length++] = matchToken;
        } else if (modelToken != null) {
            tokens[length++] = modelToken;
        }
        return new ArrayToken(tokens);
    }

    protected void _scheduleEvents(ParserScope scope, ActorToken modelToken,
            BooleanToken matchToken) throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        ERGDirector director = controller.director;

        List<?>[] schedulesArray = new List<?>[2];
        schedulesArray[0] = preemptiveTransitionList();
        schedulesArray[1] = nonpreemptiveTransitionList();
        for (List<?> schedules : schedulesArray) {
            for (Object scheduleObject : schedules) {
                SchedulingRelation schedule =
                    (SchedulingRelation) scheduleObject;
                if (schedule.isEnabled(scope)) {
                    double delay = schedule.getDelay(scope);
                    Event nextEvent = (Event) schedule.destinationState();
                    if (schedule.isCanceling()) {
                        director.cancel(nextEvent);
                    } else {
                        ArrayToken edgeArguments = schedule.getArguments(scope);
                        ArrayToken outputArguments = _getOutputArguments(
                                edgeArguments, modelToken, matchToken);
                        director.fireAt(nextEvent, director.getModelTime().add(
                                delay), outputArguments);
                    }
                }
            }
        }
    }
}
