/* An actor that outputs strings read from a text file or URL.

 @Copyright (c) 2002-2007 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.apps.apes;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Source;
import ptolemy.actor.lib.io.LineReader;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// FormattedLineReader

/**
 <p>This actor reads a file or URL, and outputs the contents of each line at a time.
  The file or URL is specified using any form acceptable
 to FileParameter. Before an end of file is reached, the <i>endOfFile</i>
 output produces <i>false</i>.  In the iteration where the last line
 of the file is read and produced on the output ports (the eof iteration), this actor
 produces <i>true</i> on the <i>endOfFile</i> port. 
 The behavior of the actor in that iteration and in subsequent iterations is defined by the value
 of the OnEOF attribute, as follows. 
</p>
 <p> 
 If OnEOF is set to "Stop", then the behavior is the same as that of the LineReader 
 actor: In the eof iteration the postfire() returns false, then in subsequent iterations
 prefire() and postfire() will both return false, <i>output</i>
 will produce the string "EOF", and <i>endOfFile</i> will produce <i>true</i>. 
 In some domains (such as SDF), returning false in postfire()
 causes the model to cease executing.
 In other domains (such as DE), this causes the director to avoid
 further firings of this actor.  So usually, the actor will not be
 invoked again after the end of file is reached.
 </p>
 <p>
 If OnEOF is set to "Repeat", then the values produced in the outputs are repeated from the 
 beginning.
</p>
 <p>
 If OnEOF is set to "Hold", then the outputs in the iterations following the eof iteration
 are the same as in the eof iteration (including the <i>true</i> on the <i>endOfFile</i> port).
 beginning.
</p> 
 <p>
 This actor reads each line from the file or URL and converts the string tokens
 according to the types of the actor's data output ports.  
 
 This actor has one output port for each format specifier in the format string, with matching types,
 where the top output port corresponds to the leftmost format specifier.
 The format string is applied to each line. A separator attribute indicates how the line
 is to be split into tokens. The default separator is the space. 
 Format conversion is applied to input tokens until either the format 
 string is finished, or there are no tokens in the input. An exception is thrown if conversion fails.
 If the number of tokens in a line is smaller than the number of format specifiers,
 no tokens are placed in the corresponding outputs.
 </p> 
 <p>
 The buffer size attribute specifies the number of lines that are read and converted when the model
 is loaded or when an attribute is changed. If the header attribute is on, then the first line is 
 regarded as a table header. Each output port receives as name the string value of its corresponding 
 header token. 
 
 This actor reads ahead in the file so that it can produce an output
 <i>true</i> on <i>endOfFile</i> in the same iteration where it outputs
 the last line.  It reads the first line in preinitialize(), and
 subsequently reads a new line in each invocation of postfire().  The
 line read is produced on the <i>output</i> in the next iteration
 after it is read.</p>
 <p>
 This actor can skip some lines at the beginning of the file or URL, with
 the number specified by the <i>numberOfLinesToSkip</i> parameter. The
 default value of this parameter is 0.</p>
 <p>
 If you need to reset this line reader to start again at the beginning
 of the file, the way to do this is to call initialize() during the run
 of the model.  This can be done, for example, using a modal model
 with a transition where reset is enabled.</p>

 @author  Stefan Resmerita

 */
public class FormattedLineReader extends LineReader {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FormattedLineReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
 

        super(container, name);
        
        // remove the output port, because it cannot be renamed by the user
        this.output.setContainer(null);

        bufferSize = new Parameter(this, "bufferSize");
        bufferSize.setExpression("100000");
        bufferSize.setTypeEquals(BaseType.LONG);

        onEOF = new StringParameter(this, "onEOF");
        onEOF.setExpression("");
        onEOF.setTypeEquals(BaseType.STRING);
        
        headerLine = new Parameter(this, "headerLine");
        headerLine.setExpression("true");
        headerLine.setTypeEquals(BaseType.BOOLEAN);

