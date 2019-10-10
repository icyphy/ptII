$Id: README.txt 2018-03-27 updated 2019-10-10 cardoso@isae.fr $


Below a list of all models about time management:
- TimeAdvancing1FederateTAR
- TimeAdvancing1FederateNER
- TimeAdvancing2FederatesNerTar
- TimeAdvancing2FederatesIntervalEvents

The initial goal of the models below was to show the time advance when there
is no data exchanged, and no internal events. But then the automated
launching of the federation with VisualModelReference could not show anything.
That is why a TimedPlotter was inserted with a SingleEvent at StopTime was added.

- TimeAdvancing2FederatesNER: If stopTime < 8 in Fed1NER, it goes imediatly to stopTime. 
But if it is > 8, it synchronizes with real time.
=> Possible explanation: there is no data exchange, so there is no event in the queue for asking time advance.

- TimeAdvancing2FederatesNERbis: If the TimedPlotter is put in the federate that is synchronized with real time (Fed2NER), then the behavior is as we would expect. 


