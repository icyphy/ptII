/* An application that executes non-graphical
 models specified on the command line.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.moml;

import java.io.File;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

//////////////////////////////////////////////////////////////////////////
//// MoMLCommandLineApplication

/** A application that reads command line arguments that set parameters
    and a .xml file naming a model, sets the parameters and runs the model.

 <p>MoMLApplication sets the look and feel, which starts up Swing,
 so we can't use MoMLApplication for non-graphical simulations.

 <p>The parent class, @{link ptolemy.moml.MoMLSimpleApplication} does
 not handle command line parameters that set parameters

 For example to use this class, try:
 <pre>
 java -classpath $PTII ptolemy.moml.MoMLCommandLineApplication -myParameter '&amp;quot;Hello, World.&amp;quot;' test/MoMLCommandLineApplicationTest.xml
 </pre>
or
 <pre>
 $PTII/bin/ptinvoke ptolemy.moml.MoMLCommandLineApplication -myParameter '"&amp;quot;Hellow, World.&amp;quot;"' test/MoMLCommandLineApplicationTest.xml
 </pre>
 <p>
 If a Ptolemy model is instantiated on the command line, either
 by giving a MoML file or a -class argument, then parameters of that
 model can be set on the command line.  The syntax is:
 <pre>
 $PTII/bin/ptinvoke ptolemy.moml.MoMLCommandLineApplication -<i>parameterName</i> <i>value</i> <i>modelFile.xml</i>
 </pre>
 where <i>parameterName</i> is the name of a parameter relative to
 the top level of a model or the director of a model.  For instance,
 if foo.xml defines a toplevel entity named <code>x</code> and
 <code>x</code> contains an entity named <code>y</code> and a
 parameter named <code>a</code>, and <code>y</code> contains a
 parameter named <code>b</code>, then:
 <pre>
 $PTII/bin/ptinvoke ptolemy.moml.MoMLCommandLineApplication  -a 5 -y.b 10 foo.xml
 </pre>
 would set the values of the two parameters.

 <p>Note that strings need to be carefully backslashed, so to set a
 parameter named <code>c</code> to the string <code>"bar"</code> it
 might be necessary to do something like:
 <pre>
 $PTII/bin/ptinvoke ptolemy.moml.MoMLCommandLineApplication -a 5 -y.b 10 -c '"&amp;quot;bar&amp;quot;" foo.xml
 </pre>
 The reason the single quotes and double quotes are necessary is because <code>ptinvoke</code>
 is a shell script which tends to strip off the double quotes.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (eal)
 */
public class MoMLCommandLineApplication extends MoMLSimpleApplication {

    /** Parse a MoML file that contains a model, update the parameters
     *  and run the model.
     *  @param args An array of strings, where the last element names
     *  a MoML file that contains a Ptolemy II model.  The string should be
     *  a relative pathname.  Other elements may name model parameters
     *  and values for the parameters.  Parameter names should begin
     *  with a "-" and be followed by the value for the parameter.
     *  @exception Throwable If there was a problem parsing
     *  or running the model.
     */
    public MoMLCommandLineApplication(String[] args) throws Throwable {
        MoMLParser parser = new MoMLParser();

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
        CompositeActor toplevel = (CompositeActor) parser.parse(null, new File(
                args[args.length - 1]).toURI().toURL());

        _manager = new Manager(toplevel.workspace(), "MoMLSimpleApplication");
        toplevel.setManager(_manager);
        toplevel.addChangeListener(this);
        _manager.addExecutionListener(this);

        // This code is very similar to code in
        // ptolemy.actor.gui.MoMLApplication
        String parameterName = null;
        String parameterValue = null;
        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i];
            if (arg.trim().startsWith("-")) {
                if (i >= args.length - 1) {
                    throw new IllegalActionException("Cannot set "
                            + "parameter " + arg + " when no value is "
                            + "given.");
                }

                // Save in case this is a parameter name and value.
                parameterName = arg.substring(1);
                parameterValue = args[i + 1];

                // First, see if there are any attributes in the top level
                Attribute attribute = toplevel.getAttribute(parameterName);
                if (attribute instanceof Settable) {
                    // Use a MoMLChangeRequest so that visual rendition (if
                    // any) is updated and listeners are notified.
                    String moml = "<property name=\"" + parameterName
                            + "\" value=\"" + parameterValue + "\"/>";
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            toplevel, moml);
                    toplevel.requestChange(request);
                }

                // Now try for parameters in the director
                Director director = toplevel.getDirector();

                if (director != null) {
                    attribute = director.getAttribute(parameterName);

                    if (attribute instanceof Settable) {
                        // Use a MoMLChangeRequest so that visual rendition (if
                        // any) is updated and listeners are notified.
                        String moml = "<property name=\"" + parameterName
                                + "\" value=\"" + parameterValue + "\"/>";
                        MoMLChangeRequest request = new MoMLChangeRequest(this,
                                director, moml);
                        director.requestChange(request);
                    }
                }
                i++;
            } else {
                // Unrecognized option.
                throw new IllegalActionException("Unrecognized option: " + arg);
            }
        }

        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4178
        // "error handling with MoMLCommandLineApplication"
        _manager.run();

        // PtExecuteApplication uses _activeCount to determine when
        // the models are done.  We can't do that here because
        // executeError() might be called from a different thread.
        // PtExecuteApplication handles this by deferring the change
        // to the Swing event thread.  We don't have a Swing event thread,
        // so we are stuck with a busy loop.
        while (!_executionFinishedOrError) {
            Thread.yield();
        }

        if (_sawThrowable != null) {
            throw _sawThrowable;
        }
    }

    /** Create an instance of a single model and run it.
     *  @param args The command-line arguments naming the .xml file to run
     */
    public static void main(String[] args) {
        try {
            new MoMLCommandLineApplication(args);
        } catch (Throwable ex) {
            System.err.println("Command failed: " + ex);
            ex.printStackTrace();
        }
    }
}
