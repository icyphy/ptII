/* Modal models.

 Copyright (c) 1999-2002 The Regents of the University of California.
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

package ptolemy.actor;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.domains.ct.kernel.CTTransparentDirector;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.HSDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.*;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.io.*;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// MobileFunction
/**
This actor extends the TypedAtomicActor. It applies a function to its inputs
and output the results. But rather than have the function specified
statically, this actor allows dynamic change to the function, which means
the computaion of this actor can be changed during executing. Its second
input accept a function token for the new function's definition. The
function token can be given by actors in the local model or remote actors.
@author Yang Zhao
@version $Id$
@since Ptolemy II 2.0
*/
public class MobileFunction extends TypedAtomicActor{

    /** Construct a MobileFunction in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public MobileFunction (Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        input = new TypedIOPort(this, "input", true, false);
        function = new TypedIOPort(this, "function", true, false);
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
    public MobileFunction (CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        function = new TypedIOPort(this, "function", true, false);
        output = new TypedIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    //public Parameter modelURL;

    public TypedIOPort input, function, output;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Initialize this actor.
     *  @exception IllegalActionException.
     */
     public void initialize() throws IllegalActionException {
        _function = null;
        _argList = new LinkedList();
        System.out.println("--- invoke initialize of model manager, and do nothing. \n");
        super.initialize();
    }

    /** if not function specified, performs as an identity function.
     * otherwise, apply the specified function to its input and output
     * the result.
     * //Fixme: it now only considers one input port for data and the function
     * //only has one argument.
     * //how to resolve typy and type signature?
     * @exception IllegalActionException.
     */
    public void fire() throws IllegalActionException  {
        if (_debugging) {
            _debug("Invoking fire");
        }
        if (function.hasToken(0)) {
            _function = (FunctionToken)function.get(0);
        }
        if (input.hasToken(0)) {
            if(_function == null) {
                output.broadcast(input.get(0));
            } else {
                Token in = input.get(0);
                _argList.add(in);

                /*int numOfArgs =_function.getNumberOfArguments();
                if (_debugging) {
                    _debug("the function require " + numOfArgs + " arguments.");
                }
                if (_argList.size() >= numOfArgs) {
                    int k = 0;
                    while( k < numOfArgs ) {

                    } */
                Token t = _function.apply(_argList);
                output.broadcast(t);
                _argList.remove(in);
            }

        }
    }

     /** return true if the actor either of its input port has token.
     *  @exception IllegalActionException should never be throwed
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

    private FunctionToken _function;
    private LinkedList _argList;
}
