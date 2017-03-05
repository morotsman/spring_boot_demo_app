package com.morotsman.spring_boot_demo_app;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class DemoApplication {

    private final RestTemplate restTemplate;

    private final ExecutorService executor = Executors.newFixedThreadPool(200);

    private final String notImportantUrl;

    @Autowired
    public DemoApplication(@Value("${not_important.url}") String notImportantUrl, 
            RestTemplate restTemplate) {
        this.notImportantUrl = notImportantUrl;
        this.restTemplate = restTemplate;
    }

    public void run(String... args)  {
        
        final Stream<Callable<String>> tasks = IntStream.range(0,201).mapToObj(i -> () -> restTemplate.getForObject(notImportantUrl, String.class));
        
        long startTime = System.currentTimeMillis();
        try {
            
            List<Future<String>> furureResults = executor.invokeAll(tasks.collect(Collectors.toList()));
           
            
             for (Future<String> f : furureResults)
             {
                try {
                    System.out.println(f.get());
                } catch (ExecutionException ex) {
                    Logger.getLogger(DemoApplication.class.getName()).log(Level.SEVERE, null, ex);
                }
             }

            
        } catch (InterruptedException ex) {
            Logger.getLogger(DemoApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Completed in: " + (System.currentTimeMillis() - startTime) + " ms.");
        
    }
    
    
 
  


    public static void main(String[] args) {
        
        ConfigurableApplicationContext ctx = SpringApplication.run(DemoApplication.class, args);

        DemoApplication mainObj = ctx.getBean(DemoApplication.class);

        mainObj.run();
    }
}
