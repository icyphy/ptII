//*****************************************************************************
//
// luminaryif.c - Ethernet Interface File for lwIP TCP/IP Stack
//
//*****************************************************************************

#include "../../../hw_memmap.h"
#include "../../../hw_types.h"
#include "../../../hw_ints.h"
#include "../../../hw_ethernet.h"
#include "../../../src/ethernet.h"
#include "../../../src/interrupt.h"
#include "../../../src/sysctl.h"
#include "lwip/opt.h"
#include "lwip/api.h"
#include "lwip/tcpip.h"
#include "lwip/def.h"
#include "lwip/mem.h"
#include "lwip/pbuf.h"
#include "lwip/sys.h"
#include "lwip/stats.h"
#include "netif/etharp.h"
#if LWIP_PTPD
#include "ptpd.h"
extern void getRxTime(TimeInternal *t);
#endif

//*****************************************************************************
//
// Sanity Check:  This module will NOT work if the following defines
// are incorrect.
//
//*****************************************************************************
#if (PBUF_LINK_HLEN != 16)
#error "Incorrect PBUF_LINK_HLEN specified!"
#endif
#if (ETH_PAD_SIZE != 2)
#error "Incorrect ETH_PAD_SIZE specified!"
#endif
#if (PBUF_POOL_BUFSIZE % 4)
#error "PBUF_POOL_BUFSIZE must be modulo 4!"
#endif

//*****************************************************************************
//
// Ethernet Configuration Structure for this interface.
//
//*****************************************************************************
struct ethernetif
{
    struct eth_addr *ethaddr;
};

#if ENABLE_ETHERNET_QUEUE_IF
//*****************************************************************************
//
// When using the ethernet queue interface, SYS_LIGHTWEIGHT_PROT must be set to
// 1 to enable protection of the pbuf management functions. Using this model,
// pbuf functions will be called under both interrupt and task context so
// protection is vital.
//
//*****************************************************************************
#if (SYS_LIGHTWEIGHT_PROT == 0)
#error "SYS_LIGHTWEIGHT_PROT must be 1 when using ENABLE_ETHERNET_QUEUE_IF"
#endif

//*****************************************************************************
//
// If the queued ethernet input interface is enabled, the following variables
// and macros are used to manage the ethernet frame queue. In this mode, the
// application should call ethernetif_queue() from the low level Ethernet
// interrupt handler to read a packet from the hardware and add it to the
// queue for later processing. The application task should later call
// ethernetif_dequeue to retrieve the next packet and process it (assuming any
// packet is available for processing).
//
// The ethernet queue is really a FIFO implemented using a circular buffer
// with read and write pointers. The FIFO is empty when the read and write
// pointers are equal.
//
// g_ulQueueWrite holds the index of the next empty entry.
// g_ulQueueRead  holds the index of the next full entry to be read.
//
// This means that we waste one entry to allow us to differentiate between the
// full and empty conditions but this is likely acceptable since the only
// information in each queue entry is a single pointer.
//*****************************************************************************
static unsigned long g_ulQueueRead;
static unsigned long g_ulQueueWrite;
static unsigned long g_ulEthernetOverflow = 0;
static struct pbuf *g_pEthernetQueue[ETHERNET_QUEUE_IF_BUFFER + 1];

#define ETHERNET_QUEUE_EMPTY ((g_ulQueueRead == g_ulQueueWrite) ? true : false)
#define ETHERNET_QUEUE_FULL \
    ((g_ulQueueRead != 0) ? \
        ((g_ulQueueWrite == (g_ulQueueRead - 1))) : \
        ((g_ulQueueWrite == ETHERNET_QUEUE_IF_BUFFER)))

//*****************************************************************************
//
// Prototypes for internal queue management functions.
//
//*****************************************************************************
static tBoolean enqueue_frame(struct pbuf *pBuf);
static struct pbuf *dequeue_frame(void);
#endif

