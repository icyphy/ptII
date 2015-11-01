/* Run the G4LTL tool.

   Copyright (c) 2013-2014 The Regents of the University of California.
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
   COPYRIGHTENDKEY 2
 */

package ptolemy.vergil.basic.imprt.g4ltl;

import g4ltl.SolverUtility;
import g4ltl.utility.ResultLTLSynthesis;
import g4ltl.utility.SynthesisEngine;

import java.io.File;
import java.util.Iterator;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// G4LTL

/**
   Run the LTL synthesis (G4LTL) tool on a model.

   <p>"G4LTL is a standalone tool and a Java library for automatically
   generating controllers realizing linear temporal logic (LTL).</p>

   <p>See <a href="http://sourceforge.net/projects/g4ltl/#in_browser">http://sourceforge.net/projects/g4ltl/</a></p>

   <p>This class uses classes defined in $PTII/lib/g4ltl.jar.  See
   $PTII/lib/g4ltl-license.htm.</p>


   <p>This class defines static methods for generating moml and
   updating models based on the contents of an LTL file.</p>

   @author Chihhong (Patrick) Cheng (Fortiss), Christopher Brooks. Based on ExportPDFAction by Edward A. Lee
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public class G4LTL {
    /** Given a Linear Temporal Logic (LTL) file, generate the
     * corresponding MoML.
     * @param solver The G4LTL Solver to use.
     * @param ltlFile The ltl file.
     * @param optionTechnique and integer where 0 means CoBeuchi, 1 means Beuchi
     * @param unrollSteps The number of unroll steps.
     * @param findStrategy True if a strategy should be found, false if a counter-strategy should
     * be found.  Typically a strategy is found first and then if the result does not contain a "&lt;",
     * this method is called with findStrategy set to false to find a counter-strategy.
     * @return The moml of a state machine that represents the LTL file.
     * @exception Exception If thrown while synthesizing.
     */
    public static ResultLTLSynthesis synthesizeFromFile(SolverUtility solver,
            File ltlFile, int optionTechnique, int unrollSteps,
            boolean findStrategy) throws Exception {
        //System.out.println("G4LTL.synthesizeFromFile(): " + ltlFile + " " + optionTechnique + " " + unrollSteps + " " + findStrategy);
        ResultLTLSynthesis result = solver.synthesizeFromFile(ltlFile,
                optionTechnique, unrollSteps,
                SynthesisEngine.OUTPUT_FSM_ACTOR_PTOLEMY, findStrategy);
        return result;
    }

    /** Given a Linear Temporal Logic (LTL) file, generate the
     * corresponding MoML and update the MoML.
     *
     * <p>This is the main entry point for non-gui use of the g4ltl
     * package.  If finding a strategy fails the gui may want to ask
     * the user if they want to find a counter strategy.</p>
     *
     * @param ltlFile The ltl file.
     * @param optionTechnique and integer where 0 means CoBeuchi, 1 means Beuchi
     * @param unrollSteps The number of unroll steps.
     * @param findStrategy True if a strategy should be found, false if a counter-strategy should
     * be found. If a strategy cannot be found, then a counter-strategy is searched for.
     * @param context The context for the change.  One way to get the
     * context is by calling basicGraphFrame.getModel().
     * @return The name of the state machine that was created.
     * @exception Exception If thrown while synthesizing.
     */
    public static String generateMoML(File ltlFile, int optionTechnique,
            int unrollSteps, boolean findStrategy, NamedObj context)
                    throws Exception {
        SolverUtility solver = new SolverUtility();
        ResultLTLSynthesis result = G4LTL.synthesizeFromFile(solver, ltlFile,
                optionTechnique, unrollSteps, /*SynthesisEngine.OUTPUT_FSM_ACTOR_PTOLEMY,*/
                findStrategy);
        if (findStrategy && result.getMessage1().startsWith("<") == false) {
            result = solver.synthesizeFromFile(ltlFile, optionTechnique,
                    unrollSteps, SynthesisEngine.OUTPUT_FSM_ACTOR_PTOLEMY,
                    false);
        }
        String name = updateModel(result.getMessage1(), context);
        return name;
    }

    /** Update the model with MoML that was presumably generated by
     * generateMoML().
     * @param updatedMoML The moml from generateMoML()
     * @param context The context for the change.  One way to get the
     * context is by calling basicGraphFrame.getModel().
     * @return The name of the state machine that was created.
     * @exception Exception If thrown while synthesizing.
     */
    public static String updateModel(String updatedMoML, NamedObj context)
            throws Exception {
        // FIXME: instantiating a new parser each time could be a
        // mistake. What about leaks?  What about initialization of
        // the filters?
        MoMLParser parser = new MoMLParser();
        NamedObj model = parser.parse(updatedMoML);
        String moml = "";
        String updatedName = "";
        if (model != null) {
            // Change the name of the output module to an unused name.
            moml = model.exportMoMLPlain();
            String moduleName = "model";
            int i = 1;
            Iterator<NamedObj> containedObjects = context
                    .containedObjectsIterator();
            while (containedObjects.hasNext()) {
                if (containedObjects.next().getName()
                        .equals(moduleName + String.valueOf(i))) {
                    containedObjects = context.containedObjectsIterator();
                    i++;
                }
            }
            // Change the module to the updated name, and commit changes
            updatedName = moduleName + String.valueOf(i);
            moml = moml.replaceFirst(moduleName, updatedName);
            MoMLChangeRequest request = new MoMLChangeRequest(context, context,
                    moml);
            context.requestChange(request);
        }
        return updatedName;
    }
}
