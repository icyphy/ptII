/* An actor that hold the last event and outputs a constant signal.

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

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
//import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// CTZeroOrderHold
/**
An actor that convert event into continuous signal. This class act
as the zero order hold. It consume the token at the postfire()
phase, since that is the point there the state of the system is
determined. This value will be hold and emitted every time it is
fired.
FIXME: Consider bypass itself, if it is not connected to any opaque
compositeActor. Do it by override getReceivers(), which return the
receivers of the port its output are connected.
@author Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class CTZeroOrderHold extends CTActor{

    public static final boolean DEBUG = true;

    /** Construct a CTActor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  A CTActor can be either dynamic, or not.  It must be set at the
     *  construction time and can't be changed thereafter.
     *  A dynamic actor will produce a token at its initialization phase.
     *
     *  @param CTSubSystem The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @param isDynamic True if the actor is a dynamic actor
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Name coincides with
     *   an entity already in the container.
     */
    public CTZeroOrderHold(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setMultiport(false);
        input.setInput(true);
        input.setOutput(false);
        input.setDeclaredType(DoubleToken.class);
        output = new TypedIOPort(this, "output");
        output.setMultiport(false);
        output.setInput(false);
        output.setOutput(true);
        output.setDeclaredType(DoubleToken.class);

    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Read the input receiver, if there is an event, remember the
     *  value.
     *  @return true Always.
     *  @exception IllegalActionException Never thrown.
     */
    public boolean postfire() throws IllegalActionException {
        if(input.hasToken(0)) {
            _lasteventvalue = ((DoubleToken)input.get(0)).doubleValue();
            if(DEBUG) {
                CTDirector dir = (CTDirector) getDirector();
                System.out.println("Receive an event at: " +
                    dir.getCurrentTime());
                System.out.println("Event value="+_lasteventvalue);
            }
        }
        return true;
    }

    /** Output a doubleToken of the last event value.
     *
     *  @exception IllegalActionException Never thrown.
     */
    public void fire()  throws IllegalActionException{
        output.broadcast(new DoubleToken(_lasteventvalue));
    }

    public TypedIOPort input;
    public TypedIOPort output;
    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private double _lasteventvalue;
}
