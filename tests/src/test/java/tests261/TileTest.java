package tests261;

import mindustry.Vars;
import mindustry.core.World;
import mindustry.entities.effect.Fire;
import mindustry.entities.type.TimedEntity;
import mindustry.net.Net;
import mindustry.world.Tile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.mockito.Mockito.*;

class TileTest
{
    @Spy
    private World world = new World();            // Spy for the World object

    Tile tile;                      // Concrete class to mock
    Net net;                        // Concrete class to mock
    TimedEntity timedEntity;        // Abstract class to mock
    Fire fire;

    @BeforeEach
    void setUp()  {
        // Initialize mock objects
        tile = mock(Tile.class);
        net = mock(Net.class);
        timedEntity = mock(TimedEntity.class);
        fire = mock(Fire.class);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    void worldFunctions() {
        Assertions.assertEquals(0, tile.worldx());
        Assertions.assertEquals(0, tile.worldy());

        verify(tile).worldx();
        verify(tile).worldy();
    }

    @Test
    void pos() {
        Assertions.assertEquals(0, tile.pos());
        verify(tile).pos();
    }

    @Test
    public void testCreatingAFireObjectFromATile() {
        // Make sure net.client() will return false because Fire is server side
        when(net.client()).thenReturn(false);
        Vars.net = this.net;
        Vars.world = this.world;

        // Call the method to create the fire on the tile
        Fire.create(tile);

        // Test the interaction between the Tile class and the Fire class after Fire has
        // successfully created a Fire object using the Tile
        verify(tile).worldx();
        verify(tile).worldy();

        // Verify that tile.pos() was called twice
        verify(tile, times(2)).pos();
    }

    @Test
    public void testExtinguishFromATile() {
        // Call dummy method to get back the map to clear
        // Reason is because we want to release the key-values within the map for our tests to work
        Fire.getIntMapFireTest().clear();

        // Call extinguish on Fire class
        Fire.extinguish(tile, 1f);

        // Verify that tile.pos() was called once, which verifies the interaction within this method
        verify(tile, times(1)).pos();
    }
}