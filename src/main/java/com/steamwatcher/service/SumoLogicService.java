package com.steamwatcher.service;

import com.steamwatcher.exceptions.ExceptionUtils;
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


@Service
@Scope("singleton")
public class SumoLogicService {

    private static final Logger logger = LoggerFactory.getLogger(SumoLogicService.class);
    private final RestService restService;
    private static final String MICROSERVICE_NAME = "buff-batch";
    private static final String LOG_FORMAT = "%s | %s: %s";

    @Value("${csgotracker.aggregator.uri:}")
    private String uri;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Autowired
    SumoLogicService(RestService restService) {
        this.restService = restService;
    }

    public void info(String jobName, String workerName, String message, String trace) {

        String logMessage = String.format(LOG_FORMAT, jobName, workerName, message);
        logger.info(logMessage);
        logger.info(trace);
        sendLogs(jobName, workerName, message, trace, "info");
    }

    public void info(String jobName, String workerName, String message) {

        String logMessage = String.format(LOG_FORMAT, jobName, workerName, message);
        logger.info(logMessage);
        sendLogs(jobName, workerName, message, "", "info");
    }

    public void warn(String jobName, String workerName, String message, String trace) {

        String logMessage = String.format(LOG_FORMAT, jobName, workerName, message);
        logger.warn(logMessage);
        logger.warn(trace);
        sendLogs(jobName, workerName, message, trace, "warning");
    }

    public void error(String jobName, String workerName, String message, String trace) {

        String logMessage = String.format(LOG_FORMAT, jobName, workerName, message);
        logger.error(logMessage);
        logger.error(trace);
        sendLogs(jobName, workerName, message, trace, "error");
    }

    public void error(String jobName, String workerName, String message, Exception exception) {

        String logMessage = String.format(LOG_FORMAT, jobName, workerName, message);
        logger.error(logMessage);

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String exceptionString = sw.toString();

        String trace = String.format("%s %n %s", exception.getMessage(), exceptionString);

        logger.error(trace);

        sendLogs(jobName, workerName, message, trace, "error");
    }

    private void sendLogs(String jobName, String workerName, String message, String trace, String severity) {
        try {

            if (uri == null || uri.isBlank()) {
                logger.error("Sumo Logic url is empty!");
                return;
            }

            // escaping della traccia
            trace = trace.replace("\\", "\\\\");
            trace = trace.replace("\"", "\\\"");
            trace = trace.replace("\b", "\\b");
            trace = trace.replace("\f", "\\f");
            trace = trace.replace("\n", "\\n");
            trace = trace.replace("\r", "\\r");
            trace = trace.replace("\t", "\\t");

            String body = String.format("{" +
                    "\"severity\": \"%s\"," +
                    "\"microservice\": \"%s\"," +
                    "\"job\": \"%s\"," +
                    "\"worker\": \"%s\"," +
                    "\"message\": \"%s\"," +
                    "\"trace\": \"%s\"" +
                    "} ", severity, MICROSERVICE_NAME, jobName, workerName, message, trace);

            Request request = new Request.Builder()
                    .url(uri)
                    .method("POST", RequestBody.create(body, JSON))
                    .build();

            // parsing della risposta
            Response response = restService.executeRequest(request);

            if (!response.isSuccessful()) {
                logger.error("Error while sending logs to Sumo Logic!, server responded with {}", response.code());
            }

            response.close();

        } catch (Exception e) {
            ExceptionUtils.printError(e, "Error while sending logs to Sumo Logic!", logger);
        }
    }


}
