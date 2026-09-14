package com.innowise.paymentservice.configuration;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseConfig {

    @Bean
    public CommandLineRunner liquibaseRunner(@Value("${spring.data.mongodb.uri}") String mongoUrl) {

        return args -> {
            Database database = DatabaseFactory.getInstance()
                    .openDatabase(
                            mongoUrl,
                            null,
                            null,
                            null,
                            null
                    );

            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.yaml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update("");
        };
    }
}