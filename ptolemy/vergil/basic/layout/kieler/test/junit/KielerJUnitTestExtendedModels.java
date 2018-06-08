/* JUnit test the Kieler Layout mechanism.

 Copyright (c) 2011-2018 The Regents of the University of California.
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

package ptolemy.vergil.basic.layout.kieler.test.junit;

///////////////////////////////////////////////////////////////////
//// KielerJUnitTestExtendedModels
/**
 * Test out Kieler by open models, using Kieler to layout the graph
 * and then doing undo and redo.
 *
 * <p>This class uses models such as ptolemy/domains/modal/demo/SystemLevelType,
 * which is not shipped with Cape Code, so these tests are in a
 * separate file.</p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class KielerJUnitTestExtendedModels extends KielerJUnitTest {

    /* ----------------------------
     *          Actor Tests
     * ---------------------------- */

    /** Test the layout of the CarTracking model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void runCarTracking() throws Exception {

        _layoutTest(
                "$CLASSPATH/ptolemy/domains/continuous/demo/CarTracking/CarTracking.xml",
                false);
    }

    /** Test the layout of the Router model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void runRouter() throws Exception {
        _layoutTest("$CLASSPATH/ptolemy/demo/ExecDemos/Demos/Router.xml",
                false);
    }

    /* ----------------------------
     *          FSM Tests
     * ---------------------------- */
    /** Test the layout of the CSPDomain FSM Model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void runCSPDomain() throws Exception {
        _layoutTest(
                "$CLASSPATH/ptolemy/domains/modal/demo/SystemLevelType/CSPDomain.xml",
                false);
    }

    /** Test the layout of the Interrupter FSM Model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void runInterrupter() throws Exception {
        _layoutTest(
                "$CLASSPATH/ptolemy/domains/modal/demo/SystemLevelType/Interrupter.xml",
                false);
    }

    /** Test the layout of the Monitor FSM Model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void runMonitor() throws Exception {
        _layoutTest(
                "$CLASSPATH/ptolemy/domains/modal/demo/SystemLevelType/Monitor.xml",
                false);
    }
}
