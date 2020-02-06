package tests261;

import mindustry.entities.traits.HealthTrait;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HealthTraitDamageTests {
    class TestObject implements HealthTrait {
        boolean dead = false;
        float health = -1f;
        float maxHealth = 50f;

        @Override
        public void health(float health) {
            this.health = health;
        }

        @Override
        public float health() {
            return this.health;
        }

        @Override
        public float maxHealth() {
            return this.maxHealth;
        }

        @Override
        public boolean isDead() {
            return this.dead;
        }

        @Override
        public void setDead(boolean dead) {
            this.dead = dead;
        }
    }

    @Test
    public void LessThanHealthDamage() {
        TestObject object = new TestObject();
        object.health(50f);
        float health = object.health();
        System.out.println("Object's health is " + health);
        object.damage(20f);
        System.out.println("Object's health is " + object.health());
        Assertions.assertEquals(30f, object.health());
    }

    @Test
    public void negativeDamage() {
        TestObject object = new TestObject();
        object.health(50f);
        float health = object.health();
        System.out.println("Object's health is " + health);
        object.damage(-20f);
        System.out.println("Object's health is " + object.health());
        Assertions.assertEquals(70f, object.health());
    }

    @Test
    public void ZeroDamage() {
        TestObject object = new TestObject();
        object.health(50f);
        float health = object.health();
        System.out.println("Object's health is " + health);
        object.damage(0f);
        System.out.println("Object's health is " + object.health());
        Assertions.assertEquals(50f, object.health());
    }

    @Test
    public void GreaterThanHealthDamage() {
        TestObject object = new TestObject();
        object.health(50f);
        float health = object.health();
        System.out.println("Object's health is " + health);
        object.damage(70f);
        System.out.println("Object's health is " + object.health());
        Assertions.assertEquals(-20f, object.health());
    }

    @Test
    public void EqualToHealthDamage() {
        TestObject object = new TestObject();
        object.health(50f);
        float health = object.health();
        System.out.println("Object's health is " + health);
        object.damage(50f);
        System.out.println("Object's health is " + object.health());
        Assertions.assertEquals(0f, object.health());
    }

    @Test
    public void MaxHealthDamage() {
        TestObject object = new TestObject();
        object.health(50f);
        float health = object.health();
        System.out.println("Object's health is " + health);
        object.damage(9999999f);
        System.out.println("Object's health is " + object.health());
        Assertions.assertEquals(-9999949f, object.health());
    }

}
