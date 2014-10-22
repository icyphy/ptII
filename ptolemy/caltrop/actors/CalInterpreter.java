/*
 @Copyright (c) 2003-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.caltrop.actors;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import caltrop.interpreter.ast.Actor;

///////////////////////////////////////////////////////////////////
//// CalInterpreter

/**
 This actor interprets CAL source as an actor inside the Ptolemy II
 framework. It has a <tt>calCode</tt> string attribute that contains
 the text of a CAL actor. It configures itself according to CAL code
 string (setting up ports, parameters, types etc.) and then proceeds
 to execute the actor by interpreting the actions using the {@link
 ptolemy.caltrop.ddi.util.DataflowActorInterpreter
 DataflowActorInterpreter} infrastructure.

 <p> The actor interpreter is configured by a context that injects the
 appropriate <tt>Token</tt>-based value system into the evaluation of
 the actions. This is implemented in the class {@link
 ptolemy.caltrop.PtolemyPlatform PtolemyPlatform}.

 <p> For further documentation on CAL, see the
 <a href = "http://embedded.eecs.berkeley.edu/caltrop/docs/LanguageReport">Language Report</a>.

 @author J&#246;rn W. Janneck <jwj@acm.org>, Christopher Chang, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.caltrop.ddi.util.DataflowActorInterpreter
 */
public class CalInterpreter extends AbstractCalInterpreter {
    /** Construct an actor in the given workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public CalInterpreter(Workspace workspace) {
        super(workspace);
    }

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CalInterpreter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        calCode = new StringAttribute(this, "calCode");
        calCode.setExpression(defaultActorText);
        calCode.setVisibility(Settable.EXPERT);
        _updateActor();
        // Set the name of the icon.  This avoids a
        // ConcurrentModificationException in
        // ptolemy/configs/test/allConfigs.tcl
        _attachActorIcon("CalActor");
    }

    /**
     * The only attribute whose modifications are handled is the
     * <tt>calCode</tt> attribute, which contains the source code of
     * the CAL actor.
     * <p>
     * Whenever the source is changed, the text is parsed,
     * transformed, and translated into an internal data structure
     * used for interpretation.
     *
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If an error occurs parsing or
     * transforming the CAL source code.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == calCode) {
            _updateActor();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * The CAL source to be interpreted.
     */
    public StringAttribute calCode;

    /** Default CAL code. */
    protected final static String defaultActorText = "actor CalActor () Input ==> Output : end";

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /** Update the actor by reading the calCode parameter.
     */
    private void _updateActor() throws IllegalActionException {
        // This method is present so as to avoid a concurrent
        // modification exception when iterating through the
        // attributes.  See ptolemy/configs/tests/allCOnfigs.tcl
        String s = calCode.getExpression();
        Actor actor;

        try {
            actor = caltrop.interpreter.util.SourceReader.readActor(s);
        } catch (Throwable ex) {
            // FIXME: It would be nice if _stringToActor threw
            // something other than Throwable here.
            throw new IllegalActionException(this, ex,
                    "Failed to read in actor in:\n  " + s
                            + "\nThis sometimes occurs if saxon8.jar "
                            + "or saxon8-dom.jar are not in "
                            + "your classpath.");
        }

        try {
            if (actor != null) {
                _setupActor(actor);
            }
        } catch (Throwable ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to set up actor'" + s + "'");
        }
    }
}
