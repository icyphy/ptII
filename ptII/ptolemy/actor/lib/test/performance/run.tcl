set filename $argv
set parser [java::new ptolemy.moml.MoMLParser]
$parser resetAll
set toplevel [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile $filename]]
set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "manager"]
$toplevel setManager $manager
puts "[time {$manager execute} 1]"
puts "[time {$manager execute} 4]"
