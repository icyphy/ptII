/* An actor that reads from images using Open Computer Vision (OpenCV)

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

package ptolemy.actor.lib.opencv.javacv;

import name.audet.samuel.javacv.jna.cxcore.CvSeq;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// Draw circles for the result sequence data

/**
 * Draw result
  * @author Tatsuaki Iwata, Edward A. Lee, Jan Reineke, Christopher Brooks
 * @version
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ChooseSeq extends Transformer {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>init</i> and <i>step</i> parameter and the <i>step</i>
     *  port. Initialize <i>init</i>
     *  to IntToken with value 0, and <i>step</i> to IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ChooseSeq(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output1 = new TypedIOPort(this, "output1", false, true);
        output.setTypeEquals(BaseType.OBJECT);
        output1.setTypeEquals(BaseType.INT);

        pathName = new StringAttribute(this, "pathName");
        pathName.setExpression("haarcascade_frontalface_default.xml");

        seq1 = new TypedIOPort(this, "sequence1", true, false);
        seq1.setTypeEquals(BaseType.OBJECT);

        seq2 = new TypedIOPort(this, "sequence2", true, false);
        seq2.setTypeEquals(BaseType.OBJECT);

        seq3 = new TypedIOPort(this, "sequence3", true, false);
        seq3.setTypeEquals(BaseType.OBJECT);

        seq4 = new TypedIOPort(this, "sequence4", true, false);
        seq4.setTypeEquals(BaseType.OBJECT);

        seq5 = new TypedIOPort(this, "sequence5", true, false);
        seq5.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** The name of the file to write to. The default
     *  value of this parameter is "haarcascade_frontalface_default.xml"
     */
    public StringAttribute pathName;

    /** The input sequence. */
    public TypedIOPort seq1, seq2, seq3, seq4, seq5, output1;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    public void fire() throws IllegalActionException {

        if (seq1.hasToken(0)) {
            ObjectToken seqToken1 = (ObjectToken) seq1.get(0);
            Object seqObject1 = seqToken1.getValue();
            if (!(seqObject1 instanceof CvSeq)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + seqObject1.getClass());
            }
            _objectSeq = (CvSeq) seqObject1;

            rotation_angle = 0;

        }

        if (seq2.hasToken(0)) {
            ObjectToken seqToken2 = (ObjectToken) seq2.get(0);
            Object seqObject2 = seqToken2.getValue();
            if (!(seqObject2 instanceof CvSeq)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + seqObject2.getClass());
            }
            CvSeq tester = (CvSeq) seqObject2;
            int total = tester.total;
            if (total != 0) {
                _objectSeq = tester;
                rotation_angle = 25;
            }

        }

        if (seq3.hasToken(0)) {
            ObjectToken seqToken3 = (ObjectToken) seq3.get(0);
            Object seqObject3 = seqToken3.getValue();
            if (!(seqObject3 instanceof CvSeq)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + seqObject3.getClass());
            }

            CvSeq tester = (CvSeq) seqObject3;
            int total = tester.total;
            if (total != 0) {
                _objectSeq = tester;
                rotation_angle = 50;
            }

        }

        if (seq4.hasToken(0)) {
            ObjectToken seqToken4 = (ObjectToken) seq4.get(0);
            Object seqObject4 = seqToken4.getValue();
            if (!(seqObject4 instanceof CvSeq)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + seqObject4.getClass());
            }

            CvSeq tester = (CvSeq) seqObject4;
            int total = tester.total;
            if (total != 0) {
                _objectSeq = tester;
                rotation_angle = -25;
            }

        }

        if (seq5.hasToken(0)) {
            ObjectToken seqToken5 = (ObjectToken) seq5.get(0);
            Object seqObject5 = seqToken5.getValue();
            if (!(seqObject5 instanceof CvSeq)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + seqObject5.getClass());
            }

            CvSeq tester = (CvSeq) seqObject5;
            int total = tester.total;
            if (total != 0) {
                _objectSeq = tester;
                rotation_angle = -50;
            }

        }

        output.send(0, new ObjectToken(_objectSeq));
        output1.send(0, new IntToken(rotation_angle));

    }

    /** Initialize internal frame.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private CvSeq _objectSeq;
    private int rotation_angle;

}
