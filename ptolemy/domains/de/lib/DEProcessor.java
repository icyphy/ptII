/* An opaque composite actor that models a processor with interrupt processing.

 Copyright (c) 1998 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// DEProcessor
/**
This opaque composite actor contains an instance of DEInterruptibleServer and
DEPoisson. The DEPoisson actor is used to model the arrival of interrupts,
which delays the service time of the DEInterruptibleServer actor.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEProcessor extends TypedCompositeActor {

    /** Construct a DEProcessor actor with the default parameters.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEProcessor(TypedCompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        this(container, name, 1.0, 0.5, 0.5);
    }

    /** Construct a DEProcessor actor with the specified parameters.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *  @param minimumServiceTime The minimum service time.
     *  @param interruptServiceTime The interrupt service time.
     *  @param lambda The mean interarrival time of the interrupt.
     *    adder.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEProcessor(TypedCompositeActor container,
            String name,
            double minimumServiceTime,
            double interruptServiceTime,
            double lambda)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create and initialize the parameters.
        _minimumServiceTime = 
            new Parameter(this, "MST", new DoubleToken(minimumServiceTime));
        _interruptServiceTime = 
            new Parameter(this, "IST", new DoubleToken(interruptServiceTime));
        _lambda = new Parameter(this, "lambda", new DoubleToken(lambda));

        // create an output port
        output = new TypedIOPort(this, "output", false, true);

        // create input ports
        input = new TypedIOPort(this, "input", true, false);

        // create and attach a local director
        DEDirector localDir = new DEDirector(name + " local director");
        this.setDirector(localDir);
        
        // create the actors.
        DEInterruptibleServer iServer = new DEInterruptibleServer(this, 
                "InterruptibleServer");
        DEPoisson poisson = new DEPoisson(this, "InterruptPoisson");

        // connect the actors
        this.connect(input, iServer.input);
        this.connect(poisson.output, iServer.interrupt);
        this.connect(iServer.output, output);

        // get the inner parameters.
        Parameter iServerMST = 
            (Parameter)iServer.getAttribute("MinimumServiceTime");
        Parameter iServerIST = 
            (Parameter)iServer.getAttribute("InterruptServiceTime");
        Parameter poissonLambda = 
            (Parameter)poisson.getAttribute("lambda");
        
        // make inner parameters depend on outer.

        if (iServerMST == null) {
            System.out.println("Weird bug");
        }

        iServerMST.setExpression(_minimumServiceTime.getName());
        //_minimumServiceTime.addParameterListener(iServerMST);
        iServerMST.evaluate();

        iServerIST.setExpression(_interruptServiceTime.getName());
        //_interruptServiceTime.addParameterListener(iServerIST);
        iServerIST.evaluate();

        poissonLambda.setExpression(_lambda.getName());
        //_lambda.addParameterListener(poissonLambda);
        poissonLambda.evaluate();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the ports.
    public TypedIOPort input;
    public TypedIOPort output;

    private Parameter _minimumServiceTime;
    private Parameter _interruptServiceTime;
    private Parameter _lambda;

}






