/* Run a model over and over again.

Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.actor.gui.test;

import ptolemy.actor.gui.MoMLSimpleApplication;


/////////////////////////////////////////////////////////////////////////
//// Rerun

/**
   Run a MoML model over and over again.
   This class reads in a MoML file and executes it over and over again.
   The time and memory usage stats are printed on stdout.

   For example to run a model 100 times:
   <pre>
   cd $PTII/ptolemy/actor/gui/test
   java -classpath $PTII ptolemy.actor.gui.test.Rerun ../../lib/test/auto/Sinewave.xml
   </pre>

   To run the model 10 times:
   <pre>
   java -classpath $PTII ptolemy.actor.gui.test.Rerun 10 ../../lib/test/auto/Sinewave.xml
   </pre>

   To create a ptplot file of the times and memory:
   <pre>
   awk '{  t[NR] = $1;
   m[NR] = substr($4, 0, length($4) - 1);
   f[NR] = substr($6, 0, length($6) - 1);
   }
   END {  print "dataset: time (ms)";
   for (i=1;i<=NR;i++) {print i, t[i]};
   print "dataset: memory (K)";
   for (i=1;i<=NR;i++) {print i, m[i]};
   print "dataset: free (K)";
   for (i=1;i<=NR;i++) {print i, f[i]};
   }' /tmp/t > /tmp/t.plt

   <pre>

   @author Christopher Hylands Brooks
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class Rerun extends MoMLSimpleApplication {
    /** Parse the xml file and run it.
     *  @param xmlFileName A string that refers to an MoML file that
     *  contains a Ptolemy II model.  The string should be
     *  a relative pathname.
     *  @exception Throwable If there was a problem parsing
     *  or running the model.
     */
    public Rerun(String xmlFileName) throws Throwable {
        super(xmlFileName);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an instance of a single model and run it
     *  @param args The command-line arguments naming the .xml file to run
     */
    public static void main(String[] args) {
        try {
            // Constructing the Rerun object runs the model once,
            // so we run it 100-1 more times.
            int runs = 99;
            String xmlFileName = null;

            if (args.length == 2) {
                try {
                    runs = Integer.parseInt(args[0]) - 1;
                } catch (Exception ex) {
                    System.err.println("Failed to parse '" + args[0]
                            + "', using " + runs + " instead.");
                    ex.printStackTrace();
                }

                xmlFileName = args[1];
            } else {
                if (args.length == 1) {
                    xmlFileName = args[0];
                } else {
                    throw new IllegalArgumentException(
                            "Usage: java -classpath $PTII ptolemy.actor.gui.test.Rerun [reRuns] model.xml\n"
                            + "    Where reRuns is an integer, "
                            + "defaults to 100");
                }
            }

            Rerun reRun = new Rerun(xmlFileName);

            for (int i = 0; i < runs; i++) {
                reRun.rerun();
            }
        } catch (Throwable ex) {
            System.err.println("Command failed: " + ex);
            ex.printStackTrace();
        }
    }
}
