/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import diva.util.jester.TestCase;
import diva.util.jester.TestFailedException;
import diva.util.jester.TestHarness;
import diva.util.jester.TestSuite;
import diva.util.xml.XmlDocument;
import diva.util.xml.XmlReader;
import diva.util.xml.XmlWriter;

/**
 * A test suite for XMLParser and XMLPrinter
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public class XMLParserTest extends TestSuite {

    /** Constructor
     */
    public XMLParserTest (TestHarness harness) {
        setTestHarness(harness);
    }

    /**
     * runSuite()
     */
    public void runSuite () {
        testParse();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Test construction of XMLParser
     */
    public void testParse () {
        runTestCase(new TestCase("XMLParse parse") {
                URL url;
                XmlReader reader;
                XmlWriter writer;
                XmlDocument document;
                String xmlout;
                public void init () throws Exception {
                    url = new URL("file:/java/diva/util/test/xml1.xml"); //FIXME
                    document = new XmlDocument(url);
                    reader = new XmlReader();
                    writer = new XmlWriter();
                }
                public void run () throws Exception {
                    reader.parse(document);
                    Writer w = new StringWriter();
                    try {
                        writer.write(document, w);
                    } catch (Exception e) {}
                    xmlout = w.toString();
                }
                public void check () throws TestFailedException {
                    StringBuffer result = new StringBuffer();
                    BufferedReader input = null;
                    String line = null;
                    try {
                        input = new BufferedReader(
                                new FileReader("/java/diva/util/test/xml1.xml"));
                        line = input.readLine();
                    } catch (Exception e) {}
                    while (line != null) {
                        result.append(line);
                        result.append("\n");
                        try {
                            line = input.readLine();
                        } catch (Exception e) {}
                    }
                    assertEquals(result, xmlout,
                            result  + " != " + xmlout);
                }
            });
    }

    ////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and
     * run all tests on it.
     */
    public static void main (String argv[]) {
        new XMLParserTest(new TestHarness()).run();
    }
}


