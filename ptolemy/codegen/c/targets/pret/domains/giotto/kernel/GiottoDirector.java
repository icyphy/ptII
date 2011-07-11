/* Code generator helper class associated with the GiottoDirector class.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.c.targets.pret.domains.giotto.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

////GiottoDirector

/**
 Code generator helper associated with the PRET GiottoDirector class. This class
 is also associated with a code generator.

 @author Ben Lickly, Shanna-Shaye Forbes, Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.2
 @Pt.ProposedRating Red (blickly)
 @Pt.AcceptedRating Red (blickly)
 */
public class GiottoDirector extends
        ptolemy.codegen.c.domains.giotto.kernel.GiottoDirector {

    /** Construct the code generator helper associated with the given
     *  GiottoDirector.
     *  @param giottoDirector The associated
     *  ptolemy.domains.giotto.kernel.GiottoDirector
     */
    public GiottoDirector(
            ptolemy.domains.giotto.kernel.GiottoDirector giottoDirector) {
        super(giottoDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     * Generate The fire function code. This method is called when the firing
     * code of each actor is not inlined. Each actor's firing code is in a
     * function with the same name as that of the actor.
     *
     * @return The fire function code, in this case, return the empty string.
     * @exception IllegalActionException Not thrown in this base class
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        if (_debugging) {
            _debug("generateFireFunctionCode from pret giotto director called here");
        }
        return "";
    }

    /** Generate the preinitialization code for the Giotto Director which includes
     * generating driver code for all the actors as well as the firefunction code.
     * None of the methods can be inlined in this implementation.
     * @return String containing the preinitialization code.
     * @exception IllegalActionException If thrown while generating input driver,
     * output driver or top level fire code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generatePreinitializeCode());

        code.append(_eol + _generateInDriverCode());
        code.append(_eol + _generateOutDriverCode());
        code.append(_generateActorsCode());

        if (_isTopDirectorFSM()) {
            code.append(_eol
                    + "//################# fire code for Giotto stuff here"
                    + _eol);
            code.append(_generateFireCode());
            code.append(_eol
                    + "//end of generate fire code stuff for top director fsm"
                    + _eol);
        }

        return processCode(code.toString());
    }

    /**
     * Generate the contents of the main loop.
     * @return The contents of the main loop.
     * @exception IllegalActionException If thrown while accessing the model.
     */
    public String generateMainLoop() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_eol);
        String jumpBuffer = "__deadline_trying_jmpbuf__";
        code.append("jmp_buf " + jumpBuffer + ";" + _eol
                + "register_jmpbuf(0,&" + jumpBuffer + ");" + _eol
                + "if (setjmp(" + jumpBuffer + ")!=0) {" + _eol
                + "  puts(\"Timing failure!\\n\");" + _eol
                + "  END_SIMULATION;" + _eol + "}" + _eol + "SYNC(\"3F\");"
                + _eol);
        //code.append("//generate main loop called for director "+_director.getFullName());
        Attribute iterations = _director.getAttribute("iterations");
        if (iterations == null) {
            code.append("while (true) {" + _eol);
        } else {
            int iterationCount = ((IntToken) ((Variable) iterations).getToken())
                    .intValue();
            if (iterationCount <= 0) {
                code.append("while (true) {" + _eol);
            } else {
                code.append("while(true){" + _eol);
                // Declare iteration outside of the loop to avoid
                // mode" with gcc-3.3.3
                //code.append(_eol + "int iteration;" + _eol);
                //code.append("for (iteration = 0; iteration < "
                //      + iterationCount + "; iteration ++) {" + _eol);
                //call the actor methods here....
            }
        }

        code.append(_generateFireCode());

        // The code generated in generateModeTransitionCode() is executed
        // after one global iteration, e.g., in HDF model.
        ActorCodeGenerator modelHelper = (ActorCodeGenerator) _getHelper(_director
                .getContainer());
        modelHelper.generateModeTransitionCode(code);

        /*if (callPostfire) {
                    code.append(_INDENT2 + "if (!postfire()) {" + _eol + _INDENT3
                            + "break;" + _eol + _INDENT2 + "}" + _eol);
                }
         */

        code.append(generatePostfireCode());

        code.append("}" + _eol);

        return code.toString();
    }

    /** Generate a variable declaration for the <i>period</i> parameter,
     *  if there is one.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer variableDeclarations = new StringBuffer();
        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            variableDeclarations.append(helperObject
                    .generateVariableDeclaration());

            List<TypedIOPort> actorPorts = actor.outputPortList();
            if (actorPorts.size() > 0) {
                Iterator portItr = actorPorts.iterator();
                TypedIOPort actorport;
                String type;
                while (portItr.hasNext()) {
                    actorport = (TypedIOPort) portItr.next();
                    //variableDeclarations.append(_eol+"//not composite actor so may need extra variable, later, make sure you check the type. It's default to static int at the moment"+_eol);
                    type = targetType(actorport.getType());
                    variableDeclarations.append("static " + type + " "
                            + _getActorName(actor) + "_output;" + _eol);
                }
            }
            variableDeclarations
                    .append(_generatePortVariableDeclarations(actor));
        }

        return variableDeclarations.toString();
    }

    /** List the extra header files required by this Giotto Director.
     *  @return HashSet containing the header files.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        HashSet files = new HashSet();
        files.add("\"deadline.h\"");
        files.add("<setjmp.h>");
        return files;
    }

    /**
     * Return an unique label for the given port channel referenced
     * by the given helper.
     * @param port The given port.
     * @param channelAndOffset The given channel and offset.
     * @param forComposite Whether the given helper is associated with
     *  a CompositeActor
     * @param isWrite The type of the reference. True if this is
     *  a write reference; otherwise, this is a read reference.
     * @param helper The specified helper.
     * @return an unique reference label for the given port channel.
     * @exception IllegalActionException If the helper throws it while
     *  generating the label.
     */
    public String getReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite, CodeGeneratorHelper helper)
            throws IllegalActionException {
        Actor actor = (Actor) port.getContainer();
        Director director = actor.getDirector();
        if (_debugging) {
            _debug("Getting reference for port " + port.getFullName()
                    + "of actor " + actor.getFullName() + " of director "
                    + director.getFullName());
        }
        if (port.isOutput() && forComposite == false
                && !director.getFullName().contains("SDF")) {
            if (channelAndOffset[0].equals("")) {
                channelAndOffset[0] = "0";
            }
            return CodeGeneratorHelper.generateName(port);
        } else {
            return super.getReference(port, channelAndOffset, forComposite,
                    isWrite, helper);
        }
    }

    /**
     * This method is similar to the getReference() method however it it tailored 
     * for use by a driver method.
     * @param port The port whose information is desired.
     * @param channelAndOffset The given channel and offset.
     * @param forComposite Whether the given helper is associated with
     *  a CompositeActor
     * @param isWrite The type of the reference. True if this is
     *  a write reference; otherwise, this is a read reference.
     * @param helper The specified helper.
     * @return an unique reference label for the given port channel.
     * @exception IllegalActionException If the helper throws it while
     *  generating the label.
     */
    public String getDriverReference(TypedIOPort port,
            String[] channelAndOffset, boolean forComposite, boolean isWrite,
            CodeGeneratorHelper helper) throws IllegalActionException {

        Actor actor = (Actor) port.getContainer();
        Director director = actor.getDirector();
        if (_debugging) {
            _debug("inDriverGetReference for actor" + actor.getFullName()
                    + "the directors name is " + director.getFullName());
        }
        if (port.isOutput()
                || ((port.isInput() && (actor instanceof CompositeActor)
                        && (director != null)
                        && (director.getFullName().contains("modal") == false) && (director
                        .getFullName().contains("_Director") == false)))) {//if it's an output or an input that's not a modal model(contains modal or _Director)
            return "*" + CodeGeneratorHelper.generateName(port) + "_PORT"; // will need to handle multiple channels later
        } else {
            return super.getReference(port, channelAndOffset, forComposite,
                    isWrite, helper);
        }

    }

    /** Generate the content of a driver methods. For each actor
     *  update it's inputs to the outputs stored in ports. The PORT
     *  allows double buffering, in this case the output variable is
     *  used as the port. PORT here is simply a common variable, not a
     *  PORT in the general Ptolemy II actor sense
     *  
     *  <p>NOTE: Duplicate ports connected through a fork are
     *  removed. IE. if an input is connected to a fork and the fork
     *  is connected to two other places... it removes the first place
     *  from the list of places and keeps the last place need to ask
     *  Jackie if there is a way to work around this b/c Reciever [][]
     *  recievers = getRecievers doesn't work.
     *  @return code that copies outputs to a port, and inputs from a
     *  port in a driver method
     *  @exception IllegalActionException If there is a problem accessing
     *  the model or generating the code.
     */
    public String _generateInDriverCode() throws IllegalActionException {
        // FIXME: this should not be public and have an underscore.
        StringBuffer code = new StringBuffer();
        if (_debugging) {
            _debug("generateDriver Code has been called");
        }
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {

            List inputPortList = actor.inputPortList();
            if (_debugging) {
                _debug("this actor" + actor.getDisplayName() + " has "
                        + inputPortList.size() + " input ports.");
            }
            Iterator inputPorts = inputPortList.iterator();

            String actorDriverCode = "";
            String sinkReference = "";
            String srcReference = "";
            String temp = "";
            StringBuffer transferIn = new StringBuffer();
            int i = 0; //sink index counter
            int j = 0; // src index counter
            CodeGeneratorHelper myHelper;
            while (inputPorts.hasNext()) {
                i = 0; // this is a test to see if this is to be done here, if so remove the i++ from the end of the loop
                j = 0;
                TypedIOPort port = (TypedIOPort) inputPorts.next();
                if (_debugging) {
                    _debug("this port's name is " + port.getFullName());
                }
                //List<IOPort> connectedPorts = port.deepConnectedOutPortList();
                List<IOPort> connectToMe = port.sourcePortList();//port.connectedPortList(); //connectedPortList();
                if (_debugging) {
                    _debug("connectToMe size is " + connectToMe.size());
                }

                Iterator tome = connectToMe.iterator();
                if (_debugging) {
                    _debug("currently connectToMe size is "
                            + connectToMe.size());
                }
                tome = connectToMe.iterator();
                while (tome.hasNext()) {
                    IOPort tempp = (IOPort) tome.next();
                    if (_debugging) {
                        _debug("******I'm connected to I think: "
                                + tempp.getFullName());
                    }
                }

                // Iterator cpIterator = connectedPorts.iterator();
                Iterator cpIterator = connectToMe.iterator();
                while (cpIterator.hasNext()) {//&&(j <connectToMe.size()-1)){
                    TypedIOPort sourcePort = (TypedIOPort) cpIterator.next();
                    // FIXME: figure out the channel number for the sourcePort.
                    // if you need to transfer inputs inside
                    if (actor instanceof CompositeActor) {
                        if (_debugging) {
                            _debug("composite actor so doing stuff for that");
                        }
                        //GiottoDirector directorHelper = (GiottoDirector) _getHelper(actor.getDirector());
                        //_generateTransferInputsCode(port, transferIn);
                        transferIn
                                .append(("//should transfer input for this actor to from the outside to inside" + _eol));
                        //generateTransferInputsCode(inputPort, code);

                    }
                    if (_debugging) {
                        _debug(" j is " + j + "and size of connect to me is "
                                + connectToMe.size());
                    }
                    String channelOffset[] = { "0", "0" };
                    if (_debugging) {
                        _debug("the sender port is named "
                                + sourcePort.getFullName()
                                + " and the receiver is " + port.getFullName());
                    }
                    myHelper = (CodeGeneratorHelper) this._getHelper(sourcePort
                            .getContainer());
                    // temp+= _generateTypeConvertFireCode(false)+_eol;
                    channelOffset[0] = Integer.valueOf(i).toString();
                    if (_debugging) {
                        _debug("channel offset is " + channelOffset[0]);
                    }
                    srcReference = this.getDriverReference(
                            (TypedIOPort) sourcePort, channelOffset, false,
                            true, myHelper);
                    if (_debugging) {
                        _debug("after first call to getReference");
                    }
                    //                   
                    myHelper = (CodeGeneratorHelper) _getHelper(actor);
                    channelOffset[0] = Integer.valueOf(j).toString();
                    if (_debugging) {
                        _debug("channel offset is " + channelOffset[0]);
                    }
                    sinkReference = this.getDriverReference((TypedIOPort) port,
                            channelOffset, false, true, myHelper);
                    if (_debugging) {
                        _debug("after second call to getReference");
                    }
                    j++;

                    //temp+= _generateTypeConvertFireCode(sourcePort,port);//+_eol;
                    temp = _typeConversion(sourcePort, port);
                    if (_debugging) {
                        _debug("I think the source Reference is "
                                + srcReference + " and it's display name is "
                                + sourcePort.getDisplayName());
                        _debug("I think the sink Reference is " + sinkReference
                                + " and it's display name is "
                                + port.getDisplayName());
                    }
                    String src;
                    temp = _typeConversion(sourcePort, port);
                    if (temp.length() == 0) {
                        src = srcReference;
                    } else {
                        src = temp + "(" + srcReference + ")";
                    }

                    actorDriverCode += sinkReference + " = " + src + ";" + _eol;
                }
                i++; // increment the ofset variable // not sure if this is correct since we're using iterators 
            }
            if (_debugging) {
                _debug("actorDriverCode is now:");
                _debug(actorDriverCode);
            }

            ArrayList args = new ArrayList();
            args.add(_generateDriverName((NamedObj) actor) + "_in");
            args.add(actorDriverCode);
            code.append(_generateBlockCode("driverCode", args));
        }

        return code.toString();
    }

    /** Generate the content of output driver methods. The output
     *  driver updates the value of a port to be that of the output of
     *  the latest execution of a task.
     *  @return code that copies outputs to a port
     *  @exception IllegalActionException If there is a problem accessing
     *  the model or generating the code.
     */
    public String _generateOutDriverCode() throws IllegalActionException {
        // FIXME: this should not be public and have an underscore.
        StringBuffer code = new StringBuffer();
        if (_debugging) {
            _debug("generateDriver Code has been called");
        }
        String sinkReference;
        String srcReference;
        StringBuffer actorDriverCode;
        CodeGeneratorHelper myHelper;
        Director dir;

        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {

            List outputPortList = actor.outputPortList();
            Iterator outputPorts = outputPortList.iterator();
            sinkReference = "";
            srcReference = "";
            actorDriverCode = new StringBuffer("");
            dir = actor.getDirector();
            code.append(_eol + "//My Director's name is: " + dir.getFullName()
                    + _eol);
            if (actor instanceof CompositeActor
                    && (dir.getFullName().contains("modal") || dir
                            .getFullName().contains("_Director"))) {
                code.append(_eol
                        + "// should transfer from my outputs to my output ports"
                        + _eol);
                actorDriverCode.append(_eol + "// in first if" + _eol);
                while (outputPorts.hasNext()) {
                    IOPort sourcePort = (IOPort) outputPorts.next();
                    // FIXME: figure out the channel number for the sourcePort.
                    // if you need to transfer inputs inside
                    String channelOffset[] = { "0", "0" };
                    int i = sourcePort.getWidth();
                    myHelper = (CodeGeneratorHelper) this._getHelper(sourcePort
                            .getContainer());
                    if (i > 1) {
                        //I don't think this is correct
                        for (int j = 0; j < i; j++) {

                            actorDriverCode.append("// multiport stuff here");
                        }
                    } else {
                        channelOffset[0] = "0";
                        //code.append(_eol+"in else"+_eol);
                        srcReference = this.getReference(
                                (TypedIOPort) sourcePort, channelOffset, false,
                                true, myHelper);
                        sinkReference = this.getDriverReference(
                                (TypedIOPort) sourcePort, channelOffset, false,
                                true, myHelper);
                        ArrayList args = new ArrayList();
                        args.add(sinkReference);
                        args.add(srcReference);
                        actorDriverCode.append(_generateBlockCode("updatePort",
                                args));
                    }
                }
            } else if (actor instanceof CompositeActor
                    && dir.getFullName().contains("SDF")) {
                code.append(_eol + "//in second if with director "
                        + dir.getFullName() + _eol);
                while (outputPorts.hasNext()) {
                    TypedIOPort sourcePort = (TypedIOPort) outputPorts.next();
                    TypedIOPort sp = sourcePort; // not the real value.. this was just assigned so the compiler would stop complaining
                    List<TypedIOPort> ports = sourcePort
                            .deepConnectedPortList();//this is a temporary fix
                    for (TypedIOPort port : ports) {
                        if (port.isOutput()) {
                            sp = port;
                        }
                        if (_debugging) {
                            _debug("In a non-modal CompositeActor, port: "
                                    + port.getFullName());
                        }
                    }

                    // FIXME: this currently assumes that the actor on the inside of the composite actor only has one output port and that
                    // port's output info needs to be moved over. I also haven't figure out how to support multiport at the moment
                    String channelOffset[] = { "0", "0" };
                    int i = sourcePort.getWidth();
                    myHelper = (CodeGeneratorHelper) this._getHelper(sourcePort
                            .getContainer());
                    if (i > 1) {
                        //I don't think this is correct
                        for (int j = 0; j < i; j++) {

                            actorDriverCode.append("//multiport stuff here");
                        }
                    } else {
                        channelOffset[0] = "0";
                        //code.append(_eol+"in else"+_eol);
                        srcReference = super.getReference((TypedIOPort) sp,
                                channelOffset, true, true, myHelper); // the hope is that a call to super will return the righ thing
                        sinkReference = this.getDriverReference(
                                (TypedIOPort) sourcePort, channelOffset, false,
                                true, myHelper);
                        String temp = _typeConversion(sp, sourcePort);
                        String src;
                        if (temp.length() == 0) {
                            src = srcReference;
                        } else {
                            src = temp + "(" + srcReference + ")";
                        }

                        actorDriverCode.append(sinkReference + " = " + src
                                + ";" + _eol);
                    }
                }

            } else if (actor instanceof CompositeActor && dir != null) {
                code.append(_eol + "//in third if with director "
                        + dir.getFullName() + _eol);
                while (outputPorts.hasNext()) {
                    TypedIOPort sourcePort = (TypedIOPort) outputPorts.next();
                    TypedIOPort sp = sourcePort; // not the real value.. this was just assigned so the compiler would stop complaining
                    List<TypedIOPort> ports = sourcePort.insidePortList();
                    for (TypedIOPort port : ports) {
                        if (port.isOutput()) {
                            sp = port;
                        }
                        if (_debugging) {
                            _debug("In a non-modal CompositeActor, port: "
                                    + port.getFullName());
                        }
                    }

                    // FIXME: this currently assumes that the actor on the inside of the composite actor only has one output port and that
                    // port's output info needs to be moved over. I also haven't figure out how to support multiport at the moment
                    String channelOffset[] = { "0", "0" };
                    int i = sourcePort.getWidth();
                    myHelper = (CodeGeneratorHelper) this._getHelper(sourcePort
                            .getContainer());
                    if (i > 1) {
                        //I don't think this is correct
                        for (int j = 0; j < i; j++) {

                            actorDriverCode.append("//multiport stuff here");
                        }
                    } else {
                        channelOffset[0] = "0";
                        //code.append(_eol+"in else"+_eol);
                        srcReference = this.getReference((TypedIOPort) sp,
                                channelOffset, true, true, myHelper);
                        sinkReference = this.getDriverReference(
                                (TypedIOPort) sourcePort, channelOffset, false,
                                true, myHelper);
                        String temp = _typeConversion(sp, sourcePort);
                        System.out.println("the name of the source port is "
                                + sp.getFullName());
                        System.out.println("the name of the sink is "
                                + sourcePort.getFullName());

                        String src;
                        if (temp.length() == 0) {
                            src = srcReference;
                        } else {
                            src = temp + "(" + srcReference + ")";
                        }

                        actorDriverCode.append(sinkReference + " = " + src
                                + ";" + _eol);
                    }
                }
            } else {
                while (outputPorts.hasNext()) {
                    IOPort sourcePort = (IOPort) outputPorts.next();
                    // FIXME: figure out the channel number for the sourcePort.
                    // if you need to transfer inputs inside
                    String channelOffset[] = { "0", "0" };
                    int i = sourcePort.getWidth();
                    myHelper = (CodeGeneratorHelper) this._getHelper(sourcePort
                            .getContainer());
                    if (i > 1) {
                        //I don't think this is correct
                        for (int j = 0; j < i; j++) {

                            actorDriverCode.append("//multiport stuff here");
                        }
                    } else {
                        channelOffset[0] = "0";
                        //code.append(_eol+"in else"+_eol);
                        srcReference = this.getReference(
                                (TypedIOPort) sourcePort, channelOffset, false,
                                true, myHelper);
                        sinkReference = this.getDriverReference(
                                (TypedIOPort) sourcePort, channelOffset, false,
                                true, myHelper);
                        ArrayList args = new ArrayList();
                        args.add(sinkReference);
                        args.add(srcReference);
                        code.append(_eol + "// my class's name is : "
                                + myHelper.getClassName() + _eol);
                        code.append(_eol + "//in Last Else of outDriverCode"
                                + _eol);
                        actorDriverCode.append(_generateBlockCode("updatePort",
                                args));
                    }
                }

            }

            ArrayList args = new ArrayList();
            args.add(_generateDriverName((NamedObj) actor) + "_out");
            args.add(actorDriverCode.toString());
            code.append(_generateBlockCode("driverCode", args));
        }

        return code.toString();
    }

    /**
     * Generate the type conversion fire code. This method is called by the
     * Director to append necessary fire code to handle type conversion.
     * @param source The source port, ignored by this method.
     * @param sink The sink port, ignored by this method.
     * @return The generated code, in this case, return the empty string.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String _generateTypeConvertFireCode(IOPort source, IOPort sink)
            throws IllegalActionException {
        if (_debugging) {
            _debug("generateTypeConvertFireCode in OpenRTOS giotto director called");
        }
        return "";
    }

    /**
     * Generate the fire code for the director.
     * In this case the fire code is simply the OpenRTOS thread code.
     * @return The generated code.
     * @exception IllegalActionException If the thread code cannot be generated.     * 
     */
    protected String _generateFireCode() throws IllegalActionException {
        if (_debugging) {
            _debug("_generateFireCode has been called");
        }
        StringBuffer code = new StringBuffer();
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {
            code.append(_generateRecursiveFireCode(actor));
        }
        return code.toString();
    }

    private String _generateRecursiveFireCode(Actor actor)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        double period = _getPeriod();
        // FIXME: generate deadline instruction w/ period

        // Note: Currently, the deadline instruction takes the number 
        // of cycle as argument. In the future, this should be changed
        // to use time unit, which is platform-independent. 
        // So for now, we will assume the clock speed to be 250 Mhz. 
        // Thus, in order to get a delay of t seconds (= period/frequency) 
        // for each actor, we will need to wait for 
        // period/frequency * 250,000,000 cycles/second.

        int cycles = (int) (250000000 * period / _getFrequency(actor));
        String driverBound = CodeGeneratorHelper.generateName((NamedObj) actor)
                + "_OUTPUT_DRIVER_WCET";
        code.append("#ifdef THREAD_" + threadID + _eol + "#ifndef "
                + driverBound
                + _eol
                // #warning is non-standard, but useful.
                + "#warning \"" + driverBound
                + " was not defined.  Using default value of 1000.\"" + _eol
                + "#define " + driverBound + " 1000" + _eol + "#endif" + _eol);
        // for
        //String index = CodeGeneratorHelper.generateName((NamedObj) actor)
        //        + "_frequency";

        code.append("DEADBRANCH0(" + cycles + "-" + driverBound
                + "); // period - driver_wcet" + _eol + _getActorName(actor)
                + "_driver_in();//read inputs from ports deterministically"
                + _eol);

        code.append(_getActorName(actor) + "();" + _eol);
        code.append("DEADBRANCH0(" + driverBound + "); // driver_wcet" + _eol);

        // code.append(helper.generateFireCode());
        //code.append(helper.generatePostfireCode());
        code.append(_getActorName(actor)
                + "_driver_out(); // output values to ports deterministically"
                + _eol);
        //code.append("}" + _eol); // end of for loop
        code.append(_eol);
        code.append("#endif /* THREAD_" + threadID + "*/\n");
        threadID++;
        if (actor instanceof CompositeActor) {
            code.append(_eol + "//I'm a composite actor" + _eol);
            if (actor.getDirector() instanceof ptolemy.domains.giotto.kernel.GiottoDirector) {
                for (Actor actor1 : (List<Actor>) ((TypedCompositeActor) actor
                        .getDirector().getContainer()).deepEntityList()) {
                    code.append(_generateRecursiveFireCode(actor1));

                }
            }
        }
        return code.toString();
    }

    /**
     * This method creates port variables for the actor passed in as a parameter.
     * @param actor Actor whose port variables need to be declared
     * @return Port variables associated with this actor
     * @throws IllegalActionException
     */

    private String _generatePortVariableDeclarations(Actor actor)
            throws IllegalActionException {

        StringBuffer code = new StringBuffer();
        code.append("/*" + _getActorName(actor)
                + "'s PORT variable declarations.*/" + _eol);
        if (actor instanceof CompositeActor) {
            Director myDir = actor.getExecutiveDirector();
            if (myDir != null) // composite actor with a director so create PORTS for my inputs
            {
                Iterator inputPorts = actor.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                    code.append("volatile "
                            + targetType(inputPort.getType())
                            + " * "
                            + generateName(inputPort)
                            + "_PORT = ("
                            + targetType(inputPort.getType())
                            + " * ) 0x"
                            + Integer
                                    .toHexString(_getThenIncrementCurrentSharedMemoryAddress(inputPort)));
                    if (inputPort.isMultiport()) {
                        code.append("[" + inputPort.getWidthInside() + "]");
                    }

                    int bufferSize = getBufferSize(inputPort);

                    if (bufferSize > 1) {
                        code.append("[" + bufferSize + "]");
                    }
                    code.append(";" + _eol);
                }

            }
        }

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // If either the output port is a dangling port or
            // the output port has inside receivers.
            //if (!outputPort.isOutsideConnected() || outputPort.isInsideConnected()) {
            if (true) {
                //not sure how this will work for multiports yet

                //code.append("/*"+_getActorName(actor)+ "'s PORT variable declarations.*/"+_eol);
                code.append("volatile "
                        + targetType(outputPort.getType())
                        + " * "
                        + generateName(outputPort)
                        + "_PORT = ("
                        + targetType(outputPort.getType())
                        + " * ) 0x"
                        + Integer
                                .toHexString(_getThenIncrementCurrentSharedMemoryAddress(outputPort)));

                if (outputPort.isMultiport()) {
                    code.append("[" + outputPort.getWidthInside() + "]");
                }

                int bufferSize = getBufferSize(outputPort);

                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
                code.append(";" + _eol);
            }

        }

        return code.toString();
    }

    /**
     * Determines the frequency of the actor. If no frequency is specified in the model
     * the default value is set to 1
     * @param actor
     * @return an integer containing the frequency of the actor
     * @throws IllegalActionException
     */
    private int _getFrequency(Actor actor) throws IllegalActionException {
        Attribute frequency = ((Entity) actor).getAttribute("frequency");
        if (frequency == null) {
            return 1;
        } else {
            return ((IntToken) ((Variable) frequency).getToken()).intValue();
        }
    }

    /**
     * This method finds the period of the top most Giotto director in the model 
     * and returns it as the period of this director.
     * @return  period of the top most Giotto Director
     * @throws IllegalActionException
     */
    private double _getPeriod() throws IllegalActionException {

        Director director = ((TypedCompositeActor) _director.getContainer())
                .getExecutiveDirector();

        while (director != null
                && !(director instanceof ptolemy.domains.giotto.kernel.GiottoDirector)) {
            director = ((TypedCompositeActor) director.getContainer())
                    .getExecutiveDirector();
        }
        if (director == null) {
            // This is the case for the outer-most Giotto director.
            Attribute period = _director.getAttribute("period");
            double periodValue;

            if (period != null) {
                periodValue = ((DoubleToken) ((Variable) period).getToken())
                        .doubleValue();
            } else {
                throw new IllegalActionException(this, "There is no period"
                        + "specified in the outer-most Giotto director");
            }
            return periodValue;
        }

        // Get the frequency.
        int frequency = _getFrequency((Actor) _director.getContainer());
        return ((GiottoDirector) _getHelper(director))._getPeriod() / frequency;

    }

    /**
     * Returns the name of the actor with '.' and ' '" replaced with '_'
     * @param actor 
     * @return name of actor
     */
    private String _getActorName(Actor actor) {
        String actorFullName = actor.getFullName();
        actorFullName = actorFullName.substring(1, actorFullName.length());
        actorFullName = actorFullName.replace('.', '_');
        actorFullName = actorFullName.replace(' ', '_');
        return actorFullName;
    }

    /**
     * Generates a name for a driver method.
     * @param namedObj The object for which a name should be generated.
     * @return The driver name
     */
    private static String _generateDriverName(NamedObj namedObj) {
        String name = StringUtilities.sanitizeName(namedObj.getFullName());
        // FIXME: Assume that all objects share the same top level. In this case,
        // having the top level in the generated name does not help to
        // expand the name space but merely lengthen the name string.
        //        NamedObj parent = namedObj.toplevel();
        //        if (namedObj.toplevel() == namedObj) {
        //            return "_toplevel_";
        //        }        
        //        String name = StringUtilities.sanitizeName(namedObj.getName(parent));
        if (name.startsWith("_")) {
            name = name.substring(1, name.length());
        }
        return name.replaceAll("\\$", "Dollar") + "_driver";

    }

    private String _generateActorsCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_debugging) {
            _debug("generateActors Code has been called");
        }
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {
            System.out.println("actor name is " + actor.getFullName()
                    + " with director " + actor.getDirector().getFullName());
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            // generate methods with fire code for the actors inside a composite actor with an sdf director
            code.append("//before appending fire code for the contents of the composite actor");
            if (actor instanceof CompositeActor) {

                if (actor.getDirector() instanceof ptolemy.domains.sdf.kernel.SDFDirector) {
                    for (Actor actor1 : (List<Actor>) ((TypedCompositeActor) actor
                            .getDirector().getContainer()).deepEntityList()) {
                        System.out.println("contained actors includes: "
                                + actor1.getFullName());
                        CodeGeneratorHelper actor1Helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor1);
                        //code.append(actor1.getFullName()+"(void) {"+_eol);
                        code.append(actor1Helper.generateFireFunctionCode());
                        //code.append(_eol+" } "+_eol);

                    }

                }
            }
            code.append("//after appending fire code for the contents of the composite actor");

            String actorFullName = _getActorName(actor);
            String methodSignature = _eol + "void " + actorFullName
                    + _getFireFunctionArguments() + " {" + _eol;

            //code.append(_eol + "void " + actorFullName+ _getFireFunctionArguments() + " {"
            //      + _eol);
            String srcReference;
            String sinkReference;

            Iterator<IOPort> inputPorts;
            inputPorts = actor.inputPortList().iterator();
            Director dir = actor.getDirector();

            if (actor instanceof CompositeActor) {

                if (dir == null) {
                    code.append(methodSignature);
                    while (inputPorts.hasNext()) {
                        IOPort inputPort = inputPorts.next();
                        if (_debugging) {
                            _debug("composite actor so doing stuff for that");
                        }
                        super.generateTransferInputsCode(inputPort, code);
                        super.generateTransferOutputsCode(inputPort, code);
                        code.append(_eol
                                + "//just did the transfer in and out of this composite actor with a call to super methods"
                                + _eol);
                        // else do nothing
                    }
                    code.append("}" + _eol);
                } else if (dir.getClassName().equals(
                        "ptolemy.domains.fsm.kernel.FSMDirector")
                        || dir.getClassName().equals(
                                "ptolemy.domains.sdf.kernel.SDFDirector")) {
                    // code.append("//second if"+_eol);

                    if (actor.getClass().getName()
                            .contains("ptolemy.actor.lib.jni.EmbeddedCActor")) {
                        //code.append("//transfer inputs in"+_eol);
                        CodeGeneratorHelper myHelper;
                        StringBuffer actorTransferCode = new StringBuffer(" ");

                        while (inputPorts.hasNext()) {
                            IOPort sourcePort = (IOPort) inputPorts.next();
                            // FIXME: figure out the channel number for the sourcePort.
                            // if you need to transfer inputs inside
                            String channelOffset[] = { "0", "0" };
                            int i = sourcePort.getWidth();
                            myHelper = (CodeGeneratorHelper) this
                                    ._getHelper(sourcePort.getContainer());
                            if (i > 1) {
                                //I don't think this is correct
                                for (int j = 0; j < i; j++) {

                                    actorTransferCode
                                            .append("//multiport stuff here");
                                }
                            } else {
                                channelOffset[0] = "0";
                                //code.append(_eol+"in else"+_eol);
                                sinkReference = this.getReference(
                                        (TypedIOPort) sourcePort,
                                        channelOffset, false, true, myHelper);
                                srcReference = this.getDriverReference(
                                        (TypedIOPort) sourcePort,
                                        channelOffset, false, true, myHelper);
                                ArrayList args = new ArrayList();
                                args.add(sinkReference);
                                args.add(srcReference);
                                actorTransferCode.append(_generateBlockCode(
                                        "updatePort", args));
                            }
                        }
                        code.append(methodSignature);

                        //code.append("//display name: "+actor.getDisplayName()+_eol);
                        inputPorts = actor.inputPortList().iterator();
                        String channelOffset[] = { "0", "0" };
                        // CodeGeneratorHelper myHelper;  
                        ArrayList args = new ArrayList();
                        while (inputPorts.hasNext()) {
                            IOPort source = inputPorts.next();
                            myHelper = (CodeGeneratorHelper) this
                                    ._getHelper(source.getContainer());

                            IOPort sink;
                            Iterator<IOPort> sinkPorts = source
                                    .deepInsidePortList().iterator();
                            while (sinkPorts.hasNext()) {
                                sink = sinkPorts.next();
                                sinkReference = super.getReference(
                                        (TypedIOPort) sink, channelOffset,
                                        false, true, myHelper);
                                srcReference = super.getReference(
                                        (TypedIOPort) source, channelOffset,
                                        false, true, myHelper);
                                args.add(sinkReference);
                                args.add(srcReference);
                                actorTransferCode.append(_generateBlockCode(
                                        "updatePort", args));
                            }

                        }
                        code.append(_eol + actorTransferCode.toString() + _eol);
                        code.append(_getActorName(actor) + "_EmbeddedActor();"
                                + _eol);
                        //code.append("//jni actor"+_eol);
                        //code.append("//transfer outputs out"+_eol);
                        code.append("}" + _eol);

                    } else {
                        code.append(methodSignature);
                        code.append(_eol + "//not jni" + _eol);
                        // System.out.println("not jni actor name is"+actor.getFullName());
                        code.append(actorHelper.generateFireFunctionCode2());
                        code.append("}" + _eol);
                    }
                } else {
                    //do nothing
                }

            } else {
                code.append(methodSignature);
                code.append(_eol + "//in final else" + _eol);
                String temp = actorHelper.generateFireFunctionCode2();
                if (temp.length() == 0) {
                    code.append(actorHelper.generateFireCode());
                } else {
                    code.append(temp);
                }
                code.append("}" + _eol);
            }

            //code.append("}" + _eol);

        }
        return code.toString();
    }

    private boolean _isTopDirectorFSM() {
        boolean returnValue = false;

        Director director = ((TypedCompositeActor) _director.getContainer())
                .getExecutiveDirector();

        if (director != null
                && (director instanceof ptolemy.domains.fsm.kernel.FSMDirector)) {
            returnValue = true;
        }

        return returnValue;

    }

    int _getThenIncrementCurrentSharedMemoryAddress(TypedIOPort port)
            throws IllegalActionException {
        String type = targetType(port.getType());
        int portWidth = port.getWidth();

        int offset = 0;
        if (type.equals("int") || type.equals("long") || type.equals("float")
                || type.equals("void *")) {
            offset = 4 * portWidth;
        } else if (type.equals("long long") || type.equals("double")) {
            offset = 8 * portWidth;
        } else if (type.equals("char")) {
            offset = 1 * portWidth;
        } else if (type.equals("short")) {
            offset = 2 * portWidth;
        } else {
            offset = 8 * portWidth;
        }
        // Make sure memory address is aligned to offset
        int currentSharedMemoryAddress = (nextSharedMemoryAddress + offset - 1)
                / offset * offset;
        nextSharedMemoryAddress = currentSharedMemoryAddress + offset;
        if (nextSharedMemoryAddress >= 0x40000000) {
            throw new IllegalActionException("out of shared data space on PRET");
        }
        return currentSharedMemoryAddress;
    }

    private String _typeConversion(TypedIOPort source, TypedIOPort sink) {
        String sourceType;
        String sinkType;
        sourceType = targetType(source.getType());
        sinkType = targetType(sink.getType());
        if (sourceType.equals(sinkType)) {
            return "";
        } else {
            char tc = sourceType.charAt(0);
            sourceType = Character.toUpperCase(tc) + sourceType.substring(1);
            tc = sinkType.charAt(0);
            sinkType = Character.toUpperCase(tc) + sinkType.substring(1);
            sinkType = sinkType.replace(tc, Character.toUpperCase(tc));
            return sourceType + "to" + sinkType;
        }
    }

    static private int nextSharedMemoryAddress = 0x3F800000;
    private int threadID = 0;

}
