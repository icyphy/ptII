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

package com.jgoodies.forms.builder;

import java.awt.Component;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Provides a means to build form-oriented panels quickly and consistently
 * using the {@link FormLayout}. This builder combines frequently used
 * panel building steps: add a new row, add a label, proceed to the next
 * data column, then add a component.<p>
 *
 * The extra value lies in the <code>#append</code> methods that
 * append gap rows and component rows if necessary and then add
 * the given components. They are built upon the superclass behavior
 * <code>#appendRow</code> and the set of <code>#add</code> methods.
 * A set of component appenders allows to add a textual label and
 * associated component in a single step.<p>
 *
 * This builder can map resource keys to internationalized (i15d) texts
 * when creating text labels, titles and titled separators. Therefore
 * you must specify a <code>ResourceBundle</code> in the constructor.
 * The builder methods throw an <code>IllegalStateException</code> if one
 * of the mapping builder methods is invoked and no bundle has been set.<p>
 *
 * You can configure the build process by setting a leading column,
 * enabling the row grouping and by modifying the gaps between normal
 * lines and between paragraphs. The leading column will be honored
 * if the cursor proceeds to the next row. All appended components
 * start in the specified lead column, except appended separators that
 * span all columns.<p>
 *
 * It is tempting to use the DefaultFormBuilder all the time and
 * to let it add rows automatically. Use a simpler style if it increases
 * the code readability. Explicit row specifications and cell constraints
 * make your layout easier to understand - but harder to maintain.
 * See also the accompanying tutorial sources and the Tips &amp; Tricks
 * that are part of the Forms documentation.<p>
 *
 * Sometimes a form consists of many standardized rows but has a few
 * rows that require a customization. The DefaultFormBuilder can do everything
 * that the superclasses {@link com.jgoodies.forms.builder.AbstractFormBuilder}
 * and {@link com.jgoodies.forms.builder.PanelBuilder} can do;
 * among other things: appending new rows and moving the cursor.
 * Again, ask yourself if the DefaultFormBuilder is the appropriate builder.
 * As a rule of thumb you should have more components than builder commands.
 * There are different ways to add custom rows. Find below example code
 * that presents and compares the pros and cons of three approaches.<p>
 *
 * The texts used in methods <code>#append(String, ...)</code> and
 * <code>#appendTitle(String)</code> as well as the localized texts used in
 * methods <code>#appendI15d</code> and <code>#appendI15dTitle</code>
 * can contain an optional mnemonic marker. The mnemonic and mnemonic index
 * are indicated by a single ampersand (<tt>&amp;</tt>).
 * For example <tt>&quot;&amp;Save&quot;</tt>, or
 * <tt>&quot;Save&nbsp;&amp;as&quot;</tt>. To use the ampersand itself,
 * duplicate it, for example <tt>&quot;Look&amp;&amp;Feel&quot;</tt>.<p>
 *
 * <strong>Example:</strong>
 * <pre>
 * public void build() {
 *     FormLayout layout = new FormLayout(
 *         "right:max(40dlu;pref), 3dlu, 80dlu, 7dlu, " // 1st major column
 *       + "right:max(40dlu;pref), 3dlu, 80dlu",        // 2nd major column
 *         "");                                         // add rows dynamically
 *     DefaultFormBuilder builder = new DefaultFormBuilder(layout);
 *     builder.setDefaultDialogBorder();
 *
 *     builder.appendSeparator("Flange");
 *
 *     builder.append("Identifier", identifierField);
 *     builder.nextLine();
 *
 *     builder.append("PTI [kW]",   new JTextField());
 *     builder.append("Power [kW]", new JTextField());
 *
 *     builder.append("s [mm]",     new JTextField());
 *     builder.nextLine();
 *
 *     builder.appendSeparator("Diameters");
 *
 *     builder.append("da [mm]",    new JTextField());
 *     builder.append("di [mm]",    new JTextField());
 *
 *     builder.append("da2 [mm]",   new JTextField());
 *     builder.append("di2 [mm]",   new JTextField());
 *
 *     builder.append("R [mm]",     new JTextField());
 *     builder.append("D [mm]",     new JTextField());
 *
 *     builder.appendSeparator("Criteria");
 *
 *     builder.append("Location",   buildLocationComboBox());
 *     builder.append("k-factor",   new JTextField());
 *
 *     builder.appendSeparator("Bolts");
 *
 *     builder.append("Material",   ViewerUIFactory.buildMaterialComboBox());
 *     builder.nextLine();
 *
 *     builder.append("Numbers",    new JTextField());
 *     builder.nextLine();
 *
 *     builder.append("ds [mm]",    new JTextField());
 * }
 * </pre><p>
 *
 * <strong>Custom Row Example:</strong>
 * <pre>
 * public JComponent buildPanel() {
 *     initComponents();
 *
 *     FormLayout layout = new FormLayout(
 *             "right:pref, 3dlu, default:grow",
 *             "");
 *     DefaultFormBuilder builder = new DefaultFormBuilder(layout);
 *     builder.setDefaultDialogBorder();
 *     builder.setRowGroupingEnabled(true);
 *
 *     CellConstraints cc = new CellConstraints();
 *
 *     // In this approach, we add a gap and a custom row.
 *     // The advantage of this approach is, that we can express
 *     // the row spec and comment area cell constraints freely.
 *     // The disadvantage is the misalignment of the leading label.
 *     // Also the row's height may be inconsistent with other rows.
 *     builder.appendSeparator("Single Custom Row");
 *     builder.append("Name", name1Field);
 *     builder.appendRow(builder.getLineGapSpec());
 *     builder.appendRow(new RowSpec("top:31dlu")); // Assumes line is 14, gap is 3
 *     builder.nextLine(2);
 *     builder.append("Comment");
 *     builder.add(new JScrollPane(comment1Area),
 *                 cc.xy(builder.getColumn(), builder.getRow(), "fill, fill"));
 *     builder.nextLine();
 *
 *     // In this approach, we append a standard row with gap before it.
 *     // The advantage is, that the leading label is aligned well.
 *     // The disadvantage is that the comment area now spans
 *     // multiple cells and is slightly less flexible.
 *     // Also the row's height may be inconsistent with other rows.
 *     builder.appendSeparator("Standard + Custom Row");
 *     builder.append("Name", name2Field);
 *     builder.append("Comment");
 *     builder.appendRow(new RowSpec("17dlu")); // Assumes line is 14, gap is 3
 *     builder.add(new JScrollPane(comment2Area),
 *                 cc.xywh(builder.getColumn(), builder.getRow(), 1, 2));
 *     builder.nextLine(2);
 *
 *     // In this approach, we append two standard rows with associated gaps.
 *     // The advantage is, that the leading label is aligned well,
 *     // and the height is consistent with other rows.
 *     // The disadvantage is that the comment area now spans
 *     // multiple cells and is slightly less flexible.
 *     builder.appendSeparator("Two Standard Rows");
 *     builder.append("Name", name3Field);
 *     builder.append("Comment");
 *     builder.nextLine();
 *     builder.append("");
 *     builder.nextRow(-2);
 *     builder.add(new JScrollPane(comment3Area),
 *                 cc.xywh(builder.getColumn(), builder.getRow(), 1, 3));
 *
 *     return builder.getPanel();
 * }
 * </pre><p>
 *
 * TODO: Consider adding a method for appending a component that spans the
 * remaining columns in the current row. Method name candidates are
 * <code>#appendFullSpan</code> and <code>#appendRemaining</code>.
 *
 * @author        Karsten Lentzsch
 * @version $Revision$
 * @since 1.0.3
 *
 * @see        com.jgoodies.forms.builder.AbstractFormBuilder
 * @see        com.jgoodies.forms.factories.FormFactory
 * @see        com.jgoodies.forms.layout.FormLayout
 */
