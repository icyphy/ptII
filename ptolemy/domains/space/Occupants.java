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

import java.util.Arrays;
import java.util.Comparator;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.BoxedValueIcon;

//////////////////////////////////////////////////////////////////////////
//// Occupants

/**
 A Occupants display actor.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Occupants extends TypedAtomicActor {

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
        
        occupants = new TypedIOPort(this, "occupants", true, false);
        // Force the type to contain at least the required fields.
        /*
        Parameter prototype = new Parameter(this, "prototype");
        prototype.setPersistent(false);
        prototype.setVisibility(Settable.NONE);
        prototype.setExpression("[{LastName=string}]");
        occupants.setTypeAtMost(prototype.getType());
        */
        contents = new StringParameter(this, "contents");
        // contents.setVisibility(Settable.EXPERT);
        
        BoxedValueIcon icon = new BoxedValueIcon(this, "_icon");
        icon.displayHeight.setExpression("100");
        icon.displayWidth.setExpression("20");
        icon.attributeName.setExpression("contents");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Port that receives the occupants of a Room.
     *  The type is an array of records.
     */
    public TypedIOPort occupants;
    
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
        // FIXME: We want to parameterize what is shown.
        if (occupants.hasToken(0)) {
            ArrayToken array = (ArrayToken)occupants.get(0);
            
            // Sort the array by desk number.
            Token[] desks = array.arrayValue();
            // FIXME: It would be nice to use the type safe version here.
            // But what is the syntax?
            Arrays.sort(desks, new DeskComparator());
            
            StringBuffer display = new StringBuffer();
            for (int i = 0; i < desks.length; i++) {
                RecordToken record = (RecordToken)desks[i];
                if (i > 0) {
                    display.append("\n");
                }
                String desk = sanitize(
                        record.get("deskno").toString(),
                        "?");
                display.append(desk);
                display.append(": ");
                String name = sanitize(
                        record.get("lname").toString(),
                        "VACANT");
                display.append(name);
            }
            String moml = "<property name=\"contents\" value=\""
                + StringUtilities.escapeForXML(display.toString())
                + "\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this, this, moml);
            requestChange(request);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a string, remove quotation marks if it has them.
     *  If the string is then empty, return the specified default.
     *  Otherwise, return the string without quotation marks.
     *  This method also trims white space, unless the white
     *  space is inside quotation marks.
     *  @param string String to sanitize.
     *  @param ifEmpty Default to use if result is empty.
     *  @return A string with no quotation marks that is not empty.
     */
    private String sanitize(String string, String ifEmpty) {
        string = string.trim();
        if (string.startsWith("\"") && string.endsWith("\"")) {
            string = string.substring(1, string.length() - 1);
        }
        if (string.trim().equals("")) {
            string = ifEmpty;
        }
        return string;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    /** Compare two record token by desk number. */
    private class DeskComparator implements Comparator {
        // FIXME: It would be nice to use the typesafe version,
        // but what is the syntax? This should operate on RecordToken.
        public int compare(Object desk1, Object desk2) {
            String desk1no = sanitize(
                    ((RecordToken)desk1).get("deskno").toString(),
                    "0");
            int desk1int = Integer.parseInt(desk1no);
            String desk2no = sanitize(
                    ((RecordToken)desk2).get("deskno").toString(),
                    "0");
            int desk2int = Integer.parseInt(desk2no);
            // FIXME
            System.out.println("HELP");
            if (desk1int < desk2int) {
                return -1;
            } else if (desk1int > desk2int) {
                return 1;
            } else {
                return 0;
            }
        }
        public boolean equals(Object obj) {
            return 0 == compare(this, obj);
        }
    }
}
