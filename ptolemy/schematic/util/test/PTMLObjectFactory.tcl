# Tests for the PTMLObjectFactory class
#
# @Author: Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#
test PTMLObjectFactory-2.1 {Constructor tests} {
    set parser [java::new ptolemy.schematic.xml.PTMLParser]
    set fileend [file join $PTII ptolemy schematic util test exampleIconLibrary.ptml]
    set filename "file:"
    append filename $fileend
    set xmllib [$parser parse $filename]
    set iconlib [java::call ptolemy.schematic.util.PTMLObjectFactory \
	    createIconLibrary $xmllib]
    $iconlib description
} {ptolemy.schematic.util.IconLibrary {SDF} parameters {
} sublibraries {
} icons {
    {ptolemy.schematic.util.Icon {LoadImage} parameters {
    } graphics {
        {ptolemy.schematic.util.GraphicElement {rectangle} attributes {
            {content=}
            {color=red}
            {fill=pink}
            {coords=0 0 60 40}
        }}
        {ptolemy.schematic.util.GraphicElement {polygon} attributes {
            {content=}
            {coords=10 10 50 30 10 30 50 10}
            {fill=blue}
            {color=black}
        }}
        {ptolemy.schematic.util.GraphicElement {ellipse} attributes {
            {content=}
            {fill=yellow}
            {coords=25 15 10 10}
            {color=black}
        }}
        {ptolemy.schematic.util.GraphicElement {line} attributes {
            {content=}
            {coords=30 20 60 20}
        }}
    }}
    {ptolemy.schematic.util.Icon {SaveImage} parameters {
    } graphics {
        {ptolemy.schematic.util.GraphicElement {rectangle} attributes {
            {content=}
            {color=red}
            {fill=orange}
            {coords=0 0 60 40}
        }}
        {ptolemy.schematic.util.GraphicElement {polygon} attributes {
            {content=}
            {coords=10 10 50 30 10 30 50 10}
            {fill=blue}
            {color=black}
        }}
        {ptolemy.schematic.util.GraphicElement {ellipse} attributes {
            {content=}
            {fill=yellow}
            {coords=25 15 10 10}
            {color=black}
        }}
        {ptolemy.schematic.util.GraphicElement {line} attributes {
            {content=}
            {coords=0 20 30 20}
        }}
    }}
} terminalstyles{
    {ptolemy.schematic.util.TerminalStyle {1out} parameters {
    } terminals {
        {ptolemy.schematic.util.Terminal {output} parameters {
        } X {64.0} Y {20.0}}
    }}
    {ptolemy.schematic.util.TerminalStyle {1in} parameters {
    } terminals {
        {ptolemy.schematic.util.Terminal {input} parameters {
        } X {-4.0} Y {20.0}}
    }}
}}

######################################################################
####
#
test PTMLObjectFactory-2.2 {Constructor tests} {
    set parser [java::new ptolemy.schematic.xml.PTMLParser]
    set fileend [file join $PTII ptolemy schematic util test exampleRootIconLibrary.ptml]
    set filename "file:"
    append filename $fileend
    set xmllib [$parser parse $filename]
    set iconroot [java::call ptolemy.schematic.util.PTMLObjectFactory createIconLibrary $xmllib]

    set fileend [file join $PTII ptolemy schematic util test exampleEntityLibrary.ptml]
    set filename "file:"
    append filename $fileend
    set xmllib [$parser parse $filename]
    set entitylib [java::call ptolemy.schematic.util.PTMLObjectFactory \
	    createEntityLibrary $xmllib $iconroot]
    $entitylib description
} {ptolemy.schematic.util.EntityLibrary {SDF} parameters {
} sublibraries {
} entites {
    {ptolemy.schematic.util.EntityTemplate {LoadImage} parameters {
    } icon {
        ptolemy.schematic.util.Icon {LoadImage} parameters {
        } graphics {
            {ptolemy.schematic.util.GraphicElement {rectangle} attributes {
                {content=}
                {color=red}
                {fill=pink}
                {coords=0 0 60 40}
            }}
            {ptolemy.schematic.util.GraphicElement {polygon} attributes {
                {content=}
                {coords=10 10 50 30 10 30 50 10}
                {fill=blue}
                {color=black}
            }}
            {ptolemy.schematic.util.GraphicElement {ellipse} attributes {
                {content=}
                {fill=yellow}
                {coords=25 15 10 10}
                {color=black}
            }}
            {ptolemy.schematic.util.GraphicElement {line} attributes {
                {content=}
                {coords=30 20 60 20}
            }}
        }
    } terminalstyle {
        ptolemy.schematic.util.TerminalStyle {1out} parameters {
        } terminals {
            {ptolemy.schematic.util.Terminal {output} parameters {
            } X {64.0} Y {20.0}}
        }
    } ports {
    }}
    {ptolemy.schematic.util.EntityTemplate {SaveImage} parameters {
    } icon {
        ptolemy.schematic.util.Icon {SaveImage} parameters {
        } graphics {
            {ptolemy.schematic.util.GraphicElement {rectangle} attributes {
                {content=}
                {color=red}
                {fill=orange}
                {coords=0 0 60 40}
            }}
            {ptolemy.schematic.util.GraphicElement {polygon} attributes {
                {content=}
                {coords=10 10 50 30 10 30 50 10}
                {fill=blue}
                {color=black}
            }}
            {ptolemy.schematic.util.GraphicElement {ellipse} attributes {
                {content=}
                {fill=yellow}
                {coords=25 15 10 10}
                {color=black}
            }}
            {ptolemy.schematic.util.GraphicElement {line} attributes {
                {content=}
                {coords=0 20 30 20}
            }}
        }
    } terminalstyle {
        ptolemy.schematic.util.TerminalStyle {1in} parameters {
        } terminals {
            {ptolemy.schematic.util.Terminal {input} parameters {
            } X {-4.0} Y {20.0}}
        }
    } ports {
    }}
}}


