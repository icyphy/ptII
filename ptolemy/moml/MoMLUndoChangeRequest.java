/* A request to undo the most recent MoML mutation request.

 Copyright (c) 2000-2003 The Regents of the University of California.
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
@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
// Review base URL stuff.
*/

package ptolemy.moml;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// MoMLUndoChangeRequest
/**
A mutation request specified in MoML to undo/redo the most recent MoML
change request. To request that the last operation is undone, simply
simply create. To request that the last undo change is redone,
create an instance of this class, and invoke the method setRedoable().
<p>
This class provides the preferred mechanism for undoing/redoing mutations
on a model while it is executing. It is primarily intended for use with
user interfaces (Vergil) so that a user can undo editing actions.
<p>
To use it to undo a previous action, create an instance of this class.
Then queue the instance of this class with a composite entity by calling
its requestChange() method.
<p>
To use it to redo a previously undone action, create an instance of this class.
and invoke the method setRedoable() on the created instance. Then queue the
instance of this class with a composite entity by calling its
requestChange() method.
<p>
See MoMLChangeRequest for subtleties regarding the use of MoML change
requests,, as some of the same issues apply to undoing those changes.
<p>
@author  Neil Smyth
@version $Id$
@since Ptolemy II 2.1
*/
public class MoMLUndoChangeRequest extends ChangeRequest {

    /** Construct a mutation request to be executed in the specified context.
     *  The context is typically a Ptolemy II container, such as an entity,
     *  within which the objects specified by the MoML code will be placed.
     *  If the top-level containing the specified context has a
     *  ParserAttribute, then the parser associated with that attribute
     *  is used.  Otherwise, a new parser is created, and set to be the
     *  top-level parser.
     *  A listener to changes will probably want to check the originator
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  Alternatively, it can call waitForCompletion(), although there
     *  is severe risk of deadlock when doing that.
     *  <p>
     *  Note that for undo requests a context is required so that
     *  there is a parser to execute the undo on.
     *  <p>
     *  @param originator The originator of the change request.
     *  @param context The context in which to execute the MoML.
     */
    public MoMLUndoChangeRequest(Object originator, NamedObj context) {
        super(originator, "Request to undo/redo most recent MoML change");
        _context = context;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the first container, moving up the hierarchy, for which there
     *  are other objects that defer their MoML definitions to it.
     *  If there is no such container, then return null. If the specified
     *  object has other objects deferring to it, then return the specified
     *  object.
     *  @return An object that deeply contains this one, or null.
     */
    public static NamedObj getDeferredToParent(NamedObj object) {
        if (object == null) {
            return null;
        } else {
            List deferList = object.getMoMLInfo().deferredFrom;
            if(deferList != null && deferList.size() > 0) {
                return object;
            } else {
                return getDeferredToParent((NamedObj)object.getContainer());
            }
        }
    }

    /** Mark this change request as a redo, thus negating the effect of
     *  the last undo action.
     */
     public void setRedoable() {
        _undoIsRedo = true;
     }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the change by evaluating the request using the
     *  specified parser.
     *  @exception Exception If an exception is thrown
     *   while evaluating the request.
     */
    protected void _execute() throws Exception {
        // Check to see whether there is a parser...
        if (_context == null) {
            // If there is no context, then there is no parser to
            // carry out an undo on!
            // FIXME: raise an exception!
            System.out.println("FIXME: require a context to carry out an undo");
            return;
        }
        // Get the parser to request an undo on...
        NamedObj toplevel = _context.toplevel();
        ParserAttribute parserAttribute =
                (ParserAttribute)toplevel.getAttribute("_parser");
        // No parser associated with this model??
        if (parserAttribute == null) {
            // FIXME: what to do? Given a context, but it has no parser
            // assocaited with it!
            System.out.println("FIXME: Undo request on a model with no " +
                               "assocaited parser");
            return;
        }
        _parser = parserAttribute.getParser();
        _parser.reset();

         // NOTE: To see what is being parsed, uncomment the following:
        /*
        System.out.println("****** Executing Undo MoML change:");
        System.out.println(getDescription());
        if (_context != null) {
            System.out.println("------ in context " + _context.getFullName());
        }
        */

        try {
            // NOTE: the parser needs an entry point into the model to get the
            // undo information, so pass in the NamedObj we have to allow
            // the parser to find the undo/redo information. However it is
            // the context from the UndoEntry that is used to carry out the
            // undo/redo
            _parser.setContext(_context);
            _parser._propagating = _propagating;
            // Request the undo/redo!
            if (!_undoIsRedo) {
                _parser.undo();
            } else {
                _parser.redo();
            }
        } finally {
            _parser._propagating = false;
        }

        // Apply the same change to any object that defers its MoML
        // definition to the context in which we just applied the change.
        List othersList = _context.getMoMLInfo().deferredFrom;
        if (othersList != null) {
            Iterator others = othersList.iterator();
            while (others.hasNext()) {
                // other here is the context to undo in...
                NamedObj other = (NamedObj)others.next();
                if (other != null) {
                    // Make the request by queueing a new change request.
                    // This needs to be done because we have no assurance
                    // that just because this change request is being
                    // executed now that the propagated one is safe to
                    // execute.
                    MoMLUndoChangeRequest newChange = new MoMLUndoChangeRequest(
                            getSource(),
                            other);
                    // Let the parser know that we are propagating
                    // changes, so that it does not need to record them
                    // using MoMLAttribute.
                    newChange._propagating = true;
                    other.requestChange(newChange);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The context in which to execute the request.
    private NamedObj _context;

    // The parser given in the constructor.
    private MoMLParser _parser;

    // Indicator of whether this request is the result of a propagating change.
    private boolean _propagating = false;

    // Indicates whether or not this chagne is a redo or an undo request
    private boolean _undoIsRedo = false;
}
