/* Director for Kahn-MacQueen process network semantics.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.domains.pn.test;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** 
 *  Illustrates a problem with port naming and class instantiation.
 *  <p> In this actor, the Java variable of the port is named "inputo",
 *  but the constructor uses the wrong name.  This results in a
 *  NullPointerException with this actor is used with the class mechanism.
 *  See $PTII/ptolemy/domains/pn/test/auto/NameProblemTest.xml
 *  @author Xiaowen Xin, Ilkay Altintas
 *  @version $Id$
 *  @since Ptolemy II 4.1
 */
public class NameProblem extends TypedAtomicActor {

    public NameProblem(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        inputo = new TypedIOPort(this, "input", true, false);
        inputo.setMultiport(false);

        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(false);
        output.setTypeEquals(BaseType.STRING);
    }

    public TypedIOPort inputo;
    public TypedIOPort output;

    public void fire() throws IllegalActionException {
        StringToken tok = (StringToken) inputo.get(0);
        output.broadcast(new StringToken("out"));
    }
}

// vim: sw=4 ts=4 et
