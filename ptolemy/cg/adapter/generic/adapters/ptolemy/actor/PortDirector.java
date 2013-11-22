/* A Director that use Ports and PortInfo.

 Copyright (c) 2013 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.adapters.ptolemy.actor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ptides.kernel.PtidesPlatform;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PortDirector

/**
 A Director that use Ports and PortInfo.

 @see GenericCodeGenerator
 @author Christopher Brooks
 @version $Id: Director.java 67792 2013-10-26 19:36:54Z cxh $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (zhouye)
 @Pt.AcceptedRating Yellow (zhouye)
 */
public abstract class PortDirector extends Director {

    /** Construct the code generator adapter associated with the given director.
     *  Note before calling the generate*() methods, you must also call
     *  setCodeGenerator(GenericCodeGenerator).
     *  @param director The associated director.
     */
    public PortDirector(ptolemy.actor.Director director) {
        super(director);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return whether the channels in multiports can be dynamically
     *  referenced using the $ref macro.
     *  @return True when the channels in multiports can be dynamically
     *  referenced using the $ref macro.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    abstract public Boolean allowDynamicMultiportReference()
            throws IllegalActionException; 

    /**
     * Generate sanitized name for the given named object. Remove all
     * underscores to avoid conflicts with systems functions.
     * @param port The port for which the name is generated.
     * @return The sanitized name.
     * @exception IllegalActionException If the variablesAsArrays parameter
     * cannot be read or if the buffer size of the port cannot be read.
     */
    abstract public String generatePortName(TypedIOPort port)
            throws IllegalActionException;

    /** Return whether we need to pad buffers or not.
     *  @return True when we need to pad buffers.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    abstract public Boolean padBuffers() throws IllegalActionException;

    /** The meta information about the ports in the container. */
    public Ports ports;

}