public final class DefaultFormBuilder extends I15dPanelBuilder {

    /**
     * Holds the row specification that is reused to describe
     * the constant gaps between component lines.
     */
    private RowSpec lineGapSpec = FormFactory.LINE_GAP_ROWSPEC;

    /**
     * Holds the row specification that describes the constant gaps
     * between paragraphs.
     */
    private RowSpec paragraphGapSpec = FormFactory.PARAGRAPH_GAP_ROWSPEC;

    /**
     * Holds the offset of the leading column - often 0 or 1.
     *
     * @see #getLeadingColumnOffset()
     * @see #setLeadingColumnOffset(int)
     * @see #getLeadingColumn()
     */
    private int leadingColumnOffset = 0;

    /**
     * Determines whether new data rows are being grouped or not.
     *
     * @see #isRowGroupingEnabled()
     * @see #setRowGroupingEnabled(boolean)
     */
    private boolean rowGroupingEnabled = false;

    // Instance Creation ****************************************************

    /**
     * Constructs an instance of <code>DefaultFormBuilder</code> for the given
     * layout.
     *
     * @param layout        the <code>FormLayout</code> to be used
     */
    public DefaultFormBuilder(FormLayout layout) {
        this(new JPanel(null), layout);
    }

    /**
     * Constructs an instance of <code>DefaultFormBuilder</code> for the given
     * panel and layout.
     *
     * @param layout    the <code>FormLayout</code> to be used
     * @param panel     the layout container
     */
    public DefaultFormBuilder(FormLayout layout, JPanel panel) {
        this(panel, layout, null);
    }

