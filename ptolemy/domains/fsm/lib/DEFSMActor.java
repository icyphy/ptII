/* A DE FSM Actor

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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.domains.fsm.lib;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.graph.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// DEFSMActor
/**
An FSMController is an FSM controller.

@authors Xiaojun Liu
@version $Id$
*/
public class DEFSMActor extends FSMController implements TypedActor {

    public DEFSMActor(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    /*  public double getCurrentTime() throws IllegalActionException {
        DEDirector dir = (DEDirector)getDirector();
        if (dir == null) {
        throw new IllegalActionException("No director available");
        }
        return dir.getCurrentTime();
        }

        public double getStartTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir == null) {
        throw new IllegalActionException("No director available");
	}
	return dir.getStartTime();
        }

        public double getStopTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir == null) {
        throw new IllegalActionException("No director available");
	}
	return dir.getStopTime();
        }

        public void refireAfterDelay(double delay) throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	// FIXME: the depth is equal to zero ???
        // If this actor has input ports, then the depth is set to be
        // one higher than the max depth of the input ports.
        // If this actor has no input ports, then the depth is set to
        // to be zero.

        dir.fireAfterDelay(this, delay);
        }
    */

    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedAtomicActor.newPort: Internal error: " +
		    ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }

    public List typeConstraintList()  {
	try {
	    workspace().getReadAccess();

	    List result = new LinkedList();
	    Iterator inPorts = inputPortList().iterator();
	    while (inPorts.hasNext()) {
	        TypedIOPort inport = (TypedIOPort)inPorts.next();
		boolean isUndeclared = inport.getTypeTerm().isSettable();
		if (isUndeclared) {
		    Iterator outPorts = outputPortList().iterator();
	    	    while (outPorts.hasNext()) {
		    	TypedIOPort outport =
                            (TypedIOPort)outPorts.next();

			isUndeclared = outport.getTypeTerm().isSettable();
		    	if (isUndeclared && inport != outport) {
			    // output also undeclared, not bi-directional port,
		            Inequality ineq = new Inequality(
                                    inport.getTypeTerm(), outport.getTypeTerm());
			    result.add(ineq);
			}
		    }
		}
	    }
	    return result;

	}finally {
	    workspace().doneReading();
	}
    }

    /** Return the type constraints of this actor.
     *  This method calls typeConstraintList() and convert the result into
     *  an enumeration.
     *  @return An enumeration of inequalities.
     *  @deprecated Use typeconstraintList() instead.
     */
    public Enumeration typeConstraints()  {
	return Collections.enumeration(typeConstraintList());
    }
}