//*****************************************************************************
//
// Low-Level initialization function for the Ethernet Controller.
//
//*****************************************************************************
void
low_level_init(struct netif *netif)
{
    unsigned long ulTemp;

#if ENABLE_ETHERNET_QUEUE_IF
    //
    // Empty the ethernet frame queue
    //
    g_ulQueueRead = 0;
    g_ulQueueWrite = 0;
#endif

    //
    // Disable all Ethernet Interrupts.
    //
    EthernetIntDisable(ETH_BASE, (ETH_INT_PHY | ETH_INT_MDIO | ETH_INT_RXER |
                                  ETH_INT_RXOF | ETH_INT_TX | ETH_INT_TXER |
                                  ETH_INT_RX));
    ulTemp = EthernetIntStatus(ETH_BASE, false);
    EthernetIntClear(ETH_BASE, ulTemp);

    //
    // Initialize the Ethernet Controller.
    //
    EthernetInitExpClk(ETH_BASE, SysCtlClockGet());

    //
    // Configure the Ethernet Controller for normal operation.
    // - Enable TX Duplex Mode
    // - Enable TX Padding
    // - Enable TX CRC Generation
    //
    EthernetConfigSet(ETH_BASE, (ETH_CFG_TX_DPLXEN |
                                 ETH_CFG_TX_CRCEN | ETH_CFG_TX_PADEN));

    //
    // Enable the Ethernet Controller transmitter and receiver.
    //
    EthernetEnable(ETH_BASE);

    //
    // Enable the Ethernet Interrupt handler.
    //
    IntEnable(INT_ETH);

    //
    // Enable Ethernet TX and RX Packet Interrupts.
    //
    EthernetIntEnable(ETH_BASE, ETH_INT_RX | ETH_INT_TX);
}

//****************************************************************************
//
// Low-Level transmit routine.  Should do the actual transmission of the
// packet. The packet is contained in the pbuf that is passed to the function.
// This pbuf might be chained.
//
//****************************************************************************
err_t
low_level_output(struct netif *netif, struct pbuf *p)
{
    struct pbuf *q;
    int i;
    unsigned char *pucBuf;
    unsigned long ulTemp;
    unsigned char *pucTemp = (unsigned char *)&ulTemp;

    //
    // Wait for space available in the TX FIFO.
    //
    while(!EthernetSpaceAvail(ETH_BASE))
    {
    }

    //
    // Fill in the first two bytes of the payload data (configured as padding
    // with ETH_PAD_SIZE = 2) with the total length of the payload data
    // (minus the Ethernet MAC layer header).
    //
    pucBuf = p->payload;
    ulTemp = p->tot_len - 16;
    *pucBuf++ = pucTemp[0];
    *pucBuf++ = pucTemp[1];

    //
    // Copy data from the pbuf(s) into the TX Fifo.
    // For now, assume every pbuf is full, except possibly the last one.
    //
    for(q = p; q != NULL; q = q->next)
    {
        pucBuf = q->payload;

        //
        // Send the data from the pbuf to the interface, one pbuf at a
        // time. The size of the data in each pbuf is kept in the ->len
        // variable.
        //
        for(i = 0; i < q->len; i += 4)
        {
            pucTemp[0] = *pucBuf++;
            pucTemp[1] = *pucBuf++;
            pucTemp[2] = *pucBuf++;
            pucTemp[3] = *pucBuf++;
            HWREG(ETH_BASE + MAC_O_DATA) = ulTemp;
        }
    }

    //
    // Wakeup the transmitter.
    //
    HWREG(ETH_BASE + MAC_O_TR) = MAC_TR_NEWTX;

#if LINK_STATS
    lwip_stats.link.xmit++;
#endif

    return ERR_OK;
}