    /**
     * Constructs an instance of <code>DefaultFormBuilder</code> for the given
     * layout and resource bundle.
     *
     * @param layout    the <code>FormLayout</code> to be used
     * @param bundle    the <code>ResourceBundle</code> used to lookup i15d
     * strings
     */
    public DefaultFormBuilder(FormLayout layout, ResourceBundle bundle) {
        this(new JPanel(null), layout, bundle);
    }

    /**
     * Constructs an instance of <code>DefaultFormBuilder</code> for the given
     * panel, layout and resource bundle.
     *
     * @param layout    the <code>FormLayout</code> to be used
     * @param panel     the layout container
     * @param bundle    the <code>ResourceBundle</code> used to lookup i15d
     * strings
     */
    public DefaultFormBuilder(FormLayout layout, ResourceBundle bundle,
            JPanel panel) {
        super(layout, bundle, panel);
    }

    /**
     * Constructs an instance of <code>DefaultFormBuilder</code> for the given
     * panel and layout.
     *
     * @param panel     the layout container
     * @param layout    the <code>FormLayout</code> to be used
     *
     * @deprecated Replaced by {@link #DefaultFormBuilder(FormLayout, JPanel)}.
     */
    @Deprecated
    public DefaultFormBuilder(JPanel panel, FormLayout layout) {
        this(layout, null, panel);
    }

    /**
     * Constructs an instance of <code>DefaultFormBuilder</code> for the given
     * panel, layout and resource bundle.
     *
     * @param panel     the layout container
     * @param layout    the <code>FormLayout</code> to be used
     * @param bundle    the <code>ResourceBundle</code> used to lookup i15d
     * strings
     *
     * @deprecated Replaced by {@link #DefaultFormBuilder(FormLayout, ResourceBundle, JPanel)}.
     */
    @Deprecated
    public DefaultFormBuilder(JPanel panel, FormLayout layout,
            ResourceBundle bundle) {
        super(layout, bundle, panel);
    }

    // Settings Gap Sizes ***************************************************

    /**
     * Returns the row specification that is used to separate component lines.
     *
     * @return the <code>RowSpec</code> that is used to separate lines
     */
    public RowSpec getLineGapSpec() {
        return lineGapSpec;
    }

