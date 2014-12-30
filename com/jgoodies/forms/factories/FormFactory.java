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

package com.jgoodies.forms.factories;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.util.LayoutStyle;

/**
 * A factory that creates instances of FormLayout for frequently used
 * form layouts. It makes form creation easier and more consistent.<p>
 *
 * <p><strong>I consider removing the <code>FormLayout</code> factory methods.
 * These seem to be less useful and may lead to poor layout readability.
 * If you want to use these methods, you may consider copying them
 * to your codebase.</strong></p>
 *
 * <p>The forms are described by major and minor columns. Major columns
 * consist of a leading label and a set of related components, for example: a
 * label plus textfield, or label plus textfield plus button. The component
 * part of a major column is divided into minor columns as shown in this
 * layout:</p>
 * <pre>
 * &lt;-    major column 1        -&gt;  &lt;-     major column 2       -&gt;
 * label1 textfield1a textfield1b  label2 textfield2a textfield2b
 * label3 textfield3a textfield3b  label4 textfield4
 * label5 textfield5               label6 textfield6
 * </pre>
 *
 * <p>Many forms use 1, 2, 3 or 4 major columns, which in turn are often split
 * into 1, 2, 3 or 4 minor columns.</p>
 *
 * @author        Karsten Lentzsch
 * @version $Revision$
 *
 * @see        com.jgoodies.forms.layout.FormLayout
 * @see        ColumnSpec
 */
public final class FormFactory {

    private FormFactory() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    // Frequently used Column Specifications ********************************

    /**
     * An unmodifyable <code>ColumnSpec</code> that determines its width by
     * computing the maximum of all column component minimum widths.
     *
     * @see #PREF_COLSPEC
     * @see #DEFAULT_COLSPEC
     */
    public static final ColumnSpec MIN_COLSPEC = new ColumnSpec(Sizes.MINIMUM);

    /**
     * An unmodifyable <code>ColumnSpec</code> that determines its width by
     * computing the maximum of all column component preferred widths.
     *
     * @see #MIN_COLSPEC
     * @see #DEFAULT_COLSPEC
     */
    public static final ColumnSpec PREF_COLSPEC = new ColumnSpec(
            Sizes.PREFERRED);

    /**
     * An unmodifyable <code>ColumnSpec</code> that determines its preferred
     * width by computing the maximum of all column component preferred widths
     * and its minimum width by computing all column component minimum widths.<p>
     *
     * Useful to let a column shrink from preferred width to minimum width
     * if the container space gets scarce.
     *
     * @see #MIN_COLSPEC
     * @see #PREF_COLSPEC
     */
    public static final ColumnSpec DEFAULT_COLSPEC = new ColumnSpec(
            Sizes.DEFAULT);

    /**
     * An unmodifyable <code>ColumnSpec</code> that has an initial width
     * of 0 pixels and that grows. Useful to describe <em>glue</em> columns
     * that fill the space between other columns.
     *
     * @see #GLUE_ROWSPEC
     */
    public static final ColumnSpec GLUE_COLSPEC = new ColumnSpec(
            ColumnSpec.DEFAULT, Sizes.ZERO, FormSpec.DEFAULT_GROW);

    // Layout Style Dependent Column Specs ***********************************

    /**
     * Describes a logical horizontal gap between a label and an associated
     * component. Useful for builders that automatically fill a grid with labels
     * and components.<p>
     *
     * <strong>Note:</strong> In a future version this constant will likely
     * be moved to a class <code>LogicalSize</code> or <code>StyledSize</code>.
     *
     * @since 1.0.3
     */
    public static final ColumnSpec LABEL_COMPONENT_GAP_COLSPEC = createGapColumnSpec(LayoutStyle
            .getCurrent().getLabelComponentPadX());

