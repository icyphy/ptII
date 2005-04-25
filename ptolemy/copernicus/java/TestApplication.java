/* An application that executes non-graphical
   models specified on the command line.

   Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.copernicus.java;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

import java.io.File;
import java.net.URL;


//////////////////////////////////////////////////////////////////////////
//// TestApplication

/** A simple application that reads in a .xml file as a command
    line argument and runs it.

    This is similar to MoMLSimpleApplication, but forces the number of
    iterations to be 100000.  This makes for good comparison with
    generated code since it reduces the relative affect of initialization
    differences between Ptolemy II and the generated code.


    @author Christopher Hylands
    @version $Id$
    @since Ptolemy II 2.0
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (eal)
*/
public class TestApplication implements ChangeListener {
    /** Parse the xml file and run it.
     *  If a parameter named "copernicus_iterations" is present, then
     *  the value of that parameter is used to set the iterations parameter.
     *  If there is no "copernicus_iterations" parameter, then then
     *  the number of iterations is set to 100000.
     */
    public TestApplication(String xmlFilename) throws Exception {
        MoMLParser parser = new MoMLParser();

        // The test suite calls MoMLSimpleApplication multiple times,
        // and the list of filters is static, so we reset it each time
        // so as to avoid adding filters every time we run an auto test.
        // We set the list of MoMLFilters to handle Backward Compatibility.
        parser.setMoMLFilters(BackwardCompatibility.allFilters());

        // Filter out any graphical classes.
        // We should filter out graphical classes or the
        // treeShakeWithoutCodegen rule will fail when we run it on
        // actor/lib/test/auto/ComplexDivide.
        parser.addMoMLFilter(new RemoveGraphicalClasses());

        //parser.setErrorHandler(new StreamErrorHandler());
        // We use parse(URL, URL) here instead of parseFile(String)
        // because parseFile() works best on relative pathnames and
        // has problems finding resources like files specified in
        // parameters if the xml file was specified as an absolute path.
        CompositeActor toplevel = null;

        // First, we gc and then print the memory stats
        // BTW to get more info about gc,
        // use java -verbose:gc -Xloggc:filename . . .
        System.gc();
        Thread.sleep(1000);

        Runtime runtime = Runtime.getRuntime();

        try {
            URL url = new URL(null, xmlFilename);
            toplevel = (CompositeActor) parser.parse(url, url.openStream());
        } catch (Exception ex) {
            File f = new File(xmlFilename);
            URL url = f.toURL();
            System.err.println("Warning: Parsing '" + xmlFilename
                + "' failed: ");
            ex.printStackTrace();
            System.err.println(" Trying '" + url + "'");

            toplevel = (CompositeActor) parser.parse(null, url);
        }

        // FIXME: nearly duplicate code in kernel/KernelMain.java
        SDFDirector director = (SDFDirector) toplevel.getDirector();

        if (director != null) {
            Parameter iterations = (Parameter) director.getAttribute(
                    "iterations");
            Parameter copernicus_iterations = (Parameter) director.getAttribute(
                    "copernicus_iterations");

            // Set to be a large number of iterations, unless
            // copernicus_iterations is set.
            if (copernicus_iterations != null) {
                iterations.setToken(copernicus_iterations.getToken());
            } else {
                iterations.setToken(new IntToken(100000));
            }

            System.out.println("TestApplication: Setting Iterations to "
                + iterations.getToken());
        }

        Manager manager = new Manager(toplevel.workspace(), "TestApplication");
        toplevel.setManager(manager);
        toplevel.addChangeListener(this);

        String modelName = toplevel.getName();

        long startTime = System.currentTimeMillis();
        long totalMemory1 = runtime.totalMemory() / 1024;
        long freeMemory1 = runtime.freeMemory() / 1024;
        System.out.println("Spent " + (startTime - _parseStartTime)
            + " ms. creating the model.");
        System.out.println(modelName + ": Stats before execution:    "
            + Manager.timeAndMemory(startTime, totalMemory1, freeMemory1));

        // Second, we run and print memory stats.
        manager.execute();

        long totalMemory2 = runtime.totalMemory() / 1024;
        long freeMemory2 = runtime.freeMemory() / 1024;
        String standardStats = Manager.timeAndMemory(startTime, totalMemory2,
                freeMemory2);

        System.out.println(modelName + ": Execution stats:           "
            + standardStats);

        // Third, we gc and print memory stats.
        System.gc();
        Thread.sleep(1000);

        long totalMemory3 = runtime.totalMemory() / 1024;
        long freeMemory3 = runtime.freeMemory() / 1024;
        System.out.println(modelName + ": After Garbage Collection:  "
            + Manager.timeAndMemory(startTime, totalMemory3, freeMemory3));

        // Print out the standard stats at the end
        // so as not to break too many scripts
        System.out.println(standardStats);
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

    /** Create an instance of a single model and run it
     *  @param args The command-line arguments naming the .xml file to run
     */
    public static void main(String[] args) {
        _parseStartTime = System.currentTimeMillis();

        try {
            TestApplication simpleApplication = new TestApplication(args[0]);
        } catch (Exception ex) {
            System.err.println("Command failed: " + ex);
            ex.printStackTrace();
        }
    }

    /** The time that creating the model started */
    protected static long _parseStartTime;
}
