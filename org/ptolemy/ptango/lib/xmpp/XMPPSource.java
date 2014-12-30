/* An actor that subscribes to an XMPP XEP-0060 node and fires upon an event.

 Copyright (c) 2012-2014 The Regents of the University of California.
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

package org.ptolemy.ptango.lib.xmpp;

import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// XMPPSource

/** An actor that subscribes to an XMPP XEP-0060 node and fires upon an event.
 *  An XMPP node is created by a user, where data can be published and subscribed.
 *  An XMPP node has a <i>nodeId</i>, which is set at creation time.
 *  This actor refers XMPP node via its <i>nodeId</i>.
 *  FIXME: comments, explain XEP-0060
 *  @see XMPPGateway
 *  @author Marten Lohstroh
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (marten)
 *  @Pt.AcceptedRating Red (marten)
 */
public class XMPPSource extends TypedAtomicActor implements XMPPSubscriber {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public XMPPSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        nodeId = new StringParameter(this, "nodeId");

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
        new Parameter(output, "_showName").setExpression("true");

        // Set flag indicating that initialize() has not yet been called.
        // Must be set prior to initialize() being called, so we set it in
        // the constructor.
        _hasInitialized = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** An output port for values received from the XMPP node.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** XMPPSource subscribes to an XMPP node, the name of which is
     *  specified by the value of nodeId.
     *  The nodeId is set when the XMPP node is created by a user.
     *  The default value is empty string.
     */
    public StringParameter nodeId;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>nodeId</i> parameter, then reset the
     *  state to the specified value.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If <i>nodeId</i> cannot be evaluated
     *   or cannot be converted to the output type, or if the superclass
     *   throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == nodeId) {
            _nodeId = ((StringToken) nodeId.getToken()).stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        XMPPSource newObject = (XMPPSource) super.clone(workspace);
        newObject.lock = new Object();
        return newObject;
    }

    /** Record the current model time and the current real time
     *  so that output events can be time stamped with the elapsed
     *  time since model start.
     *
     *  Declare as synchronized so the handlePublishedItems() method can check
     *  if initialize() has been called and, if not, wait for initialize() to
     *  be called.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public synchronized void initialize() throws IllegalActionException {
        super.initialize();
        _hasFired = false;
        _initializeModelTime = getDirector().getModelTime();
        _initializeRealTime = System.currentTimeMillis();
        _hasInitialized = true;
        _hasFired = false;
        this.notify();
    }

    /** Send the current value of the state of this actor to the output.
     *  Synchronized on a lock Object so that each data value will be handled
     *  before the next one is accepted, since incoming values call the
     *  handlePublishedItems() method from a different thread.
     *
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        synchronized (lock) {
            super.fire();
            output.send(0, new StringToken(_currentValue));

            // Notify the _handlePublishedItems() method that the actor has
            // fired, so that method can continue executing
            _hasFired = true;
            lock.notify();
        }
    }

    /** Return the nodeId.
     *  @return the nodeId.
     */
    @Override
    public String getNodeId() {
        return _nodeId;
    }

    /** Return the id of this subscriber.
     * @return the id of this subscriber.
     * @see #setSubId(String)
     */
    @Override
    public String getSubId() {
        return _subscriptionId;
    }

    /** Parse the published item from the XMPP node, save the XML and
     *  request that the actor be fired.
     *  This method is declared in the smack ItemEventListener interface.
     *  This method is called when an item is published.
     *
     *  NOTE: This method is synchronized, and the lock is _not_ released
     *  until the method is finished processing the current item event.  The
     *  first item event is completely processed before a second item event
     *  can be started.
     *
     *  @param items  The item event from the XMPP node.  Can contain
     *  multiple pieces of information.
     */
    @Override
    public synchronized void handlePublishedItems(ItemPublishEvent<Item> items) {
        // Check if initialize() has been called yet.  If not, wait, releasing
        // the lock so that initialize may be called.  Further invocations of
        // handlePublishedItems might also be called, but these will also wait
        // for initialize().
        while (!_hasInitialized) {
            try {
                this.wait(0);
            } catch (InterruptedException e) {
                // FIXME:  Do anything special if thread is interrupted?
                break;
            }
        }

        _handlePublishedItems(items);
    }

    /** Set _hasInitialized to false, so that invocations of
     *  handlePublishedItems (from a different thread) will wait until the model
     *  has been initialized on the next execution.
     */
    @Override
    public void wrapup() throws IllegalActionException {

        synchronized (lock) {
            super.wrapup();
            _hasInitialized = false;

            // Notify the handlePublishedItems() method to stop waiting to be
            // fired, since this actor will not be fire again in this iteration
            _hasFired = true;
            lock.notify();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Parse the published item from the XMPP node, save the XML and
     *  request that the actor be fired.  Synchronized on a lock object so
     *  this method will release the lock on the lock object so the fire()
     *  method may execute, but retain the lock on XMPPSource.this object
     *  so that future invocations of handlePublishedItems() must block until
     *  the current invocation is finished.
     *
     *  @param items  The item event from the XMPP node.  Can contain
     *  multiple pieces of information.
     */
    private void _handlePublishedItems(ItemPublishEvent<Item> items) {
        // The following codeblock is synchronized on the enclosing
        // actor. This lock _is_ released while waiting for the response,
        // allowing the fire method to execute its own synchronized blocks.
        synchronized (lock) {
            Object o;
            PayloadItem<?> item = (PayloadItem<?>) items.getItems().get(0);

            _hasFired = false;

            if ((o = item.getPayload()) instanceof SimplePayload) {
                SimplePayload payload = (SimplePayload) o;
                _currentValue = payload.toXML();
            } else {
                return; // published item has no recognized pay load
            }

            try {
                long elapsedRealTime = System.currentTimeMillis()
                        - _initializeRealTime;
                Time timeOfRequest = _initializeModelTime.add(elapsedRealTime);
                // Note that fireAt() will modify the requested firing time if it is in the past.
                getDirector().fireAt(XMPPSource.this, timeOfRequest);
            } catch (IllegalActionException e) {
                throw new InternalErrorException(this, e,
                        "Failed to fire at the current time.");
            }

            // Wait until this actor has been fired (thereby producing a token
            // with the received value on its output port) before this method
            // can be called again to receive a new value.

            // Note that other implementations are possible.  For example,
            // instead of blocking the caller until each value is handled,
            // this actor could implement a bounded queue and accepted up
            // to n values before blocking or before discarding values.

            // Note that the fire method purposefully does not block waiting for
            // a new value, so the actor might be fired multiple times with the
            // current value, for example if a token is received on the trigger
            // input port.  This ensures that XMPPSource will not block the
            // execution of the rest of the model - the DEDirector maintains
            // authority over execution.

            // wrapup() may be called before this actor is fired, meaning this
            // actor will not be fired.  In this situation, wrapup() sets
            // _hasFired to true and calls lock.notify() to wake up this thread
            // and allow the thread to finish
            while (!_hasFired) {
                try {
                    lock.wait(0);
                } catch (InterruptedException e) {
                    // FIXME:  Do anything special if thread is interrupted?
                    break;
                }
            }

        }
    }

    /** Register the gateway to the subscriber, so that if
     *  the subscription changes, it can notify the gateway.
     *  @param gateway The attribute responsible for managing the XMPP session.
     */
    @Override
    public void setGateway(XMPPGateway gateway) {

    }

    /** Set the id of the subscriber.
     *  @param subId A string identifying this subscriber.
     *  @see #getSubId()
     */
    @Override
    public void setSubId(String subId) {
        // XEP 0060 uses the term subId, not subscriberId.
        _subscriptionId = subId;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The XML payload from XMPP node. */
    private String _currentValue;

    /** A flag indicating if the actor has fired after a new value was received.
     */
    private boolean _hasFired;

    /** A flag indicating if initialize() was called.  handlePublishedItems()
     *  will wait if initialize() has not been called.
     */
    private boolean _hasInitialized = false;

    /** The model time at which this actor was last initialized. */
    private Time _initializeModelTime;

    /** The real time at which this actor was last initialized, in milliseconds. */
    private long _initializeRealTime;

    /** An object for the fire() and private _handlePublishedItems() methods to
     * use as a lock.  We avoid using XMPPSource as the lock object here since
     * the public handlePublishedItems uses XMPPSource as the lock object to
     * ensure one method call is completed before the next, and if we use the
     * same lock object for the private _handlePublishedItems method, this lock
     * will be released when wait() is called.  The actual value of this
     * variable is not used.
     */
    private Object lock = new Object();

    /** The cached value of nodeId parameter.
     *  @see #nodeId
     */
    private String _nodeId;

    /** The id of the subscriber. */
    private String _subscriptionId;
}
