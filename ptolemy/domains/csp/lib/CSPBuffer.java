/* A single channel buffer.

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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// CSPBuffer
/**
A single channel buffer. This actor is the canonical example of how
to use a CDO construct. It is parameterized by the Parameter "depth",
which controls how many Tokens can be stored in this buffer.
The default depth of the buffer is 1. The buffer depth is set upon
calling the fire method. The fire() method does not return until a
TerminateProcessException is thrown.
<p>
@author Neil Smyth
@version $Id$
*/

public class CSPBuffer extends CSPActor {

    /** Construct a CSPBuffer in the default workspace with an empty
     *  string as its name. The actor is parameterized by its depth,
     *  which must be an integer. The default depth of the buffer is
     *  one. The actor is created with a single input port and a single
     *  output port, both of width one. The input port is called "input",
     *  and similarly, the output port is called "output". The object is
     *  added to the workspace directory. Increment the version number of
     *  the workspace.
     *  @exception IllegalActionException If the port or parameter
     *   cannot be contained by this actor.
     *  @exception NameDuplicationException If the port name coincides
     *   with a port already in this actor, or if the parameter name
     *   coincides with a parameter already in this actor.
     */
    public CSPBuffer() throws IllegalActionException,
    	    NameDuplicationException {
        super();
        depth = new Parameter(this, "depth", (new IntToken(1)) );
        output = new TypedIOPort(this, "output", false, true);
        input = new TypedIOPort(this, "input", true, false);

	output.setTypeEquals(BaseType.GENERAL);
	input.setTypeEquals(BaseType.GENERAL);
    }

    /** Construct a CSPBuffer in the specified container with the
     *  specified name. The name must be unique within the container
     *  or an exception is thrown. The container argument must not be
     *  null, or a NullPointerException will be thrown. The actor is
     *  parameterized by its depth, which must be an integer. The default
     *  depth of the buffer is one. The actor is created with a single
     *  input port and a single output port, both of width one. The input
     *  port is called "input", and similarly, the output port is called
     *  "output".
     *  <p>
     *  @param container The TypedCompositeActor that contains this
     *   actor.
     *  @param name The actor's name.
     *  @exception IllegalActionException If the entity cannot be
     *   contained by the proposed container.
     *  @exception NameDuplicationException If the name argument
     *   coincides with an entity already in the container.
     */
    public CSPBuffer(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
        this(cont, name, 1);
    }

    /** Construct a CSPBuffer in the specified container with the
     *  specified name.  The name must be unique within the container
     *  or an exception is thrown. The container argument must not be
     *  null, or a NullPointerException will be thrown. The actor is
     *  parameterized by its depth, which must be an integer. The buffer
     *  depth is assigned to the value passed in. The actor is created
     *  with a single input port and a single output port, both of width
     *  one. The input port is called "input", and similarly, the output
     *  port is called "output".
     *  <p>
     *  @param container The TypedCompositeActor that contains this actor.
     *  @param name The actor's name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @param theDepth The depth of this buffer.
     *  @exception NameDuplicationException If the name argument coincides
     *   with an entity already in the container.
     */
    public CSPBuffer(TypedCompositeActor cont, String name, int theDepth)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);
	depth = new Parameter(this, "depth", (new IntToken(theDepth)) );
	output = new TypedIOPort(this, "output", false, true);
	input = new TypedIOPort(this, "input", true, false);

	output.setTypeEquals(BaseType.GENERAL);
	input.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port for this actor.
     */
    public TypedIOPort input;

    /** The output port for this actor.
     */
    public TypedIOPort output;

    /** The Parameter storing the depth of the buffer.
     */
    public Parameter depth;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Executes the code in this actor. This actor uses a CDO
     *  construct so that it is always able to receive or send a
     *  token, depending on the state of the buffer. It is the
     *  canonical example of using a CDO. This process continues
     *  executing until a TerminateProcessException is thrown.
     *  @exception IllegalActionException If an error occurs during
     *   executing the process.
     */
    public void fire() throws IllegalActionException {
        try {
            int iDepth = ((IntToken)depth.getToken()).intValue();
            _buffer = new Token[iDepth];
            int count = 0;
            boolean guard = false;
            boolean continueCDO = true;
            ConditionalBranch[] branches = new ConditionalBranch[2];
            while (continueCDO) {
                // step 1
                guard = (_size < iDepth);
                branches[0] = new ConditionalReceive(guard, input, 0, 0);

                guard = (_size > 0);
                branches[1] = new ConditionalSend(guard, output, 0, 1,
                        _buffer[_readFrom]);

                // step 2
                int successfulBranch = chooseBranch(branches);

                // step 3
                if (successfulBranch == 0) {
                    _size++;
                    _buffer[_writeTo] = branches[0].getToken();
                    _writeTo = ++_writeTo % iDepth;
                } else if (successfulBranch == 1) {
                    _size--;
                    _readFrom = ++_readFrom % iDepth;
                } else if (successfulBranch == -1) {
                    // all guards false so exit CDO
                    continueCDO = false;
                } else {
                    throw new IllegalActionException(getName() + ": " +
                            "invalid branch id returned during " +
                            "execution of CDO.");
                }

                count++;
            }
        } catch (NoTokenException ex) {
            throw new IllegalActionException("CSPBuffer: cannot get token.");
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // The array storing the buffered Tokens.
    private Token[] _buffer;

    // The number of Tokens currently stored in the buffer.
    private int _size = 0;

    // The next location to write a Token into.
    private int _writeTo = 0;

    // The next location to read Token from.
    private int _readFrom = 0;
}
