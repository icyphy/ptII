/* An application that executes non-graphical
   models specified on the command line.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

import java.io.File;

//////////////////////////////////////////////////////////////////////////
//// MoMLSimpleApplication
/** A simple application that reads in a .xml file as a command
line argument and runs it.

<p>MoMLApplication sets the look and feel, which starts up Swing,
so we can't use MoMLApplication for non-graphical simulations.

<p>We implement the ChangeListener interface so that this
class will get exceptions thrown by failed change requests.

For example to use this class, try:
<pre>
java -classpath $PTII ptolemy.actor.gui.MoMLSimpleApplication ../../../ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml
</pre>


@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class MoMLSimpleApplication implements ChangeListener {

    /** A Nullary constructor is necessary so that we can extends this
     *  base class with a subclass
     *  @exception Exception Not thrown in this base class
     */
    public MoMLSimpleApplication() throws Exception {
    }

    /** Parse the xml file and run it.
     *  @param xmlFileName A string that refers to an MoML file that
     *  contains a Ptolemy II model.  The string should be
     *  a relative pathname.
     *  @exception Exception If there was a problem parsing
     *  or running the model.
     */
    public MoMLSimpleApplication(String xmlFileName) throws Exception {
        MoMLParser parser = new MoMLParser();

        // The test suite calls MoMLSimpleApplication multiple times,
        // and the list of filters is static, so we reset it each time
        // so as to avoid adding filters every time we run an auto test.

        // We set the list of MoMLFilters to handle Backward Compatibility.
        parser.setMoMLFilters(BackwardCompatibility.allFilters());

        // Filter out any graphical classes.
        parser.addMoMLFilter(new RemoveGraphicalClasses());

        // If there is a MoML error, then throw the exception as opposed
        // to skipping the error.  If we call StreamErrorHandler instead,
        // then the nightly build may fail to report MoML parse errors
        // as failed tests
        //parser.setErrorHandler(new StreamErrorHandler());

        // We use parse(URL, URL) here instead of parseFile(String)
        // because parseFile() works best on relative pathnames and
        // has problems finding resources like files specified in
        // parameters if the xml file was specified as an absolute path.
        CompositeActor toplevel = (CompositeActor) parser.parse(null,
                new File(xmlFileName).toURL());

        _manager = new Manager(toplevel.workspace(),
                "MoMLSimpleApplication");
        toplevel.setManager(_manager);
        toplevel.addChangeListener(this);
        _manager.execute();
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

        // We can't throw an Exception here because this method in
        // the base class does not throw Exception.

        String description = "";
        if (change != null) {
            description = change.getDescription();
        }
        
        throw new RuntimeException("MoMLSimplApplication.changeFailed(): "
                + description + " failed: ",
                exception);
    }

    /** Create an instance of a single model and run it
     *  @param args The command-line arguments naming the .xml file to run
     */
    public static void main(String args[]) {
        try {
            new MoMLSimpleApplication(args[0]);
        } catch (Exception ex) {
            System.err.println("Command failed: " + ex);
            ex.printStackTrace();
        }
    }

    /** Execute the same model again.
     */
    public void rerun() throws Exception {
        _manager.execute();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Manager _manager = null;
}
