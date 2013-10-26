/* JUnit test for RESTGetHandler

 Copyright (c) 2010-2011 The Regents of the University of California.
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

package ptserver.test.junit;

import org.junit.Before;
import org.junit.Test;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentRelation;
import ptserver.actor.lib.io.RESTGetHandler;

///////////////////////////////////////////////////////////////////
//// RESTGetHandler
/**
 * Test for RESTGetHandler.  This test creates and executes the actor
 * (preinitialize, initialize, prefire, fire, postfire) in order to achieve
 * code coverage.  However, the actor's results are not checked here.
 * For results checking, please run the model in ptserver.actor.lib.io.test
 * @author ltrnc
 * @version $Id$
 * @see ptolemy.apps.graph.MinimumDistanceCalculatorTest
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ltrnc)
 * @Pt.AcceptedRating Red (ltrnc)
 */
public class RESTGetHandlerTest {

    /** Model is created in the setUp() method and stored here. */
    public CompositeActor testModel;

    /** The RESTGetHandler actor to test */
    public RESTGetHandler handler;

    @Before
    public void setUp() throws Exception {
        // Build test model.
        // Test model from MinimumDistanceCalculatorTest
        testModel = new CompositeActor();

        // Actor a
        AtomicActor a = new AtomicActor(testModel, "a");
        IOPort aOut = new IOPort(a, "aOut", false, true);

        // Actor b
        AtomicActor b = new AtomicActor(testModel, "b");
        IOPort bIn = new IOPort(b, "bIn", true, false);
        IOPort bOut = new IOPort(b, "bOut", false, true);

        // Actor c
        AtomicActor c = new AtomicActor(testModel, "c");
        IOPort cIn1 = new IOPort(c, "cIn1", true, false);
        IOPort cIn2 = new IOPort(c, "cIn2", true, false);

        // RESTGetHandler
        handler = new RESTGetHandler(testModel, "RESTGetHandler");

        // Connections
        ComponentRelation relation = testModel.connect(aOut, bIn);
        testModel.connect(bOut, cIn1);
        cIn2.link(relation);
    }

    @Test
    public void test() throws Exception {
        handler.preinitialize();
        handler.initialize();
        handler.prefire();
        handler.fire();
        handler.postfire();
    }
}
