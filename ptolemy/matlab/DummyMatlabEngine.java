/* Matlab Engine Interface

 Copyright (c) 1998-2003 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (zkemenczy@rim.net)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.matlab;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.Token;
import ptolemy.data.StringToken;

//////////////////////////////////////////////////////////////////////////
//// DummyMatlabEngine
/**
An implementation of the Matlab interface that throws an exception.
This implementation is created by the MatlabEngineFactory if matlab is
not installed.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class DummyMatlabEngine implements MatlabEngineInterface {

    /** Enable/disable debug statements to stdout.
     * @param d Non-zero to enable debug statements, zero to disable.
     */
    public void setDebugging(byte d) {
        // Ignore.
    }

    /** Open a connection to the default matlab engine installed on
     * this host with its output buffered.
     * @see #open(String, boolean)
     */
    public long[] open() throws IllegalActionException {
        return open("", false);
    }

    /** Open a connection to the default matlab engine installed on
     * this host with specified output buffering.
     * @param needOutput selects whether the output should be buffered
     * or not.
     * @see #open(String, boolean)
     */
    public long[] open(boolean needOutput) throws IllegalActionException {
        return open(false);
    }

    /** Open a connection to a matlab engine.<p>
     * For more information, see the matlab engine API reference engOpen()
     * @param startCmd hostname or command to use to start the engine.
     * @return long[2] retval engine handle; retval[0] is the real
     * engine handle, retval[1] is a pointer to the engine output
     * buffer; both should be preserved and passed to subsequent engine
     * calls.
     * @exception IllegalActionException If the matlab engine open is
     * unsuccessful.  This will typically occur if ptmatlab (.dll)
     * cannot be located or if the matlab bin directory is not in the
     * path.
     * @see #getOutput(long[])
     */
    public long[] open(String startCmd, boolean needOutput)
            throws IllegalActionException {
        throw new IllegalActionException(
                "The Matlab interface is not available.");
    }

    /** Close a connection to a matlab engine.
     * This will also close the matlab engine if this instance was the last
     * user of the matlab engine.
     * <p>
     * For more information, see matlab engine API reference engClose()
     */
    public int close(long[] eng) {
        // Ignore
        return 0;
    }
    
    /** Send a string for evaluation to the matlab engine.
     * @param evalStr string to evaluate.
     * @exception IllegalActionException If the matlab engine is not opened.
     */
    public int evalString(long[] eng, String evalStr)
            throws IllegalActionException {
        throw new IllegalActionException(
                "The Matlab interface is not available.");
    }

    /** Return a Token from the matlab engine using default
     * {@link Engine.ConversionParameters} values.
     * @param name Matlab variable name used to initialize the returned Token
     * @return PtolemyII Token.
     * @exception IllegalActionException If the matlab engine is not opened, or
     * if the matlab variable was not found in the engine. In this case, the
     * matlab engine's stdout is included in the exception message.
     * @see Expression
     */
    public Token get(long[] eng, String name) throws IllegalActionException {
        throw new IllegalActionException(
                "The Matlab interface is not available.");
    }

    /** Return a Token from the matlab engine using specified
     * {@link Engine.ConversionParameters} values.
     * @param name Matlab variable name used to initialize the returned Token
     * @return PtolemyII Token.
     * @exception IllegalActionException If the matlab engine is not opened, or
     * if the matlab variable was not found in the engine. In this case, the
     * matlab engine's stdout is included in the exception message.
     * @see Expression
     */
    public Token get(long[] eng, String name, ConversionParameters par)
            throws IllegalActionException {
        throw new IllegalActionException(
                "The Matlab interface is not available.");
    }

    /** Get last matlab stdout.
     * @return PtolemyII StringToken
     */
    public StringToken getOutput(long[] eng) {
        return new StringToken("");
    }

    /** Return the semaphore that can be used to reserve the matlab
     * interface.
     */
    public Object getSemaphore() {
        return this;
    }

    /** Create a matlab variable using name and a Token.
     * @param name matlab variable name.
     * @param t Token to provide value.
     * @see Engine
     */
    public int put(long[] eng, String name, Token t)
            throws IllegalActionException {
        throw new IllegalActionException(
                "The Matlab interface is not available.");
    }
}    

