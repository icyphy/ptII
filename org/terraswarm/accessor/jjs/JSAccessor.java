/* An component accessor that consists of an interface and a script.

 Copyright (c) 2015 The Regents of the University of California.
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
package org.terraswarm.accessor.jjs;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.jjs.JavaScript;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// JSAccessor

/**
 An component accessor that consists of an interface and a script.

 <p>The "<a href="#VisionOfSwarmLets">Vision of Swarmlets</a>" paper
 defines three types of accessors: Interface, Component and Composite.
 The paper states: "A component accessor has an interface and a
 script...  The script defines one or more functions that are invoked
 by the swarmlet host."</p>

 <p>This is a specialized JavaScript actor that hides the script
 from casual users by putting it in "expert" mode.
 It also sets the actor to "restricted" mode, which restricts
 the functionality of the methods methods and variables
 provided in the JavaScript context.</p>

 <p>FIXME: This should support versioning of accessors.
 It should check the accessorSource for updates and replace
 itself if there is a newer version and the user agrees to
 the replacement. This will be tricky because any parameters
 and connections previously set should be preserved.</p>
 
 <p>This actor extends {@link ptolemy.actor.lib.jjs.JavaScript}
 and thus requires Nashorn, which is present in Java-1.8 and
 later.</p>

 <h2>References</h2>

 <p><name="VisionOfSwarmLets">Elizabeth Latronico, Edward A. Lee,
 Marten Lohstroh, Chris Shaver, Armin Wasicek, Matt Weber.</a>
 <a href="http://www.terraswarm.org/pubs/332.html">A Vision of Swarmlets</a>,
 <i>IEEE Internet Computing, Special Issue on Building Internet
 of Things Software</i>, 19(2):20-29, March 2015.</p>

 @author Edward A. Lee, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (bilung)
 */
public class JSAccessor extends JavaScript {

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JSAccessor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        accessorSource = new StringAttribute(this, "accessorSource");
        accessorSource.setVisibility(Settable.NOT_EDITABLE);

        SingletonParameter hide = new SingletonParameter(script, "_hide");
        hide.setExpression("true");

        // The base class, by default, exposes the instance of this actor in the
        // JavaScript variable "actor", which gives an accessor full access
        // to the model, and hence a way to invoke Java code. Prevent this
        // by putting the actor in "restricted" mode.
        _restricted = true;

        // Set the script parameter to Visibility EXPERT.
        script.setVisibility(Settable.EXPERT);
        
        // Hide the port for the script.
        (new SingletonParameter(script.getPort(), "_hide")).setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The source of the accessor (a URL). */
    public StringAttribute accessorSource;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class so that the name of any port added is
     *  shown.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    @Override
    protected void _addPort(TypedIOPort port) throws IllegalActionException,
            NameDuplicationException {
        super._addPort(port);
        SingletonParameter showName = new SingletonParameter(port, "_showName");
        showName.setExpression("true");
    }
}
