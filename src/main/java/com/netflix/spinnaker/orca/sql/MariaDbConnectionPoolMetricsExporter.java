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
package com.netflix.spinnaker.orca.sql;

import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.patterns.PolledMeter;
import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.mariadb.jdbc.internal.util.pool.Pool;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

public class MariaDbConnectionPoolMetricsExporter {

  private final MariaDbPoolDataSource dataSource;

  private final AtomicLong activeConnections = new AtomicLong();
  private final AtomicLong totalConnections = new AtomicLong();
  private final AtomicLong idleConnections = new AtomicLong();
  private final AtomicLong blockedConnections = new AtomicLong();

  public MariaDbConnectionPoolMetricsExporter(MariaDbPoolDataSource dataSource, Registry registry) {
    this.dataSource = dataSource;

    monitorValue(registry, "active", activeConnections);
    monitorValue(registry, "total", totalConnections);
    monitorValue(registry, "idle", idleConnections);
    monitorValue(registry, "blocked", blockedConnections);
  }

  @Scheduled(fixedRate = 30_000)
  public void record() {
    // They tell me not to use test methods, but yolo. Way better than dealing with MBeans.
    Pool pool = dataSource.testGetPool();

    activeConnections.set(pool.getActiveConnections());
    totalConnections.set(pool.getTotalConnections());
    idleConnections.set(pool.getIdleConnections());
    blockedConnections.set(pool.getConnectionRequests());
  }

  private void monitorValue(Registry registry, String name, AtomicLong value) {
    PolledMeter
      .using(registry)
      .withName(format("sql.pool.%s.%s", dataSource.getPoolName(), name))
      .monitorValue(value);
  }
}

