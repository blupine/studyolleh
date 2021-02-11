package com.studyolleh.infra;

import org.testcontainers.containers.PostgreSQLContainer;

public abstract class AbstractContainerBaseTest {

    protected static final String testName = "thisistestname";
    static PostgreSQLContainer POSTGRE_SQL_CONTAINER;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer()
                .withDatabaseName("studytest");
        POSTGRE_SQL_CONTAINER.start();
    }

}
