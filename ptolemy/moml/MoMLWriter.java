/* A class that writes Ptolemy models as moml

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (bart@eecs.berkeley.edu)
*/

package ptolemy.moml;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringUtilities;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.actor.IOPort;

import java.io.*;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Configurable
/**
This is a writer that is capable of writing an MoML descriptiong of a 
Ptolemy model to another writer.

@author Steve Neuendorffer
@version $Id$
 */
public class MoMLWriter extends Writer {
    /** Create a new writer that wraps the given writer.
     */
    public MoMLWriter(Writer writer) {
        super(writer);
        _writer = writer;
    }

    /** Flush the writer.  This class simply defers to the writer
     *  that was passed in the constructor.  If a null writer was
     *  given to the constructor, then do nothing.
     */
    public void flush() throws IOException {
        synchronized(lock) {
            if(_writer != null)
                _writer.flush();
        }
    }

    /** Close this writer.   This class simply defers to the writer
     *  that was passed in the constructor.  If a null writer was
     *  given to the constructor, then do nothing.
     */
    public void close() throws IOException {
        synchronized(lock) {
            if(_writer != null) {
                _writer.close();
                _writer = null;
            }
        }
    }

    /** Write a portion of an array of characters.  
     *  This class simply defers to the writer
     *  that was passed in the constructor.  If a null writer was
     *  given to the constructor, then do nothing.
     *
     *  @param  cbuf  Array of characters
     *  @param  off   Offset from which to start writing characters
     *  @param  len   Number of characters to write
     *
     *  @exception  IOException  If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) 
            throws IOException {
        synchronized(lock) {
            if(_writer != null)
                _writer.write(cbuf, off, len);
        }
    }

    /** Write the given model to the writer that was passed in the
     *  constructor of this writer.  The model will be written as
     *  an XML string.
     */
    public void write(CompositeEntity model) throws IOException {
        write(model, 0, model.getName());
    }

    /** Write the given model to the writer that was passed in the
     *  constructor of this writer.  The model will be written as
     *  an XML string.
     */
    public void write(CompositeEntity model, String name) throws IOException {
        write(model, 0, name);
    }

    /** Write the given model to the writer that was passed in the
     *  constructor of this writer.  The model will be written as
     *  an XML string.
     */
    public void write(NamedObj object, int depth) throws IOException {
        write(object, depth, object.getName());
    }

