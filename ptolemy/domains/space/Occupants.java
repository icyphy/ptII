/* Base class for simple source actors.

 Copyright (c) 1998-2007 The Regents of the University of California.
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

import java.util.Iterator;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.BoxedValueIcon;

//////////////////////////////////////////////////////////////////////////
//// Occupants

/**
 A Occupants.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Occupants extends AtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Occupants(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        occupants = new IOPort(this, "occupants", false, true);
        
        contents = new StringParameter(this, "contents");
        // contents.setVisibility(Settable.EXPERT);
        
        BoxedValueIcon icon = new BoxedValueIcon(this, "_icon");
        icon.displayHeight.setExpression("100");
        icon.displayWidth.setExpression("20");
        icon.attributeName.setExpression("contents");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Port through which to access the occupants of a Room. */
    public IOPort occupants;
    
    /** Parameter to store the occupants. */
    public StringParameter contents;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** If connected to a Room, retrieve its occupants and update
     *  the display.
     *  @throws IllegalActionException If we fail to update the
     *   contents parameter.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        // The only port is occupants, so we just use that.
        Iterator sinks = occupants.sinkPortList().iterator();
        while (sinks.hasNext()) {
            IOPort remotePort = (IOPort)sinks.next();
            NamedObj remoteActor = remotePort.getContainer();
            if (remoteActor instanceof Room) {
                // FIXME: We want to parameterize what is shown.
                // For now, we just pick the last name.
                ArrayToken array = ((Room)remoteActor).occupants();
                // Return value is null if database update fails or
                // is canceled. Have to ensure other actors don't fire.
                // FIXME: Throwing an exception is not very friendly.
                if (array == null) {
                    throw new IllegalActionException(this, "Update canceled.");
                }
                StringBuffer display = new StringBuffer();
                for (int i = 0; i < array.length(); i++) {
                    RecordToken record = (RecordToken)array.getElement(i);
                    if (i > 0) {
                        display.append("\n");
                    }
                    String desk = record.get("Desk").toString().trim();
                    if (desk.startsWith("\"") && desk.endsWith("\"")) {
                        desk = desk.substring(1, desk.length() - 1);
                    }
                    if (desk.trim().equals("")) {
                        desk = "?";
                    }
                    display.append(desk);
                    display.append(": ");
                    String name = record.get("LastName").toString().trim();
                    if (name.startsWith("\"") && name.endsWith("\"")) {
                        name = name.substring(1, name.length() - 1);
                    }
                    if (name.trim().equals("")) {
                        name = "VACANT";
                    }
                    display.append(name);
                }
                String moml = "<property name=\"contents\" value=\""
                    + StringUtilities.escapeForXML(display.toString())
                    + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this, this, moml);
                requestChange(request);
            }
        }
    }
}
