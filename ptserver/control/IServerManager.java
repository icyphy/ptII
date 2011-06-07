/* A simplified interface to the Ptolemy server application that
   enables users to administer control commands through the servlet.

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

import java.io.InputStream;

import ptolemy.kernel.util.IllegalActionException;
import ptserver.communication.RemoteModelResponse;

///////////////////////////////////////////////////////////////////
//// IServerManager

/** Define the control commands that can be administered to the
 *  Ptolemy server from its distributed clients.  These functions are
 *  available through a synchronous, RPC-like servlet that is embedded
 *  within the Ptolemy server.
 *
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
*/
public interface IServerManager {

    /** Shut down the thread associated with the user's ticket.
     *  @param ticket Ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to
     *  destroy the simulation thread.
     */
    public void close(Ticket ticket) throws IllegalActionException;

    /** Return an input stream for the given model file for downloading on the
     *  client it.
     *  @param modelName Name for the model xml file.
     *  @param exception The exception that was raised.
     *  @exception IllegalActionException If the server encountered an error starting, stopping, or
     *                manipulating a simulation request.
     *  @return InputStream to the model xml file.
     */
    public InputStream downloadModel(String modelName)
            throws IllegalActionException;

    /** Get a listing of the models available on the server in either the
     *  database or the local file system.
     *  @return The Array of strings containing names of available models.
     *  @exception IllegalActionException If there was a problem discovering
     *  available models.
     */

    public String[] getModelListing() throws IllegalActionException;

    /** Open a model with the provided model URL and wait for the
     *  user to request the execution of the simulation.
     *  @param url The path to the model file
     *  @exception IllegalActionException If the model fails to load
     *  from the provided URL.
     *  @return The user's reference to the simulation task along with 
     *  specifically formatted for the client model XML and its inferred types
     */
    public RemoteModelResponse open(String url) throws IllegalActionException;

    /** Pause the execution of the selected simulation.
     *  @param ticket The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to
     *  pause the running simulation.
     */
    public void pause(Ticket ticket) throws IllegalActionException;

    /** Resume the execution of the selected simulation.
     *  @param ticket The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to
     *  resume the execution of the simulation.
     */
    public void resume(Ticket ticket) throws IllegalActionException;

    /** Start the execution of the selected simulation.
     *  @param ticket The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to
     *  start the simulation.
     */
    public void start(Ticket ticket) throws IllegalActionException;

    /** Stop the execution of the selected simulation.
     *  @param ticket The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to
     *  stop the simulation.
     */
    public void stop(Ticket ticket) throws IllegalActionException;

}
