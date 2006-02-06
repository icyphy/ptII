/* A parser for MoML (modeling markup language)

 Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.kernel.CompositeEntity;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// MoMLParserLeak

/** 
 Leak memory in MoMLParser by throwing an Exception.
 <p> Under Java 1.4, run this with:
 <pre>
 java -Xrunhprof:depth=15 -classpath "$PTII;." ptolemy.moml.test.MoMLParserLeak
 </pre>
 and then look in java.hprof.txt.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class MoMLParserLeak {

    /** Attempt to leak code by parsing MoML that looks for a class
     *  that does not exist.
     */
    public static CompositeEntity leak() throws Exception {
        //MoMLParser parser = new MoMLParser();
        CompositeEntity toplevel = (CompositeEntity) parser
                .parse("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"\n"
                        + "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">\n"
                        + "<entity name=\"top\" class=\"ptolemy.kernel.CompositeEntity\">\n"
                        + "<entity name=\"myRamp\" class=\"ptolemy.actor.lib.Ramp\"/>\n"
                        + "<entity name=\"notaclass\" class=\"Not.A.Class\"/>\n"
                        + "</entity>\n");
        return toplevel;
    }

    /** Attempt to leak code by parsing MoML that looks for a class
     *  that does not exist.
     *  @param args Ignored.
     *  @exception Exception if there is a problem parsing
     */
    public static void main(String[] args) throws Exception {
        parser = new MoMLParser();
        //CompositeEntity toptop = new CompositeEntity();
        //parser.setContext(toptop);
        try {
            CompositeEntity toplevel = leak();
            toplevel.setContainer(null);
            toplevel = null;
            // If we don't set parser to null or otherwise force it to
            // go out of scope, then we leak memory.
            System.out.println("Setting parser to null");
            parser = null;
        } catch (Exception ex) {
            // If we don't gc here, then references to Ramp might exist
            // if if we _don't_ throw an exception.
            System.gc();
            System.out.println("Sleeping for 2 seconds for any possible gc.");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            ex.printStackTrace();
            throw ex;
        }
    }

    // Note that the parser is what actually leaks here.
    public static MoMLParser parser;
}