    /**
     * Sets the size of gaps between component lines using the given
     * constant size.<p>
     *
     * <strong>Examples:</strong><pre>
     * builder.setLineGapSize(Sizes.ZERO);
     * builder.setLineGapSize(Sizes.DLUY9);
     * builder.setLineGapSize(Sizes.pixel(1));
     * </pre>
     *
     * @param lineGapSize   the <code>ConstantSize</code> that describes
     *     the size of the gaps between component lines
     */
    public void setLineGapSize(ConstantSize lineGapSize) {
        RowSpec rowSpec = FormFactory.createGapRowSpec(lineGapSize);
        this.lineGapSpec = rowSpec;
    }

    /**
     * Sets the size of gaps between paragraphs using the given
     * constant size.<p>
     *
     * <strong>Examples:</strong><pre>
     * builder.setParagraphGapSize(Sizes.DLUY14);
     * builder.setParagraphGapSize(Sizes.dluY(22));
     * builder.setParagraphGapSize(Sizes.pixel(42));
     * </pre>
     *
     * @param paragraphGapSize   the <code>ConstantSize</code> that describes
     *     the size of the gaps between paragraphs
     */
    public void setParagraphGapSize(ConstantSize paragraphGapSize) {
        RowSpec rowSpec = FormFactory.createGapRowSpec(paragraphGapSize);
        this.paragraphGapSpec = rowSpec;
    }

    /**
     * Returns the offset of the leading column, often 0 or 1.
     *
     * @return the offset of the leading column
     */
    public int getLeadingColumnOffset() {
        return leadingColumnOffset;
    }

    /**
     * Sets the offset of the leading column, often 0 or 1.
     *
     * @param columnOffset  the new offset of the leading column
     */
    public void setLeadingColumnOffset(int columnOffset) {
        this.leadingColumnOffset = columnOffset;
    }

    /**
     * Returns whether new data rows are being grouped or not.
     *
     * @return true indicates grouping enabled, false disabled
     */
    public boolean isRowGroupingEnabled() {
        return rowGroupingEnabled;
    }

    /**
     * Enables or disables the grouping of new data rows.
     *
     * @param enabled  indicates grouping enabled, false disabled
     */
    public void setRowGroupingEnabled(boolean enabled) {
        rowGroupingEnabled = enabled;
    }

    // Filling Columns ******************************************************

    /**
     * Adds a component to the panel using the default constraints
     * with a column span of 1. Then proceeds to the next data column.
     *
     * @param component        the component to add
     */
    public void append(Component component) {
        append(component, 1);
    }

    /**
     * Adds a component to the panel using the default constraints with
     * the given columnSpan. Proceeds to the next data column.
     *
     * @param component the component to append
     * @param columnSpan    the column span used to add
     */
    public void append(Component component, int columnSpan) {
        ensureCursorColumnInGrid();
        ensureHasGapRow(lineGapSpec);
        ensureHasComponentLine();

        add(component, createLeftAdjustedConstraints(columnSpan));
        nextColumn(columnSpan + 1);
    }

    /**
     * Adds two components to the panel; each component will span a single
     * data column. Proceeds to the next data column.
     *
     * @param c1    the first component to add
     * @param c2    the second component to add
     */
    public void append(Component c1, Component c2) {
        append(c1);
        append(c2);
    }

    /**
     * Adds three components to the panel; each component will span a single
     * data column. Proceeds to the next data column.
     *
     * @param c1    the first component to add
     * @param c2    the second component to add
     * @param c3    the third component to add
     */
    public void append(Component c1, Component c2, Component c3) {
        append(c1);
        append(c2);
        append(c3);
    }

    // Appending Labels with optional components ------------------------------

    /**
     * Adds a text label to the panel and proceeds to the next column.
     *
     * @param textWithMnemonic  the label's text - may mark a mnemonic
     * @return the added label
     */
    public JLabel append(String textWithMnemonic) {
        JLabel label = getComponentFactory().createLabel(textWithMnemonic);
        append(label);
        return label;
    }

