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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.util.LayoutStyle;

/**
 * A non-visual builder that assists you in building consistent button bars
 * that comply with popular UI style guides. It utilizes the {@link FormLayout}.
 * This class is in turn used by the
 * {@link com.jgoodies.forms.factories.ButtonBarFactory} that provides
 * an even higher level of abstraction for building consistent button bars.<p>
 *
 * Buttons added to the builder are either gridded or fixed and may fill
 * their FormLayout cell or not. All gridded buttons get the same width,
 * while fixed buttons use their own size. Gridded buttons honor
 * the default minimum button width as specified by the current
 * {@link com.jgoodies.forms.util.LayoutStyle}.<p>
 *
 * You can set an optional hint for narrow  margin for the fixed width buttons.
 * This is useful if you want to lay out a button bar that includes a button
 * with a long text. For example, in a bar with
 * 'Copy to Clipboard', 'OK', 'Cancel' you may declare the clipboard button
 * as a fixed size button with narrow margins, OK and Cancel as gridded.
 * Gridded buttons are marked as narrow by default.
 * Note that some look&amp;feels do not support the narrow margin feature,
 * and conversely, others have only narrow margins. The JGoodies look&amp;feels
 * honor the setting, the Mac Aqua l&amp;f uses narrow margins all the time.<p>
 *
 * To honor the platform's button order (left-to-right vs. right-to-left)
 * this builder uses the <em>leftToRightButtonOrder</em> property.
 * It is initialized with the current LayoutStyle's button order,
 * which in turn is left-to-right on most platforms and right-to-left
 * on the Mac OS X. Builder methods that create sequences of buttons
 * (e.g. {@link #addGriddedButtons(JButton[])} honor the button order.
 * If you want to ignore the default button order, you can either
 * add add individual buttons, or create a ButtonBarBuilder instance
 * with the order set to left-to-right. For the latter see
 * {@link #createLeftToRightBuilder()}. Also see the button order
 * example below.<p>
 *
 * <strong>Example:</strong><br>
 * The following example builds a button bar with <i>Help</i> button on the
 * left-hand side and <i>OK, Cancel, Apply</i> buttons on the right-hand side.
 * <pre>
 * private JPanel createHelpOKCancelApplyBar(
 *         JButton help, JButton ok, JButton cancel, JButton apply) {
 *     ButtonBarBuilder builder = new ButtonBarBuilder();
 *     builder.addGridded(help);
 *     builder.addRelatedGap();
 *     builder.addGlue();
 *     builder.addGriddedButtons(new JButton[]{ok, cancel, apply});
 *     return builder.getPanel();
 * }
 * </pre><p>
 *
 * <strong>Button Order Example:</strong><br>
 * The following example builds three button bars where one honors
 * the platform's button order and the other two ignore it.
 * <pre>
 * public JComponent buildPanel() {
 *     FormLayout layout = new FormLayout("pref");
 *     DefaultFormBuilder rowBuilder = new DefaultFormBuilder(layout);
 *     rowBuilder.setDefaultDialogBorder();
 *
 *     rowBuilder.append(buildButtonSequence(new ButtonBarBuilder()));
 *     rowBuilder.append(buildButtonSequence(ButtonBarBuilder.createLeftToRightBuilder()));
 *     rowBuilder.append(buildIndividualButtons(new ButtonBarBuilder()));
 *
 *     return rowBuilder.getPanel();
 * }
 *
 * private Component buildButtonSequence(ButtonBarBuilder builder) {
 *     builder.addGriddedButtons(new JButton[] {
 *             new JButton("One"),
 *             new JButton("Two"),
 *             new JButton("Three")
 *     });
 *     return builder.getPanel();
 * }
 *
 * private Component buildIndividualButtons(ButtonBarBuilder builder) {
 *     builder.addGridded(new JButton("One"));
 *     builder.addRelatedGap();
 *     builder.addGridded(new JButton("Two"));
 *     builder.addRelatedGap();
 *     builder.addGridded(new JButton("Three"));
 *     return builder.getPanel();
 * }
 * </pre>
 *
 * @author        Karsten Lentzsch
 * @version $Revision$
 *
 * @see ButtonStackBuilder
 * @see com.jgoodies.forms.factories.ButtonBarFactory
 * @see com.jgoodies.forms.util.LayoutStyle
 */
public final class ButtonBarBuilder extends PanelBuilder {

    /**
     * Specifies the columns of the initial FormLayout used in constructors.
     */
    private static final ColumnSpec[] COL_SPECS = new ColumnSpec[] {};

    /**
     * Specifies the FormLayout's the single button bar row.
     */
    private static final RowSpec[] ROW_SPECS = new RowSpec[] {
            new RowSpec("center:pref") };

    /**
     * The client property key used to indicate that a button shall
     * get narrow margins on the left and right hand side.<p>
     *
     * This optional setting will be honored by all JGoodies Look&amp;Feel
     * implementations. The Mac Aqua l&amp;f uses narrow margins only.
     * Other look&amp;feel implementations will likely ignore this key
     * and so may render a wider button margin.
     */
    private static final String NARROW_KEY = "jgoodies.isNarrow";

    /**
     * Describes how sequences of buttons are added to the button bar:
     * left-to-right or right-to-left. This setting is initialized using
     * the current {@link LayoutStyle}'s button order. It is honored
     * only by builder methods that build sequences of button, for example
     * {@link #addGriddedButtons(JButton[])}, and ignored if you add
     * individual button, for example using {@link #addGridded(JComponent)}.
     *
     * @see #isLeftToRight()
     * @see #setLeftToRight(boolean)
     * @see #addGriddedButtons(JButton[])
     * @see #addGriddedGrowingButtons(JButton[])
     */
    private boolean leftToRight;

