/* Test for ChangeRequest.

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
package ptolemy.kernel.util.test.system;

import java.util.Collections;
import java.util.Enumeration;

import ptolemy.actor.IORelation;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.Recorder;
import ptolemy.data.DoubleToken;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.lib.SampleDelay;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ChangeRequestTest

/**
 Test for ChangeRequest.

 This test is in the system directory because it uses packages outside
 of kernel.util.

 @author  Edward A. Lee, Contributor: Christopher Hylands
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 @see ptolemy.kernel.util.ChangeRequest

 */
public class ChangeRequestTest implements ChangeListener {
    /** Constructor.
     */
    public ChangeRequestTest() throws IllegalActionException,
            NameDuplicationException {
        _top = new TypedCompositeActor();
        _top.setName("top");
        _manager = new Manager();

        SDFDirector director = new SDFDirector();
        _top.setDirector(director);
        _top.setManager(_manager);

        _top.addChangeListener(this);
        _const = new Const(_top, "const");
        _rec = new Recorder(_top, "rec");
        IORelation relation = (IORelation) _top.connect(_const.output,
                _rec.input);
        relation.setWidth(1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change request has been successfully executed by
     *  doing nothing. This method is called after a change request
     *  has been executed successfully.  In this class, we
     *  do nothing.
     *  @param change The change that has been executed, or null if
     *   the change was not done via a ChangeRequest.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
    }

    /** React to a change request that has resulted in an exception.
     *  This method is called after a change request was executed,
     *  but during the execution in an exception was thrown.
     *  This method throws a runtime exception with a description
     *  of the original exception.
     *  @param change The change that was attempted or null if
     *   the change was not done via a ChangeRequest.
     *  @param exception The exception that resulted.
     */
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        // If we do not implement ChangeListener, then ChangeRequest
        // will print any errors to stdout and continue.
        // This causes no end of trouble with the test suite
        // We can't throw and Exception here because this method in
        // the base class does not throw Exception.
        // In JDK1.4, we can construct exceptions from exceptions, but
        // not in JDK1.3.1
        //throw new RuntimeException(exception);
        throw new RuntimeException(exception.toString());
    }

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
            @Override
            protected void _execute() throws Exception {
                _const.output.unlinkAll();
                _rec.input.unlinkAll();

                AddSubtract add = new AddSubtract(_top, "add");
                SampleDelay sampleDelay = new SampleDelay(_top, "sampleDelay");
                sampleDelay.initialOutputs.setExpression("{4, 5}");
                _top.connect(_const.output, add.plus);

                ComponentRelation relation = _top.connect(add.output,
                        sampleDelay.input);
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
        if (changeRequest == null) {
            changeRequest = mutateConst2ChangeRequest();
        }

        _top.requestChange(changeRequest);
    }

    public void waitForCompletionTask() {
        Thread waitForCompletionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName()
                        + " About to wait for completion");

                try {
                    changeRequest.waitForCompletion();
                } catch (Exception ex) {
                    System.out.println(ex);
                }

                System.out.println(Thread.currentThread().getName()
                        + " Done waiting for completion");
            }
        });

        waitForCompletionThread.setName("waitForCompletionThread");
        waitForCompletionThread.start();
    }

    /** Create a change request that always throws an exception. */
    public ChangeRequest mutateBadChangeRequest() {
        // Create an anonymous inner class
        changeRequest = new ChangeRequest(this,
                "Change request that always throws an Exception") {
            @Override
            protected void _execute() throws Exception {
                // Compare against true so that the code compiles.
                if (true) {
                    throw new Exception("Always Thrown Exception");
                }
            }
        };
        return changeRequest;
    }

    /** Create a change request that sets const to 2.0. */
    public ChangeRequest mutateConst2ChangeRequest() {
        // Create an anonymous inner class
        changeRequest = new ChangeRequest(this, "Changing Const to 2.0") {
            @Override
            protected void _execute() throws Exception {
                _const.value.setToken(new DoubleToken(2.0));
            }
        };
        return changeRequest;
    }

    /** Start a run.
     */
    public void start() throws KernelException {
        _manager.initialize();
        _manager.iterate();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** ChangeRequest that modifies the system. */
    public ChangeRequest changeRequest;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Manager _manager;

    private Recorder _rec;

    private Const _const;

    private TypedCompositeActor _top;
}
