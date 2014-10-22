/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.graph.test;

import diva.graph.basic.BasicGraphModel;
import diva.graph.modular.CompositeNode;
import diva.graph.modular.Edge;
import diva.graph.modular.Node;
import diva.util.jester.TestCase;
import diva.util.jester.TestFailedException;
import diva.util.jester.TestHarness;
import diva.util.jester.TestSuite;

/**
 * A test suite for the BasicGraphModel class.
 *
 * @author John Reekie
 * @version $Id$
 */
public class BasicGraphModelTest extends TestSuite {
    /** The graph factory interface.
     */
    public interface GraphFactory {
        /** Create a BasicGraphModel.
         *  @return a BasicGraphModel.
         */
        public BasicGraphModel createGraph();
    }

    /** The factory for the BasicGraphModel class.
     */
    public static class BasicGraphModelFactory implements GraphFactory {
        /** Create a BasicGraphModel.
         *  @return a BasicGraphModel.
         */
        @Override
        public BasicGraphModel createGraph() {
            return new BasicGraphModel();
        }
    }

    /**
     * The unit factory.
     */
    private GraphFactory factory;

    /** Constructor.
     */
    public BasicGraphModelTest(TestHarness harness, GraphFactory factory) {
        setTestHarness(harness);
        setFactory(factory);
        this.factory = factory;
    }

    /** Run the test.
     *
     */
    @Override
    public void runSuite() {
        testEmpty();
        testStarConnected();
        testBig();
    }

    ///////////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and run all tests on it.
     */
    public static void main(String[] argv) {
        new BasicGraphModelTest(new TestHarness(),
                new BasicGraphModelTest.BasicGraphModelFactory()).run();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Perform tests on an empty graph.
     */
    public void testEmpty() {
        runTestCase(new TestCase("Empty graph") {
            //BasicGraphModel g;

            @Override
            public void init() throws Exception {
                /*g = */factory.createGraph();
            }

            @Override
            public void run() throws Exception {
                ;
            }

            @Override
            public void check() throws TestFailedException {
                // assertExpr(g.getNodeCount(g) == 0, "Node count != 0");
            }
        });
    }

    /** Test a star-connected graph.
     */
    public void testStarConnected() {
        runTestCase(new TestCase("Star-connected from single node") {
            BasicGraphModel g;

            CompositeNode root;

            @Override
            public void init() throws Exception {
                startTimer();
                g = factory.createGraph();
                root = (CompositeNode) g.getRoot();
            }

            @Override
            public void run() throws Exception {
                Node first = g.createNode(null);
                g.addNode(this, first, root);

                for (int i = 1; i < 32; i++) {
                    Node n = g.createNode(null);
                    g.addNode(this, n, root);

                    Edge e = g.createEdge(null);
                    g.connectEdge(this, e, first, n);
                }

                stopTimer();
            }

            @Override
            public void check() throws TestFailedException {
                assertExpr(g.getNodeCount(root) == 32, "Node count != 32");
            }
        });
    }

    /** Test a large (64 knode) graph. Unfortunately, something
     * seems to hang after running this test. Presumably there's
     * a problem with the diva.graph package (apart from the bug
     * that won't even let you get the number of nodes).
     */
    public void testBig() {
        runTestCase(new TestCase("Test 64 knode graph") {
            BasicGraphModel g;

            CompositeNode root;

            //Node[] nodes = new Node[65536];

            @Override
            public void init() throws Exception {
                startTimer();
                g = factory.createGraph();
                root = (CompositeNode) g.getRoot();
            }

            @Override
            public void run() throws Exception {
                Node first = g.createNode(null);
                g.addNode(this, first, root);

                //nodes[0] = first;
                for (int i = 1; i < 65536; i++) {
                    Node n = g.createNode(null);
                    g.addNode(this, n, root);

                    //nodes[i] = n;
                    Edge e = g.createEdge(null);
                    g.connectEdge(this, e, first, n);
                }

                stopTimer();
            }

            @Override
            public void check() throws TestFailedException {
                assertExpr(g.getNodeCount(root) == 65536, "Node count != 65536");
            }
        });
    }
}
