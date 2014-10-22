/* A BranchController manages the execution of a set of branch objects by
 monitoring whether the branches have blocked.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.process;

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// BranchController

/**
 A BranchController manages the execution of a set of branch objects by
 monitoring whether the branches have blocked. A branch blocks when it is
 either unable to get data from its producer receiver or put data into its
 consumer receiver. When a branch blocks, it registers the block with its
 branch controller by passing the specific receiver that is blocked. If all
 of a branch controllers branches are blocked, then the branch controller
 informs the director associated with its containing composite actors.
 <P>
 Branches are assigned to a branch controller by the director associated
 with the controller's composite actor via the addBranches() method. This
 method takes an io port and determines the port's receivers. Branches
 are then instantiated and assigned to the receivers according to whether
 the receivers are producer or consumer receivers.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (davisj)
 */
public class BranchController implements Runnable {
    /** Construct a branch controller in the specified composite actor
     *  container.
     *
     *  @param container The parent actor that contains this object.
     */
    public BranchController(CompositeActor container) {
        _parentActor = container;
        //_parentName = ((Nameable) container).getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Activate the branches that are managed by this branch
     *  controller. This method should be invoked once when
     *  a branch controller first starts the branches it controls.
     *  Invocation of this method will cause the branches to
     *  begin transferring tokens between their assigned producer
     *  and consumer receiver. Each branch executes in its own
     *  thread.
     */
    public void activateBranches() {
        // Copy the list of branches within a synchronized block,
        // then activate them. We do not want to hold the lock
        // on this controller during the run, as this can cause
        // deadlock.
        LinkedList branchesCopy;

        synchronized (this) {
            if (!hasBranches()) {
                return;
            }

            setActive(true);
            branchesCopy = new LinkedList(_branches);
        }

        Iterator branches = branchesCopy.iterator();

        while (branches.hasNext()) {
            Branch branch = (Branch) branches.next();
            Thread thread = new Thread(branch);
            _getDirector().addThread(thread);
            thread.start();
        }
    }

    /** Add branches corresponding to the channels of the port
     *  argument. The port must be contained by the same actor
     *  that contains this controller. If branches corresponding
     *  to the specified port have already been added to this
     *  controller, then an IllegalActionException will be thrown.
     *  If the input/output polarity of this port does not match that
     *  of ports for whom branches have been previously added
     *  to this controller, then throw an IllegalActionException.
     *  @param port The port for which branches will be added to this
     *   controller.
     *  @exception IllegalActionException If branches for the
     *   port have been previously added to this controller or
     *   if the port input/output polarity does not match that
     *   of ports for whom branches were previously add to this
     *   controller.
     */
    public void addBranches(IOPort port) throws IllegalActionException {
        if (port.getContainer() != getParent()) {
            throw new IllegalActionException("Can not contain "
                    + "a port that is not contained by this "
                    + "BranchController's container.");
        }

        if (_ports.contains(port)) {
            throw new IllegalActionException(port, "This port "
                    + "is already controlled by this " + "BranchController");
        }

        // Careful; maintain order of following test in case
        // Java is like C
        if (_hasInputPorts() && !port.isInput()) {
            throw new IllegalActionException("BranchControllers "
                    + "must contain only input ports or only output "
                    + "ports; not both");
        }

        if (_hasOutputPorts() && !port.isOutput()) {
            throw new IllegalActionException("BranchControllers "
                    + "must contain only input ports or only output "
                    + "ports; not both");
        }

        _ports.add(port);

        Branch branch = null;
        ProcessReceiver producerReceiver = null;
        ProcessReceiver consumerReceiver = null;
        Receiver[][] producerReceivers = null;
        Receiver[][] consumerReceivers = null;

        for (int i = 0; i < port.getWidth(); i++) {
            if (port.isInput()) {
                producerReceivers = port.getReceivers();
                consumerReceivers = port.deepGetReceivers();
            } else if (port.isOutput()) {
                producerReceivers = port.getInsideReceivers();
                consumerReceivers = port.getRemoteReceivers();
            } else {
                throw new IllegalActionException("Bad news");
            }

            // If the port lacks either producer or consumer
            // receivers, then there is no point in creating a branch.
            if (producerReceivers.length > i && consumerReceivers.length > i) {
                try {
                    producerReceiver = (ProcessReceiver) producerReceivers[i][0];
                    consumerReceiver = (ProcessReceiver) consumerReceivers[i][0];
                } catch (ClassCastException ex) {
                    // See [Bug 5] and pn/test/PNInsideDE.xml
                    throw new IllegalActionException(
                            port,
                            ex,
                            "At the current time, process-oriented domains "
                                    + "(PN and CSP) cannot be nested inside "
                                    + "firing-based domains (SDF, DE, CT, etc.).");
                }

                branch = new Branch(producerReceiver, consumerReceiver, this);
                _branches.add(branch);
            }
        }
    }

    /** Deactivate the branches assigned to this branch controller.
     */
    public synchronized void deactivateBranches() {
        setActive(false);

        Iterator branches = _branches.iterator();
        Branch branch = null;

        while (branches.hasNext()) {
            branch = (Branch) branches.next();
            branch.setActive(false);

            Receiver receiver = branch.getConsumerReceiver();

            synchronized (receiver) {
                receiver.notifyAll();
            }

            receiver = branch.getProducerReceiver();

            synchronized (receiver) {
                receiver.notifyAll();
            }
        }

        notifyAll();
    }

    /** Return the list of branches controlled by this controller.
     *  @return The list of branches controlled by this controller.
     */
    public LinkedList getBranchList() {
        return _branches;
    }

    /** Return the composite actor that contains this branch
     *  controller.
     *  @return The composite actor that contains this controller.
     */
    public CompositeActor getParent() {
        return _parentActor;
    }

    /** Return true if this branch controller controls one or more
     *  branches; return false otherwise.
     *  @return True if this controller controls one or more branches;
     *   return false otherwise.
     */
    public boolean hasBranches() {
        return _branches.size() > 0;
    }

    /** Return true if this controller is active; return false
     *  otherwise.
     * @return True if this controller is active; false otherwise.
     */
    public synchronized boolean isActive() {
        return _isActive;
    }

    /** Return true if all of the branches assigned to this branch
     *  controller are blocked or if this branch controller has no
     *  branches; return false otherwise.
     *  @return True if all branches controlled by this branch
     *   controller are blocked or if this branch controller has
     *   no branches; return false otherwise.
     */
    public synchronized boolean isBlocked() {
        if (!hasBranches()) {
            return true;
        }

        if (_branchesBlocked >= _branches.size()) {
            if (_branchesBlocked > 0) {
                return true;
            }
        }

        return false;
    }

    /** Begin executing the branches associated with this branch
     *  controller so that they will begin transferring data in
     *  their assigned channels. If all of the branches become
     *  blocked then the director associated with this branch
     *  branch controller is notified.
     */
    @Override
    public void run() {
        try {
            activateBranches();

            // After starting the runs, acquire a lock
            // on this object.
            synchronized (this) {
                while (isActive()) {
                    while (!isBlocked() && isActive()) {
                        wait();
                    }

                    while (isBlocked() && isActive()) {
                        _getDirector()._controllerBlocked(this);
                        wait();
                    }

                    _getDirector()._controllerUnBlocked(this);
                }
            }
        } catch (InterruptedException e) {
            // FIXME: Do something
        }
    }

    /** Set this branch controller active if the active parameter is
     *  true; set this branch controller to inactive otherwise.
     *  @param active The indicator of whether this branch controller
     *   will be set active or inactive.
     */
    public synchronized void setActive(boolean active) {
        _isActive = active;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the director that controls the execution of this
     *  branch controller's containing composite actor.
     *  @return The composite process director that is associated
     *   with this branch controller's container.
     */
    protected CompositeProcessDirector _getDirector() {
        try {
            return (CompositeProcessDirector) _parentActor.getDirector();
        } catch (NullPointerException ex) {
            // If a thread has a reference to a receiver with no director it
            // is an error so terminate the process.
            String name = ((Nameable) getParent()).getName();
            throw new TerminateProcessException("Error: " + name
                    + " contains a branch controller that has a "
                    + "receiver that does not have a director");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if this branch controller has input ports associated
     *  with it; return false otherwise.
     *  @return True if this branch controller has input ports associated
     *  with it. False otherwise.
     */
    private boolean _hasInputPorts() {
        if (_ports.size() == 0) {
            return false;
        }

        Iterator ports = _ports.iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            return port.isInput();
        }

        return false;
    }

    /** Return true if this branch controller has output ports associated
     *  with it; return false otherwise.
     *  @return True if this branch controller has output ports associated
     *  with it. False otherwise.
     */
    private boolean _hasOutputPorts() {
        if (_ports.size() == 0) {
            return false;
        }

        Iterator ports = _ports.iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            return port.isOutput();
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The number of branches that are blocked
    private int _branchesBlocked = 0;

    // The CompositeActor that owns this controller object.
    private CompositeActor _parentActor;

    private LinkedList _branches = new LinkedList();

    private LinkedList _ports = new LinkedList();

    private boolean _isActive = false;

    //private String _parentName = null;
}
