/* An object that is a buffer for Tokens.

 Copyright (c) 2010-2013 The Regents of the University of California.
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
package ptolemy.domains.taskpt.lib;

import java.util.Vector;

import ptolemy.data.Token;
import ptolemy.domains.taskpt.kernel.PtrToken;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// Memory

/** An object that is a buffer for Tokens. This is used in the
 * taskpt domain to model a shared memory.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 */
public class Memory extends NamedObj {

    /** Construct a memory. Initialize the internal buffer.
     */
    public Memory() {
        _buffer = new Vector<Token>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Allocate a slot in the memory (currently at the end).
     *
     * @param size The size of the slot to be allocated.
     * @return A pointer to the address allocated in the memory.
     */
    public PtrToken allocate(int size) {
        int addr = _buffer.size();
        _buffer.setSize(addr + size);
        PtrToken result = new PtrToken(addr, size);
        return result;
    }

    /** Remove all entries from the memory. */
    public void clear() {
        _buffer.clear();
    }

    /** Read a token from the given address in this memory.
     *
     * @param index The address to be read.
     * @return The token, if there is one located at that address.
     * If not or index is exceeding the memory capacity, then null is returned.
     */
    public Token read(int index) {
        if (_buffer.size() <= index) {
            return null;
        }
        _debug("reading token from address " + index);
        return _buffer.get(index);
    }

    /** Write a token into the specified memory address given by index. If memory
     * capacity is smaller than the given address, the memory is extended
     * automatically.
     *
     * @param index The address to be written.
     * @param token The token to be written.
     */
    public void write(int index, Token token) {
        if (_buffer.size() - 1 < index) {
            _buffer.setSize(index + 1);
        }
        _debug("writing token to address " + index);
        _buffer.set(index, token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Vector<Token> _buffer;

}
