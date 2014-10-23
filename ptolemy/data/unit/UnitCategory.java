/* A property that specifies the category of a base unit.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.data.unit;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// UnitCategory

/**
 A property that specifies the category of a base unit.  For example, in the
 International System of Units, the base unit meter has the category length.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 @see ptolemy.data.unit.BaseUnit
 */
public class UnitCategory extends Attribute {
    /** Construct a unit category in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public UnitCategory() {
        super();
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public UnitCategory(Workspace workspace) {
        super(workspace);
    }

    /** Construct a unit category property with the given name contained by
     *  the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *
     *  <p>This constructor adds the created object to the system wide
     *  UnitSystem by calling
     *  {@link UnitUtilities#registerUnitCategory(String)}
     *
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public UnitCategory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        UnitUtilities.registerUnitCategory(((BaseUnit) this.getContainer())
                .getName());
    }

    /** Return the base unit.
     *  @return The base unit.
     */
    public BaseUnit getBaseUnit() {
        return (BaseUnit) getContainer();
    }

    /** Set the container and register this object in to the system wide
     *  unit system by calling
     *  {@link UnitUtilities#registerUnitCategory(String)}.
     *  @param container The container to attach this attribute to.
     *  The type of the container must be an instances of BaseUnit.
     *  @exception IllegalActionException If Attribute.setContainer()
     *  throws it.
     *  @exception NameDuplicationException If Attribute.setContainer()
     *  throws it.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        super.setContainer(container);
        UnitUtilities.registerUnitCategory(((BaseUnit) this.getContainer())
                .getName());
    }
}