//*****************************************************************************
//
// Low-Level receive routine.  Should allocate a pbuf and transfer the bytes
// of the incoming packet from the interface into the pbuf.
//
//*****************************************************************************
struct pbuf *
low_level_input(struct netif *netif)
{
    struct pbuf *p, *q;
    u16_t len;
    unsigned long ulTemp;
    int i;
    unsigned char *ptr;
    unsigned char *pucTemp = (unsigned char *)&ulTemp;
#if LWIP_PTPD
    TimeInternal tTimestamp;

    //
    // Get the current timestamp for this packet.
    //
    getRxTime(&tTimestamp);
#endif

    //
    // Obtain the size of the packet and put it into the "len" variable.
    // Note:  The length returned in the FIFO length position includes the
    // two bytes for the length + the 4 bytes for the FCS.
    //
    ulTemp = HWREG(ETH_BASE + MAC_O_DATA);
    len = ulTemp & 0xFFFF;

    //
    // We allocate a pbuf chain of pbufs from the pool.
    //
    p = pbuf_alloc(PBUF_RAW, len, PBUF_POOL);

    if(p != NULL)
    {
        //
        // Place the first word into the first pbuf location.
        //
        *(unsigned long *)p->payload = ulTemp;
        p->payload = (char *)(p->payload) + 4;
        p->len -= 4;

        //
        // Process all but the last buffer in the pbuf chain.
        //
        q = p;
        while(q->next != NULL)
        {
            //
            // Setup a byte pointer into the payload section of the pbuf.
            //
            ptr = q->payload;

            //
            // Read data from FIFO into the current pbuf
            // (assume pbuf length is modulo 4)
            //
            for(i = 0; i < q->len; i += 4)
            {
                ulTemp = HWREG(ETH_BASE + MAC_O_DATA);
                *ptr++ = pucTemp[0];
                *ptr++ = pucTemp[1];
                *ptr++ = pucTemp[2];
                *ptr++ = pucTemp[3];
            }

            //
            // Link in the next pbuf in the chain.
            //
            q = q->next;
        }

        //
        // Process the last pbuf in the list.
        //
        ptr = q->payload;

        //
        // Read data from FIFO into the current pbuf, omitting the
        // last 1-3 bytes.
        //
        for(i = 0; i < (q->len - 3); i += 4)
        {
            ulTemp = HWREG(ETH_BASE + MAC_O_DATA);
            *ptr++ = pucTemp[0];
            *ptr++ = pucTemp[1];
            *ptr++ = pucTemp[2];
            *ptr++ = pucTemp[3];
        }

        //
        // If needed, get the last 1-3 bytes of data into the pbuf.
        //
        if(i < q->len)
        {
            ulTemp = HWREG(ETH_BASE + MAC_O_DATA);
            while(i < q->len)
            {
                *ptr++ = ulTemp & 0xFF;
                i++;
                ulTemp = ulTemp >> 8;
            }
        }

        //
        // Restore the first pbuf parameters to their original values.
        //
        p->payload = (char *)(p->payload) - 4;
        p->len += 4;

#if LINK_STATS
        lwip_stats.link.recv++;
#endif
#if LWIP_PTPD

        //
        // Place the timestamp in the PBUF
        //
        p->timestamp.seconds = tTimestamp.seconds;
        p->timestamp.nanoseconds = tTimestamp.nanoseconds;
#endif
    }
    else
    {
        //
        // Just read all of the remaining data from the FIFO and dump it.
        //
        for(i = 4; i < len; i+=4)
        {
            ulTemp = HWREG(ETH_BASE + MAC_O_DATA);
        }

#if LINK_STATS
        lwip_stats.link.memerr++;
        lwip_stats.link.drop++;
#endif
    }

    return p;
}

//*****************************************************************************
//
// Output Routine.  This function is called by the TCP/IP stack when an IP
// packet should be sent. It calls the function called low_level_output() to
// do the actual transmission of the packet.
//
//*****************************************************************************
err_t
ethernetif_output(struct netif *netif, struct pbuf *p,
                  struct ip_addr *ipaddr)
{
    //
    // Resolve the hardware address, then send the packet.
    //
    return(etharp_output(netif, ipaddr, p));
}

//*****************************************************************************
//
// Input Routine.  This function should be called when a packet is ready to be
// read from the interface. It uses the function low_level_input() that should
// handle the actual reception of bytes from the network interface.
//
//*****************************************************************************
void
ethernetif_input(struct netif *netif)
{
    struct ethernetif *ethernetif = netif->state;
    struct eth_hdr *ethhdr;
    struct pbuf *p;

    if(!EthernetPacketAvail(ETH_BASE))
    {
        return;
    }

    //
    // Move received packet into a new pbuf using the low-level input
    // routine.
    //
    p = low_level_input(netif);

    //
    // No packet could be read.
    //
    if(p == NULL)
    {
        return;
    }

    //
    // Setup pointer to the Ethernet Header.
    //
    ethhdr = p->payload;

#if LINK_STATS
    lwip_stats.link.recv++;
#endif

    //
    // Determine the type of packet (IP or ARP) and process accordingly.
    //
    switch(htons(ethhdr->type))
    {
        case ETHTYPE_IP:
            //
            // Update the ARP table.
            //
            etharp_ip_input(netif, p);

            //
            // Skip the Ethernet header.
            //
            pbuf_header(p, -((s16_t)sizeof(struct eth_hdr)));

            //
            // Pass the packet to the network layer.
            //
            netif->input(p, netif);
            break;

        case ETHTYPE_ARP:
            //
            // pass the packet to the ARP layer.
            //
            etharp_arp_input(netif, ethernetif->ethaddr, p);
            break;

        default:
            //
            // Ignore the packet if it is not IP or ARP.
            //
            pbuf_free(p);
            p = NULL;
            break;
    }
}

