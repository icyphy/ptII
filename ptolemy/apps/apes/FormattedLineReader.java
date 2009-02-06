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
 <p>This actor reads a file or URL, and outputs formatted data from the file. 
 The file or URL is specified using any form acceptable to FileParameter. The standard usage
 of this actor is reading data samples represented in textual tabular format, where a column contains 
 all samples of a signal and a line contains the values of all signals at the same sampling time.  
 </p>
 <p> 
 A correspondence is established between output ports of the actor and columns of the input file.
 This can be implicit, where columns are taken from left to right and ports are considered in the 
 order in which they have been added by the user (i.e., the order in which they are given by the 
 outputPortList() of the actor), such that the leftmost column is associated to the port added first.
 An explicit association is obtained by using port names in the actor and signal names in the file, 
 given in a header line with a name for each column. In this case, data in a named column will be sent to 
 the output port with the same name (case insensitive). The order of columns does not matter.
 Data in a column with no associated output port is ignored. An output port with no corresponding 
 column will issue no token.
 </p>
 <p> 
 In each iteration, tokens from one line of the file are sent to the corresponding outputs.
 Before the last line in the file is reached, the <i>endOfFile</i>  output produces <i>false</i>.  
 In the iteration where the last line of the file is produced on the output ports (the eof iteration),
 this actor produces <i>true</i> on the <i>endOfFile</i> port. 
 The behavior of the actor in that iteration and in subsequent iterations is defined by the value
 of the OnEOF attribute, as follows. 
 </p>
 <p> 
 If OnEOF is set to "Stop", then the behavior is similar to that of the LineReader 
 actor: In the eof iteration the postfire() returns false, then in subsequent iterations
 prefire() and postfire() will both return false, no token is send through data output ports
 and <i>endOfFile</i> will produce <i>true</i>. This is the implicit behavior, applied when this 
 parameter is not set. 
 </p>
 <p>
 If OnEOF is set to "Repeat", then the values produced in the outputs are repeated from the 
 beginning.
</p>
 <p>
 If OnEOF is set to "Hold", then the outputs in the iterations following the eof iteration
 are the same as in the eof iteration (including the <i>true</i> on the <i>endOfFile</i> port).
</p> 
 <p>
 This actor can skip some lines at the beginning of the file or URL, and can be reset during 
 the run of the model, in the same way as the LineReader actor.
 </p>
 If the <i>Header Line</i> parameter is on, then the explicit correspondence output port - input column 
 is used. This assumes that the file has a header line, with a name for each column.
 Otherwise, the implicit correspondence applies. 
</p> 
 <p>
The <i>Buffered</i> parameter specifies if output tokens are to be buffered. When this is on,
all the contents of the file is parsed at the beginning, and all output tokens are created and
stored in an internal matrix. When the value of the parameter is false, one input line is read 
and parsed at each iteration.
</p> 
 <p>
