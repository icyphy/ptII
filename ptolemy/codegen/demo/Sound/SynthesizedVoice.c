// Starting ptolemy.codegen.kernel.StaticSchedulingCodeGenerator {.SynthesizedVoice.StaticSchedulingCodeGenerator} code generation.
#include "SDL_audio.h"
#include "SDL_thread.h"
#include <math.h>
#include <time.h>
#include "SDL.h"
#include "math.h"
#include <stdio.h>
/* Generate shared code for .SynthesizedVoice */

    struct AudioReader_soundBuffer {
        SDL_AudioSpec spec;
        Uint8   *sound;             /* Pointer to wave data */
        Uint32   soundLength;       /* Length of wave data */
        int      soundPosition;     /* Current read position */
    };

    int Gaussian_next(int bits, double* seed) {
        *seed = (((long long) *seed * 0x5DEECE66DLL) + 0xBLL) & ((1LL << 48) - 1);
        return (int)((signed long long) *seed >> (48 - bits));
    }
    
    double Gaussian_nextDouble(double* seed) {
        return (((long long)Gaussian_next(26, seed) << 27) + Gaussian_next(27, seed)) / (double)(1LL << 53);
    }

    struct AudioPlayer_sample {
        Uint8 *data;            /* Pointer to wave data */
        Uint32 dataPosition;    /* Position of the next data to be mixed */
        Uint32 dataLength;      /* Length of wave data */
    };
    
    // FIXME: how should we determine the buffer size??
    #define AudioPlayer_BUFFER_SIZE 32500   // ~50 KB buffer size

    // FIXME: what should we set the audio buffer size in samples equals to??
    #define AudioPlayer_SAMPLE_BUFFER_SIZE 16384
    
    double AudioPlayer_clip (double num) {
        return num > 1.0 ? 1.0 : num < -1.0 ? -1.0 : num;
    }
/* Finished generate shared code for .SynthesizedVoice */
/* Generate type resolution code for .SynthesizedVoice */

#define MISSING 0
#define boolean unsigned char
#define false 0
#define true 1
#define TYPE_String 0
#define TYPE_Array 1
#define TYPE_Double 2
#define TYPE_Boolean 3
#define TYPE_Int 4
#define FUNC_convert 0
typedef struct token Token;
typedef char* StringToken;

struct array {
    int size;           // size of the array.
    Token* elements;    // array of Token elements.
};
typedef struct array* ArrayToken;

typedef double DoubleToken;

typedef char BooleanToken;

typedef int IntToken;

struct token {                  // Base type for tokens.
    unsigned char type;         // TYPE field has to be the first field.
    union typeMembers {
        // type member declarations [i.e. Type1Token Type1;]
		StringToken String;
		ArrayToken Array;
		DoubleToken Double;
		BooleanToken Boolean;
		IntToken Int;
                 
    } payload;
};

Token String_convert(Token token);
Token String_print(Token thisToken);

Token Array_convert(Token token);
Token Array_print(Token thisToken);

Token Array_get(Token token, int i) {   
    return token.payload.Array->elements[i];
}

Token Double_convert(Token token);
Token Double_print(Token thisToken);

Token Boolean_convert(Token token);
Token Boolean_print(Token thisToken);

Token Int_convert(Token token);
Token Int_print(Token thisToken);

//int atoi (char* s);             // standard c function.
//double atof (char* s);          // standard c function.
//long atol (char* s);            // standard c function.
    
char* itoa (int i) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%d", i);
    return string;       
}

char* ltoa (long l) {
    char* string = (char*) malloc(sizeof(char) * 22);
    sprintf((char*) string, "%d", l);
    return string;       
}

char* ftoa (double d) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%g", d);
    return string;       
}

char* btoa (char b) {
    if (b) {
        return "true";
    } else {
        return "false";
    }
}

int ftoi (double d) {
    return floor(d);
}

double itof (int i) {
    return (double) i;
}
#define NUM_TYPE 5
#define NUM_FUNC 1
Token (*functionTable[NUM_TYPE][NUM_FUNC])(Token)= {
	String_convert, 
	Array_convert, 
	Double_convert, 
	Boolean_convert, 
	Int_convert
};

// make a new integer token from the given value.
Token String_new(char* s) {
    Token result;
    result.type = TYPE_String;
    result.payload.String = s;
    return result;
}

Token String_convert(Token token) {
    char* stringPointer;
    switch (token.type) {
        #ifdef TYPE_String
            case TYPE_Int:
                stringPointer = (char*) malloc(sizeof(char) * 12);
                sprintf(stringPointer, "%d", token.payload.Int);
                token.payload.String = stringPointer;
                break;
        #endif

        #ifdef TYPE_String
            case TYPE_Double:
                stringPointer = (char*) malloc(sizeof(char) * 12);
                sprintf(stringPointer, "%g", token.payload.Double);
                token.payload.String = stringPointer;
                break;
        #endif

        default:
            // FIXME: not finished
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }
    return token;
}    

// make a new array from the given values
// assume that number of the rest of the arguments == length,
// and they are in the form of (element, element, ...).
Token Array_new(int size, int given, ...) {   
    int i;
    char elementType;
    Token* element;
    boolean doConvert = false;

    Token result;
    result.type = TYPE_Array;
    result.payload.Array = (ArrayToken) malloc(sizeof(struct array));
    result.payload.Array->size = size;
    if (given > 0) {
        element = (Token*) (&size + 2);
        elementType = element->type;
    }

    // Allocate an new array of Tokens.
    result.payload.Array->elements = (Token*) calloc(size, sizeof(Token));
    for (i = 0; i < given; i++, element++) {
        if (element->type != elementType) {
            doConvert = true;

            // Get the max type.
            if (element->type > elementType) {
                elementType = element->type;
            }
        }
        result.payload.Array->elements[i] = *element;
    }
    
    // If elements are not of the same type, 
    // convert all the elements to the max type.
    if (doConvert) {
        for (i = 0; i < given; i++) {
            result.payload.Array->elements[i] = (Token) functionTable[elementType][FUNC_convert](result.payload.Array->elements[i]);
        }
    }
    return result;
}    

