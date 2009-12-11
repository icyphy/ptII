/* An aggregation of typed actors, with ports using ArrayOL informations.

 Copyright (c) 1997-2009 The Regents of the University of California.
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
package ptolemy.domains.pthales.lib;

import java.io.IOException;
import java.io.Writer;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
////PthalesCompositeActor

/**
 A composite actor imposes the use of PThalesIOPort
 as they contains needed values used by PThalesDirector.
 A PthalesCompositeActor can contain actors from diffent model (as SDF),
 but the port must be a PThalesIOPort, because of the ArrayOL parameters. 
 @author Rémi Barrère
 @see ptolemy.actor.TypedCompositeActor
 */

public class PthalesCompositeActor extends TypedCompositeActor  {
    /** Construct a PthalesCompositeActor in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     */
    public PthalesCompositeActor() throws NameDuplicationException,
            IllegalActionException {
        super();

        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is PthalesCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as PthalesCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be PthalesCompositeActor.
        setClassName("ptolemy.domains.pthales.lib.PthalesCompositeActor");

        _initialize();
    }

    /** Construct a PthalesCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public PthalesCompositeActor(Workspace workspace)
            throws NameDuplicationException, IllegalActionException {
        super(workspace);

        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is PthalesCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as PthalesCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be PthalesCompositeActor.
        setClassName("ptolemy.domains.pthales.lib.PthalesCompositeActor");

        _initialize();
    }

    /** Construct a PthalesCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PthalesCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is PthalesCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as PthalesCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be PthalesCompositeActor.
        setClassName("ptolemy.domains.pthales.lib.PthalesCompositeActor");

        _initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    private void _initialize() throws IllegalActionException,
            NameDuplicationException {
        if (getAttribute("repetitions") == null) {
            repetitions = new Parameter(this, "repetitions");
            repetitions.setExpression("{1}");
        }
    }

    /** the number of times this actor is fired
     */
    public Parameter repetitions;

    /** Attribute update
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == getAttribute(REPETITIONS)) {
            _totalRepetitions = parseRepetitions(this, REPETITIONS);
            computeIterations(_totalRepetitions);
        }
    }

    /** Create a new PThalesIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return A new PThalesIOPort.
     *  @exception NameDuplicationException If this actor already has a
     *   port with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();

            PThalesIOPort port = new PThalesIOPort(this, name, false, false);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            workspace().doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object, which
     *  in this class are the attributes plus the ports.  This method is called
     *  by exportMoML().  Each description is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /** iteration informations */
    protected int _iterations = 0;

    protected Integer[] _externalRepetitions = null;

    protected Integer[] _internalRepetitions = new Integer[0];

    protected Integer[] _totalRepetitions = null;



    ///////////////////////////////////////////////////////////////////
    ////              static methods implementation              ////

    public static int getIterations(CompositeActor actor) {
        return computeIterations(parseRepetitions(actor, REPETITIONS));
    }

    public static Integer[] getRepetitions(CompositeActor actor) {
        return parseRepetitions(actor, REPETITIONS);
    }


    /** Return a data structure giving the dimension data contained by a
     *  parameter with the specified name in the specified port or actor.
     *  The dimension data is indexed by dimension name and contains two
     *  integers, a value and a stride, in that order.
     *  @param name The name of the parameter
     *  @return The dimension data, or an empty array if the parameter does not exist.
     *  @throws IllegalActionException If the parameter cannot be evaluated.
     */
    protected static Integer[] parseRepetitions(CompositeActor actor, String name) {
        // FIXME: prepend an underscore to the name of this protected method.
        Integer[] result = new Integer[0];
        Attribute attribute = actor.getAttribute(name);
        if (attribute != null && attribute instanceof Parameter) {
            Token token = null;
            try {
                token = ((Parameter) attribute).getToken();
            } catch (IllegalActionException e) {
                // FIXME: Don't print a stack trace, instead
                // this method should throw an IllegalActionException.
                e.printStackTrace();
            }
            if (token instanceof ArrayToken) {
                int len = ((ArrayToken) token).length();
                result = new Integer[len];
                for (int i = 0; i < len; i++) {
                    result[i] = new Integer(((IntToken) ((ArrayToken) token)
                            .getElement(i)).intValue());
                }
            }
        }

        return result;
    }
    /** Compute external iterations each time 
     *  an attribute used to calculate it has changed 
     */
    protected static int computeIterations(Integer[] totalRepetitions) {
        int iterations = 0;

        // All loops are used to build array
        int iterationCount = 1;
        for (Integer iter : totalRepetitions) {
            iterationCount *= iter;
        }

        // Iteration is only done on external loops
        iterations = iterationCount;
        
        return iterations;
    }
  
    ///////////////////////////////////////////////////////////////////
    ////              static variables              ////

    protected static String REPETITIONS = "repetitions";

}