The <i>Delimiter</i> parameter contains all delimiters used to split up input lines. If this is empty,
white space is considered as column delimiter by default.
</p> 
<p>
For now, input cannot be repeated if the buffered parameter is off. The current behavior is the same as 
in the "Stop" case. 
TODO: The repeat behavior can be easily achieved if access to _openAndReadFirstLine of the base class
is provided. 
</p> 


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

        buffered = new Parameter(this, "Buffered");
        buffered.setExpression("true");
        buffered.setTypeEquals(BaseType.BOOLEAN);

        onEOF = new StringParameter(this, "onEOF");
        onEOF.setExpression("");
        onEOF.setTypeEquals(BaseType.STRING);
        
        headerLine = new Parameter(this, "Header Line");
        headerLine.setExpression("true");
        headerLine.setTypeEquals(BaseType.BOOLEAN);

        separator = new StringParameter(this, "separator");
        separator.setExpression("");
        separator.setTypeEquals(BaseType.STRING);
        
     }

    ///////////////////////////////////////////////////////////////////
    ////                      parameters                           ////

    public Parameter buffered;
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
                Token tk = dataMatrix.get(i).get(lineIndex);
                if (tk != null){
                    portArray.get(i).broadcast(tk);
                }
            }
    }

    /** Rewind to first data line
     */
    public void initialize() throws IllegalActionException {
        lineIndex = 0;
        __reachedEOF = false;
        //eofAction = ((StringToken)onEOF.getToken()).toString();
        eofAction = onEOF.stringValue();
        
        if (!isBuffered){ //must close the file and read again the first line of data
            _resetReader();
        }
    }

    /** Read the next line from the file. If there is no next line,
     *  return false.  Otherwise, return whatever the superclass returns.
     *  @exception IllegalActionException If there is a problem reading
     *   the file.
     */
    public boolean postfire() throws IllegalActionException {
        
        if (isBuffered){
            lineIndex++;
            if (lineIndex >= dataSize){
                    if (eofAction.equalsIgnoreCase("repeat")){
                        lineIndex = 0;
                    }else if (eofAction.equalsIgnoreCase("hold")){
                        lineIndex = dataSize - 1;
                    }
                __reachedEOF = true;
                endOfFile.broadcast(BooleanToken.TRUE);
                if (eofAction.equalsIgnoreCase("stop")){
                    return false;
                }             
            } 
            else{
                __reachedEOF = false;
                endOfFile.broadcast(BooleanToken.FALSE);           
            }
        }
        else{
            lineIndex = 0;
            if (super.postfire()){ //the next line from the file is read here, no eof
                _placeCurrentLineTokens();
            }
            else{
                if (_currentLine.equals("EOF")){
                    if (eofAction.equalsIgnoreCase("repeat")){
                        _resetReader();
                        return false; //Remove this after implementing resetReader
                    }else if (eofAction.equalsIgnoreCase("stop")){
                        return false;
                        }
                }
                else {
                    return false;
                }
            }
        }
    return true;        
    }

    private void _resetReader() {
        // TODO need access to _openAndReadFirstLine();
 /*       fileOrURL.close();
        _reader = null;
        _openAndReadFirstLine();
        if (isHeader){
            try {
                _currentLine = _reader.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        _placeCurrentLineTokens();
 */
        }

    /** The action depends on the value of the onEOF parameter
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        
        if (isBuffered){
            if (__reachedEOF && (eofAction.equalsIgnoreCase("stop"))) {
                    return false;
            }
        }
        else {
            if (eofAction.equalsIgnoreCase("stop")){
                return super.prefire();
            }
        }
        return true;
    }

    /** Open the file or URL, skip the number of lines specified by the
     *  <i>numberOfLinesToSkip</i> parameter, and read the first line.
     *  If needed, read all the file, create all tokens and store them up.
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened, or if the lines to be skipped and the first line to be
     *   sent out in the fire() method cannot be read.
     *  
     */
    public void preinitialize() throws IllegalActionException {
       
        super.preinitialize(); // this opens the file and reads the first line
        isHeader = false;
        if (((BooleanToken)headerLine.getToken())!= null){
            isHeader = ((BooleanToken)headerLine.getToken()).booleanValue();
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
        if (isHeader){
            StringTokenizer st;
            if ((delimiter != null) && (delimiter.length() > 0)){
                st = new StringTokenizer(_currentLine, delimiter);
            }else{
                st = new StringTokenizer(_currentLine);
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
                _currentLine = _reader.readLine();
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
        isBuffered = false;
        if (((BooleanToken)buffered.getToken())!= null){
            isBuffered = ((BooleanToken)buffered.getToken()).booleanValue();
        }


        dataMatrix = new Vector<Vector<Token>>(portArray.size());
        for (int i = 0; i < portArray.size(); i++) {
            dataMatrix.add(new Vector<Token>());
        }
        // Process the first line
        lineIndex = 0;
        _placeCurrentLineTokens();
        lineIndex++;

        if (isBuffered) {
            try {
                _currentLine = _reader.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            while (_currentLine != null) {
                 _placeCurrentLineTokens();
                lineIndex++;
                try {
                    _currentLine = _reader.readLine();
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
        dataSize = lineIndex;
        
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Array of all data output ports. It does not include the endOfFile port*/
    protected Vector<IOPort> portArray;

    /** Matrix with all tokens, in case the input is buffered*/
    protected Vector<Vector<Token>> dataMatrix;

//TODO: These should be long, but Vector does not allow a long index.
//Perhaps we should transform vectors to arrays?
    /** Number of lines stored*/
    protected int dataSize;
    
    /** The line index in the data matrix.This is equal to 0 if no buffering is used*/
    protected int lineIndex;
    
    /** Value of the OnEOF parameter*/
    protected String eofAction;
    
    /** Value of the Buffered parameter*/
    protected boolean isBuffered;
 
    /** Value of the Delimiter parameter*/
    protected String delimiter;
 
    /** Value of the HeaderLine parameter*/
    protected boolean isHeader;
    
    /** Indicate if EOF has been reached
     * This duplicates the private parameter _reachedEOF of the base class 
     * */
    protected boolean __reachedEOF;
     
    /** This array contains references to the output ports
     * One element in the array corresponds to each column in the input,
     * hence the size of the array is equal to the number of columns.
     * If a column has no corresponding port, then its array element is null.
     * */
    protected Vector<IOPort> orderedPorts;
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected Vector<Token> _parseCurrentLine(){
        
        Vector<Token> tokenArray = new Vector<Token>(portArray.size());
        StringTokenizer st;
        if ((delimiter != null) && (delimiter.length() > 0)){
            st = new StringTokenizer(_currentLine, delimiter);
        }else{
            st = new StringTokenizer(_currentLine);
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
                tokenArray.add(tok);
                portIndex++;
            }
            tokenNr++;
        }
        // If in this row there are less columns than ports 
            for (int j=portIndex;j<portArray.size();j++){
                tokenArray.add(null);
            }
        return tokenArray;
    }
    
    protected void _placeCurrentLineTokens(){
        Vector<Token> tokens = _parseCurrentLine();
        for (int i = 0; i < tokens.size(); i++) {
            dataMatrix.get(i).add(lineIndex, tokens.get(i));
        }
        
    }
}
