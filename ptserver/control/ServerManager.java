/* Ptolemy server which manages the broker, servlet, and simulations.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver.control;

import java.net.URL;

import ptolemy.kernel.util.IllegalActionException;
import ptserver.PtolemyServer;

import com.caucho.hessian.server.HessianServlet;

///////////////////////////////////////////////////////////////////
////ServerManager

/** Acts as a facade to the Ptolemy server application.  And administers
 * control commands coming through the servlet.
* 
* @author jkillian
* @version $Id$
* @Pt.ProposedRating Red (jkillian)
* @Pt.AcceptedRating Red (jkillian)
*/
public class ServerManager extends HessianServlet implements IServerManager {

    /** 
     * Open the model on a separate thread within the Ptolemy server.
     * 
     * @param url The path to the model file
     * @exception Exception If the simulation thread cannot be created or 
     * the file URL provided is invalid, throw an exception.
     * @return A reference to the execution thread of the selected model
     */
    public Ticket open(URL url) throws IllegalActionException {
        return PtolemyServer.getInstance().open(url);
    }

    /** 
     * Start the execution of the model.
     * 
     * @param ticket Reference to the execution thread
     * @exception IllegalStateException If the ticket reference is invalid 
     * or the thread's state cannot be modified, throw an exception.
     */
    public void start(Ticket ticket) throws IllegalActionException {
        PtolemyServer.getInstance().start(ticket);
    }

    /** 
     * Pause the execution of the running model.
     * 
     * @param ticket Reference to the execution thread
     * @exception IllegalStateException If the ticket reference is invalid 
     * or the thread's state cannot be modified, throw an exception.
     */
    public void pause(Ticket ticket) throws IllegalActionException {
        PtolemyServer.getInstance().pause(ticket);
    }

    /** 
     * Resume the execution of the paused model.
     * 
     * @param ticket Reference to the execution thread
     * @exception IllegalStateException If the ticket reference is invalid 
     * or the thread's state cannot be modified, throw an exception.
     */
    public void resume(Ticket ticket) throws IllegalActionException {
        PtolemyServer.getInstance().resume(ticket);
    }

    /** 
     * Stop the execution of the running model.
     * 
     * @param ticket Reference to the execution thread
     * @exception IllegalStateException If the ticket reference is invalid 
     * or the thread's state cannot be modified, throw an exception.
     */
    public void stop(Ticket ticket) throws IllegalActionException {
        PtolemyServer.getInstance().stop(ticket);
    }

    /** 
     * Close the model and destroy its owner thread.
     * 
     * @param ticket Reference to the execution thread
     * @exception IllegalStateException If the ticket reference is invalid 
     * or the thread's state cannot be modified, throw an exception.
     */
    public void close(Ticket ticket) throws IllegalActionException {
        PtolemyServer.getInstance().close(ticket);
    }

    /** 
     * Get the list of models available on the server either in the
     * database or within the file system.
     *
     * @exception Exception If there is an error querying either the 
     * database or the file system for available models, throw an exception.
     * @return Array of URL references to available model files
     */
    public URL[] getModelListing() throws IllegalActionException {
        return PtolemyServer.getInstance().getModelListing();
    }
}
