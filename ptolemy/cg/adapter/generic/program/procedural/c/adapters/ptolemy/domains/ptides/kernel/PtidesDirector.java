/* Code generator adapter class associated with the PtidesDirector class.

 Copyright (c) 2009-2013 The Regents of the University of California.
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

package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.kernel.DEDirector;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.domains.ptides.kernel.PtidesEvent;
import ptolemy.domains.ptides.lib.PtidesPort;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////DEDirector

/**
* Code generator adapter associated with the PtidesDirector class. 
* This adapter is highly experimental and extends the DE Director
* adapter.
* This class is also associated with a code generator.
*
*  @author William Lucas based on PtidesDirector.java by Patricia Derler, Edward A. Lee, Slobodan Matic, Mike Zimmer, Jia Zou
*  @version $Id$
*  @since Ptolemy II 9.1
*  @Pt.ProposedRating red (wlc)
*  @Pt.AcceptedRating red (wlc)
*/

public class PtidesDirector extends DEDirector {
    
    /** Construct the code generator adapter associated with the given
     *  PtidesDirector.
     *  @param ptidesDirector The associated
     *  ptolemy.domains.ptides.kernel.PtidesDirector
     */
    public PtidesDirector(ptolemy.domains.ptides.kernel.PtidesDirector ptidesDirector) {
        super(ptidesDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Generate The _fireAt function code.
     *  This method is the direct transposition of the _fireAt function of the director
     *  in C, it overrides its parent method.
     *
     *  @return The _fireAt function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String generateFireAtFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();        
        
        code.append(_eol + "int newIndex = index;");
        code.append(_eol + "if (" + _sanitizedDirectorName + ".currentLogicalTime != -1");
        code.append(_eol + "        && " + _sanitizedDirectorName + "._currentLogicalTime == time");
        code.append(_eol + "        && index <= " + _sanitizedDirectorName + "_getIndex()) {");
        code.append(_eol + "    newIndex = max(" + _sanitizedDirectorName + "_getIndex(), index) + 1;");
        code.append(_eol + "}");

        code.append(_eol + "if (" + _sanitizedDirectorName + ".isInitializing) {");
        code.append(_eol + "    " + _sanitizedDirectorName + "._currentSourceTimestamp = time;");
        code.append(_eol + "}");

        code.append(_eol + "int depth = actor->depth;");
        code.append(_eol
                + "PtidesEvent * newEvent = newPtidesEvent(actor, NULL, time, newIndex,");
        code.append(_eol + "                depth, 0.0, " + _sanitizedDirectorName + "._currentSourceTimestamp);");
        code.append(_eol + "CQueuePut(&(" + _sanitizedDirectorName + ".pureEvents), newEvent);");
        code.append(_eol + _sanitizedDirectorName + "._currentSourceTimestamp = -1;");
        
        code.append(_eol + "Time environmentTime = " + _sanitizedDirectorName + ".container->director->currentModelTime;");
        code.append(_eol + "if (environmentTime <= time) {");
        code.append(_eol + "//    fireContainerAt(time, 1);");
        code.append(_eol + "}");
        
        code.append(_eol + "return;");
        
        return code.toString();
    }
    
    /** Generate The IsSafeToProcess function code. 
     *  @return The IsSafeToProcess function code.
     *  @exception IllegalActionException If thrown while appending the code block.
     */
    public String generateIsSafeToProcessFunctionCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        LinkedList<String> args = new LinkedList<String>();
        args.add(CodeGeneratorAdapter.generateName(_director));
        
        codeStream.appendCodeBlock("isSafeToProcessBlock", args);

        return codeStream.toString();
    }
    
    /** Generate a main loop for an execution under the control of
     *  this Ptides director. 
     *  
     *  @return Code for the main loop of an execution.
     *  @exception IllegalActionException If something goes wrong.
     */
    @Override
    public String generateMainLoop() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        _sanitizedDirectorName = CodeGeneratorAdapter.generateName(_director);
        
        // TODO : add private (static) variables declarations here
        
        code.append("int " + _sanitizedDirectorName + "_fire() {" + _eol);
        code.append(generateFirePrivateFunctionCode());
        code.append(_eol + "}" + _eol);
        
        code.append("void " + _sanitizedDirectorName + "_fireAt(Actor * actor, Time time, int microstep) {" + _eol);
        code.append(generateFireAtFunctionCode());
        code.append(_eol + "}" + _eol);
        
        code.append("boolean _isSafeToProcess(PtidesEvent event) {" + _eol);
        code.append(generateIsSafeToProcessFunctionCode());
        code.append(_eol + "}" + _eol);
        
        code.append("Actor * _nextActorToFire() {" + _eol);
        code.append(generateNextActorToFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("Actor * _nextActorFrom(CalendarQueue * queue) {" + _eol);
        code.append(generateNextActorFromFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("void _setNextFireTime(Time time) {" + _eol);
        code.append(generateSetNextFireTimeFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "void " + _sanitizedDirectorName + "_Preinitialize() {" + _eol);
        code.append(generatePreinitializeMethodBodyCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "boolean " + _sanitizedDirectorName + "_Prefire() {" + _eol);
        code.append(generatePreFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("boolean " + _sanitizedDirectorName + "_Postfire() {" + _eol);
        code.append(generatePostFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("void " + _sanitizedDirectorName + "_Fire() {" + _eol);
        code.append(generateFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "void " + _sanitizedDirectorName + "_Initialize() {" + _eol);
        code.append(generateInitializeFunctionCode());
        code.append(_eol + "}" + _eol);
        
        code.append(_eol + "void " + _sanitizedDirectorName + "_Wrapup() {" + _eol);
        code.append(generateWrapupCode());
        code.append(_eol + "}" + _eol);
        
        return code.toString();
    }
    
    /** Generate The _NextActorFrom function code.
     *  This method is the direct transposition of the _NextActorFrom function of the director
     *  in C.
     *
     *  @return The _NextActorFrom function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateNextActorFromFunctionCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();

        codeStream.appendCodeBlock("getNextActorFromBlock");

        return codeStream.toString();
    }
    
    /** Generate The _NextActorToFire function code.
     *  This method is the direct transposition of the _NextActorToFire function of the director
     *  in C.
     *
     *  @return The _NextActorToFire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateNextActorToFireFunctionCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();

        codeStream.appendCodeBlock("getNextActorToFireBlock");

        return codeStream.toString();
    }
    
    /** Generate The postfire function code. It overrides its parents method
     *  because in Ptides we do not need a refiring mechanism.
     *  @return The postfire function code.
     *  @exception IllegalActionException If thrown while generating postfire code.
     */
    @Override
    public String generatePostFireFunctionCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        LinkedList<String> args = new LinkedList<String>();
        args.add(CodeGeneratorAdapter.generateName(_director));
        
        codeStream.appendCodeBlock("postfireBlock", args);

        return codeStream.toString();
    }
    
    /** Generate The prefire function code. It overrides its parents method.
     *  @return The prefire function code.
     *  @exception IllegalActionException If thrown while generating prefire code.
     */
    @Override
    public String generatePreFireFunctionCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        LinkedList<String> args = new LinkedList<String>();
        args.add(CodeGeneratorAdapter.generateName(_director));
        
        codeStream.appendCodeBlock("prefireBlock", args);

        return codeStream.toString();
    }
    
    /** Generate The setNextFireTime function code.
     *  @return The setNextFireTime function code.
     *  @exception IllegalActionException If thrown while generating code.
     */
    public String generateSetNextFireTimeFunctionCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.appendCodeBlock("setNextFireTimeBlock");

        return codeStream.toString();
    }
}
