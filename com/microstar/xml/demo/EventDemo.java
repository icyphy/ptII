// EventDemo.java: demonstration application showing &AElig;fred's event stream.
// NO WARRANTY! See README, and copyright below.
// Modified 11/8/98 to add package statement.

package com.microstar.xml.demo;


/**
  * Demonstration application showing &AElig;fred's event stream.
  * <p>Usage: <code>java EventDemo &lt;url&gt;</code>
  * <p>Or, use it as an applet, supplying the URL as the <code>url</code>
  * parameter.
  * @author Copyright (c) 1997, 1998 by Microstar Software Ltd.;
  * @author written by David Megginson &lt;dmeggins@microstar.com&gt;
  * @version 1.1
  * @since Ptolemy II 0.2
  * @see com.microstar.xml.XmlParser
  * @see com.microstar.xml.XmlHandler
  * @see XmlApp
  */
public class EventDemo extends XmlApp {


  /**
    * Entry point for an application.
    */
  public static void main (String args[]) 
    throws Exception
  {
    EventDemo demo = new EventDemo();

    if (args.length != 1) {
      System.err.println("java EventDemo <uri>");
      System.exit(1);
    } else {
      demo.doParse(args[0]);
    }
  }


  /**
    * Resolve an entity and print an event.
    * @see com.microstar.xml.XmlHandler#resolveEntity
    */
  public Object resolveEntity (String publicId, String systemId)
  {
    displayText("Resolving entity: pubid="+
		       publicId + ", sysid=" + systemId);
    return null;
  }


  public void startExternalEntity (String systemId)
  {
    displayText("Starting external entity:  " + systemId);
  }

  public void endExternalEntity (String systemId)
  {
    displayText("Ending external entity:  " + systemId);
  }

  /**
    * Handle the start of the document by printing an event.
    * @see com.microstar.xml.XmlHandler#startDocument
    */
  public void startDocument ()
  {
    displayText("Start document");
  }


  /**
    * Handle the end of the document by printing an event.
    * @see com.microstar.xml.XmlHandler#endDocument
    */
  public void endDocument ()
  {
    displayText("End document");
  }


  /**
    * Handle a DOCTYPE declaration by printing an event.
    * @see com.microstar.xml.XmlHandler#doctypeDecl
    */
  public void doctypeDecl (String name, String pubid, String sysid)
  {
    displayText("Doctype declaration:  " + name + ", pubid=" + pubid +
		", sysid=" + sysid);
  }


  /**
    * Handle an attribute value assignment by printing an event.
    * @see com.microstar.xml.XmlHandler#attribute
    */
  public void attribute (String name, String value, boolean isSpecified)
  {
    String s;
    if (isSpecified) {
      s = " (specified)";
    } else {
      s = " (defaulted)";
    }
    displayText("Attribute:  name=" + name + ", value=" + value + s);
  }


  /**
    * Handle the start of an element by printing an event.
    * @see com.microstar.xml.XmlHandler#startElement
    */
  public void startElement (String name)
  {
    displayText("Start element:  name=" + name);
  }


  /**
    * Handle the end of an element by printing an event.
    * @see com.microstar.xml.XmlHandler#endElement
    */
  public void endElement (String name)
  {
    displayText("End element:  " + name);
  }


  /**
    * Handle character data by printing an event.
    * @see com.microstar.xml.XmlHandler#charData
    */
  public void charData (char ch[], int start, int length)
  {
    displayText("Character data:  \"" + escape(ch, length) + '"');
  }


  /**
    * Handle ignorable whitespace by printing an event.
    * @see com.microstar.xml.XmlHandler#ignorableWhitespace
    */
  public void ignorableWhitespace (char ch[], int start, int length)
  {
    displayText("Ignorable whitespace:  \"" + escape(ch, length) + '"');
  }


  /**
    * Handle a processing instruction by printing an event.
    * @see com.microstar.xml.XmlHandler#processingInstruction
    */
  public void processingInstruction (String target, String data)
  {
    displayText("Processing Instruction:  " + target + ' ' +
		escape(data.toCharArray(),data.length()));
  }


}

// end of EventDemo.java

