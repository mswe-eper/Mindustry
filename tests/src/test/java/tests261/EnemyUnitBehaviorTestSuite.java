package tests261;

import arc.ApplicationCore;
import arc.backend.headless.HeadlessApplication;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.core.FileTree;
import mindustry.core.GameState;
import mindustry.core.Logic;
import mindustry.core.NetServer;
import mindustry.entities.type.base.GroundUnit;
import mindustry.entities.units.UnitState;
import mindustry.game.Team;
import mindustry.maps.Map;
import mindustry.net.Net;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import org.junit.jupiter.api.*;

import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

public class EnemyUnitBehaviorTestSuite {
    /**********************************************************************************************
     *
     *      FROM APPLICATION TESTS
     *
     *********************************************************************************************/
    static Map testMap;
    static boolean initialized;

    @BeforeAll
    static void launchApplication(){
        //only gets called once
        if(initialized) return;
        initialized = true;

        try{
            boolean[] begins = {false};
            Throwable[] exceptionThrown = {null};
            Log.setUseColors(false);

            ApplicationCore core = new ApplicationCore(){
                @Override
                public void setup(){
                    headless = true;
                    net = new Net(null);
                    tree = new FileTree();
                    Vars.init();
                    content.createBaseContent();

                    add(logic = new Logic());
                    add(netServer = new NetServer());

                    content.init();
                }

                @Override
                public void init(){
                    super.init();
                    begins[0] = true;
                    testMap = maps.loadInternalMap("groundZero");
                    Thread.currentThread().interrupt();
                }
            };

            new HeadlessApplication(core, null, throwable -> exceptionThrown[0] = throwable);

            while(!begins[0]){
                if(exceptionThrown[0] != null){
                    fail(exceptionThrown[0]);
                }
                Thread.sleep(10);
            }
        }catch(Throwable r){
            fail(r);
        }
    }

    @BeforeEach
    void resetWorld(){
        Time.setDeltaProvider(() -> 1f);
        logic.reset();
        state.set(GameState.State.menu);
    }

    @Test
    void initialization(){
        assertNotNull(logic);
        assertNotNull(world);
        assertTrue(content.getContentMap().length > 0);
    }
    /*********************************************************************************************/


    /**============================================================================================
     *
     *      Health Trait Damage Tests (Week 3 HW) [REMODELED]
     *
     ============================================================================================*/
    @Test
    public void testInitialHealth() {
        // First way to get dagger but without a ground unit handle
        UnitType dagger = content.units().get(3);
        System.out.println("Health is " + dagger.health);
        Assertions.assertEquals(dagger.health, 130f);

        // Correct way to get ground unit
        GroundUnit daggerGU = (GroundUnit) UnitTypes.dagger.create(Team.derelict);
        daggerGU.health(130f);
        Assertions.assertEquals(130f, daggerGU.health());
    }

    @Test
    public void LessThanHealthDamage() {
        // Initialize a dagger ground unit and set its health to 130f, the default
        GroundUnit dagger = (GroundUnit) UnitTypes.dagger.create(Team.derelict);
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that the unit took damage less than its health
        dagger.damage(80f);
        Assertions.assertEquals(50f, dagger.health());
    }

    @Test
    public void negativeDamage() {
        // Initialize a dagger ground unit and set its health to 130f, the default
        GroundUnit dagger = (GroundUnit) UnitTypes.dagger.create(Team.derelict);
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that the unit took negative damage
        dagger.damage(-20f);
        Assertions.assertEquals(150f, dagger.health());
    }

    @Test
    public void ZeroDamage() {
        // Initialize a dagger ground unit and set its health to 130f, the default
        GroundUnit dagger = (GroundUnit) UnitTypes.dagger.create(Team.derelict);
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that unit took zero damage
        dagger.damage(0f);
        Assertions.assertEquals(130f, dagger.health());
    }

    @Test
    public void GreaterThanHealthDamage() {
        // Initialize a dagger ground unit and set its health to 130f, the default
        GroundUnit dagger = (GroundUnit) UnitTypes.dagger.create(Team.derelict);
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that unit took damage greater than its health, and should fall into negative values
        dagger.damage(150f);
        Assertions.assertEquals(-20f, dagger.health());
    }

    @Test
    public void EqualToHealthDamage() {
        // Initialize a dagger ground unit and set its health to 130f, the default
        GroundUnit dagger = (GroundUnit) UnitTypes.dagger.create(Team.derelict);
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that unit took damage equal to its health, and should have its health be 0
        dagger.damage(130f);
        Assertions.assertEquals(0f, dagger.health());
    }

    @Test
    public void MaxHealthDamage() {
        // Initialize a dagger ground unit and set its health to 130f, the default
        GroundUnit dagger = (GroundUnit) UnitTypes.dagger.create(Team.derelict);
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that unit took the max damage possible, a constant we found in the system that
        // represents the most damage a unit can take
        dagger.damage(9999999f);
        Assertions.assertEquals(-9999869f, dagger.health());
    }

    /**============================================================================================
     *
     *      Enemy Unit Behavior FSM Tests (Week 4 HW) [REMODELED]
     *
     ============================================================================================*/
    /**
     * Extends GroundUnit to override getStartState() to instead return the current state
     * in order to do assertions and test the state machine.
     * Originally, this method would just return the attack state, which is the default state a
     * unit is set to
     * */
    static class TestGroundUnit extends GroundUnit {
        @Override
        public UnitState getStartState() {
            return state.current();
        }
    }

    // Redefine the UnitType for the dagger unit to use TestGroundUnit instead of GroundUnit
    @BeforeAll
    static void redefineDaggerUnitType() {
        UnitTypes.dagger = new UnitType("dagger2", TestGroundUnit::new){{
            maxVelocity = 1.1f;
            speed = 0.2f;
            drag = 0.4f;
            hitsize = 8f;
            mass = 1.75f;
            health = 130;
            weapon = new Weapon("chain-blaster"){{
                length = 1.5f;
                reload = 28f;
                alternate = true;
                ejectEffect = Fx.shellEjectSmall;
                bullet = Bullets.standardCopper;
            }};
        }};
    }

    @Test
    public void testAttackState() {
        // Initialize a dagger ground unit and set it to the attack state
        GroundUnit dagger = (GroundUnit) UnitTypes.dagger.create(Team.derelict);
        dagger.setState(dagger.attack);
        Assertions.assertSame(dagger.attack, dagger.getStartState());
        Assertions.assertNotSame(dagger.retreat, dagger.getStartState());
        Assertions.assertNotSame(dagger.rally, dagger.getStartState());

        // dagger.onCommand(UnitCommand.attack);
    }


    /**============================================================================================
     *
     ============================================================================================*/
}
