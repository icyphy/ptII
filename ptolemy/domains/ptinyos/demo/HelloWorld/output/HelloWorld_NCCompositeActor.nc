configuration NCCompositeActor {
provides interface init;
uses interface output;
}
implementation {
components NCComponent, NCComponent as NCComponent2;
NCComponent2.output -> NCComponent.input;
init = NCComponent2.init;
NCComponent.out = output;
}
