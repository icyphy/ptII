/* SMVLegacyCodeActor for embedding SMV legacy code.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.verification.lib;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
////SMVLegacyCodeActor
/**
 * An actor of this class contains pure SMV codes. Note that the
 * SMVLegacyCodeActor is currently not executable.
 *
 * <p>When performing the conversion, the code would be automatically appended to
 * the generated file. However, since for other actors, they require signal
 * information for their conversion, it is the duty of the designer to specify ports.
 *
 * <p>To use this actor within Vergil, double click on the actor and
 * insert SMV code into the code templates.
 *
 * The code writing should be similar to the embeddedCActor
 *
 * @author Chihhong Patrick Cheng, Contributor: Edward A. Lee, Christopher
 *         Brooks
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red (patrickj)
 */
public class SMVLegacyCodeActor extends TypedCompositeActor {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructor,
     *  create the <i>embeddedSMVCode</i> parameter, and initialize
     *  it to provide an empty template.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception NameDuplicationException If the container already
     *   has an actor with this name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public SMVLegacyCodeActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        embeddedSMVCode = new StringAttribute(this, "embeddedSMVCode");

        // Set the visibility to expert, as casual users should
        // not see the C code.  This is particularly true if one
        // installs an actor that is an instance of this with
        // particular C code in the library.
        embeddedSMVCode.setVisibility(Settable.EXPERT);

        String code = "\n/* The file contains contents in formats acceptable by SMV.\n"
                + " * Currently there is no content checking functionality.\n"
                + " * It is the designer's responsibility to keep it correct.\n"
                + " *\n"
                + " * The module name and the corresponding input parameter \n"
                + " * would be generated automatically.\n" + " */";
        embeddedSMVCode.setExpression(code);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"64\" height=\"30\" "
                + "style=\"fill:black\"/>\n" + "<text x=\"-29\" y=\"4\""
                + "style=\"font-size:9; fill:white; font-family:SansSerif\">"
                + "EmbeddedSMV</text>\n" + "</svg>\n");

        new SRDirector(this, "SRDirector");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The SMV code that specifies the function of this actor.  The
     *  default value is the code necessary to implement a identity
     *  function.
     */
    public StringAttribute embeddedSMVCode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Throw an IllegalActionException to indicate that this actor
     *  is used for code generation only.
     *  @exception IllegalActionException No simulation
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        throw new IllegalActionException(this, getName() + " can not run in "
                + "simulation mode.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

}
