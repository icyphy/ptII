/* This class provides a wrapper for the x-y-z coordinates in a 3d space.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (lmuliadi@eecs.berkeley.edu)
*/


package ptolemy.domains.de.demo.mems.lib;

import ptolemy.domains.de.demo.mems.lib.*;

//////////////////////////////////////////////////////////////////////////
//// Coord
/**
A wrapper for the xyz coordinate values in the three dimensional space.

@author Allen Miu, Lukito Muliadi
@version $Id$
*/

public class Coord {
  private double _x, _y, _z;
  
  public Coord(double x, double y, double z) {
    _x = x; _y = y; _z = z;
  }
  
  public double dist(double x, double y, double z) {
    double dx = (x-_x);
    double dy = (y-_y);
    double dz = (z-_z);

    double result = Math.sqrt(dx*dx + dy*dy + dz*dz);
    Debug.log(2, "Coord: measured distance = " + result);
    return result;
  }

  public double dist(Coord other) {
    return dist(other.getX(), other.getY(), other.getZ());
  }

  public double getX() { return _x; }
  public double getY() { return _y; }
  public double getZ() { return _z; }

}
