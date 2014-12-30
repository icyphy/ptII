/*
 * Copyright (c) 2002-2006 JGoodies Karsten Lentzsch. All Rights Reserved.
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

package com.jgoodies.forms.builder;

import java.awt.Component;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A general purpose panel builder that uses the {@link FormLayout}
 * to lay out <code>JPanel</code>s. In addition to its superclass
 * {@link PanelBuilder} this class provides convenience behavior to map
 * resource keys to their associated internationalized (i15d) strings
 * when adding labels, titles and titled separators.<p>
 *
 * The localized texts used in methods <code>#addI15dLabel</code>
 * and <code>#addI15dTitle</code> can contain an optional mnemonic marker.
 * The mnemonic and mnemonic index are indicated by a single ampersand
 * (<tt>&amp;</tt>). For example <tt>&quot;&amp;Save&quot;</tt>, or
 * <tt>&quot;Save&nbsp;&amp;as&quot;</tt>. To use the ampersand itself,
 * duplicate it, for example <tt>&quot;Look&amp;&amp;Feel&quot;</tt>.<p>
 *
 * @author        Karsten Lentzsch
 * @version $Revision$
 * @since 1.0.3
 *
 * @see        ResourceBundle
 */
public class I15dPanelBuilder extends PanelBuilder {

    /**
     * Holds the <code>ResourceBundle</code> used to lookup internationalized
     * (i15d) String resources.
     */
    private final ResourceBundle bundle;

    // Instance Creation ****************************************************

    /**
     * Constructs an instance of <code>I15dPanelBuilder</code> for the given
     * layout. Uses an instance of <code>JPanel</code> as layout container.
     *
     * @param layout    the form layout used to layout the container
     * @param bundle    the resource bundle used to lookup i15d strings
     */
    public I15dPanelBuilder(FormLayout layout, ResourceBundle bundle) {
        this(new JPanel(null), layout, bundle);
    }

    /**
     * Constructs an instance of <code>I15dPanelBuilder</code>
     * for the given FormLayout and layout container.
     *
     * @param layout  the <code>FormLayout</code> used to layout the container
     * @param bundle  the <code>ResourceBundle</code> used to lookup i15d strings
     * @param panel   the layout container
     */
    public I15dPanelBuilder(FormLayout layout, ResourceBundle bundle,
            JPanel panel) {
        super(layout, panel);
        this.bundle = bundle;
    }

    /**
     * Constructs an instance of <code>I15dPanelBuilder</code>
     * for the given FormLayout and layout container.
     *
     * @param panel   the layout container
     * @param bundle  the <code>ResourceBundle</code> used to lookup i15d strings
     * @param layout  the <code>FormLayout</code> used to layout the container
     *
     * @deprecated Replaced by {@link #I15dPanelBuilder(FormLayout, ResourceBundle, JPanel)}.
     */
    @Deprecated
    public I15dPanelBuilder(JPanel panel, FormLayout layout,
            ResourceBundle bundle) {
        this(layout, bundle, panel);
    }

    // Adding Labels and Separators *****************************************

    /**
     * Adds an internationalized (i15d) textual label to the form using the
     * specified constraints.
     *
     * @param resourceKey        the resource key for the label's text
     * @param constraints        the label's cell constraints
     * @return the added label
     */
    public final JLabel addI15dLabel(String resourceKey,
            CellConstraints constraints) {
        return addLabel(getI15dString(resourceKey), constraints);
    }

    /**
     * Adds an internationalized (i15d) textual label to the form using the
     * specified constraints.
     *
     * @param resourceKey         the resource key for the label's text
     * @param encodedConstraints  a string representation for the constraints
     * @return the added label
     */
    public final JLabel addI15dLabel(String resourceKey,
            String encodedConstraints) {
        return addI15dLabel(resourceKey,
                new CellConstraints(encodedConstraints));
    }

