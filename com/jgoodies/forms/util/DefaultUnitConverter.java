/*
 * Copyright (c) 2002-2007 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.forms.util;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * This is the default implementation of the {@link UnitConverter} interface.
 * It converts horizontal and vertical dialog base units to pixels.<p>
 *
 * The horizontal base unit is equal to the average width, in pixels,
 * of the characters in the system font; the vertical base unit is equal
 * to the height, in pixels, of the font.
 * Each horizontal base unit is equal to 4 horizontal dialog units;
 * each vertical base unit is equal to 8 vertical dialog units.<p>
 *
 * The DefaultUnitConverter computes dialog base units using a default font
 * and a test string for the average character width. You can configure
 * the font and the test string via the bound Bean properties
 * <em>defaultDialogFont</em> and <em>averageCharacterWidthTestString</em>.
 * See also Microsoft's suggestion for a custom computation
 * <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnwue/html/ch14e.asp">here</a>.<p>
 *
 * Since the Forms 1.1 this converter logs font information at
 * the <code>CONFIG</code> level.
 *
 * @version $Revision$
 * @author  Karsten Lentzsch
 * @see     UnitConverter
 * @see     com.jgoodies.forms.layout.Size
 * @see     com.jgoodies.forms.layout.Sizes
 */
public final class DefaultUnitConverter extends AbstractUnitConverter {

    //    public static final String UPPERCASE_ALPHABET =
    //        "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    //
    //    public static final String LOWERCASE_ALPHABET =
    //        "abcdefghijklmnopqrstuvwxyz";

    private final static Logger LOGGER = Logger
            .getLogger(DefaultUnitConverter.class.getName());

    /**
     * Holds the sole instance that will be lazily instantiated.
     */
    private static DefaultUnitConverter instance;

    /**
     * Holds the string that is used to compute the average character width.
     * By default this is just &quot;X&quot;.
     */
    private String averageCharWidthTestString = "X";

    /**
     * Holds the font that is used to compute the global dialog base units.
     * By default it is lazily created in method #getDefaultDialogFont,
     * which in turn looks up a font in method #lookupDefaultDialogFont.
     */
    private Font defaultDialogFont;

    /**
     * If any <code>PropertyChangeListeners</code> have been registered,
     * the <code>changeSupport</code> field describes them.
     *
     * @serial
     * @see #addPropertyChangeListener(PropertyChangeListener)
     * @see #addPropertyChangeListener(String, PropertyChangeListener)
     * @see #removePropertyChangeListener(PropertyChangeListener)
     * @see #removePropertyChangeListener(String, PropertyChangeListener)
     */
    private PropertyChangeSupport changeSupport;

    // Cached *****************************************************************

    /**
     * Holds the cached global dialog base units that are used if
     * a component is not (yet) available - for example in a Border.
     */
    private DialogBaseUnits cachedGlobalDialogBaseUnits = computeGlobalDialogBaseUnits();

    /**
     * Maps <code>FontMetrics</code> to horizontal dialog base units.
     * This is a second-level cache, that stores dialog base units
     * for a <code>FontMetrics</code> object.
     */
    private Map cachedDialogBaseUnits = new HashMap();

    // Instance Creation and Access *******************************************

