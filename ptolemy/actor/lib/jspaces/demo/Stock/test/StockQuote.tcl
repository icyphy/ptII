# Test StockQuote.
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

    set newObject [java::cast ptolemy.actor.lib.jspaces.demo.Stock.StockQuote [$stock clone]]
    set newTicker [getParameter $newObject ticker]
    set tickerVal [[$newTicker getToken] toString]

    list $tickerVal
} {{"YHOO"}}

# The following tests are commented out because they depend on the stock
# price of YHOO and AOL

######################################################################
#### Test fire
#
test StockQuote-2.1 {get price of YHOO} {
    set e0 [sdfModel 5]
    set stock [java::new ptolemy.actor.lib.jspaces.demo.Stock.StockQuote $e0 stock]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $stock] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    set stockPrices [enumToTokenValues [$rec getRecord 0]]
    set firstPrice [lindex $stockPrices 0]
    set lastPrice [lindex $stockPrices 4]
    # The price should be between 2 and 100 and the first and last price
    # should not be different by more than 10
    list [expr {$firstPrice > 2}] \
	    [expr {$firstPrice < 100}] \
    	    [expr {$firstPrice - $lastPrice > -10}] \
    	    [expr {$firstPrice - $lastPrice < 10}]
} {1 1 1 1}

test StockQuote-2.2 {get price of AOL} {
    set symbol [getParameter $stock ticker]
    $symbol setExpression {"AOL"}
    [$e0 getManager] execute
    set stockPrices [enumToTokenValues [$rec getRecord 0]]
    set firstPrice [lindex $stockPrices 0]
    set lastPrice [lindex $stockPrices 4]
    # The price should be between 10 and 100 and the first and last price
    # should not be different by more than 10
    list [expr {$firstPrice > 10}] \
	    [expr {$firstPrice < 100}] \
    	    [expr {$firstPrice - $lastPrice > -10}] \
    	    [expr {$firstPrice - $lastPrice < 10}]
} {1 1 1 1}

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
} {{ptolemy.kernel.util.IllegalActionException: StockQuote.fire: bad ticker: foobar
  in .top.stock}}

