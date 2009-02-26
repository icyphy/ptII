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

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

//////////////////////////////////////////////////////////////////////////
//// StaticResources

/**
 * 
 * Static resources for accessing ResourceBundles etc.
 * <p>
 * FIXME: this class imports awt classes, so it should not be in kernel.util.
 * 
 * @author Matthew Brooke
 * @version $Id: StaticResources.java 16353 2009-01-15 03:37:44Z aschultz $
 * @since Ptolemy II 7.2
 * @Pt.ProposedRating
 * @Pt.AcceptedRating
 */
public class StaticResources {

    // private constructor - non-instantiable
    private StaticResources() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////
    ///////////////////////////////////////////////////////////////////

    /**
     * Search the uiSettings resourcebundle for the width and height specified
     * by the widthKey and heightKey properties. Return a java.awt.Dimension
     * object with the width and height specified . If either or both of the
     * properties are not found, a Dimension object is returned with the width
     * and height specified by the defaultWidth and defaultHeight parameters.
     * This method should never return null
     * 
     * @param widthKey
     *            the properties key String for the width setting
     * 
     * @param heightKey
     *            the properties key String for the height setting
     * 
     * @param defaultWidth
     *            int - the default width to be used if the property cannot be
     *            found
     * 
     * @param defaultHeight
     *            int - the default height to be used if the property cannot be
     *            found
     * 
     * @return Dimension object with the width and height specified by the
     *         widthKey and heightID properties in the uiSettings
     *         resourcebundle. If either or both of the properties are not
     *         found, a Dimension object is returned with the width and height
     *         specified by the defaultWidth and defaultHeight parameters. This
     *         method should never return null
     */
    public static Dimension getDimension(String widthKey, String heightKey,
            int defaultWidth, int defaultHeight) {

        int width = 0;
        int height = 0;
        try {
            width = getInt(widthKey, getUISettingsBundle());
            height = getInt(heightKey, getUISettingsBundle());
        } catch (Exception ex) {
            if (isDebugging) {
                System.out
                        .println("StaticResources could not find Dimension(s) for the keys "
                                + widthKey
                                + " and/or "
                                + heightKey
                                + "\n; returning default dimensions: "
                                + defaultWidth + " x " + defaultHeight);
            }
            return new Dimension(defaultWidth, defaultHeight);
        }
        return new Dimension(width, height);
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
            size = getInt(sizeKey, getUISettingsBundle());
        } catch (Exception ex) {
            if (isDebugging) {
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
        String val = null;
        try {
            val = getString(key, getUISettingsBundle());
        } catch (Exception ex) {
            if (isDebugging) {
                System.out
                        .println("StaticResources could not find String property for the key: "
                                + key
                                + "\n; returning default String: "
                                + defaultString);
            }
            return defaultString;
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

        String val = null;
        try {
            val = getString(key, getDisplayTextBundle());
        } catch (Exception ex) {
            if (isDebugging) {
                System.out
                        .println("StaticResources could not find String property for the key: "
                                + key
                                + "\n; returning default String: "
                                + defaultString);
            }
            return defaultString;
        }
        return val;
    }

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
            val = Boolean.valueOf(getString(key, getUISettingsBundle()))
                    .booleanValue();
        } catch (Exception ex) {
            if (isDebugging) {
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
     * Search the uiSettings resourcebundle for the 3 color components specified
     * by the redComponent, greenComponent and blueComponent properties. Return
     * a java.awt.Color object representing the color specified. If any of the 3
     * properties are not found, null is returned
     * 
     * @param redComponent
     *            String the properties key String for the red component
     * 
     * @param greenComponent
     *            String the properties key String for the green component
     * 
     * @param blueComponent
     *            String the properties key String for the blue component
     * 
     * @return a java.awt.Color object representing the color specified. If any
     *         of the 3 properties are not found, null is returned
     */
    public static Color getColor(String redComponent, String greenComponent,
            String blueComponent) {
        int red = 0;
        int green = 0;
        int blue = 0;
        try {
            red = getInt(redComponent, getUISettingsBundle());
            green = getInt(greenComponent, getUISettingsBundle());
            blue = getInt(blueComponent, getUISettingsBundle());
        } catch (Exception ex) {
            if (isDebugging) {
                System.out
                        .println("StaticResources could not find Color component(s) for the keys:\n "
                                + redComponent
                                + ", "
                                + greenComponent
                                + " and/or "
                                + blueComponent
                                + "\n; returning NULL!");
            }
            return null;
        }
        return new Color(red, green, blue);
    }

    public static short getSVGRenderingMethod() {

        if (svgRenderingMethod == SVG_RENDERING_NOT_SET) {

            System.out
                    .println("*** Attempting to get ResourceBundle for SVG defaults ***");
            ResourceBundle defaultsBundle = null;
            try {
                defaultsBundle = getUISettingsBundle();
            } catch (Exception ex) {
                if (isDebugging) {
                    System.out.println("Exception getting defaultsBundle: "
                            + ex + "\nDefaulting to DIVA rendering");
                }
                svgRenderingMethod = SVG_DIVA_RENDERING;
                return svgRenderingMethod;
            }

            if (defaultsBundle == null) {
                if (isDebugging) {
                    System.out
                            .println("defaultsBundle==null; Defaulting to DIVA rendering");
                }
                svgRenderingMethod = SVG_DIVA_RENDERING;
                return svgRenderingMethod;
            }

            String isBatikStr = null;
            try {
                isBatikStr = defaultsBundle.getString("SVG_RENDERING_IS_BATIK");
            } catch (MissingResourceException mre) {
                if (isDebugging) {
                    System.out.println("MissingResourceException getting "
                            + "SVG_RENDERING_IS_BATIK"
                            + "\nDefaulting to DIVA rendering");
                }
                svgRenderingMethod = SVG_DIVA_RENDERING;
                return svgRenderingMethod;
            }

            if (isBatikStr != null
                    && isBatikStr.trim().equalsIgnoreCase("true")) {
                svgRenderingMethod = SVG_BATIK_RENDERING;
                System.out
                        .println("*** svgRenderingMethod = SVG_BATIK_RENDERING ***");
            } else {
                svgRenderingMethod = SVG_DIVA_RENDERING;
                System.out
                        .println("*** svgRenderingMethod = SVG_DIVA_RENDERING ***");
            }
        }
        return svgRenderingMethod;
    }

    public static ResourceBundle getUISettingsBundle() throws IOException {

        if (uiSettingsBundle == null) {
            uiSettingsBundle = ResourceBundle.getBundle(UI_SETTINGS_BUNDLE);
        }
        return uiSettingsBundle;
    }

    public static ResourceBundle getDisplayTextBundle() throws IOException {

        if (displayTextBundle == null) {
            displayTextBundle = ResourceBundle
                    .getBundle(UI_DISPLAY_TEXT_BUNDLE);
        }
        return displayTextBundle;
    }

    /**
     * get the platform the app is running on
     * 
     * @return one of the following positive int values representing the
     *         platform: KeplerApplication.WINDOWS KeplerApplication.MAC_OSX
     *         KeplerApplication.LINUX or -1 if the platform is unknown
     */
    public static int getPlatform() {
        return platform;
    }

    /**
     * set look & feel - first check if a user-specified L&F exists in the file
     * whose path is obtained from StaticResources.UI_SETTINGS_BUNDLE. If not,
     * use the default platform L&F
     */
    public static void setLookAndFeel() {

        // override ptii look & feel
        String propsLNF = null;
        String lnfClassName = null;
        try {
            ResourceBundle uiSettingsBundle = ResourceBundle
                    .getBundle(StaticResources.UI_SETTINGS_BUNDLE);
            lnfClassName = UIManager.getSystemLookAndFeelClassName();

            if (lnfClassName.indexOf("windows") > -1
                    || lnfClassName.indexOf("Windows") > -1) {
                platform = WINDOWS;
                propsLNF = uiSettingsBundle.getString("WINDOWS_LNF");
            } else if (lnfClassName.indexOf("apple") > -1
                    || lnfClassName.indexOf("Aqua") > -1) {
                platform = MAC_OSX;
                propsLNF = uiSettingsBundle.getString("MACOSX_LNF");
            } else {
                platform = LINUX;
                propsLNF = uiSettingsBundle.getString("LINUX_LNF");
            }
            Class classDefinition = Class.forName(propsLNF);
            UIManager.setLookAndFeel((LookAndFeel) classDefinition
                    .newInstance());
            return;
        } catch (Exception e) {
            // Ignore exceptions, which only result in the wrong look and feel.
        }
        // gets here only if a custom L&F was not found,
        // or was found but not successfully assigned
        try {
            UIManager.setLookAndFeel(lnfClassName);
        } catch (Exception ex) {
            // Ignore exceptions, which only result in the wrong look and feel.
        }
    }

    // FIXME - get kepler-specific ref out of this path:
    /**
     * Path to directory containing resource bundle files. All paths are
     * relative to classpath.
     */
    public static final String RESOURCEBUNDLE_DIR = "ptolemy/configs/kepler";

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

    public static final short SVG_RENDERING_NOT_SET = 0;
    public static final short SVG_DIVA_RENDERING = 1;
    public static final short SVG_BATIK_RENDERING = 2;

    public static final int WINDOWS = 1;
    public static final int MAC_OSX = 2;
    public static final int LINUX = 3;

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
    ////                      protected variables                  ////
    ///////////////////////////////////////////////////////////////////

    /**
     * Name of resource bundle containing mappings from fully-qualified
     * classnames => SVG icon paths. All paths are relative to classpath
     * 
     * @see ptolemy.kernel.util.StaticResources#RESOURCEBUNDLE_DIR
     */
    protected static final String SVG_ICON_MAPPINGS_BYCLASS_BUNDLE = RESOURCEBUNDLE_DIR
            + "/uiSVGIconMappingsByClass";

    /**
     * Path to resource bundle containing mappings from actor LSIDs => SVG icon
     * paths. All paths are relative to classpath
     * 
     * @see ptolemy.kernel.util.StaticResources#RESOURCEBUNDLE_DIR
     */
    protected static final String SVG_ICON_MAPPINGS_BYLSID_BUNDLE = RESOURCEBUNDLE_DIR
            + "/uiSVGIconMappingsByLSID";

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////
    ///////////////////////////////////////////////////////////////////

    /**
     * get the String property denoted by the propertyKey
     * 
     * @param propertyKey
     *            the properties key String identifying the property
     * 
     * @param bundle
     *            the ResourceBundle in which to search
     * 
     * @return the String value identified by the propertyKey
     * 
     * @throws java.lang.Exception
     *             if key is not found or cannot be read
     */
    private static String getString(String propertyKey, ResourceBundle bundle)
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
    private static int getInt(String propertyKey, ResourceBundle bundle)
            throws Exception {
        return Integer.parseInt(getString(propertyKey, bundle));
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    ///////////////////////////////////////////////////////////////////

    private static short svgRenderingMethod = SVG_RENDERING_NOT_SET;

    private static ResourceBundle uiSettingsBundle;
    private static ResourceBundle displayTextBundle;

    private static boolean isDebugging = false;

    private static int platform = -1;
}