    /**
     * Adds a text label and component to the panel.
     * Then proceeds to the next data column.<p>
     *
     * The created label is labelling the given component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param textWithMnemonic  the label's text - may mark a mnemonic
     * @param component         the component to add
     * @return the added label
     */
    public JLabel append(String textWithMnemonic, Component component) {
        return append(textWithMnemonic, component, 1);
    }

    /**
     * Adds a text label and component to the panel; the component will span
     * the specified number columns. Proceeds to the next data column,
     * and goes to the next line if the boolean flag is set.<p>
     *
     * The created label is labelling the given component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param textWithMnemonic  the label's text - may mark a mnemonic
     * @param c                 the component to add
     * @param nextLine          true forces a next line
     * @return the added label
     * @see JLabel#setLabelFor(java.awt.Component)
     */
    public JLabel append(String textWithMnemonic, Component c, boolean nextLine) {
        JLabel label = append(textWithMnemonic, c);
        if (nextLine) {
            nextLine();
        }
        return label;
    }

    /**
     * Adds a text label and component to the panel; the component will span
     * the specified number columns. Proceeds to the next data column.<p>
     *
     * The created label is labelling the given component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param textWithMnemonic  the label's text - may mark a mnemonic
     * @param c                 the component to add
     * @param columnSpan        number of columns the component shall span
     * @return the added label
     * @see JLabel#setLabelFor(java.awt.Component)
     */
    public JLabel append(String textWithMnemonic, Component c, int columnSpan) {
        JLabel label = append(textWithMnemonic);
        label.setLabelFor(c);
        append(c, columnSpan);
        return label;
    }

    /**
     * Adds a text label and two components to the panel; each component
     * will span a single column. Proceeds to the next data column.<p>
     *
     * The created label is labelling the first component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param textWithMnemonic  the label's text - may mark a mnemonic
     * @param c1    the first component to add
     * @param c2    the second component to add
     * @return the added label
     */
    public JLabel append(String textWithMnemonic, Component c1, Component c2) {
        JLabel label = append(textWithMnemonic, c1);
        append(c2);
        return label;
    }

    /**
     * Adds a text label and two components to the panel; each component
     * will span a single column. Proceeds to the next data column.<p>
     *
     * The created label is labelling the first component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param textWithMnemonic  the label's text - may mark a mnemonic
     * @param c1      the first component to add
     * @param c2      the second component to add
     * @param colSpan the column span for the second component
     * @return the created label
     */
    public JLabel append(String textWithMnemonic, Component c1, Component c2,
            int colSpan) {
        JLabel label = append(textWithMnemonic, c1);
        append(c2, colSpan);
        return label;
    }

    /**
     * Adds a text label and three components to the panel; each component
     * will span a single column. Proceeds to the next data column.<p>
     *
     * The created label is labelling the first component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param textWithMnemonic  the label's text - may mark a mnemonic
     * @param c1    the first component to add
     * @param c2    the second component to add
     * @param c3    the third component to add
     * @return the added label
     */
    public JLabel append(String textWithMnemonic, Component c1, Component c2,
            Component c3) {
        JLabel label = append(textWithMnemonic, c1, c2);
        append(c3);
        return label;
    }

    /**
     * Adds a text label and four components to the panel; each component
     * will span a single column. Proceeds to the next data column.<p>
     *
     * The created label is labelling the first component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param textWithMnemonic  the label's text - may mark a mnemonic
     * @param c1    the first component to add
     * @param c2    the second component to add
     * @param c3    the third component to add
     * @param c4    the fourth component to add
     * @return the added label
     */
    public JLabel append(String textWithMnemonic, Component c1, Component c2,
            Component c3, Component c4) {
        JLabel label = append(textWithMnemonic, c1, c2, c3);
        append(c4);
        return label;
    }

