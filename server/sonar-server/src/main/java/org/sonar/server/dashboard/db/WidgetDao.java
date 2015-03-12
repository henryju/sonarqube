/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
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
package org.sonar.server.dashboard.db;

import org.sonar.api.utils.System2;
import org.sonar.core.dashboard.WidgetDto;
import org.sonar.core.dashboard.WidgetMapper;
import org.sonar.core.persistence.DbSession;
import org.sonar.server.db.BaseDao;

import java.util.Collection;

public class WidgetDao extends BaseDao<WidgetMapper, WidgetDto, Long> {

  public WidgetDao(System2 system2) {
    super(WidgetMapper.class, system2);
  }

  @Override
  protected WidgetDto doGetNullableByKey(DbSession session, Long widgetId) {
    return mapper(session).selectById(widgetId);
  }

  @Override
  protected WidgetDto doUpdate(DbSession session, WidgetDto item) {
    mapper(session).update(item);
    return item;
  }

  public Collection<WidgetDto> findByDashboard(DbSession session, long dashboardKey) {
    return mapper(session).selectByDashboard(dashboardKey);
  }

  public Collection<WidgetDto> findAll(DbSession session) {
    return mapper(session).selectAll();
  }
}
