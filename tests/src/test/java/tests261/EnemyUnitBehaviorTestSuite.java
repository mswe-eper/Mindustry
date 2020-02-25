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
import mindustry.entities.EntityGroup;
import mindustry.entities.Units;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.base.GroundUnit;
import mindustry.entities.units.UnitCommand;
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

    GroundUnit dagger;              // The dagger unit that will be tested on
    GroundUnit unitOpposingTeam;    // A different unit on opposing teams of the enemy unit

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

    @BeforeEach
    void defineUnits() {
        dagger = (GroundUnit) UnitTypes.dagger.create(Team.derelict) ;
        unitOpposingTeam = (GroundUnit) UnitTypes.titan.create(Team.sharded);

        // Set initial health
        dagger.health(130f);
        unitOpposingTeam.health(460f);

        // Set initial positions
        dagger.set(0, 0);
        unitOpposingTeam.set(100, 100);

        dagger.add();
        unitOpposingTeam.add();
    }

    /**============================================================================================
     *
     *      Health Trait Damage Tests (Week 3 HW) [REMODELED]
     *
     ============================================================================================*/
    @Test
    public void testInitialHealth() {
        // First way to get dagger but without a ground unit handle
        UnitType dagger1 = content.units().get(3);
        Assertions.assertEquals(dagger1.health, 130f);

        // Test the initial health of the dagger unit after setting to it
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());
    }

    @Test
    public void LessThanHealthDamage() {
        // Set dagger unit's health to 130f, the default
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that the unit took damage less than its health
        dagger.damage(80f);
        Assertions.assertEquals(50f, dagger.health());
    }

    @Test
    public void negativeDamage() {
        // Set dagger unit's health to 130f, the default
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that the unit took negative damage
        dagger.damage(-20f);
        Assertions.assertEquals(150f, dagger.health());
    }

    @Test
    public void ZeroDamage() {
        // Set dagger unit's health to 130f, the default
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that unit took zero damage
        dagger.damage(0f);
        Assertions.assertEquals(130f, dagger.health());
    }

    @Test
    public void GreaterThanHealthDamage() {
        // Set dagger unit's health to 130f, the default
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that unit took damage greater than its health, and should fall into negative values
        dagger.damage(150f);
        Assertions.assertEquals(-20f, dagger.health());
    }

    @Test
    public void EqualToHealthDamage() {
        // Set dagger unit's health to 130f, the default
        dagger.health(130f);
        Assertions.assertEquals(130f, dagger.health());

        // Assert that unit took damage equal to its health, and should have its health be 0
        dagger.damage(130f);
        Assertions.assertEquals(0f, dagger.health());
    }

    @Test
    public void MaxHealthDamage() {
        // Set dagger unit's health to 130f, the default
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
    /* ========================================================================
     * Test Individual States (5)
     * =======================================================================*/
    @Test
    public void testAttackState() {
        // Set dagger unit to the attack state
        dagger.setState(dagger.attack);

        // Assert that state is currently attack and not any other state
        Assertions.assertSame(dagger.attack, dagger.getStartState());
        Assertions.assertNotSame(dagger.retreat, dagger.getStartState());
        Assertions.assertNotSame(dagger.rally, dagger.getStartState());

        // dagger.onCommand(UnitCommand.attack);
    }

    @Test
    public void testRetreatState() {
        // Set dagger unit to the retreat state
        dagger.setState(dagger.retreat);

        // Assert that retreat is currently attack and not any other state
        Assertions.assertNotSame(dagger.attack, dagger.getStartState());
        Assertions.assertSame(dagger.retreat, dagger.getStartState());
        Assertions.assertNotSame(dagger.rally, dagger.getStartState());
    }

    @Test
    public void testRallyState() {
        // Set dagger unit to the rally state
        dagger.setState(dagger.rally);

        // Assert that state is currently rally and not any other state
        Assertions.assertNotSame(dagger.attack, dagger.getStartState());
        Assertions.assertNotSame(dagger.retreat, dagger.getStartState());
        Assertions.assertSame(dagger.rally, dagger.getStartState());
    }

    @Test
    public void testDeadState() {
        // Assert that the dagger unit is alive
        Assertions.assertFalse(dagger.isDead());

        // Assert that unit is dead after setting state to dead
        dagger.setDead(true);
        Assertions.assertTrue(dagger.isDead());
    }

    @Test
    public void testShootingState() {
        // To be in the shooting state, units must be on opposing teams
        Assertions.assertNotEquals(dagger.getTeam(), unitOpposingTeam.getTeam());

        // To be in the shooting state, unitOpposingTeam must be in range of enemy unit's weapon
        unitOpposingTeam.set(1, 1);

        // invalidateTarget() determines shooting state
        boolean targetIsNotValid = Units.invalidateTarget(unitOpposingTeam, dagger);
        boolean isShooting = !targetIsNotValid;
        Assertions.assertTrue(isShooting);
    }

    /* ========================================================================
     * Test State Transitions (12)
     * =======================================================================*/
    @Test
    public void testAttackToRetreat() {
        // Initially set state to attack to test transition
        testEnemyUnit.getState().set(testEnemyUnit.attack);

        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.attack));

        // Testing retreat, so value should be 1
        int retreatIndexInEnum = 1;
        UnitCommand command= UnitCommand.all[retreatIndexInEnum];

        // Test transition via onCommand()
        testEnemyUnit.onCommand(command);
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }

    @Test
    public void testRetreatToRally() {
        // Initially set state to retreat to test transition
        testEnemyUnit.getState().set(testEnemyUnit.retreat);

        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.retreat));

        // Testing rally, so value should be 2
        int rallyIndexInEnum = 2;
        UnitCommand command= UnitCommand.all[rallyIndexInEnum];

        // Test transition via onCommand()
        testEnemyUnit.onCommand(command);
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }

    @Test
    public void testRallyToAttack() {
        // Initially set state to rally to test transition
        testEnemyUnit.getState().set(testEnemyUnit.rally);

        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.rally));

        // Testing attack, so value should be 0
        int attackIndexInEnum = 0;
        UnitCommand command= UnitCommand.all[attackIndexInEnum];

        // Test transition via onCommand()
        testEnemyUnit.onCommand(command);
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }

    @Test
    public void testAttackToRally() {
        // Initially set state to attack to test transition
        testEnemyUnit.getState().set(testEnemyUnit.attack);

        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.attack));

        // Testing rally, so value should be 2
        int rallyIndexInEnum = 2;
        UnitCommand command= UnitCommand.all[rallyIndexInEnum];

        // Test transition via onCommand()
        testEnemyUnit.onCommand(command);
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }

    @Test
    public void testRallyToRetreat() {
        // Initially set state to rally to test transition
        testEnemyUnit.getState().set(testEnemyUnit.rally);

        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.rally));

        // Testing retreat, so value should be 1
        int retreatIndexInEnum = 1;
        UnitCommand command= UnitCommand.all[retreatIndexInEnum];

        // Test transition via onCommand()
        testEnemyUnit.onCommand(command);
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }

    @Test
    public void testRetreatToAttack() {
        // Initially set state to retreat to test transition
        testEnemyUnit.getState().set(testEnemyUnit.retreat);

        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.retreat));

        // Testing attack, so value should be 0
        int attackIndexInEnum = 0;
        UnitCommand command= UnitCommand.all[attackIndexInEnum];

        // Test transition via onCommand()
        testEnemyUnit.onCommand(command);
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }

    /* Attack to shooting and back */
    @Test
    public void testAttackToShooting() {
        // Initially set state to attack
        testEnemyUnit.getState().set(testEnemyUnit.attack);
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.attack));

        // To be valid, enemy and player units must be on opposing teams
        Assertions.assertNotEquals(this.testEnemyUnit.team, this.testPlayerUnit.team);

        // If player unit is out of range, then enemy unit cannot shoot
        testPlayerUnit.set(0, 0);
        boolean isShooting1 = !Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 100, 100, 10);
        Assertions.assertFalse(isShooting1);

        // Player unit is in range, enemy unit should be shooting
        testPlayerUnit.set(0, 0);
        boolean isShooting2 = !Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 0, 0, 10);
        Assertions.assertTrue(isShooting2);
    }

    @Test
    public void testShootingToAttack() {
        // To be valid, enemy and player units must be on opposing teams
        Assertions.assertNotEquals(this.testEnemyUnit.team, this.testPlayerUnit.team);

        // Player unit is in range, enemy unit should be shooting
        testPlayerUnit.set(0, 0);
        boolean isShooting1 = !Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 0, 0, 10);
        Assertions.assertTrue(isShooting1);

        // If player unit is out of range, then enemy unit cannot shoot
        testPlayerUnit.set(0, 0);
        boolean isShooting2 = !Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 100, 100, 10);
        Assertions.assertFalse(isShooting2);

        // Set state to attack
        testEnemyUnit.getState().set(testEnemyUnit.attack);
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.attack));
    }

    /* Each state to dead */
    @Test
    public void testAttackToDead() {
        // Initially set state to attack
        testEnemyUnit.getState().set(testEnemyUnit.attack);
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.attack));

        testEnemyUnit.health(50f);
        Assertions.assertEquals(50f, testEnemyUnit.health());
        Assertions.assertFalse(testEnemyUnit.isDead());

        testEnemyUnit.damage(100f);
        Assertions.assertEquals(-50f, testEnemyUnit.health());
        Assertions.assertTrue(testEnemyUnit.isDead());
    }

    @Test
    public void testRetreatToDead() {
        // Initially set state to retreat
        testEnemyUnit.getState().set(testEnemyUnit.retreat);
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.retreat));

        testEnemyUnit.health(50f);
        Assertions.assertEquals(50f, testEnemyUnit.health());
        Assertions.assertFalse(testEnemyUnit.isDead());

        testEnemyUnit.damage(100f);
        Assertions.assertEquals(-50f, testEnemyUnit.health());
        Assertions.assertTrue(testEnemyUnit.isDead());
    }

    @Test
    public void testRallyToDead() {
        // Initially set state to rally
        testEnemyUnit.getState().set(testEnemyUnit.rally);
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.rally));

        testEnemyUnit.health(50f);
        Assertions.assertEquals(50f, testEnemyUnit.health());
        Assertions.assertFalse(testEnemyUnit.isDead());

        testEnemyUnit.damage(100f);
        Assertions.assertEquals(-50f, testEnemyUnit.health());
        Assertions.assertTrue(testEnemyUnit.isDead());
    }

    @Test
    public void testShootingToDead() {
        // To be valid, enemy and player units must be on opposing teams
        Assertions.assertNotEquals(this.testEnemyUnit.team, this.testPlayerUnit.team);

        // Player unit is in range, enemy unit should be shooting
        testPlayerUnit.set(0, 0);
        boolean isShooting = !Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 0, 0, 10);
        Assertions.assertTrue(isShooting);

        testEnemyUnit.health(50f);
        Assertions.assertEquals(50f, testEnemyUnit.health());
        Assertions.assertFalse(testEnemyUnit.isDead());

        testEnemyUnit.damage(100f);
        Assertions.assertEquals(-50f, testEnemyUnit.health());
        Assertions.assertTrue(testEnemyUnit.isDead());
    }

    /* ========================================================================
     * Self-looping Transitions (4)
     * =======================================================================*/
    @Test
    public void testAttackToAttack() {
        // Set the initial state to attack
        testEnemyUnit.getState().set(testEnemyUnit.getStartState());
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.rally));

        // Testing attack, so value should be 0
        int attackIndexInEnum = 0;
        UnitCommand command= UnitCommand.all[attackIndexInEnum];

        // Test transition via onCommand()
        testEnemyUnit.onCommand(command);
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.rally));

        // Check second self-loop in terms to invalidateTarget()
        Assertions.assertNotEquals(this.testEnemyUnit.team, this.testPlayerUnit.team);
        // If player unit is out of range, then enemy unit cannot shoot
        testPlayerUnit.set(0, 0);
        boolean isShooting = !Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 100, 100, 10);
        Assertions.assertFalse(isShooting);
    }

    @Test
    public void testShootingToShooting() {
        // To be valid, enemy and player units must be on opposing teams
        Assertions.assertNotEquals(this.testEnemyUnit.team, this.testPlayerUnit.team);

        // Player unit is in range, enemy unit should be shooting
        testPlayerUnit.set(0, 0);
        boolean isShooting1 = !Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 0, 0, 10);
        Assertions.assertTrue(isShooting1);

        // To be valid, enemy and player units must be on opposing teams
        Assertions.assertNotEquals(this.testEnemyUnit.team, this.testPlayerUnit.team);

        // Player unit is in range, enemy unit should be shooting (enemy unit position slightly changed)
        testPlayerUnit.set(0, 0);
        boolean isShooting2 = !Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 1, 1, 10);
        Assertions.assertTrue(isShooting2);
    }

    @Test
    public void testRetreatToRetreat() {
        // Set the initial state to retreat
        testEnemyUnit.getState().set(testEnemyUnit.retreat);
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.rally));

        // Testing retreat, so value should be 1
        int retreatIndexInEnum = 1;
        UnitCommand command= UnitCommand.all[retreatIndexInEnum];

        // Test transition via onCommand()
        testEnemyUnit.onCommand(command);
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }

    @Test
    public void testRallyToRally() {
        // Set the initial state to rally
        testEnemyUnit.getState().set(testEnemyUnit.rally);
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.rally));

        // Testing rally, so value should be 2
        int rallyIndexInEnum = 2;
        UnitCommand command= UnitCommand.all[rallyIndexInEnum];

        // Test transition via onCommand()
        testEnemyUnit.onCommand(command);
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }


    /**============================================================================================
     *
     ============================================================================================*/
}