    // Appending internationalized labels with optional components ------------

    /**
     * Adds an internationalized (i15d) text label to the panel using
     * the given resource key and proceeds to the next column.
     *
     * @param resourceKey      the resource key for the the label's text
     * @return the added label
     */
    public JLabel appendI15d(String resourceKey) {
        return append(getI15dString(resourceKey));
    }

    /**
     * Adds an internationalized (i15d) text label and component
     * to the panel. Then proceeds to the next data column.<p>
     *
     * The created label is labelling the given component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param resourceKey  the resource key for the text to add
     * @param component  the component to add
     * @return the added label
     */
    public JLabel appendI15d(String resourceKey, Component component) {
        return append(getI15dString(resourceKey), component, 1);
    }

    /**
     * Adds an internationalized (i15d) text label and component
     * to the panel. Then proceeds to the next data column.
     * Goes to the next line if the boolean flag is set.<p>
     *
     * The created label is labelling the first component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param resourceKey  the resource key for the text to add
     * @param component    the component to add
     * @param nextLine     true forces a next line
     * @return the added label
     */
    public JLabel appendI15d(String resourceKey, Component component,
            boolean nextLine) {
        return append(getI15dString(resourceKey), component, nextLine);
    }

    /**
     * Adds an internationalized (i15d) text label to the panel using
     * the given resource key; then proceeds to the next data column
     * and adds a component with the given column span.
     * Proceeds to the next data column.<p>
     *
     * The created label is labelling the first component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param resourceKey  the resource key for the text to add
     * @param c           the component to add
     * @param columnSpan  number of columns the component shall span
     * @return the added label
     */
    public JLabel appendI15d(String resourceKey, Component c, int columnSpan) {
        return append(getI15dString(resourceKey), c, columnSpan);
    }

    /**
     * Adds an internationalized (i15d) text label and two components
     * to the panel; each component will span a single column.
     * Proceeds to the next data column.<p>
     *
     * The created label is labelling the first component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param resourceKey  the resource key for the text to add
     * @param c1    the first component to add
     * @param c2    the second component to add
     * @return the added label
     */
    public JLabel appendI15d(String resourceKey, Component c1, Component c2) {
        return append(getI15dString(resourceKey), c1, c2);
    }

    /**
     * Adds an internationalized (i15d) text label and two components
     * to the panel; each component will span a single column.
     * Proceeds to the next data column.<p>
     *
     * The created label is labelling the first component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param resourceKey  the resource key for the text to add
     * @param c1      the first component to add
     * @param c2      the second component to add
     * @param colSpan the column span for the second component
     * @return the added label
     */
    public JLabel appendI15d(String resourceKey, Component c1, Component c2,
            int colSpan) {
        return append(getI15dString(resourceKey), c1, c2, colSpan);
    }

    /**
     * Adds an internationalized (i15d) text label and three components
     * to the panel; each component will span a single column.
     * Proceeds to the next data column.<p>
     *
     * The created label is labelling the first component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param resourceKey  the resource key for the text to add
     * @param c1    the first component to add
     * @param c2    the second component to add
     * @param c3    the third component to add
     * @return the added label
     */
    public JLabel appendI15d(String resourceKey, Component c1, Component c2,
            Component c3) {
        return append(getI15dString(resourceKey), c1, c2, c3);
    }

    /**
     * Adds an internationalized (i15d) text label and four components
     * to the panel; each component will span a single column.
     * Proceeds to the next data column.<p>
     *
     * The created label is labelling the first component; so the component
     * gets the focus if the (optional) label mnemonic is pressed.
     *
     * @param resourceKey  the resource key for the text to add
     * @param c1    the first component to add
     * @param c2    the second component to add
     * @param c3    the third component to add
     * @param c4    the third component to add
     * @return the added label
     */
    public JLabel appendI15d(String resourceKey, Component c1, Component c2,
            Component c3, Component c4) {
        return append(getI15dString(resourceKey), c1, c2, c3, c4);
    }