Token Array_convert(Token token) {
    Token oldToken = token;
    Token result = token;    // return the old pointer by default.

    switch (token.type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                result = Array_new(1, TYPE_Int, token);
                break;
        #endif
        
        #ifdef TYPE_Double
            case TYPE_Double:
                result = Array_new(1, TYPE_Double, token);
                break;
        #endif
        
        #ifdef TYPE_String
            case TYPE_String:
                result = Array_new(1, TYPE_String, token);
                break;
        #endif
        
        default:
            // FIXME: not finished
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }
    return result;
}    

// make a new integer token from the given value.
Token Double_new(double d) {
    Token result;
    result.type = TYPE_Double;
    result.payload.Double = d;
    return result;
}

Token Double_convert(Token token) {
    switch (token.type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                token.type = TYPE_Double;
                token.payload.Double = (double) token.payload.Int;
                break;
        #endif

        // FIXME: not finished
        default:
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }
    token.type = TYPE_Double;
    return token;
}

// make a new integer token from the given value.
Token Boolean_new(char b) {
    Token result;
    result.type = TYPE_Boolean;
    result.payload.Boolean = b;
    return result;
}

Token Boolean_convert(Token token) {
    switch (token.type) {
        // FIXME: not finished
        default:
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }    
    token.type = TYPE_Boolean;
    return token;
}    

// make a new integer token from the given value.
Token Int_new(int i) {
    Token result;
    result.type = TYPE_Int;
    result.payload.Int = i;
    return result;
}

Token Int_convert(Token token) {
    switch (token.type) {
        #ifdef TYPE_Double
            case TYPE_Double:
                token.payload.Int = floor(token.payload.Double);
                break;
        #endif
        
        // FIXME: not finished
        default:
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }    
    token.type = TYPE_Int;
    return token;
}    


