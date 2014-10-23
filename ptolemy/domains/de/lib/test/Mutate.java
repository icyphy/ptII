/* Mutations example: Figure 15.7 from the de chapter

 Copyright (c) 1999-2014 The Regents of the University of California.
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

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.Recorder;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.lib.Merge;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 @author Edward A. Lee
 @version $Id$
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Mutate {
    public Manager manager;

    private Recorder _rec;

    private Clock _clock;

    private TypedCompositeActor _top;

    private DEDirector _director;

    public Mutate() throws IllegalActionException, NameDuplicationException {
        _top = new TypedCompositeActor();
        _top.setName("top");
        manager = new Manager();
        _director = new DEDirector();
        _top.setDirector(_director);
        _top.setManager(manager);

        _clock = new Clock(_top, "clock");
        _clock.values.setExpression("[1.0]");
        _clock.offsets.setExpression("[0.0]");
        _clock.period.setExpression("1.0");
        _rec = new Recorder(_top, "recorder");
        _top.connect(_clock.output, _rec.input);
    }

    public void insertClock() {
        // Create an anonymous inner class
        ChangeRequest change = new ChangeRequest(_top, "test2") {
            @Override
            public void _execute() throws IllegalActionException,
            NameDuplicationException {
                _clock.output.unlinkAll();
                _rec.input.unlinkAll();

                Clock clock2 = new Clock(_top, "clock2");
                clock2.values.setExpression("[2.0]");
                clock2.offsets.setExpression("[0.5]");
                clock2.period.setExpression("2.0");

                Merge merge = new Merge(_top, "merge");
                _top.connect(_clock.output, merge.input);
                _top.connect(clock2.output, merge.input);
                _top.connect(merge.output, _rec.input);

                // Any pre-existing input port whose connections
                // are modified needs to have this method called.
                _rec.input.createReceivers();
                _director.invalidateSchedule();
            }
        };

        _top.requestChange(change);
    }
}
