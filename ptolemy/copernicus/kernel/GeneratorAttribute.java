/* An attribute that stores the configuration of a code generator

 Copyright (c) 1998-2003 The Regents of the University of California.
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

package ptolemy.copernicus.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.Documentation;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.StringUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// GeneratorAttribute
/**
This is an attribute that stores the configuration of a code generator.

<p>The initial default parameters, their values and their documentation
are read in from a MoML file specified by the <i>initialParametersURL</i>.
Having the parameters defined in a MoML file allows us to easily
add and modify parameters without lots of bookkeeping.

<p>To view the initial default parameters, either call toString(), or
run:
<pre>
java -classpath $PTII ptolemy.copernicus.kernel.Copernicus -help
</pre>

@author Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class GeneratorAttribute extends SingletonAttribute 
    implements ChangeListener {

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GeneratorAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>"
                + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");

        initialParametersURL =
            new StringParameter(this, "initialParametersURL");
        initialParametersURL
            .setToken(new StringToken("ptolemy/copernicus/kernel/Generator.xml"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Parameters                        ////


    /** MoML file that contains other parameters.  The default value
     *  is the string "ptolemy/copernicus/kernel/Generator.xml".
     */
    public StringParameter initialParametersURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void changeExecuted(ChangeRequest change) {
    }

    public void changeFailed(ChangeRequest change, final Exception exception) {
    }


    /** Get the string for the argument with the given name from this
     * GeneratorAttribute.
     */
    public String getParameter(String name) throws IllegalActionException {
        Attribute attribute = getAttribute(name);
        if(attribute instanceof StringAttribute) {
            StringAttribute stringAttribute = (StringAttribute)attribute;
            return stringAttribute.getExpression();
        } else if(attribute instanceof Parameter) {
            Parameter parameter = (Parameter)attribute;
            Token token = parameter.getToken();
            if(token instanceof StringToken) {
                return ((StringToken)token).stringValue();
            } else {
                throw new IllegalActionException("Parameter with name " + name 
                        + " is not a String.");
            }
        } else {
            throw new IllegalActionException("Parameter with name " + name
                    + " is not set!");
        }
    }

    /** If this GeneratorAttribute has not yet been initialized, the
     *  initialized it by reading the moml file named by the
     *  initialParametersURL and creating Parameters and Variables
     *  accordingly.
     */
    public void initialize()
            throws IllegalActionException, NameDuplicationException {
        if (_initialized) {
            return;
        }

        if (initialParametersURL == null) {
            throw new IllegalActionException(this, "initialParametersURL "
                    + "parameter was null?");
        }

        // Read in the initialParameters file.
        URL initialParameters =
            getClass().getClassLoader()
            .getResource(initialParametersURL.getExpression());
        //            .getResource(((StringToken)initialParametersURL.getToken())
            //                    .stringValue());
        if (initialParameters == null) {
            throw new IllegalActionException(this, "Failed to find the "
                    + "value of the "
                    + "initialParametersURL: '"
                    + initialParametersURL
                    .getExpression()
                    + "'");
        }


        try {
            BufferedReader inputReader = new BufferedReader(
                    new InputStreamReader(
                            initialParameters.openStream()));

            String inputLine;
            StringBuffer buffer = new StringBuffer();
            while ((inputLine = inputReader.readLine()) != null) {
                buffer.append(inputLine + "\n" );
            }
            inputReader.close();
            addChangeListener(this);
            try {
                requestChange(new MoMLChangeRequest(this, this,
                        buffer.toString()));
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex, "Failed to parse "
                        + buffer.toString());
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Failed to parse '"
                    + initialParametersURL
                    .getExpression()
                    + "'");
        }
        // We sanity check after modelPath has had a chance to be
        // set so that we avoid calling the parser on the initial default
        // file that we are not going to use anyway.
        //sanityCheckAndUpdateParameters();

        _initialized = true;
    }


    /** Given a dot separated classname, return the jar file or directory
     *  where the class can be found.
     *  @param necessaryClass  The dot separated class name, for example
     *  "ptolemy.kernel.util.NamedObj"
     *  @return If the class can be found as a resource, return the
     *  directory or jar file where the necessary class can be found.
     *  otherwise, return null.
     *  @deprecated Call ptolemy.util.ClassUtilities.lookupClassAsResource()
     *  instead;
     */
    public static String lookupClassAsResource(String necessaryClass) {
        return ptolemy.util
            .ClassUtilities.lookupClassAsResource(necessaryClass);
    }

    /*  Sanity check the parameters of this generator attribute and
     *  update them as necessary.  The moml file named by the
     *  modelPathOrURL method parameter is read in and the Parameters
     *  that are determined by the model itself are checked.
     *  Pathnames are also checked
     *
     *  @param modelPathOrURL The file pathname or URL to the model.
     *  If modelPathOrURL is null, then the value of the modelPath
     *  parameter is used.
     */
    public void sanityCheckAndUpdateParameters(String modelPathOrURL)
            throws IllegalActionException, NameDuplicationException {
       
        if (modelPathOrURL == null) {
            // Get the modelPath and update modelPath and model.
            modelPathOrURL = getParameter("modelPath");
        }

        // Update the modelName and iterations Parameters.
        updateModelAttributes(modelPathOrURL);

        String ptII = getParameter("ptII");
        String ptIIUserDirectory = getParameter("ptIIUserDirectory");

        // Check that we will be able to write to the value of
        // the ptIIUserDirectory Parameter, and that
        // if we are running under Webstart or InstallAnywhere,
        // then ptIIUserDirectory does not equal ptII.
        File ptIIUserDirectoryFile = new File(ptIIUserDirectory);
        if (!ptIIUserDirectoryFile.isDirectory()
                || !ptIIUserDirectoryFile.canWrite()
                || ((JNLPUtilities.isRunningUnderWebStart()
                     || StringUtilities.getProperty("lax.user.dir").length()
                            > 0)
                        && ptIIUserDirectory == ptII)) {

            // It would be nice to tell the user we are changing the
            // ptIIUserDirectory directory of the build.  Usually
            // ptIIUserDirectory is $PTII or ptolemy.ptII.dir

            // Get user.dir and create a ptII/cg subdir if necessary
            String userDir =
                StringUtilities.getProperty("user.dir");
            if (userDir != null) {
                ptIIUserDirectoryFile =
                    new File(userDir + "/ptII/cg");
                if (!ptIIUserDirectoryFile.isDirectory()) {
                    // No need to check the return value here,
                    // we do it later anyway
                    ptIIUserDirectoryFile.mkdirs();
                }
                if (!ptIIUserDirectoryFile.isDirectory()
                        || !ptIIUserDirectoryFile.canWrite()) {
                    throw new IllegalActionException("'" + ptIIUserDirectory
                            + "' was not a "
                            + "writable directory, "
                            + "so we tried '"
                            + ptIIUserDirectoryFile
                            + "', but we failed to "
                            + "make a writable"
                            + "directory?");
                } else {
                    ptIIUserDirectory = ptIIUserDirectoryFile.getPath();
                    ((StringParameter)getAttribute("ptIIUserDirectory"))
                        .setExpression("property(\"user.dir\") + "
                                + "\"/ptII/cg\"");
                }
            }
        }


        // Update ptIIUserDirectoryAsURL
        String ptIIUserDirectoryAsURL;
        try {
            ptIIUserDirectoryAsURL =
                (new File(ptIIUserDirectory)).toURL().toString();
        } catch (java.net.MalformedURLException ex) {
            ptIIUserDirectoryAsURL = ex.getMessage();
        }
        // Strip of the trailing /, it causes problems when
        // we invoke the browser to view an applet.
        if (ptIIUserDirectoryAsURL.endsWith("/")) {
            ptIIUserDirectoryAsURL =
                ptIIUserDirectoryAsURL
                .substring(0, ptIIUserDirectoryAsURL.length() - 1);
        }

        ((Variable)getAttribute("ptIIUserDirectoryAsURL"))
            .setExpression(ptIIUserDirectoryAsURL);
        

        String targetPath = getParameter("targetPath");

        // Check that ptIIUserDirectory + targetPath is writable.
        // targetPath depends on ptIIUserDirectory, so we should mess
        // with ptIIUserDirectory first.

        File targetPathFile = new File(ptIIUserDirectory, targetPath);
        if (!targetPathFile.isDirectory()
                || !targetPathFile.canWrite()) {
            // Make any directories
            if (!targetPathFile.mkdirs()) {
                throw new
                    IllegalActionException("'" + targetPathFile
                            + "' was not a "
                            + "writable directory, and "
                            + "mkdirs() failed");
            }
        }

        _updateNecessaryClassPath();

    }

    /** Return a String representation of this object. */
    public String toString() {
        // We use reflection here so that we don't have to edit
        // this method every time we add a field.
        StringBuffer results = new StringBuffer();
        Iterator attributes = attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            if (attribute instanceof Parameter) {
                StringBuffer value = new StringBuffer("\n Value:         ");
                try {
                    value.append(((Parameter)attribute).getToken());
                } catch (Exception ex) {
                    value.append(ex);
                }
                
                String className = "Parameter";
                if(attribute instanceof StringParameter) {
                    className = "StringParameter";
                }
                results.append(className + ":      " + attribute.getName()
                        + "\n Expression:    "
                        + ((Parameter)attribute).getExpression()
                        + value.toString()
                               );
            } else {
                results.append("Attribute:      " + attribute.getName());
            }
            Attribute tooltipAttribute =
                ((NamedObj)attribute).getAttribute("tooltip");
            if (tooltipAttribute != null
                    && tooltipAttribute instanceof Documentation) {
                results.append("\n Documentation: "
                        + ((Documentation)tooltipAttribute).getValue());
            } else {
                String tip = Documentation.consolidate((NamedObj)attribute);
                if (tip != null) {
                    results.append("\n Documentation: " + tip);
                }
            }
            results.append("\n\n");

        }
        return results.toString();
    }

    /** Update the modelPath, modelName and iterations parameters in
     *  the GeneratorAttribute.  This method parses the model and
     *  updates all GeneratorAttribute parameters that are determined
     *  by the model itself.
     *
     *  @param modelPathOrURL The file pathname or URL to the model.
     */
    public void updateModelAttributes(String modelPathOrURL)
            throws IllegalActionException {
        URL modelURL = null;
        try {
            modelURL = MoMLApplication.specToURL(modelPathOrURL);
        } catch (IOException ex) {
            try {
                // We might have a JAR URL because we are inside webstart
                modelURL = JNLPUtilities.jarURLEntryResource(modelPathOrURL);
            } catch (IOException ex2) {
            }
            if (modelURL == null) {
                throw new IllegalActionException(this, ex,
                        "Failed to parse '"
                        + modelPathOrURL + "'");
            }
        }

        MoMLParser parser = new MoMLParser();

        // Get the old filters, save them, add our own
        // filters, use them, remove our filters,
        // and then readd the old filters in the finally clause.
        List oldFilters = parser.getMoMLFilters();
        parser.setMoMLFilters(null);

        // Parse the model and get the name of the model.
        try {
            // Handle Backward Compatibility.
            parser.addMoMLFilters(BackwardCompatibility.allFilters());

            // Filter out any graphical classes
            RemoveGraphicalClasses filter = new RemoveGraphicalClasses();
            // FIXME: Not sure why this is necessary, but it helps
            // when generating an applet for moml/demo/spectrum.xml
            filter.put("ptolemy.kernel.util.Location", null);
            parser.addMoMLFilter(filter);


            NamedObj toplevel = null;
            try {
                toplevel = parser.parse(null, modelURL);
                // FIXME: 1st arg of parse() could be $PTII as a URL.
                modelPathOrURL = modelURL.toExternalForm();
            } catch (FileNotFoundException ex) {
                try {
                    // Might be under Web Start, try it this way.
                    URL anotherURL =
                        JNLPUtilities.jarURLEntryResource(modelPathOrURL);
                    if (anotherURL != null) {
                        toplevel = parser.parse(null, anotherURL);
                        modelPathOrURL = anotherURL.toExternalForm();
                    } else {
                        throw new Exception("1. Failed to find '"
                                + modelURL.toExternalForm()
                                + "'\n"
                                + "2. Failed to find '"
                                + anotherURL
                                + "'");
                    }
                } catch (Exception ex1) {
                    throw new IllegalActionException(this, ex1,
                            "Failed to parse '"
                            + modelPathOrURL + "'"
                            + " Tried loading as a resource, too!"
                                                     );
                }
            }

            Parameter modelPath = (StringParameter)getAttribute("modelPath");
            modelPath.setExpression(modelPathOrURL);

            // Strip off the leading '.' and then sanitize.
            String modelNameValue =
                StringUtilities
                .sanitizeName(toplevel.getFullName().substring(1));

            Parameter modelName = (StringParameter)getAttribute("modelName");
            modelName.setExpression(modelNameValue);

            // Set the iterations parameter.
            CompositeActor compositeActor = (CompositeActor)toplevel;
            Director director = compositeActor.getDirector();
            // If we save a blank model, then there might not be a director.
            Parameter iterations = (StringParameter)getAttribute("iterations");
            if (director == null) {
                iterations.setExpression("1000");
            } else {
                Attribute directorIterations =
                    director.getAttribute("iterations");
                if (directorIterations != null) {
                    Token iterationsToken =
                        ((Parameter)directorIterations)
                        .getToken();
                    iterations.setExpression(iterationsToken.toString());
                } else {
                    iterations.setExpression("1000");
                }
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to parse '"
                    + modelPathOrURL + "'");
        } finally {
            parser.setMoMLFilters(oldFilters);
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Update the value of the necessaryClassPath, where for each
    // element in necessaryClasses, we look for that class, and append
    // the jar file where it was found.  This is only necessary for
    // Java Web Start and other installations that are shipped as
    // separate jar files
    private void _updateNecessaryClassPath() throws IllegalActionException {

        
        //StringParameter necessaryClassesParameter =
        //    (StringParameter)getAttribute("necessaryClasses");
        ArrayToken necessaryClassesToken =
            (ArrayToken)
            ((Parameter)getAttribute("necessaryClasses"))
            .getToken();


         List classPathList = new LinkedList();
        for (int i = 0; i < necessaryClassesToken.length(); i++) {
            String necessaryClass =
                ((StringToken)necessaryClassesToken.getElement(i))
                .stringValue();

            String sanitizedResourceName = ptolemy.util
                .ClassUtilities.lookupClassAsResource(necessaryClass);

            if (sanitizedResourceName != null
                    && !classPathList.contains(sanitizedResourceName)) {
                classPathList.add(sanitizedResourceName);
            }
        }

        // Convert the list of directories to a classpath with separators.

        // We could use property("path.separator") here, but if the user
        // changes the classPathSeparator parameter, then we better use it.
        String classPathSeparator =
            ((StringToken)
                    ((Parameter)getAttribute("classPathSeparator"))
                    .getToken()).stringValue();


        StringBuffer necessaryClassPath = new StringBuffer();

        Iterator classPaths = classPathList.iterator();
        while (classPaths.hasNext()) {
            if (necessaryClassPath.length() > 0) {
                necessaryClassPath.append(classPathSeparator);
            }
            necessaryClassPath.append(classPaths.next());

        }

        ((StringParameter)getAttribute("necessaryClassPath"))
            .setExpression(necessaryClassPath.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // True if initialized() was called.
    private boolean _initialized = false;
}