main(int argc, char *argv[]) {


/* Variable Declarations .SynthesizedVoice */

/* Composite actor's variable declarations. */

/* SynthesizedVoice's variable declarations. */
	Token _SynthesizedVoice_VariableRecursiveLattice_reflectionCoefficients = Array_new(4, 4, Double_new(0.804534), Double_new(-0.820577), Double_new(0.521934), Double_new(-0.205));

/* AudioReader's variable declarations. */

/* GradientAdaptiveLattice's variable declarations. */
	double _SynthesizedVoice_GradientAdaptiveLattice_input;

/* AbsoluteValue's variable declarations. */
	double _SynthesizedVoice_AbsoluteValue_input;

/* Gaussian's variable declarations. */

/* MultiplyDivide's variable declarations. */
	double _SynthesizedVoice_MultiplyDivide_multiply[2];

/* VariableRecursiveLattice's variable declarations. */
	double _SynthesizedVoice_VariableRecursiveLattice_input;
	Token _SynthesizedVoice_VariableRecursiveLattice_newCoefficients;

/* AudioPlayer's variable declarations. */
	double _SynthesizedVoice_AudioPlayer_input[1];

/* preinitialize SynthesizedVoice */
/* The preinitialization of the director. */

    struct AudioReader_soundBuffer _SynthesizedVoice_AudioReader_wave;
    unsigned int _SynthesizedVoice_AudioReader_bitsPerSample;     

    #define _SynthesizedVoice_GradientAdaptiveLattice__order 27

    double _SynthesizedVoice_GradientAdaptiveLattice__backward[_SynthesizedVoice_GradientAdaptiveLattice__order+1];
    double _SynthesizedVoice_GradientAdaptiveLattice__backwardCache[_SynthesizedVoice_GradientAdaptiveLattice__order+1];
    double _SynthesizedVoice_GradientAdaptiveLattice__forward[_SynthesizedVoice_GradientAdaptiveLattice__order+1];
    double _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[_SynthesizedVoice_GradientAdaptiveLattice__order+1];
    Token  _SynthesizedVoice_GradientAdaptiveLattice__reflectionCoefficients = Array_new(27, 27, Double_new(0.0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0), Int_new(0));
    double _SynthesizedVoice_GradientAdaptiveLattice__estimatedErrorPower[_SynthesizedVoice_GradientAdaptiveLattice__order+1];
    double _SynthesizedVoice_GradientAdaptiveLattice__estimatedErrorPowerCache[_SynthesizedVoice_GradientAdaptiveLattice__order+1];
    double _SynthesizedVoice_GradientAdaptiveLattice__reflectionCoefficientsCache[_SynthesizedVoice_GradientAdaptiveLattice__order];
    
    int _SynthesizedVoice_GradientAdaptiveLattice_i;
    double _SynthesizedVoice_GradientAdaptiveLattice_k;
    double _SynthesizedVoice_GradientAdaptiveLattice_fe_i;
    double _SynthesizedVoice_GradientAdaptiveLattice_be_i;
    double _SynthesizedVoice_GradientAdaptiveLattice_fe_ip;
    double _SynthesizedVoice_GradientAdaptiveLattice_be_ip;
    double _SynthesizedVoice_GradientAdaptiveLattice_newError;
    double _SynthesizedVoice_GradientAdaptiveLattice_newCoefficient;
    double _SynthesizedVoice_GradientAdaptiveLattice__oneMinusAlpha;
    double _SynthesizedVoice_GradientAdaptiveLattice__alpha;

/* preinitialize AbsoluteValue */

    double _SynthesizedVoice_Gaussian_seed;
    int _SynthesizedVoice_Gaussian_haveNextNextGaussian = 0;  // false
    double _SynthesizedVoice_Gaussian_nextNextGaussian;

    // intermediate values
    double _SynthesizedVoice_Gaussian_v1, _SynthesizedVoice_Gaussian_v2, _SynthesizedVoice_Gaussian_s;   
    double _SynthesizedVoice_Gaussian_multiplier;

/* preinitialize MultiplyDivide */

/* preinitialize VariableRecursiveLattice */

    int _SynthesizedVoice_VariableRecursiveLattice_i;
    int _SynthesizedVoice_VariableRecursiveLattice_M;
    double _SynthesizedVoice_VariableRecursiveLattice_k;
    int _SynthesizedVoice_VariableRecursiveLattice__valueLength;
    double* _SynthesizedVoice_VariableRecursiveLattice__backward;
    double* _SynthesizedVoice_VariableRecursiveLattice__backwardCache;
    double* _SynthesizedVoice_VariableRecursiveLattice__forward;
    double* _SynthesizedVoice_VariableRecursiveLattice__forwardCache;
    int _SynthesizedVoice_VariableRecursiveLattice__blockSizeValue;

    int _SynthesizedVoice_AudioPlayer_i, _SynthesizedVoice_AudioPlayer_j;
    Uint8 _SynthesizedVoice_AudioPlayer_convertedSample;    
    
    //const int _debug = 0;
        
    struct AudioPlayer_sample _SynthesizedVoice_AudioPlayer_sounds[1];
        
    SDL_sem *_SynthesizedVoice_AudioPlayer_sem;
    SDL_AudioSpec _SynthesizedVoice_AudioPlayer_fmt;

    // Callback method associated with this instance. It mixes the audio data
    // and then signals the semaphore.
    void _SynthesizedVoice_AudioPlayer_mixaudio(void *unused, Uint8 *stream, int len)
    {
        int i;
        int _resetSize = 0;
        Uint32 amount;
    
        //if (_debug) {
        //    fprintf(stdout, "*******************************mixaudio running (data_length= %d), (len= %d)\n", _SynthesizedVoice_AudioPlayer_sounds[0].dataLength, len);
        //}
    
        // mixing sound data into output stream
        for ( i=0; i<1; i++ ) {
            while (_SynthesizedVoice_AudioPlayer_sounds[i].dataLength > _SynthesizedVoice_AudioPlayer_sounds[i].dataPosition) {
                amount = (_SynthesizedVoice_AudioPlayer_sounds[i].dataLength - _SynthesizedVoice_AudioPlayer_sounds[i].dataPosition);
                if ( amount > len ) {
                    amount = len;
                }
                SDL_MixAudio(stream, &_SynthesizedVoice_AudioPlayer_sounds[i].data[_SynthesizedVoice_AudioPlayer_sounds[i].dataPosition], amount, SDL_MIX_MAXVOLUME);
                _SynthesizedVoice_AudioPlayer_sounds[i].dataPosition += amount;
            }
        }
        
        // reset dataPosition, so mixing would start from the beginning of the buffer next time 
        // reset dataLength, so we would put sample from the beginning
        for ( i=0; i<1; i++ ) {
            _resetSize += _SynthesizedVoice_AudioPlayer_sounds[i].dataLength;
            _SynthesizedVoice_AudioPlayer_sounds[i].dataLength = 0;
            _SynthesizedVoice_AudioPlayer_sounds[i].dataPosition = 0;
        }
    
        ///////////// UNLOCK //////////////////////////////
        
        for (i=0; i<_resetSize; i++) {
            if (SDL_SemPost(_SynthesizedVoice_AudioPlayer_sem) == -1) {
                fprintf(stderr, "Error posting semaphor: %s\n",SDL_GetError());
                exit(1);
            }
        }
        ///////////// UNLOCK //////////////////////////////
    }

    // Wait for the semaphore, lock out the callback and put sample into
    // the sound data buffer.
    void _SynthesizedVoice_AudioPlayer_putSample (Uint8 data, int channel) {
        //if (_debug) 
        //    fprintf(stdout, "putSample running (data= %d), (sem= %d)\n", data, SDL_SemValue(_SynthesizedVoice_AudioPlayer_sem));
        /////////////// LOCK //////////////////////////////
        if (SDL_SemWait(_SynthesizedVoice_AudioPlayer_sem) != 0) {
            fprintf(stderr, "Error waiting for semaphore: %s\n",SDL_GetError());
            exit(1);
        }
        /////////////// LOCK //////////////////////////////
        SDL_LockAudio();
    
        _SynthesizedVoice_AudioPlayer_sounds[channel].data[_SynthesizedVoice_AudioPlayer_sounds[channel].dataLength] = data;

        //if (_debug) {
        //    fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data, _SynthesizedVoice_AudioPlayer_sounds[channel].data[sounds[channel].dataLength]);
        //}
        _SynthesizedVoice_AudioPlayer_sounds[channel].dataLength++;
    
        SDL_UnlockAudio();
    }
static int iteration = 0;
/* Initialize .SynthesizedVoice */
/* The initialization of the director. */

    _SynthesizedVoice_AudioReader_wave.soundPosition = 0;
    
    /* Load the SDL library */
    if ( SDL_Init(SDL_INIT_AUDIO) < 0 ) {
        fprintf(stderr, "Couldn't initialize SDL: %s\n",SDL_GetError());
        exit(1);
    }

    if ( SDL_LoadWAV("C:/Program Files/eclipse/workspace/ptII/ptolemy/actor/lib/javasound/voice.wav", &_SynthesizedVoice_AudioReader_wave.spec, &_SynthesizedVoice_AudioReader_wave.sound, &_SynthesizedVoice_AudioReader_wave.soundLength) == NULL ) {
        fprintf(stderr, "Couldn't load C:/Program Files/eclipse/workspace/ptII/ptolemy/actor/lib/javasound/voice.wav: %s\n", SDL_GetError());
        exit(1);
    }
    if (_SynthesizedVoice_AudioReader_wave.spec.format == AUDIO_U8 || _SynthesizedVoice_AudioReader_wave.spec.format == AUDIO_S8) {
        _SynthesizedVoice_AudioReader_bitsPerSample = 8;
    }
    else {
        _SynthesizedVoice_AudioReader_bitsPerSample = 16;        
    }

/* initialize GradientAdaptiveLattice */

    _SynthesizedVoice_VariableRecursiveLattice_newCoefficients = Array_new(_SynthesizedVoice_GradientAdaptiveLattice__order, 0);
    //_SynthesizedVoice_VariableRecursiveLattice_newCoefficients.payload.Array->elementsType = TYPE_Double;
    for (_SynthesizedVoice_GradientAdaptiveLattice_i = 0; _SynthesizedVoice_GradientAdaptiveLattice_i < _SynthesizedVoice_GradientAdaptiveLattice__order; _SynthesizedVoice_GradientAdaptiveLattice_i++) {
        _SynthesizedVoice_VariableRecursiveLattice_newCoefficients.payload.Array->elements[_SynthesizedVoice_GradientAdaptiveLattice_i].type = TYPE_Double;
    }
    
    for (_SynthesizedVoice_GradientAdaptiveLattice_i = 0; _SynthesizedVoice_GradientAdaptiveLattice_i <= _SynthesizedVoice_GradientAdaptiveLattice__order; _SynthesizedVoice_GradientAdaptiveLattice_i++) {
        _SynthesizedVoice_GradientAdaptiveLattice__forward[_SynthesizedVoice_GradientAdaptiveLattice_i] = 0;
        _SynthesizedVoice_GradientAdaptiveLattice__backward[_SynthesizedVoice_GradientAdaptiveLattice_i] = 0;
        _SynthesizedVoice_GradientAdaptiveLattice__estimatedErrorPower[_SynthesizedVoice_GradientAdaptiveLattice_i] = 0;
        _SynthesizedVoice_GradientAdaptiveLattice__estimatedErrorPowerCache[_SynthesizedVoice_GradientAdaptiveLattice_i] = 0;
    }
    _SynthesizedVoice_GradientAdaptiveLattice__oneMinusAlpha = ((15 - 1.0) / (15 + 1.0));
    _SynthesizedVoice_GradientAdaptiveLattice__alpha = 1.0 - _SynthesizedVoice_GradientAdaptiveLattice__oneMinusAlpha;

/* initialize AbsoluteValue */

    /* see documentation from http://java.sun.com/j2se/1.4.2/docs/api/java/util/Random.html#setSeed(long) */
    //this.seed = (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
    _SynthesizedVoice_Gaussian_seed = ((long long) 10000 ^ 0x5DEECE66DLL)  & ((1LL << 48) - 1);

/* initialize MultiplyDivide */

/* initialize VariableRecursiveLattice */

    _SynthesizedVoice_VariableRecursiveLattice__valueLength = _SynthesizedVoice_VariableRecursiveLattice_reflectionCoefficients.payload.Array->size;
    _SynthesizedVoice_VariableRecursiveLattice__backward = (double*) malloc((_SynthesizedVoice_VariableRecursiveLattice__valueLength + 1) * sizeof(double));
    _SynthesizedVoice_VariableRecursiveLattice__backwardCache = (double*) malloc((_SynthesizedVoice_VariableRecursiveLattice__valueLength + 1) * sizeof(double));
    _SynthesizedVoice_VariableRecursiveLattice__forward = (double*) malloc((_SynthesizedVoice_VariableRecursiveLattice__valueLength + 1) * sizeof(double));
    _SynthesizedVoice_VariableRecursiveLattice__forwardCache = (double*) malloc((_SynthesizedVoice_VariableRecursiveLattice__valueLength + 1) * sizeof(double));
    _SynthesizedVoice_VariableRecursiveLattice__blockSizeValue = 1;

    if (_SynthesizedVoice_VariableRecursiveLattice__blockSizeValue < 1) {
        fprintf(stderr, "Invalid blockSize: %d,", _SynthesizedVoice_VariableRecursiveLattice__blockSizeValue);
        exit(1);
    }

    for (_SynthesizedVoice_VariableRecursiveLattice_i = 0; _SynthesizedVoice_VariableRecursiveLattice_i < _SynthesizedVoice_VariableRecursiveLattice__valueLength + 1; _SynthesizedVoice_VariableRecursiveLattice_i++) {
        _SynthesizedVoice_VariableRecursiveLattice__forward[_SynthesizedVoice_VariableRecursiveLattice_i] = 0;
        _SynthesizedVoice_VariableRecursiveLattice__backward[_SynthesizedVoice_VariableRecursiveLattice_i] = 0;
    }

    /* Load the SDL library */
    if ( SDL_Init(SDL_INIT_AUDIO | SDL_INIT_EVENTTHREAD) < 0 ) {
        fprintf(stderr, "Couldn't initialize SDL: %s\n",SDL_GetError());
        exit(1);
    }

    if ((_SynthesizedVoice_AudioPlayer_sem = SDL_CreateSemaphore(AudioPlayer_BUFFER_SIZE)) == NULL) {
        fprintf(stderr, "Error creating semaphor: %s\n",SDL_GetError());
        exit(1);
    }

    /* Set 16-bit stereo audio at 22Khz */
    _SynthesizedVoice_AudioPlayer_fmt.freq = 8000;
    _SynthesizedVoice_AudioPlayer_fmt.format = AUDIO_U8;
    _SynthesizedVoice_AudioPlayer_fmt.channels = 1;
    _SynthesizedVoice_AudioPlayer_fmt.samples = AudioPlayer_SAMPLE_BUFFER_SIZE;    
    _SynthesizedVoice_AudioPlayer_fmt.callback = _SynthesizedVoice_AudioPlayer_mixaudio;
    _SynthesizedVoice_AudioPlayer_fmt.userdata = NULL;

    /* Open the audio device and start playing sound! */
    if ( SDL_OpenAudio(&_SynthesizedVoice_AudioPlayer_fmt, NULL) < 0 ) {
        fprintf(stderr, "Unable to open audio: %s\n", SDL_GetError());
        exit(1);
    }
    for ( _SynthesizedVoice_AudioPlayer_i=0; _SynthesizedVoice_AudioPlayer_i<1; ++_SynthesizedVoice_AudioPlayer_i ) {
        _SynthesizedVoice_AudioPlayer_sounds[_SynthesizedVoice_AudioPlayer_i].data = (Uint8*) malloc(AudioPlayer_BUFFER_SIZE);
        _SynthesizedVoice_AudioPlayer_sounds[_SynthesizedVoice_AudioPlayer_i].dataPosition = 0;
        _SynthesizedVoice_AudioPlayer_sounds[_SynthesizedVoice_AudioPlayer_i].dataLength = 0;
    }
/* Static schedule: */
for (iteration = 0; iteration < 16000; iteration ++) {

/* fire SynthesizedVoice */

/* fire Gaussian */

    if (_SynthesizedVoice_Gaussian_haveNextNextGaussian) {
        _SynthesizedVoice_Gaussian_haveNextNextGaussian = 0;   // false
        _SynthesizedVoice_MultiplyDivide_multiply[0] = _SynthesizedVoice_Gaussian_nextNextGaussian;
    } else {
        do { 
            _SynthesizedVoice_Gaussian_v1 = 2 * Gaussian_nextDouble(&_SynthesizedVoice_Gaussian_seed) - 1;   // between -1.0 and 1.0
            _SynthesizedVoice_Gaussian_v2 = 2 * Gaussian_nextDouble(&_SynthesizedVoice_Gaussian_seed) - 1;   // between -1.0 and 1.0
            _SynthesizedVoice_Gaussian_s = _SynthesizedVoice_Gaussian_v1 * _SynthesizedVoice_Gaussian_v1 + _SynthesizedVoice_Gaussian_v2 * _SynthesizedVoice_Gaussian_v2;
        } while (_SynthesizedVoice_Gaussian_s >= 1 || _SynthesizedVoice_Gaussian_s == 0);

        _SynthesizedVoice_Gaussian_multiplier = sqrt(-2 * log(_SynthesizedVoice_Gaussian_s)/_SynthesizedVoice_Gaussian_s);
        _SynthesizedVoice_Gaussian_nextNextGaussian = _SynthesizedVoice_Gaussian_v2 * _SynthesizedVoice_Gaussian_multiplier;
        _SynthesizedVoice_Gaussian_haveNextNextGaussian = 1;   // true
        _SynthesizedVoice_MultiplyDivide_multiply[0] = _SynthesizedVoice_Gaussian_v1 * _SynthesizedVoice_Gaussian_multiplier;
    }
    _SynthesizedVoice_MultiplyDivide_multiply[0] = (_SynthesizedVoice_MultiplyDivide_multiply[0] * 10.0) + 0.0;    

/* fire AudioReader */

    if (_SynthesizedVoice_AudioReader_wave.soundPosition >= _SynthesizedVoice_AudioReader_wave.soundLength) {
       // if there is no more data from the sound file, return 0
       _SynthesizedVoice_GradientAdaptiveLattice_input = 0;
    } else if (_SynthesizedVoice_AudioReader_bitsPerSample == 8) {
        // Convert sample (Digital to Analog)
        // Input range [0, 255] --> output range [-1.0, 1.0)

        _SynthesizedVoice_GradientAdaptiveLattice_input = _SynthesizedVoice_AudioReader_wave.sound[_SynthesizedVoice_AudioReader_wave.soundPosition];
        _SynthesizedVoice_GradientAdaptiveLattice_input -= 128;  // 2^7
        _SynthesizedVoice_GradientAdaptiveLattice_input /= (double) 128;  // 2^7 
        _SynthesizedVoice_AudioReader_wave.soundPosition++;    
    } else {
        // Convert sample (Digital to Analog)
        // Input range [0, 65535] --> output range [-1.0, 1.0)

        _SynthesizedVoice_GradientAdaptiveLattice_input = _SynthesizedVoice_AudioReader_wave.sound[_SynthesizedVoice_AudioReader_wave.soundPosition] & (_SynthesizedVoice_AudioReader_wave.sound[_SynthesizedVoice_AudioReader_wave.soundPosition+1] << 8);
        _SynthesizedVoice_GradientAdaptiveLattice_input -= 32768;  // 2^15
        _SynthesizedVoice_GradientAdaptiveLattice_input /= (double) 32768; // 2^15 
        _SynthesizedVoice_AudioReader_wave.soundPosition += 2;
    }

/* fire GradientAdaptiveLattice */

    // System.arraycopy(_backward, 0, _backwardCache, 0, _order + 1);
    // System.arraycopy(_forward, 0, _forwardCache, 0, _order + 1);
    for (_SynthesizedVoice_GradientAdaptiveLattice_i = 0; _SynthesizedVoice_GradientAdaptiveLattice_i < _SynthesizedVoice_GradientAdaptiveLattice__order + 1; _SynthesizedVoice_GradientAdaptiveLattice_i++) {
        _SynthesizedVoice_GradientAdaptiveLattice__backwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i] = _SynthesizedVoice_GradientAdaptiveLattice__backward[_SynthesizedVoice_GradientAdaptiveLattice_i];
        _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i] = _SynthesizedVoice_GradientAdaptiveLattice__forward[_SynthesizedVoice_GradientAdaptiveLattice_i];
    }

    _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[0] = _SynthesizedVoice_GradientAdaptiveLattice_input; // _forwardCache(0) = x(n)

    // NOTE: The following code is ported from Ptolemy Classic.
    // Update forward errors.
    for (_SynthesizedVoice_GradientAdaptiveLattice_i = 0; _SynthesizedVoice_GradientAdaptiveLattice_i < _SynthesizedVoice_GradientAdaptiveLattice__order; _SynthesizedVoice_GradientAdaptiveLattice_i++) {
        _SynthesizedVoice_GradientAdaptiveLattice_k = _SynthesizedVoice_GradientAdaptiveLattice__reflectionCoefficients.payload.Array->elements[_SynthesizedVoice_GradientAdaptiveLattice_i].payload.Double;
        _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i + 1] = (-_SynthesizedVoice_GradientAdaptiveLattice_k * _SynthesizedVoice_GradientAdaptiveLattice__backwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i]) + _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i];
    }

    // Backward: Compute the weights for the next round Note:
    // strictly speaking, _backwardCache[_order] is not necessary
    // for computing the output.  It is computed for the use of
    // subclasses which adapt the reflection coefficients.
    for (_SynthesizedVoice_GradientAdaptiveLattice_i = _SynthesizedVoice_GradientAdaptiveLattice__order; _SynthesizedVoice_GradientAdaptiveLattice_i > 0; _SynthesizedVoice_GradientAdaptiveLattice_i--) {
        _SynthesizedVoice_GradientAdaptiveLattice_k = _SynthesizedVoice_GradientAdaptiveLattice__reflectionCoefficients.payload.Array->elements[_SynthesizedVoice_GradientAdaptiveLattice_i - 1].payload.Double;
        _SynthesizedVoice_GradientAdaptiveLattice__backwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i] = (-_SynthesizedVoice_GradientAdaptiveLattice_k * _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i - 1]) + _SynthesizedVoice_GradientAdaptiveLattice__backwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i - 1];

        _SynthesizedVoice_GradientAdaptiveLattice_fe_i = _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i];
        _SynthesizedVoice_GradientAdaptiveLattice_be_i = _SynthesizedVoice_GradientAdaptiveLattice__backwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i];
        _SynthesizedVoice_GradientAdaptiveLattice_fe_ip = _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i - 1];
        _SynthesizedVoice_GradientAdaptiveLattice_be_ip = _SynthesizedVoice_GradientAdaptiveLattice__backwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i - 1];

        _SynthesizedVoice_GradientAdaptiveLattice_newError = (_SynthesizedVoice_GradientAdaptiveLattice__estimatedErrorPower[_SynthesizedVoice_GradientAdaptiveLattice_i] * _SynthesizedVoice_GradientAdaptiveLattice__oneMinusAlpha) + (_SynthesizedVoice_GradientAdaptiveLattice__alpha * ((_SynthesizedVoice_GradientAdaptiveLattice_fe_ip * _SynthesizedVoice_GradientAdaptiveLattice_fe_ip) + (_SynthesizedVoice_GradientAdaptiveLattice_be_ip * _SynthesizedVoice_GradientAdaptiveLattice_be_ip)));
        _SynthesizedVoice_GradientAdaptiveLattice_newCoefficient = _SynthesizedVoice_GradientAdaptiveLattice__reflectionCoefficients.payload.Array->elements[_SynthesizedVoice_GradientAdaptiveLattice_i - 1].payload.Double;

        if (_SynthesizedVoice_GradientAdaptiveLattice_newError != 0.0) {
            _SynthesizedVoice_GradientAdaptiveLattice_newCoefficient += ((_SynthesizedVoice_GradientAdaptiveLattice__alpha * ((_SynthesizedVoice_GradientAdaptiveLattice_fe_i * _SynthesizedVoice_GradientAdaptiveLattice_be_ip) + (_SynthesizedVoice_GradientAdaptiveLattice_be_i * _SynthesizedVoice_GradientAdaptiveLattice_fe_ip))) / _SynthesizedVoice_GradientAdaptiveLattice_newError);

            if (_SynthesizedVoice_GradientAdaptiveLattice_newCoefficient > 1.0) {
                _SynthesizedVoice_GradientAdaptiveLattice_newCoefficient = 1.0;
            } else if (_SynthesizedVoice_GradientAdaptiveLattice_newCoefficient < -1.0) {
                _SynthesizedVoice_GradientAdaptiveLattice_newCoefficient = -1.0;
            }
        }

        _SynthesizedVoice_VariableRecursiveLattice_newCoefficients.payload.Array->elements[_SynthesizedVoice_GradientAdaptiveLattice_i - 1].payload.Double = _SynthesizedVoice_GradientAdaptiveLattice_newCoefficient;
        _SynthesizedVoice_GradientAdaptiveLattice__reflectionCoefficientsCache[_SynthesizedVoice_GradientAdaptiveLattice_i - 1] = _SynthesizedVoice_GradientAdaptiveLattice_newCoefficient;
        _SynthesizedVoice_GradientAdaptiveLattice__estimatedErrorPowerCache[_SynthesizedVoice_GradientAdaptiveLattice_i] = _SynthesizedVoice_GradientAdaptiveLattice_newError;
    }

    _SynthesizedVoice_GradientAdaptiveLattice__backwardCache[0] = _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[0]; // _backwardCache[0] = x[n]

    // Send the forward residual.
    _SynthesizedVoice_AbsoluteValue_input = _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[_SynthesizedVoice_GradientAdaptiveLattice__order];

    //arraycopy(_estimatedErrorPowerCache, 0, _estimatedErrorPower, 0, _order + 1);
    //arraycopy(_reflectionCoefficientsCache, 0, _reflectionCoefficients, 0, _order);
    //arraycopy(_backwardCache, 0, _backward, 0, _order + 1);
    //arraycopy(_forwardCache, 0, _forward, 0, _order + 1);
    for (_SynthesizedVoice_GradientAdaptiveLattice_i = 0; _SynthesizedVoice_GradientAdaptiveLattice_i < _SynthesizedVoice_GradientAdaptiveLattice__order + 1; _SynthesizedVoice_GradientAdaptiveLattice_i++) {
        _SynthesizedVoice_GradientAdaptiveLattice__estimatedErrorPower[_SynthesizedVoice_GradientAdaptiveLattice_i] = _SynthesizedVoice_GradientAdaptiveLattice__estimatedErrorPowerCache[_SynthesizedVoice_GradientAdaptiveLattice_i];
        _SynthesizedVoice_GradientAdaptiveLattice__backward[_SynthesizedVoice_GradientAdaptiveLattice_i] = _SynthesizedVoice_GradientAdaptiveLattice__backwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i];
        _SynthesizedVoice_GradientAdaptiveLattice__forward[_SynthesizedVoice_GradientAdaptiveLattice_i] = _SynthesizedVoice_GradientAdaptiveLattice__forwardCache[_SynthesizedVoice_GradientAdaptiveLattice_i];
    }
    for (_SynthesizedVoice_GradientAdaptiveLattice_i = 0; _SynthesizedVoice_GradientAdaptiveLattice_i < _SynthesizedVoice_GradientAdaptiveLattice__order; _SynthesizedVoice_GradientAdaptiveLattice_i++) {
        _SynthesizedVoice_GradientAdaptiveLattice__reflectionCoefficients.payload.Array->elements[_SynthesizedVoice_GradientAdaptiveLattice_i].payload.Double = _SynthesizedVoice_GradientAdaptiveLattice__reflectionCoefficientsCache[_SynthesizedVoice_GradientAdaptiveLattice_i];
    }

