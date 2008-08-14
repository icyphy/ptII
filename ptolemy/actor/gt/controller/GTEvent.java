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

import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.erg.kernel.ERGController;
import ptolemy.domains.erg.kernel.Event;
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
        isInitialEvent.setVisibility(Settable.NONE);
        isFinalEvent.setVisibility(Settable.NONE);
    }

    protected CompositeEntity _getModelVariable()
    throws IllegalActionException {
        ActorParameter actorParameter = _getActorParameter();
        ObjectToken token = (ObjectToken) actorParameter.getToken();
        try {
            CompositeEntity entity = (CompositeEntity) token.getValue();
            return entity;
        } catch (ClassCastException e) {
            throw new IllegalActionException("The object stored in the " +
                    "HostModel parameter must be an instanceof " +
                    "CompositeEntity.");
        }
    }

    protected boolean _getSuccessVariable() throws IllegalActionException {
        Parameter parameter = _getMatchedParameter();
        return ((BooleanToken) parameter.getToken()).booleanValue();
    }

    protected void _setModelVariable(CompositeEntity entity)
    throws IllegalActionException {
        ActorParameter actorParameter = _getActorParameter();
        actorParameter.setToken(new ObjectToken(entity, CompositeEntity.class));
    }

    protected void _setSuccessVariable(boolean patternMatched)
    throws IllegalActionException {
        Parameter parameter = _getMatchedParameter();
        parameter.setToken(BooleanToken.getInstance(patternMatched));
    }

    private ActorParameter _getActorParameter() throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        ActorParameter actorParameter = (ActorParameter) controller
                .getAttribute("HostModel", ActorParameter.class);
        if (actorParameter == null) {
            throw new IllegalActionException("Unable to find the HostModel " +
                    "parameter in the ERG controller of type ActorParameter.");
        }
        return actorParameter;
    }

    private Parameter _getMatchedParameter()
    throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        Parameter parameter = (Parameter) controller.getAttribute("Matched",
                Parameter.class);
        if (parameter == null) {
            throw new IllegalActionException("Unable to find the " +
                    "PatternMatched parameter in the ERG controller.");
        }
        return parameter;
    }
}
