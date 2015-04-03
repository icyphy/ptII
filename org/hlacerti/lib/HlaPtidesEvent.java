package org.hlacerti.lib;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

///////////////////////////////////////////////////////////////////
//// HlaPtidesEvent

/**
 * <p>This class implements a data structure to encapsulate PTIDES events sent
 * through a HLA/CERTI Federation. The logical time of the PTIDES event and the
 * value are stored in a Vector<Byte>. Only PTIDES event from NetworkPort are
 * supported by this implementation.
 * </p><p>
 * This implementation reuses a subset of methods implemented in the
 * certi.communiation.MessageBuffer class provided by JCERTI available at:
 * <br><a href="http://savannah.nongnu.org/projects/certi" target="_top">http://savannah.nongnu.org/projects/certi</a></br>
 * </p><p>
 * NOTE: The JVM is big endian, so all the write methods are big endians.
 * The read methods can handle both big and little endians.
 * </p>
 *
 *  @author Gilles Lasnier, Based on MessageBuffer.java by Pancik et al. (see JCERTI)
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public class HlaPtidesEvent {

    /** Construct a HlaPtidesEvent. The HlaPtidesEvent structure stores
     *  the PTIDES <i>logicalTime</i>, the <i>microStep</i>, the <i>sourceTime</i>
     *  and the <i>value</i> of the event in a Vector<byte> buffer.
     *  @param logicalTime The PTIDES logical time as double.
     *  @param microStep The current microstep of the event.
     *  @param sourceTime The souce timestamp of the event.
     *  @param value The value of the event as array of bytes.
     */
    public HlaPtidesEvent(Double logicalTime, int microStep, Double sourceTime,
            byte[] value) {
        _buffer = new Vector<Byte>();
        _iter = _buffer.iterator();

        this._write(logicalTime);
        this._write(microStep);
        this._write(sourceTime);
        this._write(value);
    }

    /** Construct a HlaPtidesEvent from an array of bytes. The <i>byteArray</i>
     *  is already a representation of a HlaPtidesEvent sent through the
     *  HLA/CERTI Federation.
     *  @param byteArray The representation of the HlaPtidesEvent as array of
     *  bytes.
     */
    public HlaPtidesEvent(byte[] byteArray) {
        _buffer = new Vector<Byte>();
        _iter = _buffer.iterator();

        for (byte element : byteArray) {
            _buffer.add(element);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the representation of the HlaPtidesEvent as an array of bytes.
     *  @return The HlaPtidesEvent as byte array.
     */
    public byte[] getBuffer() {
        byte[] byteArray = new byte[_buffer.size()];
        for (int i = 0; i < _buffer.size(); i++) {
            byteArray[i] = _buffer.get(i);
        }

        return byteArray;
    }

    /** Return the PTIDES logical time of the event, encapsulated in the buffer.
     * @return The PTIDES logical time as Double.
     */
    public Double getLogicalTime() {
        _iter = _buffer.iterator();
        return this._readDouble();
    }

    /** Return the microstep of the event, encapsulated in the buffer.
     * @return The microstep as Int.
     */
    public int getMicroStep() {
        _iter = _buffer.iterator();

        // Remove the logicalTime.
        this._readDouble();

        return this._readInt();
    }

    /** Return the source timestamp of the event, encapsulated in the buffer.
     * @return The source timestamp as Double.
     */
    public Double getSourceTime() {
        _iter = _buffer.iterator();

        // Remove the logicalTime, then the microStep.
        this._readDouble();
        this._readInt();

        return this._readDouble();
    }

    /** Return the value of the event, encapsulated in the buffer.
     *  @return The value of the event as array of byte.
     */
    public byte[] getValue() {
        _iter = _buffer.iterator();

        // Remove the logicalTime, the microStep and the sourceTime.
        this._readDouble();
        this._readInt();
        this._readDouble();

        return this._readBytes();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Read the byte from the buffer at the current offset pointed by
     *  <i>_iter</i>.
     *  @return A byte.
     *  @exception NoSuchElementException If there is no more byte to read.
     */
    private byte _readByte() throws NoSuchElementException {
        return _iter.next();
    }

    /** Read and return an array of bytes from the buffer at the current offset
     *  pointed by <i>_iter</i>. First read the size (integer) and then return
     *  the array.
     *  @return An array of bytes.
     */
    private byte[] _readBytes() {
        // Treat first integer as length.
        int size = this._readInt();

        byte[] byteArray = new byte[size];
        for (int i = 0; i < size; i++) {
            byteArray[i] = this._readByte();
        }

        return byteArray;
    }

    /** Read a Double encoded in the buffer. The endianness is
     *  taken care of.
     *  @return a java (big endian IEEE 754) Double.
     */
    private double _readDouble() {
        return Double.longBitsToDouble(this._readLong());
    }

    /** Read an integer from the buffer with correct endianess.
     * NOTE: there are no unsigned integers of any size in java.
     * This methods assume that the int it is trying to read is inside the
     * bounds of a signed int. There are no conversion to an long if it is
     * not the case.
     * @return A int.
     * @exception NoSuchElementException If there is no more byte to read.
     */
    private int _readInt() throws NoSuchElementException {
        int i = 0;
        if (_endianness == _BIG_ENDIAN) {
            for (int s = 3; s >= 0; s--) {
                i = i | ((int) _iter.next() & 0x000000FF) << _BYTE_LENGTH * s;
            }
        } else {
            for (int s = 0; s <= 3; s++) {
                i = i | ((int) _iter.next() & 0x000000FF) << s * _BYTE_LENGTH;
            }
        }
        return i;
    }

    /** Read a long from the buffer with correct endianess.
     * NOTE: there are no unsigned integers of any size in java.
     * This methods assume that the long it is trying to read is inside the
     * bounds of a signed long. There are no conversion to something bigger if
     * it is not the case.
     * @return A long.
     * @exception NoSuchElementException If there is no more byte to read.
     */
    private long _readLong() throws NoSuchElementException {
        long l = 0;
        if (_endianness == _BIG_ENDIAN) {
            for (int i = 7; i >= 0; i--) {
                l = l
                        | ((long) _iter.next() & 0x00000000000000FF) << i
                        * _BYTE_LENGTH;
            }
        } else {
            for (int i = 0; i <= 7; i++) {
                l = l
                        | ((long) _iter.next() & 0x00000000000000FF) << i
                        * _BYTE_LENGTH;
            }
        }
        return l;
    }

    /** This method set the buffer back in the state just after its creation.
     *  Its should be used before re-using the buffer to write.
     */
    public void reset() {
        _buffer.clear();
    }

    /** Write byte array to buffer. First is the length of the array, stored as
     *  a integer, then the bytes themselves.
     *  @param array The array of bytes to store.
     */
    private void _write(byte[] array) {
        this._write(array.length);

        for (byte b : array) {
            _buffer.add(b);
        }
    }

    /** Write a double in a big endian (IEEE 754 standard way).
     *  @param dbl The Double value to store.
     */
    private void _write(double dbl) {
        this._write(Double.doubleToLongBits(dbl));
    }

    /** Write integer to the buffer (in a big endian way).
     *  @param i The integer to be written.
     */
    public void _write(int i) {
        for (int j = 3; j >= 0; j--) {
            _buffer.add((byte) (i >>> j * _BYTE_LENGTH));
        }
    }

    /** Write long to the buffer (in a big endian way).
     *  @param l The long to be written.
     */
    private void _write(long l) {
        for (int i = 7; i >= 0; i--) {
            _buffer.add((byte) (l >>> i * _BYTE_LENGTH));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Define BIG_ENDIAN macro. */
    private static byte _BIG_ENDIAN = 1;

    /** Indicate if the system is big endian. */
    private byte _endianness = _BIG_ENDIAN;

    /** Constant for the size of a byte in the system. */
    public static final int _BYTE_LENGTH = 8;

    /** Buffer which contains PTIDES logical time, microstep, source timestamp
     *  and the encoded HLA value. */
    private Vector<Byte> _buffer = null;

    /** Iterator uses for the read operations. */
    private Iterator<Byte> _iter;
}
