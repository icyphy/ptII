/* One line description of file.

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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.test;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;


//////////////////////////////////////////////////////////////////////////
//// CTTestValueSink
/**
A sink actor that test its last consumed token value with its parameter
"Value" value (default value 1.0).
If they are equal within a given threshold (1e-1), then
the test is considered successful, i.e. the isSuccessful() method returns
true. Otherwise, the method returns false.
@author  Jie Liu
@version $Id$

*/
public class CTTestValueSink extends TypedAtomicActor {
    /** Constructor
     *  @param container The container.
     *  @param name The name.
     */
    public CTTestValueSink(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setOutput(false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);
        testValue = new Parameter(this, "Value", new DoubleToken(1.0));
        print = new Parameter(this, "Print", new BooleanToken(false));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The single-input port.
     */
    public TypedIOPort input;

    /** Parameter for the value to be tested.
     */
    public Parameter testValue;

    /** Parameter for whether the tokens will be printed to System.out.
     */
    public Parameter print;

    ///////////////////////////////////////////////////////////////////
    ////                         public method                     ////


    /** Return true if the test is successful.
     *  @return true if the test is successful.
     */
    public boolean isSuccessful() {
        return _success;
    }

    /** If the parameter print is true, print the last consumed token
     *  to the stdout.
     *  @return true always.
     *  @exception IllegalActionException If no token available.
     */
    public boolean postfire() throws IllegalActionException {
        _debug(getName() + " postfire.");
        _lastToken = input.get(0);
        if(((BooleanToken)print.getToken()).booleanValue()) {
            Director dir = getDirector();
            if(dir != null) {
                System.out.println(dir.getCurrentTime() + " " +
                        ((DoubleToken)_lastToken).doubleValue());
            }
        }
        return true;
    }

    /** Wrapup. Compare the last token with 1.0. For correct integration,
     *  the last token should be very close to it.
     *  We take 1e-10 as the value resolution.
     *  @exception IllegalActionException If testValue has an invalid
     *   expression.
     */
    public void wrapup() throws IllegalActionException {
        _debug(getName() + " wrapping up.");
        double v = ((DoubleToken)_lastToken).doubleValue();
        //System.out.println("lasttoken=" + v);
        double p = ((DoubleToken)testValue.getToken()).doubleValue();
        if (Math.abs(v-p) < 1e-10) {
            _success = true;
        } else {
            _success = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _success = false;

    private Token _lastToken;
}
