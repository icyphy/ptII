/* Test for ChangeRequest.

 Copyright (c) 1998-2002 The Regents of the University of California.
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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.kernel.util.test;

import ptolemy.kernel.util.*;
import ptolemy.kernel.ComponentRelation;
import ptolemy.actor.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.SampleDelay;
import ptolemy.actor.lib.*;
import ptolemy.data.*;

import java.util.Collections;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ChangeRequestTest
/**
Test for ChangeRequest.

@author  Edward A. Lee
@version $Id$
@see ptolemy.kernel.util.ChangeRequest

*/
public class ChangeRequestTest {

    /** Constructor.
     */
    public ChangeRequestTest()
            throws IllegalActionException, NameDuplicationException {
        _top = new TypedCompositeActor();
        _top.setName("top");
        _manager = new Manager();
        SDFDirector director = new SDFDirector();
        _top.setDirector(director);
        _top.setManager(_manager);

        _const = new Const(_top, "const");
        _rec = new Recorder(_top, "rec");
        _top.connect(_const.output, _rec.input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Finish a run.  Return the results.
     */
    public Enumeration finish() throws KernelException {
        for (int i = 0; i < 4; i++) {
            _manager.iterate();
        }
        _manager.wrapup();
        return Collections.enumeration(_rec.getHistory(0));
    }

    /** Insert a feedback loop.
     */
    public void insertFeedback() {
        // Create an anonymous inner class
        ChangeRequest change = new ChangeRequest(this, "test2") {
            protected void _execute() throws Exception {
                _const.output.unlinkAll();
                _rec.input.unlinkAll();
                AddSubtract add = new AddSubtract(_top, "add");
                SampleDelay sampleDelay = new SampleDelay(_top, "sampleDelay");
                sampleDelay.initialOutputs.setExpression("{4, 5}");
                _top.connect(_const.output, add.plus);
                ComponentRelation relation =
                    _top.connect(add.output, sampleDelay.input);
                _rec.input.link(relation);
                // Any pre-existing input port whose connections
                // are modified needs to have this method called.
                _rec.input.createReceivers();
                _top.connect(sampleDelay.output, add.plus);
            }
        };
        _top.requestChange(change);
    }

    /** Mutate.
     */
    public void mutate() {
        // Create an anonymous inner class
        ChangeRequest change = new ChangeRequest(this, "test") {
            protected void _execute() throws Exception {
                _const.value.setToken(new DoubleToken(2.0));
            }
        };
        _top.requestChange(change);
    }

    /** Start a run.
     */
    public void start() throws KernelException {
        _manager.initialize();
        _manager.iterate();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Manager _manager;
    private Recorder _rec;
    private Const _const;
    private TypedCompositeActor _top;
}
