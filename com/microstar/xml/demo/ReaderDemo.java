// ReaderDemo.java: demonstration application showing &AElig;fred's reader stream.
// NO WARRANTY! See README, and copyright below.
// $Id$
// Modified 11/8/98 to add package statement.

package com.microstar.xml.demo;

import java.io.Reader;
import java.io.StringReader;

import com.microstar.xml.XmlParser;


/**
  * Demonstration application showing &AElig;fred's event stream from a stream.
  * <p>Usage: <code>java StreamDemo</code>
  * @author Copyright (c) 1998 by Microstar Software Ltd.;
  * @author written by David Megginson &lt;dmeggins@microstar.com&gt;
  * @version 1.1
  * @since Ptolemy II 0.2
  * @see com.microstar.xml.XmlParser
  * @see com.microstar.xml.XmlHandler
  * @see XmlApp
  * @see EventDemo
  */
public class ReaderDemo extends EventDemo {

  public static void main (String args[]) 
    throws Exception
  {
    ReaderDemo handler = new ReaderDemo();
    Reader reader;

    if (args.length != 0) {
      System.err.println("Usage: java ReaderDemo");
      System.exit(1);
    }

    reader = new StringReader("<doc>\n<title>Sample</title>\n<p n=\"1\">Sample document</p>\n</doc>\n");

    XmlParser parser = new XmlParser();
    parser.setHandler(handler);
    parser.parse(null, null, reader);
  }

}
