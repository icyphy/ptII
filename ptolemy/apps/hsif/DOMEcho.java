/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Crimson" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Sun Microsystems, Inc.,
 * http://www.sun.com.  For more information on the Apache Software
 * Foundation, please see <http://www.apache.org/>.
 */

package ptolemy.apps.hsif;

import ptolemy.actor.Actor;
import ptolemy.actor.lib.Assertion;
import ptolemy.actor.Director;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.domains.ct.kernel.CTMixedSignalDirector;
import ptolemy.domains.ct.kernel.CTEmbeddedDirector;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.Relation;
import ptolemy.vergil.fsm.modal.ModalModel;
import ptolemy.vergil.fsm.modal.ModalPort;
import ptolemy.vergil.fsm.modal.ModalController;
import ptolemy.vergil.fsm.modal.Refinement;
import ptolemy.vergil.fsm.modal.RefinementPort;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import ptolemy.apps.hsif.lib.*;

// JAXP packages
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

import java.io.*;


/**
 * This is a program to echo a DOM tree using DOM Level 2 interfaces.  Use
 * JAXP to load an XML file and create a DOM tree.  DOM currently does not
 * provide a method to do this.  (This is planned for Level 3.)  See the
 * method "main" for the three basic steps.  Once the application obtains a
 * DOM Document tree, it dumps out the nodes in the tree and associated
 * node attributes for each node.
 *
 * This program also shows how to validate a document along with using an
 * ErrorHandler to capture validation errors.
 *
 * Note: Program flags may be used to create non-conformant but possibly
 * useful DOM trees.  In some cases, particularly with element content
 * whitespace, applications may not want to rely on JAXP to filter out
 * these nodes but may want to skip the nodes themselves so the application
 * will be more robust.
 *
 * @author Edwin Goei
 */
public class DOMEcho {
    /** All output will use this encoding */
    static final String outputEncoding = "UTF-8";

    /** Output goes here */
    private PrintWriter out;

    /** Indent level */
    private int indent = 0;

    /** Indentation will be in multiples of basicIndent  */
    private final String basicIndent = "  ";

    /** Constants used for JAXP 1.2 */
    static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";

    DOMEcho(PrintWriter out) {
        this.out = out;
    }

    /**
     * Echo common attributes of a DOM2 Node and terminate output with an
     * EOL character.
     */
    private void printlnCommon(Node n) {
        out.print(" nodeName=\"" + n.getNodeName() + "\"");

        String val = n.getNamespaceURI();
        if (val != null) {
            out.print(" uri=\"" + val + "\"");
        }

        val = n.getPrefix();
        if (val != null) {
            out.print(" pre=\"" + val + "\"");
        }

        val = n.getLocalName();
        if (val != null) {
            out.print(" local=\"" + val + "\"");
        }

        val = n.getNodeValue();
        if (val != null) {
            out.print(" nodeValue=");
            if (val.trim().equals("")) {
                // Whitespace
                out.print("[WS]");
            } else {
                out.print("\"" + n.getNodeValue() + "\"");
            }
        }
        out.println();
    }

    public TypedCompositeActor getTopLevel() {
	return _container;
    }

    /**
     * Indent to the current level in multiples of basicIndent
     */
    private void outputIndentation() {
        for (int i = 0; i < indent; i++) {
            out.print(basicIndent);
        }
    }

