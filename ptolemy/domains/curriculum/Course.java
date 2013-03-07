/* Base class for simple source actors.

 Copyright (c) 1998-2013 The Regents of the University of California.
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
//// Course

/**
 A course in a curriculum. This component customizes its interface
 by containing a DependencyHighlighter attribute. This attribute
 specifies a custom context menu that includes four commands for
 highlighting prerequisites and dependents.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Course extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  The postrequisites and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Course(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        postrequisites = new TypedIOPort(this, "postrequisites", false, true);
        postrequisites.setTypeEquals(BaseType.BOOLEAN);

        prerequisites = new TypedIOPort(this, "prerequisites", true, false);
        prerequisites.setMultiport(true);
        prerequisites.setTypeEquals(BaseType.BOOLEAN);

        Parameter hide = new Parameter(this, "_hideName");
        hide.setVisibility(Settable.EXPERT);

        /*DependencyHighlighter controller =*///new DependencyHighlighter(this, "_controller");
        units = new Parameter(this, "units");
        units.setTypeEquals(BaseType.INT);
        units.setExpression("4");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The postrequisites port. */
    public TypedIOPort postrequisites = null;

    /** The prerequisites port. */
    public TypedIOPort prerequisites = null;

    /** The number of units. This is an integer with default value 4. */
    public Parameter units;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
}
