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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
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
//// MoMLChangeRequest
/**
A mutation request specified in MoML.  This class provides the preferred
mechanism for implementing mutations on a model while it is executing.
To use it, create an instance of this class, specifying MoML code as
an argument to the constructor.  Then queue the instance of this class
with a composite entity by calling its requestChange() method.

@author  Edward A. Lee
@version $Id$
*/
public class MoMLChangeRequest extends ChangeRequest {

    /** Construct a mutation request for the specified parser.
     *  The originator is the source of the change request.
     *  A listener to changes will probably want to check the originator
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  Alternatively, it can call waitForCompletion().
     *  All external references are assumed to be absolute URLs.  Whenever
     *  possible, use a different constructor that specifies the base.
     *  @param originator The originator of the change request.
     *  @param parser The parser to execute the request.
     *  @param request The mutation request in MoML.
     */
    public MoMLChangeRequest(
             Object originator, MoMLParser parser, String request) {
	this(originator, parser, request, null);
    }

    /** Construct a mutation request for the specified parser.
     *  The originator is the source of the change request.
     *  A listener to changes will probably want to check the originator
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  Alternatively, it can call waitForCompletion().
     *  External references will be resolved relative to the given base URL.
     *  @param originator The originator of the change request.
     *  @param parser The parser to execute the request, or null to use
     *   the default parser.
     *  @param request The mutation request in MoML.
     *  @param base The URL relative to which external references should
     *  be resolved.
     */
    public MoMLChangeRequest(
             Object originator, MoMLParser parser, String request, URL base) {
        super(originator, request);
        if (parser == null) {
            _parser = _staticParser;
        } else {
            _parser = parser;
        }
        _context = parser.getToplevel();
	_base = base;
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
	this(originator, _staticParser, context, request, null);
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
     *  External references will be resolved relative to the given base URL.
     *  @param originator The originator of the change request.
     *  @param context The context in which to execute the MoML.
     *  @param request The mutation request in MoML.
     *  @param base The URL relative to which external references should
     *   be resolved.
     */
    public MoMLChangeRequest(
            Object originator, NamedObj context, String request, URL base) {
	this(originator, _staticParser, context, request, null);
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
     *  @param parser The parser to execute the request.
     *  @param context The context in which to execute the MoML.
     *  @param request The mutation request in MoML.
     *  @param base The URL relative to which external references should
     *   be resolved.
     */
    public MoMLChangeRequest(
            Object originator, MoMLParser parser, NamedObj context,
            String request, URL base) {
        super(originator, request);
        if (parser == null) {
            _parser = _staticParser;
        } else {
            _parser = parser;
        }
        _context = context;
	_base = base;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the change by evaluating the request using the
     *  specified parser.
     *  @exception Exception If an exception is thrown
     *   while evaluating the request.
     */
    protected void _execute() throws Exception {
        _parser.reset();
        try {
            _parser._propagating = _propagating;
            
            if (_context != null) {
                _parser.setContext(_context);
            }
            _parser.parse(_base, getDescription());
        } finally {
            _parser._propagating = false;
        }

        // Apply the same change to any object that defers its MoML
        // definition to the context in which we just applied the change.
        NamedObj context = _context;
        if (context == null) {
            context = _parser.getToplevel();
        }
        List othersList = context.deferredMoMLDefinitionFrom();
        if (othersList != null) {

            Iterator others = othersList.iterator();
            while (others.hasNext()) {
                NamedObj other = (NamedObj)others.next();
                if (other != null) {
                    // Make the request by queueing a new change request.
                    // This needs to be done because we have no assurance
                    // that just because this change request is being
                    // executed now that the propogated one is safe to
                    // execute.
                    // FIXME: undo is going to be difficult to
                    // handle here.  I guess we just count on undo
                    // commands propagating as well...
                    MoMLChangeRequest newChange = new MoMLChangeRequest(
                            getOriginator(),
                            _parser,
                            other,              // context
                            getDescription(),   // MoML code
                            _base);
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

    // The URL relative to which external references should be resolved.
    private URL _base;

    // The parser given in the constructor.
    private MoMLParser _parser;

    // Indicator of whether this request is the result of a propagating change.
    private boolean _propagating = false;

    // A generic parser to use if no parser is specified.
    private static MoMLParser _staticParser = new MoMLParser();
}
