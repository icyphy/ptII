/* A model for testing.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui.test;

import java.util.List;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.domains.sdf.kernel.SDFDirector;

//////////////////////////////////////////////////////////////////////////
//// TestModel
/**

@author Edward A. Lee
@version $Id$
*/
public class TestModel extends TypedCompositeActor {

    public TestModel(Workspace workspace) throws Exception {
        super(workspace);

        // Construct the model.
        Ramp ramp = new Ramp(this, "ramp");
        _rec = new Recorder(this, "rec");
        connect(ramp.output, _rec.input);

        // Attach a director.
        SDFDirector dir = new SDFDirector(this, "director");
        dir.iterations.setExpression("3");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public List getResults() {
        return _rec.getHistory(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Recorder _rec;
}