        separator = new StringParameter(this, "separator");
        separator.setExpression("");
        separator.setTypeEquals(BaseType.STRING);
        
     }

    ///////////////////////////////////////////////////////////////////
    ////                      parameters                           ////

    public Parameter bufferSize;
    public Parameter headerLine;
    public StringParameter onEOF;
    public StringParameter separator;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Output the data for the current iteration
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }

            for (int i=0;i<portArray.size();i++){
                if (dataMatrix.get(i).get(currentIteration) != null){
                    Token tk = dataMatrix.get(i).get(currentIteration);
                    portArray.get(i).broadcast(tk);
                }
            }
    }

    /** Rewind to first data line
     */
    public void initialize() throws IllegalActionException {
        currentIteration = 0;
        //eofAction = ((StringToken)onEOF.getToken()).toString();
        eofAction = onEOF.stringValue();
    }

    /** Read the next line from the file. If there is no next line,
     *  return false.  Otherwise, return whatever the superclass returns.
     *  @exception IllegalActionException If there is a problem reading
     *   the file.
     */
    public boolean postfire() throws IllegalActionException {
        
        currentIteration++;
        
        if (currentIteration >= dataSize){
            currentIteration = dataSize;
            output.broadcast(BooleanToken.TRUE);
            if (eofAction.equalsIgnoreCase("stop")){
                return false;
            }             
        } 
        else{
            output.broadcast(BooleanToken.FALSE);           
        }
        return true;
    }

    /** The action depends on the value of the onEOF parameter
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        
        if (currentIteration >= dataSize) {
            if (eofAction.equalsIgnoreCase("repeat")){
                currentIteration = 0;
            }else if (eofAction.equalsIgnoreCase("hold")){
                currentIteration = dataSize - 1;
            }else if (eofAction.equalsIgnoreCase("stop")){
                return false;
            } 
        }
        return true;
    }

    /** Open the file or URL, skip the number of lines specified by the
     *  <i>numberOfLinesToSkip</i> parameter, and read the first line to
     *  be sent out in the fire() method.
     *  This is done in preinitialize() so
     *  that derived classes can extract information from the file
     *  that affects information used in type resolution or scheduling.
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened, or if the lines to be skipped and the first line to be
     *   sent out in the fire() method cannot be read.
     *  
     */
    public void preinitialize() throws IllegalActionException {
        String currentLine, delimiter;
        boolean header = false;
        Vector<IOPort> orderedPorts;
        
        super.preinitialize(); // this opens the file and reads the first line
        currentLine = _currentLine;
        if (((BooleanToken)headerLine.getToken())!= null){
            header = ((BooleanToken)headerLine.getToken()).booleanValue();
        }
        
//        String header = ((StringToken)headerLine.getToken()).toString();
        List dataPorts = this.outputPortList();
        dataPorts.remove(endOfFile);
        orderedPorts = new Vector<IOPort>();
//        String delimiter = ((StringToken)separator.getToken()).toString();
        delimiter = null;
        if (((StringToken)separator.getToken()) != null){
            delimiter = separator.stringValue();
        }
        if (header){
            StringTokenizer st;
            if ((delimiter != null) && (delimiter.length() > 0)){
                st = new StringTokenizer(currentLine, delimiter);
            }else{
                st = new StringTokenizer(currentLine);
            }
            while (st.hasMoreTokens()){
                String dataName = st.nextToken();               
                boolean found = false;
                for (int j=0;j<dataPorts.size();j++){
                    if (((IOPort)dataPorts.get(j)).getName().equalsIgnoreCase(dataName)){
                        orderedPorts.add((IOPort)dataPorts.get(j));
                        found = true;
                        break;
                    }
                }
                if (!found){
                    orderedPorts.add(null);
                }
            }
            
            try {
                currentLine = _reader.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else {
            for (int j=0;j<dataPorts.size();j++){
                     orderedPorts.add((IOPort)dataPorts.get(j));
            }
        }
        
        portArray = new Vector<IOPort>();
        for (int i=0; i<orderedPorts.size();i++){
            if (orderedPorts.get(i) != null){
                portArray.add(orderedPorts.get(i));
            }
        }
        dataMatrix = new Vector<Vector<Token>>(portArray.size());
        for (int i=0;i<portArray.size();i++){
            dataMatrix.add(new Vector<Token>());
        }
        dataSize = 0;
        while (currentLine != null){
            StringTokenizer st;
            if ((delimiter != null) && (delimiter.length() > 0)){
                st = new StringTokenizer(currentLine, delimiter);
            }else{
                st = new StringTokenizer(currentLine);
            }
            int tokenNr = 0;
            int portIndex = 0;
            while (st.hasMoreTokens()){
                String dataValue = st.nextToken();
                if (orderedPorts.get(tokenNr) != null){
                    Token tok = null;
                    if(((TypedIOPort)portArray.get(portIndex)).getType().equals(BaseType.DOUBLE)){
                        tok = new DoubleToken(Double.parseDouble(dataValue));
                    }
                    if(((TypedIOPort)portArray.get(portIndex)).getType().equals(BaseType.INT)){
                        tok = new IntToken(Integer.parseInt(dataValue));
                    }
                    if(((TypedIOPort)portArray.get(portIndex)).getType().equals(BaseType.BOOLEAN)){
                        tok = new BooleanToken(Boolean.parseBoolean(dataValue));
                    }
                    dataMatrix.get(portIndex).add(tok);
                    portIndex++;
                }
                tokenNr++;
            }
            // If in this row there are less columns than ports 
            if (portIndex < portArray.size()){
                for (int j=portIndex;j<portArray.size();j++){
                    dataMatrix.get(j).add(null);
                }
            }
            dataSize++;
            try {
                currentLine = _reader.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }          
        }
        try {
            _reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("data size: " + String.valueOf(dataSize));
    }
protected Vector<IOPort> portArray;
protected Vector<Vector<Token>> dataMatrix;

//TODO: These should be long, but Vector does not allow a long index.
//Perhaps we should transform vectors to arrays?
protected int dataSize, currentIteration;
protected String eofAction;
}
