/*
 Task that will be used to execute a Ptolemy simulation.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
import java.util.HashSet;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.Attribute;
import ptserver.communication.ProxyModelInfrastructure;
import ptserver.util.ProxyModelBuilder.ProxyModelType;
import ptserver.util.ServerUtility;

///////////////////////////////////////////////////////////////////
//// SimulationTask

/** Launch the simulation on the current thread under the provided
 *  ticket reference and wait for the user to issue control commands.
 *
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class SimulationTask implements Runnable {

    /** Create an instance of the simulation task to be run by the Ptolemy
     *  server application.
     *  @param ticket Reference to the simulation request.
     *  @exception Exception If the simulation encounters a problem setting
     *  the director or getting workspace access.
     */
    public SimulationTask(final Ticket ticket) throws Exception {
        CompositeActor model = (CompositeActor) ServerUtility
                .createMoMLParser().parse(null, new URL(ticket.getModelUrl()));
        CompositeActor layout = (CompositeActor) ServerUtility
                .createMoMLParser().parse(null, new URL(ticket.getLayoutUrl()));
        HashSet<String> remoteAttributes = new HashSet<String>();
        remoteAttributes.add(ServerUtility.REMOTE_OBJECT_TAG);
        ServerUtility.mergeModelWithLayout(model, layout,
                new HashSet<Class<? extends Attribute>>(), remoteAttributes);
        _proxyModelInfrastructure = new ProxyModelInfrastructure(
                ProxyModelType.SERVER, model);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the remote model.
     */
    public void close() {
        getProxyModelInfrastructure().close();
    }

    /** Start the execution of the simulation by kicking off the thread.
     */
    @Override
    public void run() {
        try {
            getProxyModelInfrastructure().getTopLevelActor().getManager()
                    .execute();
        } catch (Throwable e) {
            getProxyModelInfrastructure().fireModelException(
                    "Problem starting model execution", e);
        }
    }

    /** Get the manager responsible for coordinating the model of computation.
     *  @return The Manager used to control the simulation
     */
    public Manager getManager() {
        return getProxyModelInfrastructure().getManager();
    }

    /** Return the task's remote model.
     *  @return the remoteModel of the instance.
     */
    public ProxyModelInfrastructure getProxyModelInfrastructure() {
        return _proxyModelInfrastructure;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables

    /** The remote model that is used to replaced model actors.
     */
    private final ProxyModelInfrastructure _proxyModelInfrastructure;
}
