/* A library of audio operations.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.media;

import java.lang.*;
import java.util.*;
import java.io.*;


//////////////////////////////////////////////////////////////////////////
//// Audio
/**
 * Instances of this class represent audio data equivalent to that
 * contained by a Sun/NeXT audio file (.au file).  The class also
 * includes a set of utility static methods for manipulating audio
 * signals.
 * Currently, only an 8kHz sample rate, mu-law encoded, monophonic
 * audio format is supported.
 * <p>
 *  The format of an audio file is:
 *  <CENTER>
 *  <TABLE BORDER = 1>
 *  <TR BGCOLOR = #DFDFA0><TD>byte</TD><TD>type</TD>
 *  <TD>field name   </TD><TD> field value                      </TD></TR>
 *  <TR><TD>0x00</TD><TD>byte </TD><TD>magic[4]        </TD>
 *  <TD> 0x2E736E64 '.snd' in ASCII     </TD></TR>
 *  <TR><TD>0x04</TD><TD>int </TD><TD>offset </TD>
 *  <TD> offset of audio data relative to the start of the stream </TD></TR>
 *  <TR><TD>0x08</TD><TD>int </TD><TD>size     </TD>
 *  <TD> number of bytes of data        </TD></TR>
 *  <TR><TD>0x0C</TD><TD>int </TD><TD>format   </TD>
 *  <TD> format code: 1 for 8-bit u-law </TD></TR>
 *  <TR><TD>0x10</TD><TD>int </TD><TD>samplingRate </TD>
 *  <TD> the sampling rate              </TD></TR>
 *  <TR><TD>0x14</TD><TD>int </TD><TD>numChannels </TD>
 *  <TD> the number of channels         </TD></TR>
 *  <TR><TD>0x18</TD><TD>byte</TD><TD>info[]      </TD>
 *  <TD> optional text information      </TD></TR>
 *  </TABLE>
 *  </CENTER>
 *  <p>
 *  The design of this class is based on the web page of
 *  <a href=mailto:donahu@cooper.edu>Billy Donahue</a>.
 *  <a href=http://www.cooper.edu/~donahu/auformat/auFormat.html>
 *  http://www.cooper.edu/~donahu/auformat/auFormat.html</a>.
 *  Note that this class serves the same role as
 *  the class by the same name in the sun.audio package, but is much
 *  more public about its information.  For example, the Sun version
 *  does not give any access to the audio data itself.
 *
 * @author Edward A. Lee
 * @version $Id$
 */

public class Audio {

    /** Construct an instance initialized with the audio
     *  signal given by the argument.  The argument is an array
     *  of bytes that are mu-law encoded.  The audio signal is assumed
     *  to have an 8kHz sample rate, with a single channel.
     *  @param audio An audio signal.
     */
    public Audio(byte[] audio) {
        String ptinfo = "Ptolemy audio";
        // NOTE: This uses the platform encoding, which is probably wrong.
        info = ptinfo.getBytes();
        offset = 24 + info.length;
        size = audio.length;
        format = 1;
        sampleRate = 8000;
        numChannels = 1;
        this.audio = new byte[1][];
        this.audio[0] = audio;
    }

    /** Construct an instance initialized with the audio
     *  signal given by the argument.  The argument is an array
     *  of double-precision, floating point audio samples assumed to
     *  be normalized to be in the range -1.0 to 1.0.
     *  The data will be encoded according to the mu-law standard at an
     *  8kHz sample rate, with a single channel.
     *  @param audio An audio signal.
     */
  public Audio(double[] audio) {
        String ptinfo = "Ptolemy audio";
        // NOTE: This uses the platform encoding, which is probably wrong.
        info = ptinfo.getBytes();
        offset = 24 + info.length;
        size = audio.length;
        format = 1;
        sampleRate = 8000;
        numChannels = 1;
        this.audio = new byte[1][size];
        for (int i = size-1; i >= 0; i--) {
            this.audio[0][i] = lin2mu((int)(audio[i]*31616.0));
        }
    }

