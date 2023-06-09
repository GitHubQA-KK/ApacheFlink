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

package org.apache.flink.table.types.inference.strategies;

import org.apache.flink.annotation.Internal;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.functions.BuiltInFunctionDefinitions;
import org.apache.flink.table.functions.FunctionDefinition;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.inference.ArgumentTypeStrategy;
import org.apache.flink.table.types.inference.CallContext;
import org.apache.flink.table.types.inference.Signature.Argument;
import org.apache.flink.table.types.logical.ArrayType;
import org.apache.flink.table.types.logical.LogicalType;

import java.util.Optional;

import static org.apache.flink.table.types.logical.utils.LogicalTypeCasts.supportsImplicitCast;

/** Specific {@link ArgumentTypeStrategy} for {@link BuiltInFunctionDefinitions#ARRAY_CONTAINS}. */
@Internal
class ArrayElementArgumentTypeStrategy implements ArgumentTypeStrategy {

    @Override
    public Optional<DataType> inferArgumentType(
            CallContext callContext, int argumentPos, boolean throwOnFailure) {
        final ArrayType haystackType =
                (ArrayType) callContext.getArgumentDataTypes().get(0).getLogicalType();
        final LogicalType needleType =
                callContext.getArgumentDataTypes().get(argumentPos).getLogicalType();
        LogicalType haystackElementType = haystackType.getElementType();

        if (!haystackElementType.isNullable() && needleType.isNullable()) {
            haystackElementType = haystackElementType.copy(true);
        }

        if (supportsImplicitCast(needleType, haystackElementType)) {
            return Optional.of(DataTypes.of(haystackElementType));
        }
        return Optional.empty();
    }

    @Override
    public Argument getExpectedArgument(FunctionDefinition functionDefinition, int argumentPos) {
        return Argument.of("<ARRAY ELEMENT>");
    }
}