    /**
     * Recursive routine to print out DOM tree nodes
     */
    public void echo(Node n, Workspace ws, NamedObj no) {

	Workspace workspace = ws;
	NamedObj container = no;
	boolean composite = false;
	boolean modalModel = false;

	// Indent to the current level before printing anything
        outputIndentation();

	try {
	    int type = n.getNodeType();
	    String nodeName = n.getNodeName();
	    switch (type) {
	    case Node.ATTRIBUTE_NODE:
		out.print("ATTR:");
		printlnCommon(n);
		break;
	    case Node.CDATA_SECTION_NODE:
		out.print("CDATA:");
		printlnCommon(n);

		String cData = n.getNodeValue();
		if (container instanceof Attribute) {
		    String attName = container.getName();
		    if (attName.equals("Guard")) {
			Transition transition = (Transition) container.getContainer();
			transition.setGuardExpression(cData);
		    } else if (attName.equals("Trigger")) {
			Transition transition = (Transition) container.getContainer();
			transition.setTriggerExpression(cData);
		    } else if (attName.equals("SendAction")) {
			Transition transition = (Transition) container.getContainer();
		    } else if (attName.equals("UpdateAction")) {
			Transition transition = (Transition) container.getContainer();
			try {
			    transition.setActions.setExpression(cData);
			    //			    transition.setActions.validate();
			} catch (IllegalActionException ex) {
			    throw new Exception("Error in setting the "
						+ "setActions expression of a transition.");
			}
		    } else if (attName.equals("InitialState")) {
			if (cData.equals("true")) {
			    Refinement refinement = (Refinement) container.getContainer();
			    FSMActor fsmActor = ((FSMDirector)((ModalModel) refinement.getContainer()).getDirector()).getController();
			    fsmActor.initialStateName.setExpression(refinement.getName());
			}
		    } else if (attName.equals("Expr")) {
			System.out.println("Expr: " + cData);
			Assertion assertion = (Assertion) container.getContainer();
			    assertion.assertion.setExpression(cData);
		    }
		} else {
		}

		break;
	    case Node.COMMENT_NODE:
		out.print("COMM:");
		printlnCommon(n);
		break;
	    case Node.DOCUMENT_FRAGMENT_NODE:
		out.print("DOC_FRAG:");
		printlnCommon(n);
		break;
	    case Node.DOCUMENT_NODE:
		out.print("DOC:");
		printlnCommon(n);
		break;
	    case Node.DOCUMENT_TYPE_NODE:
		out.print("DOC_TYPE:");
		printlnCommon(n);

		// Print entities if any
		NamedNodeMap nodeMap = ((DocumentType)n).getEntities();
		indent += 2;
		for (int i = 0; i < nodeMap.getLength(); i++) {
		    Entity entity = (Entity)nodeMap.item(i);
		    echo(entity, workspace, container);
		}
		indent -= 2;
		break;
	    case Node.ELEMENT_NODE:

		out.print("ELEM:");
		printlnCommon(n);

		// Print attributes if any.  Note: element attributes are not
		// children of ELEMENT_NODEs but are properties of their
		// associated ELEMENT_NODE.  For this reason, they are printed
		// with 2x the indent level to indicate this.
		NamedNodeMap atts = n.getAttributes();
		indent += 2;

		if (nodeName.equals("project")) {
		    workspace = new Workspace();
		} else if (nodeName.equals("folder")) {
		    if (no == null) {
			container = new TypedCompositeActor(workspace);
			_container = (TypedCompositeActor) container;
			new CTMixedSignalDirector((TypedCompositeActor) container, "CT Director");
		    } else {
			container = new TypedCompositeActor((TypedCompositeActor) container, null);
			new CTMixedSignalDirector((TypedCompositeActor) container, "CT Director");
		    }
		    workspace = null;
		    composite = true;
		    String id = "";
		    for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String attName = att.getNodeName();
			if (attName.equals("id")) {
			    id = att.getNodeValue();
			}
			echo(att, workspace, container);
		    }
		    System.out.println("Container: " + container.getFullName() + " " + id);
		    Parameter IDParameter = new Parameter(container, "id");
		    IDParameter.setToken(new StringToken(id));
		} else if (nodeName.equals("model")) {
		    composite = true;
		    String id = "";
		    for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String attName = att.getNodeName();
			if (attName.equals("kind")) {
			    String nodeKind = att.getNodeValue();
			    if (nodeKind.equals("DNHA")) {
				container = new TypedCompositeActor((TypedCompositeActor) container, null);
				new DEDirector((TypedCompositeActor) container, "DE Director");
			    } else if (nodeKind.equals("HybridAutomaton")) {
				container = new ModalModel((TypedCompositeActor) container, "modalModel");
				modalModel = true;
			    } else if (nodeKind.equals("DiscreteState")) {
				FSMActor fsmActor = ((FSMDirector) ((ModalModel) container).getDirector()).getController();;
				new State(fsmActor, "NameToBeConfigured");
				container = new Refinement((TypedCompositeActor) container, null);
				new CTEmbeddedDirector((TypedCompositeActor) container, "CT Embedded Director");
			    }
			}

			if (attName.equals("id")) {
			    id = att.getNodeValue();
			}
			echo(att, workspace, container);
		    } 

		    System.out.println("Container: " + container.getFullName() + " " + id);
		    Parameter IDParameter = new Parameter(container, "id");
		    IDParameter.setToken(new StringToken(id));

		} else if (nodeName.equals("atom")) {
		    String id = "";
		    for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String attName = att.getNodeName();
			if (attName.equals("kind")) {
			    String nodeKind = att.getNodeValue();
				//System.out.println(att.getNodeValue());
			    if (nodeKind.endsWith("Variable")) {
				if (container instanceof ModalModel) {
				    if (nodeKind.equals("BooleanVariable")) {
					container = (ModalPort) ((ModalModel) container).newPort("modalPort");
					((TypedIOPort) container).setTypeEquals(BaseType.BOOLEAN);
				    } else if (nodeKind.equals("IntegerVariable")) {
					container = (ModalPort) ((ModalModel) container).newPort("modalPort");
					((TypedIOPort) container).setTypeEquals(BaseType.INT);
				    } else if (nodeKind.equals("RealVariable")) {
					container = (ModalPort) ((ModalModel) container).newPort("modalPort");
					((TypedIOPort) container).setTypeEquals(BaseType.DOUBLE);
				    }
				} else {
				    if (nodeKind.equals("BooleanVariable")) {
					container = new TypedIOPort((ComponentEntity) container, null);
					((TypedIOPort) container).setTypeEquals(BaseType.BOOLEAN);
				    } else if (nodeKind.equals("IntegerVariable")) {
					container = new TypedIOPort((ComponentEntity) container, null);
					((TypedIOPort) container).setTypeEquals(BaseType.INT);
				    } else if (nodeKind.equals("RealVariable")) {
					container = new TypedIOPort((ComponentEntity) container, null);
					((TypedIOPort) container).setTypeEquals(BaseType.DOUBLE);
				    }
				}
			    } else if (nodeKind.endsWith("Parameter")) {
				if (nodeKind.equals("BooleanParameter")) {
				    container = new Parameter((ComponentEntity) container, null);
				    ((Parameter) container).setTypeEquals(BaseType.BOOLEAN);
				} else if (nodeKind.equals("IntegerParameter")) {
				    container = new Parameter((ComponentEntity) container, null);
				    ((Parameter) container).setTypeEquals(BaseType.INT);
				} else if (nodeKind.equals("RealParameter")) {
				    container = new Parameter((ComponentEntity) container, null);
				    ((Parameter) container).setTypeEquals(BaseType.DOUBLE);
				}
			    } else if (nodeKind.equals("Channel")) {
				container = new TypedIORelation((CompositeEntity) container, null);
			    } else if (nodeKind.equals("OutputChannel")) {
				if (container instanceof ModalModel) {
				    container = (ModalPort) ((ModalModel) container).newPort("modalPort");
				    ((ModalPort) container).setOutput(true);
				} else {
				    container = new TypedIOPort((ComponentEntity) container, null, false, true);
				}
			    } else if (nodeKind.equals("InputChannel")) {
				if (container instanceof ModalModel) {
				    container = (ModalPort) ((ModalModel) container).newPort("modalPort");
				    ((ModalPort) container).setInput(true);
				} else {
				    container = new TypedIOPort((ComponentEntity) container, null, true, false);
				}
			    } else if (nodeKind.equals("Invariant")) {
				container = new Assertion((CompositeEntity) container, null);
			    } else if (nodeKind.equals("AlgEquation")) {
				container = new Expression((CompositeEntity) container, null);
				// FIXME			  
			    } else if (nodeKind.equals("FlowEquation")) {
				new Integrator((CompositeEntity) container, "ToBeConfigured");
				container = new Expression((CompositeEntity) container, null);
				// FIXME			    
			    }
			}

			if (attName.equals("id")) {
			    id = att.getNodeValue();
			}
			echo(att, workspace, container);
		    }

		    System.out.println("Container: " + container.getFullName() + " " + id);
		    Parameter IDParameter = new Parameter(container, "id");
		    IDParameter.setToken(new StringToken(id));

		} else if (nodeName.equals("attribute")) {
		    NamedObj namedObj = container;
		    for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String attName = att.getNodeName();
			if (attName.equals("kind")) {
			    String nodeKind = att.getNodeValue();
			    if (nodeKind.equals("Kind")) {
				// do nothing ?
			    } else if (nodeKind.indexOf("Value") != -1) {
				if (container instanceof TypedIOPort) {
				    Type ptype = ((TypedIOPort) container).getType();
				    container = new Parameter(container, nodeKind);
				    ((Parameter) container).setTypeEquals(ptype);
				} else if (container instanceof Parameter) {
				    // do nothing?
				}
			    } else if (nodeKind.equals("Guard")) {
				container = new Attribute(container, nodeKind);
			    } else if (nodeKind.equals("Trigger")) {
				container = new Attribute(container, nodeKind);
			    } else if (nodeKind.equals("SendAction")) {
				container = new Attribute(container, nodeKind);
			    } else if (nodeKind.equals("UpdateAction")) {
				container = new Attribute(container, nodeKind);
			    } else if (nodeKind.equals("InitialState")) {
				container= new Attribute(container, nodeKind);
			    } else if (nodeKind.equals("EntryAction")) {
				container= new Attribute(container, nodeKind);
			    } else if (nodeKind.equals("ExitAction")) {
				container= new Attribute(container, nodeKind);
			    } else if (nodeKind.equals("Var")) {
				container= new Attribute(container, nodeKind);
			    } else if (nodeKind.equals("Expr")) {
				container= new Attribute(container, nodeKind);
			    }
			}
			echo(att, workspace, container);
		    }
		} else if (nodeName.equals("connection")) {
		    for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String attName = att.getNodeName();
			if (attName.equals("kind")) {
			    String nodeKind = att.getNodeValue();
			    if (nodeKind.equals("Transition")) {
				FSMActor fsmActor = ((FSMDirector)((ModalModel)container).getDirector()).getController();
				container = new Transition(fsmActor, "transition");
				new TransitionInterface((Transition) container, "transitionConnection");
				echo(att, workspace, (Transition) container);
			    } else if (nodeKind.indexOf("Channel") != -1) {
				container = new ChannelInterface((TypedCompositeActor)container, null);
				if (nodeKind.equals("OutputToChannel")) {
				    ((ChannelInterface) container).setOutput(true);
				} else if (nodeKind.equals("ChannelToInput")) {
				    ((ChannelInterface) container).setInput(true);
				}
				echo(att, workspace, (ChannelInterface) container);
			    }
			}
		    }
		} else if (nodeName.equals("connpoint")) {
		    String role = "";
		    for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String attName = att.getNodeName();
			if (attName.equals("role")) {
			    role = att.getNodeValue();
			} else if (attName.equals("target")) {
			    if (container instanceof ChannelInterface) {
				if (role.equals("src")) {
				    ((ChannelInterface)container).setSrc(att.getNodeValue());
				} else if (role.equals("dst")) {
				    ((ChannelInterface)container).setDst(att.getNodeValue());
				}
			    } else if (container instanceof Transition) {
				ListIterator tis = ((Transition) container).attributeList(TransitionInterface.class).listIterator();
				while (tis.hasNext()) {
				    TransitionInterface ti = (TransitionInterface) tis.next();
				    if (role.equals("src")) {
					ti.setSrc(att.getNodeValue());
				    } else if (role.equals("dst")) {
					ti.setDst(att.getNodeValue());
				    }
				}    
			    }
			}
		    }
		}
		if(nodeName.equals("name")) {
		    _nameText = !_nameText;
		}
		if (nodeName.equals("value")) {
		    _valueText = !_valueText;
		}
		indent -= 2;
		break;
	    case Node.ENTITY_NODE:
		out.print("ENT:");
		printlnCommon(n);
		break;
	    case Node.ENTITY_REFERENCE_NODE:
		out.print("ENT_REF:");
		printlnCommon(n);
		break;
	    case Node.NOTATION_NODE:
		out.print("NOTATION:");
		printlnCommon(n);
		break;
	    case Node.PROCESSING_INSTRUCTION_NODE:
		out.print("PROC_INST:");
		printlnCommon(n);
		break;
	    case Node.TEXT_NODE:
		out.print("TEXT:");
		printlnCommon(n);
		if (nodeName.equals("#text") && _nameText) {
		    if (!( n.getNodeValue() == null || n.getNodeValue().trim().equals(""))) {
			System.out.println("--- text field content: " + n.getNodeValue());
			if (container instanceof Refinement) {
			    String stateName = n.getNodeValue();
			    FSMActor fsmActor = ((FSMDirector)((ModalModel) container.getContainer()).getDirector()).getController();
			    container.setName(stateName);
			    State state = (State) fsmActor.getEntity("NameToBeConfigured");
			    state.setName(stateName);
			    state.refinementName.setExpression(stateName);
			} else {
			    if (workspace != null) {
				workspace.setName(n.getNodeValue());
				System.out.println("==> work space name: " + workspace.getName());
			    } else {
				try {
				    container.setName(n.getNodeValue());
				} catch (NameDuplicationException e) {
				    container.setName(n.getNodeValue() + "1");
				}
				System.out.println("==> container name: " + container.getFullName());
			    }
			}
		    }
		    _nameText = !_nameText;
		}
		if (nodeName.equals("#text") && _valueText) {
		    if (!( n.getNodeValue() == null || n.getNodeValue().trim().equals(""))) {
			String value = n.getNodeValue();
			System.out.println("--- value field content: " + value);

			if ((container instanceof Attribute) && !(container instanceof Parameter)) {
			    System.out.println("Attribute value text");
			    String attName = container.getName();
			    if (attName.equals("Guard")) {
				Transition transition = (Transition) container.getContainer();
				transition.setGuardExpression(value);
			    } else if (attName.equals("Trigger")) {
				Transition transition = (Transition) container.getContainer();
				transition.setTriggerExpression(value);
			    } else if (attName.equals("SendAction")) {
				Transition transition = (Transition) container.getContainer();
			    } else if (attName.equals("UpdateAction")) {
				Transition transition = (Transition) container.getContainer();
				try {
				    transition.setActions.setExpression(value);
				    //			    transition.setActions.validate();
				} catch (IllegalActionException ex) {
				    throw new Exception("Error in setting the "
							+ "setActions expression of a transition.");
				}
			    } else if (attName.equals("InitialState")) {
				if (value.equals("true")) {
				    Refinement refinement = (Refinement) container.getContainer();
				    FSMActor fsmActor = ((FSMDirector)((ModalModel) refinement.getContainer()).getDirector()).getController();
				    fsmActor.initialStateName.setExpression(refinement.getName());
				}
			    } else if (attName.equals("Invariant")) {
				Assertion assertion = (Assertion) container.getContainer();
				assertion.assertion.setExpression(value);
			    } else if (attName.equals("Var")) {
				Expression expression = (Expression) container.getContainer();
				Refinement refinement = (Refinement) expression.getContainer();
				TypedIORelation relation = new TypedIORelation(refinement, value);
				ListIterator integrators = refinement.entityList(Integrator.class).listIterator();
				while (integrators.hasNext()) {
				    Integrator integrator = (Integrator) integrators.next();
				    if (integrator.getName().equals("ToBeConfigured")) {
					integrator.setName(value);
				    }
				}
			    } else if (attName.equals("Expr")) {
				TypedAtomicActor taa = (TypedAtomicActor) container.getContainer();
				if (taa instanceof Expression) {
				    ((Expression) taa).expression.setExpression(value);
				} else if (taa instanceof Assertion) {
				    ((Assertion) taa).assertion.setExpression(value);
				}
			    }
			} else {
			    System.out.println("NOT Attribute value text");
			    if (value.equals("Controlled")) {
				((TypedIOPort) container).setOutput(true);
			    } else if (value.equals("Input")) {
				((TypedIOPort) container).setInput(true);
			    } else if (value.equals("Observable")) {
				((TypedIOPort) container).setOutput(true);
			    } else {
				((Parameter) container).setExpression(value);
			    }
			}
		    }
		    _valueText = !_valueText;
		}
		break;
	    default:
		out.print("UNSUPPORTED NODE: " + type);
		printlnCommon(n);
		break;
	    }
	    
	} catch (Exception e) {
	    System.out.println(" something wrong " + e.getMessage());
	}
	
	// Print children if any
	indent++;
	for (Node child = n.getFirstChild(); child != null;
	     child = child.getNextSibling()) {
	    
	    if (child.getNodeName().equals("regnode")) {
		// no position information is necessary
	    } else {
		echo(child, workspace, container);
	    }

	}
	indent--;

	if (modalModel && composite) {
	    System.out.println(" Now the container is " + container.getFullName() + " modalModel");
	} else if (composite){
	    System.out.println(" Now the container is " + container.getFullName() + " composite");
	}
	
	// handle connections

	if (composite) {
	    try {
		if (modalModel) {	
		    ModalController controller = (ModalController) ((FSMDirector)((TypedCompositeActor) container).getDirector()).getController();
		    ListIterator transitions = controller.relationList().listIterator();
		    System.out.println(((TypedCompositeActor) container).relationList().size());
		    while (transitions.hasNext()) {
			Transition transition = (Transition) transitions.next();
			System.out.println(transition.getName()); // + transition.attributeList().size());
			ListIterator connections = transition.attributeList(TransitionInterface.class).listIterator();
			while (connections.hasNext()) {
			    TransitionInterface connection = (TransitionInterface) connections.next();
			    String srcID = connection.getSrc();
			    String dstID = connection.getDst();

			    ListIterator refinements = ((TypedCompositeActor) container).entityList(Refinement.class).listIterator();
			    while (refinements.hasNext()) {
				Refinement refinement = (Refinement) refinements.next();
				String refinementID = ((StringToken) ((Parameter) refinement.getAttribute("id")).getToken()).stringValue();
				String refinementName = refinement.getName();
				System.out.println("refinementID " + refinementID + " src --> dst " + srcID + " --> " + dstID);

				ListIterator states = controller.entityList(State.class).listIterator();
				while (states.hasNext()) {
				    State state = (State) states.next();
				    if (state.getName().equals(refinementName)) {
					if (refinementID.equals(srcID)) {
					    state.outgoingPort.link(transition);
					} 
					if (refinementID.equals(dstID)) {
					    state.incomingPort.link(transition);
					}
				    }
				}
			    }
			}
		    }
		} else if (container instanceof Refinement) {
		} else {
		    ListIterator channels = ((TypedCompositeActor) container).relationList().listIterator();
		    //System.out.println(((TypedCompositeActor) container).relationList().size());
		    while (channels.hasNext()) {
			TypedIORelation channel = (TypedIORelation) channels.next();
			String channelID = ((StringToken) ((Parameter) channel.getAttribute("id")).getToken()).stringValue();
			ListIterator connections = ((TypedCompositeActor) container).attributeList(ChannelInterface.class).listIterator();
			while (connections.hasNext()) { 
			    ChannelInterface connection = (ChannelInterface) connections.next();
			    String srcID = connection.getSrc();
			    String dstID = connection.getDst();
			    
				//System.out.println("channelID " + channelID + " src --> dst " + srcID + " --> " + dstID);
			    
			    if (connection.getType().equals("output")) {
				if (dstID.equals(channelID)) {
				// search the TypedIOPort with the same ID with srcID
				    ListIterator entities = ((TypedCompositeActor) container).entityList(TypedCompositeActor.class).listIterator();
				    //System.out.println("entities number " + ((TypedCompositeActor) container).entityList(TypedCompositeActor.class).size());
				    while (entities.hasNext()) {
					TypedCompositeActor entity = (TypedCompositeActor) entities.next();
					ListIterator outputs = entity.outputPortList().listIterator();
					while (outputs.hasNext()) {
					    TypedIOPort output = (TypedIOPort) outputs.next();
					    String outputID = ((StringToken) ((Parameter) output.getAttribute("id")).getToken()).stringValue();
					    if (outputID.equals(srcID)) {
						// FIXME has to check if there are multi connections
						output.link(channel);
						// System.out.println("Connecting ... " + srcID + " --> " + dstID);
					    }
					}
				    }
				}
			    } else if (connection.getType().equals("input")) {
				if (srcID.equals(channelID)) {
				// search the TypedIOPort with the same ID with dstID
				    ListIterator entities = ((TypedCompositeActor) container).entityList(TypedCompositeActor.class).listIterator();
				    while (entities.hasNext()) {
					TypedCompositeActor entity = (TypedCompositeActor) entities.next();
					ListIterator inputs = entity.inputPortList().listIterator();
					while (inputs.hasNext()) {
					    TypedIOPort input = (TypedIOPort) inputs.next();
					    String inputID = ((StringToken) ((Parameter) input.getAttribute("id")).getToken()).stringValue();
					    if (inputID.equals(dstID)) {
						// FIXME has to check if there are multi connections
						input.link(channel);
						// System.out.println("Connecting ... " + srcID + " --> " + dstID);
					    }
					}
				    }
				}
			    }
			}
		    }
		}
	    } catch (IllegalActionException e) {
		System.out.println("Connection error: " + e.getMessage());
	    }
	}
    }

    // Error handler to report errors and warnings
    private static class MyErrorHandler implements ErrorHandler {
	/** Error handler output goes here */
	private PrintWriter out;

	MyErrorHandler(PrintWriter out) {
	    this.out = out;
	}

	/**
	 * Returns a string describing parse exception details
	 */
	private String getParseExceptionInfo(SAXParseException spe) {
	    String systemId = spe.getSystemId();
	    if (systemId == null) {
		systemId = "null";
	    }
	    String info = "URI=" + systemId +
		" Line=" + spe.getLineNumber() +
		": " + spe.getMessage();
	    return info;
	}

	// The following methods are standard SAX ErrorHandler methods.
	// See SAX documentation for more info.

	public void warning(SAXParseException spe) throws SAXException {
	    out.println("Warning: " + getParseExceptionInfo(spe));
	}

	public void error(SAXParseException spe) throws SAXException {
	    String message = "Error: " + getParseExceptionInfo(spe);
	    throw new SAXException(message);
	}

	public void fatalError(SAXParseException spe) throws SAXException {
	    String message = "Fatal Error: " + getParseExceptionInfo(spe);
	    throw new SAXException(message);
	}
    }

    private TypedCompositeActor _container;
    private Workspace _workspace;
    private boolean _nameText = false;
    private boolean _valueText = false;

}
