/* A unit system as defined by a set of base and derived units.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@Pt.ProposedRating Red (liuxj@eecs.berkeley.edu)
@Pt.AcceptedRating Red (liuxj@eecs.berkeley.edu)
*/

package ptolemy.data.unit;

import ptolemy.data.expr.ScopeExtendingAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// UnitSystem
/**
A unit system as defined by a set of base and derived units.
<p>
The various measurement units of a unit system are represented by the
parameters of an instance of UnitSystem.
The units belong to a number of categories, such as length and time
in the International System of Units (SI). Each category has a base unit,
for example meter in the length category.
<p>
Several basic unit systems are provided with Ptolemy II. They are specified
using MoML. Customized unit systems can be created following these examples.

@author Xiaojun Liu
@version $Id$
@since Ptolemy II 2.0
*/

public class UnitSystem extends ScopeExtendingAttribute {

    // FIXME: these issues should be addressed (cxh 8/02)
    // 1. The entire notion of a category being indexed into a vector by an
    // integer is a little strange.  I don't think we have quite the right
    // storage structure here.  In anycase, it would be good to
    // discuss the implementation details in a comment inside the UnitSystem
    // method
    //
    // 2. Having static data makes for somewhat less robust code, especially
    // when there is no way to reset the static data, which is why I added a
    // reset() method.  UnitSystem has no way of removing a unit from
    // the static structures - what do I do if I screw up and add a bad unit?

    /** Construct a unit system with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public UnitSystem(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
}
