/* A parameter that is in string mode by default.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// StringParameter
/**
This subclass of Parameter is almost identical to Parameter except that it
sets itself to string mode in the constructor.
In addition, there is a convenience method, stringValue(), that
is equivalent to the following expression:
<pre>
   ((StringToken)this.getToken()).stringValue()
</pre>
<p>
This subclass is provided mainly
so that string-mode parameters can be specified in MoML.  Consequently,
the only constructor provided is the one needed by MoML.

@author Edward A. Lee
@version $Id$
*/

public class StringParameter extends Parameter {

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public StringParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setStringMode(true);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the string value of this parameter.  This is
     *  equivalent to
     *  <pre>
     *     ((StringToken)this.getToken()).stringValue()
     *  </pre>
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public String stringValue() throws IllegalActionException {
        return ((StringToken)getToken()).stringValue();
    }
}
