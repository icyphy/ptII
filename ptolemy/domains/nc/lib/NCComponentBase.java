/* Actor that serves as a placeholder for NC modules and configurations.

@Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)


*/
package ptolemy.domains.nc.lib;

import ptolemy.actor.AtomicActor;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// NCComponentBase
/**
Base class for nesC component classes.  These are classes with source
code defined in a .nc file intended for use with TinyOS to program
the Berkeley Motes.  This class provides a parameter <i>source</i>
that is used to identify the nesC source file. It works in conjunction
with the NCComponent MoML class, which attaches a tableau factory
so that look inside will open the nesC source file.

@author Elaine Cheong, Edward A. Lee, Yang Zhao
@version $Id$
@since Ptolemy II 4.0
*/
public class NCComponentBase extends AtomicActor {

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public NCComponentBase(Workspace workspace) {
        super(workspace);
        try {
            _init();
        } catch (KernelException e) {
            throw new InternalErrorException(
            "Error constructing parameters of NCActor.");
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
    public NCComponentBase(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _init();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** The source code file or URL. */
    public FileParameter source;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _init() throws IllegalActionException, NameDuplicationException {
        source = new FileParameter(this, "source");
        source.setExpression("$PTII/ptolemy/domains/nc/lib/NCActor.nc");
        source.setVisibility(Settable.EXPERT);
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-20\" y=\"-20\" "
                + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"-3\" y=\"5\" "
                + "style=\"font-size:18\">\n"
                + "NC\n"
                + "</text>\n"
                + "</svg>\n");
    }
}