    /**
     * Describes a logical horizontal gap between two related components.
     * For example the <em>OK</em> and <em>Cancel</em> buttons are considered
     * related.<p>
     *
     * <strong>Note:</strong> In a future version this constant will likely
     * be moved to a class <code>LogicalSize</code> or <code>StyledSize</code>.
     *
     * @see #UNRELATED_GAP_COLSPEC
     */
    public static final ColumnSpec RELATED_GAP_COLSPEC = createGapColumnSpec(LayoutStyle
            .getCurrent().getRelatedComponentsPadX());

    /**
     * Describes a logical horizontal gap between two unrelated components.<p>
     *
     * <strong>Note:</strong> In a future version this constant will likely
     * be moved to a class <code>LogicalSize</code> or <code>StyledSize</code>.
     *
     * @see #RELATED_GAP_COLSPEC
     */
    public static final ColumnSpec UNRELATED_GAP_COLSPEC = createGapColumnSpec(LayoutStyle
            .getCurrent().getUnrelatedComponentsPadX());

    /**
     * Describes a logical horizontal column for a fixed size button. This spec
     * honors the current layout style's default button minimum width.<p>
     *
     * <strong>Note:</strong> In a future version this constant will likely
     * be moved to a class <code>LogicalSize</code> or <code>StyledSize</code>.
     *
     * @see #GROWING_BUTTON_COLSPEC
     */
    public static final ColumnSpec BUTTON_COLSPEC = new ColumnSpec(
            Sizes.bounded(Sizes.PREFERRED, LayoutStyle.getCurrent()
                    .getDefaultButtonWidth(), null));

    /**
     * Describes a logical horizontal column for a growing button. This spec
     * does <em>not</em> use the layout style's default button minimum width.<p>
     *
     * <strong>Note:</strong> In a future version this constant will likely
     * be moved to a class <code>LogicalSize</code> or <code>StyledSize</code>.
     *
     * @see #BUTTON_COLSPEC
     */
    public static final ColumnSpec GROWING_BUTTON_COLSPEC = new ColumnSpec(
            ColumnSpec.DEFAULT, BUTTON_COLSPEC.getSize(), FormSpec.DEFAULT_GROW);

    // Frequently used Row Specifications ***********************************

    /**
     * An unmodifyable <code>RowSpec</code> that determines its height by
     * computing the maximum of all column component minimum heights.
     *
     * @see #PREF_ROWSPEC
     * @see #DEFAULT_ROWSPEC
     */
    public static final RowSpec MIN_ROWSPEC = new RowSpec(Sizes.MINIMUM);

    /**
     * An unmodifyable <code>RowSpec</code> that determines its height by
     * computing the maximum of all column component preferred heights.
     *
     * @see #MIN_ROWSPEC
     * @see #DEFAULT_ROWSPEC
     */
    public static final RowSpec PREF_ROWSPEC = new RowSpec(Sizes.PREFERRED);

    /**
     * An unmodifyable <code>RowSpec</code> that determines its preferred
     * height by computing the maximum of all column component preferred heights
     * and its minimum height by computing all column component minimum heights.<p>
     *
     * Useful to let a column shrink from preferred height to minimum height
     * if the container space gets scarce.
     *
     * @see #MIN_COLSPEC
     * @see #PREF_COLSPEC
     */
    public static final RowSpec DEFAULT_ROWSPEC = new RowSpec(Sizes.DEFAULT);

    /**
     * An unmodifyable <code>RowSpec</code> that has an initial height
     * of 0 pixels and that grows. Useful to describe <em>glue</em> rows
     * that fill the space between other rows.
     *
     * @see #GLUE_COLSPEC
     */
    public static final RowSpec GLUE_ROWSPEC = new RowSpec(RowSpec.DEFAULT,
            Sizes.ZERO, FormSpec.DEFAULT_GROW);

    // Layout Style Dependent Row Specs *************************************

