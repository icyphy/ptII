/* An component accessor that consists of an interface and a script.

 Copyright (c) 2015 The Regents of the University of California.
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

 */
package org.terraswarm.accessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.jjs.JavaScript;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.Actionable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// JSAccessor

/**
 An component accessor that consists of an interface and a script.

 <p>The "<a href="#VisionOfSwarmLets">Vision of Swarmlets</a>" paper
 defines three types of accessors: Interface, Component and Composite.
 The paper states: "A component accessor has an interface and a
 script...  The script defines one or more functions that are invoked
 by the swarmlet host."</p>

 <p>This is a specialized JavaScript actor that hides the script
 from casual users by putting it in "expert" mode.
 It also sets the actor to "restricted" mode, which restricts
 the functionality of the methods methods and variables
 provided in the JavaScript context.</p>

 <p>FIXME: This should support versioning of accessors.
 It should check the accessorSource for updates and replace
 itself if there is a newer version and the user agrees to
 the replacement. This will be tricky because any parameters
 and connections previously set should be preserved.</p>
 
 <p>This actor extends {@link ptolemy.actor.lib.jjs.JavaScript}
 and thus requires Nashorn, which is present in Java-1.8 and
 later.</p>

 <h2>References</h2>

 <p><name="VisionOfSwarmLets">Elizabeth Latronico, Edward A. Lee,
 Marten Lohstroh, Chris Shaver, Armin Wasicek, Matt Weber.</a>
 <a href="http://www.terraswarm.org/pubs/332.html">A Vision of Swarmlets</a>,
 <i>IEEE Internet Computing, Special Issue on Building Internet
 of Things Software</i>, 19(2):20-29, March 2015.</p>

 @author Edward A. Lee, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (bilung)
 */
public class JSAccessor extends JavaScript {

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JSAccessor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        accessorSource = new ActionableAttribute(this, "accessorSource");
        // Make the source editable so that you can update from another site.
        // accessorSource.setVisibility(Settable.NOT_EDITABLE);

        SingletonParameter hide = new SingletonParameter(script, "_hide");
        hide.setExpression("true");

        // The base class, by default, exposes the instance of this actor in the
        // JavaScript variable "actor", which gives an accessor full access
        // to the model, and hence a way to invoke Java code. Prevent this
        // by putting the actor in "restricted" mode.
        _restricted = true;

        // Set the script parameter to Visibility EXPERT.
        script.setVisibility(Settable.EXPERT);
        
