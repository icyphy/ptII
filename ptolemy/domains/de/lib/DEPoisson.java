 /* An actor that generates events according to Poisson process.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

// FIXME: Delete these when infrastructure improves (see FIXME below).
import java.util.Enumeration;
import collections.LinkedList;
import ptolemy.graph.Inequality;

//////////////////////////////////////////////////////////////////////////
//// DEPoisson
/**
Generate events according to Poisson process. The first event is
always at time zero. The mean inter-arrival time and value of the
events are given as parameters.
FIXME: at current implementation, the first event is not at time zero, rather
it'll depend on the initialization value of current time field in the
director.

@author Lukito Muliadi
@version $Id$
*/
public class DEPoisson extends DEActor {

    /** Construct a DEPoisson actor with the default parameters.
     *  @param container The composite actor that this actor belongs to.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEPoisson(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        meantime = new Parameter(this, "lambda", new DoubleToken(0.1));
        outputvalue = new Parameter(this, "value");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
	try {
	    DEPoisson newobj = (DEPoisson)super.clone(ws);
	    newobj.output = (TypedIOPort)newobj.getPort("output");
	    newobj.outputvalue = (Parameter)newobj.getAttribute("outputvalue");
	    newobj.meantime = (Parameter)newobj.getAttribute("meantime");
	    return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Produce the initializer event that will cause the generation of
     *  the first event at time zero.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        double curTime = getCurrentTime();
	fireAfterDelay(0.0-curTime);
    }

    /** Produce an output event at the current time, and then schedule
     *  a firing in the future.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {

        double lambda = ((DoubleToken)meantime.getToken()).doubleValue();

	// send a token via the output port.
	output.broadcast(outputvalue.getToken());

        // compute an exponential random variable.
        double exp = -Math.log((1-Math.random()))*lambda;
	fireAfterDelay(exp);
    }

    /** Return the type constraint that the output type must be
     *  greater than or equal to the type of the value parameter.
     *  If the the value parameter has not been set, then it is
     *  set to type BooleanToken with value <i>true</i>.
     */
    // FIXME: This should be simplified when infrastructure support improves.
    public Enumeration typeConstraints() {
	if (outputvalue.getToken() == null) {
	    outputvalue.setToken(new BooleanToken(true));
	}
	LinkedList result = new LinkedList();
	Class paramType = outputvalue.getToken().getClass();
        Inequality ineq = new Inequality(new TypeTerm(paramType),
                output.getTypeTerm());
	result.insertLast(ineq);
	return result.elements();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public TypedIOPort output;

    // the mean inter-arrival time and value
    public Parameter meantime;
    public Parameter outputvalue;
}











