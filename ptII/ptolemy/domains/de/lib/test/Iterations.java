/* A simple test case for iterations and microsteps.

 Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.de.lib.test;

import java.util.Iterator;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Recorder;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Iterations

/* A test for iterations and microsteps.
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Iterations {
    private Recorder _recorder;

    public Iterations() throws KernelException {
        Workspace w = new Workspace("w");
        TypedCompositeActor toplevel = new TypedCompositeActor(w);
        toplevel.setName("toplevel");

        DEDirector director = new DEDirector(toplevel, "director");

        //director.addDebugListener(new StreamListener());
        Manager manager = new Manager(w, "manager");
        toplevel.setManager(manager);

        TestSource source1 = new TestSource(toplevel, "source1");
        TestSource source2 = new TestSource(toplevel, "source2");
        _recorder = new Recorder(toplevel, "recorder");

        toplevel.connect(source1.output, source2.input);
        toplevel.connect(source2.output, _recorder.input);

        director.startTime.setToken(new DoubleToken(0.0));
        director.stopTime.setToken(new DoubleToken(1000.0));
        manager.initialize();
        manager.iterate();
        manager.iterate();
        manager.wrapup();
    }

    public String getResult() {
        StringBuffer result = new StringBuffer();
        Iterator tokens = _recorder.getHistory(0).iterator();

        while (tokens.hasNext()) {
            Token token = (Token) tokens.next();

            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(token.toString());
        }

        return result.toString();
    }
}