/* fire AbsoluteValue */

    // Cannot use abs() 
    //_SynthesizedVoice_MultiplyDivide_multiply[1] = abs(_SynthesizedVoice_AbsoluteValue_input);
    _SynthesizedVoice_MultiplyDivide_multiply[1] = _SynthesizedVoice_AbsoluteValue_input < 0.0 ? -_SynthesizedVoice_AbsoluteValue_input : _SynthesizedVoice_AbsoluteValue_input;

/* fire MultiplyDivide */
_SynthesizedVoice_VariableRecursiveLattice_input = _SynthesizedVoice_MultiplyDivide_multiply[0] * _SynthesizedVoice_MultiplyDivide_multiply[1];

/* fire VariableRecursiveLattice */

    // System.arraycopy(_backward, 0, _backwardCache, 0, _valueLength + 1);
    // System.arraycopy(_forward, 0, _forwardCache, 0, _valueLength + 1);
    for (_SynthesizedVoice_VariableRecursiveLattice_i = 0; _SynthesizedVoice_VariableRecursiveLattice_i < _SynthesizedVoice_VariableRecursiveLattice__valueLength + 1; _SynthesizedVoice_VariableRecursiveLattice_i++) {
        _SynthesizedVoice_VariableRecursiveLattice__backwardCache[_SynthesizedVoice_VariableRecursiveLattice_i] = _SynthesizedVoice_VariableRecursiveLattice__backward[_SynthesizedVoice_VariableRecursiveLattice_i];
        _SynthesizedVoice_VariableRecursiveLattice__forwardCache[_SynthesizedVoice_VariableRecursiveLattice_i] = _SynthesizedVoice_VariableRecursiveLattice__forward[_SynthesizedVoice_VariableRecursiveLattice_i];
    }

    // FIXME: we need to check if (newCoefficients.hasToken(0)) in the future
    _SynthesizedVoice_VariableRecursiveLattice_reflectionCoefficients = _SynthesizedVoice_VariableRecursiveLattice_newCoefficients;
   
   //--------------- reallocate -------------------------
    //if ((_backward == null) || (valueLength != (_backward.length - 1))) {
    if (_SynthesizedVoice_VariableRecursiveLattice__valueLength != _SynthesizedVoice_VariableRecursiveLattice_reflectionCoefficients.payload.Array->size) {
        _SynthesizedVoice_VariableRecursiveLattice__valueLength = _SynthesizedVoice_VariableRecursiveLattice_reflectionCoefficients.payload.Array->size;
        
        // Need to allocate or reallocate the arrays.
        free(_SynthesizedVoice_VariableRecursiveLattice__backward);
        free(_SynthesizedVoice_VariableRecursiveLattice__backwardCache);
        free(_SynthesizedVoice_VariableRecursiveLattice__forward);
        free(_SynthesizedVoice_VariableRecursiveLattice__forwardCache);
        
        _SynthesizedVoice_VariableRecursiveLattice__backward = (double*) malloc((_SynthesizedVoice_VariableRecursiveLattice__valueLength + 1) * sizeof(double));
        _SynthesizedVoice_VariableRecursiveLattice__backwardCache = (double*) malloc((_SynthesizedVoice_VariableRecursiveLattice__valueLength + 1) * sizeof(double));
        _SynthesizedVoice_VariableRecursiveLattice__forward = (double*) malloc((_SynthesizedVoice_VariableRecursiveLattice__valueLength + 1) * sizeof(double));
        _SynthesizedVoice_VariableRecursiveLattice__forwardCache = (double*) malloc((_SynthesizedVoice_VariableRecursiveLattice__valueLength + 1) * sizeof(double));
    }

    //----------------------------------------------------   

    for (_SynthesizedVoice_VariableRecursiveLattice_i = 0; _SynthesizedVoice_VariableRecursiveLattice_i < _SynthesizedVoice_VariableRecursiveLattice__blockSizeValue; _SynthesizedVoice_VariableRecursiveLattice_i++) {
        // NOTE: The following code is ported from Ptolemy Classic.
        _SynthesizedVoice_VariableRecursiveLattice_M = _SynthesizedVoice_VariableRecursiveLattice__valueLength;

        // Forward prediction error
        _SynthesizedVoice_VariableRecursiveLattice__forwardCache[0] = _SynthesizedVoice_VariableRecursiveLattice_input; // _forward(0) = x(n)

        for (_SynthesizedVoice_VariableRecursiveLattice_i = 1; _SynthesizedVoice_VariableRecursiveLattice_i <= _SynthesizedVoice_VariableRecursiveLattice_M; _SynthesizedVoice_VariableRecursiveLattice_i++) {
            _SynthesizedVoice_VariableRecursiveLattice_k = _SynthesizedVoice_VariableRecursiveLattice_reflectionCoefficients.payload.Array->elements[_SynthesizedVoice_VariableRecursiveLattice_M - _SynthesizedVoice_VariableRecursiveLattice_i].payload.Double;
            _SynthesizedVoice_VariableRecursiveLattice__forwardCache[_SynthesizedVoice_VariableRecursiveLattice_i] = (_SynthesizedVoice_VariableRecursiveLattice_k * _SynthesizedVoice_VariableRecursiveLattice__backwardCache[_SynthesizedVoice_VariableRecursiveLattice_i]) + _SynthesizedVoice_VariableRecursiveLattice__forwardCache[_SynthesizedVoice_VariableRecursiveLattice_i - 1];
        }

        _SynthesizedVoice_AudioPlayer_input[0] = _SynthesizedVoice_VariableRecursiveLattice__forwardCache[_SynthesizedVoice_VariableRecursiveLattice_M];

        // Backward:  Compute the w's for the next round
        for (_SynthesizedVoice_VariableRecursiveLattice_i = 1; _SynthesizedVoice_VariableRecursiveLattice_i < _SynthesizedVoice_VariableRecursiveLattice_M; _SynthesizedVoice_VariableRecursiveLattice_i++) {
            _SynthesizedVoice_VariableRecursiveLattice_k = -_SynthesizedVoice_VariableRecursiveLattice_reflectionCoefficients.payload.Array->elements[_SynthesizedVoice_VariableRecursiveLattice_M - 1 - _SynthesizedVoice_VariableRecursiveLattice_i].payload.Double;
            _SynthesizedVoice_VariableRecursiveLattice__backwardCache[_SynthesizedVoice_VariableRecursiveLattice_i] = _SynthesizedVoice_VariableRecursiveLattice__backwardCache[_SynthesizedVoice_VariableRecursiveLattice_i + 1] + (_SynthesizedVoice_VariableRecursiveLattice_k * _SynthesizedVoice_VariableRecursiveLattice__forwardCache[_SynthesizedVoice_VariableRecursiveLattice_i + 1]);
        }

        _SynthesizedVoice_VariableRecursiveLattice__backwardCache[_SynthesizedVoice_VariableRecursiveLattice_M] = _SynthesizedVoice_VariableRecursiveLattice__forwardCache[_SynthesizedVoice_VariableRecursiveLattice_M];
    }

    //arraycopy(_backwardCache, 0, _backward, 0, _valueLength + 1);
    //arraycopy(_forwardCache, 0, _forward, 0, _valueLength + 1);
    for (_SynthesizedVoice_VariableRecursiveLattice_i = 0; _SynthesizedVoice_VariableRecursiveLattice_i < _SynthesizedVoice_VariableRecursiveLattice__valueLength + 1; _SynthesizedVoice_VariableRecursiveLattice_i++) {
        _SynthesizedVoice_VariableRecursiveLattice__backward[_SynthesizedVoice_VariableRecursiveLattice_i] = _SynthesizedVoice_VariableRecursiveLattice__backwardCache[_SynthesizedVoice_VariableRecursiveLattice_i];
        _SynthesizedVoice_VariableRecursiveLattice__forward[_SynthesizedVoice_VariableRecursiveLattice_i] = _SynthesizedVoice_VariableRecursiveLattice__forwardCache[_SynthesizedVoice_VariableRecursiveLattice_i];
    }

