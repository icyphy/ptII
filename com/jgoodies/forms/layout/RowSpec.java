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
 * Specifies rows in FormLayout by their default orientation,
 * start size and resizing behavior.<p>
 *
 * <strong>Examples:</strong><br>
 * The following examples specify a centered row with a size of 14&nbsp;dlu
 * that won't grow.
 * <pre>
 * new RowSpec(Sizes.dluX(14));
 * new RowSpec(RowSpec.CENTER, Sizes.dluX(14), 0.0);
 * new RowSpec(rowSpec.CENTER, Sizes.dluX(14), RowSpec.NO_GROW);
 * new RowSpec("14dlu");
 * new RowSpec("14dlu:0");
 * new RowSpec("center:14dlu:0");
 * </pre><p>
 *
 * The {@link com.jgoodies.forms.factories.FormFactory} provides
 * predefined frequently used RowSpec instances.
 *
 * @author        Karsten Lentzsch
 * @version $Revision$
 *
 * @see     com.jgoodies.forms.factories.FormFactory
 */

@SuppressWarnings("serial")
public final class RowSpec extends FormSpec {

    // Vertical Orientations ************************************************

    /**
     * By default put the components in the top.
     */
    public static final DefaultAlignment TOP = FormSpec.TOP_ALIGN;

    /**
     * By default put the components in the center.
     */
    public static final DefaultAlignment CENTER = FormSpec.CENTER_ALIGN;

    /**
     * By default put the components in the bottom.
     */
    public static final DefaultAlignment BOTTOM = FormSpec.BOTTOM_ALIGN;

    /**
     * By default fill the component into the row.
     */
    public static final DefaultAlignment FILL = FormSpec.FILL_ALIGN;

    /**
     * Unless overridden the default alignment for a row is CENTER.
     */
    public static final DefaultAlignment DEFAULT = CENTER;

    // Instance Creation ****************************************************

    /**
     * Constructs a RowSpec from the given default orientation,
     * size, and resize weight.<p>
     *
     * The resize weight must be a non-negative double; you can use
     * <code>NO_FILL</code> as a convenience value for no resize.
     *
     * @param defaultAlignment  the row's default alignment
     * @param size              constant size, component size, or bounded size
     * @param resizeWeight      the row's non-negative resize weight
     * @exception IllegalArgumentException if the size is invalid or
     *      the resize weight is negative
     */
    public RowSpec(DefaultAlignment defaultAlignment, Size size,
            double resizeWeight) {
        super(defaultAlignment, size, resizeWeight);
    }

    /**
     * Constructs a RowSpec for the given size using the
     * default alignment, and no resizing.
     *
     * @param size             constant size, component size, or bounded size
     * @exception IllegalArgumentException if the size is invalid
     */
    public RowSpec(Size size) {
        super(DEFAULT, size, NO_GROW);
    }

    /**
     * Constructs a RowSpec from the specified encoded
     * description. The description will be parsed to set initial values.
     *
     * @param encodedDescription        the encoded description
     */
    public RowSpec(String encodedDescription) {
        super(DEFAULT, encodedDescription);
    }

    // Implementing Abstract Behavior ***************************************

    /**
     * Returns if this is a horizontal specification (vs. vertical).
     * Used to distinct between horizontal and vertical dialog units,
     * which have different conversion factors.
     *
     * @return true for horizontal, false for vertical
     */
    protected boolean isHorizontal() {
        return false;
    }

    // Parsing and Decoding of Row Descriptions *****************************

    /**
     * Parses and splits encoded row specifications and returns
     * an array of RowSpec objects.
     *
     * @param encodedRowSpecs     comma separated encoded row specifications
     * @return an array of decoded row specifications
     * @exception NullPointerException if the encoded row specifications string
     *     is <code>null</code>
     *
     * @see RowSpec#RowSpec(String)
     */
    public static RowSpec[] decodeSpecs(String encodedRowSpecs) {
        if (encodedRowSpecs == null) {
            throw new NullPointerException(
                    "The row specification must not be null.");
        }

        StringTokenizer tokenizer = new StringTokenizer(encodedRowSpecs, ", ");
        int rowCount = tokenizer.countTokens();
        RowSpec[] rowSpecs = new RowSpec[rowCount];
        for (int i = 0; i < rowCount; i++) {
            rowSpecs[i] = new RowSpec(tokenizer.nextToken());
        }
        return rowSpecs;
    }

}
