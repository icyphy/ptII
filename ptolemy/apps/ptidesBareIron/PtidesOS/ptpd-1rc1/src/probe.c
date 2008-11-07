#include "ptpd.h"

#define KEY_ARRAY_LEN 6
UInteger8 management_key_array[KEY_ARRAY_LEN] =
  { PTP_MM_OBTAIN_IDENTITY, PTP_MM_GET_DEFAULT_DATA_SET, PTP_MM_GET_CURRENT_DATA_SET,
    PTP_MM_GET_PARENT_DATA_SET, PTP_MM_GET_PORT_DATA_SET, PTP_MM_GET_GLOBAL_TIME_DATA_SET };

void displayHeader(MsgHeader*);
void displayManagement(MsgHeader*,MsgManagement*);

void probe(RunTimeOpts *rtOpts, PtpClock *ptpClock)
{
  UInteger16 i;
  UInteger16 length;
  TimeInternal interval, now, finish, timestamp;

  /* check */
  if(rtOpts->probe_management_key == PTP_MM_UPDATE_DEFAULT_DATA_SET
    || rtOpts->probe_management_key == PTP_MM_UPDATE_GLOBAL_TIME_PROPERTIES
    || rtOpts->probe_management_key == PTP_MM_SET_SYNC_INTERVAL)
  {
    ERROR("send not supported for that management message\n");
    return;
  }

  /* init */
  if(!netInit(&ptpClock->netPath, rtOpts, ptpClock))
  {
    ERROR("failed to initialize network\n");
    return;
  }

  initData(rtOpts, ptpClock);
  msgPackHeader(ptpClock->msgObuf, ptpClock);

  memset(&ptpClock->msgTmp.manage, 0, sizeof(MsgManagement));
  ptpClock->msgTmp.manage.targetCommunicationTechnology = PTP_DEFAULT;

  /* send */
  for(i = 0; i < KEY_ARRAY_LEN; ++i)
  {
    if(rtOpts->probe_management_key > 0)
    {
      ptpClock->msgTmp.manage.managementMessageKey = rtOpts->probe_management_key;
      ptpClock->msgTmp.manage.recordKey = rtOpts->probe_record_key;
    }
    else
      ptpClock->msgTmp.manage.managementMessageKey = management_key_array[i];

    if(!(length = msgPackManagement(ptpClock->msgObuf, &ptpClock->msgTmp.manage, ptpClock)))
    {
      ERROR("failed to pack management message\n");
      return;
    }

    printf("\n(sending managementMessageKey %u)\n", ptpClock->msgTmp.manage.managementMessageKey);

    if(!netSendGeneral(ptpClock->msgObuf, length, &ptpClock->netPath))
    {
      ERROR("failed to send message\n");
      return;
    }

    if(rtOpts->probe_management_key > 0)
      break;
  }

  getTime(&finish);
  finish.seconds += PTP_SYNC_INTERVAL_TIMEOUT(ptpClock->sync_interval);
  for(;;)
  {
    interval.seconds = PTP_SYNC_INTERVAL_TIMEOUT(ptpClock->sync_interval);
    interval.nanoseconds = 0;
    netSelect(&interval, &ptpClock->netPath);

    netRecvEvent(ptpClock->msgIbuf, &timestamp, &ptpClock->netPath);

    if(netRecvGeneral(ptpClock->msgIbuf, &ptpClock->netPath))
    {
      msgUnpackHeader(ptpClock->msgIbuf, &ptpClock->msgTmpHeader);

      if(ptpClock->msgTmpHeader.control == PTP_MANAGEMENT_MESSAGE)
      {
        msgUnpackManagement(ptpClock->msgIbuf, &ptpClock->msgTmp.manage);
        msgUnpackManagementPayload(ptpClock->msgIbuf, &ptpClock->msgTmp.manage);

        displayManagement(&ptpClock->msgTmpHeader, &ptpClock->msgTmp.manage);
      }

      fflush(stdout);
    }

    getTime(&now);
    if( now.seconds > finish.seconds || (now.seconds == finish.seconds
      && now.nanoseconds > finish.nanoseconds) )
      break;
  }

  /* done */
  printf("\n");
  ptpdShutdown();

  exit(0);
}