#if ENABLE_ETHERNET_QUEUE_IF
//*****************************************************************************
//
// When using the queued ethernet interface, this function should be called
// from the ethernet receive interrupt handler to read a frame and add it to
// the internal queue for later processing via a call to ethernetif_dequeue().
//
// The function returns true if a packet is read and enqueued or false if
// no packet is available or an error occurred that prevented the packet from
// being enqueued. In failure conditions, the frame data will be read from
// the hardware FIFO and discarded.
//
//*****************************************************************************
tBoolean
ethernetif_enqueue(struct netif *netif)
{
    struct pbuf *p;
    tBoolean bRetcode;


    if(!EthernetPacketAvail(ETH_BASE))
    {
        //
        // No packet is available so return false to indicate that we didn't
        // enqueue anything.
        //
        return(false);
    }

    //
    // Move received packet into a new pbuf using the low-level input
    // routine.
    //
    p = low_level_input(netif);

    //
    // No packet could be read.
    //
    if(p == NULL)
    {
        return(false);
    }

    //
    // Add the frame to the queue for later processing using a call to
    // ethernetif_dequeue().
    //
    bRetcode = enqueue_frame(p);

    //
    // Did we successfully add the packet to the queue?
    //
    if(!bRetcode)
    {
        //
        // No - something went wrong so discard the frame.
        //
        pbuf_free(p);
    }

    //
    // Tell the caller whether we successfully enqueued a frame or not.
    //
    return(bRetcode);
}

//*****************************************************************************
//
// Input Routine.  This function should be called from task context to
// read and process the next frame from the ethernet queue. Frames are written
// to the queue from the ethernet receive interrupt handler using a matching
// call to ethernetif_enqueue().
//
//*****************************************************************************
void
ethernetif_dequeue(struct netif *netif)
{
    struct ethernetif *ethernetif = netif->state;
    struct eth_hdr *ethhdr;
    struct pbuf *p;

    //
    // Get the top frame from the queue
    //
    p = dequeue_frame();

    //
    // No packet could be read.
    //
    if(p == NULL)
    {
        return;
    }

    //
    // Setup pointer to the Ethernet Header.
    //
    ethhdr = p->payload;

#if LINK_STATS
    lwip_stats.link.recv++;
#endif

    //
    // Determine the type of packet (IP or ARP) and process accordingly.
    //
    switch(htons(ethhdr->type))
    {
        case ETHTYPE_IP:
            //
            // Update the ARP table.
            //
            etharp_ip_input(netif, p);

            //
            // Skip the Ethernet header.
            //
            pbuf_header(p, -((s16_t)sizeof(struct eth_hdr)));

            //
            // Pass the packet to the network layer.
            //
            netif->input(p, netif);
            break;

        case ETHTYPE_ARP:
            //
            // pass the packet to the ARP layer.
            //
            etharp_arp_input(netif, ethernetif->ethaddr, p);
            break;

        default:
            //
            // Ignore the packet if it is not IP or ARP.
            //
            pbuf_free(p);
            p = NULL;
            break;
    }
}
#endif // ENABLE_ETHERNET_QUEUE_IF

//*****************************************************************************
//
// ARP Timer Callback Function
//
//*****************************************************************************
void
arp_timer(void *arg)
{
    etharp_tmr();
    sys_timeout(ARP_TMR_INTERVAL, arp_timer, NULL);
}

