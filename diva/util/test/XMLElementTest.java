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
package diva.util.test;

import java.util.TreeMap;

import diva.util.jester.TestCase;
import diva.util.jester.TestFailedException;
import diva.util.jester.TestHarness;
import diva.util.jester.TestSuite;
import diva.util.xml.XmlElement;

/**
 * A test suite for XmlElement
 *
 * @author John Reekie
 * @version $Id$
 */
public class XMLElementTest extends TestSuite {
    /** Constructor
     */
    public XMLElementTest(TestHarness harness) {
        setTestHarness(harness);
    }

    /**
     * runSuite()
     */
    @Override
    public void runSuite() {
        testConstructor();
        testAttributes();
        testElements();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Test construction of XmlElement
     */
    public void testConstructor() {
        runTestCase(new TestCase("XmlElement constructor") {
            XmlElement elt = null;

            @Override
            public void run() throws Exception {
                TreeMap attrs = new TreeMap();
                attrs.put("name0", "value0");
                attrs.put("name1", "value1");
                elt = new XmlElement("element", attrs);
            }

            @Override
            public void check() throws TestFailedException {
                String result = "<element name0=\"value0\" name1=\"value1\"></element>\n";
                assertEquals(result, elt.toString(),
                        result + " != " + elt.toString());
            }
        });
    }

    /** Test attribute setting, getting, and removing
     */
    public void testAttributes() {
        runTestCase(new TestCase("XmlElement attributes") {
            XmlElement elt = null;

            @Override
            public void run() throws Exception {
                elt = new XmlElement("element");
                elt.setAttribute("name0", "value0");
                elt.setAttribute("name1", "value1");
            }

            @Override
            public void check() throws TestFailedException {
                String result = "<element name0=\"value0\" name1=\"value1\"></element>\n";
                assertEquals(result, elt.toString(),
                        result + " != " + elt.toString());

                assertEquals("value0", elt.getAttribute("name0"),
                        "Attribute name0");
                assertEquals("value1", elt.getAttribute("name1"),
                        "Attribute name1");

                elt.setAttribute("name0", "value2");
                assertEquals("value2", elt.getAttribute("name0"),
                        "Attribute name0 after setting");

                result = "<element name1=\"value1\"></element>\n";
                elt.removeAttribute("name0");
                assertEquals(result, elt.toString(),
                        result + " != " + elt.toString());
            }
        });
    }

    /** Test children manipulation
     */
    public void testElements() {
        runTestCase(new TestCase("XmlElement children") {
            XmlElement elt0 = null;

            XmlElement elt1 = null;

            XmlElement elt2 = null;

            @Override
            public void init() throws Exception {
                elt0 = new XmlElement("element0");
                elt1 = new XmlElement("element1");
                elt2 = new XmlElement("element2");
            }

            @Override
            public void run() throws Exception {
                elt0.addElement(elt1);
                elt1.addElement(elt2);
            }

            @Override
            public void check() throws TestFailedException {
                String result = "<element0>\n<element1>\n<element2></element2>\n</element1>\n</element0>\n";
                assertEquals(result, elt0.toString(),
                        result + " != " + elt0.toString());

                assertExpr(elt0.containsElement(elt1),
                        "elt0.containsElement(elt1)");
                assertExpr(elt1.containsElement(elt2),
                        "elt1.containsElement(elt2)");

                assertExpr(!elt1.containsElement(elt0),
                        "!elt1.containsElement(elt0)");
                assertExpr(!elt2.containsElement(elt1),
                        "!elt2.containsElement(elt1)");

                assertExpr(!elt0.containsElement(elt2),
                        "!elt0.containsElement(elt2)");
                assertExpr(!elt2.containsElement(elt0),
                        "!elt2.containsElement(elt0)");

                // No go ahead and remove some stuff
                result = "<element0></element0>\n";
                elt0.removeElement(elt1);
                assertEquals(result, elt0.toString(),
                        result + " != " + elt0.toString());
                assertExpr(!elt0.containsElement(elt1),
                        "!elt0.containsElement(elt1)");
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and
     * run all tests on it.
     */
    public static void main(String[] argv) {
        new XMLElementTest(new TestHarness()).run();
    }
}
