/* Record all input tokens for later querying.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Recorder
/**
Record all input tokens for later querying.  This actor can be used for
testing configurations of actors.  It can also be used in programs that
invoke Ptolemy models and wish to query the results after the model
is run.  The input tokens are read in the postfire() method so that
in domains with fixed-point semantics, only the final, settled value
is recorded.  The current time is also recorded for each value.
<p>
The <i>capacity</i> parameter limits the size of the record.
If the capacity is set to zero, then no tokens are recorded, but
the total number of input tokens is counted.  You can access
the count via the getCount() method.  If the capacity is 1,
then only the most recently seen token on each channel is recorded.
If the capacity is negative (the default), then the capacity
is infinite.

@author Edward A. Lee
@version $Id$
*/

public class Recorder extends Sink {

    /** Construct an actor with an input multiport that can accept any
     *  Token.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Recorder(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        capacity = new Parameter(this, "capacity", new IntToken(-1));
        capacity.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The capacity of the record for each channel.
     *  This parameter must contain an IntToken.
     */
    public Parameter capacity;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>input</code>
     *  variable to equal the new port.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        Recorder newobj = (Recorder)super.clone(ws);
        newobj.capacity = (Parameter)newobj.getAttribute("capacity");
        return newobj;
    }

    /** Get the total number of events seen so far.
     *  @return The total number of events seen so far.
     */
    public int getCount() {
        return _count;
    }

    /** Get the history for the specified channel number.  If in any
     *  firing there is no such channel, or no token was read on that
     *  channel, then a string token with value "_" is returned in the
     *  position of the list corresponding to that firing.
     *  If nothing has been recorded (there have been no firings),
     *  then return an empty list.
     *  @param channel The input channel for which the history is desired.
     *  @return A list of Token objects.
     */
    public List getHistory(int channel) {
        ArrayList result = new ArrayList();
        if (_records != null) {
            result.ensureCapacity(_records.size());
            Iterator firings = _records.iterator();
            while (firings.hasNext()) {
                Token[] record = (Token[])firings.next();
                if (channel < record.length) {
                    if (record[channel] != null) {
                        result.add(record[channel]);
                        continue;
                    }
                }
                result.add(_bottom);
            }
        }
        return result;
    }

    /** Get the latest input for the specified channel.
     *  If there has been no record yet for the specified channel,
     *  then return the string token "_", representing "bottom".
     *  @param channel The input channel for the record is desired.
     *  @return The latest input token.
     */
    public Token getLatest(int channel) {
        if (_latest == null || channel >= _latest.length ||
                _latest[channel] == null) {
            return(_bottom);
        }
        return (_latest[channel]);
    }

    /** Get the record for the specified channel number.  If in any
     *  firing there is no such channel, or no token was read on that
     *  channel, then a string token with value "_" is returned.
     *  If nothing has been recorded (there have been no firings),
     *  then return an empty enumeration.
     *  @param channel The input channel for the record is desired.
     *  @return An enumeration of Token objects.
     *  @deprecated This method is deprecated. Use getHistory().
     */
    public Enumeration getRecord(int channel) {
        return Collections.enumeration(getHistory(channel));
    }

    /** Get the history of the time of each invocation of postfire().
     *  @return A list of Double objects.
     */
    public List getTimeHistory() {
        return _timeRecord;
    }

    /** Get the record of the current time of each invocation of postfire().
     *  @return An enumeration of Double objects.
     *  @deprecated This method is deprecated. Use getTimeHistory().
     */
    public Enumeration getTimeRecord() {
        return Collections.enumeration(_timeRecord);
    }

    /** Initialize the lists used to record input data.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _records = new LinkedList();
        _timeRecord = new LinkedList();
        _latest = null;
        _count = 0;
    }

    /** Read at most one token from each input channel and record its value.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        int width = input.getWidth();
        Token[] record = new Token[width];
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                record[i] = token;
                _count++;
            }
        }
        int capacityValue = ((IntToken)(capacity.getToken())).intValue();
        if (capacityValue != 0) {
            _records.add(record);
            _timeRecord.add(new Double(getDirector().getCurrentTime()));
            if (capacityValue > 0 && _records.size() > capacityValue) {
                // Remove the first element.
                _records.remove(0);
                _timeRecord.remove(0);
            }
        }
        _latest = record;
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Count of events seen.
    private int _count = 0;

    // A linked list of arrays.
    private List _records;

    // The most recent set of inputs.
    Token[] _latest;

    // A linked list of Double objects, which are times.
    private List _timeRecord;

    // A token to indicate absence.
    private static Token _bottom = new StringToken("_");
}
