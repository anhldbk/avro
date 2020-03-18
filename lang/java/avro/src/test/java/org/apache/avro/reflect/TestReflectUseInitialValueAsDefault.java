/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avro.reflect;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TestReflectUseInitialValueAsDefault {
  private static Boolean DEFAULT_BOOLEAN = true;
  private static Byte DEFAULT_BYTE = 55;
  private static Short DEFAULT_SHORT = 555;
  private static Integer DEFAULT_INT = 5555;
  private static Long DEFAULT_LONG = 55555555L;
  private static Float DEFAULT_FLOAT = 3.14F;
  private static Double DEFAULT_DOUBLE = 3.14;

  private static class Primitives {
    boolean aBoolean = DEFAULT_BOOLEAN;
    byte aByte = DEFAULT_BYTE;
    short aShort = DEFAULT_SHORT;
    int anInt = DEFAULT_INT;
    long aLong = DEFAULT_LONG;
    float aFloat = DEFAULT_FLOAT;
    double aDouble = DEFAULT_DOUBLE;
  }

  private static class Wrappers {
    Boolean aBoolean = DEFAULT_BOOLEAN;
    Byte aByte = DEFAULT_BYTE;
    Short aShort = DEFAULT_SHORT;
    Integer anInt = DEFAULT_INT;
    Long aLong = DEFAULT_LONG;
    Float aFloat = DEFAULT_FLOAT;
    Double aDouble = DEFAULT_DOUBLE;
    Primitives anObject;
  }

  private static class UseInitialValueAsDefaultWithNullable {
    @Nullable Double aDouble;

    @AvroSchema("[\"double\", \"long\"]")
    Object doubleOrLong;

    @Nullable
    @AvroSchema("[\"double\", \"long\"]")
    Object doubleOrLongOrNull1;

    @AvroSchema("[\"double\", \"long\", \"null\"]")
    Object doubleOrLongOrNull2;

    @Nullable
    @AvroSchema("[\"double\", \"long\", \"null\"]")
    Object doubleOrLongOrNull3;
  }

  protected void assetField(
      Schema schema, String fieldName, Class fieldClass, Object fieldDefaultValue) {
    Schema.Field field = schema.getField(fieldName);
    Assert.assertEquals(requiredSchema(fieldClass), field.schema());
    Assert.assertEquals(fieldDefaultValue, field.defaultVal());
  }

  @Test
  public void testPrimitives() {
    // UseInitialValueAsDefault only makes fields nullable, so testing must use a base record
    Schema primitives = ReflectData.UseInitialValueAsDefault.get().getSchema(Primitives.class);

    assetField(primitives, "aBoolean", boolean.class, DEFAULT_BOOLEAN);
    // non-int values are encoded as int values
    assetField(primitives, "aByte", byte.class, DEFAULT_BYTE.intValue());
    assetField(primitives, "aShort", short.class, DEFAULT_SHORT.intValue());
    assetField(primitives, "anInt", int.class, DEFAULT_INT);
    assetField(primitives, "aLong", long.class, DEFAULT_LONG);
    assetField(primitives, "aFloat", float.class, DEFAULT_FLOAT);
    assetField(primitives, "aDouble", double.class, DEFAULT_DOUBLE);
  }

  @Test
  public void testWrappers() {
    // UseInitialValueAsDefault only makes fields nullable, so testing must use a base record
    Schema wrappers = ReflectData.UseInitialValueAsDefault.get().getSchema(Wrappers.class);
    Assert.assertEquals(nullableSchema(boolean.class), wrappers.getField("aBoolean").schema());
    Assert.assertEquals(nullableSchema(byte.class), wrappers.getField("aByte").schema());
    Assert.assertEquals(nullableSchema(short.class), wrappers.getField("aShort").schema());
    Assert.assertEquals(nullableSchema(int.class), wrappers.getField("anInt").schema());
    Assert.assertEquals(nullableSchema(long.class), wrappers.getField("aLong").schema());
    Assert.assertEquals(nullableSchema(float.class), wrappers.getField("aFloat").schema());
    Assert.assertEquals(nullableSchema(double.class), wrappers.getField("aDouble").schema());
    Assert.assertEquals(nullableSchema(Primitives.class), wrappers.getField("anObject").schema());
  }

  @Test
  public void testUseInitialValueAsDefaultWithNullableAnnotation() {
    Schema withNullable =
        ReflectData.UseInitialValueAsDefault.get()
            .allowNull(true)
            .getSchema(UseInitialValueAsDefaultWithNullable.class);

    Assert.assertEquals(
        "Should produce a nullable double",
        nullableSchema(double.class),
        withNullable.getField("aDouble").schema());

    Schema nullableDoubleOrLong =
        Schema.createUnion(
            Arrays.asList(
                Schema.create(Schema.Type.NULL),
                Schema.create(Schema.Type.DOUBLE),
                Schema.create(Schema.Type.LONG)));

    Assert.assertEquals(
        "Should add null to a non-null union",
        nullableDoubleOrLong,
        withNullable.getField("doubleOrLong").schema());

    Assert.assertEquals(
        "Should add null to a non-null union",
        nullableDoubleOrLong,
        withNullable.getField("doubleOrLongOrNull1").schema());

    Schema doubleOrLongOrNull =
        Schema.createUnion(
            Arrays.asList(
                Schema.create(Schema.Type.DOUBLE),
                Schema.create(Schema.Type.LONG),
                Schema.create(Schema.Type.NULL)));

    Assert.assertEquals(
        "Should add null to a non-null union",
        doubleOrLongOrNull,
        withNullable.getField("doubleOrLongOrNull2").schema());

    Assert.assertEquals(
        "Should add null to a non-null union",
        doubleOrLongOrNull,
        withNullable.getField("doubleOrLongOrNull3").schema());
  }

  private Schema requiredSchema(Class<?> type) {
    return ReflectData.get().getSchema(type);
  }

  private Schema nullableSchema(Class<?> type) {
    return Schema.createUnion(
        Arrays.asList(Schema.create(Schema.Type.NULL), ReflectData.get().getSchema(type)));
  }
}
