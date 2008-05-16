/* Code generator for the Chaco graph partitioner.

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

////ChacoCodeGenerator

/** Base class for Chaco code generator.
 *
 *  @author Jia Zou, Isaac Liu, Man-Kit Leung
 *  @version $Id$
 *  @since Ptolemy II 7.0
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (mankit)
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
        action.addChoice("CLEAR");
        action.setExpression("GENERATE");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    StringParameter action;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void transformGraph() throws KernelException {

        if (!_isClearMode()) {
            _readChacoOutputFile();
        }
        
        // Traverse through the graph and annotate each port whether
        // it is a _isMpiBuffer (send/receive)
        _annotateMpiPorts();

        // Repaint the GUI.
        getContainer().requestChange(new ChangeRequest(this,
        "Repaint the GUI.") {
            protected void _execute() throws Exception {}
        });        
    }

    public int generateCode(StringBuffer code) throws KernelException {

        if (action.getExpression().equals("CLEAR")) {
            transformGraph();
            return 0;
        }

        if (action.getExpression().equals("TRANSFORM")) {
            if (_generated == false) {
                throw new IllegalActionException(this, (Throwable) null,
                "GENERATE first before TRANSFORM");
            }
            transformGraph();
            return 0;
        } 

        _reset();

        _codeFileName = _writeCode(code);

        StringBuffer codeBuffer = new StringBuffer();

        // Traversing through the actor list to see all actors
        CompositeEntity compositeActor = (CompositeEntity) getContainer();
        for (Actor actor : (List<Actor>) compositeActor.deepEntityList()) {
            if (actor instanceof Actor) {
                _numVertices++;
                _HashActorKey.put(actor, _numVertices);
                _HashNumberKey.put(_numVertices, actor);
            }
        }
        code.append(_numVertices + " ");

        // Traverse through the actors again to get all edges
        for (Actor actor : (List<Actor>) compositeActor.deepEntityList()) {
            int actorId = (Integer) _HashActorKey.get(actor);
            codeBuffer.append(actorId + " ");

            // Get vertex weights from the model
            codeBuffer.append(_getVertexWeight(actor) + " ");

            for (TypedIOPort inputPort : (List<TypedIOPort>) actor.inputPortList()) {

                for (TypedIOPort sourcePort : (List<TypedIOPort>) inputPort.sourcePortList()) {
                    Actor sourceActor = (Actor) sourcePort.getContainer();
                    int outInt = (Integer) _HashActorKey.get(sourceActor);
                    codeBuffer.append(outInt + " ");

                    // Add the edge weight from the model by traversing through
                    // the relations and ports connected to these relations
                    for (Relation relation : (List<Relation>) inputPort.linkedRelationList()) {
                        List portList = relation.linkedPortList(inputPort);
                        Iterator portIt = (Iterator) portList.listIterator();

                        boolean foundFlag = false;
                        while (foundFlag == false && portIt.hasNext()) {
                            TypedIOPort sinkPort = (TypedIOPort) portIt
                            .next();

                            if (!sinkPort.isOpaque()) {
                                // go into where this input port is connected to.
                                if (sinkPort.isOutput()) {
                                    portList.addAll(sinkPort
                                            .deepInsidePortList());
                                } else if (sinkPort.isInput()) {
                                    portList.addAll(sinkPort
                                            .sourcePortList());
                                }
                                portList.remove(sinkPort);
                                portIt = (Iterator) portList.listIterator();

                            } else if (sinkPort.equals(sourcePort)) {
                                codeBuffer.append(_getEdgeWeight(relation) + " ");
                                foundFlag = true;
                            }
                        }
                    }
                }
            }

            for (TypedIOPort outputPort : (List<TypedIOPort>) actor.outputPortList()) {

                for (TypedIOPort sinkPort : (List<TypedIOPort>) outputPort.sinkPortList()) {

                    Actor tempActor = (Actor) sinkPort.getContainer();
                    int inInt = (Integer) _HashActorKey.get(tempActor);
                    codeBuffer.append(inInt + " ");

                    // Add the edge weight from the model by traversing through
                    // the relations and ports connected to these relations
                    for (Relation relation : (List<Relation>) outputPort.linkedRelationList()) {

                        List portList = relation.linkedPortList(outputPort);
                        Iterator portIt = (Iterator) portList.listIterator();
                        boolean foundFlag = false;
                        while (foundFlag == false && portIt.hasNext()) {
                            TypedIOPort connInputPort = (TypedIOPort) portIt
                            .next();
                            if (!connInputPort.isOpaque()) {
                                // go into where this input port is connected to.
                                if (connInputPort.isInput()) {
                                    //portList.addAll(connInputPort.insideSinkPortList());
                                    portList.addAll(connInputPort
                                            .deepInsidePortList());
                                } else if (connInputPort.isOutput()) {
                                    portList.addAll(connInputPort
                                            .sinkPortList());
                                }
                                portList.remove(connInputPort);
                                portIt = (Iterator) portList.listIterator();
                            } else {
                                if (connInputPort.equals(sinkPort)) {
                                    codeBuffer.append(_getEdgeWeight(relation) + " ");
                                    foundFlag = true;
                                }
                            }
                        }
                    }
                    _numEdges++;
                }
            }
            codeBuffer.append(_eol);
        }
        code.append(_numEdges + " 111" + _eol);

        code.append(codeBuffer);

        _writeChacoInputFile(code.toString());

        _generated = true;

        return 0;
    }

    private void _reset() {
        _HashActorKey.clear();
        _HashNumberKey.clear();
        _colorMap.clear();
        _rankNumbers.clear();
        _numVertices = 0;
        _numEdges = 0;
    }

    private String _getVertexWeight(Actor actor) {
        Parameter vertexParam = (Parameter) 
        ((NamedObj) actor).getAttribute("_vertexWeight");

        if (vertexParam == null) {
            return "1";
        } else {
            return vertexParam.getExpression();
        }
    }

    private static String _getEdgeWeight(Relation relation) {
        Parameter edgeParam = (Parameter) 
        relation.getAttribute("_edgeWeight");

        if (edgeParam == null) {
            return "1";
        } else {
            return edgeParam.getExpression();
        }
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

        return (String) (_sanitizedModelName + ".graph");
    }

    protected void _writeChacoInputFile(String code)
    throws IllegalActionException {
        try {
            // Create file 
            FileWriter fstream = new FileWriter(_codeFileName);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(code);
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
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
                Actor actor = (Actor) _HashNumberKey.get(actorNum);

                Parameter parameter = _getPartitionParameter(actor);
                parameter.setExpression(rankString);

                actorNum++;

                _rankNumbers.add(rankString);                
            }

            // dispose all the resources after using them.
            fis.close();
            bis.close();
            dis.close();


        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

    }

    private void _highlightPartitions() throws IllegalActionException {
        if (!_isClearMode()) {
            // Generate and map the distinct colors to partitions.
            Iterator rankNumbers = _rankNumbers.iterator();
            for (String color : getDistinctColors(_rankNumbers.size() - 1)) {
                //System.out.println("color: " + color + ", rank: " + rankNumbers.next());
                _colorMap.put(rankNumbers.next(), color);
            }
        }
        
        // Insert the highlight color attributes.
        CompositeEntity compositeActor = (CompositeEntity) getContainer();
        for (Actor actor : (List<Actor>) compositeActor.deepEntityList()) {

            if (_isClearMode()) {
                _removeAttribute((NamedObj) actor, "_partition");
                _removeAttribute((NamedObj) actor, "_highlightColor");

            } else {
                Parameter actorPartition = _getPartitionParameter(actor);
                String color = (String) _colorMap.get(actorPartition.getExpression());
                
                ColorAttribute colorAttribute = _getHighlightAttribute((NamedObj) actor);
                colorAttribute.setExpression(color);
            }
        }
    }

    private ColorAttribute _getHighlightAttribute(NamedObj actor)
    throws IllegalActionException {
        ColorAttribute attribute = (ColorAttribute) actor
        .getAttribute("_highlightColor");

        try {
            if (_isClearMode() && attribute != null) {
                attribute.setContainer(null);
                return null;
            }

            return (attribute != null) ? attribute
                    : new ColorAttribute(actor, "_highlightColor");
        } catch (NameDuplicationException e) {
            assert false;
        }
        return null;
    }

    private boolean _isClearMode() {
        return action.getExpression().equals("CLEAR");
    }

    /**
     * Find the first integer-valued cube that is greater than or equal to val.
     */
    private static final int _approxCubeRoot(int val) {
        int index = 0;
        int cube  = 0;

        while (val >= cube) {
            index++;
            cube = index * index * index;
        }

        return (index);
    }

    /**
     * Generate an array of distinct colors.
     *
     * @param  numColors is the number of distinct colors.
     * @return an array size (numColors + 1) filled with distinct colors.
     *         The +1 is there in case the clusters range from 0 to numColors
     *         or 1 to (numColors + 1).
     */
    public static final String[] getDistinctColors(int numColors) {

        //// Need to pick numColors distinct colors.
        String[] colors            = new String[numColors + 1];
        int       numDiscreteValues = _approxCubeRoot(numColors);
        float[]   colorVals         = new float[numDiscreteValues];
        int       r_index;
        int       g_index;
        int       b_index;

        //// 1. Generate a table of discrete values for rgb.
        for (int i = 0; i < numDiscreteValues; i++) {
            colorVals[i] = 1.0f - ((float) i) / ((float) numDiscreteValues - 1);

            //// 1.1. Fix the values just in case.
            if (colorVals[i] < 0.0f) {
                colorVals[i] = 0.0f;
            } else if (colorVals[i] > 1.0f) {
                colorVals[i] = 1.0f;
            }
        }

        //// 2. Now generate the colors.
        r_index = 0;
        g_index = 0;
        b_index = 0;

        for (int i = 0; i < colors.length; i++) {
            colors[i] = new String("{" + colorVals[r_index] + ", " +
                    colorVals[g_index] + ", " + 
                    colorVals[b_index] + ", 1.0}");


            //// 2.1. Now go to the next color values.
            b_index++;
            if (b_index >= numDiscreteValues) {
                g_index++;
                if (g_index >= numDiscreteValues) {
                    r_index++;
                    r_index %= numDiscreteValues;
                }
                g_index %= numDiscreteValues;
            }
            b_index %= numDiscreteValues;
        }

        return (colors);
    } 

    private Parameter _getPartitionParameter(Actor actor)
    throws IllegalActionException {
        Parameter attribute = (Parameter) ((NamedObj) actor)
        .getAttribute("_partition");

        try {
            if (attribute == null) {
                attribute = new Parameter((NamedObj) actor, "_partition");
            }
        } catch (NameDuplicationException e) {
            assert false;
        }
        return attribute;
    }

    private Parameter _getNumConnectionsParameter()
    throws IllegalActionException {

        Director director = (Director) ((TypedCompositeActor) 
                getContainer()).getDirector();

        Parameter attribute = (Parameter) 
        director.getAttribute("_numberOfMpiConnections");

        try {
            if (attribute == null) {
                attribute = new Parameter(director, "_numberOfMpiConnections");
            }
        } catch (NameDuplicationException e) {
            assert false;
        }
        return attribute;
    }

    private void _annotateMpiPorts() throws IllegalActionException {
        int count = 1;
        int mpiBufferId = 0;
        int localBufferId = 0;

        _highlightPartitions();

        while (count <= _numVertices) {
            Actor actor = (Actor) _HashNumberKey.get(count);

            Parameter partitionAttribute = _getPartitionParameter(actor);

            for (TypedIOPort inputPort : (List<TypedIOPort>) actor.inputPortList()) {

                // Clear the _isMpiBuffer parameter if it already exists
                _removeAttribute(inputPort, "_mpiBuffer");
                _removeAttribute(inputPort, "_localBuffer");
                _removeAttribute(inputPort, "_showInfo");

                if (_isClearMode()) {
                    continue;
                }

                int channel = 0;
                for (TypedIOPort sourcePort : (List<TypedIOPort>) inputPort.sourcePortList()) {
                    Actor sourceActor = (Actor) sourcePort.getContainer();

                    Parameter attrTemp =  _getPartitionParameter(sourceActor);

                    if (!partitionAttribute.getExpression()
                            .equals(attrTemp.getExpression())) {

                        StringAttribute mpiBuffer = _getMpiAttribute(inputPort);

                        StringAttribute showInfo = _getShowInfoAttribute(inputPort);

                        String mpiBufferValue = mpiBuffer.getExpression();
                        if (mpiBufferValue.equals("")) {
                            mpiBufferValue = "receiver";
                        }
                        mpiBufferValue = mpiBufferValue.concat("_ch["
                                + channel + "]" + "id[" + mpiBufferId + "]");

                        mpiBuffer.setExpression(mpiBufferValue);
                        showInfo.setExpression(mpiBufferValue);
                        // Keep track of the number of MPI connections
                        mpiBufferId++;
                    } else {
                        // Annotate local buffer.
                        StringAttribute localBuffer = _getLocalAttribute(inputPort);
                        if (!_isClearMode()) {
                            String localBufferValue = localBuffer.getExpression();

                            localBufferValue = localBufferValue.concat("_ch["
                                    + channel + "]" + "id[" + localBufferId + "]");
                            localBuffer.setExpression(localBufferValue);
                        }

                        localBufferId++;
                    }
                    channel++;
                }
            }

            for (TypedIOPort outputPort : (List<TypedIOPort>) actor.outputPortList()) {

                // Clear the _isMpiBuffer parameter if it already exists
                _removeAttribute(outputPort, "_mpiBuffer");

                if (!_isClearMode()) {
                    continue;
                }
                
                int sinkIndex = 0;
                for (TypedIOPort sinkPort : (List<TypedIOPort>) outputPort.sinkPortList()) {

                    Actor sinkActor = (Actor) sinkPort.getContainer();
                    Parameter sinkPartitionAttribute =  _getPartitionParameter(sinkActor);

                    if (!partitionAttribute.getExpression().equals(
                            sinkPartitionAttribute.getExpression())) {

                        StringAttribute mpiAttribute = _getMpiAttribute(outputPort);
                        String mpiValue = mpiAttribute.getExpression();

                        if (mpiValue.equals("")) {
                            mpiValue = "sender";
                        }
                        mpiValue = mpiValue.concat("_ch[" + sinkIndex + "]");
                        mpiAttribute.setExpression(mpiValue);
                    }
                }
                sinkIndex++;
            }

            count++;
        }
        Parameter numConnections = _getNumConnectionsParameter();
        numConnections.setExpression(Integer.toString(mpiBufferId));
    }

    private StringAttribute _getLocalAttribute(TypedIOPort port)
    throws IllegalActionException {
        StringAttribute attribute = 
            (StringAttribute) port.getAttribute("_localBuffer");


        try {
            if (attribute == null) {
                attribute = new StringAttribute(port, "_localBuffer");
            }
        } catch (NameDuplicationException e) {
            assert false;
        }
        return attribute;
    }

    private StringAttribute _getMpiAttribute(TypedIOPort port)
    throws IllegalActionException {
        StringAttribute result = (StringAttribute) ((NamedObj) port)
        .getAttribute("_mpiBuffer");

        if (result == null) {
            try {
                result = new StringAttribute((NamedObj) port, "_mpiBuffer");
            } catch (NameDuplicationException e) {
                assert false;
            }
        }
        return result;
    }

    private StringAttribute _getShowInfoAttribute(TypedIOPort port)
    throws IllegalActionException {
        StringAttribute attribute = 
            (StringAttribute) port.getAttribute("_showInfo");


        try {
            if (attribute == null) {
                attribute = new StringAttribute(port, "_showInfo");
            }
        } catch (NameDuplicationException e) {
            assert false;
        }
        return attribute;
    }

    private void _removeAttribute(NamedObj namedObj, String attributeName) {
        Attribute attribute = namedObj.getAttribute(attributeName);
        if (attribute != null) {
            try {
                attribute.setContainer(null);
            } catch (KernelException e) {
                assert false;
            }
        }
    }

    private HashMap _colorMap = new HashMap();

    // These hashtables save the mapping between Actor and an Integer
    // value used for Chaco identification
    private HashMap _HashActorKey = new HashMap();

    private HashMap _HashNumberKey = new HashMap();

    private HashSet _rankNumbers = new HashSet();

    private int _numVertices = 0;

    private int _numEdges = 0;

    private boolean _generated = false;

}
