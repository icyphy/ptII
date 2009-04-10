/*
 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.kernel.util;

import java.io.IOException;
import java.util.ResourceBundle;

//////////////////////////////////////////////////////////////////////////
//// StaticResources

/**
 *
 * Static resources for accessing ResourceBundles.
 *
 * @author Matthew Brooke
 * @version $Id$
 * @since Ptolemy II 7.2
 * @Pt.ProposedRating
 * @Pt.AcceptedRating
 */
public class StaticResources {
    // kepler.gui.StaticGUIResources contains GUI specific code,
    // this class should _not_ import GUI code.

    // protected constructor - non-instantiable
    protected StaticResources() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////
    ///////////////////////////////////////////////////////////////////

    /**
     * Search the uiDisplayText resourcebundle for the property specified by the
     * key parameter. Return the boolean value of the property String
     * corresponding to the specified key. If the property is not found, the
     * defaultVal parameter is returned.
     *
     * @param key
     *            the properties key identifying the value to be found
     * @param defaultVal
     *            - the default boolean value to be returned if the requested
     *            property cannot be found or read
     * @return boolean value of the property String corresponding to the
     *         specified key. If the property is not found, the defaultVal
     *         parameter is returned.
     */
    public static boolean getBoolean(String key, boolean defaultVal) {

        boolean val = defaultVal;
        try {
            val = Boolean.valueOf(_getString(key, getUISettingsBundle()))
                    .booleanValue();
        } catch (Exception ex) {
            if (_isDebugging) {
                System.out
                        .println("StaticResources could not find the property for the key: "
                                + key
                                + "\n; returning default value: "
                                + defaultVal);
            }
        }
        return val;
    }

    /**
     * Search the uiDisplayText resourcebundle for the property specified by the
     * key parameter. Return the String value of the property value specified.
     * If the property is not found, the default defaultString parameter is
     * returned.
     *
     * @param key
     *            the properties key for the String to be found
     * @param defaultString
     *            - the default String to be returned if the property cannot be
     *            found or read
     * @return String value associated with the specified key, or the
     *         defaultString parameter if the property is not found.
     */
    public static String getDisplayString(String key, String defaultString) {

        String result = null;
        try {
            result = _getString(key, getDisplayTextBundle());
        } catch (Exception ex) {
            if (_isDebugging) {
                System.out
                        .println("StaticResources could not find String property for the key: "
                                + key
                                + "\n; returning default String: "
                                + defaultString);
            }
            return defaultString;
        }
        return result;
    }

    /**
     * Search for the ResourceBundle containing the UI Display Text.
     * @return The resource bundle that corresponds with
     * {@link #UI_DISPLAY_TEXT_BUNDLE}
     */
    public static ResourceBundle getDisplayTextBundle() throws IOException {

        if (_displayTextBundle == null) {
            _displayTextBundle = ResourceBundle
                    .getBundle(UI_DISPLAY_TEXT_BUNDLE);
        }
        return _displayTextBundle;
    }


    /**
     * Search the uiSettings resourcebundle for the property specified by the
     * key parameter. Return the String value of the property value specified.
     * If the property is not found, the default defaultString parameter is
     * returned.
     *
     * @param key
     *            the properties key for the String to be found
     * @param defaultString
     *            - the default String to be returned if the property cannot be
     *            found or read
     * @return String value associated with the specified key, or the
     *         defaultString parameter if the property is not found.
     */
    public static String getSettingsString(String key, String defaultString) {
        String result = null;
        try {
            result = _getString(key, getUISettingsBundle());
        } catch (Exception ex) {
            if (_isDebugging) {
                System.out
                        .println("StaticResources could not find String property for the key: "
                                + key
                                + "\n; returning default String: "
                                + defaultString);
            }
            return defaultString;
        }
        return result;
    }

    /**
     * Search the uiSettings resourcebundle for the size property specified by
     * the sizeKey. Return the integer (int) value of the size specified. If the
     * property is not found, the defaultSize parameter is returned.
     *
     * @param sizeKey
     *            the properties key String for the size setting
     * @param defaultSize
     *            - the default size to be used if the property cannot be found
     * @return integer (int) value of the size specified. If the property is not
     *         found, the defaultSize parameter is returned.
     */
    public static int getSize(String sizeKey, int defaultSize) {

        int size = 0;
        try {
            size = _getInt(sizeKey, getUISettingsBundle());
        } catch (Exception ex) {
            if (_isDebugging) {
                System.out
                        .println("StaticResources could not find size property for the key: "
                                + sizeKey
                                + "\n; returning default size: "
                                + defaultSize);
            }
            return defaultSize;
        }
        return size;
    }

