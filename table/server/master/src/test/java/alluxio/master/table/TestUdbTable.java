/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.master.table;

import alluxio.grpc.table.ColumnStatisticsInfo;
import alluxio.grpc.table.FieldSchema;
import alluxio.grpc.table.Layout;
import alluxio.grpc.table.Schema;
import alluxio.grpc.table.layout.hive.PartitionInfo;
import alluxio.table.common.UdbPartition;
import alluxio.table.common.layout.HiveLayout;
import alluxio.table.common.udb.UdbTable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestUdbTable implements UdbTable {
  private String mDbName;
  private String mName;
  private PartitionInfo mPartitionInfo;
  private Layout mTableLayout;
  private List<UdbPartition> mTestPartitions;
  private Schema mSchema;
  private List<FieldSchema> mPartitionCols;

  public TestUdbTable(String dbName, String name, int numOfPartitions) {
    mDbName = dbName;
    mName = name;
    mPartitionInfo = PartitionInfo.newBuilder()
        .setDbName(mDbName)
        .setTableName(mName)
        .setPartitionName(mName).build();
    mTableLayout = Layout.newBuilder()
        .setLayoutType(HiveLayout.TYPE)
        .setLayoutData(mPartitionInfo.toByteString())
        .build();
    FieldSchema col = FieldSchema.newBuilder().setName("col1")
        .setType("int").setId(1).build();
    mSchema = Schema.newBuilder().addCols(col).build();
    mPartitionCols = Arrays.asList(col);
    mTestPartitions = Stream.iterate(1, n -> n + 1)
        .limit(numOfPartitions).map(i -> new TestPartition(new HiveLayout(genPartitionInfo(
            mDbName, mName, i), Collections.emptyList())))
        .collect(Collectors.toList());
  }

  private static PartitionInfo genPartitionInfo(String dbName, String tableName, int index) {
    return PartitionInfo.newBuilder()
        .setDbName(dbName)
        .setTableName(tableName)
        .setPartitionName("col1=" + index).build();
  }

  @Override
  public String getName() {
    return mName;
  }

  @Override
  public Schema getSchema() {
    return mSchema;
  }

  @Override
  public String getOwner() {
    return "testowner";
  }

  @Override
  public Map<String, String> getParameters() {
    return Collections.emptyMap();
  }

  @Override
  public List<FieldSchema> getPartitionCols() {
    return mPartitionCols;
  }

  @Override
  public Layout getLayout() {
    return mTableLayout;
  }

  @Override
  public List<ColumnStatisticsInfo> getStatistics() {
    return Collections.emptyList();
  }

  @Override
  public List<UdbPartition> getPartitions() throws IOException {
    return mTestPartitions;
  }

  private class TestPartition implements UdbPartition{
    private HiveLayout mLayout;

    private TestPartition(HiveLayout hiveLayout) {
      mLayout = hiveLayout;
    }

    @Override
    public String getSpec() {
      return mLayout.getSpec();
    }

    @Override
    public alluxio.table.common.Layout getLayout() {
      return mLayout;
    }
  }
}