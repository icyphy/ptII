/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
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
 * @version $Revision$
 */
public class BasicGraphModelTest extends TestSuite {

    /** The graph factory interface
     */
    public interface GraphFactory {
        public BasicGraphModel createGraph ();
    }

    /** The factory for the BasicGraphModel class
     */
    public static class BasicGraphModelFactory implements GraphFactory {
        public BasicGraphModel createGraph () {
            return new BasicGraphModel();
        }
    }

    /**
     * The unit factory
     */
    private GraphFactory factory;

    /** Constructor
     */
    public BasicGraphModelTest (TestHarness harness, GraphFactory factory) {
        setTestHarness(harness);
        setFactory(factory);
        this.factory = factory;
    }

    /**
     * runSuite()
     */
    public void runSuite () {
        testEmpty();
        testStarConnected();
        testBig();
    }

    ////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and run all tests on it.
     */
    public static void main (String argv[]) {
         new BasicGraphModelTest(new TestHarness(),
                new BasicGraphModelTest.BasicGraphModelFactory()).run();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Perform tests on an empty graph
     */
    public void testEmpty () {
        runTestCase(new TestCase("Empty graph") {
            BasicGraphModel g;

            public void init () throws Exception {
                g = factory.createGraph();
            }
            public void run () throws Exception {
                ;
            }
            public void check () throws TestFailedException {
                // assertExpr(g.getNodeCount(g) == 0, "Node count != 0");
            }
        });
    }

    /** Test a star-connected graph
     */
    public void testStarConnected () {
        runTestCase(new TestCase("Star-connected from single node") {
            BasicGraphModel g;
            CompositeNode root;

            public void init () throws Exception {
                startTimer();
                g = factory.createGraph();
                root = (CompositeNode) g.getRoot();
            }
            public void run () throws Exception {
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
            public void check () throws TestFailedException {
                assertExpr(g.getNodeCount(root) == 32, "Node count != 32");
            }
        });
    }

    /** Test a large (64 knode) graph. Unfortunately, something
     * seems to hang after running this test. Presumably there's
     * a problem with the diva.graph package (apart from the bug
     * that won't even let you get the number of nodes).
     */
    public void testBig () {
        runTestCase(new TestCase("Test 64 knode graph") {
            BasicGraphModel g;
            CompositeNode root;
            Node[] nodes = new Node[65536];

            public void init () throws Exception {
                startTimer();
                g = factory.createGraph();
                root = (CompositeNode) g.getRoot();
            }
            public void run () throws Exception {
                Node first = g.createNode(null);
                g.addNode(this, first, root);
                //nodes[0] = first;

                for (int i = 1; i < 65536; i++) {
                    Node n = g.createNode(null);
                    g.addNode(this, n, root);
                    //nodes[i] = n;
                    Edge e = g.createEdge(null);
                    int s = i / 2;
                    g.connectEdge(this, e, first, n);
                }
                stopTimer();
            }
            public void check () throws TestFailedException {
                assertExpr(g.getNodeCount(root) == 65536, "Node count != 65536");
            }
        });
    }
}
