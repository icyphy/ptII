/* An actor that apply dynamically defined functions to its input.

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.hoc;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.CompositeEntity;
import ptolemy.data.FunctionToken;
import ptolemy.data.Token;
import java.util.LinkedList;


//////////////////////////////////////////////////////////////////////////
//// MobileFunction
/**
This actor extends the TypedAtomicActor. It applies a function to its inputs
and outputs the results. But rather than has the function specified
statically, this actor allows dynamic change to the function, which means
the computation of this actor can be changed during executing. Its second
input accept a function token for the new function's definition. The
function token can be given by actors in the local model or remote actors.

Currently, it only accept functions that has one argument. The return type
of the function needs to be less than the output type of this actor.

@author Yang Zhao
@version $Id$
@since Ptolemy II 3.0
*/
public class MobileFunction extends TypedAtomicActor{

    /** Construct a MobileFunction in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public MobileFunction(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        input = new TypedIOPort(this, "input", true, false);
        function = new TypedIOPort(this, "function", true, false);
        //function.setTypeAtMost(new FunctionType
        //        (new Type[]{BaseType.UNKNOWN}, BaseType.GENERAL));
        output = new TypedIOPort(this, "output", false, true);
    }

    /** Construct a MobileFunction with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MobileFunction(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        function = new TypedIOPort(this, "function", true, false);
        //function.setTypeAtMost(new FunctionType
        //        (new Type[]{BaseType.UNKNOWN}, BaseType.GENERAL));
        output = new TypedIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    /** The input port for incoming data.  The type of this port is
     *  undeclared, meaning that it will resolve to any data type.
     */
    public TypedIOPort input;

    /** The input port for function definition. The type of this port is
     *  undeclared, but to have this actor work, the designer has to provide
     *  a matched function token for it.
     *  Note: The reason that we don't declare the type for it is because
     *  currently there is not cast supported in the FunctionType class.
     *  we'll fix this later.
     */
   public TypedIOPort function;

    /** The output port.
     *  Note: Due to the same reason above, the type of the output can't be
     *  resolved currently. User has to spedify the type.
     */
    public TypedIOPort output;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the function is not specified, then perform identity function;
     *  otherwise, apply the specified function to its input and output
     *  the result.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
    public void fire() throws IllegalActionException  {
        if (_debugging) {
            _debug("Invoking fire");
        }
        if (function.hasToken(0)) {
            _function = (FunctionToken)function.get(0);
        }
        if (input.hasToken(0)) {
            if (_function == null) {
                output.broadcast(input.get(0));
            } else {
        // FIXME: it now only considers one input port for data and the
        // function only has one argument.  how to resolve type and type
        // signature?
                Token in = input.get(0);
                _argList.add(in);
                Token t = _function.apply(_argList);
                output.broadcast(t);
                _argList.remove(in);
            }

        }
    }

    /** Initialize this actor.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        _function = null;
        _argList = new LinkedList();
        super.initialize();
    }

    /** Return true if the actor either of its input port has token.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Invoking prefire");
        }
        if (input.hasToken(0) || function.hasToken(0)) {
            return true;
        }
        return false;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
     /** The most recently updated function to this actor.
     *
     */
    private FunctionToken _function;

    /** The arguments list for the applying function.
     *
     */
    private LinkedList _argList;
}
