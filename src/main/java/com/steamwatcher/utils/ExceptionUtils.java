package com.steamwatcher.utils;


import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

    private ExceptionUtils(){}

    /**
     * riduce il boilerplate per printare gli errori con lo stack trace
     */
    public static void printError(Exception e, String customMessage, Logger logger){
        logger.error(customMessage);
        logger.error(e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        logger.error(trace);
    }

    /**
     * riduce il boilerplate delle eccezioni con il wait e il retry
     */
    public static void handleExceptionAndWaitForRetry(
            String name,
            Exception e
    ) throws JobExecutionException {
        JobExecutionException jeex = new JobExecutionException(e.getMessage());
        // aspetto 120 secondi e continuo l'esecuzione dei job (riprovando anche quello attuale)
        try {
            Thread.sleep(120000);
        } catch (InterruptedException iex) {
            String message2 = String.format("[%s] Thread interrupted while waiting for retry", name);
            logger.error(message2);
            Thread.currentThread().interrupt();
        }
        jeex.setRefireImmediately(true);
        throw jeex;
    }

    /**
     * riduce il boilerplate delle interrupredExceptions
     */
    public static void handleMainJobException(
            String name,
            Exception e,
            Scheduler scheduler,
            JobKey jobKey
    ) throws JobExecutionException {

        String message = String.format("[%s] Terminated with an error!, retrying now!", name);
        logger.error(message);

        // provo a eliminare il job, per evitare che partano i trigger
        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException se) {

            message = String.format("Failed to delete %s!, last-resorting to clearing the scheduler," +
                    " this may lead to unexpected consequences", jobKey.getName());
            logger.error(message);

            // in caso di fallimento, provo a pulire la schedule
            try {
                scheduler.clear();
            }catch (SchedulerException se2){

                message = "Failed to clear the schedule!, i'm tired boss... i'm going to sleep...";
                logger.error(message);

                // se anche questo fallisce, spengo tutto e vaffanculo
                System.exit(1);
            }

        }

        // restarto il job, sperando che sta volta tutto vada per il verso giusto
        JobExecutionException jeex = new JobExecutionException(e.getMessage());
        jeex.setRefireImmediately(true);
        throw jeex;

    }
}
