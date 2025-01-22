package org.propertymanagement.associationmeeting;

import org.propertymanagement.associationmeeting.config.NoOpConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = {
        NoOpConfig.class
})
public class PropertyManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PropertyManagementApplication.class, args);
    }
}
