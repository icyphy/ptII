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

package com.jgoodies.forms.layout;

import java.util.StringTokenizer;

/**
 * Specifies columns in FormLayout by their default orientation,
 * start size and resizing behavior.<p>
 *
 * <strong>Examples:</strong><br>
 * The following examples specify a column with FILL alignment, a size of
 * 10&nbsp;dlu that won't grow.
 * <pre>
 * new ColumnSpec(Sizes.dluX(10));
 * new ColumnSpec(ColumnSpec.FILL, Sizes.dluX(10), 0.0);
 * new ColumnSpec(ColumnSpec.FILL, Sizes.dluX(10), ColumnSpec.NO_GROW);
 * new ColumnSpec("10dlu");
 * new ColumnSpec("10dlu:0");
 * new ColumnSpec("fill:10dlu:0");
 * </pre><p>
 *
 * The {@link com.jgoodies.forms.factories.FormFactory} provides
 * predefined frequently used ColumnSpec instances.
 *
 * @author        Karsten Lentzsch
 * @version $Revision$
 *
 * @see     com.jgoodies.forms.factories.FormFactory
 */

@SuppressWarnings("serial")
public final class ColumnSpec extends FormSpec {

    // Horizontal Orientations *********************************************

    /**
     * By default put components in the left.
     */
    public static final DefaultAlignment LEFT = FormSpec.LEFT_ALIGN;

    /**
     * By default put the components in the center.
     */
    public static final DefaultAlignment CENTER = FormSpec.CENTER_ALIGN;

    /**
     * By default put components in the middle.
     */
    public static final DefaultAlignment MIDDLE = CENTER;

    /**
     * By default put components in the right.
     */
    public static final DefaultAlignment RIGHT = FormSpec.RIGHT_ALIGN;

    /**
     * By default fill the component into the column.
     */
    public static final DefaultAlignment FILL = FormSpec.FILL_ALIGN;

    /**
     * Unless overridden the default alignment for a column is FILL.
     */
    public static final DefaultAlignment DEFAULT = FILL;

    // Instance Creation ****************************************************

    /**
     * Constructs a ColumnSpec for the given default alignment,
     * size and resize weight.<p>
     *
     * The resize weight must be a non-negative double; you can use
     * <code>NO_GROW</code> as a convenience value for no resize.
     *
     * @param defaultAlignment the column's default alignment
     * @param size             constant, component size or bounded size
     * @param resizeWeight     the column's non-negative resize weight
     * @exception IllegalArgumentException if the size is invalid or
     *      the resize weight is negative
     */
    public ColumnSpec(DefaultAlignment defaultAlignment, Size size,
            double resizeWeight) {
        super(defaultAlignment, size, resizeWeight);
    }

    /**
     * Constructs a ColumnSpec for the given size using the
     * default alignment, and no resizing.
     *
     * @param size             constant size, component size, or bounded size
     * @exception IllegalArgumentException if the size is invalid
     */
    public ColumnSpec(Size size) {
        super(DEFAULT, size, NO_GROW);
    }

    /**
     * Constructs a ColumnSpec from the specified encoded description.
     * The description will be parsed to set initial values.
     *
     * @param encodedDescription        the encoded description
     */
    public ColumnSpec(String encodedDescription) {
        super(DEFAULT, encodedDescription);
    }

    // Implementing Abstract Behavior ***************************************

    /**
     * Returns if this is a horizontal specification (vs. vertical).
     * Used to distinct between horizontal and vertical dialog units,
     * which have different conversion factors.
     *
     * @return  always true (for horizontal)
     */
    protected final boolean isHorizontal() {
        return true;
    }

    // Parsing and Decoding of Column Descriptions **************************

    /**
     * Parses and splits encoded column specifications and returns
     * an array of ColumnSpec objects.
     *
     * @param encodedColumnSpecs  comma separated encoded column specifications
     * @return an array of decoded column specifications
     * @exception NullPointerException if the encoded column specifications string
     *     is <code>null</code>
     *
     * @see ColumnSpec#ColumnSpec(String)
     */
    public static ColumnSpec[] decodeSpecs(String encodedColumnSpecs) {
        if (encodedColumnSpecs == null) {
            throw new NullPointerException(
                    "The column specification must not be null.");
        }

        StringTokenizer tokenizer = new StringTokenizer(encodedColumnSpecs,
                ", ");
        int columnCount = tokenizer.countTokens();
        ColumnSpec[] columnSpecs = new ColumnSpec[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnSpecs[i] = new ColumnSpec(tokenizer.nextToken());
        }
        return columnSpecs;
    }

}
