# Simple standalone clone test

source testDefs.tcl
set workspace [java::new ptolemy.kernel.util.Workspace]
set compositeEntity [java::new ptolemy.kernel.CompositeEntity $workspace]
$compositeEntity setName Top
#set actor [java::new ptolemy.actor.lib.colt.ColtZeta $compositeEntity "Zeta"]
set actor [java::new ptolemy.actor.lib.Gaussian $compositeEntity "Gaussian"]
set clone [java::cast ptolemy.actor.TypedAtomicActor [$actor clone $workspace]]
set constraints [$actor typeConstraintList]
set c [jdkPrintArray [$constraints toArray] "\n" ]

set cloneConstraints [$clone typeConstraintList]
set cc [jdkPrintArray [$cloneConstraints toArray] "\n" ]

puts "$c"
puts "$cc"
