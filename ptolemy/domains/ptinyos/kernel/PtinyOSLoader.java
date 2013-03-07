/* Interface for the Ptolemy/TinyOS Loader.

 Copyright (c) 2005-2013 The Regents of the University of California.
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

import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// PtinyOSLoader

/**
 Interface for the Ptolemy/TinyOS Loader.  Defines the requirements
 for an object that loads a C based TinyOS shared object into a
 running Java Ptolemy environment.

 <p>The {@link ptolemy.domains.ptinyos.kernel.PtinyOSDirector#preinitialize()}
 method creates a .java file that implements this class and then compiles
 the .java file

 @author Elaine Cheong
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Green (celaine)
 @Pt.AcceptedRating Green (celaine)
 */
public interface PtinyOSLoader {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enqueue the next TOSSIM event into ptII at the specified time.
     *
     *  <p>This is a JNI method that gets called by TOSSIM to enqueue
     *  an event.
     *
     *  @param newTime A string representation of the time of the next
     *  event.  In TOSSIM, unit of time is a tick of a 4MHz clock, and
     *  time is stored in a long long in C (a 64-bit integer on most
     *  systems).
     */
    public void enqueueEvent(String newTime);

    /** Get a char value from PortParameter named parameter.
     *
     *  <p>This is a JNI method that gets called by TOSSIM to get a
     *  sensor value.
     *
     *  @param parameter The parameter.
     *  @return The char value of the parameter, or 0 if there is an error.
     */
    public char getCharParameterValue(String parameter);

    /** Load the JNI shared object associated with the toplevel
     *  PtinyOSDirector.
     *  @param path The directory that contains the JNI shared object.
     *  @param director The director that loads this PtinyOSLoader.
     */
    public void load(String path, PtinyOSDirector director);

    /** Native method that invokes the main() method in TOSSIM.
     *  @param argsToMain Arguments to pass to TOSSIM.
     *  @return Zero if call completes successfully, non-zero if there
     *  are errors in TOSSIM.
     *  @exception InternalErrorException If thrown in TOSSIM.
     */
    public int main(String[] argsToMain) throws InternalErrorException;

    /** Native method that calls TOSSIM to process an event at the
     *  current time.
     *  @param currentTime The current time.
     */
    public void processEvent(long currentTime);

    /** Native method that calls TOSSIM to receive a packet in TOSSIM.
     *  @param currentTime The current time.
     *  @param packet The string value of the packet to send to TOSSIM.
     */
    public void receivePacket(long currentTime, String packet);

    /** SSend an expression to a ptII port.  This is used, for example,
     *  to send LED or packet data from TOSSIM to the rest of the
     *  Ptolemy II model.
     *
     *  <p>This is a JNI method that gets called by TOSSIM to send a
     *  value to a ptII port.
     *
     *  @param portName The name of the port.
     *  @param expression The expression to send to the ptII port.
     *  @return true if the expression was successfully sent, false if the
     *  port is not connected or not found.
     */
    public boolean sendToPort(String portName, String expression);

    /** Print a debug message in ptII.
     *
     *  <p>This is a JNI method that gets called by TOSSIM to print a
     *  debug message in ptII.
     *
     *  @param debugMode A long long in C (currently unused).
     *  @param message A char * in C.
     *  @param nodeID A short in C.
     */
    public void tosDebug(String debugMode, String message, String nodeID);

    /** Native method that calls TOSSIM to shut itself down.
     */
    public void wrapup();

    /** Start the TOSSIM event accept and TOSSIM command read threads. */
    public void startThreads();

    /** Join the TOSSIM event accept and TOSSIM command read threads.
     *  @return true if successful, otherwise false.
     */
    public boolean joinThreads();
}
