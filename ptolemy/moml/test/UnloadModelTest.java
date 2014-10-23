/* Test unloading a model
 Copyright (c) 2010-2014 The Regents of the University of California.
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

import java.io.File;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.MoMLSimpleApplication;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.StringUtilities;

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
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class UnloadModelTest extends MoMLSimpleApplication {

    /** Parse the xml file and run it.
     *  @param xmlFileName A string that refers to an MoML file that
     *  contains a Ptolemy II model.  The string should be
     *  a relative pathname.
     *  @exception Throwable If there was a problem parsing
     *  or running the model.
     */
    public UnloadModelTest(String xmlFileName) throws Throwable {
        workspace = new Workspace("MyWorkspace");
        parser = new MoMLParser(workspace);

        // The test suite calls MoMLSimpleApplication multiple times,
        // and the list of filters is static, so we reset it each time
        // so as to avoid adding filters every time we run an auto test.
        // We set the list of MoMLFilters to handle Backward Compatibility.
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

        // Filter out any graphical classes.
        MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

        // If there is a MoML error, then throw the exception as opposed
        // to skipping the error.  If we call StreamErrorHandler instead,
        // then the nightly build may fail to report MoML parse errors
        // as failed tests
        //parser.setErrorHandler(new StreamErrorHandler());
        // We use parse(URL, URL) here instead of parseFile(String)
        // because parseFile() works best on relative pathnames and
        // has problems finding resources like files specified in
        // parameters if the xml file was specified as an absolute path.
        toplevel = (CompositeActor) parser.parse(null, new File(xmlFileName)
        .toURI().toURL());

        _manager = new Manager(toplevel.workspace(), "MoMLSimpleApplication");
        toplevel.setManager(_manager);
        toplevel.addChangeListener(this);

        _manager.addExecutionListener(this);
        _activeCount++;

        _manager.startRun();

        Thread waitThread = new UnloadThread();

        // Note that we start the thread here, which could
        // be risky when we subclass, since the thread will be
        // started before the subclass constructor finishes (FindBugs)
        waitThread.start();
        waitThread.join();
        if (_sawThrowable != null) {
            throw _sawThrowable;
        }
    }

    /** Load a model and then unload it.
     *  <p>Typically, this class is invoked with something like:
     *  <pre>
     *  java -classpath $PTII ptolemy.moml.test.UnloadModelTest ../demo/test.xml
     *  </pre>
     *  @param args The first argument is the name of the file to be loaded.
     */
    public static void main(String[] args) {
        try {
            new UnloadModelTest(args[0]);
        } catch (Throwable ex) {
            System.err.println("Command failed: " + ex);
            ex.printStackTrace();
            StringUtilities.exit(1);
        }
    }

    /** The MoMLParser that is created and then destroyed. */
    public MoMLParser parser;

    /** The toplevel model that is created and then destroyed. */
    public CompositeActor toplevel;

    /** The workspace in which the model and Manager are created. */
    public Workspace workspace;

    /** Return the amount of memory used.
     *  @return A string that describes the amount of memory.
     */
    public static String memory() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024;
        long freeMemory = runtime.freeMemory() / 1024;
        return "Memory: "
        + totalMemory
        + "K Free: "
        + freeMemory
        + "K ("
        + Math.round((double) freeMemory / (double) totalMemory * 100.0)
        + "%)";
    }

    /** Wait for the run to finish and the unload the model.
     */
    public class UnloadThread extends Thread {
        @Override
        public void run() {
            waitForFinish();
            try {
                // First, we gc and then print the memory stats
                // BTW to get more info about gc,
                // use java -verbose:gc . . .
                System.gc();
                Thread.sleep(1000);
                System.out.println("Memory before unloading: " + memory());

                //              if (toplevel instanceof CompositeEntity) {
                //                  try {
                //                      ParserAttribute parserAttribute = (ParserAttribute) toplevel
                //                          .getAttribute("_parser", ParserAttribute.class);
                //                      parserAttribute.setContainer(null);
                //                      ((CompositeEntity)toplevel).setContainer(null);
                //                  } catch (Exception ex) {
                //                      ex.printStackTrace();
                //                  }
                //              }

                if (parser != null) {
                    //                  if (parser.topObjectsCreated() != null) {
                    //                      parser.topObjectsCreated().remove(toplevel);
                    //                  }

                    parser.resetAll();
                    // parser is a public variable so setting it to
                    // null will (hopefully) cause the garbage
                    // collector to collect it.
                    parser = null;

                    // The next line removes the static backward compatibility
                    // filters, which is probably not what we want if we
                    // want to parse another file.
                    // BackwardCompatibility.clear();

                    // The next line will remove the static MoMLParser
                    // used by the filters.  If we add filters, then the static
                    // MoMLParser is recreated.
                    MoMLParser.setMoMLFilters(null);
                }

                //                 try {
                //                     toplevel.setContainer(null);
                //                     _manager.terminate();
                //                     toplevel.setManager(null);
                //                 } catch (Exception ex) {
                //                     ex.printStackTrace();
                //                 }

                // _manager is a protected variable so setting it to
                // null will (hopefully) cause the garbage
                // collector to collect it.
                _manager = null;

                //                 try {
                //                     toplevel.workspace().getWriteAccess();
                //                     toplevel.workspace().removeAll();
                //                 } catch (Throwable throwable) {
                //                     throwable.printStackTrace();
                //                 } finally {
                //                     toplevel.workspace().doneWriting();
                //                 }

                // toplevel and workspace are a public variables so
                // setting it to null will (hopefully) cause the
                // garbage collector to collect them

                // Set toplevel to null so that the Manager is collected.
                toplevel = null;

                // Set workspace to null so that the objects contained
                // by the workspace may be collected.
                workspace = null;

                System.gc();
                Thread.sleep(1000);
                System.out.println("Memory after  unloading: " + memory());

                System.out.println("Sleeping for 1000 seconds");
                if (_sawThrowable != null) {
                    throw new RuntimeException("Execution failed",
                            _sawThrowable);
                }
                Thread.sleep(1000000);
            } catch (InterruptedException ex) {
                throw new RuntimeException("InterrupteException", ex);
            }
        }
    }
}
