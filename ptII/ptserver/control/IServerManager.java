/* Ptolemy server's servlet interface definition

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

///////////////////////////////////////////////////////////////////
////IServerManager

/** Defines the control commands that can be administered to the 
 * Ptolemy server from it's distributed clients.  These functions are 
 * available through a synchronous, RPC-like servlet that is embedded 
 * within the Ptolemy server.
 * 
 * @author jkillian
 * @version $Id$
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
*/

public interface IServerManager {

    /** Open the model on a separate thread within the Ptolemy server.
     * 
     * @param url Path to the model file
     * @return Reference to the execution thread of the selected model
     */
    public Ticket open(URL url) throws Exception;

    /** Start the execution of the model.
     * 
     * @param ticket Reference to the execution thread
     * @return Status of the start() call
     */
    public void start(Ticket ticket) throws Exception;

    /** Pause the execution of the running model.
     * 
     * @param ticket Reference to the execution thread
     * @return Status of the pause() call
     */
    public void pause(Ticket ticket) throws Exception;

    /** Resume the execution of the paused model.
     * 
     * @param ticket Reference to the execution thread
     * @return Status of the resume() call
     */
    public void resume(Ticket ticket) throws Exception;

    /** Stop the execution of the running model.
     * 
     * @param ticket Reference to the execution thread
     * @return Status of the stop() call
     */
    public void stop(Ticket ticket) throws Exception;

    /** Close the model and destroy it's owner thread.
     * 
     * @param ticket Reference to the execution thread
     * @return Status of the close() call
     */
    public void close(Ticket ticket) throws Exception;

    /** Get the list of models available on the server either in the
     * database or within the file system.
     *
     * @return List of files on the server
     */
    public String[] getModelListing() throws Exception;
}