    /**
     * Search for the ResourceBundle containing the ui settings.
     * @return The resource bundle that corresponds with
     * {@link #UI_SETTINGS_BUNDLE}
     */
    public static ResourceBundle getUISettingsBundle() throws IOException {

        if (_uiSettingsBundle == null) {
            _uiSettingsBundle = ResourceBundle.getBundle(UI_SETTINGS_BUNDLE);
        }
        return _uiSettingsBundle;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public variables                     ////
    ///////////////////////////////////////////////////////////////////


    // FIXME - get kepler-specific ref out of this path:
    /**
     * Path to directory containing resource bundle files. All paths are
     * relative to classpath.
     */
    public static final String RESOURCEBUNDLE_DIR = "ptolemy/configs/kepler";

    /**
     * Name of resource bundle containing mappings from fully-qualified
     * classnames => SVG icon paths. All paths are relative to classpath
     *
     * @see ptolemy.kernel.util.StaticResources#RESOURCEBUNDLE_DIR
     */
    public static final String SVG_ICON_MAPPINGS_BYCLASS_BUNDLE = RESOURCEBUNDLE_DIR
            + "/uiSVGIconMappingsByClass";

    /**
     * Path to resource bundle containing mappings from actor LSIDs => SVG icon
     * paths. All paths are relative to classpath
     *
     * @see ptolemy.kernel.util.StaticResources#RESOURCEBUNDLE_DIR
     */
    public static final String SVG_ICON_MAPPINGS_BYLSID_BUNDLE = RESOURCEBUNDLE_DIR
            + "/uiSVGIconMappingsByLSID";

    /**
     * Path to resource bundle containing basic default settings for SVG icons.
     *
     * @see ptolemy.kernel.util.StaticResources#RESOURCEBUNDLE_DIR
     */
    public static final String UI_SETTINGS_BUNDLE = RESOURCEBUNDLE_DIR
            + "/uiSettings";

    /**
     * Path to resource bundle containing UI Display Text.
     *
     * @see ptolemy.kernel.util.StaticResources#RESOURCEBUNDLE_DIR
     */
    public static final String UI_DISPLAY_TEXT_BUNDLE = RESOURCEBUNDLE_DIR
            + "/uiDisplayText";

    static {
        try {
            getUISettingsBundle();
        } catch (IOException ex) {
            // no worries - just try again when we actually need it
        }
        try {
            getDisplayTextBundle();
        } catch (IOException ex) {
            // no worries - just try again when we actually need it
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                      ////
    ///////////////////////////////////////////////////////////////////

    /**
     * Get the String property denoted by the propertyKey.
     *
     * @param propertyKey
     *            the properties key String identifying the property
     *
     * @param bundle
     *            the ResourceBundle in which to search
     *
     * @return the String value identified by the propertyKey
     *
     * @exception java.lang.Exception
     *             if key is not found or cannot be read
     */
    protected static String _getString(String propertyKey, ResourceBundle bundle)
            throws Exception {
        return bundle.getString(propertyKey);
    }

    /**
     * Get the integer (int) property denoted by the propertyKey.
     *
     * @param propertyKey
     *            the properties key String identifying the property
     *
     * @param bundle
     *            the ResourceBundle in which to search
     *
     * @return the int value identified by the propertyKey
     *
     * @exception Exception
     *             if key is not found, cannot be read, or cannot be parsed as
     *             an integer
     */
    protected static int _getInt(String propertyKey, ResourceBundle bundle)
            throws Exception {
        return Integer.parseInt(_getString(propertyKey, bundle));
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////
    ///////////////////////////////////////////////////////////////////

    /** Set to true and recompile for debugging such as error messages.*/
    protected final static boolean _isDebugging = false;

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    ///////////////////////////////////////////////////////////////////

    private static ResourceBundle _uiSettingsBundle;
    private static ResourceBundle _displayTextBundle;
}
