/*
 * Copyright (C) 2009-2012 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.history.suite;

import com.sonar.it.ItUtils;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.locator.MavenLocation;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  HistoryTest.class, SinceLastVersionHistoryTest.class, SinceXDaysHistoryTest.class, TimeMachinePageTest.class,
  DifferentialPeriodsTest.class
})
public class HistoryTestSuite {

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = Orchestrator.builderEnv()
    .addPlugin(ItUtils.xooPlugin())
    .addPlugin(MavenLocation.of("org.codehaus.sonar-plugins.java", "sonar-checkstyle-plugin", "2.1"))
    .addPlugin(MavenLocation.of("org.codehaus.sonar-plugins.java", "sonar-pmd-plugin", "2.1"))
    .build();
}
