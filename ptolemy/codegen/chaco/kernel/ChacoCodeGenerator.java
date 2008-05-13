/* Code generator for the C language.

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
package ptolemy.codegen.chaco.kernel;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

////CodeGenerator

/** Base class for C code generator.
 *
 *  @author Jia Zou, Isaac Liu
 *  @version $Id$
 *  @since Ptolemy II 6.0
 *  @Pt.ProposedRating red (jiazou)
 *  @Pt.AcceptedRating red ()
 */

public class ChacoCodeGenerator extends CodeGenerator {

    /** Create a new instance of the C code generator.
     *  @param container The container.
     *  @param name The name of the C code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public ChacoCodeGenerator(NamedObj container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        generatorPackage.setExpression("ptolemy.codegen.chaco");
        
        action = new StringParameter(this, "action");
        action.addChoice("GENERATE");
        action.addChoice("TRANSFORM");
        action.setExpression("GENERATE");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    StringParameter action;
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public int transformGraph() throws KernelException {
        _readChacoOutputFile();
        return 0;
    }

    public int generateCode(StringBuffer code) throws KernelException {

        if (action.getExpression().equals("TRANSFORM")) {

            if (_generated == false)
                throw new IllegalActionException("GENERATE first before TRANSFORM");

            return transformGraph();
        }

        this.HashActorKey.clear();
        this.HashNumberKey.clear();

        int returnValue = -1;

        _codeFileName = _writeCode(code);

        StringBuffer codeBuffer = new StringBuffer();

        CompositeEntity model = (CompositeEntity) getContainer();

        // NOTE: The cast is safe because setContainer ensures
        // the container is an Actor.
        ptolemy.actor.Director director = ((Actor) model).getDirector();

        CompositeActor compositeActor =
            (CompositeActor) director.getContainer();

        List actorList = compositeActor.deepEntityList();

        Iterator actors = actorList.iterator();
        actors = actorList.iterator();

        actors = actorList.iterator();

        numVertices = 0;
        numEdges = 0;

        // Traversing through the actor list to see all actors
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof Actor) {
                numVertices++;
                HashActorKey.put(actor, numVertices);
                HashNumberKey.put(numVertices, actor);
            }
        }
        code.append(numVertices + " ");

        // Traverse through the actors again to get all edges
        actors = actorList.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            int thisInt;
            thisInt = (Integer) HashActorKey.get(actor);
            codeBuffer.append("{" + thisInt + "} ");
            
            // Get vertex weights from the model
            Parameter vertexParam = new Parameter();
            vertexParam = (Parameter)((NamedObj)actor).getAttribute("_vertexWeight");
            String vertexWeightString;
            if (vertexParam == null)
                vertexWeightString = "1.0";
            else
                vertexWeightString = vertexParam.getExpression();
            codeBuffer.append("[" + vertexWeightString + "] ");

            List inList =  actor.inputPortList();
            Iterator inputIt = (Iterator) inList.listIterator();

            while(inputIt.hasNext()) {
                TypedIOPort thisInput = (TypedIOPort) inputIt.next();

                // Iterator connOut = (Iterator) thisInput.deepConnectedOutPortList().iterator();
                Iterator connOut = (Iterator) thisInput.sourcePortList().iterator();
                //Iterator connOut = (Iterator)thisInput.deepInsidePortList().iterator();
                while (connOut.hasNext()) {

                    TypedIOPort tempOutput = (TypedIOPort) connOut.next();
                    Actor tempActor = (Actor) tempOutput.getContainer();
                    int outInt = (Integer) HashActorKey.get(tempActor);
                    codeBuffer.append(outInt + " ");
                    
                    String edgeWeightString;
                    // Add the edge weight from the model by traversing through
                    // the relations and ports connected to these relations
                    List relationList = thisInput.linkedRelationList();
                    Iterator relationIt = (Iterator)relationList.listIterator();
                    boolean foundFlag = false;
                    while (relationIt.hasNext()) {
                        Relation thisRelation = (Relation)relationIt.next();
                        List portList = thisRelation.linkedPortList(thisInput);
                        Iterator portIt = (Iterator)portList.listIterator();
                        while (foundFlag == false && portIt.hasNext()) {
                            TypedIOPort connOutputPort = (TypedIOPort)portIt.next();
                            
                            if (!connOutputPort.isOpaque()) {
                                // go into where this input port is connected to.
                                if (connOutputPort.isOutput()) {
                                    portList.addAll(connOutputPort.deepInsidePortList());
                                } else if (connOutputPort.isInput()){
                                    portList.addAll(connOutputPort.sourcePortList());
                                }
                                portList.remove(connOutputPort);
                                portIt = (Iterator)portList.listIterator();
                            }
                            else {

                                if (connOutputPort.equals(tempOutput)) {
                                    Parameter edgeParam = new Parameter();
                                    edgeParam = (Parameter)thisRelation.getAttribute("_edgeWeight");
                                    if (edgeParam == null) {
                                        edgeWeightString = "1.0";
                                    } else {
                                        edgeWeightString = edgeParam.getExpression();
                                    }
                                    codeBuffer.append("(" + edgeWeightString + ") ");
                                    foundFlag = true;
                                }
                            }
                        }
                    }
                }
            }

            List outList = actor.outputPortList();
            Iterator outputIt = (Iterator) outList.listIterator();

            while(outputIt.hasNext()) {
                TypedIOPort thisOutput = (TypedIOPort) outputIt.next();
                //Iterator connIn = (Iterator) thisOutput.deepConnectedInPortList().iterator();
                Iterator connIn = (Iterator) thisOutput.sinkPortList().iterator();
                while ( connIn.hasNext()) {

                    TypedIOPort tempInput = (TypedIOPort) connIn.next();
                    Actor tempActor = (Actor) tempInput.getContainer();
                    int inInt = (Integer) HashActorKey.get(tempActor);
                    codeBuffer.append(inInt + " ");

                    // Add the edge weight from the model by traversing through
                    // the relations and ports connected to these relations
                    String edgeWeightString;
                    List relationList = thisOutput.linkedRelationList();
                    Iterator relationIt = (Iterator)relationList.listIterator();
                    while (relationIt.hasNext()) {
                        Relation thisRelation = (Relation)relationIt.next();
                        List portList = thisRelation.linkedPortList(thisOutput);
                        Iterator portIt = (Iterator)portList.listIterator();
                        boolean foundFlag = false;
                        while (foundFlag == false && portIt.hasNext()) {
                            TypedIOPort connInputPort = (TypedIOPort)portIt.next();
                            if (!connInputPort.isOpaque()) {
                                // go into where this input port is connected to.
                                if (connInputPort.isInput()) {
                                    //portList.addAll(connInputPort.insideSinkPortList());
                                    portList.addAll(connInputPort.deepInsidePortList());
                                } else if (connInputPort.isOutput()){
                                    portList.addAll(connInputPort.sinkPortList());
                                }
                                portList.remove(connInputPort);
                                portIt = (Iterator)portList.listIterator();
                            }
                            else {
                                if (connInputPort.equals(tempInput)) {
                                    Parameter edgeParam = new Parameter();
                                    edgeParam = (Parameter)thisRelation.getAttribute("_edgeWeight");
                                    if (edgeParam == null) {
                                        edgeWeightString = "1.0";
                                    } else {
                                        edgeWeightString = edgeParam.getExpression();
                                    }

                                    codeBuffer.append("(" + edgeWeightString + ") ");     
                                    foundFlag = true;
                                }
                            }
                        }
                    }

                    numEdges++;
                }
            }
            codeBuffer.append(_eol);
        }
        code.append(numEdges + " {1} [1] (1)" + _eol);

        code.append(codeBuffer);

        _writeChacoInputFile(code.toString());
        
        _generated = true;

        return returnValue;
    }

    /** Write the code with the sanitized model name postfixed with "_tb"
     *  if the current generate file is the testbench module; Otherwise,
     *  write code with the sanitized model name (as usual). 
     *  @param code The StringBuffer containing the code.
     *  @return The name of the file that was written.
     *  @exception IllegalActionException  If the super class throws it.
     */
    protected String _writeCode(StringBuffer code)
    throws IllegalActionException {
        _sanitizedModelName = CodeGeneratorHelper.generateName(_model);

        return (String)(_sanitizedModelName + ".graph");
    }

