/** Default preferences definition for Vergil. */
package ptolemy.vergil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.JNLPUtilities;
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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.kernel.attributes.TextAttribute;

//////////////////////////////////////////////////////////////////////////
//// VergilPreferences

/**
 * Default preferences definition for Vergil. This is defined as a class rather
 * than in MoML so that the inheritance mechanism prevents exported MoML for
 * every model from duplicating this information.
 * 
 * @author Edward A. Lee
 * @version $Id$
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class VergilPreferences extends ScopeExtendingAttribute {

    /** Construct an instance of the preferences attribute
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public VergilPreferences(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        // Give the default values for all the preferences.
        // Names start with underscores by convention to minimize
        // the probability of conflict with model-specific parameters.
        // Note that the default values here should conform with the
        // defaults used in the code where these preferences are checked,
        // if there are any.  It's a good idea to provide them in
        // case setAsDefault() is never called.
        Parameter _relationSize = new Parameter(this, "_relationSize");
        _relationSize.setTypeEquals(BaseType.DOUBLE);
        _relationSize.setExpression("12.0");
        _relationSize.setDisplayName("Relation size");

        Parameter _linkBendRadius = new Parameter(this, "_linkBendRadius");
        _linkBendRadius.setTypeEquals(BaseType.DOUBLE);
        _linkBendRadius.setExpression("20.0");
        _linkBendRadius.setDisplayName("Link bend radius");

        StringParameter _showParameters = new StringParameter(this, "_showParameters");
        _showParameters.addChoice("None");
        _showParameters.addChoice("Overridden parameters only");
        _showParameters.addChoice("All");
        _showParameters.setExpression("None");
        _showParameters.setDisplayName("Show parameters");

        // The icon.
        EditorIcon _icon = new EditorIcon(this, "_icon");
        RectangleAttribute rectangle = new RectangleAttribute(_icon, "rectangle");
        rectangle.width.setExpression("120.0");
        rectangle.height.setExpression("20.0");
        rectangle.fillColor.setExpression("{0.2,1.0,1.0,1.0}");
        Location _location = new Location(rectangle, "_location"); 
        _location.setExpression("-5.0, -15.0");
        
        TextAttribute text = new TextAttribute(_icon, "text");
        text.text.setExpression("LocalPreferences");
        
        // Hide the name.
        SingletonParameter _hideName = new SingletonParameter(this, "_hideName");
        _hideName.setToken(BooleanToken.TRUE);
        _hideName.setVisibility(Settable.EXPERT);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Check to see whether a preference of the specified name is
     *  defined in the specified context, and if it is, return it's value.
     *  Note that if there is an error in the expression for the preference,
     *  then this method will return null and report the error to standard out.
     *  This is done because we assume the error will normally be caught
     *  before this method is called.
     *  @param context The context for the preference.
     *  @param preferenceName The name of the preference.
     *  @return The value of the preference, or null if it is not set.
     */
    public static Token preferenceValue(NamedObj context, String preferenceName) {
        Variable result = ModelScope.getScopedVariable(null, context, preferenceName);
        if (result != null) {
            try {
                return result.getToken();
            } catch (IllegalActionException ex) {
                System.out.println("Warning: Invalid _relationSize preference: " + ex);
            }
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
        FileWriter writer = new FileWriter(file);
        exportMoML(writer);
        writer.close();
    }
    
    /** Set the values in this instance of VergilPreferences to be
     *  the default values by creating entries in the Constants class
     *  so that these values are accessible to any expression.
     *  @exception IllegalActionException If any expression for
     *   a preference cannot be evaluated.
     */
    public void setAsDefault() throws IllegalActionException {
        // Make the current global variables conform with any
        // overridden preference values.
        Iterator parameters = attributeList(Variable.class).iterator();
        while (parameters.hasNext()) {
            Variable parameter = (Variable)parameters.next();
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
     */
    public static void setDefaultPreferences(Configuration configuration) {
        VergilPreferences preferences = null;
        try {
            preferences = (VergilPreferences)configuration.getAttribute(
                    VergilPreferences.PREFERENCES_WITHIN_CONFIGURATION,
                    VergilPreferences.class);
        } catch (IllegalActionException ex) {
            System.out.println(
                    "Warning: Problem with preferences attribute in the configuration: "
                    + ex.getMessage());
            // Can't do anything further.
            return;
        }
        // Now override with the user file, if present.
        String libraryName = null;
        try {
            libraryName = StringUtilities.preferencesDirectory()
                    + PREFERENCES_FILE_NAME;
        } catch (Exception ex) {
            System.out.println("Warning: Failed to get the preferences "
                    + "directory (-sandbox always causes this): " + ex.getMessage());
            // Can't do anything further.
            return;
        }
        File file = new File(libraryName);

        if (file.isFile() && file.canRead()) {
            System.out.println("Opening user preferences "
                    + PREFERENCES_FILE_NAME
                    + "...");

            // If we have a jar URL, convert spaces to %20
            URL fileURL;
            try {
                fileURL = JNLPUtilities.canonicalizeJarURL(file.toURL());
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
            MoMLParser.setErrorHandler(new VergilErrorHandler());
            try {
                parser.parse(fileURL, fileURL);
            } catch (Exception ex) {
                System.out.println("Failed to read user preferences file: " + ex);
            }
        }
        try {
            preferences.setAsDefault();
        } catch (IllegalActionException ex) {
            System.out.println(
                    "Warning: Problem with preferences value: "
                    + ex.getMessage());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       public variables                    ////

    /** The file name where user-defined preferences are stored. */
    public static String PREFERENCES_FILE_NAME
            = "VergilPreferences.xml";
    
    /** The location with the configuration of the preferences attribute. */
    public static String PREFERENCES_WITHIN_CONFIGURATION
            = "actor library.Utilities.LocalPreferences";
}