    /**
     * Constructs a DefaultUnitConverter and registers
     * a listener that handles changes in the look&amp;feel.
     */
    private DefaultUnitConverter() {
        UIManager.addPropertyChangeListener(new LookAndFeelChangeHandler());
        changeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Lazily instantiates and returns the sole instance.
     *
     * @return the lazily instantiated sole instance
     */
    public static DefaultUnitConverter getInstance() {
        if (instance == null) {
            instance = new DefaultUnitConverter();
        }
        return instance;
    }

    // Access to Bound Properties *********************************************

    /**
     * Returns the string used to compute the average character width.
     * By default it is initialized to &quot;X&quot;.
     *
     * @return the test string used to compute the average character width
     */
    public String getAverageCharacterWidthTestString() {
        return averageCharWidthTestString;
    }

    /**
     * Sets a string that will be used to compute the average character width.
     * By default it is initialized to &quot;X&quot;. You can provide
     * other test strings, for example:
     * <ul>
     *  <li>&quot;Xximeee&quot;</li>
     *  <li>&quot;ABCEDEFHIJKLMNOPQRSTUVWXYZ&quot;</li>
     *  <li>&quot;abcdefghijklmnopqrstuvwxyz&quot;</li>
     * </ul>
     *
     * @param newTestString   the test string to be used
     * @exception IllegalArgumentException if the test string is empty
     * @exception NullPointerException     if the test string is <code>null</code>
     */
    public void setAverageCharacterWidthTestString(String newTestString) {
        if (newTestString == null) {
            throw new NullPointerException("The test string must not be null.");
        }
        if (newTestString.length() == 0) {
            throw new IllegalArgumentException(
                    "The test string must not be empty.");
        }

        String oldTestString = averageCharWidthTestString;
        averageCharWidthTestString = newTestString;
        changeSupport.firePropertyChange("averageCharacterWidthTestString",
                oldTestString, newTestString);
    }

    /**
     * Lazily creates and returns the dialog font used to compute
     * the dialog base units.
     *
     * @return the font used to compute the dialog base units
     */
    public Font getDefaultDialogFont() {
        if (defaultDialogFont == null) {
            defaultDialogFont = lookupDefaultDialogFont();
        }
        return defaultDialogFont;
    }

    /**
     * Sets a dialog font that will be used to compute the dialog base units.
     *
     * @param newFont   the default dialog font to be set
     */
    public void setDefaultDialogFont(Font newFont) {
        Font oldFont = defaultDialogFont; // Don't use the getter
        defaultDialogFont = newFont;
        changeSupport.firePropertyChange("defaultDialogFont", oldFont, newFont);
    }

    // Implementing Abstract Superclass Behavior ******************************

    /**
     * Returns the cached or computed horizontal dialog base units.
     *
     * @param component     a Component that provides the font and graphics
     * @return the horizontal dialog base units
     */
    @Override
    protected double getDialogBaseUnitsX(Component component) {
        return getDialogBaseUnits(component).x;
    }

    /**
     * Returns the cached or computed vertical dialog base units
     * for the given component.
     *
     * @param component     a Component that provides the font and graphics
     * @return the vertical dialog base units
     */
    @Override
    protected double getDialogBaseUnitsY(Component component) {
        return getDialogBaseUnits(component).y;
    }

    // Compute and Cache Global and Components Dialog Base Units **************

    /**
     * Lazily computes and answer the global dialog base units.
     * Should be re-computed if the l&amp;f, platform, or screen changes.
     *
     * @return a cached DialogBaseUnits object used globally if no container is available
     */
    private DialogBaseUnits getGlobalDialogBaseUnits() {
        if (cachedGlobalDialogBaseUnits == null) {
            cachedGlobalDialogBaseUnits = computeGlobalDialogBaseUnits();
        }
        return cachedGlobalDialogBaseUnits;
    }

    /**
     * Looks up and returns the dialog base units for the given component.
     * In case the component is <code>null</code> the global dialog base units
     * are answered.<p>
     *
     * Before we compute the dialog base units we check whether they
     * have been computed and cached before - for the same component
     * <code>FontMetrics</code>.
     *
     * @param c  the component that provides the graphics object
     * @return the DialogBaseUnits object for the given component
     */
    private DialogBaseUnits getDialogBaseUnits(Component c) {
        if (c == null) { // || (font = c.getFont()) == null) {
            // logInfo("Missing font metrics: " + c);
            return getGlobalDialogBaseUnits();
        }
        FontMetrics fm = c.getFontMetrics(getDefaultDialogFont());
        DialogBaseUnits dialogBaseUnits = (DialogBaseUnits) cachedDialogBaseUnits
                .get(fm);
        if (dialogBaseUnits == null) {
            dialogBaseUnits = computeDialogBaseUnits(fm);
            cachedDialogBaseUnits.put(fm, dialogBaseUnits);
        }
        return dialogBaseUnits;
    }

    /**
     * Computes and returns the horizontal dialog base units.
     * Honors the font, font size and resolution.<p>
     *
     * Implementation Note: 14dluY map to 22 pixel for 8pt Tahoma on 96 dpi.
     * I could not yet manage to compute the Microsoft compliant font height.
     * Therefore this method adds a correction value that seems to work
     * well with the vast majority of desktops.<p>
     *
     * TODO: Revise the computation of vertical base units as soon as
     * there are more information about the original computation
     * in Microsoft environments.
     *
     * @param metrics  the FontMetrics used to measure the dialog font
     * @return the horizontal and vertical dialog base units
     */
    private DialogBaseUnits computeDialogBaseUnits(FontMetrics metrics) {
        double averageCharWidth = computeAverageCharWidth(metrics,
                averageCharWidthTestString);
        int ascent = metrics.getAscent();
        double height = ascent > 14 ? ascent : ascent + (15 - ascent) / 3;
        DialogBaseUnits dialogBaseUnits = new DialogBaseUnits(averageCharWidth,
                height);
        LOGGER.config("Computed dialog base units " + dialogBaseUnits
                + " for: " + metrics.getFont());
        return dialogBaseUnits;
    }

    /**
     * Computes the global dialog base units. The current implementation
     * assumes a fixed 8pt font and on 96 or 120 dpi. A better implementation
     * should ask for the main dialog font and should honor the current
     * screen resolution.<p>
     *
     * Should be re-computed if the l&amp;f, platform, or screen changes.
     *
     * @return a DialogBaseUnits object used globally if no container is available
     */
    private DialogBaseUnits computeGlobalDialogBaseUnits() {
        LOGGER.config("Computing global dialog base units...");
        Font dialogFont = getDefaultDialogFont();
        FontMetrics metrics = createDefaultGlobalComponent().getFontMetrics(
                dialogFont);
        DialogBaseUnits globalDialogBaseUnits = computeDialogBaseUnits(metrics);
        return globalDialogBaseUnits;
    }

    /**
     * Looks up and returns the font used by buttons.
     * First, tries to request the button font from the UIManager;
     * if this fails a JButton is created and asked for its font.
     *
     * @return the font used for a standard button
     */
    private Font lookupDefaultDialogFont() {
        Font buttonFont = UIManager.getFont("Button.font");
        return buttonFont != null ? buttonFont : new JButton().getFont();
    }

    /**
     * Creates and returns a component that is used to lookup the default
     * font metrics. The current implementation creates a <code>JPanel</code>.
     * Since this panel has no parent, it has no toolkit assigned. And so,
     * requesting the font metrics will end up using the default toolkit
     * and its deprecated method <code>ToolKit#getFontMetrics()</code>.<p>
     *
     * TODO: Consider publishing this method and providing a setter, so that
     * an API user can set a realized component that has a toolkit assigned.
     *
     * @return a component used to compute the default font metrics
     */
    private Component createDefaultGlobalComponent() {
        return new JPanel(null);
    }

    /**
     * Invalidates the caches. Resets the global dialog base units
     * and clears the Map from <code>FontMetrics</code> to dialog base units.
     * This is invoked after a change of the look&amp;feel.
     */
    private void invalidateCaches() {
        cachedGlobalDialogBaseUnits = null;
        cachedDialogBaseUnits.clear();
    }

    // Managing Property Change Listeners **********************************

    /**
     * Adds a PropertyChangeListener to the listener list. The listener is
     * registered for all bound properties of this class.<p>
     *
     * If listener is null, no exception is thrown and no action is performed.
     *
     * @param listener      the PropertyChangeListener to be added
     *
     * @see #removePropertyChangeListener(PropertyChangeListener)
     * @see #removePropertyChangeListener(String, PropertyChangeListener)
     * @see #addPropertyChangeListener(String, PropertyChangeListener)
     */
    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list. This method
     * should be used to remove PropertyChangeListeners that were registered
     * for all bound properties of this class.<p>
     *
     * If listener is null, no exception is thrown and no action is performed.
     *
     * @param listener      the PropertyChangeListener to be removed
     *
     * @see #addPropertyChangeListener(PropertyChangeListener)
     * @see #addPropertyChangeListener(String, PropertyChangeListener)
     * @see #removePropertyChangeListener(String, PropertyChangeListener)
     */
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Adds a PropertyChangeListener to the listener list for a specific
     * property. The specified property may be user-defined.<p>
     *
     * Note that if this Model is inheriting a bound property, then no event
     * will be fired in response to a change in the inherited property.<p>
     *
     * If listener is null, no exception is thrown and no action is performed.
     *
     * @param propertyName      one of the property names listed above
     * @param listener          the PropertyChangeListener to be added
     *
     * @see #removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public synchronized void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list for a specific
     * property. This method should be used to remove PropertyChangeListeners
     * that were registered for a specific bound property.<p>
     *
     * If listener is null, no exception is thrown and no action is performed.
     *
     * @param propertyName      a valid property name
     * @param listener          the PropertyChangeListener to be removed
     *
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     * @see #removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public synchronized void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    // Helper Code ************************************************************

    /**
     * Describes horizontal and vertical dialog base units.
     */
    private static final class DialogBaseUnits {

        final double x;
        final double y;

        DialogBaseUnits(double dialogBaseUnitsX, double dialogBaseUnitsY) {
            this.x = dialogBaseUnitsX;
            this.y = dialogBaseUnitsY;
        }

        @Override
        public String toString() {
            return "DBU(x=" + x + "; y=" + y + ")";
        }
    }

    /**
     * Listens to changes of the Look and Feel and invalidates the cache.
     */
    private final class LookAndFeelChangeHandler implements
    PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            invalidateCaches();
        }
    }

}
