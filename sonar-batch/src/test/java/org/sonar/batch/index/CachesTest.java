/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.batch.index;

import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.CoreProperties;
import org.sonar.batch.bootstrap.BatchTempUtils;
import org.sonar.batch.bootstrap.BootstrapProperties;
import org.sonar.batch.bootstrap.BootstrapSettings;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class CachesTest {

  @ClassRule
  public static TemporaryFolder temp = new TemporaryFolder();

  public static Caches createCacheOnTemp(TemporaryFolder temp) {
    BootstrapSettings bootstrapSettings = new BootstrapSettings(new BootstrapProperties(Collections.emptyMap()));
    try {
      bootstrapSettings.properties().put(CoreProperties.WORKING_DIRECTORY, temp.newFolder().getAbsolutePath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new Caches(new BatchTempUtils(bootstrapSettings));
  }

  Caches caches;

  @Before
  public void prepare() throws Exception {
    caches = createCacheOnTemp(temp);
  }

  @After
  public void stop() {
    caches.stop();
  }

  @Test
  public void should_stop_and_clean_temp_dir() throws Exception {
    File tempDir = caches.tempDir();
    assertThat(tempDir).isDirectory().exists();
    assertThat(caches.persistit()).isNotNull();
    assertThat(caches.persistit().isInitialized()).isTrue();

    caches.stop();

    assertThat(tempDir).doesNotExist();
    assertThat(caches.tempDir()).isNull();
    assertThat(caches.persistit()).isNull();
  }

  @Test
  public void should_create_cache() throws Exception {
    caches.start();
    Cache<String, Element> cache = caches.createCache("foo");
    assertThat(cache).isNotNull();
  }

  @Test
  public void should_not_create_cache_twice() throws Exception {
    caches.start();
    caches.<String, Element>createCache("foo");
    try {
      caches.<String, Element>createCache("foo");
      fail();
    } catch (IllegalStateException e) {
      // ok
    }
  }

  static class Element implements Serializable {

  }
}
