/* A template actor that is associated with a block of C-code. This
 actor is for use only with the Giotto code domain.


 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.giotto.cgc;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// CActorBase

/**
 This is a base class for actors that are intended to be used with an
 instance of GiottoCEmachineFrameworkGenerator, an attribute that is
 placed in the model and that generates code when the user double clicks on
 its icon. This class provides a parameter <i>source</i> that is
 used to identify the C source file that provides the functionality
 of the actor. The
 .c file along with the generated code  can then be compiled with emachine
 files for a specific platform to generate an executable that runs on
 that platform. This actor also has a second parameter
 <i>frequency</i> that is Giotto specific, and is used to specify the number of
 times the C task associated with this class is executed in a Giotto
 iteration (a "super iteration" in Giotto). It works in conjunction
 with the CActor MoML class, which
 attaches a tableau factory so that look inside will open the C source
 file.

 @author N. Vinay Krishnan, Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @see ptolemy.domains.giotto.cgc.GiottoCEmachineFrameworkGenerator
 @Pt.ProposedRating Red (vkris)
 @Pt.AcceptedRating Red (cxh)
 */
public class CActorBase extends TypedAtomicActor {
    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public CActorBase(Workspace workspace) {
        super(workspace);

        try {
            _init();
        } catch (KernelException e) {
            throw new InternalErrorException(
                    "Error constructing parameters of CActorBase.");
        }
    }

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */

    /* Two separate constructors are used because I'm not sure if
     * one is redundant. Edward tried removing the constructor above
     * but that created anexception when being tha class was being
     * instantiated from CActor.xml. Therefore I put it back.
     */
    public CActorBase(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The source code file or URL. */
    public FileParameter source;

    /** The number of times this actor gets executed in one super-period
     *  time frame specified by the Giotto director.
     */
    public Parameter frequency;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new instance of CPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *  @param name The name for the new port.
     *  @return The new port.
     *  @see CPort
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            CPort port = new CPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to throw an exception if the added port
     *  is not an instance of CPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set the container of the port to point to
     *  this entity. It assumes that the port is in the same workspace
     *  as this actor.
     *  Derived classes may override this method to further constrain the
     *  port to be a subclass of CPort. This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     *
     *  @param port The port to add to this actor.
     *  @exception IllegalActionException If the port is not an instance
     *   of CPort, or the port has no name.
     *  @exception NameDuplicationException If the port name coincides with
     *   the name of another port already in the actor.
     */
    protected void _addPort(CPort port) throws IllegalActionException,
            NameDuplicationException {
        super._addPort(port);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        source = new FileParameter(this, "source");
        source.setExpression("$PTII/ptolemy/domains/giotto/cgc/demo/task_code.c");

        // Should this be visible?
        // source.setVisibility(Settable.EXPERT);
        frequency = new Parameter(this, "frequency");
        frequency.setExpression("1");
        frequency.setTypeEquals(BaseType.INT);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n" + "<text x=\"-16\" y=\"5\" "
                + "style=\"font-size:18\">\n" + "CActor\n" + "</text>\n"
                + "</svg>\n");
    }
}
