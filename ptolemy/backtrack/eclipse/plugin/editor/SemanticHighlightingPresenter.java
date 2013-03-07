/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/* Semantic highlighting presenter.

 Copyright (c) 2005-2013 The Regents of the University of California.
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

package ptolemy.backtrack.eclipse.plugin.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.JavaPresentationReconciler;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;

import ptolemy.backtrack.eclipse.plugin.editor.SemanticHighlightingReconciler.HighlightedPosition;
import ptolemy.backtrack.eclipse.plugin.editor.SemanticHighlightingReconciler.HighlightingStyle;

///////////////////////////////////////////////////////////////////
//// SemanticHighlightingPresenter
/**
 Semantic highlighting presenter. This class is a modification of Eclipse's
 Java semantic highlighting presenter.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SemanticHighlightingPresenter implements
        ITextPresentationListener, ITextInputListener, IDocumentListener {

    /**
     * Adds all current positions to the given list.
     * <p>
     * NOTE: Called from background thread.
     * </p>
     *
     * @param list The list
     */
    public void addAllPositions(List<HighlightedPosition> list) {
        synchronized (_positionLock) {
            list.addAll(_positions);
        }
    }

    /** This method is called when a text presentation is about to be applied to
     *  the text viewer. The receiver is allowed to change the text presentation
     *  during that call.
     *
     *  @param textPresentation the current text presentation
     */
    public void applyTextPresentation(TextPresentation textPresentation) {
        IRegion region = textPresentation.getExtent();
        int i = _computeIndexAtOffset(_positions, region.getOffset());
        int n = _computeIndexAtOffset(_positions, region.getOffset()
                + region.getLength());

        if ((n - i) > 2) {
            List<StyleRange> ranges = new ArrayList<StyleRange>(n - i);

            for (; i < n; i++) {
                HighlightedPosition position = _positions.get(i);

                if (!position.isDeleted()) {
                    ranges.add(position.createStyleRange());
                }
            }

            StyleRange[] array = new StyleRange[ranges.size()];
            array = ranges.toArray(array);
            textPresentation.replaceStyleRanges(array);
        } else {
            for (; i < n; i++) {
                HighlightedPosition position = _positions.get(i);

                if (!position.isDeleted()) {
                    textPresentation.replaceStyleRange(position
                            .createStyleRange());
                }
            }
        }
    }

    /**
     * Creates and returns a new highlighted position with the given offset, length and highlighting.
     * <p>
     * NOTE: Also called from background thread.
     * </p>
     *
     * @param offset The offset
     * @param length The length
     * @param highlighting The highlighting
     * @return The new highlighted position
     */
    public HighlightedPosition createHighlightedPosition(int offset,
            int length, HighlightingStyle highlighting) {
        // TODO: reuse deleted positions
        return new HighlightedPosition(offset, length, highlighting,
                _positionUpdater);
    }

    /**
     * Create a text presentation in the background.
     * <p>
     * NOTE: Called from background thread.
     * </p>
     *
     * @param addedPositions the added positions
     * @param removedPositions the removed positions
     * @return the text presentation or <code>null</code>, if reconciliation should be canceled
     */
    public TextPresentation createPresentation(List<?> addedPositions,
            List<?> removedPositions) {
        JavaSourceViewer sourceViewer = _sourceViewer;
        JavaPresentationReconciler presentationReconciler = _presentationReconciler;

        if ((sourceViewer == null) || (presentationReconciler == null)) {
            return null;
        }

        if (isCanceled()) {
            return null;
        }

        IDocument document = sourceViewer.getDocument();

        if (document == null) {
            return null;
        }

        int minStart = Integer.MAX_VALUE;
        int maxEnd = Integer.MIN_VALUE;

        for (int i = 0, n = removedPositions.size(); i < n; i++) {
            HighlightedPosition position = (HighlightedPosition) removedPositions
                    .get(i);
            int offset = position.getOffset();
            minStart = Math.min(minStart, offset);
            maxEnd = Math.max(maxEnd, offset + position.getLength());
        }

        for (int i = 0, n = addedPositions.size(); i < n; i++) {
            HighlightedPosition position = (HighlightedPosition) addedPositions
                    .get(i);
            int offset = position.getOffset();
            minStart = Math.min(minStart, offset);
            maxEnd = Math.max(maxEnd, offset + position.getLength());
        }

        if (minStart < maxEnd) {
            try {
                return presentationReconciler.createRepairDescription(
                        new Region(minStart, maxEnd - minStart), document);
            } catch (RuntimeException e) {
                // Assume concurrent modification from UI thread
            }
        }

        return null;
    }

    /**
     * Create a runnable for updating the presentation.
     * <p>
     * NOTE: Called from background thread.
     * </p>
     * @param textPresentation the text presentation
     * @param addedPositions the added positions
     * @param removedPositions the removed positions
     * @return the runnable or <code>null</code>, if reconciliation should be canceled
     */
    public Runnable createUpdateRunnable(
            final TextPresentation textPresentation,
            List<HighlightedPosition> addedPositions,
            List<HighlightedPosition> removedPositions) {
        if ((_sourceViewer == null) || (textPresentation == null)) {
            return null;
        }

        // TODO: do clustering of positions and post multiple fast runnables
        final HighlightedPosition[] added = new HighlightedPosition[addedPositions
                .size()];
        addedPositions.toArray(added);

        final HighlightedPosition[] removed = new HighlightedPosition[removedPositions
                .size()];
        removedPositions.toArray(removed);

        if (isCanceled()) {
            return null;
        }

        Runnable runnable = new Runnable() {
            public void run() {
                updatePresentation(textPresentation, added, removed);
            }
        };

        return runnable;
    }

    /** The manipulation described by the document event will be performed.
     *
     *  @param event the document event describing the document change
     */
    public void documentAboutToBeChanged(DocumentEvent event) {
        setCanceled(true);
    }

    /** The manipulation described by the document event has been performed.
     *
     *  @param event the document event describing the document change
     */
    public void documentChanged(DocumentEvent event) {
    }

    /**
     * Invalidate text presentation of positions with the given highlighting.
     *
     * @param highlighting The highlighting
     */
    public void highlightingStyleChanged(HighlightingStyle highlighting) {
        for (int i = 0, n = _positions.size(); i < n; i++) {
            HighlightedPosition position = _positions.get(i);

            if (position.getHighlighting() == highlighting) {
                _sourceViewer.invalidateTextPresentation(position.getOffset(),
                        position.getLength());
            }
        }
    }

    /** Called before the input document is replaced.
     *
     *  @param oldInput the text viewer's previous input document
     *  @param newInput the text viewer's new input document
     */
    public void inputDocumentAboutToBeChanged(IDocument oldInput,
            IDocument newInput) {
        setCanceled(true);
        _releaseDocument(oldInput);
        _resetState();
    }

    /** Called after the input document has been replaced.
     *
     *  @param oldInput the text viewer's previous input document
     *  @param newInput the text viewer's new input document
     */
    public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
        _manageDocument(newInput);
    }

    /**
     * Install this presenter on the given source viewer and background presentation
     * reconciler.
     *
     * @param sourceViewer the source viewer
     * @param backgroundPresentationReconciler the background presentation reconciler,
     *  can be <code>null</code>, in that case {@link SemanticHighlightingPresenter#createPresentation(List, List)}
     *  should not be called
     */
    public void install(JavaSourceViewer sourceViewer,
            JavaPresentationReconciler backgroundPresentationReconciler) {
        _sourceViewer = sourceViewer;
        _presentationReconciler = backgroundPresentationReconciler;

        _sourceViewer.addTextPresentationListener(this);
        _sourceViewer.addTextInputListener(this);
        _manageDocument(_sourceViewer.getDocument());
    }

    /** Test whether the current reconcile is canceled.
     *
     *  @return true iff the current reconcile is canceled.
     */
    public boolean isCanceled() {
        IDocument document = (_sourceViewer != null) ? _sourceViewer
                .getDocument() : null;

        if (document == null) {
            return _isCanceled;
        }

        synchronized (_getLockObject(document)) {
            return _isCanceled;
        }
    }

    /**
     * Set whether or not the current reconcile is canceled.
     *
     * @param isCanceled <code>true</code> iff the current reconcile is canceled
     */
    public void setCanceled(boolean isCanceled) {
        IDocument document = (_sourceViewer != null) ? _sourceViewer
                .getDocument() : null;

        if (document == null) {
            _isCanceled = isCanceled;
            return;
        }

        synchronized (_getLockObject(document)) {
            _isCanceled = isCanceled;
        }
    }

    /**
     * Uninstall this presenter.
     */
    public void uninstall() {
        setCanceled(true);

        if (_sourceViewer != null) {
            _sourceViewer.removeTextPresentationListener(this);
            _releaseDocument(_sourceViewer.getDocument());
            _invalidateTextPresentation();
            _resetState();

            _sourceViewer.removeTextInputListener(this);
            _sourceViewer = null;
        }
    }

    /**
     * Invalidate the presentation of the positions based on the given added positions and the existing deleted positions.
     * Also unregisters the deleted positions from the document and patches the positions of this presenter.
     * <p>
     * NOTE: Indirectly called from background thread by UI runnable.
     * </p>
     * @param textPresentation the text presentation or <code>null</code>, if the presentation should computed in the UI thread
     * @param addedPositions the added positions
     * @param removedPositions the removed positions
     */
    public void updatePresentation(TextPresentation textPresentation,
            HighlightedPosition[] addedPositions,
            HighlightedPosition[] removedPositions) {
        if (_sourceViewer == null) {
            return;
        }

        //      checkOrdering("added positions: ", Arrays.asList(addedPositions)); //$NON-NLS-1$
        //      checkOrdering("removed positions: ", Arrays.asList(removedPositions)); //$NON-NLS-1$
        //      checkOrdering("old positions: ", fPositions); //$NON-NLS-1$
        // TODO: double-check consistency with document.getPositions(...)
        // TODO: reuse removed positions
        if (isCanceled()) {
            return;
        }

        IDocument document = _sourceViewer.getDocument();

        if (document == null) {
            return;
        }

        String positionCategory = _getPositionCategory();

        List<HighlightedPosition> removedPositionsList = Arrays
                .asList(removedPositions);

        try {
            synchronized (_positionLock) {
                List<HighlightedPosition> oldPositions = _positions;
                int newSize = (_positions.size() + addedPositions.length)
                        - removedPositions.length;
                List<HighlightedPosition> newPositions = new ArrayList<HighlightedPosition>(
                        newSize);
                HighlightedPosition position = null;
                HighlightedPosition addedPosition = null;

                for (int i = 0, j = 0, n = oldPositions.size(), m = addedPositions.length; (i < n)
                        || (position != null)
                        || (j < m)
                        || (addedPosition != null);) {
                    while ((position == null) && (i < n)) {
                        position = oldPositions.get(i++);

                        if (position.isDeleted()
                                || _contain(removedPositionsList, position)) {
                            document.removePosition(positionCategory, position);
                            position = null;
                        }
                    }

                    if ((addedPosition == null) && (j < m)) {
                        addedPosition = addedPositions[j++];
                        document.addPosition(positionCategory, addedPosition);
                    }

                    if (position != null) {
                        if (addedPosition != null) {
                            if (position.getOffset() <= addedPosition
                                    .getOffset()) {
                                newPositions.add(position);
                                position = null;
                            } else {
                                newPositions.add(addedPosition);
                                addedPosition = null;
                            }
                        } else {
                            newPositions.add(position);
                            position = null;
                        }
                    } else if (addedPosition != null) {
                        newPositions.add(addedPosition);
                        addedPosition = null;
                    }
                }

                _positions = newPositions;
            }
        } catch (BadPositionCategoryException e) {
            // Should not happen
            JavaPlugin.log(e);
        } catch (BadLocationException e) {
            // Should not happen
            JavaPlugin.log(e);
        }

        //      checkOrdering("new positions: ", fPositions); //$NON-NLS-1$
        if (textPresentation != null) {
            _sourceViewer.changeTextPresentation(textPresentation, false);
        } else {
            _sourceViewer.invalidateTextPresentation();
        }
    }

    /**
     * Add a position with the given range and highlighting unconditionally, only from UI thread.
     * The position will also be registered on the document. The text presentation is not invalidated.
     *
     * @param offset The range offset
     * @param length The range length
     * @param highlighting
     */
    private void _addPositionFromUI(int offset, int length,
            HighlightingStyle highlighting) {
        HighlightedPosition position = createHighlightedPosition(offset,
                length, highlighting);

        synchronized (_positionLock) {
            _insertPosition(position);
        }

        IDocument document = _sourceViewer.getDocument();

        if (document == null) {
            return;
        }

        String positionCategory = _getPositionCategory();

        try {
            document.addPosition(positionCategory, position);
        } catch (BadLocationException e) {
            // Should not happen
            JavaPlugin.log(e);
        } catch (BadPositionCategoryException e) {
            // Should not happen
            JavaPlugin.log(e);
        }
    }

    /**
     * Returns the index of the first position with an offset greater than the given offset.
     *
     * @param positions the positions, must be ordered by offset and must not overlap
     * @param offset the offset
     * @return the index of the last position with an offset greater than the given offset
     */
    private int _computeIndexAfterOffset(List<HighlightedPosition> positions,
            int offset) {
        int i = -1;
        int j = positions.size();

        while ((j - i) > 1) {
            int k = (i + j) >> 1;
            HighlightedPosition position = positions.get(k);

            if (position.getOffset() > offset) {
                j = k;
            } else {
                i = k;
            }
        }

        return j;
    }

    /**
     * Returns the index of the first position with an offset equal or greater than the given offset.
     *
     * @param positions the positions, must be ordered by offset and must not overlap
     * @param offset the offset
     * @return the index of the last position with an offset equal or greater than the given offset
     */
    private int _computeIndexAtOffset(List<HighlightedPosition> positions,
            int offset) {
        int i = -1;
        int j = positions.size();

        while ((j - i) > 1) {
            int k = (i + j) >> 1;
            HighlightedPosition position = positions.get(k);

            if (position.getOffset() >= offset) {
                j = k;
            } else {
                i = k;
            }
        }

        return j;
    }

    /**
     * Returns <code>true</code> iff the positions contain the position.
     * @param positions the positions, must be ordered by offset and must not overlap
     * @param position the position
     * @return <code>true</code> iff the positions contain the position
     */
    private boolean _contain(List<HighlightedPosition> positions,
            HighlightedPosition position) {
        return _indexOf(positions, position) != -1;
    }

    /**
     * @param document the document
     * @return the document's lock object
     */
    private Object _getLockObject(IDocument document) {
        if (document instanceof ISynchronizable) {
            return ((ISynchronizable) document).getLockObject();
        }

        return document;
    }

    /**
     * @return The semantic reconciler position's category.
     */
    private String _getPositionCategory() {
        return toString();
    }

    /**
     * Returns index of the position in the positions, <code>-1</code> if not found.
     * @param positions the positions, must be ordered by offset and must not overlap
     * @param position the position
     * @return the index
     */
    private int _indexOf(List<HighlightedPosition> positions,
            HighlightedPosition position) {
        int index = _computeIndexAtOffset(positions, position.getOffset());
        return ((index < positions.size()) && (positions.get(index) == position)) ? index
                : -1;
    }

    /**
     * Insert the given position in <code>fPositions</code>, s.t. the offsets remain in linear order.
     *
     * @param position The position for insertion
     */
    private void _insertPosition(HighlightedPosition position) {
        int i = _computeIndexAfterOffset(_positions, position.getOffset());
        _positions.add(i, position);
    }

    /**
     * Invalidate text presentation of all positions.
     */
    private void _invalidateTextPresentation() {
        for (int i = 0, n = _positions.size(); i < n; i++) {
            HighlightedPosition position = _positions.get(i);
            _sourceViewer.invalidateTextPresentation(position.getOffset(),
                    position.getLength());
        }
    }

    /**
     * Start managing the given document.
     *
     * @param document The document
     */
    private void _manageDocument(IDocument document) {
        if (document != null) {
            document.addPositionCategory(_getPositionCategory());
            document.addPositionUpdater(_positionUpdater);
            document.addDocumentListener(this);
        }
    }

    /**
     * Stop managing the given document.
     *
     * @param document The document
     */
    private void _releaseDocument(IDocument document) {
        if (document != null) {
            document.removeDocumentListener(this);
            document.removePositionUpdater(_positionUpdater);

            try {
                document.removePositionCategory(_getPositionCategory());
            } catch (BadPositionCategoryException e) {
                // Should not happen
                JavaPlugin.log(e);
            }
        }
    }

    /**
     * Reset to initial state.
     */
    private void _resetState() {
        synchronized (_positionLock) {
            _positions.clear();
        }
    }

    /** <code>true</code> iff the current reconcile is canceled. */
    private boolean _isCanceled = false;

    /** UI position lock */
    private Object _positionLock = new Object();

    /** Position updater */
    private IPositionUpdater _positionUpdater = new HighlightingPositionUpdater(
            _getPositionCategory());

    /** UI's current highlighted positions - can contain <code>null</code> elements */
    private List<HighlightedPosition> _positions = new ArrayList<HighlightedPosition>();

    /** The background presentation reconciler */
    private JavaPresentationReconciler _presentationReconciler;

    /** The source viewer this semantic highlighting reconciler is installed on */
    private JavaSourceViewer _sourceViewer;

    ///////////////////////////////////////////////////////////////////
    ////                    private inner classes                  ////

    /**
     * Semantic highlighting position updater.
     */
    private class HighlightingPositionUpdater implements IPositionUpdater {

        /**
         * Creates a new updater for the given <code>category</code>.
         *
         * @param category the new category.
         */
        public HighlightingPositionUpdater(String category) {
            _category = category;
        }

        /*
         * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
         */
        public void update(DocumentEvent event) {
            int eventOffset = event.getOffset();
            int eventOldLength = event.getLength();
            int eventEnd = eventOffset + eventOldLength;

            try {
                Position[] positions = event.getDocument().getPositions(
                        _category);

                for (int i = 0; i != positions.length; i++) {
                    HighlightedPosition position = (HighlightedPosition) positions[i];

                    // Also update deleted positions because they get deleted by the background thread and removed/invalidated only in the UI runnable
                    //                  if (position.isDeleted())
                    //                      continue;
                    int offset = position.getOffset();
                    int length = position.getLength();
                    int end = offset + length;

                    if (offset > eventEnd) {
                        updateWithPrecedingEvent(position, event);
                    } else if (end < eventOffset) {
                        updateWithSucceedingEvent(position, event);
                    } else if ((offset <= eventOffset) && (end >= eventEnd)) {
                        updateWithIncludedEvent(position, event);
                    } else if (offset <= eventOffset) {
                        updateWithOverEndEvent(position, event);
                    } else if (end >= eventEnd) {
                        updateWithOverStartEvent(position, event);
                    } else {
                        updateWithIncludingEvent(position, event);
                    }
                }
            } catch (BadPositionCategoryException e) {
                // ignore and return
            }
        }

        /**
         * Update the given position with the given event. The event is included by the position.
         *
         * @param position The position
         * @param event The event
         */
        private void updateWithIncludedEvent(HighlightedPosition position,
                DocumentEvent event) {
            int eventOffset = event.getOffset();
            String newText = event.getText();

            if (newText == null) {
                newText = ""; //$NON-NLS-1$
            }

            int eventNewLength = newText.length();

            int deltaLength = eventNewLength - event.getLength();

            int offset = position.getOffset();
            int length = position.getLength();
            int end = offset + length;

            int includedLength = 0;

            while ((includedLength < eventNewLength)
                    && Character.isJavaIdentifierPart(newText
                            .charAt(includedLength))) {
                includedLength++;
            }

            if (includedLength == eventNewLength) {
                position.setLength(length + deltaLength);
            } else {
                int newLeftLength = eventOffset - offset + includedLength;

                int excludedLength = eventNewLength;

                while ((excludedLength > 0)
                        && Character.isJavaIdentifierPart(newText
                                .charAt(excludedLength - 1))) {
                    excludedLength--;
                }

                int newRightOffset = eventOffset + excludedLength;
                int newRightLength = (end + deltaLength) - newRightOffset;

                if (newRightLength == 0) {
                    position.setLength(newLeftLength);
                } else {
                    if (newLeftLength == 0) {
                        position.update(newRightOffset, newRightLength);
                    } else {
                        position.setLength(newLeftLength);
                        _addPositionFromUI(newRightOffset, newRightLength,
                                position.getHighlighting());
                    }
                }
            }
        }

        /**
         * Update the given position with the given event. The event includes the position.
         *
         * @param position The position
         * @param event The event
         */
        private void updateWithIncludingEvent(HighlightedPosition position,
                DocumentEvent event) {
            position.delete();
            position.update(event.getOffset(), 0);
        }

        /**
         * Update the given position with the given event. The event overlaps with the end of the position.
         *
         * @param position The position
         * @param event The event
         */
        private void updateWithOverEndEvent(HighlightedPosition position,
                DocumentEvent event) {
            String newText = event.getText();

            if (newText == null) {
                newText = ""; //$NON-NLS-1$
            }

            int eventNewLength = newText.length();

            int includedLength = 0;

            while ((includedLength < eventNewLength)
                    && Character.isJavaIdentifierPart(newText
                            .charAt(includedLength))) {
                includedLength++;
            }

            position.setLength(event.getOffset() - position.getOffset()
                    + includedLength);
        }

        /**
         * Update the given position with the given event. The event overlaps with the start of the position.
         *
         * @param position The position
         * @param event The event
         */
        private void updateWithOverStartEvent(HighlightedPosition position,
                DocumentEvent event) {
            int eventOffset = event.getOffset();
            int eventEnd = eventOffset + event.getLength();

            String newText = event.getText();

            if (newText == null) {
                newText = ""; //$NON-NLS-1$
            }

            int eventNewLength = newText.length();

            int excludedLength = eventNewLength;

            while ((excludedLength > 0)
                    && Character.isJavaIdentifierPart(newText
                            .charAt(excludedLength - 1))) {
                excludedLength--;
            }

            int deleted = eventEnd - position.getOffset();
            int inserted = eventNewLength - excludedLength;
            position.update(eventOffset + excludedLength, position.getLength()
                    - deleted + inserted);
        }

        /**
         * Update the given position with the given event. The event precedes the position.
         *
         * @param position The position
         * @param event The event
         */
        private void updateWithPrecedingEvent(HighlightedPosition position,
                DocumentEvent event) {
            String newText = event.getText();
            int eventNewLength = (newText != null) ? newText.length() : 0;
            int deltaLength = eventNewLength - event.getLength();

            position.setOffset(position.getOffset() + deltaLength);
        }

        /**
         * Update the given position with the given event. The event succeeds the position.
         *
         * @param position The position
         * @param event The event
         */
        private void updateWithSucceedingEvent(HighlightedPosition position,
                DocumentEvent event) {
        }

        /** The position category. */
        private final String _category;
    }
}
