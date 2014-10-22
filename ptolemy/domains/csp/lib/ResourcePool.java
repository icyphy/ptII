/* An actor representing a resource pool with a specified number of resources.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.domains.csp.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.domains.csp.kernel.CSPActor;
import ptolemy.domains.csp.kernel.ConditionalBranch;
import ptolemy.domains.csp.kernel.ConditionalReceive;
import ptolemy.domains.csp.kernel.ConditionalSend;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////

/**
 This actor manages a pool of resources, where each resource is
 represented by a token with an arbitrary value.  Resources are
 granted on the <i>grant</i> output port and released on the
 <i>release</i> input port. These ports are both multiports,
 so resources can be granted to multiple users of the resources,
 and released by multiple actors.
 <p>
 The initial pool of resources is provided by the <i>initialPool</i>
 parameter, which is an array of arbitrary type.  The <i>grant</i>
 output port and <i>release</i> input port are constrained to have
 compatible types. Specifically, the <i>grant</i> output port must be
 able to send tokens with types that match the elements of this array,
 and it must also be able to send tokens with types that match
 inputs provided at the <i>release</i> input.
 <p>
 This actor is designed for use in the CSP domain, where it will
 execute in its own thread. At all times, it is ready to
 rendezvous with any other actor connected to its <i>release</i>
 input port.  When such a rendezvous occurs, the token provided
 at that input is added to the resource pool. In addition,
 whenever the resource pool is non-empty, this actor is ready
 to rendezvous with any actor connected to its <i>grant</i>
 output port. When such a rendezvous occurs, it sends
 the first token in the resource pool to that output port
 and removes that token from the resource pool.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class ResourcePool extends CSPActor {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ResourcePool(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        grant = new TypedIOPort(this, "grant", false, true);
        grant.setMultiport(true);

        release = new TypedIOPort(this, "release", true, false);
        release.setMultiport(true);

        initialPool = new Parameter(this, "initialPool");
        initialPool.setExpression("{1}");

        // Set type constraints.
        grant.setTypeAtLeast(ArrayType.elementType(initialPool));
        grant.setTypeAtLeast(release);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port through which this actor grants resources.
     *  This port has type equal to the element type
     *  of the <i>initialPool</i> parameter.
     */
    public TypedIOPort grant;

    /** The input port through which other actors release resources.
     *  This port has type equal to the element type
     *  of the <i>initialPool</i> parameter.
     */
    public TypedIOPort release;

    /** The initial resource pool. This is an array with default
     *  value {1} (an integer array with one entry with value 1).
     */
    public Parameter initialPool;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reset the resource pool to
     *  match the specified initialPool value.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == initialPool) {
            ArrayToken pool = (ArrayToken) initialPool.getToken();
            // Reset the pool.
            _pool.clear();
            // Copy the tokens into the pool.
            for (int i = 0; i < pool.length(); i++) {
                _pool.add(pool.getElement(i));
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the base class to set the type constraints.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ResourcePool actor.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ResourcePool newObject = (ResourcePool) super.clone(workspace);
        // set type constraints.
        try {
            newObject.grant.setTypeAtLeast(ArrayType
                    .elementType(newObject.initialPool));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        newObject.grant.setTypeAtLeast(newObject.release);

        return newObject;
    }

    /** If there are available resources, then perform a conditional
     *  branch on any <i>release</i> input or <i>grant</i> output. If the selected
     *  branch is a release input, then add the provided token to the
     *  end of the resource pool. If it is a grant output, then remove
     *  the first element from the resource pool and send it to the output.
     *  If there are no available resources, then perform a conditional branch
     *  only on the release inputs.
     *  @exception IllegalActionException If an error occurs during
     *   executing the process.
     *  @exception TerminateProcessException If the process termination
     *   is requested by the director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("Resources available: " + _pool);
        }
        int numberOfConditionals = release.getWidth();
        if (_pool.size() > 0) {
            numberOfConditionals += grant.getWidth();
        }
        ConditionalBranch[] branches = new ConditionalBranch[numberOfConditionals];
        for (int i = 0; i < release.getWidth(); i++) {
            // The branch has channel i and ID i.
            branches[i] = new ConditionalReceive(release, i, i);
            if (_debugging && _VERBOSE_DEBUGGING) {
                branches[i].addDebugListener(this);
            }
        }
        if (_pool.size() > 0) {
            Token token = (Token) _pool.get(0);
            for (int i = release.getWidth(); i < numberOfConditionals; i++) {
                int channel = i - release.getWidth();
                branches[i] = new ConditionalSend(grant, channel, i, token);
                if (_debugging && _VERBOSE_DEBUGGING) {
                    branches[i].addDebugListener(this);
                }
            }
        }
        int successfulBranch = chooseBranch(branches);
        if (_debugging && _VERBOSE_DEBUGGING) {
            for (ConditionalBranch branche : branches) {
                branche.removeDebugListener(this);
            }
        }
        if (successfulBranch < 0) {
            _branchEnabled = false;
        } else if (successfulBranch < release.getWidth()) {
            // Rendezvous occurred with a release input.
            _branchEnabled = true;
            Token received = branches[successfulBranch].getToken();
            _pool.add(received);
            if (_debugging) {
                _debug("Resource released on channel " + successfulBranch
                        + ": " + received);
            }
        } else {
            // Rendezvous occurred with a grant output.
            _branchEnabled = true;
            if (_debugging) {
                _debug("Resource granted on channel "
                        + (successfulBranch - release.getWidth()) + ": "
                        + _pool.get(0));
            }
            _pool.remove(0);
        }
    }

    /** Return true unless none of the branches were enabled in
     *  the most recent invocation of fire().
     *  @return True if another iteration can occur.
     *  @exception IllegalActionException If thrown by the base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // FIXME: We deliberately ignore the return value of super.postfire()
        // here because CSPActor.postfire() returns false.
        super.postfire();
        // Note that CSPActor.postfire() also ignores the return value
        // AtomicActor.postfire(), which means that if a stop is
        // requested, then it is ignored.
        // However, if we check the value of AtomicActor._stopRequested
        // and return false if _stopRequested is true, then csp/test/auto/ResourcePool3.xml fails
        //if (!_stopRequested) {
        //    return false;
        //}

        return _branchEnabled;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The current resource pool. */
    private List _pool = new LinkedList();

    /** Indicator that a branch was successfully enabled in the fire() method. */
    private boolean _branchEnabled;

    /** Flag to set verbose debugging messages. */
    private static boolean _VERBOSE_DEBUGGING = true;
}
