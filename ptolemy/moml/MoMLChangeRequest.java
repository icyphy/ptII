/* A mutation request specified in MoML.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
// Review base URL stuff.
*/

package ptolemy.moml;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.UndoStackAttribute;

//////////////////////////////////////////////////////////////////////////
//// MoMLChangeRequest
/**
A mutation request specified in MoML.  This class provides the preferred
mechanism for implementing mutations on a model while it is executing.
To use it, create an instance of this class, specifying MoML code as
an argument to the constructor.  Then queue the instance of this class
with a composite entity by calling its requestChange() method.
<p>
There is one significant subtlety with using this class.
If you create a MoMLChangeRequest with a specified context,
then the change will be executed in that context.  Moreover, if
that context has other objects deferring their MoML definitions to
it, then the change will be replicated in those other objects.
This is the principal mechanism in MoML for an object to serve
as a class definition, and others to serve as instances.  A change
to the class propagates to the instances.  However, it means that
when you make a change request, you have to be sure to pick the
right context.  The getDeferredToParent() method returns the first
parent in the containment hierarchy of its argument that has other
objects deferring their MoML definitions to it.  That is the correct
context to use for a change request.
<p>
The parser used to implement the change will be the parser contained
by a ParserAttribute of the top-level element of the context.  If no
context is given, or there is no ParserAttribute in its top level,
then a new parser is created, and a new ParserAttribute is placed
in the top level.
<p>
NOTE: A significant subtlety remains.  If the parent
returned by getDeferredToParent() itself has a parent that
is deferred to, then changes will <i>not</i> propagate to the
objects that defer to it.  That is, if a MoML class contains
a MoML class, and a change is made to the inner class, then
instances of the outer class are unaffected.
Perhaps MoML should not permit inner classes.

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class MoMLChangeRequest extends ChangeRequest {

    /** Construct a mutation request.
     *  The originator is the source of the change request.
     *  Since no context is given, a new parser will be used, and it
     *  will create a new top level.
     *  A listener to changes will probably want to check the originator
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  Alternatively, it can call waitForCompletion().
     *  All external references are assumed to be absolute URLs.  Whenever
     *  possible, use a different constructor that specifies the base.
     *  @param originator The originator of the change request.
     *  @param request The mutation request in MoML.
     */
    public MoMLChangeRequest(Object originator, String request) {
        this(originator, null, request, null);
    }

    /** Construct a mutation request to be executed in the specified context.
     *  The context is typically a Ptolemy II container, such as an entity,
     *  within which the objects specified by the MoML code will be placed.
     *  This method resets and uses a parser that is a static member
     *  of this class.
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
    public MoMLChangeRequest(
            Object originator, NamedObj context, String request) {
        this(originator, context, request, null);
    }

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
     *  @param originator The originator of the change request.
     *  @param context The context in which to execute the MoML.
     *  @param request The mutation request in MoML.
     *  @param base The URL relative to which external references should
     *   be resolved.
     */
    public MoMLChangeRequest(
            Object originator, NamedObj context, String request, URL base) {
        super(originator, request);
        _context = context;
        _base = base;
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
            if (deferList != null && deferList.size() > 0) {
                return object;
            } else {
                return getDeferredToParent((NamedObj)object.getContainer());
            }
        }
    }

    /** Set whether or not this change is undoable
     *  @param undoable whether or not this change should be treated
     *   as an incremental change that is undoable
     */
    public void setUndoable(boolean undoable) {
        _undoable = undoable;
    }

    /** Set whether or not the undo from this change should be merged with
     *  the previous undoable change
     *  @param mergeWithPrevious whether or not this change should be merged
     */
    public void setMergeWithPreviousUndo(boolean mergeWithPrevious) {
        _mergeWithPreviousUndo = mergeWithPrevious;
    }
    
    /** Specify whether or not to report errors via the handler
     *  that is registered with the parser. By default, if this
     *  method is not called, errors will be reported. Note that in either
     *  case, exceptions will be reported to any change listeners
     *  that are registered with this object via their changeFailed()
     *  method.
     *  @param report False to disable error reporting.
     */
    public void setReportErrorsToHandler(boolean report) {
        _reportToHandler = report;
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
        if (_context != null) {
            _parser = ParserAttribute.getParser(_context);
            _parser.reset();
        }
        if (_parser == null) {
            // There is no previously associated parser (can only
            // happen if _context is null).
            _parser = new MoMLParser();
        }
        // NOTE: To see what is being parsed, uncomment the following:
        /*
          System.out.println("****** Executing MoML change:");
          System.out.println(getDescription());
          if (_context != null) {
              System.out.println("------ in context " + _context.getFullName());
          }
        */

        try {
            _parser._propagating = _propagating;

            if (_context != null) {
                _parser.setContext(_context);

            }
            // Tell the parser if this change is undoable
            if (_undoable) {
                _parser.setUndoable(true);
            }
            ErrorHandler handler = MoMLParser.getErrorHandler();
            if (!_reportToHandler) {
                MoMLParser.setErrorHandler(null);
            }
            _parser.parse(_base, getDescription());
            MoMLParser.setErrorHandler(handler);
        } finally {
            _parser._propagating = false;
        }

        // Merge the undo entry created if needed
        if (_undoable && _mergeWithPreviousUndo) {
            UndoStackAttribute undoInfo = UndoStackAttribute.getUndoInfo(_context);
            undoInfo.mergeTopTwo();
        }

        // Apply the same change to any object that defers its MoML
        // definition to the context in which we just applied the change.
        NamedObj context = _context;
        if (context == null) {
            context = _parser.getToplevel();
        }
        List othersList = context.getMoMLInfo().deferredFrom;
        if (othersList != null) {

            Iterator others = othersList.iterator();
            while (others.hasNext()) {
                NamedObj other = (NamedObj)others.next();
                if (other != null) {
                    // Make the request by queueing a new change request.
                    // This needs to be done because we have no assurance
                    // that just because this change request is being
                    // executed now that the propagated one is safe to
                    // execute.
                    MoMLChangeRequest newChange = new MoMLChangeRequest(
                            getSource(),
                            other,              // context
                            getDescription(),   // MoML code
                            _base);
                    // Let the parser know that we are propagating
                    // changes, so that it does not need to record them
                    // using MoMLAttribute.
                    newChange._propagating = true;
                    newChange._undoable = _undoable;
                    newChange._mergeWithPreviousUndo = _mergeWithPreviousUndo;
                    other.requestChange(newChange);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The context in which to execute the request.
    private NamedObj _context;

    // The URL relative to which external references should be resolved.
    private URL _base;

    // The parser given in the constructor.
    private MoMLParser _parser;
    
    // Indicator of whether this request is the result of a
    // propagating change.
    private boolean _propagating = false;

    // Flag indicating whether to report to the handler registered
    // with the parser.
    private boolean _reportToHandler = false;

    // Flag indicating if this change is undoable or not
    private boolean _undoable = false;

    // Indicates that the undo MoML from this change request should be merged
    // in with the undo MoML from the previos undoable change request if they
    // both have the same context.
    private boolean _mergeWithPreviousUndo = false;
}