    /** Write the given model to the writer that was passed in the
     *  constructor of this writer.  The model will be written as
     *  an XML string.
     */
    public void write(NamedObj object, int depth, String name)
        throws IOException {
        synchronized(lock) {
            if(object instanceof Documentation) {
                Documentation container = (Documentation)object;
                write(_getIndentPrefix(depth));
                // If the name is the default name, then omit it
                if(name.equals("_doc")) {
                    write("<doc>");
                } else {
                    write("<doc name=\"");
                    write(name);
                    write("\">");
                }
                write(container.getValue());
                write("</doc>\n");
                return;
            }
            NamedObj.MoMLInfo info = object.getMoMLInfo();
          
            // Write the first line.
            write(_getIndentPrefix(depth));
            write("<");
            write(info.elementName);
            write(" name=\"");
            write(name);

            if(info.elementName.equals("class")) {
                write("\" extends=\"");
                write(info.superclass);
            } else {
                write("\" class=\"");
                write(info.className);
            }
            if(info.source != null) {
                write("\" source=\"");
                write(info.source);
            }
            if(object instanceof Settable) {
                Settable settable = (Settable)object;
                String value = settable.getExpression();
                if(value != null && !value.equals("")) {
                    write("\" value=\"");
                    write(StringUtilities.escapeForXML(value));
                }
            }
            write("\">\n");
            
            // Write if nothing is being deferred to and there is no
            // class name.
            NamedObj deferredObject = null;
            if(info.deferTo != null) {
                deferredObject = info.deferTo;
            } else if(info.className != null) {
                // This sucks.  We should integrate with 
                // the classloader mechanism.
                //                CompositeEntity container = FIXME!
                MoMLParser parser = new MoMLParser();
                String source = "<entity name=\""
                    + object.getName() + "\" class=\""
                    + info.className + "\"/>";
                try {
                    deferredObject = parser.parse(source);
                }
                catch (Exception ex) {
                    System.out.println("Exception occured during parsing "
                     + ex);
                    deferredObject = null;
                }
            }

            // Describe parameterization.
            Iterator attributes = object.attributeList().iterator();
            while(attributes.hasNext()) {
                Attribute attribute = (Attribute)attributes.next();

                if(deferredObject == null) {
                    // If we have nothing to refer to, then just write the
                    // attribute.
                    write(attribute, depth + 1);
                } else {
                    // Otherwise, check inside the referred object to
                    // see if we need to write the attribute.
                    Attribute deferAttribute =
                        deferredObject.getAttribute(attribute.getName());
                    _writeForDeferred(attribute, deferAttribute, depth + 1);
                }
            }
            
            // If we have nothing to defer to, then write out 
            // the internals as well.
            if(info.deferTo == null) {
                // FIXME: Better done using a visitor?  How do we visit an
                // interface, though?
                if(object instanceof Configurable) {
                    Configurable container = (Configurable)object;
                    Configurable deferredContainer = 
                        (Configurable)deferredObject;
                    String source = container.getSource();
                    String text = container.getText();
                    boolean hasSource = source != null && !source.equals("");
                    boolean hasText = text != null && !text.equals("");

                    if(hasSource) {
                        write(_getIndentPrefix(depth + 1));
                        write("<configure source=\"");
                        write(source);
                        write("\">");
                    } else if(hasText) {
                        write(_getIndentPrefix(depth + 1));
                        write("<configure>");
                    }
                    if(hasText)
                        write(text);
                    if(hasText || hasSource)
                        write("</configure>\n");
                    
                    // Rather awkwardly we have to configure the
                    // container, to handle the entity library.
                    try {
                        deferredContainer.configure(null, source, text);
                    } 
                    catch (Exception ex) {
                        throw new RuntimeException(
                                "Failed to configure because " 
                                + ex);
                    }
                }
                if(object instanceof ptolemy.kernel.Entity) {
                    Entity container = (Entity)object;
                    Iterator ports = container.portList().iterator();
                    while (ports.hasNext()) {
                        Port port = (Port)ports.next();
                        write(port, depth + 1);
                    }               
                } 
                if(object instanceof ptolemy.kernel.CompositeEntity) {
                    CompositeEntity container = (CompositeEntity)object;
                    CompositeEntity deferredContainer = 
                        (CompositeEntity) deferredObject;
                    Iterator entities = container.entityList().iterator();
                    while (entities.hasNext()) {
                        ComponentEntity entity = 
                            (ComponentEntity)entities.next();
                        if(deferredContainer == null) {
                            // If we have nothing to refer to, 
                            // then just write the
                            // entity.
                            write(entity, depth + 1);
                        } else {
                            // Otherwise, check inside the referred object to
                            // see if we need to write the entity.
                            Entity deferredEntity =
                                deferredContainer.getEntity(entity.getName());
                            _writeForDeferred(entity, deferredEntity, 
                                    depth + 1);
                        }
                    }
                    Iterator relations = container.relationList().iterator();
                    while (relations.hasNext()) {
                        ComponentRelation relation
                            = (ComponentRelation)relations.next();
                        write(relation, depth + 1);
                    }
                    // Next write the links.
                    // FIXME: pull this in.
                    write(container.exportLinks(depth, null));
                }
                if(object instanceof ptolemy.actor.IOPort) {
                    IOPort container = (IOPort)object;
                    if (container.isInput()) {
                        write(_getIndentPrefix(depth + 1));
                        write("<property name=\"input\"/>\n");
                    }
                    if (container.isOutput()) {
                        write(_getIndentPrefix(depth + 1));
                        write("<property name=\"output\"/>\n");
                    }
                    if (container.isMultiport()) {
                        write(_getIndentPrefix(depth + 1));
                        write("<property name=\"multiport\"/>\n");
                    } 
                }             
                if(object instanceof ptolemy.moml.Vertex) {
                    Vertex container = (Vertex)object;
                    Vertex linked = container.getLinkedVertex();
                    if(linked != null) {
                        write(_getIndentPrefix(depth + 1));
                        write("<pathTo=\"");
                        write(linked.getName());
                        write("\"/>\n");
                    }
                }
            }
            write(_getIndentPrefix(depth) + "</"
                    + info.elementName + ">\n");
            
        }
    }

    /** Write the moml header information.  This is usually called
     *  exactly once prior to writing a model to an external file.
     */
    public void writePreamble() throws IOException {
        synchronized(lock) {
            _writer.write(_preamble);
        }
    }
       
    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        String result = "";
        for (int i = 0; i < level; i++) {
            result += "    ";
        }
        return result;
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param object The object to be written.
     *  @param deferredObject The object that might represent the object.
     *  @param depth The level of indenting represented by the spaces.
     */
    protected void _writeForDeferred(NamedObj object, NamedObj deferredObject, 
            int depth) throws IOException {
        // If there is no deferred object, then write the object.
        if(deferredObject == null) {
            write(object, depth);
        } else {
            StringWriter deferStringWriter = new StringWriter();
            MoMLWriter deferWriter =
                new MoMLWriter(deferStringWriter);
            deferWriter.write(deferredObject, depth);
            String deferredString = deferStringWriter.toString();
            
            StringWriter stringWriter = new StringWriter();
            MoMLWriter writer = new MoMLWriter(stringWriter);
            writer.write(object, depth);
            String string = stringWriter.toString();
            
            // If the object is different, then write it.
            if(!string.equals(deferredString)) {
                System.out.println("String =\n" + string);
                System.out.println("DeferredString =\n" + deferredString);
                write(string);
            } 
        }
    }

    // The writer
    private Writer _writer;
    private String _preamble = new String("<?xml version=\"1.0\""
            +" standalone=\"no\"?>\n"
            + "<!DOCTYPE entity PUBLIC "
            + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
            + "    \"http://ptolemy.eecs.berkeley.edu"
            + "/xml/dtd/MoML_1.dtd\">\n");
}
