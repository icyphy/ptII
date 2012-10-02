/* Base class for simple source actors.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
package ptolemy.domains.curriculum;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// Or

/**
 This is a rather uninteresting component used to indicate
 an alternative between prerequisites.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Or extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  The postrequisites and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Or(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        postrequisites = new TypedIOPort(this, "postrequisites", false, true);
        postrequisites.setTypeEquals(BaseType.BOOLEAN);

        prerequisites = new TypedIOPort(this, "prerequisites", true, false);
        prerequisites.setMultiport(true);
        prerequisites.setTypeEquals(BaseType.BOOLEAN);

        Parameter hide = new Parameter(this, "_hideName");
        hide.setVisibility(Settable.EXPERT);

        _attachText("_iconDescription", "<svg>\n"
                + "<circle cx=\"0\" cy=\"0\" r=\"8\""
                + "style=\"fill:black\"/>\n" + "<text x=\"-7\" y=\"3\" "
                + "style=\"font-size:11;font-family:sans-serif;fill:white\">\n"
                + "Or" + "</text>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The postrequisites port.
     */
    public TypedIOPort postrequisites = null;

    /** The prerequisites port.
     */
    public TypedIOPort prerequisites = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
}
