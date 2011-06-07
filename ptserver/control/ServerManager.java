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

import java.io.InputStream;
import java.util.logging.Level;

import ptolemy.kernel.util.IllegalActionException;
import ptserver.PtolemyServer;
import ptserver.communication.RemoteModelResponse;

import com.caucho.hessian.server.HessianServlet;

///////////////////////////////////////////////////////////////////
//// ServerManager

/**
 * Acts as a facade to the Ptolemy server application and administers control
 * commands coming through the servlet.
 * 
 * @author Justin Killian
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
 */
public class ServerManager extends HessianServlet implements IServerManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Close the model and destroy its owner thread.
     * 
     * @param ticket Reference to the execution thread
     * @exception IllegalStateException If the ticket reference is invalid or the thread's state
     * cannot be modified.
     */
    public void close(Ticket ticket) throws IllegalActionException {
        try {
            PtolemyServer.getInstance().close(ticket);
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to close the simulation request.");
            throw new IllegalActionException(null, e,
                    "Unable to close the simulation request.");
        }
    }

    /**
     * Return an input stream for the given model file for downloading on the
     * client it.
     * 
     * @param modelName Name for the model xml file.
     * @param exception The exception that was raised.
     * @exception IllegalActionException If the server encountered an error starting, stopping, or
     * manipulating a simulation request.
     * @return InputStream to the model xml file.
     */
    public InputStream downloadModel(String modelName)
            throws IllegalActionException {
        try {
            InputStream inputStream = PtolemyServer.getInstance()
                    .downloadModel(modelName);
            return inputStream;
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to download the model file.");
            throw new IllegalActionException(null, e,
                    "Unable to download the model file.");
        }
    }

    /**
     * Get the list of models available on the server either in the database or
     * within the file system.
     * 
     * @return The Array of strings containing names of available models.
     * @exception Exception If there is an error querying either the database or the
     *                      file system for available models.
     */
    public String[] getModelListing() throws IllegalActionException {
        return PtolemyServer.getInstance().getModelListing();
    }

    /**
     * Open the model on a separate thread within the Ptolemy server.
     * 
     * @param url The path to the model file
     * @exception ExceptionIf the simulation thread cannot be created or the file URL
     * provided is invalid, throw an exception.
     * @return The user's reference to the simulation task along with
     * specifically formatted for the client model XML and its inferred
     * types
     */
    public RemoteModelResponse open(String url) throws IllegalActionException {
        try {
            return PtolemyServer.getInstance().open(url);
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to open the requested model file.");
            throw new IllegalActionException(null, e,
                    "Unable to open the requested model file.");
        }
    }

    /**
     * Pause the execution of the running model.
     * 
     * @param ticket Reference to the execution thread
     * @exception IllegalStateException If the ticket reference is invalid or the thread's state
     * cannot be modified, throw an exception.
     */
    public void pause(Ticket ticket) throws IllegalActionException {
        try {
            PtolemyServer.getInstance().pause(ticket);
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to pause the running simulation.");
            throw new IllegalActionException(null, e,
                    "Unable to pause the running simulation.");
        }
    }

    /**
     * Resume the execution of the paused model.
     * 
     * @param ticket Reference to the execution thread
     * @exception IllegalStateException If the ticket reference is invalid or the thread's state
     * cannot be modified, throw an exception.
     */
    public void resume(Ticket ticket) throws IllegalActionException {
        try {
            PtolemyServer.getInstance().resume(ticket);
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to resume the idle simulation.");
            throw new IllegalActionException(null, e,
                    "Unable to resume the idle simulation.");
        }
    }

    /**
     * Start the execution of the model.
     * 
     * @param ticket Reference to the execution thread
     * @exception IllegalStateException  If the ticket reference is invalid or the thread's state
     *                cannot be modified, throw an exception.
     */
    public void start(Ticket ticket) throws IllegalActionException {
        try {
            PtolemyServer.getInstance().start(ticket);
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to start the requested simulation.");
            throw new IllegalActionException(null, e,
                    "Unable to start the requested simulation.");
        }
    }

    /**
     * Stop the execution of the running model.
     * 
     * @param ticket  The ticket reference to the simulation request.
     * @exception IllegalStateException If the ticket reference is invalid or the thread's state
     * cannot be modified, throw an exception.
     */
    public void stop(Ticket ticket) throws IllegalActionException {
        try {
            PtolemyServer.getInstance().stop(ticket);
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to stop the running simulation.");
            throw new IllegalActionException(null, e,
                    "Unable to stop the running simulation.");
        }

    }

}
