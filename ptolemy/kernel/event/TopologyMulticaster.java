/* An aggregation of listeners.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.event;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// TopologyMulticaster
/**
A TopologyMulticaster forwards topology change events it
receives to a list of other listeners. This is typically used
by classes that provide addTopologyListener() and removeTologyListener()
methods.

@author John Reekie
@version $Id$
@see TopologyChangeRequest
*/
public class TopologyMulticaster implements TopologyListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new topology listener. Any time any of the
     * event notification methods is called, the call will be forwarded
     * to the added listener.
     *
     * @param listener An object that listens to topology events
     */
    public void addTopologyListener (TopologyListener listener) {
        _listeners.insertLast(listener);
    }

    /** Notify that an entity has been added to a composite.
     * The <b>compositeEntity</b> and <b>entity</b> fields of the
     * passed event will be valid.
     *
     * @param event The topology event
     */
    public void entityAdded (TopologyEvent event) {
        for (Enumeration e = _listeners.elements(); e.hasMoreElements(); ) {
            ((TopologyListener) e.nextElement()).entityAdded(event);
        }
    }

    /** Notify that an entity has been removed from a composite.
     * The <b>compositeEntity</b> and <b>entity</b> fields of the
     * passed event will be valid.
     *
     * @param event The topology event
     */
    public void entityRemoved (TopologyEvent event) {
        for (Enumeration e = _listeners.elements(); e.hasMoreElements(); ) {
            ((TopologyListener) e.nextElement()).entityRemoved(event);
        }
    }

    /** Notify that a port has been added to an entity.
     * The <b>entity</b> and <b>port</b> fields of the
     * passed event will be valid.
     *
     * @param event The topology event
     */
    public void portAdded (TopologyEvent event) {
        for (Enumeration e = _listeners.elements(); e.hasMoreElements(); ) {
            ((TopologyListener) e.nextElement()).portAdded(event);
        }
    }

    /** Notify that a port has been removed from a entity.
     * The <b>entity</b> and <b>port</b> fields of the
     * passed event will be valid.
     *
     * @param event The topology event
     */
    public void portRemoved (TopologyEvent event) {
        for (Enumeration e = _listeners.elements(); e.hasMoreElements(); ) {
            ((TopologyListener) e.nextElement()).portRemoved(event);
        }
    }

    /** Remove a topology listener. The listener will
     * no longer be notified of topology events. If the
     * given listener has not been previously registered
     * with addTopologyListener() (or is null), then do
     * nothing.
     *
     * @param listener An object that listens to topology events
     */
    public void removeTopologyListener (TopologyListener listener) {
        _listeners.removeOneOf(listener);
    }

    /** Notify that a relation has been added to a composite.
     * The <b>compositeEntity</b> and <b>relation</b> fields of the
     * passed event will be valid.
     *
     * @param event The topology event
     */
    public void relationAdded (TopologyEvent event) {
        for (Enumeration e = _listeners.elements(); e.hasMoreElements(); ) {
            ((TopologyListener) e.nextElement()).relationAdded(event);
        }
    }

    /** Notify that a relation has been removed from a composite.
     * The <b>compositeEntity</b> and <b>relation</b> fields of the
     * passed event will be valid.
     *
     * @param event The topology event
     */
    public void relationRemoved (TopologyEvent event) {
        for (Enumeration e = _listeners.elements(); e.hasMoreElements(); ) {
            ((TopologyListener) e.nextElement()).relationRemoved(event);
        }
    }

    /** Notify that a port has been linked to a relation.
     * The <b>relation</b> and <b>port</b> fields of the
     * passed event will be valid.
     *
     * @param event The topology event
     */
    public void portLinked (TopologyEvent event) {
        for (Enumeration e = _listeners.elements(); e.hasMoreElements(); ) {
            ((TopologyListener) e.nextElement()).portLinked(event);
        }
    }

    /** Notify that a port has been unlinked from a relation.
     * The <b>relation</b> and <b>port</b> fields of the
     * passed event will be valid.
     *
     * @param event The topology event
     */
    public void portUnlinked (TopologyEvent event) {
        for (Enumeration e = _listeners.elements(); e.hasMoreElements(); ) {
            ((TopologyListener) e.nextElement()).portUnlinked(event);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    // FIXME: use ArrayList when we port to JDK1.2
    private LinkedList _listeners = new LinkedList();
}
