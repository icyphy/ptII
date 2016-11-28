/*
 * Copyright (c) 2004-2007 by Michael Connor. All Rights Reserved.
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
 *  o Neither the name of FormLayoutBuilder or Michael Connor nor the names of
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
/*
 * TransferableWrapper.java
 *
 * Created on October 18, 2004, 6:05 PM
 */

package org.mlc.swing.layout;

import java.awt.datatransfer.DataFlavor;

/**
 * A wrapper for transferring data using drag and drop.
 * @author Michael Connor
 * @version $Id$
 * @since Ptolemy II 8.0
 */
public class TransferableWrapper implements java.awt.datatransfer.Transferable {
    Object dragObject;

    /** Instantiate a TransferableWrapper.
     *  @param dragObject The drag object
     */
    public TransferableWrapper(Object dragObject) {
        this.dragObject = dragObject;
    }

    /** Get the transfer data for a flavor.
     *  @param flavor the flavor
     *  @retrun the transfer data.
     */
    @Override
    public synchronized Object getTransferData(DataFlavor flavor) {
        return dragObject;
    }

    /** Return true if the data flavor is supported.
     *  @param flavor the flavor to be checked.
     *  @return true if the data flavor is supported.        
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getMimeType().equals(
                DataFlavor.javaJVMLocalObjectMimeType);
    }

    /** Get the transfer data flavors.
     *  @retrun the transfer data flavors.
     */
    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
        try {
            return new DataFlavor[] { new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType) };
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Unable to load DataFlavor "
                    + DataFlavor.javaJVMLocalObjectMimeType, cnfe);
        }
    }
}
