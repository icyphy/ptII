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

package ptolemy.actor.lib.jxta;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;

//////////////////////////////////////////////////////////////////////////
//// MoMLSimpleApplication
/** A simple application that reads in a .xml file as a command
line argument and modify it.


@author Yang Zhao
@version $Id$
@since Ptolemy II 2.0
*/
public class MoMLSimpleApplication extends NamedObj implements ChangeListener {

    /** A Nullary constructor is necessary so that we can extends this
     *  base class with a subclass
     *  @exception Exception Not thrown in this base class
     */
    public MoMLSimpleApplication() throws Exception {
    }

    /** Parse the xml file.
     */
    public MoMLSimpleApplication(String xmlFilename) throws Exception {
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
        toplevel = (CompositeActor) parser.parse(null,
                new File(xmlFilename).toURL());

        _manager =new Manager(toplevel.workspace(),
                "MoMLSimpleApplication");
        toplevel.setManager(_manager);
        toplevel.addChangeListener(this);
        //_manager.execute();
    }

    public CompositeActor toplevel;
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
    public static void main(String args[]) {
        try {
            MoMLSimpleApplication simpleApplication =
                new MoMLSimpleApplication(args[0]);
            System.out.println("open a new simpleApplication");
            StringBuffer moml = new StringBuffer("<group><entity  name=\"Const2\" class=\"ptolemy.actor.lib.Const\">");
            moml.append("<property name=\"value\" class=\"ptolemy.data.expr.Parameter\" value=\"2\">");
            moml.append("</property>");
            moml.append("<port name=\"in\" class=\"ptolemy.actor.TypedIOPort\">");
            moml.append("<property name=\"input\"/>");
            moml.append("</port>") ;
            moml.append("<port name=\"out\" class=\"ptolemy.actor.TypedIOPort\">");
            moml.append("<property name=\"output\"/>");
            moml.append("</port>");
            moml.append("</entity></group>");

            System.out.println("consturct request change");

            //NamedObj parent = MoMLChangeRequest.getDeferredToParent();
            ChangeRequest request = new MoMLChangeRequest(
                    simpleApplication,            // originator
                    simpleApplication.toplevel,          // context
                    moml.toString(), // MoML code
                    null);           // base

            System.out.println("consturct a moml change request");

            simpleApplication.toplevel.requestChange(request);

            StringWriter buffer = new StringWriter();
            simpleApplication.toplevel.exportMoML(buffer) ;
            // FIXME: hardwired path
            String fileName = 
                "c:/Cygwin/home/ellen_zh/ptII/ptolemy/actor/lib/jxta/model.xml";
            FileOutputStream file = null;
            try {
                file = new FileOutputStream(fileName);
                PrintStream out = new PrintStream(file);
                out.println(buffer);
                out.flush();
            } finally {
                if (file != null) {
                    try {
                        file.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on " + fileName);
                        throwable.printStackTrace();
                    }
                }
            }
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

    private NamedObj _context;
}
