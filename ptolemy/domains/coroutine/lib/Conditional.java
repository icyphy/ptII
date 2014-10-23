/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package ptolemy.domains.coroutine.lib;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.coroutine.kernel.AtomicContinuationActor;
import ptolemy.domains.coroutine.kernel.ControlEntryToken;
import ptolemy.domains.coroutine.kernel.ControlExitToken;
import ptolemy.domains.coroutine.kernel.ControlExitToken.ExitLocation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * Conditional class.
 *
 * @author shaver
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Conditional extends AtomicContinuationActor {

    public Conditional() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Conditional(Workspace workspace) {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    public Conditional(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.ContinuationActor#enter(ptolemy.domains.coroutine.kernel.ControlEntryToken)
     */
    @Override
    public ControlExitToken controlEnter(ControlEntryToken entry)
            throws IllegalActionException {

        ControlExitToken extk = null;
        int insize = _inputs.getWidth();

        Boolean val = true;

        try {
            for (int k = 0; k < insize; ++k) {
                Token inToken = _inputs.get(k);
                if (inToken instanceof BooleanToken) {
                    val = val && ((BooleanToken) inToken).booleanValue();
                }
            }
            extk = ControlExitToken.Exit(val ? trueExit : falseExit);
        } catch (NoRoomException e) {
            e.printStackTrace();
        } catch (NoTokenException e) {
            e.printStackTrace();
        }

        if (extk != null) {
            return extk;
        } else {
            return super.controlEnter(entry);
        }
    }

    public TypedIOPort _inputs;

    ///////////////////////////////////////////////////////////////////

    protected void _init() throws IllegalActionException,
    NameDuplicationException {

        addExitLocation(trueExit);
        addExitLocation(falseExit);

        _inputs = new TypedIOPort(this, "Inputs", true, false);
        _inputs.setTypeEquals(BaseType.BOOLEAN);
    }

    final public ExitLocation trueExit = new ExitLocation("true");
    final public ExitLocation falseExit = new ExitLocation("false");

}
