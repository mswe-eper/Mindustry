package mindustry.world;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TileTest
{
    Tile newTile;
    @BeforeEach
    void setUp()
    {
        newTile = mock(Tile.class);
    }
    @Test
    void worldFunctions()
    {
        Assertions.assertEquals(0, newTile.worldx());
        Assertions.assertEquals(0, newTile.worldy());
        verify(newTile).worldx();
        verify(newTile).worldy();
    }
    @Test
    void pos()
    {
        Assertions.assertEquals(0, newTile.pos());
        verify(newTile).pos();
    }
}