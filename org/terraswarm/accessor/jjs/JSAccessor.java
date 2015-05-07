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
package org.terraswarm.accessor.jjs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.jjs.JavaScript;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.FileUtilities;

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

        accessorSource = new StringAttribute(this, "accessorSource");
        accessorSource.setVisibility(Settable.NOT_EDITABLE);

        reloadAccessor = new Parameter(this, "reloadAccessor", BooleanToken.FALSE);
        reloadAccessor.setTypeEquals(BaseType.BOOLEAN);

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

    /** If true, then reload the accessor during preinitialize().
     *  The default value is a boolean token with the value false,
     *  indicating that the accessor is not reloaded during preinitialize();
     */
    public Parameter reloadAccessor;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Generate MoML for an Accessor.
     *  The accessor is read in from a url, processed with 
     *  XSLT and MoML is returned.
     *  @param urlSpec A string naming the url of the accessor.
     *  @return MoML of the accessor, which is typically passed to
     *  handleAccessorMoMLChangeRequest().
     *  @exception IOException If the urlSpec cannot be converted, opened
     *  read, parsed or closed.
     *  @exception TransformerConfigurationException If a factory cannot
     *  be created from the xslt file.
     */
    public static String accessorToMoML(final String urlSpec)
            throws IOException, TransformerConfigurationException {

        // This method is a separate method so that we can use it for
        // testing the reimportation of accessors.  See
        // https://www.terraswarm.org/accessors/wiki/Main/TestAPtolemyAccessorImport

        URL url;
        String input = "";
        StringBuffer buffer = new StringBuffer();
        buffer.append("<group name=\"auto\">\n");

            url = FileUtilities.nameToURL(urlSpec, null, null);

            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                                url.openStream()));
                StringBuffer contents = new StringBuffer();
                while ((input = in.readLine()) != null) {
                    contents.append(input);
                }
                TransformerFactory factory = TransformerFactory.newInstance();
                String xsltLocation = "$CLASSPATH/org/terraswarm/accessor/jjs/XMLJSToMoML.xslt";
                Source xslt = new StreamSource(FileUtilities.nameToFile(
                                xsltLocation, null));
                Transformer transformer = factory.newTransformer(xslt);
                StreamSource source = new StreamSource(new InputStreamReader(
                                url.openStream()));
                StringWriter outWriter = new StringWriter();
                StreamResult result = new StreamResult(outWriter);
                try {
                    transformer.transform(source, result);
                    contents = outWriter.getBuffer();
                    buffer.append(contents);
                } catch (Throwable throwable) {
                    IOException ioException = new IOException("Failed to parse \""
                            + url
                            + "\".");
                    ioException.initCause(throwable);
                    throw ioException;
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        buffer.append("</group>\n");

        return buffer.toString();
    }

    /** Handle an accessor-specific MoMLChangeRequest.
     *   
     *  In the postParse() phase, the _location and accessorSource
     *  attributes are updated.
     *
     *  @param originator The originator of the change request.
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
                                ((StringAttribute) source)
                                        .setExpression(urlSpec);
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

    /** If necessary reload the accessor.
     *  @exception IllegalActionException If the accessor cannot be reloaded
     *  or if thrown by the base class.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        // "Need to create a new actor and splice it in where the old
        // actor was, all the while preserving parameter overrides and
        // port connections. Here, having a type check at the actor
        // level would be useful... Is the new version of the accessor
        // a substitution instance for the old?"
        if (((BooleanToken)reloadAccessor.getToken()).booleanValue()) {
            try {
                // Get the ChangeRequest
                String changeRequest = JSAccessor.accessorToMoML(accessorSource.getExpression());

                // Iterate through the ports and parameters and look for
                // Mismatches.

                // Look for ports and parameters that are not present in
                // the new accessor.

                // Here's where it gets tricky, do we delete ourselves and
                // then recreate the actor?
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Problem reloading \"" + accessorSource.getExpression() + "\".");
            }
        }
        super.preinitialize();
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
}
