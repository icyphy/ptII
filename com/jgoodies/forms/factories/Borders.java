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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.StringTokenizer;

import javax.swing.border.Border;

import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.util.LayoutStyle;

/**
 * Provides constants and factory methods for <code>Border</code>s that use
 * instances of {@link ConstantSize} to define the margins.<p>
 *
 * <strong>Examples:</strong><br>
 * <pre>
 * Borders.DLU2_BORDER
 * Borders.createEmptyBorder(Sizes.DLUY4, Sizes.DLUX2, Sizes.DLUY4, Sizes.DLUX2);
 * Borders.createEmptyBorder("4dlu, 2dlu, 4dlu, 2dlu");
 * </pre>
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 *
 * @see     Border
 * @see     Sizes
 */
public final class Borders {

    private Borders() {
        // Overrides default constructor; prevents instantiation.
    }

    // Constant Borders *****************************************************

    /**
     * A prepared and reusable EmptyBorder without gaps.
     */
    public static final Border EMPTY_BORDER = new javax.swing.border.EmptyBorder(
            0, 0, 0, 0);

    /**
     * A prepared and reusable Border with 2dlu on all sides.
     */
    public static final Border DLU2_BORDER = createEmptyBorder(Sizes.DLUY2,
            Sizes.DLUX2, Sizes.DLUY2, Sizes.DLUX2);

    /**
     * A prepared and reusable Border with 4dlu on all sides.
     */
    public static final Border DLU4_BORDER = createEmptyBorder(Sizes.DLUY4,
            Sizes.DLUX4, Sizes.DLUY4, Sizes.DLUX4);

    /**
     * A prepared and reusable Border with 7dlu on all sides.
     */
    public static final Border DLU7_BORDER = createEmptyBorder(Sizes.DLUY7,
            Sizes.DLUX7, Sizes.DLUY7, Sizes.DLUX7);

    /**
     * A prepared Border with 14dlu on all sides.
     */
    public static final Border DLU14_BORDER = createEmptyBorder(Sizes.DLUY14,
            Sizes.DLUX14, Sizes.DLUY14, Sizes.DLUX14);

    /**
     * A standardized Border that describes the gap between a component
     * and a button bar in its bottom.
     */
    public static final Border BUTTON_BAR_GAP_BORDER = createEmptyBorder(
            LayoutStyle.getCurrent().getButtonBarPad(), Sizes.dluX(0),
            Sizes.dluY(0), Sizes.dluX(0));

    /**
     * A standardized Border that describes the border around
     * a dialog content that has no tabs.
     *
     * @see #TABBED_DIALOG_BORDER
     */
    public static final Border DIALOG_BORDER = createEmptyBorder(LayoutStyle
            .getCurrent().getDialogMarginY(), LayoutStyle.getCurrent()
            .getDialogMarginX(), LayoutStyle.getCurrent().getDialogMarginY(),
            LayoutStyle.getCurrent().getDialogMarginX());

    /**
     * A standardized Border that describes the border around
     * a dialog content that uses tabs.
     *
     * @see #DIALOG_BORDER
     */
    public static final Border TABBED_DIALOG_BORDER = createEmptyBorder(
            LayoutStyle.getCurrent().getTabbedDialogMarginY(), LayoutStyle
            .getCurrent().getTabbedDialogMarginX(), LayoutStyle
            .getCurrent().getTabbedDialogMarginY(), LayoutStyle
            .getCurrent().getTabbedDialogMarginX());

    // Factory Methods ******************************************************

    /**
     * Creates and returns an <code>EmptyBorder</code> with the specified
     * gaps.
     *
     * @param top                the top gap
     * @param left                the left-hand side gap
     * @param bottom        the bottom gap
     * @param right        the right-hand side gap
     * @return an <code>EmptyBorder</code> with the specified gaps
     *
     * @see #createEmptyBorder(String)
     */
    public static Border createEmptyBorder(ConstantSize top, ConstantSize left,
            ConstantSize bottom, ConstantSize right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    /**
     * Creates and returns a <code>Border</code> using sizes as specified by
     * the given string. This string is a comma-separated encoding of
     * 4 <code>ConstantSize</code>s.
     *
     * @param encodedSizes         top, left, bottom, right gap encoded as String
     * @return an <code>EmptyBorder</code> with the specified gaps
     *
     * @see #createEmptyBorder(ConstantSize, ConstantSize, ConstantSize, ConstantSize)
     */
    public static Border createEmptyBorder(String encodedSizes) {
        StringTokenizer tokenizer = new StringTokenizer(encodedSizes, ", ");
        int tokenCount = tokenizer.countTokens();
        if (tokenCount != 4) {
            throw new IllegalArgumentException(
                    "The border requires 4 sizes, but '" + encodedSizes
                    + "' has " + tokenCount + ".");
        }
        ConstantSize top = Sizes.constant(tokenizer.nextToken(), false);
        ConstantSize left = Sizes.constant(tokenizer.nextToken(), true);
        ConstantSize bottom = Sizes.constant(tokenizer.nextToken(), false);
        ConstantSize right = Sizes.constant(tokenizer.nextToken(), true);
        return createEmptyBorder(top, left, bottom, right);
    }

    /**
     * An empty border that uses 4 instances of {@link ConstantSize}
     * to define the gaps on all sides.
     */
    public static final class EmptyBorder implements Border {

        private final ConstantSize top;
        private final ConstantSize left;
        private final ConstantSize bottom;
        private final ConstantSize right;

        private EmptyBorder(ConstantSize top, ConstantSize left,
                ConstantSize bottom, ConstantSize right) {
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
        }

        /**
         * Returns this border's top size.
         *
         * @return this border's top size
         */
        public ConstantSize top() {
            return top;
        }

        /**
         * Returns this border's left size.
         *
         * @return this border's left size
         */
        public ConstantSize left() {
            return left;
        }

        /**
         * Returns this border's bottom size.
         *
         * @return this border's bottom size
         */
        public ConstantSize bottom() {
            return bottom;
        }

        /**
         * Returns this border's right size.
         *
         * @return this border's right size
         */
        public ConstantSize right() {
            return right;
        }

        /**
         * Paints the border for the specified component with the specified
         * position and size.
         *
         * @param c the component for which this border is being painted
         * @param g the paint graphics
         * @param x the x position of the painted border
         * @param y the y position of the painted border
         * @param width the width of the painted border
         * @param height the height of the painted border
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height) {
            // An empty border doesn't paint.
        }

        /**
         * Returns the insets of the border.
         *
         * @param c the component for which this border insets value applies
         * @return the border's Insets
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(top.getPixelSize(c), left.getPixelSize(c),
                    bottom.getPixelSize(c), right.getPixelSize(c));
        }

        /**
         * Returns whether or not the border is opaque.  If the border
         * is opaque, it is responsible for filling in it's own
         * background when painting.
         *
         * @return false - because the empty border is not opaque
         */
        @Override
        public boolean isBorderOpaque() {
            return false;
        }

    }

}
