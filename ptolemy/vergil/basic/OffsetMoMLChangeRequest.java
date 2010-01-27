/* A MoMLChangeRequest that offsets any objects that are created.

 Copyright (c) 2007-2010 The Regents of the University of California.
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
 2
 */
package ptolemy.vergil.basic;

import java.util.Iterator;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// OffsetMoMLChangeRequest
/** A mutation request specified in MoML that offsets any objects
 *  that are created in the toplevel.
 *  This class is used by the paste action in Vergil so that the
 *  pasted icon does not overlap the original icon.
 @author  Christopher Brooks, based on code from BasicGraphFrame by Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class OffsetMoMLChangeRequest extends MoMLChangeRequest {

    /** Construct a mutation request to be executed in the specified
     *  context.  The context is typically a Ptolemy II container,
     *  such as an entity, within which the objects specified by the
     *  MoML code will be placed.  This method resets and uses a
     *  parser that is a static member of this class.
     *  A listener to changes will probably want to check the originator
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  Alternatively, it can call waitForCompletion().
     *  All external references are assumed to be absolute URLs.  Whenever
     *  possible, use a different constructor that specifies the base.
     *  @param originator The originator of the change request.
     *  @param context The context in which to execute the MoML.
     *  @param request The mutation request in MoML.
     */
    public OffsetMoMLChangeRequest(Object originator, NamedObj context,
            String request) {
        super(originator, context, request);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected method                  ////

    /** Offset the locations of top level objects that are created
     *  by the change request.
     *
     *  @param parser The parser
     */
    protected void _postParse(MoMLParser parser) {
        Iterator topObjects = parser.topObjectsCreated().iterator();
        while (topObjects.hasNext()) {
            NamedObj topObject = (NamedObj) topObjects.next();
            try {
                Iterator locations = topObject.attributeList(Locatable.class)
                        .iterator();
                while (locations.hasNext()) {
                    Locatable location = (Locatable) locations.next();
                    double[] locationValue = location.getLocation();
                    for (int i = 0; i < locationValue.length; i++) {
                        locationValue[i] += _PASTE_OFFSET;
                    }
                    location.setLocation(locationValue);
                }
            } catch (IllegalActionException e) {
                MessageHandler.error("Change failed", e);
            }
        }
        parser.clearTopObjectsList();
    }

    /** Clear the list of top objects.
     *  @param parser The parser
     */
    protected void _preParse(MoMLParser parser) {
        super._preParse(parser);
        parser.clearTopObjectsList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /** Offset used when pasting objects. */
    private static int _PASTE_OFFSET = 10;

}
