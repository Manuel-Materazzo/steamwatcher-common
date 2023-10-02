package com.steamwatcher.utils;

import org.quartz.*;

public class JobUtils {

    private JobUtils(){}

    /**
     * Schedule a job to fire instantly with the given details
     * @param scheduler the scheduler where the job will be scheduled
     * @param jobDetail details of the job
     * @param dataMap data to inject inside the job
     * @param triggerKey name of the trigger
     * @throws SchedulerException if something goes wrong while scheduling
     */
    public static void fireJob(Scheduler scheduler, JobDetail jobDetail, JobDataMap dataMap, String triggerKey)
            throws SchedulerException {
        // creo una chiave trigger di mockup
        TriggerKey tkey = new TriggerKey(triggerKey);

        // se il trigger con questa chiave esiste già, elimino quello vecchio
        if (scheduler.checkExists(tkey)) {
            scheduler.unscheduleJob(tkey);
        }

        // creo il nuovo trigger per questo job e injecto i dati dello sheet
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(triggerKey)
                .withDescription("")
                .usingJobData(dataMap)
                .startNow()
                .build();

        // comunico allo scheduler che c'è un job da far runnare
        scheduler.scheduleJob(trigger);
    }
}
