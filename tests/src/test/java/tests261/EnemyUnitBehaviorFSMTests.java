import arc.math.geom.Vec2;
import mindustry.entities.Units;
import mindustry.entities.traits.HealthTrait;
import mindustry.entities.traits.TargetTrait;
import mindustry.entities.units.StateMachine;
import mindustry.entities.units.UnitCommand;
import mindustry.entities.units.UnitState;
import mindustry.game.Team;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class EnemyUnitBehaviorFSMTests {
    private TestGroundUnit testEnemyUnit;
    private TestGroundUnit testPlayerUnit;


    /* ========================================================================
     * Testing class
     * =======================================================================*/
    private class TestGroundUnit implements HealthTrait, TargetTrait {
        boolean dead = false;
        float health = -1f;
        float maxHealth = 100f;
        Team team;
        float x, y;
        private StateMachine state = new StateMachine();

        public TestGroundUnit(Team team) {
            this.team = team;
            this.x = 0;
            this.y = 0;
        }

        // Added this method to return the state of the current TestGroundUnit instance
        public StateMachine getState() {
            return this.state;
        }

        // The 3 Unit States instantiated as attack, retreat, and rally, which parallels GroundUnit and FlyingUnit classes
        // GroundUnit and FlyingUnit extends BaseUnit, which has a state variable called state of type StateMachine
        public final UnitState
            attack = new UnitState() {
                public void entered() {
                }
                public void update() {
                }
            },
            rally = new UnitState() {
                public void update() {
                }
            },
            retreat = new UnitState() {
                public void entered() {
                }
                public void update() {
                }
            };
        public UnitState getStartState() {
            return attack;
        }

        // GroundUnit and FlyingUnit which extends BaseUnit also overrides onCommand() of BaseUnit
        // Commands of type UnitCommand is an enum but the CommandCenter is responsible for issuing
        // transitions in state by accessing the enum via index (0 = attack, 1 = retreat, and 2 = rally)
        // It does so with the following line
        //                  UnitCommand  command= UnitCommand.all[value];
        public void onCommand(UnitCommand command){
            state.set(command == UnitCommand.retreat ? retreat :
                    command == UnitCommand.attack ? attack :
                            command == UnitCommand.rally ? rally :
                                    null);
        }

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

        // Target Trait Implementation methods
        @Override
        public Team getTeam() {
            return null;
        }

        @Override
        public Vec2 velocity() {
            return null;
        }

        @Override
        public void setX(float x) {
        }

        @Override
        public void setY(float y) {
        }

        @Override
        public float getX() {
            return 0;
        }

        @Override
        public float getY() {
            return 0;
        }
    }


    /* ========================================================================
     * BeforeEach
     * =======================================================================*/
    @BeforeEach
    public void initEach() {
        // Define teams so that they can be compared when implementing the
        // tests that involve the shooting state
        // (Values are arbitrary --> as long as they are different)
        this.testEnemyUnit = new TestGroundUnit(Team.derelict);
        this.testPlayerUnit = new TestGroundUnit(Team.sharded);
    }


    /* ========================================================================
    * Test Individual States (5)
    * =======================================================================*/
    @Test
    public void testAttackState() {
        // The start state is attack, just as it is in GroundUnit
        testEnemyUnit.getState().set(testEnemyUnit.getStartState());
        System.out.println("Is state currently attack? " + testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }

    @Test
    public void testRetreatState() {
        testEnemyUnit.getState().set(testEnemyUnit.retreat);
        System.out.println("Is state currently retreat? " + testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }

    @Test
    public void testRallyState() {
        testEnemyUnit.getState().set(testEnemyUnit.rally);
        System.out.println("Is state currently rally? " + testEnemyUnit.getState().is(testEnemyUnit.rally));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.attack));
        Assertions.assertFalse(testEnemyUnit.getState().is(testEnemyUnit.retreat));
        Assertions.assertTrue(testEnemyUnit.getState().is(testEnemyUnit.rally));
    }

    @Test
    public void testDeadState() {
        Assertions.assertFalse(testEnemyUnit.isDead());

        testEnemyUnit.setDead(true);
        Assertions.assertTrue(testEnemyUnit.isDead());
    }

    @Test
    public void testShootingState() {
        // To be valid, enemy and player units must be on opposing teams
        Assertions.assertNotEquals(this.testEnemyUnit.team, this.testPlayerUnit.team);

        // Enemy unit is the targeter, player unit is the target
        testPlayerUnit.set(0, 0);
        boolean targetIsNotValid = Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 25, 25, 100);
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
    
    /* Self-loops */
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
        boolean isShooting = !Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 0, 0, 10);
        Assertions.assertTrue(isShooting);
        
        // To be valid, enemy and player units must be on opposing teams
        Assertions.assertNotEquals(this.testEnemyUnit.team, this.testPlayerUnit.team);

        // Player unit is in range, enemy unit should be shooting (enemy unit position slightly changed)
        testPlayerUnit.set(0, 0);
        boolean isShooting = !Units.invalidateTarget(testPlayerUnit, testEnemyUnit.team, 1, 1, 10);
        Assertions.assertTrue(isShooting);
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
}
