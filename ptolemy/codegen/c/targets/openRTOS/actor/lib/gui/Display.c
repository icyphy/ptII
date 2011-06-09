/***preinitBlock***/
/**/

/***initBlock***/
    xOLEDMessage xMessage;
    unsigned portLONG ulY, ulMaxY;
    static portCHAR cMessage[ mainMAX_MSG_LEN ];
    extern volatile unsigned portLONG ulMaxJitter;
    unsigned portBASE_TYPE uxUnusedStackOnEntry;
    const unsigned portCHAR *pucImage;
    /* Functions to access the OLED.  The one used depends on the dev kit
    being used. */
    void ( *vOLEDInit )( unsigned portLONG ) = NULL;
    void ( *vOLEDStringDraw )( const portCHAR *, unsigned portLONG, unsigned portLONG, unsigned portCHAR ) = NULL;
    void ( *vOLEDImageDraw )( const unsigned portCHAR *, unsigned portLONG, unsigned portLONG, unsigned portLONG, unsigned portLONG ) = NULL;
    void ( *vOLEDClear )( void ) = NULL;
    /* Just for demo purposes. */
    uxUnusedStackOnEntry = uxTaskGetStackHighWaterMark( NULL );
    /* Map the OLED access functions to the driver functions that are appropriate
    for the evaluation kit being used. */
    switch( HWREG( SYSCTL_DID1 ) & SYSCTL_DID1_PRTNO_MASK )
    {
        case SYSCTL_DID1_PRTNO_6965 :
        case SYSCTL_DID1_PRTNO_2965 : vOLEDInit = OSRAM128x64x4Init;
                vOLEDStringDraw = OSRAM128x64x4StringDraw;
                vOLEDImageDraw = OSRAM128x64x4ImageDraw;
                vOLEDClear = OSRAM128x64x4Clear;
                ulMaxY = mainMAX_ROWS_64;
                pucImage = pucBasicBitmap;
                break;
        case SYSCTL_DID1_PRTNO_1968 :
        case SYSCTL_DID1_PRTNO_8962 : vOLEDInit = RIT128x96x4Init;
                vOLEDStringDraw = RIT128x96x4StringDraw;
                vOLEDImageDraw = RIT128x96x4ImageDraw;
                vOLEDClear = RIT128x96x4Clear;
                ulMaxY = mainMAX_ROWS_96;
                pucImage = pucBasicBitmap;
                break;
        default : vOLEDInit = vFormike128x128x16Init;
                vOLEDStringDraw = vFormike128x128x16StringDraw;
                vOLEDImageDraw = vFormike128x128x16ImageDraw;
                vOLEDClear = vFormike128x128x16Clear;
                ulMaxY = mainMAX_ROWS_128;
                pucImage = pucGrLibBitmap;
                break;
    }
    ulY = ulMaxY;
    /* Initialise the OLED and display a startup message. */
    vOLEDInit( ulSSI_FREQUENCY );
/**/

/***fireBlock***/
        /* Wait for a message to arrive that requires displaying. */
        xQueueReceive( xOLEDQueue, &xMessage, portMAX_DELAY );

$get(input, 0)

        /* Write the message on the next available row. */
        ulY += mainCHARACTER_HEIGHT;
        if( ulY >= ulMaxY ) {
            ulY = mainCHARACTER_HEIGHT;
            vOLEDClear();
            vOLEDStringDraw( $val(title), 0, 0, mainFULL_SCALE );
        }
        /* Display the message along with the maximum jitter time from the
        high priority time test. */
        sprintf( cMessage, "%s [%uns]", xMessage.pcMessage, ulMaxJitter * mainNS_PER_CLOCK );
        vOLEDStringDraw( cMessage, 0, ulY, mainFULL_SCALE );

/**/

/***wrapupBlock***/
/**/


