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
package ptolemy.domains.coroutine.kernel;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * AccumLUB class.
 *
 * @author shaver
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class AccumLUB extends TypedAtomicActor {

    public AccumLUB() {
        super();
    }

    public AccumLUB(Workspace workspace) {
        super(workspace);
    }

    public AccumLUB(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.AtomicActor#fire()
     */
    // TODO This assumes consistency. I should check for it.
    @Override
    public void fire() throws IllegalActionException {
        int width = _ins.getWidth();
        for (int i = 0; i < width; ++i) {
            if (!_ins.isKnown(i)) {
                continue;
            }
            _out.broadcast(_ins.hasToken(i) ? _ins.get(i) : null);
            break;
        }
    }

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        _ins = new TypedIOPort(this, "Ins", true, false);
        _out = new TypedIOPort(this, "Out", false, true);
        _ins.setMultiport(true);
    }

    public TypedIOPort _ins;
    public TypedIOPort _out;

}
