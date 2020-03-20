package org.apache.avro.reflect;

import org.apache.avro.Schema;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Objects;

import static org.junit.Assert.fail;

public class TestDefaultReflect {
  public static class Human {
    public String name = "Andy";
    public ArrayList<Human> friends = new ArrayList<>();

    public Human(String name) {
      this.name = name;
    }

    public Human() {}

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Human human = (Human) o;
      return Objects.equals(name, human.name) && Objects.equals(friends, human.friends);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, friends);
    }
  }

  public static class Machine {
    public String name = "BB-8";

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Machine machine = (Machine) o;
      return Objects.equals(name, machine.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }

  @Test
  public void testDefaults() {
    Human andy = new Human("andy");
    Human grass = new Human("grass");
    andy.friends.add(grass);

    Schema schema =
        ReflectData.UseInitialValueAsDefault.get()
            .withDefault(Human.class, andy)
            .getSchema(Human.class);
    System.out.println(schema.toString(true));
  }

  @Union({Human.class, Machine.class})
  public static class Kind extends Object {}

  public static class Meta {
    @Union({Machine.class, Human.class})
    Object kind;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Meta meta = (Meta) o;
      if (this.kind == null) {
        if (meta.kind == null) {
          return true;
        }
      }
      //      if(this.kind instanceof Human){
      //        return ((Human)this.kind).equals(meta.kind);
      //      }
      return this.kind.equals(meta.kind);
    }

    @Override
    public int hashCode() {
      return Objects.hash(kind);
    }
  }

  @Test
  public void testUnionDefaults() {
    Human andy = new Human();
    Meta meta = new Meta();
    //    meta.kind = andy;
    //    meta.kinds.add((Kind)andy);
    ReflectData.UseInitialValueAsDefault reflect =
        ReflectData.UseInitialValueAsDefault.get().withDefault(Meta.class, meta);
    Schema schema = reflect.getSchema(Meta.class);

    System.out.println(schema.toString(true));

    try {
      Meta object = new Meta();
      TestReflect.checkReadWrite(meta, schema);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Should have no exception");
    }
  }
}
