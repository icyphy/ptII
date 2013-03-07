/* A Token that represents a pointer to an address in memory.

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
package ptolemy.domains.taskpt.kernel;

import ptolemy.data.Token;

///////////////////////////////////////////////////////////////////
//// PtrToken

/** A Token that represents a pointer to an address in memory. In addition
 * the token has a size that specifies the size of the array in memory.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 **/
public class PtrToken extends Token {

    /** Create a Token.
     */
    public PtrToken() {
        super();
    }

    /** Create a Token with an initial address and an initial size.
     *
     * @param addr The address in the memory that this token is pointing to.
     * @param size The size of the array in the memory that this token
     * is pointing to.
     */
    public PtrToken(int addr, int size) {
        super();
        setAddress(addr);
        setSize(size);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the address in memory this token is pointing to. Do
     * not check, if there actually is something at that memory address.
     *
     * @see #setAddress(int)
     *
     * @return The address in memory this token is pointing to.
     */
    public int getAddress() {
        return _addr;
    }

    /** Get the size of the array in memory this token is pointing to. Do
     * not check, if the array in the memory is of that size.
     *
     * @see #setSize(int)
     *
     * @return The size of the array in memory this token is pointing to.
     */
    public int getSize() {
        return _size;
    }

    /** Set the address in memory this token should point to. Do not
     * change the position of the array in the memory.
     *
     * @see #getAddress()
     *
     * @param addr The address in memory this token should point to.
     */
    public void setAddress(int addr) {
        this._addr = addr;
    }

    /** Set the size of the array in memory this token is pointing to. Do not
     * change the actual content or size of the memory nor the array
     * in the memory.
     *
     * @see #getSize()
     *
     * @param size The size of the array in memory this token is pointing to.
     */
    public void setSize(int size) {
        this._size = size;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _addr;

    private int _size;

}