    protected void _writeChacoInputFile(String code) throws IllegalActionException {
        try {
            // Create file 
            FileWriter fstream = new FileWriter(_codeFileName);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(code);
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    protected void _readChacoOutputFile() throws IllegalActionException {
        File file = new File(_sanitizedModelName + ".out");
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;
        
        try {
            fis = new FileInputStream(file);

            // Here BufferedInputStream is added for fast reading.
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
            
            int actorNum = 1;
            // dis.available() returns 0 if the file does not have more lines.
            while (dis.available() != 0) {

                // this statement reads the line from the file and print it to
                // the console.
                String rankString = dis.readLine();
                Actor actor = (Actor)HashNumberKey.get(actorNum);
                
                Parameter parameter = _getPartitionParameter(actor);
                parameter.setExpression(rankString);
                
                actorNum++;

            }

            // dispose all the resources after using them.
            fis.close();
            bis.close();
            dis.close();

            // Traverse through the graph and annotate each port whether
            // it is a _isMpiBuffer (send/receive)
            _annotateMpiPorts();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
    }
    
    private StringAttribute _getMpiAttribute(TypedIOPort port)
            throws IllegalActionException {
        StringAttribute result = (StringAttribute)((NamedObj)port).getAttribute("_isMpiBuffer");

        if(result == null) {
            try {
                result = new StringAttribute ((NamedObj)port, "_isMpiBuffer");
            } catch (NameDuplicationException e) {
                assert false;
            }
        }
        return result;
    }

    private Parameter _getPartitionParameter(Actor actor)
            throws IllegalActionException {
        Parameter result = (Parameter) ((NamedObj) actor).getAttribute("_partition");

        if (result == null) {
            try {
                result = new Parameter((NamedObj)actor, "_partition");
            } catch (NameDuplicationException e) {
                assert false;
            }
        }
        return result;
    }
    
    private void _annotateMpiPorts() {
        int count = 1;
        
        try {
            //while (count <= HashNumberKey.size()) {
            while (count <= numVertices) {
                Actor actor = (Actor) HashNumberKey.get(count);
                
                Parameter attrActor = new Parameter();
                attrActor = (Parameter)((NamedObj)actor).getAttribute("_partition");
                assert attrActor != null;
                
                List inputList = actor.inputPortList();
                Iterator inputIt = (Iterator) inputList.listIterator();

                while(inputIt.hasNext()) {
                    TypedIOPort thisInput = (TypedIOPort) inputIt.next();
                    
                    // Clear the _isMpiBuffer parameter if it already exists
                    StringAttribute clearPortParam = _getMpiAttribute(thisInput);
                    clearPortParam.setExpression("");

                    //Iterator connOut = (Iterator) thisInput.deepConnectedOutPortList().iterator();
                    Iterator connOut = (Iterator) thisInput.sourcePortList().iterator();
                    int sourceIndex = 0;
                    while (connOut.hasNext()) {
                        TypedIOPort tempOutput = (TypedIOPort) connOut.next();
                        Actor tempActor = (Actor) tempOutput.getContainer();

                        Parameter attrTemp = new Parameter();
                        attrTemp = (Parameter)((NamedObj)tempActor).getAttribute("_partition");
                        assert attrTemp != null;
                        if (!attrActor.getExpression().equals(attrTemp.getExpression())) {
                           StringAttribute portAttr = _getMpiAttribute(thisInput);
                           //portParam.setExpression("receiver"); 
                           String tempString = portAttr.getExpression();
                           if (tempString.equals("")) {
                               tempString = "receiver";
                           }
                           tempString = tempString.concat("_" + Integer.toString(sourceIndex));
                           portAttr.setExpression(tempString);
                        }
                        sourceIndex++;
                    }
                }
                List outputList =  actor.outputPortList();
                Iterator outputIt = (Iterator) outputList.listIterator();

                while(outputIt.hasNext()) {
                    TypedIOPort thisOutput = (TypedIOPort) outputIt.next();

                    // Clear the _isMpiBuffer parameter if it already exists
                    StringAttribute clearPortParam = _getMpiAttribute(thisOutput);
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
                        if (!attrActor.getExpression().equals(attrTemp.getExpression())) {                      
                           StringAttribute portAttr = _getMpiAttribute(thisOutput);
                           String tempString = portAttr.getExpression();
                           if (tempString.equals("")) {
                               tempString = "sender";
                           }
                           tempString = tempString.concat("_" + Integer.toString(sinkIndex));
                           portAttr.setExpression(tempString);
                           //portParam.setExpression("sender");
                        }
                        sinkIndex++;
                    }
                }
                count++;
            } 
        } catch (IllegalActionException e) {
            System.err.println("Error: " + e.getMessage());
        }

    }

    // These hashtables save the mapping between Actor and an Integer
    // value used for Chaco identification
    private Hashtable HashActorKey= new Hashtable();
    private Hashtable HashNumberKey = new Hashtable();
    private Integer numVertices = new Integer(0);       
    private Integer numEdges = new Integer(0);
    private boolean _generated = false;
}
