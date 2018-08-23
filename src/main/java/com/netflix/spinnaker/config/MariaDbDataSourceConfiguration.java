/*
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spinnaker.config;

import com.netflix.spectator.api.Registry;
import com.netflix.spinnaker.orca.sql.MariaDbConnectionPoolMetricsExporter;
import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@ConditionalOnProperty("sql.enabled")
public class MariaDbDataSourceConfiguration {

  @Bean
  @DependsOn("liquibase")
  public DataSource dataSource(SqlProperties properties) {
    try {
      MariaDbPoolDataSource dataSource = new MariaDbPoolDataSource(properties.getConnectionPool().getJdbcUrl());
      dataSource.setUser(properties.getConnectionPool().getUser());
      dataSource.setPassword(properties.getConnectionPool().getPassword());
      return dataSource;
    } catch (SQLException e) {
      throw new BeanCreationException("Failed creating pooled data source", e);
    }
  }

  @Bean
  public MariaDbConnectionPoolMetricsExporter connectionPoolMetricsExporter(MariaDbPoolDataSource dataSource,
                                                                            Registry registry) {
    return new MariaDbConnectionPoolMetricsExporter(dataSource, registry);
  }
}
