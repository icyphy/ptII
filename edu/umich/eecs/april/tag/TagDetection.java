/*
Copyright (c) 2013 by the Regents of the University of Michigan.
Developed by the APRIL robotics lab under the direction of Edwin Olson (ebolson@umich.edu).

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation
are those of the authors and should not be interpreted as representing
official policies, either expressed or implied, of the FreeBSD
Project.

This implementation of the AprilTags detector is provided for
convenience as a demonstration.  It is an older version implemented in
Java that has been supplanted by a much better performing C version.
If your application demands better performance, you will need to
replace this implementation with the newer C version and using JNI or
JNA to interface the C version to Java.

For details about the C version, see
https://april.eecs.umich.edu/wiki/index.php/AprilTags-C

 */

package edu.umich.eecs.april.tag;

public class TagDetection {
    /** Is the detection good enough? **/
    public boolean good;

    /** Observed code **/
    public long obsCode;

    /** Matched code **/
    public long code;

    /** What was the ID of the detected tag? **/
    public int id;

    /** The hamming distance between the detected code and the true code. **/
    public int hammingDistance;

    /** How many 90 degree rotations were required to align the code. **/
    public int rotation;

    ////////////////// Fields below here are filled in by TagDetector ////////////////
    /** Position (in fractional pixel coordinates) of the detection. The
     * points travel around counter clockwise around the target,
     * always starting from the same corner of the tag. Dimensions
     * [4][2]. **/
    public double p[][];

    /** Center of tag in pixel coordinates. **/
    public double cxy[];

    /** Measured in pixels, how long was the observed perimeter
     * (i.e., excluding inferred perimeter which is used to
     * connect incomplete quads) **/
    public double observedPerimeter;

    /** A 3x3 homography that computes pixel coordinates from
     * tag-relative coordinates. Both the input and output coordinates
     * are 2D homogenous vectors, with y = Hx. y are pixel
     * coordinates, x are tag-relative coordinates. Tag coordinates
     * span from (-1,-1) to (1,1). The orientation of the homography
     * reflects the orientation of the target. **/
    public double homography[][];

    /** The homography is relative to image center, whose coordinates
     * are below.
     **/
    public double hxy[];

    /** interpolate point given (x,y) is in tag coordinate space from (-1,-1) to (1,1) **/
    public double[] interpolate(double x, double y) {
        double z = homography[2][0] * x + homography[2][1] * y
                + homography[2][2];
        return new double[] {
                (homography[0][0] * x + homography[0][1] * y + homography[0][2])
                        / z + hxy[0],
                (homography[1][0] * x + homography[1][1] * y + homography[1][2])
                        / z + hxy[1] };
    }

    public String toString() {
        return String
                .format("[TagDetection code 0x%010x   id=%-5d   errors=%d   position =  (%8.2f,%8.2f) @ %3d deg]",
                        code, id, hammingDistance, cxy[0], cxy[1],
                        rotation * 90);
    }
}
