/* An application for testing the component domain.
 Copyright (c) 1998-2013 The Regents of the University of California.
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

 @ProposedRating Red
 @AcceptedRating Red (cxh)
 */
package ptolemy.component.test;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;

import ptolemy.data.IntToken;
import ptolemy.data.TupleToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MCApplication

/**
 * An application for testing the component domain.
 * <p>To run this, do
 * cd $PTII/ptolemy/component/test
 * java -classpath $PTII ptolemy.component.test.MCApplication
 *
 * @author  Yang Zhao
 * @version $Id$
 * @since Ptolemy II 10.0
 * @version $Id$
 */
public class MCApplication {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Build the model.
     *  <p>Read the model "NCApplication.xml" from the current directory.
     *  @param args Ignored
     *  @exception Exception If there is a problem reading the model.
     */
    public static void main(String[] args) throws Exception {
        CompositeEntity _toplevel;

        try {
            Workspace workspace = new Workspace("NC");
            _toplevel = new CompositeEntity(workspace);
            _toplevel.setName("NCTest");

            Counter counter = new Counter(_toplevel, "Couter");
            Leds leds = new Leds(_toplevel, "Leds");
            _toplevel.connect(counter.output, leds.display, "R1");

            // Generate moml file to be tested in vergil.
            StringWriter buffer = new StringWriter();
            _toplevel.exportMoML(buffer);

            String fileName = "NCApplication.xml";
            FileOutputStream file = null;
            PrintStream out = null;
            try {
                file = new FileOutputStream(fileName);

                out = new PrintStream(file);
                out.println(buffer);
                out.flush();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on " + fileName);
                        throwable.printStackTrace();
                    }
                }
            }

            // Execute the model.
            counter.initialize();

            IntToken[] t = new IntToken[1];
            t[0] = new IntToken(2);

            TupleToken arg = new TupleToken(t);

            for (int i = 0; i < 10; i++) {
                counter.increment.call(arg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