        // Hide the port for the script.
        (new SingletonParameter(script.getPort(), "_hide")).setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The source of the accessor (a URL). */
    public StringAttribute accessorSource;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Generate MoML for an Accessor. This produces only the body MoML.
     *  It must be wrapped in an <entity></entity> or <class></class>
     *  element to be instantiable, or in a <group></group> to be used
     *  to update an accessor. 
     *  The accessor is read in from a url, processed with 
     *  XSLT and MoML is returned.
     *  @param url The URL of the accessor.
     *  @return MoML of the accessor, which is typically passed to
     *  handleAccessorMoMLChangeRequest().
     *  @exception IOException If the urlSpec cannot be converted, opened
     *  read, parsed or closed.
     *  @exception TransformerConfigurationException If a factory cannot
     *  be created from the xslt file.
     *  @throws IllegalActionException If no source file is specified.
     */
    public static String accessorToMoML(final String urlSpec)
            throws IOException, TransformerConfigurationException, IllegalActionException {

        // This method is a separate method so that we can use it for
        // testing the reimportation of accessors.  See
        // https://www.terraswarm.org/accessors/wiki/Main/TestAPtolemyAccessorImport

	if (urlSpec == null || urlSpec.trim().equals("")) {
	    throw new IllegalActionException("No source file specified.");
	}

        final URL url = FileUtilities.nameToURL(urlSpec, null, null);
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
        	    url.openStream()));
            StringBuffer contents = new StringBuffer();
            String input;
            while ((input = in.readLine()) != null) {
        	contents.append(input);
        	contents.append("\n");
            }
            
            // If the spec is a JavaScript file, then do not use XSLT, but just
            // instantiate JSAccessor with the script parameter.
            String extension = urlSpec.substring(urlSpec.lastIndexOf('.') + 1, urlSpec.length());
            extension = extension.toLowerCase().trim();
            if (extension == null || extension.equals("")) {
        	throw new IllegalActionException("Can't tell file type from extension: " + urlSpec);
            }
            if (extension.equals("js")) {
        	// JavaScript specification.
        	StringBuffer result = new StringBuffer("<property name=\"script\" value=\"");
        	// Since $ causes the expression parser to try to substitute a variable, we need
        	// to escape it.
        	String escaped = contents.toString().replace("$", "$$");
        	result.append(StringUtilities.escapeForXML(escaped));
        	result.append("\"/>");
        	return result.toString();
            } else if (extension.equals("xml")) {
        	// XML specification.
        	TransformerFactory factory = TransformerFactory.newInstance();
        	String xsltLocation = "$CLASSPATH/org/terraswarm/accessor/XMLJSToMoML.xslt";
        	Source xslt = new StreamSource(FileUtilities.nameToFile(
        		xsltLocation, null));
        	Transformer transformer = factory.newTransformer(xslt);
        	StreamSource source = new StreamSource(new InputStreamReader(
        		url.openStream()));
        	StringWriter outWriter = new StringWriter();
        	// NOTE: Could target a DOMResult here instead, which would give
        	// much more flexibility.
        	StreamResult result = new StreamResult(outWriter);
        	try {
        	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");   
        	    transformer.setOutputProperty(OutputKeys.INDENT,"yes");
        	    contents = outWriter.getBuffer();
        	    transformer.transform(source, result);
        	    contents = outWriter.getBuffer();
        	} catch (Throwable throwable) {
        	    IOException ioException = new IOException("Failed to parse \""
        		    + url
        		    + "\".");
        	    ioException.initCause(throwable);
        	    throw ioException;
        	}

                // Wrap in a group element that will rename the instance if there is a
                // naming conflict.
                StringBuffer moml = new StringBuffer("<group name=\"auto\">\n");
                // Wrap the transformed MoML in <entity></entity>
                // First get the file name only.
                String fileName = urlSpec.substring(urlSpec.lastIndexOf('/') + 1, urlSpec.length());
                String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                String instanceNameRoot = StringUtilities.sanitizeName(fileNameWithoutExtension);
                moml.append("<entity name=\""  + instanceNameRoot
                        + "\" class=\"org.terraswarm.accessor.jjs.JSAccessor\">"
                        + contents.toString()
                        + "</entity></group>");
                return moml.toString();
            } else {
        	throw new IllegalActionException("Unrecognized file extension: " + extension);
            }
        } finally {
            if (in != null) {
        	in.close();
            }
        }
    }

    /** React to a change in an attribute, and if the attribute is the
     *  script parameter, FIXME.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If evaluating the script fails.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
	super.attributeChanged(attribute);
	if (attribute == script) {
	    // Force the script to be marked not overridden.
	    // In other words, each time you set the value of the script,
	    // the new value will be assumed to be that specified by the class
	    // of which this accessor is an instance.  This means that each time
	    // you perform an Update on the accessor, the script will be reloaded,
	    // even if you have overridden it.  This should be OK, since the script
	    // is visible only in expert mode.  Failing to do this results in
	    // an Update NOT updating the script ever, which is definitely not what
	    // we want.
	    attribute.setDerivedLevel(Integer.MAX_VALUE);
	    // The above will have the side effect that a script will not be saved
	    // when you save the model. Force it to be saved.
	    attribute.setPersistent(true);
	}
    }

    /** Handle an accessor-specific MoMLChangeRequest.
     *   
     *  In the postParse() phase, the _location and accessorSource
     *  attributes are updated.
     *
     *  @param originator The originator of the change request.
     *  @param urlSpec The URL string specification.
     *  @param context The context in which the FMU actor is created.
     *  @param changeRequest The text of the change request,
     *  typically generated by {@link #accessorToMoML(String)}.
     *  @param x The x-axis value of the actor to be created.
     *  @param y The y-axis value of the actor to be created.
     */        
    public static void handleAccessorMoMLChangeRequest(Object originator,
            final String urlSpec,
            NamedObj context, String changeRequest,
            final double x, final double y) {

        // This method is a separate method because it makes sense
        // to move it away from the gui code in
        // ptolemy/vergil/basic/imprt/accessor/ImportAccessorAction.java

        MoMLChangeRequest request = new MoMLChangeRequest(originator, context,
                changeRequest) {
                @Override
                protected void _postParse(MoMLParser parser) {
                    List<NamedObj> topObjects = parser.topObjectsCreated();
                    if (topObjects == null) {
                        return;
                    }
                    for (NamedObj object : topObjects) {
                        Location location = (Location) object
                                .getAttribute("_location");
                        // Set the location.
                        if (location == null) {
                            try {
                                location = new Location(object, "_location");
                            } catch (KernelException e) {
                                // Ignore.
                            }
                        }
                        if (location != null) {
                            try {
                                location.setLocation(new double[] { x, y });
                            } catch (IllegalActionException e) {
                                // Ignore.
                            }
                        }
                        // Set the source.
                        Attribute source = object
                                .getAttribute("accessorSource");
                        if (source instanceof StringAttribute) {
                            try {
                                ((StringAttribute) source).setExpression(urlSpec);
                                // Have to mark persistent or the urlSpec will be assumed to be part
                                // of the class definition and hence will not be exported to MoML.
                                ((StringAttribute) source)
                                        .setDerivedLevel(Integer.MAX_VALUE);
                                ((StringAttribute) source).setPersistent(true);
                            } catch (IllegalActionException e) {
                                // Should not happen.
                                throw new InternalErrorException(object, e,
                                        "Failed to set accessorSource");
                            }
                        }
                    }
                    parser.clearTopObjectsList();
                    super._postParse(parser);
                }

                @Override
                protected void _preParse(MoMLParser parser) {
                    super._preParse(parser);
                    parser.clearTopObjectsList();
                }
            };
        context.requestChange(request);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class so that the name of any port added is
     *  shown.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    @Override
    protected void _addPort(TypedIOPort port) throws IllegalActionException,
            NameDuplicationException {
        super._addPort(port);
        SingletonParameter showName = new SingletonParameter(port, "_showName");
        showName.setExpression("true");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Attribute with an associate named action.
     */
    public class ActionableAttribute extends StringAttribute implements Actionable {

	/** Create a new actionable attribute.
	 *  @param container The container.
	 *  @param name The name.
	 *  @throws IllegalActionException If the base class throws it.
	 *  @throws NameDuplicationException If the base class throws it.
	 */
	public ActionableAttribute(NamedObj container, String name)
		throws IllegalActionException, NameDuplicationException {
	    super(container, name);
	}

	/** Return "Reload". */
	@Override
	public String actionName() {
	    return "Reload";
	}

	/** Reload the accessor. */
	@Override
	public void performAction() throws Exception {
	    /* No longer need the following, since we don't overwrite overrides.
	    try {
		MessageHandler.warning("Warning: Overridden parameter values will be lost. Proceed?");
	    } catch (CancelException e) {
		return;
	    }
	    */
	    StringBuffer moml = new StringBuffer("<group name=\"doNotOverwriteOverrides\">");
	    moml.append(JSAccessor.accessorToMoML(accessorSource.getExpression()));
	    moml.append("</group>");
	    final NamedObj container = getContainer();
	    MoMLChangeRequest request = new MoMLChangeRequest(container, container, moml.toString()) {
		// Wrap this to give a more useful error message.
		protected void _execute() throws Exception {
		    try {
			super._execute();
		    } catch (Exception e) {
			// FIXME: Can we undo?
			throw new IllegalActionException(container, e,
				"Failed to reload accessor. Perhaps changes are two extensive."
				+ " Try re-importing the accessor.");
		    }
		}
	    };
	    request.setUndoable(true);
	    container.requestChange(request);
	}
    }
}
