/* Handle a storm.

   Copyright (c) 2015 The Regents of the University of California.
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

package ptolemy.domains.atc.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

////////////////////////////////////////////////////////////////
/// StormHandling

/** Handle a storm.
 *  @author Maryam Bagheri
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class StormHandling extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StormHandling(CompositeEntity container, String name)
            throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        selectedTrack = new TypedIOPort(this, "selectedTrack", true, false);
        selectedTrack.setTypeEquals(BaseType.INT);
        selectedValue = new TypedIOPort(this, "selectedValue", true, false);
        selectedValue.setTypeEquals(BaseType.BOOLEAN);
        numberOfTracks = new Parameter(this, "numberOfTracks", new IntToken(0));
        numberOfTracks.setTypeEquals(BaseType.INT);
        trackStatus=new TypedIOPort(this,"trackStatus",false,true);
        trackStatus.setTypeEquals(new ArrayType(BaseType.BOOLEAN));
    }


    /** An integer indicating the selected track. */
    public TypedIOPort selectedTrack;

    /** An boolean indicating the selected value. */
    public TypedIOPort selectedValue;

    /** The track status. */
    public TypedIOPort trackStatus;

    /** The number of tracks. */
    public Parameter numberOfTracks;

    /** Initialize this actor.  Derived classes override this method
     *  to perform actions that should occur once at the beginning of
     *  an execution, but after type resolution.  Derived classes can
     *  produce output data and schedule events.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _count = ((IntToken)numberOfTracks.getToken()).intValue();
        _temp=new Token[_count];
        for (int i=0;i<_count;i++)
            _temp[i]=(Token) new BooleanToken(false);
    }

    /** Fire the actor.
     *  @exception IllegalActionException If thrown by the baseclass
     *  or if there is a problem accessing the ports or parameters.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int trackNumber=0;
        Token value;
        if (selectedValue.hasToken(0)) {
            value=selectedValue.get(0);
            if (selectedTrack.hasToken(0)) {
                trackNumber=((IntToken)selectedTrack.get(0)).intValue();
                _temp[trackNumber-1]=value;
            }

        }
        trackStatus.send(0, (Token)(new ArrayToken(BaseType.BOOLEAN, _temp)));
    }

    private int _count = 0;
    private Token[] _temp;
}