    /**
     * Describes a logical vertical gap between two related components.
     * For example the <em>OK</em> and <em>Cancel</em> buttons are considered
     * related.<p>
     *
     * <strong>Note:</strong> In a future version this constant will likely
     * be moved to a class <code>LogicalSize</code> or <code>StyledSize</code>.
     *
     * @see #UNRELATED_GAP_ROWSPEC
     */
    public static final RowSpec RELATED_GAP_ROWSPEC = createGapRowSpec(LayoutStyle
            .getCurrent().getRelatedComponentsPadY());

    /**
     * Describes a logical vertical gap between two unrelated components.<p>
     *
     * <strong>Note:</strong> In a future version this constant will likely
     * be moved to a class <code>LogicalSize</code> or <code>StyledSize</code>.
     *
     * @see #RELATED_GAP_ROWSPEC
     */
    public static final RowSpec UNRELATED_GAP_ROWSPEC = createGapRowSpec(LayoutStyle
            .getCurrent().getUnrelatedComponentsPadY());

    /**
     * Describes a logical vertical narrow gap between two rows in the grid.
     * Useful if the vertical space is scarce or if an individual vertical gap
     * shall be small than the default line gap.<p>
     *
     * <strong>Note:</strong> In a future version this constant will likely
     * be moved to a class <code>LogicalSize</code> or <code>StyledSize</code>.
     *
     * @see #LINE_GAP_ROWSPEC
     * @see #PARAGRAPH_GAP_ROWSPEC
     */
    public static final RowSpec NARROW_LINE_GAP_ROWSPEC = createGapRowSpec(LayoutStyle
            .getCurrent().getNarrowLinePad());

    /**
     * Describes the logical vertical default gap between two rows in the grid.
     * A little bit larger than the narrow line gap.<p>
     *
     * <strong>Note:</strong> In a future version this constant will likely
     * be moved to a class <code>LogicalSize</code> or <code>StyledSize</code>.
     *
     * @see #NARROW_LINE_GAP_ROWSPEC
     * @see #PARAGRAPH_GAP_ROWSPEC
     */
    public static final RowSpec LINE_GAP_ROWSPEC = createGapRowSpec(LayoutStyle
            .getCurrent().getLinePad());

    /**
     * Describes the logical vertical default gap between two paragraphs in
     * the layout grid. This gap is larger than the default line gap.<p>
     *
     * <strong>Note:</strong> In a future version this constant will likely
     * be moved to a class <code>LogicalSize</code> or <code>StyledSize</code>.
     *
     * @see #NARROW_LINE_GAP_ROWSPEC
     * @see #LINE_GAP_ROWSPEC
     */
    public static final RowSpec PARAGRAPH_GAP_ROWSPEC = createGapRowSpec(LayoutStyle
            .getCurrent().getParagraphPad());

    // Factory Methods ******************************************************

    /**
     * Creates and returns an instance of <code>FormLayout</code>
     * to build forms with the specified number of major and minor columns.<p>
     *
     * The layout will use default values for all gaps.<p>
     *
     * <strong>This method may be removed from a future version.</strong>
     *
     * @param majorColumns     the number of used major columns
     * @param minorColumns     the number of used minor columns
     * @param labelColumnSpec  specifies the label columns
     * @return a prepared <code>FormLayout</code>
     */
    public static FormLayout createColumnLayout(int majorColumns,
            int minorColumns, ColumnSpec labelColumnSpec) {
        return createColumnLayout(majorColumns, minorColumns, labelColumnSpec,
                Sizes.DLUX14, Sizes.DLUX2);
    }

