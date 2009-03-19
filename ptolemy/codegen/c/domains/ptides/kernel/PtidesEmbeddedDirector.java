/* Code generator helper class associated with the PtidesDirector class.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.ptides.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
////PtidesEmbeddedDirector

/**
 Code generator helper associated with the PtidesEmbeddedDirector class. This class
 is also associated with a code generator.
 Also unlike the Ptolemy implementation, the execution does not depend on the WCET
 of actor.
 @author Jia Zou
 @version $Id$
 @since Ptolemy II 7.0
 @Pt.ProposedRating red (jiazou)
 @Pt.AcceptedRating 
 */
public class PtidesEmbeddedDirector extends Director { 

    /** Construct the code generator helper associated with the given
     *  PtidesEmbeddedDirector.
     *  @param ptidesEmbeddedDirector The associated
     *  ptolemy.domains.ptides.kernel.PtidesEmbeddedDirector
     */
    public PtidesEmbeddedDirector(ptolemy.domains.ptides.kernel.PtidesEmbeddedDirector ptidesEmbeddedDirector) {
        super(ptidesEmbeddedDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    /** Generate the code for the firing of actors.
     *  In this base class, it is attempted to fire all the actors once.
     *  In subclasses such as the helpers for SDF and Giotto directors, the
     *  firings of actors observe the associated schedule. In addition,
     *  some special handling is needed, e.g., the iteration limit in SDF
     *  and time advancement in Giotto.
     *  @return The generated code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_codeGenerator.comment("The firing of the director."));

        Iterator actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();

        while (actors.hasNext()) {

            Actor actor = (Actor) actors.next();
            code.append(_eol + "void* " + 
                    CodeGeneratorHelper.generateName((NamedObj) actor) + "(void* arg) {" + _eol);

            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            code.append(helper.generateFireCode());
            code.append(helper.generateTypeConvertFireCode());

            code.append("}" + _eol);
        }
        return code.toString();
    }

    /** Generate the initialize code for the associated PtidesEmbedded director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());

        ptolemy.actor.CompositeActor container = (ptolemy.actor.CompositeActor) getComponent()
        .getContainer();
        CodeGeneratorHelper containerHelper = (CodeGeneratorHelper) _getHelper(container);

        // FIXME: I don't really know what this does, and I don't know what I would use this for...
        // Generate code for creating external initial production.
        /*
        Iterator outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            int rate = DFUtilities.getTokenInitProduction(outputPort);

            if (rate > 0) {
                for (int i = 0; i < outputPort.getWidthInside(); i++) {
                    if (i < outputPort.getWidth()) {
                        String name = outputPort.getName();

                        if (outputPort.isMultiport()) {
                            name = name + '#' + i;
                        }

                        for (int k = 0; k < rate; k++) {
                            code.append(CodeStream.indent(containerHelper
                                    .getReference(name + "," + k)));
                            code.append(" = ");
                            code.append(containerHelper.getReference("@" + name
                                    + "," + k));
                            code.append(";" + _eol);
                        }
                    }
                }

                // The offset of the ports connected to the output port is
                // updated by outside director.
                _updatePortOffset(outputPort, code, rate);
            }
        }
        */

        code.append(_codeStream.getCodeBlock("initPIBlock"));
        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     *   FIXME: fire code for each function, as well as the scheduler, should all go here
     *   Take care of platform independent code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = 
            new StringBuffer(super.generatePreinitializeCode());

        code.append(_generateActorFireCode());
        
        List args = new LinkedList();
        args.add(_generateDirectorHeader());

        args.add(((CompositeActor) 
                _director.getContainer()).deepEntityList().size());

        // FIXME: this fetching of preinitBlock only fetches the platform dependent part.
        code.append("void initPIBlock() {" + _eol);
        code.append(_codeStream.getCodeBlock("initPIBlock")); 
        code.append("}" + _eol);
        
        code.append(_codeStream.getCodeBlock("preinitPIBlock", args));        

        return code.toString();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Check for the given channel of the given port to see if
     *  variables are needed for recording read offset and write
     *  offset. If the buffer size of a channel divides the readTokens
     *  and writeTokens given in the argument, then there is no need
     *  for the variables. Otherwise the integer offsets are replaced
     *  with variables and the code to initialize these variables are
     *  generated.  If padded buffers are desired (based on the padBuffers
     *  parameter of the CodeGenerator), pad the buffers.
     *
     *  @param port The port to be checked.
     *  @param channelNumber The channel number.
     *  @param readTokens The number of tokens read.
     *  @param writeTokens The number of tokens written.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.
     */

    protected String _generateDirectorHeader() {
        return CodeGeneratorHelper.generateName(_director) + "_controlBlock";
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    /** fire methods for each actor.
     * @return fire methods for each actor
     * @throws IllegalActionException 
     * @exception IllegalActionException If thrown when getting the port's helper.
     */
    private String _generateActorFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helper = (CodeGeneratorHelper)_getHelper((NamedObj)actor);
            code.append("void " + CodeGeneratorHelper.generateName((NamedObj) actor) + "() " + "{" + _eol);
            code.append(helper.generateFireCode());
            code.append(helper.generateTypeConvertFireCode());
            code.append("}" + _eol);
        }
        return code.toString();
    }

    /** 
     * Generate the label of the task generated for the specified actor.
     * @param actor The specified actor.
     * @return The task label for the specified actor.
     */
    private String _getActorTaskLabel(Actor actor) {
        return CodeGeneratorHelper.generateName((NamedObj) actor)
        + "_TaskFunction";
    }
}