    /**
     * Adds an internationalized (i15d) label and component to the panel using
     * the given cell constraints. Sets the label as <i>the</i> component label
     * using {@link JLabel#setLabelFor(java.awt.Component)}.<p>
     *
     * <strong>Note:</strong> The {@link CellConstraints} objects for the label
     * and the component must be different. Cell constraints are implicitly
     * cloned by the <code>FormLayout</code> when added to the container.
     * However, in this case you may be tempted to reuse a
     * <code>CellConstraints</code> object in the same way as with many other
     * builder methods that require a single <code>CellConstraints</code>
     * parameter.
     * The pitfall is that the methods <code>CellConstraints.xy**(...)</code>
     * just set the coordinates but do <em>not</em> create a new instance.
     * And so the second invocation of <code>xy***(...)</code> overrides
     * the settings performed in the first invocation before the object
     * is cloned by the <code>FormLayout</code>.<p>
     *
     * <strong>Wrong:</strong><pre>
     * builder.add("name.key",
     *             cc.xy(1, 7),         // will be modified by the code below
     *             nameField,
     *             cc.xy(3, 7)          // sets the single instance to (3, 7)
     *            );
     * </pre>
     * <strong>Correct:</strong><pre>
     * builder.add("name.key",
     *             cc.xy(1, 7).clone(), // cloned before the next modification
     *             nameField,
     *             cc.xy(3, 7)          // sets this instance to (3, 7)
     *            );
     * </pre>
     *
     * @param resourceKey           the resource key for the label
     * @param labelConstraints      the label's cell constraints
     * @param component             the component to add
     * @param componentConstraints  the component's cell constraints
     * @return the added label
     * @exception IllegalArgumentException if the same cell constraints instance
     *     is used for the label and the component
     * @see JLabel#setLabelFor(java.awt.Component)
     */
    public final JLabel addI15dLabel(String resourceKey,
            CellConstraints labelConstraints, Component component,
            CellConstraints componentConstraints) {

        return addLabel(getI15dString(resourceKey), labelConstraints,
                component, componentConstraints);
    }

    /**
     * Adds an internationalized (i15d) titled separator to the form using the
     * specified constraints.
     *
     * @param resourceKey  the resource key for the separator title
     * @param constraints  the separator's cell constraints
     * @return the added titled separator
     */
    public final JComponent addI15dSeparator(String resourceKey,
            CellConstraints constraints) {
        return addSeparator(getI15dString(resourceKey), constraints);
    }

    /**
     * Adds an internationalized (i15d)  titled separator to the form using
     * the specified constraints.
     *
     * @param resourceKey         the resource key for the separator title
     * @param encodedConstraints  a string representation for the constraints
     * @return the added titled separator
     */
    public final JComponent addI15dSeparator(String resourceKey,
            String encodedConstraints) {
        return addI15dSeparator(resourceKey, new CellConstraints(
                encodedConstraints));
    }

    /**
     * Adds a title to the form using the specified constraints.
     *
     * @param resourceKey  the resource key for  the separator title
     * @param constraints  the separator's cell constraints
     * @return the added title label
     */
    public final JLabel addI15dTitle(String resourceKey,
            CellConstraints constraints) {
        return addTitle(getI15dString(resourceKey), constraints);
    }

    /**
     * Adds a title to the form using the specified constraints.
     *
     * @param resourceKey         the resource key for the separator title
     * @param encodedConstraints  a string representation for the constraints
     * @return the added title label
     */
    public final JLabel add15dTitle(String resourceKey,
            String encodedConstraints) {
        return addI15dTitle(resourceKey,
                new CellConstraints(encodedConstraints));
    }

    // Helper Code **********************************************************

    /**
     * Looks up and returns the internationalized (i15d) string for the given
     * resource key from the <code>ResourceBundle</code>.
     *
     * @param resourceKey  the key to look for in the resource bundle
     * @return the associated internationalized string, or the resource key
     *     itself in case of a missing resource
     * @exception IllegalStateException  if no <code>ResourceBundle</code>
     *     has been set
     */
    protected String getI15dString(String resourceKey) {
        if (bundle == null) {
            throw new IllegalStateException("You must specify a ResourceBundle"
                    + " before using the internationalization support.");
        }
        try {
            return bundle.getString(resourceKey);
        } catch (MissingResourceException mre) {
            return resourceKey;
        }
    }

}
