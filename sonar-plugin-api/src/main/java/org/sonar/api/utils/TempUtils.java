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
package org.sonar.api.utils;

import org.sonar.api.ServerComponent;
import org.sonar.api.task.TaskComponent;

import javax.annotation.Nullable;

import java.io.File;

/**
 * Use this component to deal with temp files/folders. Root location of temp files/folders
 * depends on situation:
 * <ul>
 * <li>${SONAR_HOME}/temp on server side</li>
 * <li>Working directory on batch side (see sonar.working.directory)</li>
 * </ul>
 * @since 4.0
 *
 */
public interface TempUtils extends TaskComponent, ServerComponent {

  /**
   * Create a directory in temp folder with a random unique name.
   */
  File createTempDirectory();

  /**
   * Create a directory in temp folder with a random unique name and using the provided prefix when possible.
   */
  File createTempDirectory(@Nullable String prefix);

  /**
   * Create a directory in temp folder using provided name.
   */
  File createDirectory(String name);

  File createTempFile();

  File createTempFile(@Nullable String prefix, @Nullable String suffix);

}
