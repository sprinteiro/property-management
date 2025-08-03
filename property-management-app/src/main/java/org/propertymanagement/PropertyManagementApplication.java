package org.propertymanagement;

import org.propertymanagement.associationmeeting.config.AssociationMeetingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AssociationMeetingConfig.class})
public class PropertyManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PropertyManagementApplication.class, args);
    }
}
