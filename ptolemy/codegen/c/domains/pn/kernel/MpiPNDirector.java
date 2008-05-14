/* Code generator helper class associated with the MpiPNDirector class.

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
package ptolemy.codegen.c.domains.pn.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.codegen.kernel.Director;
import ptolemy.codegen.kernel.PortCodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper.Channel;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.properties.util.MultiHashMap;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;


////MpiPNDirector

/**
 Code generator helper associated with the MpiPNDirector class.
 This director initializes all the actors, then starts a thread
 for each actor that invokes the fire code for the actor in an
 infinite loop.

 FIXME: How to make it possible for executions to be finite?

 @author Man-Kit Leung, Isaac Liu, Jia Zou
 @version $Id$
 @since Ptolemy II 7.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class MpiPNDirector extends Director {

    public static boolean _DEBUG = false;

    /** Construct the code generator helper associated with the given
     *  PNDirector.
     *  @param pnDirector The associated
     *  ptolemy.domains.pn.kernel.PNDirector
     */
    public MpiPNDirector(ptolemy.domains.pn.kernel.PNDirector pnDirector) {
        super(pnDirector);
        this.analyzeModel();

    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    // Traverse through the model to see which actors belong to which rank.
    public void analyzeModel() {// throws IllegalActionException {

        _rankActors.clear();
        _numProcessors = 0;

        try {
            CompositeActor compositeActor = 
                (CompositeActor) _director.getContainer();       
            List actorList = compositeActor.deepEntityList();
            for (Actor actor : (List<Actor>) actorList) {
                int rank = getRankNumber(actor);
                _rankActors.put(rank, actor);
                if (rank > _numProcessors)
                    _numProcessors = rank;
            }
            _numProcessors++;
        } catch (IllegalActionException e){
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /** Generate the body code that lies between variable declaration
     *  and wrapup.
     *  @return The generated body code.
     *  @exception IllegalActionException If the
     *  {@link #generateFireCode()} method throws the exceptions.
     */
    public String generateFireCode() throws IllegalActionException {
        
        this.analyzeModel();
        
        StringBuffer code = new StringBuffer();
        CompositeActor compositeActor = 
            (CompositeActor) _director.getContainer();       

        //code.append(_codeGenerator.comment("Create a thread for each actor."));
        //Added by Isaac liu for testing
        code.append(_codeStream.getCodeBlock("mpiInit"));
        if (_doMeasureTime()) {
            code.append(_codeStream.getCodeBlock("initTimer"));
        }

        List actorList = compositeActor.deepEntityList();
        if (_doMeasureTime()) {
            for (Actor actor : (List<Actor>) actorList) {
                int rankNumber = getRankNumber(actor);
                code.append("if (rank == " + rankNumber + ") {" + _eol);
                code.append("initialize_timer(&" + 
                        _getActorTimer(actor) + ");" + _eol + "}" + _eol);
            }
        }
        
        code.append(_codeGenerator.comment("I'm in mpi director."));
        //Removed since we don't use pthreads -iliu
        //code.append("pthread_attr_init(&pthread_custom_attr);" + _eol + _eol);


        code.append("while (true) {" + _eol);
        for (Actor actor : (List<Actor>) actorList) {
            int rankNumber = getRankNumber(actor);
            code.append("if (rank == " + rankNumber + ") {" + _eol);

            code.append("if (" + _getActorRounds(actor) + " < " + 
                    _getActorRoundsLimit(actor) + ") {" + _eol);
          if (_DEBUG) {
            code.append("printf(\"" + _getActorRounds(actor) + "[%d], rank[" + rankNumber + "].\\n\", "
                    + _getActorRounds(actor) + ");" + _eol);
        }

            code.append(_getActorMpiLabel(actor) + "();" + _eol);


            code.append("} else {" + _eol + _getActorTerminate(actor) + 
                    " = true;" + _eol + "}" + _eol);

            code.append("}" + _eol);
        }
        
        // Check if we are ready to terminate
        int rank = 0;
        while (rank < _numProcessors) {

            code.append("if (rank == " + rank + ") {" + _eol);
            code.append("if (true");

            List actorOfThisRankList = (List)_rankActors.get(rank);
            for (Actor actor: (List<Actor>) actorOfThisRankList) {
                code.append(" && " + _getActorTerminate(actor));
            }
            code.append(")" + _eol + "break;" + _eol);
            code.append("}" + _eol);
            
            rank++;
        }
        
        code.append("}" + _eol); //end of while(true)
        
        // print out the timer values
        if (_doMeasureTime()) {
            code.append("stop_timer(&total_timer);" + _eol); 
            code.append("printf(\"rank = %d, total time takes: %g\\n\", rank, timer_duration(total_timer));" +_eol);
            rank = 0;
            while (rank < _numProcessors) {
                code.append("if (rank == " + rank + ") {" + _eol);
                List actorOfThisRankList = (List)_rankActors.get(rank);
                for (Actor actor: (List<Actor>) actorOfThisRankList) {
                    code.append(" printf(\"rank = %d, " + _getActorTimer(actor) 
                            + " takes: %g\\n\", rank, timer_duration(" + _getActorTimer(actor)
                            + "));" +_eol);
                }
                code.append("}" +_eol);
                rank++;
            }
        }
        
        code.append("fflush(stdout);" + _eol);
        
        return code.toString();
    }

    public static int getRankNumber(Actor actor) throws IllegalActionException {
        return ((IntToken) ((Parameter) ((Entity) actor).getAttribute("_partition")).getToken()).intValue();
    }

    /** Do nothing in generating fire function code. The fire code is
     *  wrapped in a for/while loop inside the thread function.
     *  The thread function is generated in 
     *  {@link #generatePreinitializeCode()} outside the main function. 
     *  @return An empty string.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        _generateThreadFunctionCode(code);
        return code.toString();
    }

    /** Get the files needed by the code generated from this helper class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this helper class.
     *  @exception IllegalActionException If something goes wrong.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("<mpi.h>");
        //files.add("<timer.h>");
        return files;
    }

    /** Return the libraries specified in the "libraries" blocks in the
     *  templates of the actors included in this CompositeActor.
     *  @return A Set of libraries.
     *  @exception IllegalActionException If thrown when gathering libraries.
     */
    public Set getLibraries() throws IllegalActionException {
        Set libraries = new LinkedHashSet();
        libraries.add("pthread");
        return libraries;
    }

    /** Generate the initialize code for the associated PN director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_codeGenerator
                .comment("Initialization code of the MpiPNDirector."));

        // Don't generate the code to initialize all the actors.
        code.append(super.generateInitializeCode());

        List args = new LinkedList();
        args.add(generateDirectorHeader());

        // Initialize the director head variable.
        code.append(_codeStream.getCodeBlock("initBlock", args));

        // Initialize each buffer variables.
        for (String bufferVariable : _buffers) {
            args.set(0, bufferVariable);
            code.append(_codeStream.getCodeBlock("initBuffer", args));
        }
        return code.toString();
    }

    /**
     * 
     */
    public String generateMainLoop()
    throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
        .booleanValue();

        if (inline) {
            code.append(generateFireCode());
        } else {
            code.append(CodeGeneratorHelper.generateName(_director
                    .getContainer()) + "();" + _eol);
        }

        return code.toString();
    }

    /** Generate the preinitialize code for the associated PN director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating preinitialize code for the actor.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer bufferCode = new StringBuffer();

        _buffers.clear();

        List actorList = 
            ((CompositeEntity) _director.getContainer()).deepEntityList();

        StringBuffer code = new StringBuffer(super.generatePreinitializeCode());

        Iterator actors = actorList.iterator();
        while (actors.hasNext()) {
            Entity actor = (Entity) actors.next();
            Iterator ports = actor.portList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();

                if (port.getWidth() > 0) {
                    bufferCode.append(_createDynamicOffsetVariables(port));
                }                

                CodeGeneratorHelper helper = 
                    (CodeGeneratorHelper) _getHelper(actor);

                // Set offset objects.
                for (int i = 0; i < port.getWidth(); i++) {
                    for (Channel sinkChannel : getReferencedChannels(port, i)) {
                        IOPort sink = sinkChannel.port;
                        CodeGeneratorHelper sinkHelper = 
                            (CodeGeneratorHelper) _getHelper(sink.getContainer());

                        int channelNumber = sinkChannel.channelNumber;

                        if (isLocalBuffer(port, i)) {
                            sinkHelper.setBufferSize(sink, channelNumber, getBufferSize(sink, channelNumber));
                            sinkHelper.setReadOffset(sink, channelNumber, generatePortHeader(sink, channelNumber) + ".readOffset");
                            sinkHelper.setWriteOffset(sink, channelNumber, generatePortHeader(sink, channelNumber) + ".writeOffset");

                        } else if (isMpiSendBuffer(port, i)) {
                            //"freeSlot[head.current]");
                            sinkHelper.setBufferSize(sink, channelNumber, getBufferSize(sink, channelNumber));
//                          helper.setReadOffset(sink, channelNumber, generateFreeSlots(sink, channelNumber) + "[" +
//                          generatePortHeader(sink, channelNumber) + ".current]");                        
                            helper.setWriteOffset(sink, channelNumber, generateFreeSlots(sink, channelNumber) + "[" +
                                    generatePortHeader(sink, channelNumber) + ".current]");                        

                        }
                    }
                }
            }
        }

        _codeStream.clear();
        _codeStream.appendCodeBlock("preinitBlock");
        code.append(_codeStream.toString());

        if (_codeGenerator.inline.getToken() == BooleanToken.TRUE) {
            _generateThreadFunctionCode(code);
        }

        return bufferCode.toString() + code.toString();
    }

    public String generatePostfireCode() throws IllegalActionException {
        return "";
    }

    public void generateTransferOutputsCode(IOPort inputPort, StringBuffer code)
    throws IllegalActionException {
        code.append(CodeStream.indent(_codeGenerator.comment("MpiPNDirector: "
                + "Transfer tokens to the outside.")));
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
    throws IllegalActionException {
        code.append(CodeStream.indent(_codeGenerator.comment("MpiPNDirector: "
                + "Transfer tokens to the inside.")));

        int rate = DFUtilities.getTokenConsumptionRate(inputPort);

        CompositeActor container = (CompositeActor) getComponent()
        .getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor compositeActorHelper 
        = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);

        for (int i = 0; i < inputPort.getWidth(); i++) {
            if (i < inputPort.getWidthInside()) {
                String name = inputPort.getName();

                if (inputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                for (int k = 0; k < rate; k++) {
                    code.append(CodeStream.indent(compositeActorHelper
                            .getReference("@" + name + "," + k)));
                    code.append(" =" + _eol);
                    code.append(compositeActorHelper.getReference(name + ","
                            + k));
                    code.append(";" + _eol);
                }
            }
        }
        // Generate the type conversion code before fire code.
        code.append(compositeActorHelper.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, rate);
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
    throws IllegalActionException {
        return super.generateVariableInitialization();
    }

    /** Generate the wrapup code for the associated PN director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating preinitialize code for the actor.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // Note: We don't need to call the super class method nor
        // append the wrapup code for each of the containing actor.
        // Instead, the actor wrapup code resides in the actor 
        // thread function code.

        code.append(_codeStream.getCodeBlock("wrapupBlock"));

        return code.toString();
    }

    public int getBufferSize(IOPort port, int channelNumber)
    throws IllegalActionException {

        if (isLocalBuffer(port, channelNumber)) {
            IntToken sizeToken = (IntToken) ((Parameter) 
                    ((ptolemy.domains.pn.kernel.PNDirector) _director)
                    .getAttribute("_localBufferSize")).getToken();

            return sizeToken.intValue();
        } 

        // mpi buffer size.        
        IntToken sizeToken = (IntToken)
        ((ptolemy.domains.pn.kernel.PNDirector) _director)
        .initialQueueCapacity.getToken();

        // FIXME: Force buffer size to be at least 2.
        // We need to handle size 1 as special case.
        return Math.max(sizeToken.intValue(), 2);
    }

    public static boolean isLocalBuffer(IOPort port, int channel) {
        if (port.getWidth() <= 0) { 
            return true;
        }

        return !isMpiSendBuffer(port, channel) && !isMpiReceiveBuffer(port, channel);
    }
    
    public static boolean isMpiReceiveBuffer(IOPort inputPort, int channel) {
        return getMpiReceiveBufferId(inputPort, channel) >= 0;
    }

    public static int getMpiReceiveBufferId(IOPort inputPort, int channel) {
        StringAttribute bufferAttribute = 
            (StringAttribute) inputPort.getAttribute("_isMpiBuffer");
        
        if (bufferAttribute != null) {
            String value = bufferAttribute.getExpression();
            if (value.startsWith("receiver")) {
                StringTokenizer tokenizer = new StringTokenizer(value, "[]", false);
                
                while (tokenizer.hasMoreTokens()) {
                    // "ch". 
                    tokenizer.nextToken();

                    // "[#]"
                    String channelString = tokenizer.nextToken();

                    // "id"
                    tokenizer.nextToken();
                    
                    // "[#]"
                    String idString = tokenizer.nextToken();
                    
                    if (channelString.equals("" + channel)) {
                        return Integer.parseInt(idString);
                    }
                }
            }
        }
        return -1;
    }


    public static boolean isMpiSendBuffer(IOPort port, int channel) {
        StringAttribute bufferAttribute = 
            (StringAttribute) port.getAttribute("_isMpiBuffer");

        if (bufferAttribute != null) {
            String value = bufferAttribute.getExpression();
            if (value.startsWith("sender")) {
                StringTokenizer tokenizer = new StringTokenizer(value, "[]", false);
                
                while (tokenizer.hasMoreTokens()) {
                    // "ch". 
                    tokenizer.nextToken();

                    // "[#]"
                    String channelString = tokenizer.nextToken();

                    if (channelString.equals("" + channel)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public String getReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite, CodeGeneratorHelper helper)
    throws IllegalActionException {

//      String result = port.getName() + "_";


        String result = "##REF(" + port.getName() + ", " + isWrite + ")";

        // Generate channel.
        int channelNumber = 0;
        if (channelAndOffset[0] != null && channelAndOffset[0].length() > 0) {
            channelNumber = Integer.parseInt(channelAndOffset[0]); 
        }
        if (isLocalBuffer(port, channelNumber)) {
            return super.getReference(port, channelAndOffset, forComposite, isWrite, helper);
        } else if (isMpiReceiveBuffer(port, channelNumber)){
            return getBufferLabel(port, channelNumber);
        } else if (isMpiSendBuffer(port, channelNumber)){
            return super.getReference(port, channelAndOffset, forComposite, isWrite, helper);
        }

        // get reference for mpi ports.
        return result; 
        //return super.getReference(port, channelAndOffset, forComposite, isWrite, helper);
    }


    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();
        
        // FIXME: This code ensures the termination of the mpi PN code,
        // however this scheme may not be of the PN semantics.
        StringBuffer code = new StringBuffer();
        
        CompositeActor compositeActor = (CompositeActor) _director.getContainer();
        List actorList = compositeActor.deepEntityList();

        // FIXME: this is NOT right! It's a hack, assuming all actors will be fired EXACT
        // the same amount of times.
        Parameter Iterations = (Parameter)_director.getContainer().getAttribute("iterations");
        String numIterations = new String();
        if (Iterations == null)
            numIterations = "1000";
        else 
            numIterations = Iterations.getExpression();
        
        for (Actor actor : (List<Actor>) actorList) {
            code.append("static int " + _getActorRoundsLimit(actor) + " = " + numIterations 
                    + ";" + _eol);
            code.append("static int " + _getActorRounds(actor) + " = 0;" + _eol);
            code.append("boolean " + _getActorTerminate(actor) + " = 0;" + _eol);
        }
        sharedCode.add(code.toString());
        
        if (_doMeasureTime()) {
            _codeStream.clear();
            _codeStream.appendCodeBlock("timerSharedBlock");
            sharedCode.add(_codeStream.toString());
            
            StringBuffer codeBuffer = new StringBuffer();
            List actorList2 = compositeActor.deepEntityList();
            for (Actor actor : (List<Actor>) actorList2) {
                codeBuffer.append("struct Timer " + _getActorTimer(actor) + ";" + _eol);
            }
            sharedCode.add(codeBuffer.toString());
        }
                
        return sharedCode;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    private class Analyzer {
        private Set mpiReceivePorts = new HashSet();
        private Set mpiSendPorts = new HashSet();

        public boolean _isMpiReceive(TypedIOPort port, int channel) {
            return mpiReceivePorts.contains(new Channel(port, channel));
        }

        public boolean _isMpiSend(TypedIOPort port, int channel) {
            return mpiSendPorts.contains(new Channel(port, channel));
        }

        public boolean _isLocalBuffer(TypedIOPort port, int channel) {
            return !mpiReceivePorts.contains(new Channel(port, channel))
            && !mpiSendPorts.contains(new Channel(port, channel));
        }

        private int _getPartitionNumber(Actor actor)
        throws IllegalActionException {
            Parameter result = (Parameter) ((NamedObj) actor).getAttribute("_partition");
            assert result != null;
            
            return ((IntToken) result.getToken()).intValue();
        }

        protected void _analyzeMpiPorts(CompositeActor container) {
            try {
                for (Actor actor : (List<Actor>) container.deepEntityList()) {

                    int actorPartition = _getPartitionNumber(actor);

                    for (TypedIOPort input : (List<TypedIOPort>) actor.inputPortList()) {

                        for (int i = 0; i < input.getWidth(); i++) {
                            
                            int sourceIndex = 0;
                            Channel source = CodeGeneratorHelper.getSourceChannel(input, i);

                            Actor sourceActor = (Actor) source.port.getContainer();

                            if (actorPartition != _getPartitionNumber(sourceActor)) {
                                // Add (sourcePort, )
                                mpiSendPorts.add(source);
                            }
                        }

                        //Iterator connOut = (Iterator) thisInput.deepConnectedOutPortList().iterator();
                    }
                    
                    List outputList =  actor.outputPortList();
                    Iterator outputIt = (Iterator) outputList.listIterator();

                    while(outputIt.hasNext()) {
                        TypedIOPort thisOutput = (TypedIOPort) outputIt.next();

                        // Clear the _isMpiBuffer parameter if it already exists
                        Parameter clearPortParam = _getMpiParameter(thisOutput);
                        clearPortParam.setExpression("");

                        // Iterator connIn = (Iterator) thisOutput.deepConnectedInPortList().iterator();
                        Iterator connIn = (Iterator) thisOutput.sinkPortList().iterator();
                        int sinkIndex = 0;
                        while (connIn.hasNext()) {
                            TypedIOPort tempInput = (TypedIOPort) connIn.next();
                            Actor tempActor = (Actor) tempInput.getContainer();
                            Parameter attrTemp = new Parameter();
                            attrTemp = (Parameter)((NamedObj)tempActor).getAttribute("_partition");
                            assert attrTemp != null;
                            if (!attrTemp.getExpression().equals(attrTemp.getExpression())) {                      
                                Parameter portParam = _getMpiParameter(thisOutput);
                                String tempString = portParam.getExpression();
                                if (tempString.length() == 0) {
                                    tempString = "sender";
                                }
                                tempString = tempString.concat("_" + Integer.toString(sinkIndex));
                                portParam.setExpression(tempString);
                                //portParam.setExpression("sender");
                            }
                            sinkIndex++;
                        }
                    }

                }


            } catch (IllegalActionException e) {
                System.err.println("Error: " + e.getMessage());
            }

        }

        private Parameter _getMpiParameter(TypedIOPort thisOutput) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    /** Create offset variables for the channels of the given port.
     *  The offset variables are generated unconditionally.
     *
     *  @param port The port whose offset variables are generated.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.
     */
    protected String _createDynamicOffsetVariables(IOPort port)
    throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_eol + _codeGenerator.comment(
        "PN Director's offset variable declarations."));

        int width;
        if (port.isInput()) {
            width = port.getWidth();
        } else {
            width = 0;//port.getWidthInside();
        }

        if (width != 0) {

            // Declare the buffer header struct.
            List args = new LinkedList();
            args.add("");  // buffer header name
            args.add("");  // director header name
            args.add("");  // capacity
            args.add("");  // index

            // FIXME: Do some filtering, only generate needed buffer.
            for (int i = 0; i < width; i++) {
                if (isLocalBuffer(port, i)) {
                    args.set(0, generatePortHeader(port, i));
                    args.set(1, generateDirectorHeader());
                    args.set(2, getBufferSize(port, i));
                    args.set(3, _buffers.size());

                    code.append(_codeStream.getCodeBlock("declareLocalBufferHeader", args));

                    // Record all the buffer instantiated.
                    _buffers.add(generatePortHeader(port, i));
                }
            }
        }
        return code.toString();
    }

    public static String generatePortHeader(IOPort port, int i) {
        return CodeGeneratorHelper.generateName(port)
        + "_" + i + "_mpiHeader";
    }

    /** Generate the notTerminate flag variable for the associated PN director.
     * Generating notTerminate instead of terminate saves the negation in checking
     * the flag (e.g. "while (!terminate) ..."). 
     * @return The varaible label of the notTerminate flag.
     */
    protected String _generateNotTerminateFlag() {
        return "true";//"director_controlBlock.controlBlock";
    }

    public String generateDirectorHeader() {
        return CodeGeneratorHelper.generateName(_director) + "_controlBlock";
    }

    /** 
     * @param functionCode The given code buffer.
     * @throws IllegalActionException
     */
    private void _generateThreadFunctionCode(StringBuffer code) throws IllegalActionException {
        CompositeActor compositeActor = (CompositeActor) _director.getContainer();

        List actorList = compositeActor.deepEntityList();

        boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
        .booleanValue();

        // Generate the function for each actor thread.
        for (Actor actor : (List<Actor>) actorList) {

            StringBuffer bufferCheckCode = new StringBuffer();
            StringBuffer prefireCondition = new StringBuffer();;

            CodeGeneratorHelper helper = 
                (CodeGeneratorHelper) _getHelper((NamedObj) actor);

//            if (!inline) {
//                code.append(helper.generateFireFunctionCode());
//            } 

            code.append("//I'm inside generate thread function" + _eol);
            code.append(_eol + "void* " + 
                    _getActorMpiLabel(actor) + "() {" + _eol);

            code.append("int " + _generateTestCounter() + " = 0;" + _eol);

            // DECLARE
            LinkedList args = new LinkedList();
            args.add("");
            args.add("");
            args.add("");
            args.add("");

            
            int variableInitializationPos;
            _codeStream.clear();
            for (TypedIOPort outport : (List<TypedIOPort>) actor.outputPortList()) {
                int maxWidth = Math.max(outport.getWidth(), outport.getWidthInside());

                for (int i = 0; i < maxWidth; i++) {

                    for (Channel channel : getReferencedChannels(outport, i)) {
                        IOPort port = channel.port;
                        int channelNumber = channel.channelNumber;

                        int bufferSize = getBufferSize(port, channelNumber);
                        if (isMpiReceiveBuffer(port, channelNumber)) {
                            args.set(0, generatePortHeader(port, channelNumber));
                            args.set(1, generateDirectorHeader());
                            args.set(2, bufferSize);
                            args.set(3, _buffers.size());

                            _codeStream.appendCodeBlock("declareBufferHeader", args);

                            // free slots declarations.
                            code.append("static int " + generateFreeSlots(port, channelNumber) + "[" + bufferSize + "] = {");
                            for (int j = 0; j < bufferSize; j++) {
                                code.append((j == 0 ? "" : ", ") + j);
                            }
                            code.append("};" + _eol);

                            // MPI_Request array declarations.
                            code.append("static MPI_Request " + generateMpiRequest(port, channelNumber) + "[" + bufferSize + "];" + _eol);

                            // Generate Testsome() check code.
                            //bufferCheckCode.append("if (" + generatePortHeader(port, channelNumber) + ".available - " + generatePortHeader(port, channelNumber) + ".current < " + DFUtilities.getRate(port) + ") {" + _eol);
                            int rate = DFUtilities.getRate(port);
                            bufferCheckCode.append("if (" +
                            		getIsMpiBufferFull(port, channelNumber, rate) + ") {" + _eol);
                            bufferCheckCode.append("MPI_Testsome(" + bufferSize + ", " + generateMpiRequest(port, channelNumber) + ", &" + 
                                    generatePortHeader(port, channelNumber) + ".available, " + generateFreeSlots(port, channelNumber) + ", MPI_STATUSES_IGNORE);" + _eol);
                            bufferCheckCode.append(generatePortHeader(port, channelNumber) + ".current = 0;" + _eol + "}" + _eol);
                            
                            
                            prefireCondition.append(" && !" + getIsMpiBufferFull(port, channelNumber, rate));

                        }
                    }
                }
            }
            code.append(_codeStream.toString());

            variableInitializationPos = code.length();

            // mainLoop

            // Check if the actor is an opague CompositeActor. 
            // The actor is guaranteed to be opague from calling deepEntityList(),
            // so all we need to check whether or not it is a CompositeActor.
            if (actor instanceof CompositeActor) {
                Director directorHelper = (Director) _getHelper(actor.getDirector()); 

                // If so, it should contain a different Director.
                assert (directorHelper != this);

                code.append(directorHelper.generateMainLoop());            

            } else {
                boolean openBracket = false;

                String resetCondition = "";

                for (TypedIOPort inputPort : (List<TypedIOPort>) actor.inputPortList()) {

                    if (inputPort.getWidth() > 0) {
                        for (int channel = 0; channel < inputPort.getWidth(); channel++) {
                            boolean isReceive = isMpiReceiveBuffer(inputPort, channel);    
                            
                            int rate = DFUtilities.getRate(inputPort);

                            // FIXME: handle channel. 
                            if (isReceive) {
                                code.append("static int " + _getHasInputFlag(inputPort, channel) + " = false;" + _eol);
                                code.append("static int " + _getReceiveFlag(inputPort, channel) + " = false;" + _eol);
                                code.append("static MPI_Request " + _generateRequest(inputPort, channel) +
                                		"[" + rate + "];" + _eol);
                                code.append("static " + CodeGeneratorHelper.targetType(inputPort.getType())
                                        + " " + getBufferLabel(inputPort, channel) + ";" + _eol);

//                                
//                                code.append("// Initialize send tag value." + _eol);
//                                code.append("static int " + MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + " = " +
//                                        MpiPNDirector.getMpiReceiveBufferId(sinkPort, sinkChannelNumber) + ";" + _eol);

                                code.append("// Initialize receive tag value." + _eol);
                                code.append("static int " + getReceiveTag(inputPort, channel) + " = " +
                                        getMpiReceiveBufferId(inputPort, channel) + ";" + _eol);
//                                
                                code.append("if (!" + _getHasInputFlag(inputPort, channel) + ") { " + _eol +
                                        "if (!" + _getReceiveFlag(inputPort, channel) + ") {" + _eol);

                                for (int offset = 0; offset < rate; offset++) {
                                    Channel sourceChannel = CodeGeneratorHelper.getSourceChannel(inputPort, channel);
                                    int sourceRank = getRankNumber((Actor) sourceChannel.port.getContainer());

                                    if (_DEBUG) {
                                        int sinkRank = getRankNumber(actor);
                                        code.append("printf(\"" + actor.getName() + "[" + sinkRank + "] receiving msg <" + 
                                                sourceRank + ", %d> for " + getBufferLabel(inputPort, channel) + 
                                                		"\\n\", " + getReceiveTag(inputPort, channel) + ");" + _eol);
                                    }
                                    
                                    code.append("MPI_Irecv(&" + 
                                            getBufferLabel(inputPort, channel) + ", 1, ");

                                    if (inputPort.getType() == BaseType.DOUBLE) {
                                        code.append("MPI_DOUBLE, ");
                                    } else if (inputPort.getType() == BaseType.INT) {
                                        code.append("MPI_INT, ");
                                    } else {
                                        assert false;
                                    }

                                    code.append(sourceRank + ", " + getReceiveTag(inputPort, channel) + 
                                            ", comm, &" + _generateRequest(inputPort, channel) + "[" + offset + "]);" + _eol);

                                    code.append(getReceiveTag(inputPort, channel) + " += " + 
                                            getNumberOfMpiConnections(true) + ";" + _eol);
                                    code.append(getReceiveTag(inputPort, channel) + " &= 32767; // 2^15 - 1 which is the max tag value." + _eol);
                                }

                                code.append(_getReceiveFlag(inputPort, channel) + " = true;" + _eol + "}" + _eol);

                                // Generate test statements.
                                code.append("for (" + _generateTestCounter() + 
                                        " = 0; " + _generateTestCounter() + " < " + rate + 
                                        "; " + _generateTestCounter() + "++) {" + _eol);

                                code.append("MPI_Test(&" +
                                        _generateRequest(inputPort, channel) + "[" + _generateTestCounter() + "], &" + _getHasInputFlag(inputPort, channel) + ", MPI_STATUS_IGNORE);" + _eol);
                                code.append("if (!" + _getHasInputFlag(inputPort, channel) + ") {" + _eol + "break;" + _eol + "}" + _eol + "}" + _eol); 


                                code.append("}" + _eol);

                                prefireCondition.append(" && " + _getHasInputFlag(inputPort, channel));
                                resetCondition += _getHasInputFlag(inputPort, channel) + " = false;" + _eol;
                                resetCondition += _getReceiveFlag(inputPort, channel) + " = false;" + _eol;
                            } else {
                                prefireCondition.append(" && " + _getHasLocalInput(inputPort, channel, rate));
                            }
                        }
                    }
                }

                // Generate checks for local buffer to see if there is room
                // is to insert output tokens.
                for (TypedIOPort outputPort : (List<TypedIOPort>) actor.outputPortList()) {
                    if (outputPort.getWidth() > 0) {
                        for (int i = 0; i < outputPort.getWidth(); i++) {
                            if (isLocalBuffer(outputPort, i)) {
                                int rate = DFUtilities.getRate(outputPort);
                                
                                for (Channel sink : CodeGeneratorHelper.getSinkChannels(outputPort, i)) {
                                    prefireCondition.append(" && !" + 
                                            _getIsLocalBufferFull(sink.port, sink.channelNumber, rate));
                                }
                            }
                        }
                    }
                }

                code.append(bufferCheckCode);

                if (prefireCondition.length() > 0) {
                    code.append("if (" + prefireCondition.substring(4) + ") {" + _eol);
                }
                if (resetCondition.length() > 0) {
                    code.append(resetCondition + _eol);
                }                

                String pnPostfireCode = "";
                code.append("//This is where the loops are generated" + _eol);
                // if firingCountLimit exists, generate for loop.
                if (actor instanceof LimitedFiringSource) {
                    int firingCount = ((IntToken) ((LimitedFiringSource) actor)
                            .firingCountLimit.getToken()).intValue();

                    if (firingCount > 0) {
                        openBracket = true;
                        code.append("static int i = 0;" + _eol);
                        code.append("if (i < " + firingCount 
                                + ") {" + _eol + "i++;" + _eol);
                    }

                    pnPostfireCode = _eol;
                } else {

                }


                if (_DEBUG) {
                    code.append("printf(\"Fire " + 
                            actor.getName() + "\\n\");" + _eol);
                }
                
                // Starts the timer for the fire block
                if (_doMeasureTime()) {
                    code.append("start_timer(&" + _getActorTimer(actor) + ");" + _eol);
                }
                    
                code.append(helper.generateFireCode());

                if (_DEBUG) {
                    code.append("printf(\"Finished firing " + 
                            actor.getName() + "\\n\");" + _eol);
                }

                // If not inline, generateFireCode() would be a call
                // to the fire function which already includes the 
                // type conversion code.
                if (inline) {
                    code.append(helper.generateTypeConvertFireCode());
                }

                code.append(helper.generatePostfireCode());

                // Increment port offset.
                for (IOPort port : (List<IOPort>) ((Entity) actor).portList()) {
                    // Determine the amount to increment.
                    int rate = DFUtilities.getRate(port);

                    PortCodeGenerator portHelper = (PortCodeGenerator) _getHelper(port);

                    //int width = port.getWidth();
                    //for (int i = 0; i < width; i++) {
                    if (port.isInput()) {
                        pnPostfireCode += portHelper.updateOffset(rate, _director);
                    } else {
                        pnPostfireCode += portHelper.updateConnectedPortsOffset(rate, _director);
                    }
                    //}
                }

                // Code for incrementing buffer offsets.
                code.append(pnPostfireCode);
                
                // End the timer for the fire block
                if (_doMeasureTime()) {
                    code.append("stop_timer(&" + _getActorTimer(actor) + ");" + _eol);
                }
                
                // increment number of firings
                code.append(_getActorRounds(actor) + "++;" + _eol);

                if (openBracket) {
                    code.append("}" + _eol);
                }
                if (prefireCondition.length() > 0) {
                    code.append("}" + _eol);
                }

                //code.append("incrementReadBlockingThreads(&" + + ");" + _eol);
            }            

            // wrapup
            //code.append(helper.generateWrapupCode());

            code.append("return NULL;" + _eol);
            code.append("}" + _eol);
            
            //code.insert(variableInitializationPos, helper.generateVariableInitialization());
        }
    }

    
    private String getIsMpiBufferFull(IOPort port, int channelNumber, int rate) {
        return "$isMpiBufferFull(&" + generatePortHeader(port, channelNumber) + ", " + rate + ")";
    }

    public int getNumberOfMpiConnections(boolean promoteToPowerOfTwo) throws IllegalActionException {
        int value = ((IntToken) ((Parameter) _director.getAttribute(
                "_numberOfMpiConnections")).getToken()).intValue();
        
        return (promoteToPowerOfTwo) ? _ceilToPowerOfTwo(value) : value;  
        
    }

    public static String getSendTag(IOPort port, int channel) {
        return getBufferLabel(port, channel) + "_sendTag";
    }

    public static String getReceiveTag(IOPort port, int channel) {
        return getBufferLabel(port, channel) + "_recvTag";
    }

    private String _generateRequest(TypedIOPort port, int channel) {
        return CodeGeneratorHelper.generateName(port) + "_" + channel + "_request";        
    }

    private String _generateTestCounter() {
        return "test_counter";
    }

    private String _getReceiveFlag(IOPort port, int channel) {
        return "has_" + CodeGeneratorHelper.generateName(port) + "_" + channel + "_recvFlag";
    }

    public static String generateFreeSlots(IOPort port, int channelNumber) {
        return MpiPNDirector.generatePortHeader(port, channelNumber) + "_freeSlots";        
    }

    public static String getBufferLabel(IOPort port, int channel) {
        return CodeGeneratorHelper.generateName(port) + "_" + channel; 
    }

    private String _getHasLocalInput(IOPort port, int channel, int rate) {        
        return "$hasLocalInput(&" + generatePortHeader(port, channel) + ")";
    }

    private String _getIsLocalBufferFull(IOPort port, int channel, int rate) {        
        return "$isLocalBufferFull(&" + generatePortHeader(port, channel) + ")";
    }
    
    private String _getHasInputFlag(IOPort port, int channel) {
        return "has_" + CodeGeneratorHelper.generateName(port) + "_" + channel;
    }

    /** Generate the Mpi function name for a given actor.
     * @param actor The given actor.
     * @return A unique label for the actor thread function.
     */
    private String _getActorMpiLabel(Actor actor) {
        return CodeGeneratorHelper.generateName((NamedObj) actor)
        + "_MpiFunction";
    }

    private static String _getActorTimer(Actor actor) {
        return CodeGeneratorHelper.generateName((NamedObj) actor) + "_timer";
    }

    private static String _getActorRoundsLimit(Actor actor) {
        return CodeGeneratorHelper.generateName((NamedObj) actor).toUpperCase() 
            + "_ROUNDS_LIMIT";
    }

    private static String _getActorRounds(Actor actor) {
        return CodeGeneratorHelper.generateName((NamedObj) actor) + "_Rounds";
    }

    private static String _getActorTerminate(Actor actor) {
        return CodeGeneratorHelper.generateName((NamedObj) actor) + "_Terminate";
    }
    
    private HashSet<String> _buffers = new HashSet<String>();

    public static String generateMpiRequest(IOPort port,
            int channelNumber) {
        return MpiPNDirector.generatePortHeader(port, channelNumber) + "_requests";        
    }
    
    private boolean _doMeasureTime() { 
        return _director.getAttribute("_measureTime") != null;
    }

    MultiHashMap _rankActors = new MultiHashMap();
    int _numProcessors = 0;
}
