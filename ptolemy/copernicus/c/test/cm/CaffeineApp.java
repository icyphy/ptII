/*
  @Copyright (c) 2003-2005 The Regents of the University of California.
  All rights reserved.

  Permission is hereby granted, without written agreement and without
  license or royalty fees, to use, copy, modify, and distribute this
  software and its documentation for any purpose, provided that the
  above copyright notice and the following two paragraphs appear in all
  copies of this software.

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
public class CaffeineApp {
    public static void main(String[] args) {
        float total = 1;

        BenchmarkUnit[] units = new BenchmarkUnit[6];
        units[0] = new BenchmarkUnit(new SieveAtom());
        units[1] = new BenchmarkUnit(new LoopAtom());
        units[2] = new BenchmarkUnit(new LogicAtom());
        units[3] = new BenchmarkUnit(new StringAtom());
        units[4] = new BenchmarkUnit(new FloatAtom());
        units[5] = new BenchmarkUnit(new MethodAtom());

        for (int i = 0; i < 6; i++) {
            BenchmarkUnit unit = units[i];
            int score = unit.testScore();
            System.out.print(unit.testName());
            System.out.print(" score = ");
            System.out.println(score);

            // We need the geometric mean.
            total = total * (float) Math.exp(Math.log(score) / 6);
        }

        System.out.print("Overall score = ");
        System.out.println((int) total);
    }
}