    /** Construct an instance and initialize it by reading
     *  the specified stream.
     *  @param input The input stream.
     *  @exception IOException If an error occurs reading the input data
     *   (e.g. a premature end of file).
     */
    public Audio(DataInputStream input) throws IOException {
        input.read(magic, 0, 4);

        // Check the magic number, which should be 0x2E736E64, '.snd'
        // in ASCII.
        if (magic[0] != 0x2E || magic[1] != 0x73 || magic[2] != 0x6E ||
                 magic[3] != 0x64) {
             throw new IllegalArgumentException(
                     "ptolemy.media.Audio: bad magic number in "
                     + "stream header.  Not an audio file?");
        }

        offset = input.readInt();
        size = input.readInt();
        format = input.readInt();
        sampleRate = input.readInt();
        numChannels = input.readInt();

        if (offset < 0 || offset > 10000) {
            throw new IllegalArgumentException("ptolemy.media.Audio:"
                    + " offset value '" + offset +
                    "' is out of range 0-10000");
        }
        info = new byte[offset-24];
        input.read(info, 0, offset-24);

        if (format != 1) {
            throw new IllegalArgumentException("ptolemy.media.Audio:"
                    + " Sorry, only 8-bit mu-law encoded data can be read.");
        }
        if (numChannels != 1) {
            throw new IllegalArgumentException("ptolemy.media.Audio:"
                    + " Sorry, only one-channel audio data can be read.");
        }

        // Finally read the audio data.
        audio = new byte[1][size];
        input.readFully(audio[0]);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public members                         ////

    /** The file type identifier, 0x2E736E64 or '.snd' in ASCII. */
    public byte magic[] = {(byte)'.', (byte)'s', (byte)'n', (byte)'d'};

    /** Offset of audio data relative to the start of the stream. */
    public int offset;

    /** Number of bytes of audio data. */
    public int size;

    /** Format code; 1 for 8-bit mu-law. */
    public int format;

    /** The sampling rate. */
    public int sampleRate;

    /** The number of channels. */
    public int numChannels;

    /** Four byte info field. */
    public byte info[];

    /** Audio data, by channel. */
    public byte[][] audio;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert an integer linear representation of an audio sample
     *  into a mu-255 companded representation.  Mu law is the standard
     *  used in Sun .au files as well as throughout the telephone network.
     *  <p>
     *  The integer argument is a 16-bit representation of the sample.
     *  Anything outside the range -32635 to 32635 will be clipped to within
     *  that range.
     *  <p>
     *  The mu-255 representation is a byte SEEEMMMM where S is the sign
     *  bit, EEE is the three-bit exponent, and MMMM is the four-bit
     *  mantissa. The bits are flipped, so that the binary 10000000
     *  is the largest positive number and 00000000 is the largest negative
     *  number. If you have called that static method setZeroTrap() with a
     *  <i>true</i> argument, then
     *  per MIL-STD-188-113, the 00000000 representation
     *  is never used, replaced instead with 00000010 (0x02).
     *  By default, this trap is not used.
     *  <p>
     *  This implementation was written by Anthony Hursh, who included with it
     *  the following information:
     *  <p>
     *  Copyright 1997 by Anthony Hursh
     *  &lt;hursha@saturn.math.uaa.alaska.edu&gt;
     *  This code may be freely used as long as proper credit
     *  is given.  It was originally written in C by
     *  Craig Reese (IDA/Supercomputing Research Center) and
     *  Joe Campbell (Department of Defense), and
     *  ported to Java by Tony Hursh, January 1997.
     *  References:
     *  <ol>
     *  <li> CCITT Recommendation G.711  (very difficult to follow)
     *  <li> "A New Digital Technique for Implementation of Any
     *     Continuous PCM Companding Law," Villeret, Michel,
     *     et al. 1973 IEEE Int. Conf. on Communications, Vol 1,
     *     1973, pg. 11.12-11.17
     *  <li> MIL-STD-188-113,"Interoperability and Performance Standards
     *     for Analog-to_Digital Conversion Techniques,"
     *     17 February 1987
     *  </ol>
     *
     *  @param lin A linear representation of the sample.
     *  @return A mu-255 representation of the sample.
     */
    public static byte lin2mu(int sample) {
        int sign = 0;
        if(sample < 0) {
            sample = -sample;
            sign = 0x80;
        }
        // clip the magnitude
        if (sample > CLIP) sample = CLIP;
        sample = sample + BIAS;
        int exponent =  exp_lut[(sample>>7) & 0xFF];
        int mantissa = (sample >> (exponent+3)) & 0x0F;
        int ulawbyte = (sign | (exponent << 4) | mantissa);
        // System.out.println(" sign = " + sign + " exponent = " +
        // exponent + " mantissa = " + mantissa );
        ulawbyte =  ~ulawbyte;
        ulawbyte &= 0xFF;
        if(_zerotrap && ulawbyte == 0 ) {
            // optional CCITT trap
            ulawbyte = 0x02;
        }
        return (byte)ulawbyte;
    }

    /** Convert mu-255 companded representation of an audio sample
     *  into an integer linear representation.  Mu law is the standard
     *  used in Sun .au files as well as throughout the telephone network.
     *  This implementation is based on the web page by
     *  <a href=mailto:donahu@cooper.edu>Billy Donahue</a>:
     *  <a href=http://www.cooper.edu/~donahu/auformat/auFormat.html>
     *  http://www.cooper.edu/~donahu/auformat/auFormat.html</a>.
     *  The resulting integer values are scaled to be in the range -31616
     *  to 31616.  This uses the low order 16 bits of the resulting integer,
     *  and thus provides a convenient 16-bit linear encoding.
     *  <p>
     *  The mu-255 representation is a byte SEEEMMMM where S is the sign
     *  bit, EEE is the three-bit exponent, and MMMM is the four-bit
     *  mantissa.  The bits are flipped, so that the binary 10000000
     *  is the largest positive number and 00000000 is the largest negative
     *  number.
     *  <p>
     *  If you have called setZeroTrap() with a <i>true</i> argument, then
     *  this will not be an exact inverse of lin2mu because the zero code
     *  is interpreted as being the largest negative number, -31616.
     *
     *  @param b A mu-255 representation of the sample.
     *  @return A linear representation of the sample.
     */
    public static int mu2lin(byte b) {
        // flip the bits
        int mu = b ^ 0xFF;
        int sign = (mu & 0x80) >> 7 ;
        int exponent = (mu & 0x70) >> 4 ;
        int mantissa = (mu & 0x0F);
        // System.out.println(" sign = " + sign + " exponent = " +
        // exponent + " mantissa = " + mantissa );
        int linear = (mantissa<<(exponent+1)) - 0x20 + (0x20 << exponent);
        // Make into a 16 bit sample.
        linear <<= 2;
        return (sign == 1) ? -linear : linear;
    }

    /** Read Sun audio file (.au) format and return the audio data as an array.
     *  The argument stream may represent a file, a URL, or a byte array that
     *  contains the .au format.
     *  For example, given a URL called "url," you can read the audio file
     *  as follows:
     *  <pre>
     *     double audio[] = readAudio(new DataInputStream(url.openStream());
     *  </pre>
     *  <p>
     *  The returned values lie in the range -1.0 to 1.0.
     *
     *  @param input The input stream.
     *  @exception IOException If an I/O error occurs reading the stream.
     *  @exception IllegalArgumentException If the input stream
     *   is not an understood format.  This is a runtime exception, so it need
     *   not be declared explicitly
     */
    public static double[] readAudio(DataInputStream input)
            throws IOException {
        Audio audio = new Audio(input);
        return audio.toDouble(0);
    }

    /** Configure all instances of this class to use the MIL-STD zero trap.
     *  I.e., per MIL-STD-188-113, the 00000000 representation
     *  is never used, replaced instead with 00000010 (0x02).
     *  For some reason, an all zero
     *  mu-law code is sometimes undesirable.  By default, this class does
     *  not use this trap, so you must call this with a <i>true</i> argument
     *  to use the trap.
     *  @param boole If true, use zero-trap encoding.
     */
    public static void setZeroTrap(boolean boole) {
        _zerotrap = boole;
    }

    /** Convert the audio data to linear double encoding (from mu-law).
     *  The returned numbers lie in the range -1.0 to 1.0.
     *  @param channel The channel number.
     *  @return A new array of integers, or null if there is no audio data.
     */
    public double[] toDouble(int channel) {
        int[] intdata = toLinear(channel);
        if (intdata != null) {
            double[] result = new double[intdata.length];
            for (int i = intdata.length-1; i >= 0; i--) {
                result[i] = ((double)(intdata[i]))/31616.0;
            }
            return result;
        }
        return null;
    }

    /** Convert the audio data to linear integer encoding (from mu-law).
     *  The returned integers use the low-order 16 bits only, lying
     *  in the range -31616 to 31616.
     *  @param channel The channel number.
     *  @return A new array of integers, or null if there is no audio data.
     */
    public int[] toLinear(int channel) {
        if (audio != null) {
            if (audio.length > channel && audio[channel] != null) {
                int[] result = new int[audio[channel].length];
                for (int i = audio[channel].length-1; i >= 0; i--) {
                    result[i] = mu2lin(audio[channel][i]);
                }
                return result;
            }
        }
        return null;
    }

    /** Return a readable representation of the header data. */
    public String toString() {
        return "file ID tag = " + new String(magic) + "\n"
            + "offset = " + offset + "\n"
            + "size = " + size + "\n"
            + "format code = " + format + "\n"
            + "sampleRate = " + sampleRate + "\n"
            + "number of channels = " + numChannels + "\n"
            + "info field = " + new String(info).trim();
    }

    /** Write the audio data to an output stream in the Sun audio format.
     *
     *  @param output The output stream.
     *  @exception IOException If an error occurs writing to the stream.
     */
    public void write(DataOutputStream output) throws IOException {
        output.write(magic, 0, 4);
        output.writeInt(offset);
        output.writeInt(size);
        output.writeInt(format);
        output.writeInt(sampleRate);
        output.writeInt(numChannels);
        output.write(info, 0, offset-24);
        output.write(audio[0], 0, size);
    }

    /** Write the raw audio data to an output stream.
     *  This method can be used to play the audio data using the
     *  (undocumented and unsupported) sun.audio package as follows:
     *  <pre>
     *      // The constructor argument below is optional
     *      ByteArrayOutputStream out =
     *               new ByteArrayOutputStream(sound.size);
     *      try {
     *          sound.writeRaw(new DataOutputStream(out));
     *      } catch (IOException ex) {
     *          throw new RuntimeException("Audio output failed");
     *      }
     *      byte[] iobuffer = out.toByteArray();
     *      ByteArrayInputStream instream =
     *              new ByteArrayInputStream(_iobuffer);
     *      AudioPlayer.player.start(instream);
     *  </pre>
     *  The above code assumes we have an sun.audio.AudioData object
     *  called "sound".
     *  Although it would seem reasonable to include a "play" method in this
     *  class to do this, we wish to avoid a dependence on the sun.audio
     *  package in this Ptolemy package, so you will have to implement
     *  the above code yourself.
     *
     *  @param output The output stream.
     *  @exception IOException If an error occurs writing to the stream.
     */
    public void writeRaw(DataOutputStream output) throws IOException {
        output.write(audio[0], 0, size);
    }

    /** Write Sun audio file (.au) format from an array.
     *  The argument is an array of doubles assumed to lie in the range of
     *  -1.0 to 1.0.  The data is converted to one-channel mu-law samples
     *  at 8kHz.
     *
     *  @param audio The audio data, as an array of doubles.
     *  @param output The output stream.
     *  @xception IOException If an I/O error occurs writing to the stream.
     */
    public static void writeAudio(double[] audio, DataOutputStream output)
            throws IOException {
        Audio obj = new Audio(audio);
        obj.write(output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                         ////

    // The following are used for mu-law conversion.
    // Turn on the trap as per the MIL-STD (this prevents a result of 0).
    private static boolean _zerotrap = false;

    // define the add-in bias for 16 bit samples.
    private static final int BIAS = 0x84;

    // clipping value for inputs.
    private static final int CLIP =  32635;

    // lookup table for the exponent.
    private static final byte exp_lut[] = {0, 0, 1, 1, 2, 2, 2, 2, 3,
3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7}; }
