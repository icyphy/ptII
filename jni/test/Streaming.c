int tmb_type_queue=0;
long VLDSynP(long* mb_type_queue,
                         long* mb_cbp_queue,
                         long* mb_acpred_queue,
                         long* mb_dqp_queue,
                         long* mb_dc_queue,
                         long* mb_ac_queue)
{

        mb_type_queue[0]   = tmb_type_queue;
        mb_cbp_queue[0]    = tmb_type_queue;
        mb_acpred_queue[0] = tmb_type_queue;
        mb_dqp_queue[0]    = tmb_type_queue;
        mb_dc_queue[0]     = tmb_type_queue;
        mb_ac_queue[0]     = tmb_type_queue;

        tmb_type_queue++;
        return 0;

}
