/* EventDictionary is a dictionary that associates the event name and the event ID.

 Copyright (c) 2012-2013 The Regents of the University of California.
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

package ptolemy.domains.metroII.kernel;

import java.util.Hashtable;

///////////////////////////////////////////////////////////////////
////EventDictionary

/**
 * <p>
 * EventDictionary is a dictionary that associates the event name and the event
 * ID. The event name is the key and the event ID is the value. When a new event
 * name is added into the dictionary, a new id is created and associated with
 * the added event name.
 * </p>
 *
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class EventDictionary implements Cloneable {

    /**
     * Construct an EventDictionary.
     */
    public EventDictionary() {
        _eventName2ID = new Hashtable<String, Integer>();
    }

    /**
     * Clone an EventDictionary.
     *
     * @exception CloneNotSupportedException
     *             the object's class does not implement the Cloneable
     *             interface.
     */
    @Override
    public EventDictionary clone() throws CloneNotSupportedException {
        EventDictionary newObject = (EventDictionary) super.clone();
        newObject._eventName2ID = (Hashtable<String, Integer>) _eventName2ID
                .clone();
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the event ID associated with the event name. If the event name is
     * not in the dictionary, return -1;
     *
     * @param name
     *            Event name
     * @return Event ID
     */
    public int getID(String name) {
        if (_eventName2ID.containsKey(name)) {
            return _eventName2ID.get(name);
        } else {
            return -1;
        }
    }

    /**
     * Add a new event name into the dictionary. If the name is already in the
     * dictionary, do nothing. If the name is new, add the name and create a new
     * ID. Associate the ID the the name.
     *
     * @param name
     *            Event name
     */
    public void add(String name) {
        if (!_eventName2ID.containsKey(name)) {
            _eventName2ID.put(name, _nextAvailableID);
            _nextAvailableID++;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /**
     * The next available event ID. If an new event is proposed, the
     * _nextAvailableID is assigned to the new event and _nextAvailableID is
     * increased by one.
     */
    private int _nextAvailableID = 0;

    /**
     * The dictionary of event name and ID pair.
     *
     */
    private Hashtable<String, Integer> _eventName2ID;
}
