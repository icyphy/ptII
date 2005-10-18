/* Interface for the Ptolemy/TinyOS Loader.

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.domains.ptinyos.kernel;

//////////////////////////////////////////////////////////////////////////
//// PtinyOSLoader

/**
 Interface for the Ptolemy/TinyOS Loader.
 <p>The {@link ptolemy.domains.ptinyos.kernel.PtinyOSDirector#preinitialize()}
 method creates a .java file that implements this class and then compiles
 the .java file

 @author Elaine Cheong
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
import ptolemy.kernel.util.IllegalActionException;

public interface PtinyOSLoader {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** A callback method (from C code) for the application to enqueue the
     *  next event.
     *  @param newTime The next event, which should be a long long in C.
     *  @exception IllegalActionException If there is a problem calling
     *  fireAt().
     */
    public void enqueueEvent(String newTime) throws IllegalActionException;

    /** Get a Boolean value from PortParameter named parameter.
     *  @param parameter The parameter
     *  @return The Boolean value of the parameter, or 0 if there is an error.
     *  @exception IllegalActionException If there is an error.
     */
    public boolean getBooleanParameterValue(String parameter)
            throws IllegalActionException;

    /** Get a char value from PortParameter named parameter.
     *  @param parameter The parameter
     *  @return The char value of the parameter, or 0 if there is an error.
     *  @exception IllegalActionException If there is an error.
     */
    public char getCharParameterValue(String parameter)
            throws IllegalActionException;

    /** Load the JNI shared object associated with the toplevel
     *  PtinyOSDirector.
     *  @param path The directory that contains the JNI shared object.
     *  @param director The directory (FIXME: currently ignored?)
     */
    public void load(String path, PtinyOSDirector director);

    /** Invoke the main() method associated with the toplevel PtinyOSDirector.
     *  @param argsToMain Arguments to pass.
     */
    public int main(String[] argsToMain);

    /** Process an event.
     *  @param currentTime The current time.
     */
    public void processEvent(long currentTime);

    /** Receive a packet.
     *  @param currentTime The current time.
     *  @param packet The packet.
     */
    public void receivePacket(long currentTime, String packet);

    /** Send an expression to a port.
     *  @param portName The name of the port
     *  @param expression The expression
     *  @return 1 if the expression was successfully sent, 0 if the
     *  port is not connected or not found and -1 if the port is
     *  of any type other than Boolean or String.
     *  @exception IllegalActionException If thrown by the director.
     */ 
    public int sendToPort(String portName, String expression)
            throws IllegalActionException;

    /** A callback method (from C code) for the application to print a debug
     *  message.
     *  @param debugMode A long long in C (currently unused)
     *  @param message A char * in C
     *  @param nodeNumber is a short in C
     */
    public void tosDebug(String debugMode, String message, String nodeNumber);

    /** Invoke the wrapup() method of the toplevel Director.
     */
    public void wrapup();
}
