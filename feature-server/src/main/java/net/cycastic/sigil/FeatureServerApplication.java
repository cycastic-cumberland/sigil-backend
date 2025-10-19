package net.cycastic.sigil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FeatureServerApplication {
    static {
        LambdaHandler.loadClass();
    }

    public static void main(String[] args) {
        if (System.getenv("AWS_EXECUTION_ENV") != null){
            return;
        }

        SpringApplication.run(FeatureServerApplication.class, args);
    }
}