void displayHeader(MsgHeader *header)
{
  printf(
    "  sourceCommunicationTechnology %u\n"
    "  sourceUuid %02x:%02x:%02x:%02x:%02x:%02x\n"
    "  sourcePortId %u\n",
    header->sourceCommunicationTechnology,
    header->sourceUuid[0], header->sourceUuid[1], header->sourceUuid[2],
    header->sourceUuid[3], header->sourceUuid[4], header->sourceUuid[5],
    header->sourcePortId);
}

void displayManagement(MsgHeader *header, MsgManagement *manage)
{
  Integer16 i;

  switch(manage->managementMessageKey)
  {
  case PTP_MM_CLOCK_IDENTITY:
    printf("\n");
    displayHeader(header);
    printf("  managementMessageKey %u (PTP_MM_CLOCK_IDENTITY)\n", manage->managementMessageKey);
    printf("  clockCommunicationTechnology %u\n", manage->payload.clockIdentity.clockCommunicationTechnology);
    printf("  clockUuidField %02x:%02x:%02x:%02x:%02x:%02x\n",
      manage->payload.clockIdentity.clockUuidField[0], manage->payload.clockIdentity.clockUuidField[1],
      manage->payload.clockIdentity.clockUuidField[2], manage->payload.clockIdentity.clockUuidField[3],
      manage->payload.clockIdentity.clockUuidField[4], manage->payload.clockIdentity.clockUuidField[5]);
    printf("  clockPortField %u\n", manage->payload.clockIdentity.clockPortField);
    printf("  manufacturerIdentity ");
    for(i = 0; i < MANUFACTURER_ID_LENGTH && manage->payload.clockIdentity.manufacturerIdentity[i]; ++i)
      putchar(manage->payload.clockIdentity.manufacturerIdentity[i]);
    putchar('\n');
    break;

  case PTP_MM_DEFAULT_DATA_SET:
    printf("\n");
    displayHeader(header);
    printf("  managementMessageKey %u (PTP_MM_DEFAULT_DATA_SET)\n", manage->managementMessageKey);
    printf("  clockCommunicationTechnology %u\n", manage->payload.defaultData.clockCommunicationTechnology);
    printf("  clockUuidField %02x:%02x:%02x:%02x:%02x:%02x\n",
      manage->payload.defaultData.clockUuidField[0], manage->payload.defaultData.clockUuidField[1],
      manage->payload.defaultData.clockUuidField[2], manage->payload.defaultData.clockUuidField[3],
      manage->payload.defaultData.clockUuidField[4], manage->payload.defaultData.clockUuidField[5]);
    printf("  clockPortField %u\n", manage->payload.defaultData.clockPortField);
    printf("  clockStratum %u\n", manage->payload.defaultData.clockStratum);
    printf("  clockIdentifier ");
    for(i = 0; i < PTP_CODE_STRING_LENGTH && manage->payload.defaultData.clockIdentifier[i]; ++i)
      putchar(manage->payload.defaultData.clockIdentifier[i]);
    putchar('\n');
    printf("  clockVariance %d\n", manage->payload.defaultData.clockVariance);
    printf("  clockFollowupCapable %u\n", manage->payload.defaultData.clockFollowupCapable);
    printf("  preferred %u\n", manage->payload.defaultData.preferred);
    printf("  initializable %u\n", manage->payload.defaultData.initializable);
    printf("  externalTiming %u\n", manage->payload.defaultData.externalTiming);
    printf("  isBoundaryClock %u\n", manage->payload.defaultData.isBoundaryClock);
    printf("  syncInterval %d\n", manage->payload.defaultData.syncInterval);
    printf("  subdomainName ");
    for(i = 0; i < PTP_SUBDOMAIN_NAME_LENGTH && manage->payload.defaultData.subdomainName[i]; ++i)
      putchar(manage->payload.defaultData.subdomainName[i]);
    putchar('\n');
    printf("  numberPorts %u\n", manage->payload.defaultData.numberPorts);
    printf("  numberForeignRecords %u\n", manage->payload.defaultData.numberForeignRecords);
    break;

  case PTP_MM_CURRENT_DATA_SET:
    printf("\n");
    displayHeader(header);
    printf("  managementMessageKey %u (PTP_MM_CURRENT_DATA_SET)\n", manage->managementMessageKey);
    printf("  stepsRemoved %u\n", manage->payload.current.stepsRemoved);
    printf("  offsetFromMaster %s%u.%09d\n",
      manage->payload.current.offsetFromMaster.nanoseconds & 0x80000000 ? "-" : "",
      manage->payload.current.offsetFromMaster.seconds,
      manage->payload.current.offsetFromMaster.nanoseconds & ~0x80000000);
    printf("  oneWayDelay %s%u.%09d\n",
      manage->payload.current.oneWayDelay.nanoseconds & 0x80000000 ? "-" : "",
      manage->payload.current.oneWayDelay.seconds,
      manage->payload.current.oneWayDelay.nanoseconds & ~0x80000000);
    break;

  case PTP_MM_PARENT_DATA_SET:
    printf("\n");
    displayHeader(header);
    printf("  managementMessageKey %u (PTP_MM_PARENT_DATA_SET)\n", manage->managementMessageKey);
    printf("  parentCommunicationTechnology %u\n", manage->payload.parent.parentCommunicationTechnology);
    printf("  parentUuid %02x:%02x:%02x:%02x:%02x:%02x\n",
      manage->payload.parent.parentUuid[0], manage->payload.parent.parentUuid[1],
      manage->payload.parent.parentUuid[2], manage->payload.parent.parentUuid[3],
      manage->payload.parent.parentUuid[4], manage->payload.parent.parentUuid[5]);
    printf("  parentPortId %u\n", manage->payload.parent.parentPortId);
    printf("  parentLastSyncSequenceNumber %u\n", manage->payload.parent.parentLastSyncSequenceNumber);
    printf("  parentFollowupCapable %u\n", manage->payload.parent.parentFollowupCapable);
    printf("  parentExternalTiming %u\n", manage->payload.parent.parentExternalTiming);
    printf("  parentVariance %d\n", manage->payload.parent.parentVariance);
    printf("  parentStats %u\n", manage->payload.parent.parentStats);
    printf("  observedVariance %d\n", manage->payload.parent.observedVariance);
    printf("  observedDrift %d\n", manage->payload.parent.observedDrift);
    printf("  utcReasonable %u\n", manage->payload.parent.utcReasonable);
    printf("  grandmasterCommunicationTechnology %u\n", manage->payload.parent.grandmasterCommunicationTechnology);
    printf("  grandmasterUuidField %02x:%02x:%02x:%02x:%02x:%02x\n",
      manage->payload.parent.grandmasterUuidField[0], manage->payload.parent.grandmasterUuidField[1],
      manage->payload.parent.grandmasterUuidField[2], manage->payload.parent.grandmasterUuidField[3],
      manage->payload.parent.grandmasterUuidField[4], manage->payload.parent.grandmasterUuidField[5]);
    printf("  grandmasterPortIdField %u\n", manage->payload.parent.grandmasterPortIdField);
    printf("  grandmasterStratum %u\n", manage->payload.parent.grandmasterStratum);
    printf("  grandmasterIdentifier ");
    for(i = 0; i < PTP_CODE_STRING_LENGTH && manage->payload.parent.grandmasterIdentifier[i]; ++i)
      putchar(manage->payload.parent.grandmasterIdentifier[i]);
    putchar('\n');
    printf("  grandmasterVariance %d\n", manage->payload.parent.grandmasterVariance);
    printf("  grandmasterPreferred %u\n", manage->payload.parent.grandmasterPreferred);
    printf("  grandmasterIsBoundaryClock %u\n", manage->payload.parent.grandmasterIsBoundaryClock);
    printf("  grandmasterSequenceNumber %u\n", manage->payload.parent.grandmasterSequenceNumber);
    break;

  case PTP_MM_PORT_DATA_SET:
    printf("\n");
    displayHeader(header);
    printf("  managementMessageKey %u (PTP_MM_PORT_DATA_SET)\n", manage->managementMessageKey);
    printf("  returnedPortNumber %u\n", manage->payload.port.returnedPortNumber);
    printf("  portState %u\n", manage->payload.port.portState);
    printf("  lastSyncEventSequenceNumber %u\n", manage->payload.port.lastSyncEventSequenceNumber);
    printf("  lastGeneralEventSequenceNumber %u\n", manage->payload.port.lastGeneralEventSequenceNumber);
    printf("  portCommunicationTechnology %u\n", manage->payload.port.portCommunicationTechnology);
    printf("  portUuidField %02x:%02x:%02x:%02x:%02x:%02x\n",
      manage->payload.port.portUuidField[0], manage->payload.port.portUuidField[1],
      manage->payload.port.portUuidField[2], manage->payload.port.portUuidField[3],
      manage->payload.port.portUuidField[4], manage->payload.port.portUuidField[5]);
    printf("  portIdField %u\n", manage->payload.port.portIdField);
    printf("  burstEnabled %u\n", manage->payload.port.burstEnabled);
    printf("  subdomainAddressOctets %u\n", manage->payload.port.subdomainAddressOctets);
    printf("  eventPortAddressOctets %u\n", manage->payload.port.eventPortAddressOctets);
    printf("  generalPortAddressOctets %u\n", manage->payload.port.generalPortAddressOctets);
    printf("  subdomainAddress ");
    printf("%u", manage->payload.port.subdomainAddress[0]);
    for(i = 1; i < SUBDOMAIN_ADDRESS_LENGTH; ++i)
      printf(".%u", manage->payload.port.subdomainAddress[i]);
    putchar('\n');
    printf("  eventPortAddress %u\n", *(UInteger16*)manage->payload.port.eventPortAddress);
    printf("  generalPortAddress %u\n", *(UInteger16*)manage->payload.port.generalPortAddress);
    break;

  case PTP_MM_GLOBAL_TIME_DATA_SET:
    printf("\n");
    displayHeader(header);
    printf("  managementMessageKey %u (PTP_MM_GLOBAL_TIME_DATA_SET)\n", manage->managementMessageKey);
    printf("  localTime %s%u.%09d\n",
      manage->payload.globalTime.localTime.nanoseconds & 0x80000000 ? "-" : "",
      manage->payload.globalTime.localTime.seconds,
      manage->payload.globalTime.localTime.nanoseconds & ~0x80000000);
    printf("  currentUtcOffset %d\n", manage->payload.globalTime.currentUtcOffset);
    printf("  leap59 %u\n", manage->payload.globalTime.leap59);
    printf("  leap61 %u\n", manage->payload.globalTime.leap61);
    printf("  epochNumber %u\n", manage->payload.globalTime.epochNumber);
    break;


  case PTP_MM_FOREIGN_DATA_SET:
    printf("\n");
    displayHeader(header);
    printf("  managementMessageKey %u (PTP_MM_FOREIGN_DATA_SET)\n", manage->managementMessageKey);
    printf("  returnedPortNumber %u\n", manage->payload.foreign.returnedPortNumber);
    printf("  returnedRecordNumber %u\n", manage->payload.foreign.returnedRecordNumber);
    printf("  foreignMasterCommunicationTechnology %u\n", manage->payload.foreign.foreignMasterCommunicationTechnology);
    printf("  foreignMasterUuid %02x:%02x:%02x:%02x:%02x:%02x\n",
      manage->payload.foreign.foreignMasterUuid[0], manage->payload.foreign.foreignMasterUuid[1],
      manage->payload.foreign.foreignMasterUuid[2], manage->payload.foreign.foreignMasterUuid[3],
      manage->payload.foreign.foreignMasterUuid[4], manage->payload.foreign.foreignMasterUuid[5]);

    printf("  foreignMasterPortId %u\n", manage->payload.foreign.foreignMasterPortId);
    printf("  foreignMasterSyncs %u\n", manage->payload.foreign.foreignMasterSyncs);
    break;

  case PTP_MM_NULL:
    printf("\n");
    displayHeader(header);
    printf("  managementMessageKey %u (NULL)\n", manage->managementMessageKey);
    break;

  default:
    break;
  }

  return;
}
