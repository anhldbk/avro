[![Build Status](https://travis-ci.org/apache/avro.svg?branch=master)](https://travis-ci.org/apache/avro)

# Apache Avro™

## Overview
Apache Avro™ is a data serialization system.

Learn more about Avro, please visit our website at:

  https://avro.apache.org/

To contribute to Avro, please read:

  https://cwiki.apache.org/confluence/display/AVRO/How+To+Contribute
  
## Why this fork

I want to make a `code-first` RPC platform with Avro for encoding & gRPC first for transport.

This fork is a journey to realize [AVRO-2723](https://issues.apache.org/jira/browse/AVRO-2723?page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel&focusedCommentId=17031652#). Some works are already merged in Apache Avro v1.10.0 via [PR #842](https://github.com/apache/avro/pull/842).
 
Here are  several changes proposed.

### Union Spec

Avro spec v1.9.2 states that

> Unions, as mentioned above, are represented using JSON arrays. For example, ["null", "string"] declares a schema which may be either a null or string.
> 
> (Note that when a default value is specified for a record field whose type is a union, the type of the default value must match the first element of the union. Thus, for unions containing "null", the "null" is usually listed first, since the default value of such unions is typically null.)

I think the default value must match ANY element of the union, not just the first

### Ambiguous Union

This is mainly from [the mailing discussion](https://lists.apache.org/thread.html/905ceacf8af89d7434fe9ab183e8751eed47d8c6d9589953f2330961%40%3Cuser.avro.apache.org%3E)

To not break backward compatibilities, I think enumerations in a union should NOT use the same symbols. See `org.apache.avroTestSchema.testAmbiguousUnion()`

### Jackson Utils

AVRO-2775 [JacksonUtils: exception when calling toJsonNode()](https://issues.apache.org/jira/projects/AVRO/issues/AVRO-2775?filter=allissues)

### Union array & @Array annotation

If you want to make an array of union, you may have to do this

```java
public class Human extends Kind {
 String name = "Andy";
 ArrayList < Human > friends = new ArrayList < > ();
}

public class Machine extends Kind {
 String name = "BB-8";
}

@Union({
 Human.class,
 Machine.class
})
public class Kind extends Object {}

public class Meta {
 @Union({
  Human.class,
  Machine.class
 })
 List < Kind > kinds = new ArrayList < > ();
}
```

Things get complicated if you have to work with enumerations. Because Java enums do NOT allow you to derive. `@Array` is introduced to make writing such array easier


```java
public class Human {
 String name = "Andy";
 ArrayList < Human > friends = new ArrayList < > ();
}

public class Machine {
 String name = "BB-8";
}

enum First {
 A,
 B,
 C
}

enum Second {
 X,
 Y,
 Z
}

public class Meta {
 @Array({
  First.class,
  Second.class
 })
 List < Object > ranks = new ArrayList < > ();

 @Array({
  Human.class,
  Machine.class
 })
 List < Object > kinds = new ArrayList < > ();
}
```

## Usage

### Installation

```bash
$ scripts/local-install.sh
```

### Dependency

```xml
<dependency>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro</artifactId>
    <version>1.10.0.avro-2723</version>
</dependency>

```

### Code 

Simply just use normal Java code, no additional annotations.

```java
public class Human {
  String name = "Andy";
  ArrayList<Human> friends = new ArrayList<>();
}

ReflectData.UseInitialValueAsDefault reflect = ReflectData.UseInitialValueAsDefault.get();
Schema schema = reflect.getSchema(Human.class);

System.out.println(schema.toString(true));
```

The output:

```json
{
  "type" : "record",
  "name" : "Human",
  "namespace" : "org.apache.avro.reflect.TestReflectUseInitialValueAsDefault",
  "fields" : [ {
    "name" : "name",
    "type" : [ "null", "string" ],
    "default" : "Andy"
  }, {
    "name" : "friends",
    "type" : [ "null", {
      "type" : "array",
      "items" : "Human",
      "java-class" : "java.util.ArrayList"
    } ],
    "default" : [ ]
  } ]
}
```
