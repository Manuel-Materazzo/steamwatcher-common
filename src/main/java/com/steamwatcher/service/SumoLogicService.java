package com.steamwatcher.service;

import com.steamwatcher.dto.enums.Severity;
import com.steamwatcher.utils.ExceptionUtils;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


@Service
@Scope("singleton")
public class SumoLogicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SumoLogicService.class);
    private final RestService restService;
    private static final String LOG_FORMAT = "%s | %s: %s";

    @Value("${csgotracker.aggregator.uri:}")
    public String uri;

    @Value("${csgotracker.microservice-name:}")
    public String microserviceName;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final List<String> infoQueue = new ArrayList<>();
    private final List<String> warningQueue = new ArrayList<>();

    @Autowired
    public SumoLogicService(RestService restService) {
        this.restService = restService;
    }

    public synchronized void info(String jobName, String workerName, String message, String trace) {

        String logMessage = String.format(LOG_FORMAT, jobName, workerName, message);
        LOGGER.info(logMessage);
        LOGGER.info(trace);

        String jsonLog = generateJsonLog(jobName, workerName, message, trace, Severity.INFO);

        // aggiungo i log alla lista
        infoQueue.add(jsonLog);

        // se la lista ha almeno 10 elementi
        if (infoQueue.size() > 9) {
            StringBuilder groupedLog = new StringBuilder();
            for (var log: infoQueue){
                groupedLog.append(log).append("\n");
            }
            // li mando tutti
            sendLogs(groupedLog.toString());
            infoQueue.clear();
        }

    }

    public synchronized void info(String jobName, String workerName, String message) {
        info(jobName, workerName, message, "");
    }

    public synchronized void warn(String jobName, String workerName, String message, String trace) {

        String logMessage = String.format(LOG_FORMAT, jobName, workerName, message);
        LOGGER.warn(logMessage);
        LOGGER.warn(trace);

        String jsonLog = generateJsonLog(jobName, workerName, message, trace, Severity.WARNING);
        // aggiungo i log alla lista
        warningQueue.add(jsonLog);

        // se la lista ha almeno 5 elementi
        if (warningQueue.size() > 4) {
            StringBuilder groupedLog = new StringBuilder();
            for (var log: warningQueue){
                groupedLog.append(log).append("\n");
            }
            // li mando tutti
            sendLogs(groupedLog.toString());
            warningQueue.clear();
        }
    }

    public synchronized void error(String jobName, String workerName, String message, String trace) {

        String logMessage = String.format(LOG_FORMAT, jobName, workerName, message);
        LOGGER.error(logMessage);
        LOGGER.error(trace);

        String jsonLog = generateJsonLog(jobName, workerName, message, trace, Severity.ERROR);
        sendLogs(jsonLog);
    }

    public synchronized void error(String jobName, String workerName, String message, Exception exception) {

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String exceptionString = sw.toString();

        String trace = String.format("%s %n %s", exception.getMessage(), exceptionString);

        error(jobName, workerName, message, trace);
    }

    private void sendLogs(String log) {
        try {

            if (uri == null || uri.isBlank()) {
                LOGGER.error("Sumo Logic url is empty!");
                return;
            }

            Request request = new Request.Builder()
                    .url(uri)
                    .method("POST", RequestBody.create(log, JSON))
                    .build();

            // parsing della risposta
            Response response = restService.executeRequest(request);

            if (!response.isSuccessful()) {
                LOGGER.error("Error while sending logs to Sumo Logic!, server responded with {}", response.code());
            }

            response.close();

        } catch (Exception e) {
            ExceptionUtils.printError(e, "Error while sending logs to Sumo Logic!", LOGGER);
        }
    }

    private String generateJsonLog(String jobName, String workerName, String message, String trace, Severity severity) {
        // escaping della traccia
        trace = trace.replace("\\", "\\\\");
        trace = trace.replace("\"", "\\\"");
        trace = trace.replace("\b", "\\b");
        trace = trace.replace("\f", "\\f");
        trace = trace.replace("\n", "\\n");
        trace = trace.replace("\r", "\\r");
        trace = trace.replace("\t", "\\t");

        return String.format("{" +
                "\"severity\": \"%s\"," +
                "\"microservice\": \"%s\"," +
                "\"job\": \"%s\"," +
                "\"worker\": \"%s\"," +
                "\"message\": \"%s\"," +
                "\"trace\": \"%s\"" +
                "} ", severity.value, microserviceName, jobName, workerName, message, trace);
    }


}
