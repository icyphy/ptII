// StreamDemo.java: demonstration application showing &AElig;fred's event stream.
// NO WARRANTY! See README, and copyright below.
// $Id$
// Modified 11/8/98 to add package statement.

package com.microstar.xml.demo;

import java.io.FileInputStream;
import java.io.InputStream;

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
public class StreamDemo extends EventDemo {

  public static void main (String args[]) 
    throws Exception
  {
    StreamDemo handler = new StreamDemo();
    InputStream is;

    if (args.length != 1) {
      System.err.println("Usage: java StreamDemo <file>");
      System.exit(1);
    }

    is = new FileInputStream(args[0]);

    XmlParser parser = new XmlParser();
    parser.setHandler(handler);
    parser.parse(makeAbsoluteURL(args[0]), null, is, null);
  }

}
