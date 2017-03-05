package com.morotsman.spring_boot_demo_app;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;

@Controller
@EnableAutoConfiguration
@ComponentScan
public class DemoApplication {

    private final RestTemplate restTemplate;

    private final ExecutorService executor = Executors.newFixedThreadPool(200);

    private final String importantUrl;
    private final String notImportantUrl;

    @Autowired
    public DemoApplication(@Value("${important.url}") String importantUrl,
            @Value("${not_important.url}") String notImportantUrl, 
            RestTemplate restTemplate) {
        this.importantUrl = importantUrl;
        this.notImportantUrl = notImportantUrl;
        this.restTemplate = restTemplate;
    }



    @RequestMapping("/important")
    @ResponseBody
    String important() {
        return restTemplate.getForObject(importantUrl, String.class);
    }

    @RequestMapping("/seq_aggregate")
    @ResponseBody
    String seqAggregate() {
        final String notImportantResult  =restTemplate.getForObject(notImportantUrl, String.class);
        final String importantResult = restTemplate.getForObject(importantUrl, String.class);     
        return importantResult + " ; " + notImportantResult;
    }    
    
    
    @RequestMapping("/aggregate")
    @ResponseBody
    String aggregate() {

        final Future<String> futureNotImportantResult = executor.submit(() -> {
            return restTemplate.getForObject(notImportantUrl, String.class);
        });

        final String importantResult = restTemplate.getForObject(importantUrl, String.class);
        String notImportantResult;
        try {
            notImportantResult = futureNotImportantResult.get(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            notImportantResult = "Nothing to important anyway";
        }
        return importantResult + " ; " + notImportantResult;
    }
    
  @ExceptionHandler({java.net.SocketTimeoutException.class})
  @ResponseBody
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public String requestTimeoutToServerException() {
    return "Timeout when calling external server.";
  }
  
  @ExceptionHandler({org.apache.http.conn.ConnectionPoolTimeoutException.class})
  @ResponseBody
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public String connectionTimeoutException() {
    return "It't took to long time to get connection from the internal pool.";
  }
  
  


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
