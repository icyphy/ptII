/* A Factory to create useful objects from XMLElements.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic.util;

import ptolemy.kernel.*;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import java.util.*;
import collections.*;
import java.io.*;
import ptolemy.schematic.xml.*;
import java.net.*;
import java.lang.reflect.*;

//////////////////////////////////////////////////////////////////////////
//// PTMLObjectFactory
/**
An PTMLObjectFactory supports the creation of useful objects (IconLibraries, 
DomainLibraries, etc.) from XMLElements that represent the root elements
of the correspinding PTML file.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class PTMLObjectFactory {

    /** 
     * Create the root EntityLibrary from an XMLElement that was parsed from 
     * the root EntityLibrary.  
     * @exception IllegalActionException If the XML element does 
     * not have a type of "entitylibrary".
     * @exception NameDuplicationException If the XML element contains two
     * named objects with the same name.
     */
    public static EntityLibrary createEntityLibrary(XMLElement e, 
            IconLibrary iconroot) 
            throws IllegalActionException, NameDuplicationException {

        _checkElement(e, "entitylibrary");

        EntityLibrary ptmlobject = new EntityLibrary();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement) children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("director")) {
                // if it's an director, then create it, 
                // and add it to the list of directors.
                ptmlobject.addDirector(
                        _createSchematicDirector(child, iconroot, null));
            } else if(etype.equals("entity")) {
                // if it's an Entity, then create it, 
                // and add it to the list of entities.
                ptmlobject.addEntity(
                        _createSchematicEntity(child, iconroot, null));
            } else if(etype.equals("sublibrary")) {
                // if it's a sublibrary, then add it to the 
                // list of sublibraries.

                String url = "";
                try {
                    String offset = child.getAttribute("url");
                    XMLElement sublibtree = _parseSubURL(e, offset);
                    EntityLibrary sublib = 
                        createEntityLibrary(sublibtree, iconroot); 
                    ptmlobject.addSubLibrary(sublib);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Couldn't parse entitylibrary " +
                            "from url " + url);
                    System.out.println(ex.getMessage());
                }

            } else if(etype.equals("description")) {
                ptmlobject.setDocumentation(child.getPCData());
            } else {
                _unknownElementType(ptmlobject, child);
            }
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                try {
                    ptmlobject.setName(_getString(e, n));
                } catch (Exception ex) {};
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        return ptmlobject;
            
    }

    /** 
     * Create the root IconLibrary from an XMLElement that was parsed from 
     * the root IconLibrary.  
     * @exception IllegalActionException If the XML element does not 
     * have a type of "iconlibrary"
     * @exception NameDuplicationException If the XML element contains two
     * named objects with the same name.
     */
    public static IconLibrary createIconLibrary(XMLElement e) 
            throws IllegalActionException, NameDuplicationException {

        _checkElement(e, "iconlibrary");

        IconLibrary ptmlobject = new IconLibrary();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement) children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("icon")) {
                // if it's an Icon, then create it, 
                // and add it to the list of icons.
                ptmlobject.addIcon(_createIcon(child));
            } else if(etype.equals("sublibrary")) {
                // if it's a sublibrary, then add it to the 
                // list of sublibraries.

                String url = "";
                try {
                    String offset = child.getAttribute("url");
                    XMLElement sublibtree = _parseSubURL(e, offset);
                    IconLibrary sublib = createIconLibrary(sublibtree); 
                    ptmlobject.addSubLibrary(sublib);
                }
                catch (Exception ex) {
                    System.out.println("Couldn't parse iconlibrary from url "+
                            url);
                    System.out.println(ex.getMessage());
                }

            } else if(etype.equals("description")) {
                ptmlobject.setDocumentation(child.getPCData());
            } else if(etype.equals("terminalstyle")) {
                ptmlobject.addTerminalStyle(_createTerminalStyle(child));
            } else {
                _unknownElementType(ptmlobject, child);
            }
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                try {
                    ptmlobject.setName(_getString(e, n));
                } catch (Exception ex) {};
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }

        return ptmlobject;
            
    }

    /** 
     * Create a Schematic from the given XML element and entity library.  
     * @exception IllegalActionException If the XML element does not 
     * have a type of "schematic"
     * @exception NameDuplicationException If the XML element contains two
     * named objects with the same name.
     */
    public static Schematic createSchematic(XMLElement e,
            IconLibrary iconLib, EntityLibrary entityLib) 
            throws IllegalActionException, NameDuplicationException {

        _checkElement(e, "schematic");
        Schematic ptmlobject = new Schematic();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement) children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("port")) {
                // if it's a port, then create it, 
                // and add it to the list of ports.
                // FIXME
                //ptmlobject.addPort(_createSchematicPort(child));
	    } else if(etype.equals("parameter")) {
                ptmlobject.addParameter(_createSchematicParameter(child));
	    } else if(etype.equals("terminal")) {
                // if it's a terminal, then create it, 
                // and add it to the list of terminals.
                ptmlobject.addTerminal(_createSchematicTerminal(child));
            } else if(etype.equals("entity")) {
                // if it's a entity, then create it, 
                // and add it to the list of entities.
                ptmlobject.addEntity(_createSchematicEntity(child, 
                        iconLib, entityLib));
            } else if(etype.equals("director")) {
                // if it's a director, then create it, 
                // and add it to the list of directors.
                ptmlobject.setDirector(_createSchematicDirector(child, 
                        iconLib, entityLib));
            } else if(etype.equals("relation")) {
                // if it's a relation, then create it, 
                // and add it to the list of relations.
                ptmlobject.addRelation(
                        _createSchematicRelation(child, ptmlobject));
            } else if(etype.equals("description")) {
                ptmlobject.setDocumentation(child.getPCData());
            } else {
                _unknownElementType(ptmlobject, child);
            }
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                try {
                    ptmlobject.setName(_getString(e, n));
                } catch (Exception ex) {};
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }

        return ptmlobject;            
    }

    /** 
     * Parse an Icon Library from the given url string. 
     * Create a PTMLParser and point it at the URL, then call createIconLibrary
     * on the returned tree of XMLElements.
     */
    public static IconLibrary parseIconLibrary(URL url) 
            throws Exception {

        String urlstring = url.toString();        
        XMLElement root = _parser.parse(urlstring);
        return PTMLObjectFactory.createIconLibrary(root);
    }

    /** 
     * Parse an entity library from the given url string. 
     * Point the PTMLParser at the URL, then call createIconLibrary
     * on the returned tree of XMLElements.  All Icons and TerminalStyles
     * should be contained within the given IconLibrary, which will be used
     * to resolve references.
     */
    public static EntityLibrary parseEntityLibrary(URL url, 
            IconLibrary iconLib) 
            throws Exception {

        String urlstring = url.toString();        
        XMLElement root = _parser.parse(urlstring);
        return PTMLObjectFactory.createEntityLibrary(root, iconLib);
    }

    /** 
     * Parse an schematic from the given url string. 
     * Point the PTMLParser at the URL, then call createSchematic
     * on the returned tree of XMLElements.  All entities
     * should be contained within the given EntityLibrary, which will be used
     * to resolve references.
     */
    public static Schematic parseSchematic(URL url, IconLibrary iconLib,
            EntityLibrary entityLib) 
            throws Exception {

        String urlstring = url.toString();        
        XMLElement root = _parser.parse(urlstring);
        return PTMLObjectFactory.createSchematic(root, iconLib, entityLib);
    }

    /** 
     * Create the root IconLibrary from an XMLElement that was parsed from 
     * the root IconLibrary.  
     * @exception If the XML element does not have a type of "iconlibrary"
     */
    /*    public static DomainLibrary createDomainLibrary(XMLElement e)
    throws IllegalActionException {
        
    }*/
   
    /**
     * Check the validity of the XML element.  This method is used to 
     * check external inputs to this class, which should not be flagged
     * as fatal errors (although the calling code may interpret them as such).
     *
     * @exception IllegalActionException If the element is null, or its
     * type is not equal to the given type.
     */
    private static void _checkElement(XMLElement e, String elementtype) 
            throws IllegalActionException {
        if(e == null) {
            throw new IllegalActionException("_checkElement: " +
                    "Received null XMLElement");
        }
        if(!e.getElementType().equals(elementtype)) {
            throw new IllegalActionException("createIconLibrary: " +
                    "Element type " + e.getElementType() + 
                    "differs from expected " + elementtype + ".");
        }
    }

    private static GraphicElement _createGraphicElement(XMLElement e)
        throws IllegalActionException {

        String name = e.getElementType();
        GraphicElement ptmlobject = new GraphicElement(name);
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(ptmlobject, child);
        }
        
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            String v = e.getAttribute(n);        
            ptmlobject.setAttribute(n, v);
        }

        ptmlobject.setLabel(e.getPCData());
        return ptmlobject;
    }

    private static Icon _createIcon(XMLElement e)
        throws IllegalActionException {

        _verifyElement(e, "icon");

        Icon ptmlobject = new Icon();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("tclscript")) {
                // if it's an Icon, then create it, 
                // and add it to the list of icons.
            } else if(etype.equals("description")) {
                ptmlobject.setDocumentation(child.getPCData());
            } else if(etype.equals("terminal")) {
            } else if(etype.equals("xmlgraphic")) {
                Enumeration graphics = child.childElements();
                while(graphics.hasMoreElements()) {
                    XMLElement graphic = (XMLElement)graphics.nextElement();
                    String gtype = graphic.getElementType();                
                    GraphicElement g = _createGraphicElement(graphic);
                    ptmlobject.addGraphicElement(g);
                }
            } else {
                _unknownElementType(ptmlobject, child);
            }    
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                try {
                    ptmlobject.setName(_getString(e, n));
                } catch (Exception ex) {};
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        return ptmlobject;
    }

    private static SchematicDirector _createSchematicDirector(XMLElement e,
            IconLibrary iconroot, EntityLibrary entityLib)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "director");

	//	System.out.println("creating director:" + e);
 
        SchematicDirector ptmlobject = 
            new SchematicDirector();

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("implementation")) {
                ptmlobject.setImplementation(_getString(e, n));
            } else if (n.equals("name")) {
                ptmlobject.setName(_getString(e, n));
            } else if (n.equals("template")) {
		if(entityLib == null) {
		    throw new IllegalActionException("Entity cannot have " +
			"a template since no entity library is specified.");
		} else {
		    String templateString = _getString(e, "template");
		    SchematicDirector template = 
			entityLib.findDirector(templateString);
		    ptmlobject.setTemplate(template);
		}		
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
	    String etype = child.getElementType();
	    if(etype.equals("parameter")) {
                ptmlobject.addParameter(_createSchematicParameter(child));
            } else if(etype.equals("description")) {
                ptmlobject.setDocumentation(child.getPCData());
            } else 
		_unknownElementType(ptmlobject, child);
        }

        return ptmlobject;
    }

    private static SchematicEntity _createSchematicEntity(XMLElement e,
            IconLibrary iconroot, EntityLibrary entityLib)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "entity");

        // System.out.println("creating entity:" + e);
 
        SchematicEntity ptmlobject = 
            new SchematicEntity();

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("implementation")) {
                ptmlobject.setImplementation(_getString(e, n));
            } else if (n.equals("name")) {
                ptmlobject.setName(_getString(e, n));
            } else if (n.equals("icon")) {
                Icon icon = iconroot.findIcon(_getString(e, n));
                ptmlobject.setIcon(icon);
            } else if (n.equals("terminalstyle")) {
                TerminalStyle terminalstyle =
                    iconroot.findTerminalStyle(_getString(e, n));
                ptmlobject.setTerminalStyle(terminalstyle);
            } else if (n.equals("template")) {
		if(entityLib == null) {
		    throw new IllegalActionException("Entity cannot have " +
			"a template since no entity library is specified.");
		} else {
		    String templateString = _getString(e, "template");
		    SchematicEntity template = 
			entityLib.findEntity(templateString);
		    ptmlobject.setTemplate(template);
		}
            } else if (n.equals("x")) {
                ptmlobject.setX(_getDouble(e, n));
            } else if (n.equals("y")) {
                ptmlobject.setY(_getDouble(e, n));
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }

        // If we have an implementation, then extract any information we
        // can from the implementation.
        if(ptmlobject.getImplementation() != null) {
            // Reflection could be really slow.
            Class entityClass = null;
            boolean found = true;
            try {
                entityClass = Class.forName(ptmlobject.getImplementation());
            }
            catch (ClassNotFoundException ex) {
                found = false;
            }
            if(found) {
                try {
                    Class formalArgs[] = new Class[2];
                    formalArgs[0] = Class.forName("ptolemy.actor.TypedCompositeActor");
                    formalArgs[1] = Class.forName("java.lang.String");
                    Constructor entityConstructor = 
                        entityClass.getConstructor(formalArgs);
                    Object actualArgs[] = new Object[2];
                    actualArgs[0] = new ptolemy.actor.TypedCompositeActor();
                    actualArgs[1] = "bogusname";
                    Entity instance =
                        (Entity)entityConstructor.newInstance(actualArgs);

                    Enumeration attribs = instance.getAttributes();
                    while(attribs.hasMoreElements()) {
                        Attribute attrib = (Attribute) attribs.nextElement();
                        if(attrib instanceof Parameter) {
                            Parameter param = (Parameter) attrib;
                            SchematicParameter model = 
                                new SchematicParameter();
                            model.setName(param.getName());
                            model.setValue(param.getToken().stringValue());
                            model.setType(param.getToken().getClass().getName());
                            ptmlobject.addParameter(model);
                        }
                    }

                    Enumeration ports = instance.getPorts();
                    while(ports.hasMoreElements()) {
                        TypedIOPort port = (TypedIOPort) ports.nextElement();
                        SchematicPort model = 
                            new SchematicPort();
                        model.setName(port.getName());
                        model.setMultiport(port.isMultiport());
                        model.setInput(port.isInput());
                        model.setOutput(port.isOutput());
                        ptmlobject.addPort(model);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new IllegalActionException(
                            "Error creating actor: " + ex);
                }
            }
        }

        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
	    String etype = child.getElementType();
	    if(etype.equals("parameter")) {
                ptmlobject.addParameter(_createSchematicParameter(child));
            } else if(etype.equals("description")) {
                ptmlobject.setDocumentation(child.getPCData());
            } else if(etype.equals("port")) {
                SchematicPort newPort = _createSchematicPort(child);
                SchematicPort oldPort = ptmlobject.getPort(newPort.getName());
                if(oldPort == null) {
                    ptmlobject.addPort(_createSchematicPort(child));
                } else {
                    throw new IllegalActionException("Port exists with " + 
                            "same name as " + child + " in " + ptmlobject);
                }
            } else if(etype.equals("terminalmap")) {
                ptmlobject.setTerminalMap(_createTerminalMap(child, 
                        ptmlobject.getTerminalStyle()));
            } else 
		_unknownElementType(ptmlobject, child);
        }

        return ptmlobject;
    }

    private static SchematicLink _createSchematicLink(XMLElement e, 
            Schematic schematic)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "link");

        SchematicLink ptmlobject = new SchematicLink();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(ptmlobject, child);
        }

        // FIXME hmm... I bet it is possible for the terminal that 
        // Schematic Terminal returns to not be in this relation.  This is
        // Probably bad, but we don't check for it.
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("to")) {
                ptmlobject.setTo(
                        schematic.findSchematicTerminal(_getString(e, n)));
            } else if (n.equals("from")) {
                ptmlobject.setFrom(
                        schematic.findSchematicTerminal(_getString(e, n)));
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        return ptmlobject;
    }

    private static SchematicParameter _createSchematicParameter(XMLElement e)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "parameter");

        SchematicParameter ptmlobject = new SchematicParameter();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
	    _unknownElementType(ptmlobject, child);
        }

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                ptmlobject.setName(_getString(e, n));
            } else if (n.equals("value")) {
                ptmlobject.setValue(_getString(e, n));
            } else if (n.equals("type")) {
                ptmlobject.setType(_getString(e, n));
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        return ptmlobject;
    }

    private static SchematicPort _createSchematicPort(XMLElement e)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "port");

        SchematicPort ptmlobject = new SchematicPort();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(ptmlobject, child);
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                ptmlobject.setName(_getString(e, n));
            } else if (n.equals("type")) {
                ptmlobject.setType(_getString(e, n));
            } else if (n.equals("input")) {
                ptmlobject.setInput(_getBoolean(e, n));
            } else if (n.equals("output")) {
                ptmlobject.setOutput(_getBoolean(e, n));
            } else if (n.equals("multiport")) {
                ptmlobject.setMultiport(_getBoolean(e, n));
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        return ptmlobject;
    }
 
    private static SchematicRelation _createSchematicRelation(XMLElement e, 
            Schematic schematic)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "relation");

        SchematicRelation ptmlobject = new SchematicRelation();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("link")) {
                ptmlobject.addLink(_createSchematicLink(child, schematic));
	    } else if(etype.equals("parameter")) {
                ptmlobject.addParameter(_createSchematicParameter(child));
            } else if(etype.equals("terminal")) {
                ptmlobject.addTerminal(_createSchematicTerminal(child));
            } else 
                _unknownElementType(ptmlobject, child);
        }

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                ptmlobject.setName(_getString(e, n));
            } else if (n.equals("width")) {
                ptmlobject.setWidth(_getInt(e, n));
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        return ptmlobject;
    }

    private static SchematicTerminal _createSchematicTerminal(XMLElement e)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "terminal");

        SchematicTerminal ptmlobject = new SchematicTerminal("terminal", null);
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(ptmlobject, child);
        }

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                ptmlobject.setName(_getString(e, n));
            } else if (n.equals("x")) {
                ptmlobject.setX(_getDouble(e, n));
            } else if (n.equals("y")) {
                ptmlobject.setY(_getDouble(e, n));
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        return ptmlobject;
    }

    /*
    private static Terminal _createTerminal(XMLElement e)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "terminal");

        Terminal ptmlobject = new Terminal();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(ptmlobject, child);
        }

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                ptmlobject.setName(_getString(e, n));
            } else if (n.equals("x")) {
                ptmlobject.setX(_getDouble(e, n));
            } else if (n.equals("y")) {
                ptmlobject.setY(_getDouble(e, n));
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        return ptmlobject;
    }
    */

    private static TerminalMap _createTerminalMap(XMLElement e, 
            TerminalStyle terminalStyle)
        throws IllegalActionException, NameDuplicationException {
 
        _verifyElement(e, "terminalmap");
	//System.out.println("creating terminalmap = " + e);
	//System.out.println("creating terminalstyle = " + terminalStyle.description()); 
 
        TerminalMap ptmlobject = new TerminalMap();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(ptmlobject, child);
        }

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("ports")) {
                ptmlobject = 
		    new TerminalMap(terminalStyle, _getString(e, n));
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        return ptmlobject;
    }

    private static TerminalStyle _createTerminalStyle(XMLElement e)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "terminalstyle");

        TerminalStyle ptmlobject = new TerminalStyle();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("terminal")) {
                ptmlobject.addTerminal(_createSchematicTerminal(child));
            } else {
                _unknownElementType(ptmlobject, child);
            }    
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                ptmlobject.setName(_getString(e, n));
            } else {
                _unknownAttribute(ptmlobject, e, n);
            }
        }
        return ptmlobject;
    }

    /** Return a boolean corresponding to the value of the attribute with
     * the given name in the given XMLElement.
     * @throws IllegalActionException If the value of the XML attribute
     *  is not "true" or "false"
     */
    private static boolean _getBoolean(XMLElement e, String name) 
        throws IllegalActionException {
        String v = e.getAttribute(name);
        if(v.toLowerCase().equals("true")) 
            return true;
        else if(v.toLowerCase().equals("false"))
            return false;
        else throw new IllegalActionException(
                "Attribute " + name + " with value " + v + 
                    " does not represent a valid boolean.");
    }
   
    /** Return a double corresponding to the value of the attribute with
     * the given name in the given XMLElement.
     * @throws IllegalActionException If the value of the XML attribute
     *  does not represent a valid double
     */
    private static double _getDouble(XMLElement el, String name) 
        throws IllegalActionException {
        String v = el.getAttribute(name);
        try {
            Double d = new Double(v);
            return d.doubleValue();
        } catch (NumberFormatException e) {
            throw new IllegalActionException(
                    "Attribute " + name + " with value " + v + 
                    " does not represent a valid double.");
        }
    }

   /** Return an integer corresponding to the value of the attribute with
     * the given name in the given XMLElement.
     * @throws IllegalActionException If the value of the XML attribute
     *  does not represent a valid int
     */
    private static int _getInt(XMLElement el, String name) 
        throws IllegalActionException {
        String v = el.getAttribute(name);
        try {
            Integer d = new Integer(v);
            return d.intValue();
        } catch (NumberFormatException e) {
            throw new IllegalActionException(
                    "Attribute " + name + " with value " + v + 
                    " does not represent a valid double.");
        }
    }

    /** Return a string corresponding to the value of the attribute with
     * the given name in the given XMLElement.
     */
    private static String _getString(XMLElement el, String name) {
        String v = el.getAttribute(name);
        return v;
    }

    /** Parse the xml file that is at a relative location to the location 
     * of the given XMLElement given by the urloffset.
     */
    private static XMLElement _parseSubURL(XMLElement e, String urloffset) 
            throws Exception {
        URL baseurl = new URL(e.getXMLFileLocation());        
        URL newurl = new URL(baseurl, urloffset);
        String url = newurl.toString();
        
        XMLElement sublibtree = _parser.parse(url);
        return sublibtree;
    }

    /** 
     * Print a message about the unknown element.
     */
    private static void _unknownElementType(Object parent, XMLElement el) {
            String etype = el.getElementType();
                System.out.println("Unrecognized element type = " +
                    etype + " found in " + parent.getClass().getName());
    }
        
    /** 
     * Print a message about the unknown element
     */
    private static void _unknownAttribute(Object parent, XMLElement e, 
            String name) {
        System.out.println("Unrecognized attribute (" + name + "=" +
                _getString(e, name) + ") found in " +
                parent.getClass().getName());
    }
        
    /**
     * Check the validity of the XML element.   This method is very similar 
     * to _checkElement, except that it is used internally to check state
     * that should already be true, unless the code is broken.
     *
     * @exception InternalErrorException If the element is null, or its
     * type is not equal to the given type.
     */
     private static void _verifyElement(XMLElement e, String elementtype) {
        if(e == null) {
            throw new InternalErrorException("_checkElement: " +
                    "Received null XMLElement");
        }
        if(!e.getElementType().equals(elementtype)) {
            throw new InternalErrorException("createIconLibrary: " +
                    "Element type " + e.getElementType() + 
                    " differs from expected " + elementtype + ".");
        }
     }

    private static PTMLParser _parser = new PTMLParser();
}

