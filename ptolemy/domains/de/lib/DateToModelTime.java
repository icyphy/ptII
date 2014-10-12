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

import java.util.Collections;
import java.util.HashSet;

import ptolemy.actor.Director;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DateToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A timed actor that outputs the model time that corresponds to the date.
 * @author Patricia Derler
@version $Id$
@since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DateToModelTime extends Transformer {

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
    public DateToModelTime(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DATE);
        output.setTypeEquals(BaseType.DOUBLE);
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

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                DateToken token = (DateToken) input.get(i);
                Time fireTime = new Time(
                        _director,
                        (token.getCalendarInstance().getTimeInMillis() - _director
                        .getRealStartTimeMillis())
                        * _director.localClock.getTimeResolution());
                output.send(0, new DoubleToken(fireTime.getDoubleValue()));
            }
        }
       

    }

    private DEDirector _director;

}