    // Adding Titles ----------------------------------------------------------

    /**
     * Adds a title label to the panel and proceeds to the next column.
     *
     * @param textWithMnemonic  the label's text - may mark a mnemonic
     * @return the added title label
     */
    public JLabel appendTitle(String textWithMnemonic) {
        JLabel titleLabel = getComponentFactory().createTitle(textWithMnemonic);
        append(titleLabel);
        return titleLabel;
    }

    /**
     * Adds an internationalized title label to the panel and
     * proceeds to the next column.
     *
     * @param resourceKey   the resource key for the title's text
     * @return the added title label
     */
    public JLabel appendI15dTitle(String resourceKey) {
        return appendTitle(getI15dString(resourceKey));
    }

    // Appending Separators ---------------------------------------------------

    /**
     * Adds a separator without text that spans all columns.
     *
     * @return the added titled separator
     */
    public JComponent appendSeparator() {
        return appendSeparator("");
    }

    /**
     * Adds a separator with the given text that spans all columns.
     *
     * @param text      the separator title text
     * @return the added titled separator
     */
    public JComponent appendSeparator(String text) {
        ensureCursorColumnInGrid();
        ensureHasGapRow(paragraphGapSpec);
        ensureHasComponentLine();

        setColumn(super.getLeadingColumn());
        int columnSpan = getColumnCount();
        setColumnSpan(getColumnCount());
        JComponent titledSeparator = addSeparator(text);
        setColumnSpan(1);
        nextColumn(columnSpan);
        return titledSeparator;
    }

    /**
     * Appends an internationalized titled separator for
     * the given resource key that spans all columns.
     *
     * @param resourceKey   the resource key for the separator title's text
     * @return the added titled separator
     */
    public JComponent appendI15dSeparator(String resourceKey) {
        return appendSeparator(getI15dString(resourceKey));
    }

    // Overriding Superclass Behavior ***************************************

    /**
     * Returns the leading column. Unlike the superclass this method
     * honors the column offset.
     *
     * @return the leading column
     */
    @Override
    protected int getLeadingColumn() {
        int column = super.getLeadingColumn();
        return column + getLeadingColumnOffset() * getColumnIncrementSign();
    }

    // Adding Rows **********************************************************

    /**
     * Ensures that the cursor is in the grid. In case it's beyond the
     * form's right hand side, the cursor is moved to the leading column
     * of the next line.
     */
    private void ensureCursorColumnInGrid() {
        if (isLeftToRight() && getColumn() > getColumnCount()
                || !isLeftToRight() && getColumn() < 1) {
            nextLine();
        }
    }

    /**
     * Ensures that we have a gap row before the next component row.
     * Checks if the current row is the given <code>RowSpec</code>
     * and appends this row spec if necessary.
     *
     * @param gapRowSpec  the row specification to check for
     */
    private void ensureHasGapRow(RowSpec gapRowSpec) {
        if (getRow() == 1 || getRow() <= getRowCount()) {
            return;
        }

        if (getRow() <= getRowCount()) {
            RowSpec rowSpec = getCursorRowSpec();
            if (rowSpec == gapRowSpec) {
                return;
            }
        }
        appendRow(gapRowSpec);
        nextLine();
    }

    /**
     * Ensures that the form has a component row. Adds a component row
     * if the cursor is beyond the form's bottom.
     */
    private void ensureHasComponentLine() {
        if (getRow() <= getRowCount()) {
            return;
        }
        appendRow(FormFactory.PREF_ROWSPEC);
        if (isRowGroupingEnabled()) {
            getLayout().addGroupedRow(getRow());
        }
    }

    /**
     * Looks up and returns the row specification of the current row.
     *
     * @return the row specification of the current row
     */
    private RowSpec getCursorRowSpec() {
        return getLayout().getRowSpec(getRow());
    }

}
