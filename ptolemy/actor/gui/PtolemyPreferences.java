/** Default preferences definition for Vergil. */

/*
 Copyright (c) 2006-2014 The Regents of the University of California.
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
 */

package ptolemy.actor.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ScopeExtendingAttribute;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// PtolemyPreferences

/**
 * Default preferences definition for Vergil. This is defined as a class rather
 * than in MoML so that the inheritance mechanism prevents exported MoML for
 * every model from duplicating this information.
 *
 * @author Edward A. Lee, Contributor: Bert Rodiers
 * @version $Id$
 @since Ptolemy II 5.2
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PtolemyPreferences extends ScopeExtendingAttribute {
    /** Construct an instance of the preferences attribute.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PtolemyPreferences(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Give the default values for all the preferences.
        // Names start with underscores by convention to minimize
        // the probability of conflict with model-specific parameters.
        // Note that the default values here should conform with the
        // defaults used in the code where these preferences are checked,
        // if there are any.  It's a good idea to provide them in
        // case setAsDefault() is never called.
        Parameter relationSize = new Parameter(this, "_relationSize");
        relationSize.setTypeEquals(BaseType.DOUBLE);
        relationSize.setExpression("12.0");
        relationSize.setDisplayName("Relation size");

        Parameter portSize = new Parameter(this, "_portSize");
        portSize.setTypeEquals(BaseType.DOUBLE);
        portSize.setExpression("4.0");
        portSize.setDisplayName("Port size");

        Parameter linkBendRadius = new Parameter(this, "_linkBendRadius");
        linkBendRadius.setTypeEquals(BaseType.DOUBLE);
        linkBendRadius.setExpression("20.0");
        linkBendRadius.setDisplayName("Link bend radius");

        backgroundColor = new ColorAttribute(this, "backgroundColor");
        backgroundColor.setExpression("{0.9, 0.9, 0.9, 1.0}");
        backgroundColor.setDisplayName("Background Color");

        StringParameter showParameters = new StringParameter(this,
                "_showParameters");
        showParameters.addChoice("None");
        showParameters.addChoice("Overridden parameters only");
        showParameters.addChoice("All");
        showParameters.setExpression("None");
        showParameters.setDisplayName("Show parameters");

        Parameter checkWidthConsistencyAtMultiports = new Parameter(this,
                "_checkWidthConsistencyAtMultiports");
        checkWidthConsistencyAtMultiports.setTypeEquals(BaseType.BOOLEAN);
        checkWidthConsistencyAtMultiports.setExpression("true");
        checkWidthConsistencyAtMultiports
                .setDisplayName("Check width consistency at multiports");

        Parameter checkWidthConstraints = new Parameter(this,
                "_checkWidthConstraints");
        checkWidthConstraints.setTypeEquals(BaseType.BOOLEAN);
        checkWidthConstraints.setExpression("true");
        checkWidthConstraints.setDisplayName("Check width constraints");

        Parameter defaultInferredWidthTo1 = new Parameter(this,
                "_defaultInferredWidthTo1");
        defaultInferredWidthTo1.setTypeEquals(BaseType.BOOLEAN);
        defaultInferredWidthTo1.setExpression("false");
        defaultInferredWidthTo1.setDisplayName("Default inferred width to 1");

        // The icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-60\" y=\"-10\" " + "width=\"120\" height=\"20\" "
                + "style=\"fill:#00FFFF\"/>\n" + "<text x=\"-55\" y=\"5\" "
                + "style=\"font-size:14; font-family:SansSerif; fill:blue\">\n"
                + "LocalPreferences\n" + "</text>\n" + "</svg>\n");
        // Hide the name.
        SingletonParameter hideName = new SingletonParameter(this, "_hideName");
        hideName.setToken(BooleanToken.TRUE);
        hideName.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public parameters                   ////

    /** The background color. */
    public ColorAttribute backgroundColor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the PtolemyPreferences within the specified configuration.
     *  @param configuration  The configuration in which to search for
     *  {@link #PREFERENCES_WITHIN_CONFIGURATION}
     *  @return The associated PtolemyPreferences or null if not found.
     *  @exception IllegalActionException If there is a problem getting the
     *  {@link #PREFERENCES_WITHIN_CONFIGURATION} attribute.
     */
    public static PtolemyPreferences getPtolemyPreferencesWithinConfiguration(
            Configuration configuration) throws IllegalActionException {
        PtolemyPreferences preferences = null;

        try {
            preferences = (PtolemyPreferences) configuration.getAttribute(
                    PtolemyPreferences.PREFERENCES_WITHIN_CONFIGURATION,
                    PtolemyPreferences.class);
        } catch (IllegalActionException ex) {
            return null;
        }
        return preferences;
    }

    /** Check to see whether a preference of the specified name is
     *  defined in the specified context, and if it is, return its value.
     *  Note that if there is an error in the expression for the preference,
     *  then this method will return null and report the error to standard out.
     *  This is done because we assume the error will normally be caught
     *  before this method is called.
     *  @param context The context for the preference.
     *  @param preferenceName The name of the preference.
     *  @return The value of the preference, or null if it is not set.
     */
    public static Token preferenceValue(NamedObj context, String preferenceName) {
        return ModelScope.preferenceValue(context, preferenceName);
    }

    /** Check to see whether a preference of the specified name is
     *  defined in the container of the specified context, either directly
     *  or within an instance of PtolemyPreferences, or
     *  globally, and if it is, return it's value. Do not look any higher
     *  in the hierarchy.
     *  Note that if there is an error in the expression for the preference,
     *  then this method will return null and report the error to standard out.
     *  This is done because we assume the error will normally be caught
     *  before this method is called.
     *  @param context The context for the preference.
     *  @param preferenceName The name of the preference.
     *  @return The value of the preference, or null if it is not set.
     */
    public static Token preferenceValueLocal(NamedObj context,
            String preferenceName) {
        try {
            NamedObj container = context.getContainer();
            if (container != null) {
                Attribute attribute = container.getAttribute(preferenceName);
                if (attribute instanceof Variable) {
                    return ((Variable) attribute).getToken();
                }
                Iterator<?> preferences = container.attributeList(
                        PtolemyPreferences.class).iterator();
                while (preferences.hasNext()) {
                    PtolemyPreferences preference = (PtolemyPreferences) preferences
                            .next();
                    attribute = preference.getAttribute(preferenceName);
                    if (attribute instanceof Variable) {
                        return ((Variable) attribute).getToken();
                    }
                }
            }
        } catch (IllegalActionException ex) {
            System.out.println("Warning: Invalid preference: " + ex);
        }
        // If no scoped variable is found, try for a defined constant.
        return Constants.get(preferenceName);
    }

    /** Save the preference values in this instance to the user
     *  preferences file.
     *  @exception IOException If an error occurs writing the file.
     */
    public void save() throws IOException {
        String libraryName = StringUtilities.preferencesDirectory()
                + PREFERENCES_FILE_NAME;
        File file = new File(libraryName);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            exportMoML(writer);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    System.out.println("Failed to close \"" + libraryName
                            + "\": " + ex);

                }
            }
        }
    }

    /** Set the values in this instance of PtolemyPreferences to be
     *  the default values by creating entries in the Constants class
     *  so that these values are accessible to any expression.
     *  @exception IllegalActionException If any expression for
     *   a preference cannot be evaluated.
     */
    public void setAsDefault() throws IllegalActionException {
        // Make the current global variables conform with any
        // overridden preference values.
        Iterator<?> parameters = attributeList(Variable.class).iterator();

        while (parameters.hasNext()) {
            Variable parameter = (Variable) parameters.next();
            Token token = parameter.getToken();
            Constants.add(parameter.getName(), token);
        }
    }

    /** Look for a default preferences object within the
     *  specified configuration, and set it as the default
     *  preferences. Then look for a user preferences file,
     *  and override the default preferences with the contents
     *  of that file. This method prints warning messages
     *  on standard out if there are problems with the
     *  preferences.
     *  @param configuration The specified configuration.
     */
    public static void setDefaultPreferences(Configuration configuration) {
        PtolemyPreferences preferences = null;
        try {
            preferences = getPtolemyPreferencesWithinConfiguration(configuration);
        } catch (IllegalActionException ex) {
            System.out.println("Warning: Problem with preferences attribute "
                    + "in the configuration: " + ex.getMessage());

            // Can't do anything further.
            return;
        }

        if (preferences == null) {
            // No preferences found in the configuration at
            // location "actor library.Utilities.LocalPreferences"
            return;
        }

        // Now override with the user file, if present.
        String libraryName = null;

        try {
            libraryName = StringUtilities.preferencesDirectory()
                    + PREFERENCES_FILE_NAME;
        } catch (Exception ex) {
            System.out.println("Warning: Failed to get the preferences "
                    + "directory (-sandbox always causes this): "
                    + ex.getMessage());

            // Can't do anything further.
            return;
        }

        File file = new File(libraryName);

        if (file.isFile() && file.canRead()) {
            // If we have a jar URL, convert spaces to %20
            URL fileURL;

            try {
                fileURL = JNLPUtilities
                        .canonicalizeJarURL(file.toURI().toURL());
            } catch (MalformedURLException ex) {
                // This should not occur.
                System.err.println("Malformed preferences URL: " + ex);
                return;
            }

            MoMLParser parser = new MoMLParser(preferences.workspace());
            parser.setContext(preferences.getContainer());

            // Set the ErrorHandler so that if we have
            // compatibility problems between devel and production
            // versions, we can skip that element.
            // MoMLParser.setErrorHandler(new VergilErrorHandler());

            try {
                parser.parse(fileURL, fileURL);
            } catch (Exception ex) {
                System.out.println("Failed to read user preferences file: "
                        + ex);
            }
        }

        try {
            preferences.setAsDefault();
        } catch (IllegalActionException ex) {
            System.out.println("Warning: Problem with preferences value: "
                    + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The file name where user-defined preferences are stored. */
    public static final String PREFERENCES_FILE_NAME = "PtolemyPreferences.xml";

    /** The location with the configuration of the preferences attribute. */
    public static final String PREFERENCES_WITHIN_CONFIGURATION = "actor library.Utilities.LocalPreferences";
}