    // Instance Creation ****************************************************

    /**
     * Constructs an instance of <code>ButtonBarBuilder</code> on a
     * <code>JPanel</code> using a preconfigured FormLayout as layout manager.
     */
    public ButtonBarBuilder() {
        this(new JPanel(null));
    }

    /**
     * Constructs an instance of <code>ButtonBarBuilder</code> on the given
     * panel using a preconfigured FormLayout as layout manager.
     *
     * @param panel  the layout container
     */
    public ButtonBarBuilder(JPanel panel) {
        super(new FormLayout(COL_SPECS, ROW_SPECS), panel);
        leftToRight = LayoutStyle.getCurrent().isLeftToRightButtonOrder();
    }

    /**
     * Creates and returns a <code>ButtonBarBuilder</code> with
     * initialized with a left to right button order.
     *
     * @return a button bar builder with button order set to left-to-right
     */
    public static ButtonBarBuilder createLeftToRightBuilder() {
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.setLeftToRightButtonOrder(true);
        return builder;
    }

    // Accessing Properties *************************************************

    /**
     * Returns whether button sequences will be ordered from
     * left to right or from right to left.
     *
     * @return true if button sequences are ordered from left to right
     * @since 1.0.3
     *
     * @see LayoutStyle#isLeftToRightButtonOrder()
     */
    public boolean isLeftToRightButtonOrder() {
        return leftToRight;
    }

    /**
     * Sets the order for button sequences to either left to right,
     * or right to left.
     *
     * @param newButtonOrder  true if button sequences shall be ordered
     *     from left to right
     * @since 1.0.3
     *
     * @see LayoutStyle#isLeftToRightButtonOrder()
     */
    public void setLeftToRightButtonOrder(boolean newButtonOrder) {
        leftToRight = newButtonOrder;
    }

    // Default Borders ******************************************************

    /**
     * Sets a default border that has a gap in the bar's north.
     */
    public void setDefaultButtonBarGapBorder() {
        getPanel().setBorder(Borders.BUTTON_BAR_GAP_BORDER);
    }

    // Adding Components ****************************************************

    /**
     * Adds a sequence of related gridded buttons each separated by
     * a default gap. Honors this builder's button order. If you
     * want to use a fixed left to right order, add individual buttons.
     *
     * @param buttons  an array of buttons to add
     *
     * @see LayoutStyle
     */
    public void addGriddedButtons(JButton[] buttons) {
        int length = buttons.length;
        for (int i = 0; i < length; i++) {
            int index = leftToRight ? i : length - 1 - i;
            addGridded(buttons[index]);
            if (i < buttons.length - 1) {
                addRelatedGap();
            }
        }
    }

    /**
     * Adds a sequence of gridded buttons that grow
     * where each is separated by a default gap.
     * Honors this builder's button order. If you
     * want to use a fixed left to right order,
     * add individual buttons.
     *
     * @param buttons  an array of buttons to add
     *
     * @see LayoutStyle
     */
    public void addGriddedGrowingButtons(JButton[] buttons) {
        int length = buttons.length;
        for (int i = 0; i < length; i++) {
            int index = leftToRight ? i : length - 1 - i;
            addGriddedGrowing(buttons[index]);
            if (i < buttons.length - 1) {
                addRelatedGap();
            }
        }
    }

    /**
     * Adds a fixed size component. Unlike the gridded components,
     * this component keeps its individual preferred dimension.
     *
     * @param component  the component to add
     */
    public void addFixed(JComponent component) {
        getLayout().appendColumn(FormFactory.PREF_COLSPEC);
        add(component);
        nextColumn();
    }

    /**
     * Adds a fixed size component with narrow margins. Unlike the gridded
     * components, this component keeps its individual preferred dimension.
     *
     * @param component  the component to add
     */
    public void addFixedNarrow(JComponent component) {
        component.putClientProperty(NARROW_KEY, Boolean.TRUE);
        addFixed(component);
    }

    /**
     * Adds a gridded component, i.e. a component that will get
     * the same dimension as all other gridded components.
     *
     * @param component  the component to add
     */
    public void addGridded(JComponent component) {
        getLayout().appendColumn(FormFactory.BUTTON_COLSPEC);
        getLayout().addGroupedColumn(getColumn());
        component.putClientProperty(NARROW_KEY, Boolean.TRUE);
        add(component);
        nextColumn();
    }

    /**
     * Adds a gridded component that grows. The component's initial size
     * (before it grows) is the same as for all other gridded components.
     *
     * @param component  the component to add
     */
    public void addGriddedGrowing(JComponent component) {
        getLayout().appendColumn(FormFactory.GROWING_BUTTON_COLSPEC);
        getLayout().addGroupedColumn(getColumn());
        component.putClientProperty(NARROW_KEY, Boolean.TRUE);
        add(component);
        nextColumn();
    }

    /**
     * Adds a glue that will be given the extra space,
     * if this box is larger than its preferred size.
     */
    public void addGlue() {
        appendGlueColumn();
        nextColumn();
    }

    /**
     * Adds the standard gap for related components.
     */
    public void addRelatedGap() {
        appendRelatedComponentsGapColumn();
        nextColumn();
    }

    /**
     * Adds the standard gap for unrelated components.
     */
    public void addUnrelatedGap() {
        appendUnrelatedComponentsGapColumn();
        nextColumn();
    }

    /**
     * Adds a strut of a specified size.
     *
     * @param size  a <code>ConstantSize</code> that describes the gap's size
     */
    public void addStrut(ConstantSize size) {
        getLayout().appendColumn(
                new ColumnSpec(ColumnSpec.LEFT, size, FormSpec.NO_GROW));
        nextColumn();
    }

}
