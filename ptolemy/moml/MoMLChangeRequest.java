/* A mutation request specified in MoML.

 Copyright (c) 2000 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.moml;

import ptolemy.kernel.event.ChangeRequest;
import ptolemy.kernel.event.ChangeFailedException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// MoMLChangeRequest
/**
A mutation request specified in MoML.

@author  Edward A. Lee
@version $Id$
*/
public class MoMLChangeRequest extends ChangeRequest {

    /** Construct a mutation request for the specified parser.
     *  @param parser The parser to execute the request.
     *  @param request The mutation request in MoML.
     */
    public MoMLChangeRequest(MoMLParser parser, String request) {
        // NOTE: The first argument below is supposed to be the originator,
        // a Nameable object.  However, there seems to be no reasonable
        // originator in this case.  Perhaps the base class should offer
        // a constructor that does not require this argument?
        // The second argument is a description of the change request.
        super(null, request);
        _parser = parser;
    }

    /** Construct a mutation request for the specified top-level entity.
     *  This method creates a new parser to handle the request.
     *  @param toplevel The top-level entity to change.
     *  @param request The mutation request in MoML.
     */
    public MoMLChangeRequest(NamedObj toplevel, String request) {
        // NOTE: The first argument below is supposed to be the originator,
        // a Nameable object.  However, there seems to be no reasonable
        // originator in this case.  Perhaps the base class should offer
        // a constructor that does not require this argument?
        // The second argument is a description of the change request.
        super(null, request);
        _parser = new MoMLParser();
        _parser.setToplevel(toplevel);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change by evaluating the request using the
     *  specified parser.
     *  @exception ChangeFailedException If an exception is thrown
     *   while evaluating the request.
     */
    public void execute() throws ChangeFailedException {
        try {
	    _parser.parse(getDescription());
        } catch (Exception ex) {
            throw new ChangeFailedException(this, ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The parser given in the constructor.
    private MoMLParser _parser;
}