//*****************************************************************************
//
// Should be called at the beginning of the program to set up the
// network interface. It calls the function low_level_init() to do the
// actual setup of the hardware.
//
//*****************************************************************************
err_t
ethernetif_init(struct netif *netif)
{
    struct ethernetif *ethernetif;

    ethernetif = mem_malloc(sizeof(struct ethernetif));

    if(ethernetif == NULL)
    {
        LWIP_DEBUGF(NETIF_DEBUG, ("ethernetif_init: out of memory\n"));
        return ERR_MEM;
    }

    netif->state = ethernetif;
    netif->name[0] = 'l';
    netif->name[1] = 'm';
    netif->hwaddr_len = 6;
    netif->mtu = 1500;
    netif->flags = NETIF_FLAG_BROADCAST;
    netif->output = ethernetif_output;
    netif->linkoutput = low_level_output;

    EthernetMACAddrGet(ETH_BASE, &(netif->hwaddr[0]));
    ethernetif->ethaddr = (struct eth_addr *)&(netif->hwaddr[0]);

    low_level_init(netif);

    etharp_init();

    sys_timeout(ARP_TMR_INTERVAL, arp_timer, NULL);

    return ERR_OK;
}

#if SYS_LIGHTWEIGHT_PROT
//*****************************************************************************
//
// This function is used to lock access to critical sections when lwipopt.h
// defines SYS_LIGHTWEIGHT_PROT. It disables interrupts and returns a value
// indicating the interrupt enable state when the function entered. This
// value must be passed back on the matching call to sys_arch_unprotect().
//
//*****************************************************************************
sys_prot_t
sys_arch_protect(void)
{
    tBoolean bRet = 1;

    bRet = IntMasterDisable();

    return((sys_prot_t)bRet);
}
//*****************************************************************************
//
// This function is used to unlock access to critical sections when lwipopt.h
// defines SYS_LIGHTWEIGHT_PROT. It enables interrupts if the value of the lev
// parameter indicates that they were enabled when the matching call to
// sys_arch_protect() was made.
//
//*****************************************************************************
void
sys_arch_unprotect(sys_prot_t lev)
{
    //
    // Only turn interrupts back on if they were originally on when the
    // matching sys_arch_protect() call was made.
    //
    if(!(lev & 1))
    {
        IntMasterEnable();
    }
}
#endif /* SYS_LIGHTWEIGHT_PROT */

#if ENABLE_ETHERNET_QUEUE_IF
//*****************************************************************************
//
// Return the ethernet frame from the top of the queue or NULL if the queue
// is empty.
//
//*****************************************************************************
static struct pbuf *
dequeue_frame(void)
{
    sys_prot_t Prot;
    struct pbuf *pBuf;

    //
    // All access to the ethernet queue must be made within a critical section
    // to ensure read and write pointers remain consistent. If we don't do
    // this, we end up with possible race conditions that could corrupt the
    // list.
    //
    Prot = sys_arch_protect();

    if(ETHERNET_QUEUE_EMPTY)
    {
        //
        // If the queue is empty, just return NULL.
        pBuf = (struct pbuf *)NULL;
    }
    else
    {
        //
        // The queue is not empty so return the next frame from it
        // and adjust the read pointer accordingly.
        //
        pBuf = g_pEthernetQueue[g_ulQueueRead];
        g_ulQueueRead = (g_ulQueueRead == ETHERNET_QUEUE_IF_BUFFER) ?
                        0 : (g_ulQueueRead + 1);
    }

    sys_arch_unprotect(Prot);

    return(pBuf);
}

//*****************************************************************************
//
// Add a new ethernet frame to the queue. If no space is available, return
// false, else return true.
//
//*****************************************************************************
static tBoolean
enqueue_frame(struct pbuf *pBuf)
{
    sys_prot_t Prot;
    tBoolean bRetcode;

    //
    // All access to the ethernet queue must be made from within a critical
    // section to ensure read and write pointers remain consistent. If we
    // don't do this, we end up with possible race conditions that could
    // corrupt the queue.
    //
    Prot = sys_arch_protect();

    if(!ETHERNET_QUEUE_FULL)
    {
        //
        // The queue isn't full so we add the new frame at the current
        // write position and move the write pointer.
        //
        g_pEthernetQueue[g_ulQueueWrite] = pBuf;
        g_ulQueueWrite = (g_ulQueueWrite == ETHERNET_QUEUE_IF_BUFFER) ?
                        0 : (g_ulQueueWrite + 1);
        bRetcode = true;
    }
    else
    {
        //
        // The stack is full so we are throwing away this value.  Keep track
        // of the number of times this happens.
        //
        g_ulEthernetOverflow++;
        bRetcode = false;
    }

    //
    // We're done so end the critical section.
    //
    sys_arch_unprotect(Prot);

    //
    // Let the caller know whether we were successful or not.
    //
    return(bRetcode);
}
#endif // ENABLE_ETHERNET_QUEUE_IF
