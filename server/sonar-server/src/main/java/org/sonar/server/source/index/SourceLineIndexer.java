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
package org.sonar.server.source.index;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.sonar.core.persistence.DbSession;
import org.sonar.server.db.DbClient;
import org.sonar.server.es.BaseIndexer;
import org.sonar.server.es.BulkIndexer;
import org.sonar.server.es.EsClient;

import java.sql.Connection;
import java.util.Iterator;

import static org.sonar.server.source.index.SourceLineIndexDefinition.FIELD_FILE_UUID;
import static org.sonar.server.source.index.SourceLineIndexDefinition.FIELD_PROJECT_UUID;

/**
 * Add to Elasticsearch index {@link SourceLineIndexDefinition} the rows of
 * db table FILE_SOURCES that are not indexed yet
 */
public class SourceLineIndexer extends BaseIndexer {

  private final DbClient dbClient;

  public SourceLineIndexer(DbClient dbClient, EsClient esClient) {
    super(esClient, 0L, SourceLineIndexDefinition.INDEX, SourceLineIndexDefinition.TYPE, SourceLineIndexDefinition.FIELD_UPDATED_AT);
    this.dbClient = dbClient;
  }

  @Override
  protected long doIndex(long lastUpdatedAt) {
    final BulkIndexer bulk = new BulkIndexer(esClient, SourceLineIndexDefinition.INDEX);
    bulk.setLarge(lastUpdatedAt == 0L);

    DbSession dbSession = dbClient.openSession(false);
    Connection dbConnection = dbSession.getConnection();
    try {
      SourceFileResultSetIterator rowIt = SourceFileResultSetIterator.create(dbClient, dbConnection, lastUpdatedAt);
      long maxUpdatedAt = doIndex(bulk, rowIt);
      rowIt.close();
      return maxUpdatedAt;

    } finally {
      dbSession.close();
    }
  }

  public long index(Iterator<SourceFileResultSetIterator.Row> dbRows) {
    BulkIndexer bulk = new BulkIndexer(esClient, SourceLineIndexDefinition.INDEX);
    return doIndex(bulk, dbRows);
  }

  private long doIndex(BulkIndexer bulk, Iterator<SourceFileResultSetIterator.Row> dbRows) {
    long maxUpdatedAt = 0L;
    bulk.start();
    while (dbRows.hasNext()) {
      SourceFileResultSetIterator.Row row = dbRows.next();
      addDeleteRequestsForLinesGreaterThan(bulk, row);
      for (UpdateRequest updateRequest : row.getLineUpdateRequests()) {
        bulk.add(updateRequest);
      }
      maxUpdatedAt = Math.max(maxUpdatedAt, row.getUpdatedAt());
    }
    bulk.stop();
    return maxUpdatedAt;
  }

  /**
   * Use-case:
   * - file had 10 lines in previous analysis
   * - same file has now 5 lines
   * Lines 6 to 10 must be removed from index.
   */
  private void addDeleteRequestsForLinesGreaterThan(BulkIndexer bulk, SourceFileResultSetIterator.Row fileRow) {
    int numberOfLines = fileRow.getLineUpdateRequests().size();
    SearchRequestBuilder searchRequest = esClient.prepareSearch(SourceLineIndexDefinition.INDEX)
      .setTypes(SourceLineIndexDefinition.TYPE)
      .setRouting(fileRow.getProjectUuid())
      .setQuery(QueryBuilders.filteredQuery(
        QueryBuilders.matchAllQuery(),
        FilterBuilders.boolFilter()
          .must(FilterBuilders.termFilter(FIELD_FILE_UUID, fileRow.getFileUuid()).cache(false))
          .must(FilterBuilders.rangeFilter(SourceLineIndexDefinition.FIELD_LINE).gt(numberOfLines).cache(false))
          .cache(false)
      ));
    bulk.addDeletion(searchRequest);
  }

  public void deleteByFile(String fileUuid) {
    // TODO would be great to have the projectUuid for routing
    SearchRequestBuilder searchRequest = esClient.prepareSearch(SourceLineIndexDefinition.INDEX)
      .setTypes(SourceLineIndexDefinition.TYPE)
      .setQuery(QueryBuilders.filteredQuery(
        QueryBuilders.matchAllQuery(),
        FilterBuilders.termFilter(FIELD_FILE_UUID, fileUuid).cache(false)));
    BulkIndexer.delete(esClient, SourceLineIndexDefinition.INDEX, searchRequest);
  }

  public void deleteByProject(String projectUuid) {
    SearchRequestBuilder searchRequest = esClient.prepareSearch(SourceLineIndexDefinition.INDEX)
      .setRouting(projectUuid)
      .setTypes(SourceLineIndexDefinition.TYPE)
      .setQuery(QueryBuilders.filteredQuery(
        QueryBuilders.matchAllQuery(),
        FilterBuilders.termFilter(FIELD_PROJECT_UUID, projectUuid).cache(false)));
    BulkIndexer.delete(esClient, SourceLineIndexDefinition.INDEX, searchRequest);
  }
}
