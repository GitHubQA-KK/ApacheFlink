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

package org.apache.flink.formats.avro;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.formats.avro.generated.Address;
import org.apache.flink.formats.avro.generated.UnionLogicalType;
import org.apache.flink.formats.avro.utils.TestDataGenerator;
import org.apache.flink.util.InstantiationUtil;

import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Random;

import static org.apache.flink.formats.avro.utils.AvroTestUtils.getSmallSchema;
import static org.apache.flink.formats.avro.utils.AvroTestUtils.writeRecord;
import static org.assertj.core.api.Assertions.assertThat;

/** Tests for {@link AvroDeserializationSchema}. */
class AvroSerializationSchemaTest {

    private static final Address address = TestDataGenerator.generateRandomAddress(new Random());

    @Test
    void testGenericRecord() throws Exception {
        SerializationSchema<GenericRecord> serializationSchema =
                AvroSerializationSchema.forGeneric(address.getSchema());

        byte[] encodedAddress = writeRecord(address, Address.getClassSchema());
        byte[] dataSerialized = serializationSchema.serialize(address);
        assertThat(dataSerialized).isEqualTo(encodedAddress);
    }

    @Test
    void testSpecificRecord() throws Exception {
        SerializationSchema<Address> serializer =
                AvroSerializationSchema.forSpecific(Address.class);

        byte[] encodedAddress = writeRecord(address, Address.getClassSchema());
        byte[] serializedAddress = serializer.serialize(address);
        assertThat(serializedAddress).isEqualTo(encodedAddress);
    }

    @Test
    void testSpecificRecordWithUnionLogicalType() throws Exception {
        Random rnd = new Random();
        UnionLogicalType data = new UnionLogicalType(Instant.ofEpochMilli(rnd.nextLong()));
        AvroSerializationSchema<UnionLogicalType> serializer =
                AvroSerializationSchema.forSpecific(UnionLogicalType.class);

        byte[] encodedData = writeRecord(data);
        byte[] serializedData = serializer.serialize(data);
        assertThat(serializedData).isEqualTo(encodedData);
    }

    @Test
    void testSerializability() throws Exception {
        AvroSerializationSchema<GenericRecord> ser =
                AvroSerializationSchema.forGeneric(getSmallSchema());
        final byte[] serBytes = InstantiationUtil.serializeObject(ser);

        final AvroSerializationSchema<GenericRecord> serCopy =
                InstantiationUtil.deserializeObject(
                        serBytes, Thread.currentThread().getContextClassLoader());

        assertThat(serCopy.getSchema()).isNull();
        serCopy.open(null);
        assertThat(serCopy.getSchema()).isNotNull();
        assertThat(serCopy.getSchema()).isEqualTo(getSmallSchema());
    }
}