    /**
     * Creates and returns an instance of <code>FormLayout</code>
     * to build forms with the given number of major columns.
     * Major columns consists of a label and a component section, where each
     * component section is divided into the given number of minor columns.<p>
     *
     * The layout will use the specified gaps to separate major columns,
     * and the label and component section.<p>
     *
     * <strong>This method may be removed from a future version.</strong>
     *
     * @param majorColumns         the number of major columns
     * @param minorColumns         the number of minor columns
     * @param labelColumnSpec      specifies the label columns
     * @param indent               an optional <code>ConstantSize</code>
     * that describes the width of the leading indent column
     * @param minorColumnGap       a <code>ConstantSize</code> that describes
     * the gap between minor columns
     * @return a prepared <code>FormLayout</code>
     */
    public static FormLayout createColumnLayout(int majorColumns,
            int minorColumns, ColumnSpec labelColumnSpec, ConstantSize indent,
            ConstantSize minorColumnGap) {
        return createColumnLayout(majorColumns, minorColumns, labelColumnSpec,
                PREF_COLSPEC, indent, Sizes.DLUX14, minorColumnGap);
    }

    /**
     * Creates and returns an instance of <code>FormLayout</code>
     * to build forms with the given number of major columns.
     * Major columns consists of a label and a component section, where each
     * component section is divided into the given number of minor columns.<p>
     *
     * The layout will use the specified gaps to separate major columns,
     * minor columns, and the label and component section.<p>
     *
     * <strong>This method may be removed from a future version.</strong>
     *
     * @param majorColumns         the number of major columns
     * @param minorColumns         the number of minor columns
     * @param labelColumnSpec      specifies the label columns
     * @param componentColumnSpec  specifies the label columns
     * @param indent               an optional <code>ConstantSize</code>
     * that describes the width of the leading indent column
     * @param majorColumnGap       a <code>ConstantSize</code> that describes
     * the gap between major columns
     * @param minorColumnGap       a <code>ConstantSize</code> that describes
     * the gap between minor columns
     * @return a prepared <code>FormLayout</code>
     */
    public static FormLayout createColumnLayout(int majorColumns,
            int minorColumns, ColumnSpec labelColumnSpec,
            ColumnSpec componentColumnSpec, ConstantSize indent,
            ConstantSize majorColumnGap, ConstantSize minorColumnGap) {

        ColumnSpec majorGapColSpec = createGapColumnSpec(majorColumnGap);
        ColumnSpec minorGapColSpec = createGapColumnSpec(minorColumnGap);
        FormLayout layout = new FormLayout(new ColumnSpec[] {},
                new RowSpec[] {});

        // Add the optional leading indent.
        if (indent != null) {
            layout.appendColumn(createGapColumnSpec(indent));
        }
        for (int i = 0; i < majorColumns; i++) {
            // Add the optional label column with gap.
            if (labelColumnSpec != null) {
                layout.appendColumn(labelColumnSpec);
                layout.appendColumn(RELATED_GAP_COLSPEC);
            }
            // Add the minor columns with separating gaps.
            for (int j = 0; j < minorColumns; j++) {
                layout.appendColumn(componentColumnSpec);
                layout.addGroupedColumn(layout.getColumnCount());
                if (j < minorColumns - 1) {
                    layout.appendColumn(minorGapColSpec);
                }
            }
            // Add a gap between major columns.
            if (i < majorColumns - 1) {
                layout.appendColumn(majorGapColSpec);
            }
        }
        return layout;
    }

    // Helper Code **********************************************************

    /**
     * Creates and returns a {@link ColumnSpec} that represents a gap with the
     * specified {@link ConstantSize}.
     *
     * @param gapSize        a <code>ConstantSize</code> that specifies the gap
     * @return a <code>ColumnSpec</code> that describes a horizontal gap
     */
    public static ColumnSpec createGapColumnSpec(ConstantSize gapSize) {
        return new ColumnSpec(ColumnSpec.LEFT, gapSize, FormSpec.NO_GROW);
    }

    /**
     * Creates and returns a {@link RowSpec} that represents a gap with the
     * specified {@link ConstantSize}.
     *
     * @param gapSize   a <code>ConstantSize</code> that specifies the gap
     * @return a <code>RowSpec</code> that describes a vertical gap
     */
    public static RowSpec createGapRowSpec(ConstantSize gapSize) {
        return new RowSpec(RowSpec.TOP, gapSize, FormSpec.NO_GROW);
    }

}