######################################################################
####
#
test PTMLObjectFactory-2.3 {Constructor tests} {
    set parser [java::new ptolemy.schematic.xml.PTMLParser]

    set fileend [file join $PTII ptolemy schematic util test exampleRootEntityLibrary.ptml]
    set filename "file:"
    append filename $fileend
    set xmllib [$parser parse $filename]
    set entityroot [java::call ptolemy.schematic.util.PTMLObjectFactory \
	    createEntityLibrary $xmllib $iconroot]

    set fileend [file join $PTII ptolemy schematic util test exampleschematic.ptml]
    set filename "file:"
    append filename $fileend
    set xmllib [$parser parse $filename]
    set schematic [java::call ptolemy.schematic.util.PTMLObjectFactory createSchematic $xmllib $entityroot]

    $schematic description
} {ptolemy.schematic.util.Schematic {SDF} parameters {
} entities {
    {ptolemy.schematic.util.SchematicEntity {Load BMP File} parameters {
    } template {
        ptolemy.schematic.util.EntityTemplate {LoadImage} parameters {
        } icon {
            ptolemy.schematic.util.Icon {LoadImage} parameters {
            } graphics {
                {ptolemy.schematic.util.GraphicElement {rectangle} attributes {
                    {content=}
                    {color=red}
                    {fill=pink}
                    {coords=0 0 60 40}
                }}
                {ptolemy.schematic.util.GraphicElement {polygon} attributes {
                    {content=}
                    {coords=10 10 50 30 10 30 50 10}
                    {fill=blue}
                    {color=black}
                }}
                {ptolemy.schematic.util.GraphicElement {ellipse} attributes {
                    {content=}
                    {fill=yellow}
                    {coords=25 15 10 10}
                    {color=black}
                }}
                {ptolemy.schematic.util.GraphicElement {line} attributes {
                    {content=}
                    {coords=30 20 60 20}
                }}
            }
        } terminalstyle {
            ptolemy.schematic.util.TerminalStyle {1out} parameters {
            } terminals {
                {ptolemy.schematic.util.Terminal {output} parameters {
                } X {64.0} Y {20.0}}
            }
        } ports {
        }
    } terminalstyle {
        ptolemy.schematic.util.TerminalStyle {1out} parameters {
        } terminals {
            {ptolemy.schematic.util.Terminal {output} parameters {
            } X {64.0} Y {20.0}}
        }
    } ports {
    } terminals {
        {ptolemy.schematic.util.SchematicTerminal {output} parameters {
        } template {
            ptolemy.schematic.util.Terminal {output} parameters {
            } X {64.0} Y {20.0}
        } X {64.0} Y {20.0}}
    }}
    {ptolemy.schematic.util.SchematicEntity {Save BMP File} parameters {
    } template {
        ptolemy.schematic.util.EntityTemplate {SaveImage} parameters {
        } icon {
            ptolemy.schematic.util.Icon {SaveImage} parameters {
            } graphics {
                {ptolemy.schematic.util.GraphicElement {rectangle} attributes {
                    {content=}
                    {color=red}
                    {fill=orange}
                    {coords=0 0 60 40}
                }}
                {ptolemy.schematic.util.GraphicElement {polygon} attributes {
                    {content=}
                    {coords=10 10 50 30 10 30 50 10}
                    {fill=blue}
                    {color=black}
                }}
                {ptolemy.schematic.util.GraphicElement {ellipse} attributes {
                    {content=}
                    {fill=yellow}
                    {coords=25 15 10 10}
                    {color=black}
                }}
                {ptolemy.schematic.util.GraphicElement {line} attributes {
                    {content=}
                    {coords=0 20 30 20}
                }}
            }
        } terminalstyle {
            ptolemy.schematic.util.TerminalStyle {1in} parameters {
            } terminals {
                {ptolemy.schematic.util.Terminal {input} parameters {
                } X {-4.0} Y {20.0}}
            }
        } ports {
        }
    } terminalstyle {
        ptolemy.schematic.util.TerminalStyle {1in} parameters {
        } terminals {
            {ptolemy.schematic.util.Terminal {input} parameters {
            } X {-4.0} Y {20.0}}
        }
    } ports {
    } terminals {
        {ptolemy.schematic.util.SchematicTerminal {input} parameters {
        } template {
            ptolemy.schematic.util.Terminal {input} parameters {
            } X {-4.0} Y {20.0}
        } X {-4.0} Y {20.0}}
    }}
} ports {
} terminals {
} relations {
    {ptolemy.schematic.util.SchematicRelation {R1} parameters {
    } terminals {
    } links {
        {ptolemy.schematic.util.SchematicLink
         to {
            ptolemy.schematic.util.SchematicTerminal {output} parameters {
            } template {
                ptolemy.schematic.util.Terminal {output} parameters {
                } X {64.0} Y {20.0}
            } X {64.0} Y {20.0}
        } from {
            ptolemy.schematic.util.SchematicTerminal {input} parameters {
            } template {
                ptolemy.schematic.util.Terminal {input} parameters {
                } X {-4.0} Y {20.0}
            } X {-4.0} Y {20.0}
        }}
    }}
}}

