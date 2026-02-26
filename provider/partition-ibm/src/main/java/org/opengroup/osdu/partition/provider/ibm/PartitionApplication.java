
/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.provider.ibm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan({"org.opengroup.osdu"})
@SpringBootApplication
@PropertySource("classpath:swagger.properties")
public class PartitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartitionApplication.class, args);
    }
}
