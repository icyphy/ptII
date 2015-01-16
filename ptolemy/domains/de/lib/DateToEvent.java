/* A timed actor that outputs a const value at a given date.

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

package ptolemy.domains.de.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DateToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A timed actor that excepts DateTokens on the input. If the date in 
 *  the DateToken is in the future, the output is generated at this 
 *  future date. If the date is in the past, an exception is thrown. 
 *  The output is a DateToken with the system time obtained when producing
 *  the output. Internally, the input date is converted to model time, which
 *  is used in the local clock of the director. 
 *  Because we are using system time here, the output time will not be
 *  exact and the output DateToken will not be exactly the same as the
 *  DateToken received on the input.
 *  
 *  Using this actor makes sense in models that synchronize to real time
 *  (e.g. in the DE domain by enabling the synchronizeToRealTime property).
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DateToEvent extends Transformer {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public DateToEvent(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DATE);
        output.setTypeEquals(BaseType.DATE);
    }

    /** Check weather enclosing director is a DEDirector with
     *  synchronizeToRealTime is enabled.
     *  @exception IllegalActionException Thrown if the enclosing director is not a
     *  DEDirector or the synchronizeToRealTime property is false.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        Director director = getDirector();
        if (director instanceof DEDirector) {
            if (!((BooleanToken) ((DEDirector) director).synchronizeToRealTime
                    .getToken()).booleanValue()) {
                throw new IllegalActionException(
                        this,
                        "This actor can only be used when synchronizeToRealTime "
                                + "in the director is enabled because a reference to real time is needed to compare "
                                + "dates.");
            }
            _director = (DEDirector) director;
        }
    }

    /** Read date tokens from the input and store them until the real
     *  time equals the date in the token. If the date token on the input
     *  contains a date in the past, an exception is thrown.
     *  @exception IllegalActionException Thrown if the input date in the date 
     *  token lies in the past.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        long systemTime = System.currentTimeMillis();
        Time time = _director.getModelTime();
        if (_outputTokensForChannel != null && _outputTokensForChannel.size() > 0) {
            Time t = (Collections.min(_outputTokensForChannel.keySet()));
            if (t.compareTo(time) == 0) {
                List<Integer> channels = _outputTokensForChannel.get(t);
                for (int i = 0; i < channels.size(); i++) {
                    output.send(channels.get(i), new DateToken(systemTime));
                }
                _outputTokensForChannel.remove(t);
            }
        }
        for (int channel = 0; channel < input.getWidth(); channel++) {
            if (input.hasToken(0)) {
                DateToken token = (DateToken) input.get(0);
                if (token.getCalendarInstance().getTimeInMillis() < systemTime) {
                    throw new IllegalActionException(this,
                            "The date on the input port ("
                            + token.toString()
                            + ") lies in the past.");
                } else {
                    long realTimeDifferenceInMillis = token.getCalendarInstance().getTimeInMillis() - _director
                    .getRealStartTimeMillis();
                    Time fireTime = new Time(
                            _director,
                            ((double) realTimeDifferenceInMillis / 1000)); // The default unit of time is seconds.
                    _director.fireAt(this, fireTime);
                    if (_outputTokensForChannel == null) {
                        _outputTokensForChannel = new HashMap<Time, List<Integer>>();
                    }
                    List<Integer> channels = _outputTokensForChannel.get(fireTime);
                    if (channels == null) {
                        channels = new ArrayList<Integer>();
                    }
                    channels.add(channel);
                    _outputTokensForChannel.put(fireTime, channels);
                }
            }
        }
    }

    private HashMap<Time, List<Integer>> _outputTokensForChannel;

    private DEDirector _director;

}
