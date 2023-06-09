/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.test.state.operator.restore.unkeyed;

import org.apache.flink.FlinkVersion;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.state.operator.restore.ExecutionMode;

import static org.apache.flink.test.state.operator.restore.unkeyed.NonKeyedJob.createFirstStatefulMap;
import static org.apache.flink.test.state.operator.restore.unkeyed.NonKeyedJob.createSecondStatefulMap;
import static org.apache.flink.test.state.operator.restore.unkeyed.NonKeyedJob.createSource;
import static org.apache.flink.test.state.operator.restore.unkeyed.NonKeyedJob.createThirdStatefulMap;

/**
 * Verifies that the state of all operators is restored if a topology change removes an operator
 * from a chain.
 *
 * <p>This test specifically checks that stateless operators can be removed even if all states from
 * the previous job must be restored.
 *
 * <p>The test shares the snapshots generated by {@link ChainBreakTest}.
 */
public class ChainLengthStatelessDecreaseTest extends AbstractNonKeyedOperatorRestoreTestBase {

    public ChainLengthStatelessDecreaseTest(FlinkVersion flinkVersion) {
        super(flinkVersion);
    }

    @Override
    public void createRestoredJob(StreamExecutionEnvironment env) {
        /*
         * Original job: Source -> StatefulMap1 -> CHAIN(StatefulMap2 -> Map -> StatefulMap3)
         * Modified job: Source -> StatefulMap1 -> CHAIN(StatefulMap2 -> StatefulMap3)
         */
        DataStream<Integer> source = createSource(env, ExecutionMode.RESTORE);

        SingleOutputStreamOperator<Integer> first =
                createFirstStatefulMap(ExecutionMode.RESTORE, source);
        first.startNewChain();

        SingleOutputStreamOperator<Integer> second =
                createSecondStatefulMap(ExecutionMode.RESTORE, first);
        second.startNewChain();

        SingleOutputStreamOperator<Integer> third =
                createThirdStatefulMap(ExecutionMode.RESTORE, second);
    }
}
