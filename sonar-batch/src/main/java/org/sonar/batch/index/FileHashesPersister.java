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
package org.sonar.batch.index;

import org.sonar.api.database.model.Snapshot;
import org.sonar.core.persistence.DbSession;
import org.sonar.core.persistence.MyBatis;
import org.sonar.core.source.SnapshotDataTypes;
import org.sonar.core.source.db.SnapshotDataDao;
import org.sonar.core.source.db.SnapshotDataDto;

import java.util.Map;

/**
 * Store file hashes in snapshot_data for reuse in next analysis to know if a file is modified
 */
public class FileHashesPersister implements ScanPersister {
  private final ComponentDataCache data;
  private final SnapshotCache snapshots;
  private final SnapshotDataDao dao;
  private final MyBatis mybatis;

  public FileHashesPersister(ComponentDataCache data, SnapshotCache snapshots,
    SnapshotDataDao dao, MyBatis mybatis) {
    this.data = data;
    this.snapshots = snapshots;
    this.dao = dao;
    this.mybatis = mybatis;
  }

  @Override
  public void persist() {
    DbSession session = mybatis.openSession(true);
    try {
      for (Map.Entry<String, Snapshot> componentEntry : snapshots.snapshots()) {
        String componentKey = componentEntry.getKey();
        Snapshot snapshot = componentEntry.getValue();
        String fileHashesdata = data.getStringData(componentKey, SnapshotDataTypes.FILE_HASHES);
        if (fileHashesdata != null) {
          SnapshotDataDto dto = new SnapshotDataDto();
          dto.setSnapshotId(snapshot.getId());
          dto.setResourceId(snapshot.getResourceId());
          dto.setDataType(SnapshotDataTypes.FILE_HASHES);
          dto.setData(fileHashesdata);
          dao.insert(session, dto);
        }
      }
      session.commit();
    } finally {
      MyBatis.closeQuietly(session);
    }
  }
}