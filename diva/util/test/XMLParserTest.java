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
 * @author John Reekie
 * @version $Id$
 */
public class XMLParserTest extends TestSuite {
    /** Constructor
     */
    public XMLParserTest(TestHarness harness) {
        setTestHarness(harness);
    }

    /**
     * runSuite()
     */
    @Override
    public void runSuite() {
        testParse();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Test construction of XMLParser
     */
    public void testParse() {
        runTestCase(new TestCase("XMLParse parse") {
            URL url;

            XmlReader reader;

            XmlWriter writer;

            XmlDocument document;

            String xmlout;

            @Override
            public void init() throws Exception {
                url = new URL("file:/java/diva/util/test/xml1.xml"); //FIXME
                document = new XmlDocument(url);
                reader = new XmlReader();
                writer = new XmlWriter();
            }

            @Override
            public void run() throws Exception {
                reader.parse(document);

                Writer w = new StringWriter();

                writer.write(document, w);

                xmlout = w.toString();
            }

            @Override
            public void check() throws TestFailedException {
                StringBuffer result = new StringBuffer();
                BufferedReader input = null;
                String line = null;

                try {
                    input = new BufferedReader(new FileReader(
                            "/java/diva/util/test/xml1.xml"));
                    line = input.readLine();
                } catch (Exception e) {
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Exception ex) {
                            System.err.println("Failed to close: " + ex);
                        }
                    }
                }

                while (line != null) {
                    result.append(line);
                    result.append("\n");

                    try {
                        line = input.readLine();
                    } catch (Throwable throwable) {
                    }
                }

                assertEquals(result, xmlout, result + " != " + xmlout);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and
     * run all tests on it.
     */
    public static void main(String[] argv) {
        new XMLParserTest(new TestHarness()).run();
    }
}