/* fire AudioPlayer */

    SDL_PauseAudio(0);      /* Start the callback */
    
    // FIXME: we should not put the same sample into different channels
    for ( _SynthesizedVoice_AudioPlayer_j=0; _SynthesizedVoice_AudioPlayer_j<1; ++_SynthesizedVoice_AudioPlayer_j ) {
        // Convert sample (Analog to Digital)
        // Input range [-1.0, 1.0] --> output range [0, 255]
        
        //Use the following line if RINT() is not defined
        //_SynthesizedVoice_AudioPlayer_convertedSample = floor((AudioPlayer_clip(_SynthesizedVoice_AudioPlayer_input) * 127) + 128); //128 = 2^7
        _SynthesizedVoice_AudioPlayer_convertedSample = rint((AudioPlayer_clip(_SynthesizedVoice_AudioPlayer_input[0]) * 127) + 128); //128 = 2^7
        _SynthesizedVoice_AudioPlayer_putSample (_SynthesizedVoice_AudioPlayer_convertedSample, _SynthesizedVoice_AudioPlayer_j);
    }
}
/* Wrapup .SynthesizedVoice */
/* The wrapup of the director. */

    SDL_FreeWAV(_SynthesizedVoice_AudioReader_wave.sound);
    SDL_Quit();

}

// Code generation complete.
