/* Base class for simple source actors.

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
package ptolemy.domains.space;

import ptolemy.actor.lib.database.DatabaseSelect;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// Room

/**
 A Room.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Room extends DatabaseSelect {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Room(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        building = new StringParameter(this, "building");
        building.setExpression("Cory");

        room = new StringParameter(this, "room");
        room.setExpression("545Q");

        Parameter hide = new Parameter(trigger, "_hide");
        hide.setExpression("true");

        columns.setVisibility(Settable.EXPERT);
        columns.setExpression("{bldg=string, room=string, lname=string, "
                + "fnames=string, deskno=string, spaceid=string, classcd=string, "
                + "sponsorlname=string, email=string, spacenotes=string, "
                + "occupancy=string, departure=string}");

        hide = new Parameter(columns.getPort(), "_hide");
        hide.setExpression("true");

        pattern.setVisibility(Settable.EXPERT);
        hide = new Parameter(pattern.getPort(), "_hide");
        hide.setExpression("true");

        orderBy.setVisibility(Settable.EXPERT);
        orderBy.setExpression("deskno asc");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Name of the building. */
    public StringParameter building;

    /** Name of the room. */
    public StringParameter room;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the occupants from the database and produce them on the output
     *  port.
     *  @exception IllegalActionException If the database query fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        pattern.setExpression("trim(bldg)='" + building.stringValue()
                + "' and trim(room)='" + room.stringValue() + "'");
        super.fire();
    }
}
