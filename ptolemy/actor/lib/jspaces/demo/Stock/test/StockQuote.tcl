# Test StockQuote.
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

######################################################################
####
#
test StockQuote-1.1 {test clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]


    set stock [java::new ptolemy.actor.lib.jspaces.demo.Stock.StockQuote $e0 stock]

    set newobj [java::cast ptolemy.actor.lib.jspaces.demo.Stock.StockQuote [$stock clone]]
    set newTicker [getParameter $newobj ticker]
    set tickerVal [[$newTicker getToken] toString]

    list $tickerVal
} {YHOO}

# The following tests are commented out because they depend on the stock
# price of YHOO and AOL

######################################################################
#### Test fire
#
# test StockQuote-2.1 {get price of YHOO} {
#     set e0 [sdfModel 5]
#     set stock [java::new ptolemy.actor.lib.jspaces.demo.Stock.StockQuote $e0 stock]
#     set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
#     $e0 connect \
#             [java::field [java::cast ptolemy.actor.lib.Source $stock] output] \
#             [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
#     [$e0 getManager] execute
#     enumToTokenValues [$rec getRecord 0]
# } {54 54 54 54 54}
# 
# test StockQuote-2.2 {get price of Lucent} {
#     set symbol [getParameter $stock ticker]
#     $symbol setExpression {"AOL"}
#     [$e0 getManager] execute
#     enumToTokenValues [$rec getRecord 0]
# } {71 71 71 71 71}

######################################################################
#### Use bogus ticker
#
test StockQuote-3.1 {Use a bogus ticker} {
    set e0 [sdfModel 5]
    set stock [java::new ptolemy.actor.lib.jspaces.demo.Stock.StockQuote $e0 stock]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $stock] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set symbol [getParameter $stock ticker]
    $symbol setExpression {"foobar"}
    catch {[$e0 getManager] execute} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .top.stock:
StockQuote.fire: bad ticker: foobar}}

