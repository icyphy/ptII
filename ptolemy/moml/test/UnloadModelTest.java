/* Test unloading a model
 Copyright (c) 2010 The Regents of the University of California.
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
package ptolemy.moml.test;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

/**
 Test unloading a model.
 
 <p>This class is used to test loading and unlooading a model.
 When used with a memory profiler like JProfiler, we can
 look for leaks.  To run the test, use:
 <pre>
java -classpath $PTII ptolemy.moml.test.UnloadModelTest ../demo/test.xml
 </pre>
 </p>

 @author Brian Hudson, Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class UnloadModelTest {

    public UnloadModelTest(String modelFileName) throws Exception {
        _parser = new MoMLParser();
        _model = (CompositeActor) _parser.parseFile(modelFileName);
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

        _manager = new Manager(_model.workspace(), "MoMLSimpleApplication");
        _model.setManager(_manager);
        //_model.addChangeListener(this);

        //_manager.addExecutionListener(this);
        _activeCount++;

        _manager.startRun();

        Thread waitThread = new Thread() {
            public void run() {
                waitForFinish();
                if (_sawThrowable != null) {
                    throw new RuntimeException("Execution failed",
                            _sawThrowable);
                }
            }
        };

        // Note that we start the thread here, which could
        // be risky when we subclass, since the thread will be
        // started before the subclass constructor finishes (FindBugs)
        waitThread.start();
        waitThread.join();

        System.out.println(_model.exportMoML().substring(0,50) + "...");
        unloadModel();
    }

    /** Load a model and then unload it.
     *  @param args The first argument is the name of the file to be loaded.
     *  @exception Exception If the model cannot be parsed or unloaded.
     */
    public static void main(String[] args) throws Exception {
        try {
            new UnloadModelTest(args[0]);
        } catch (Throwable ex) {
            System.err.println("Command failed: " + ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void unloadModel() {
        if (_model == null) return;

        if (_model instanceof CompositeEntity) {
            try {
                ((CompositeEntity)_model).setContainer(null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            _model.workspace().getWriteAccess();
            _model.workspace().removeAll();
        } finally {
            _model.workspace().doneWriting();
        }

        if (_parser != null) {
            if(_parser.topObjectsCreated() != null) {
                _parser.topObjectsCreated().remove(_model);
            }
            _parser.resetAll();
            _parser = null;
        }
        _model = null;
    }
    
    /** Wait for all executing runs to finish, then return.
     */
    public synchronized void waitForFinish() {
        while (_activeCount > 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The count of currently executing runs. */
    protected volatile int _activeCount = 0;

    /** The manager of this model. */
    protected Manager _manager = null;

    /** The exception seen by executionError(). */
    protected volatile Throwable _sawThrowable = null;

    private static CompositeActor _model;
    private static MoMLParser _parser;
}
 