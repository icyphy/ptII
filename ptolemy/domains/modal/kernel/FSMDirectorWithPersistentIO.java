/* A FSM Director with persistent input and output ports.

   Copyright (c) 2014 The Regents of the University of California.
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


package ptolemy.domains.modal.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.domains.modal.modal.ModalPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** A FSM Director with persistent input and output ports.
 *  @author Patricia Derler
 *  @version $Id: PtidesDirector.java 67858 2013-11-13 20:11:03Z pd $
 *  @since Ptolemy II 10.0
 *
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class FSMDirectorWithPersistentIO extends FSMDirector {

    /**
     * Construct a director in the default workspace with an empty
     * string as its name. The director is added to the list of
     * objects in the workspace. Increment the version number of the
     * workspace.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public FSMDirectorWithPersistentIO() throws IllegalActionException,
            NameDuplicationException {
        super();
        _initialize();
    }

    /**
     * Construct a director in the workspace with an empty name. The
     * director is added to the list of objects in the
     * workspace. Increment the version number of the workspace.
     * @param workspace The workspace of this director.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public FSMDirectorWithPersistentIO(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _initialize();
    }

    /**
     * Construct a director in the given container with the given
     * name. The container argument must not be null, or a
     * NullPointerException will be thrown. If the name argument is
     * null, then the name is set to the empty string. Increment the
     * version number of the workspace.
     *
     * @param container Container of this director.
     * @param name Name of this director.
     * @exception IllegalActionException If the name has a period in it, or the director
     * is not compatible with the specified container.
     * @exception NameDuplicationException If the container not a CompositeActor and the
     * name collides with an entity in the container.
     */
    public FSMDirectorWithPersistentIO(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }
    
    /** Add the initialization parameter for the receiver.
     * @param port The port that contains the receiver.
     * @throws IllegalActionException If new parameter cannot be created.
     * @throws NameDuplicationException If new parameter cannot be created.
     */
    public void addPortInitAttribute(String portName) throws IllegalActionException, NameDuplicationException {
    	addPortInitAttribute(portName, new DoubleToken(1.0));
    }
    
    /** Add the initialization parameter for the receiver.
     * @param port The port that contains the receiver.
     * @param token The initial token.
     * @throws IllegalActionException If new parameter cannot be created.
     * @throws NameDuplicationException If new parameter cannot be created.
     */
    public void addPortInitAttribute(String portName, Token token) throws IllegalActionException, NameDuplicationException {
    	ModalModel model = (ModalModel) getContainer();
    	if (model.getAttribute("init_" + portName) == null) {
            new Parameter(model, "init_" + portName, token);
        }
    }
    
    /**
     * Return a receiver that is a one-place buffer. A token put into
     * the receiver will override any token already in the receiver.
     *
     * @return A receiver that is a one-place buffer.
     */
    @Override
    public Receiver newReceiver() {
        return new PersistentFSMReceiver();
    }

    /** Set the receiver tokens to be the tokens in the initialization.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
    	super.preinitialize();
    	if (getContainer() != null) {
            ModalModel model = (ModalModel) getContainer();
            for (Object portObject : model.portList()) {
                IOPort port = (IOPort) ((ModalPort) portObject).insidePortList().get(0);
                Parameter parameter = (Parameter) model.getAttribute("init_" + port.getName());
                Receiver[][] receivers = port.getReceivers();
                Token token = parameter.getToken();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        receivers[i][j].put(token);
                    }
                }
                receivers = port.getRemoteReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        receivers[i][j].put(token);
                    }
                }
            }
    	}
    }
	
    /** Remove initialization parameters for port. 
     * @param port The port
     */
    public void removePortInitAttribute(String portName) {
        ModalModel model = (ModalModel) getContainer();
        Attribute attribute = model.getAttribute("init_" + portName);
    	if (attribute != null) {
            model.removeAttribute(attribute);
        }
    }
	
    /** Rename port init attribute when port is renamed.
     * @param oldPortName The old port name.
     * @param newPortName The new port name.
     * @throws IllegalActionException If port init attributes cannot be edited.
     * @throws NameDuplicationException If port init attributes cannot be edited.
     */
    public void renamePortInitAttribute(String oldPortName, String newPortName) throws IllegalActionException, NameDuplicationException {
        ModalModel model = (ModalModel) getContainer();
        Parameter parameter = (Parameter) model.getAttribute("init_" + oldPortName);
        Token token = parameter.getToken();
        removePortInitAttribute(oldPortName);
    	addPortInitAttribute(newPortName, token);
    }

    /** Initialize parameters.
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    private void _initialize() throws IllegalActionException, NameDuplicationException {
    	if (getContainer() != null) {
            ModalModel model = (ModalModel) getContainer();
            for (Object portObject : model.portList()) {
                IOPort port = (IOPort) portObject;
                addPortInitAttribute(port.getName());
            }
    	}
    }
	
}
