/***preinitBlock***/
#include "audio.h"
#include "inc/hw_pwm.h"
#include "driverlib/pwm.h"
#define SONG_MEASURES 8
static unsigned int $actorSymbol(result) = 0;
unsigned int song[SONG_MEASURES<<3] = {G3, G3, G3, G3, SILENCE, SILENCE, SILENCE, G3, D4, D4, D4, D4, SILENCE, SILENCE, SILENCE, D4, G4, G4, G4, G4, SILENCE, G4, F4, DS4, F4, DS4, D4, D4, SILENCE, SILENCE, SILENCE, D4, C4, D4, DS4, DS4, SILENCE, DS4, D4, C4, AS3, C4, D4, D4, SILENCE, D4, C4, AS3, A3, B3, CS4, CS4, SILENCE, CS4, B3, CS4, D4, D4, D3, D3, SILENCE, SILENCE, SILENCE, SILENCE};
/**/

/*** sharedBlock ***/
//GPIOA_Transmitter sharedBlock
/**/

/*** initBlock ***/
//GPIOA_Transmitter initBlock
/**/

/*** initializeAudioOutput ***/
$actorSymbol(result) = 0;
/**/

/*** fireBlock ***/
$put(output#0, g_sysClock / (song[$actorSymbol(result)++]<<3))
if ($actorSymbol(result) >= (SONG_MEASURES<<3))
    $actorSymbol(result) = 0;

/**/

