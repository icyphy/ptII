/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
/**
 *
 */
package ptolemy.domains.coroutine.kernel;

/**
 * @author shaver
@version $Id$
@since Ptolemy II 10.0
 *
 */
public class ControlEntryToken extends ControlToken {

    private ControlEntryToken() {
        _type = ControlType.Non;
        _location = null;
    }

    public static ControlEntryToken EntryToken(String etS) {
        if (etS.contentEquals("init")) {
            return Init();
        }
        if (etS.contentEquals("resume")) {
            return Resume();
        }
        return Enter(new EntryLocation(etS));
    }

    public static ControlEntryToken Init() {
        ControlEntryToken ct = new ControlEntryToken();
        ct._setEntry(ControlType.Init, new EntryLocation("init"));
        return ct;
    }

    public static ControlEntryToken Resume() {
        ControlEntryToken ct = new ControlEntryToken();
        ct._setEntry(ControlType.Resume, new EntryLocation("init"));
        return ct;
    }

    public static ControlEntryToken Enter(EntryLocation l) {
        ControlEntryToken ct = new ControlEntryToken();
        ct._setEntry(ControlType.Enter, l);
        return ct;
    }

    public enum ControlType {
        Non, Init, Resume, Enter
    };

    @Override
    public boolean isEntry() {
        return true;
    }

    @Override
    public boolean isExit() {
        return false;
    }

    public boolean isInit() {
        return _type == ControlType.Init;
    }

    public boolean isResume() {
        return _type == ControlType.Resume;
    }

    public boolean isLocation() {
        return _type == ControlType.Enter;
    }

    public ControlEntryToken.EntryLocation getLocation() {
        return _location;
    }

    public static class EntryLocation implements Location {
        public EntryLocation(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public String name;

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (name == null ? 0 : name.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            EntryLocation other = (EntryLocation) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

    }

    ////

    @Override
    public String toString() {
        String str = "Non";
        /**/if (_type == ControlType.Init) {
            str = "Init";
        } else if (_type == ControlType.Resume) {
            str = "Resume";
        } else if (_type == ControlType.Enter) {
            str = "Enter[";
            str += _location.toString();
            str += "]";
        }

        return str;
    }

    ////

    private void _setEntry(ControlType t, EntryLocation l) {
        _type = t;
        _location = l;
    }

    private ControlType _type;
    private EntryLocation _location;

}
