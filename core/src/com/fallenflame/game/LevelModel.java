package com.fallenflame.game;

import com.badlogic.gdx.math.*;

import java.util.*;

public class LevelModel {

    /** 2D tile representation of board where TRUE indicates tile is available for movement*/
    private ArrayList<ArrayList<Boolean>> tileGrid;
    /** Size of tiles (tiles are square so is x and y) */
    private int tileSize;

    public LevelModel(){ }

    public void initialize(Rectangle bounds, PlayerModel player, List<WallModel> walls, List<EnemyModel> enemies) {
        // TODO
        // also need to think about where the grid granularity constant is?
        // can make it a factor of the player size?
    }

    /**
     * Sets tiles previously covered by player as available
     * @param player
     */
    public void removePlayer(PlayerModel player) {
        // TODO
    }

    /**
     * Sets tiles currently covered by player as unavailable
     * @param player
     */
    public void placePlayer(PlayerModel player) {
        // TODO
    }

    /**
     * Sets tiles previously covered by enemy as available
     * @param enemy
     */
    public void removeEnemy(EnemyModel enemy) {
        // TODO
    }

    /**
     * Sets tiles currently covered by enemy as unavailable
     * @param enemy
     */
    public void placeEnemy(EnemyModel enemy) {
        // TODO
    }

    /**
     * Returns the tile cell index for a screen position.
     *
     * While all positions are 2-dimensional, the dimensions to
     * the board are symmetric. This allows us to use the same
     * method to convert an x coordinate or a y coordinate to
     * a cell index.
     *
     * @param f Screen position coordinate
     *
     * @return the tile cell index for a screen position.
     */
    public int screenToTile(float f) {
        return (int)(f / tileSize);
    }

    /**
     * Returns the screen position coordinate for a tile cell index.
     *
     * While all positions are 2-dimensional, the dimensions to
     * the board are symmetric. This allows us to use the same
     * method to convert an x coordinate or a y coordinate to
     * a cell index.
     *
     * @param n Tile cell index
     *
     * @return the screen position coordinate for a tile cell index.
     */
    public float tileToScreen(int n) {
        return (float) (n + 0.5f) * tileSize;
    }

    /**
     * Returns whether the input tile is available for movement.
     *
     * @param x Tile x-coor
     * @param y Tile y-coor
     * @return isSafe boolean
     */
    public boolean getSafe(int x, int y) {
        return tileGrid.get(x).get(y);
    }
}
