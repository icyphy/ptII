/* A simple implementation of a 3-digit version specification.

Copyright (c) 2014 The Regents of the University of California; iSencia Belgium NV.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package org.ptolemy.commons;

import java.util.Arrays;

/**
 * A simple implementation of a version specification, based on a merge of OSGi-conventions and Ptolemy (which in turn seems to be based on JNLP).
 * <p>
 * Concretely, this means :
 * <ul>
 * <li>Reuse concept of numeric (int) major.minor.micro version number with optional trailing string-based qualifier</li>
 * <li>Don't care about the concrete concatenated format, possible delimiters etc. (i.e. do not enforce the usage of "."-separated version formatting)</li>
 * <li>Allow an arbitrary count of qualifiers</li>
 * <li>Compare qualifiers using plain text-compare</li>
 * </ul>
 * </p>
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Yellow (ErwinDL)
 */
public class ThreeDigitVersionSpecification extends VersionSpecification implements Comparable<VersionSpecification> {
  private static final long serialVersionUID = -383837397410414307L;

  /**
   * @param major
   * @param minor
   * @param micro
   * @param qualifiers
   */
  public ThreeDigitVersionSpecification(int major, int minor, int micro, String... qualifiers) {
    this._major = major;
    this._minor = minor;
    this._micro = micro;
    if (qualifiers != null) {
      this._qualifiers = qualifiers;
    }
  }

  /**
   *
   * @return the major (leading) version digit
   */
  public int getMajor() {
    return _major;
  }

  /**
   *
   * @return the minor (middle) version digit
   */
  public int getMinor() {
    return _minor;
  }

  /**
   *
   * @return the micro (third) version digit
   */
  public int getMicro() {
    return _micro;
  }

  /**
   *
   * @return the optional array of version qualifiers. When no qualifiers are present, this returns an empty array.
   */
  public String[] getQualifiers() {
    return Arrays.copyOf(_qualifiers, _qualifiers.length);
  }

  /**
   * @return a new version spec with increased micro digit.
   * Does not change the current version spec instance.
   */
  public ThreeDigitVersionSpecification increaseMicro() {
    return new ThreeDigitVersionSpecification(_major, _minor, _micro+1);
  }

  /**
   * @return a new version spec with increased minor digit.
   * Does not change the current version spec instance.
   */
  public ThreeDigitVersionSpecification increaseMinor() {
    return new ThreeDigitVersionSpecification(_major, _minor+1, _micro);
  }

  /**
   * @return a new version spec with increased major digit.
   * Does not change the current version spec instance.
   */
  public ThreeDigitVersionSpecification increaseMajor() {
    return new ThreeDigitVersionSpecification(_major+1, _minor, _micro);
  }

  public int compareTo(VersionSpecification otherVersSpec) {
    if (otherVersSpec == this)
      return 0;
    if(otherVersSpec == null)
      return 1;
    if (otherVersSpec instanceof ThreeDigitVersionSpecification) {
      ThreeDigitVersionSpecification other = (ThreeDigitVersionSpecification) otherVersSpec;
      int result = _major - other._major;
      if (result != 0)
        return result;
      result = _minor - other._minor;
      if (result != 0)
        return result;
      result = _micro - other._micro;
      if (result != 0)
        return result;
      else if (_qualifiers.length > 0) {
        if (other._qualifiers.length > 0) {
          int maxQualifierCount = Math.max(_qualifiers.length, other._qualifiers.length);
          for (int i = 0; i < maxQualifierCount; ++i) {
            String myQualifier = "";
            String otherQualifier = "";
            if (i < _qualifiers.length) {
              myQualifier = _qualifiers[i];
            }
            if (i < other._qualifiers.length) {
              otherQualifier = other._qualifiers[i];
            }
            int cmp = myQualifier.compareTo(otherQualifier);
            if (cmp > 0) {
              return 1;
            } else if (cmp < 0) {
              return -1;
            }
          }
          return 0;
        } else {
          return 1;
        }
      } else if (other._qualifiers.length > 0) {
        return -1;
      } else {
        return 0;
      }
    } else {
      return this._versionString.compareTo(otherVersSpec._versionString);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _major;
    result = prime * result + _micro;
    result = prime * result + _minor;
    result = prime * result + _qualifiers.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ThreeDigitVersionSpecification other = (ThreeDigitVersionSpecification) obj;
    if (_major != other._major)
      return false;
    if (_micro != other._micro)
      return false;
    if (_minor != other._minor)
      return false;
    if (!Arrays.equals(_qualifiers,other._qualifiers))
      return false;
    return true;
  }

  /**
   * Produces a string representation that is itself valid again to be parsed as a VersionSpecification.
   */
  @Override
  public String toString() {
    if (_versionString == null) {
      StringBuilder versionStrBldr = new StringBuilder(_major + "." + _minor + "." + _micro);
      for (String qualifier : _qualifiers) {
        versionStrBldr.append("-" + qualifier);
      }
      _versionString = versionStrBldr.toString();
    }
    return _versionString;
  }


  // private things

  private int _major;
  private int _minor;
  private int _micro;
  /** An optional array of arbitrary version qualifiers */
  private String[] _qualifiers = new String[0];
}
