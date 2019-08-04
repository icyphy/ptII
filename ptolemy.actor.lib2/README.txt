This is not a project to be used in a real production environment, but just a show-by-example bundle
to contribute extra actors to an OSGi/eclipse-based Ptolemy II runtime (e.g. Triquetrum).

The special thing is that Ptolemy II provides a large and ever-growing collection of actors in a single Java package ptolemy.actor.lib.
This does not play well with modular maintenance and evolution/extension in an OSGi runtime, where we would like to be able to add new actors
in new/extra bundles without always impacting the default ptolemy.actor.lib. Especially as that has to go through the Eclipse IP process to
allow it to be delivered with Triquetrum.

To prevent issues with reuse of package names across bundles, a possible solution is to rename the actors' Java package
and to use class-name aliases in the ModelElementClassProvider implementation registered in the Activator.

TODO : move this AliasModelElementClassProvider to the standard org.ptolemy.classloading package in ptolemy.core,
for the next Triquetrum release.