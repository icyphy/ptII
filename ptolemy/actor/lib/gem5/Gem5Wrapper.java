/* An actor that interacts with gem5 architectural simulator

   Copyright (c) 2017 The Regents of the University of California.
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
package ptolemy.actor.lib.gem5;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.Comparator;

import ptolemy.data.type.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Gem5Wrapper

/**
 *  An actor that interacts with gem5 architectural simulator.
 *
 *  <p>The <a href="http://gem5.org/#in_browser">gem5 simulator</a>
 *  "is a modular platform for computer-system architecture
 *  research."</p>
 *
 * @author Hokeun Kim
 * @version $Id: Gem5Wrapper.java 67679 2013-10-13 03:48:10Z cxh $
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Gem5Wrapper extends SequenceSource {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>init</i> and <i>step</i> parameter and the <i>step</i>
     *  port. Initialize <i>init</i>
     *  to IntToken with value 0, and <i>step</i> to IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Gem5Wrapper(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);
        init = new PortParameter(this, "init");
        init.setExpression("0");
        new Parameter(init.getPort(), "_showName", BooleanToken.TRUE);

        pipePathPrifix = new StringParameter(this, "pipePathPrifix");
        pipePathPrifix.setExpression("");
        
        step = new PortParameter(this, "step");
        step.setExpression("1");
        new Parameter(step.getPort(), "_showName", BooleanToken.TRUE);
        
        // set the type constraints.
        //ArrayType arrayOfCommandsType = new ArrayType(BaseType.GENERAL, 318);
        RecordType recordType = new RecordType(_labels, _types);
        ArrayType arrayOfRecordsType = new ArrayType(recordType);
        output.setTypeEquals(arrayOfRecordsType);
        output.setAutomaticTypeConversion(false);
        //output.setTypeEquals(BaseType.Arra);

        _attachText("_iconDescription", "<svg>\n"
                    + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                    + "style=\"fill:white\"/>\n"
                    + "<polygon points=\"-20,10 20,-10 20,10\" "
                    + "style=\"fill:grey\"/>\n" + "</svg>\n");

        // Show the firingCountLimit parameter last.
        firingCountLimit.moveToLast();
        br = null;
        process = null;
        os = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The value produced by the ramp on its first iteration.
     *  If this value is changed during execution, then the new
     *  value will be the output on the next iteration.
     *  The default value of this parameter is the integer 0.
     */
    public PortParameter init;

    /** The prefix of the file path for the pipe used for communicating
     *  with gem5 simulator.
     */
    public StringParameter pipePathPrifix;

    /** The amount by which the ramp output is incremented on each iteration.
     *  The default value of this parameter is the integer 1.
     */
    public PortParameter step;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>init</i> parameter, then reset the
     *  state to the specified value.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If <i>init<i> cannot be evaluated
     *   or cannot be converted to the output type, or if the superclass
     *   throws it.
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>init</code> and <code>step</code>
     *  public members to the parameters of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Ramp newObject = (Ramp) super.clone(workspace);

        // set the type constraints.
        newObject.output.setTypeAtLeast(newObject.init);
        newObject.output.setTypeAtLeast(newObject.step);
        return newObject;
    }

    private ArrayToken getGem5SimResult() throws IllegalActionException {
        try {
            int line;
            while (true) {
                char[] cbuf = new char[256];
                line = is.read(cbuf);
                if (line != -1) {
                    break;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        ArrayList<RecordToken> tokenArray = new ArrayList<RecordToken>();
        try {
            String line = br.readLine();
            while (line != null) {
                if (line.contains("PTOLEMY_LOG")) {
                    Token tokens[] = new Token[_labels.length];
                    //StringToken[] tuple = new StringToken[2];
                    StringTokenizer strTokenizer = new StringTokenizer(line);
                    String command = "";
                    int commandTime = 0;
                    int serviceTime = 0;
                    int rankNum = -1;
                    int bankNum = -1;
                    int initTime = 0;
                    boolean isFirstToken = true;
                    boolean isCommand = false;
                    boolean isRank = false;
                    boolean isBank = false;
                    while (strTokenizer.hasMoreTokens()) {
                        String curToken = strTokenizer.nextToken();
                        if (isFirstToken) {
                            isFirstToken = false;
                                                
                            long cpuInitTime = Long.parseLong(curToken.substring(0, curToken.length() - 1));
                            initTime = (int)((cpuInitTime + _systemClkPeriod) / _systemClkPeriod); // in ns
                            serviceTime = initTime % _sampleTime;
                        }
                        else if (curToken.contains("Rank")) {
                            isRank = true;
                        }
                        else if (isRank) {
                            isRank = false;
                            rankNum = Integer.parseInt(curToken.substring(0, curToken.length()));
                        }
                        else if (curToken.contains("Bank")) {
                            isBank = true;
                        }
                        else if (isBank) {
                            isBank = false;
                            bankNum = Integer.parseInt(curToken.substring(0, curToken.length()));
                        }
                        else if (curToken.contains("PRE") || curToken.contains("ACT")
                                 || curToken.contains("READ") || curToken.contains("WRITE")) {
                            isCommand = true;
                            command = new String(curToken.substring(0, curToken.length() - 1));
                        }
                        else if (isCommand) {
                            isCommand = false;
                            // from previous delay
                            int delayDiff = Integer.parseInt(curToken);
                            delayDiff = ((delayDiff + _systemClkPeriod) / _systemClkPeriod); // in ns
                            commandTime += delayDiff;
                            tokens[0] = new StringToken(command);
                            tokens[1] = new IntToken(initTime + commandTime);
                            tokens[2] = new IntToken(rankNum);
                            tokens[3] = new IntToken(bankNum);
                            tokens[4] = new IntToken(serviceTime + commandTime);
                                                
                            tokenArray.add(new RecordToken(_labels, tokens));
                            //tokenArray.add(new ArrayToken(BaseType.STRING,tuple));
                        }
                    }
                    sb.append(line);
                    sb.append(System.lineSeparator());
                }
                line = br.readLine();
            }
            //everything = sb.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
                
        Collections.sort(tokenArray, new SortByCommandTime());
                
        //StringToken stringToken = new StringToken("*************Iteration Count: " + _iterationCount + "\n" + sb.toString());
        Token[] dummy = new Token[0];
        if (tokenArray.isEmpty()) {
            return null;
        }
        else {
            return new ArrayToken(tokenArray.toArray(dummy));
        }
    }
    
    /** Send the current value of the state of this actor to the output.
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        arrayToken = getGem5SimResult();
        if (arrayToken != null) {
            output.send(0, arrayToken);
        }
        _iterationCount++;
    }

    /** Set the state to equal the value of the <i>init</i> parameter.
     *  The state is incremented by the value of the <i>step</i>
     *  parameter on each iteration (in the postfire() method).
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        //String pipePathPrifix = "/Users/hokeunkim/Development/ee219dproject/gem5-stable_2015_09_03/";
        try {
            if (process != null) {
                process.destroy();
                process = null;
                os = null;
                is = null;
            }
                
            String outputFileName = pipePathPrifix.getValueAsString() + "/read_pipe";
            os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFileName))));

            os.newLine();
            os.flush();
            String inputFileName = pipePathPrifix.getValueAsString() + "/write_pipe";
            is = new InputStreamReader(new FileInputStream(new File(inputFileName)));
            //process = pb.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        
        try {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            br = new BufferedReader(new FileReader(pipePathPrifix.getValueAsString() + "/temp_pipe"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            throw new InternalErrorException(
                                             "No pipe file! in "
                                             + getFullName());
        }
    }

    /** Invoke a specified number of iterations of this actor. Each
     *  iteration updates the state of the actor by adding the
     *  value of the <i>step</i> parameter to the state and sending
     *  the value of the state to the output. The iteration count
     *  is also incremented by the value of <i>count</i>, and if
     *  the result is greater than or equal to <i>firingCountLimit</i>
     *  then return STOP_ITERATING.
     *  <p>
     *  This method should be called instead of the usual prefire(),
     *  fire(), postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.  This leads to more
     *  efficient execution.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, if the maximum
     *   iteration count has been reached, return STOP_ITERATING.
     *  @exception IllegalActionException If iterating cannot be
     *  performed.
     */
    /** Update the state of the actor by adding the value of the
     *  <i>step</i> parameter to the state.  Also, increment the
     *  iteration count, and if the result is equal to
     *  <i>firingCountLimit</i>, then
     *  return false.
     *  @return False if the number of iterations matches the number requested.
     *  @exception IllegalActionException If the firingCountLimit parameter
     *   has an invalid expression.
     */
    public boolean postfire() throws IllegalActionException {
        //_stateToken = _stateToken.add(step.getToken());
        
        try {
            os.newLine();
            os.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private BufferedReader br;
    private Process process;
    private BufferedWriter os;
    private InputStreamReader is;
    private ArrayToken arrayToken;
    private String[] _labels = {"cmd", "cmd_time", "rank", "bank", "service_time"};
    private Type[] _types = {BaseType.STRING, BaseType.INT, BaseType.INT, BaseType.INT, BaseType.INT};
    private int _systemClkPeriod = 1000;
    private int _sampleTime = 500 * 1000;        // 0.5 ms
        
    public class SortByCommandTime implements Comparator<RecordToken> {
        public int compare(RecordToken t1, RecordToken t2) {
            int time1 = ((IntToken)t1.get(_labels[1])).intValue();
            int time2 = ((IntToken)t2.get(_labels[1])).intValue();
            return (time1 - time2);
        }
    }
}
