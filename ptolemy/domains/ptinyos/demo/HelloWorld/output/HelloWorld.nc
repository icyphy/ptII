configuration HelloWorld {
}
implementation {
components NCComponent, NCComponent as NCComponent2, NCComponent as NCComponent3, CompositeActor as NCCompositeActor;
NCComponent.output -> NCComponent2.input;
NCComponent3.init -> NCCompositeActor.init;
}
