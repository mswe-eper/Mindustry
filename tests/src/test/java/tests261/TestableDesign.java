package tests261;

import arc.ApplicationCore;
import arc.audio.Music;
import arc.backend.headless.HeadlessApplication;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
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
import mindustry.entities.EntityCollisions;
import mindustry.entities.type.base.GroundUnit;
import mindustry.entities.units.StateMachine;
import mindustry.entities.units.UnitState;
import mindustry.game.MusicControl;
import mindustry.game.Team;
import mindustry.maps.Map;
import mindustry.net.Net;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestableDesign {
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
    /**
     * Extends GroundUnit to override getStartState() to instead return the current state
     * in order to do assertions and test the state machine.
     * Originally, this method would just return the attack state, which is the default state a
     * unit is set to
     * */
    static class TestGroundUnit extends GroundUnit {
        public StateMachine getState() {
            return state;
        }

        @Override
        public UnitState getStartState() {
            return state.current();
        }
    }

    GroundUnit dagger;              // The dagger unit that will be tested on
    GroundUnit unitOpposingTeam;    // A different unit on opposing teams of the enemy unit
    EnemyUnitBehaviorTestSuite.TestGroundUnit testDagger;      // The dagger unit of type TestGroundUnit to access new methods for testing

    // Redefine the UnitType for the dagger unit to use TestGroundUnit instead of GroundUnit
    @BeforeAll
    static void redefineDaggerUnitType() {
        UnitTypes.dagger = new UnitType("dagger2", EnemyUnitBehaviorTestSuite.TestGroundUnit::new){{
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

    @BeforeEach
    void defineUnits() {
        dagger = (GroundUnit) UnitTypes.dagger.create(Team.derelict) ;
        unitOpposingTeam = (GroundUnit) UnitTypes.titan.create(Team.sharded);
        testDagger = (EnemyUnitBehaviorTestSuite.TestGroundUnit) UnitTypes.dagger.create(Team.derelict);

        // Set initial health
        dagger.health(130f);
        unitOpposingTeam.health(460f);
        testDagger.health(130f);

        // Set initial positions
        dagger.set(0, 0);
        unitOpposingTeam.set(500, 500);
        testDagger.set(0, 0);

        dagger.add();
        unitOpposingTeam.add();
        testDagger.add();
    }


    /**============================================================================================
     *
     *      Collisions Test for Testable Design (Week 6 HW)
     *
     ============================================================================================*/
    @Test
    public void testDidCollide() {
        // Set initial positions for dagger unit and unit on the opposing team
        dagger.set(0, 0);
        unitOpposingTeam.set(0, 0);

        // Initialize EntityCollisions and assert that it isn't null
        EntityCollisions entityCollisions = new EntityCollisions();
        Assertions.assertNotNull(entityCollisions);

        Vec2 l1 = new Vec2();       // Empty vector to pass
        Rect r1 = new Rect();       // hitbox of first unit
        Rect r2 = new Rect();       // hitbox of second unit
        dagger.hitbox(r1);
        unitOpposingTeam.hitbox(r2);

        float vax = 1;
        float vay = 1;
        float vbx = 2;
        float vby = 2;

        boolean collideTest = entityCollisions.collideTest(dagger.x, dagger.y, r1.width, r1.height, vax, vay,
                unitOpposingTeam.x, unitOpposingTeam.y, r2.width, r2.height, vbx, vby, l1);
        Assertions.assertTrue(collideTest);
    }

    @Test
    public void testDidNotCollide() {
        // Set initial positions for dagger unit and unit on the opposing team
        dagger.set(100, 100);
        unitOpposingTeam.set(0, 0);

        // Initialize EntityCollisions and assert that it isn't null
        EntityCollisions entityCollisions = new EntityCollisions();
        Assertions.assertNotNull(entityCollisions);

        Vec2 l1 = new Vec2();       // Empty vector to pass
        Rect r1 = new Rect();       // hitbox of first unit
        Rect r2 = new Rect();       // hitbox of second unit
        dagger.hitbox(r1);
        unitOpposingTeam.hitbox(r2);

        float vax = 1;
        float vay = 1;
        float vbx = 2;
        float vby = 2;

        boolean collideTest = entityCollisions.collideTest(dagger.x, dagger.y, r1.width, r1.height, vax, vay,
                unitOpposingTeam.x, unitOpposingTeam.y, r2.width, r2.height, vbx, vby, l1);
        Assertions.assertFalse(collideTest);
    }
}
