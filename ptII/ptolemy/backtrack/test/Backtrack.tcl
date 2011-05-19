# Tests for the Backtracking
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

#
# Invoke the main method of className and return the results of stdout.
proc backtrack {className} {
    jdkCapture {    	
        if [catch {
		java::call ptolemy.backtrack.test.$className main \
		    [java::new {String[]} 0]} errMsg] {
	    puts $errMsg
	}
    } results
    return $results
}

puts "Running 'make backtrack' to run Transformer"
if [catch {set results [exec -stderrok make backtrack]} errMsg] {
    error $errMsg	
} else {
    puts $results	
}


######################################################################
####
#
test Backtrack-1.1 {Test1Main} {
    backtrack Test1Main
} {0 1 2 3 3 2 1 0 
}

######################################################################
####
#
test Backtrack-2.1 {Test2Main} {
    backtrack Test2Main
} {1 1 2 2 3 3 28 28 37 37 6 6 7 7 
}

######################################################################
####
#
test Backtrack-3.1 {ArrayTest1Main} {
    backtrack ArrayTest1Main
} {   0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17  18  19
  20  21  22  23  24  25  26  27  28  29  30  31  32  33  34  35  36  37  38  39
  40  41  42  43  44  45  46  47  48  49  50  51  52  53  54  55  56  57  58  59
  60  61  62  63  64  65  66  67  68  69  70  71  72  73  74  75  76  77  78  79
  80  81  82  83  84  85  86  87  88  89  90  91  92  93  94  95  96  97  98  99
 100 101 102 103 104 105 106 107 108 109 110 111 112 113 114 115 116 117 118 119
 120 121 122 123 124 125 126 127 128 129 130 131 132 133 134 135 136 137 138 139
 140 141 142 143 144 145 146 147 148 149 150 151 152 153 154 155 156 157 158 159
 160 161 162 163 164 165 166 167 168 169 170 171 172 173 174 175 176 177 178 179
 180 181 182 183 184 185 186 187 188 189 190 191 192 193 194 195 196 197 198 199
}

######################################################################
####
#
test Backtrack-4.1 {RandomTest1Main} {
    backtrack RandomTest1Main
} {0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 
}
