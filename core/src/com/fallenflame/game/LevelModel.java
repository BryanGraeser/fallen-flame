package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.fallenflame.game.enemies.EnemyModel;
import com.fallenflame.game.physics.obstacle.BoxObstacle;
import com.fallenflame.game.physics.obstacle.WheelObstacle;

import java.util.*;

public class LevelModel {

    public static class Tile {
        /** Is this a goal tiles */
        public boolean goal = false;
        /** Has this tile been visited by pathfinding? */
        public boolean visited = false;
        /** Has wall? */
        public boolean wall = false;
        /** Has enemy? */
        public boolean enemy = false;
        /** Has player? */
        public boolean player = false;

    }

    private enum TileOccupiedBy {
        WALL, ENEMY, PLAYER
    }


    /** 2D tile representation of board where TRUE indicates tile is available for movement only for PATHFINDING */
    private Tile[][] pathTileGrid;
    /** 2D tile representation of board where TRUE indicates tile is available for movement only for FOG */
    private Tile[][] fogTileGrid;
    /** Constant tile size (tiles are square so this is x and y) only for PATHFINDING */
    public static final float PATH_TILE_SIZE = .6f;
    /** Constant tile size (tiles are square so this is x and y) only for FOG */
    public static final float FOG_TILE_SIZE = .4f;
    /** Width of screen */
    private float width;
    /** Height of screen */
    private float height;


    public LevelModel(){ }

    public void initialize(Rectangle bounds, List<WallModel> walls, List<EnemyModel> enemies) {
        width = bounds.getWidth();
        height = bounds.getHeight();

        pathTileGrid = new Tile[(int) Math.ceil(width / PATH_TILE_SIZE)][(int) Math.ceil(height / PATH_TILE_SIZE)];
        for(int x = 0; x < pathTileGrid.length; x++){
            for(int y = 0; y < pathTileGrid[0].length; y++){
                pathTileGrid[x][y] = new Tile();
            }
        }
        fogTileGrid = new Tile[(int) Math.ceil(width / FOG_TILE_SIZE)][(int) Math.ceil(height / FOG_TILE_SIZE)];
        for(int x = 0; x < fogTileGrid.length; x++){
            for(int y = 0; y < fogTileGrid[0].length; y++){
                fogTileGrid[x][y] = new Tile();
            }
        }
        // Set grid to false where obstacle exists
        // TODO: place enemies?
        for(WallModel w : walls) {
            setBoxObstacleInGrid(w, true, TileOccupiedBy.WALL, true);
            setBoxObstacleInGrid(w, true, TileOccupiedBy.WALL, false);
        }
    }

    /**
     * @return the width of the screen
     */
    public float getWidth() {return this.width;}

    /**
     * @return the height of the screen
     */
    public float getHeight() {return this.height;}

    /**
     * Sets tiles previously covered by player as available
     * @param player
     */
    public void removePlayer(PlayerModel player) {
        setWheelObstacleInGrid(player, false, TileOccupiedBy.PLAYER, true);
        setWheelObstacleInGrid(player, false, TileOccupiedBy.PLAYER, false);
    }

    /**
     * Sets tiles currently covered by player as unavailable
     * @param player
     */
    public void placePlayer(PlayerModel player) {
        setWheelObstacleInGrid(player, true, TileOccupiedBy.PLAYER, true);
        setWheelObstacleInGrid(player, true, TileOccupiedBy.PLAYER, false);
    }

    /**
     * Sets tiles previously covered by enemy as available
     * @param enemy
     */
    public void removeEnemy(EnemyModel enemy) {
        setWheelObstacleInGrid(enemy, false, TileOccupiedBy.ENEMY, true);
        setWheelObstacleInGrid(enemy, false, TileOccupiedBy.ENEMY, false);
    }

    /**
     * Sets tiles currently covered by enemy as unavailable
     * @param enemy
     */
    public void placeEnemy(EnemyModel enemy) {
        setWheelObstacleInGrid(enemy, true, TileOccupiedBy.ENEMY, true);
        setWheelObstacleInGrid(enemy, true, TileOccupiedBy.ENEMY, false);
    }

    private void markObstacleTypeInGrid(int x, int y, boolean b, TileOccupiedBy o, boolean path) {
        Tile[][] tileGrid = path ? pathTileGrid : fogTileGrid;
        switch (o) {
            case WALL:
                tileGrid[x][y].wall = b;
                break;
            case ENEMY:
                tileGrid[x][y].enemy = b;
                break;
            case PLAYER:
                tileGrid[x][y].player = b;
                break;
        }
    }

    /**
     * Set tiles currently covered by WheelObstacle obs to boolean b
     * @param obs Wheel obstacle
     * @param b Boolean value
     * @param o Type of obstacle
     * @param path True if path (false implies fog)
     */
    public void setWheelObstacleInGrid(WheelObstacle obs, boolean b, TileOccupiedBy o, boolean path) {
        for(int x = screenToTile(obs.getX() - obs.getRadius(), path);
            x <= screenToTile(obs.getX() + obs.getRadius(), path); x++) {
            for(int y = screenToTile(obs.getY() - obs.getRadius(), path);
                y <= screenToTile(obs.getY() + obs.getRadius(), path); y++) {
                if (!inBounds(x, y, path)) continue;
                markObstacleTypeInGrid(x, y, b, o, path);
            }
        }
    }

    /**
     * Set tiles currently covered by BoxObstacle obs to boolean b
     * @param obs Wheel obstacle
     * @param b Boolean value
     * @param o Type of obstacle
     * @param path True if path (false implies fog)
     */
    public void setBoxObstacleInGrid(BoxObstacle obs, boolean b, TileOccupiedBy o, boolean path) {
        for(int x = screenToTile(obs.getX() - obs.getWidth()/2, path);
            x <= screenToTile(obs.getX() + obs.getWidth()/2, path); x++) {
            for(int y = screenToTile(obs.getY() - obs.getHeight()/2, path);
                y <= screenToTile(obs.getY() + obs.getHeight()/2, path); y++) {
                if (!inBounds(x, y, path)) continue;
                markObstacleTypeInGrid(x, y, b, o, path);
            }
        }
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
     * @param path True if path (false implies fog)
     *
     * @return the tile cell index for a screen position.
     */
    public int screenToTile(float f, boolean path) {
        if(path)
            return (int)(f / PATH_TILE_SIZE);
        return (int)(f / FOG_TILE_SIZE);
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
     * @param path True if path (false implies fog)
     *
     * @return the screen position coordinate for a tile cell index.
     */
    public float tileToScreen(int n, boolean path) {
        if(path)
            return (float) (n + 0.5f) * PATH_TILE_SIZE;
        return (float) (n + 0.5f) * FOG_TILE_SIZE;
    }

    /**
     * Returns true if the given position is a valid tile
     *
     * It does not return if the tile is safe or not
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @param path True if path (false implies fog)
     *
     * @return true if the given position is a valid tile
     */
    public boolean inBounds(int x, int y, boolean path) {
        if(path)
            return x >= 0 && y >= 0 && x < pathTileGrid.length && y < pathTileGrid[0].length;
        return x >= 0 && y >= 0 && x < fogTileGrid.length && y < fogTileGrid[0].length;
    }

    /**
     * Returns whether the input tile is available for movement.
     *
     * @param x Tile x-coor
     * @param y Tile y-coor
     * @param path True if path (false implies fog)
     * @return isSafe boolean
     */
    public boolean isSafe(int x, int y, boolean path) {
        if(path)
            return inBounds(x,y, true) && (!(pathTileGrid[x][y].wall) ||pathTileGrid[x][y].goal);
        return inBounds(x,y, false) && (!(fogTileGrid[x][y].wall) ||fogTileGrid[x][y].goal);
    } //TODO: temporary change

    /** Whether wall is on a tile. */
    public boolean hasWall(int x, int y, boolean path) {
        if(path)
            return pathTileGrid[x][y].wall;
        return fogTileGrid[x][y].wall;
    }

    /** Whether player is on a tile. */
    public boolean hasPlayer(int x, int y, boolean path) {
        if(path)
            return pathTileGrid[x][y].player;
        return fogTileGrid[x][y].player;
    }

    /** Whether enemy is on a tile. */
    public boolean hasEnemy(int x, int y, boolean path) {
        if(path)
            return pathTileGrid[x][y].enemy;
        return fogTileGrid[x][y].enemy;
    }

    /** Fog tile grid size. */
    public int[] fogTileGridSize() {
        return new int[]{fogTileGrid.length, fogTileGrid[0].length};
    }

    /**
     * Returns true if the tile has been visited.
     *
     * A tile position that is not on the board will return false.
     * Precondition: tile in bounds
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @param path True if path (false implies fog)
     *
     * @return true if the tile is a goal.
     */
    public boolean isVisited(int x, int y, boolean path){
        if (!inBounds(x,y,path)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return false;
        }
        Tile[][] tileGrid = path ? pathTileGrid : fogTileGrid;
        return tileGrid[x][y].visited;
    }

    /**
     * Marks a tile as visited.
     *
     * A marked tile will return true for isVisited(), until a call to clearAllTiles().
     * A tile position that is not on the board will raise an error
     * Precondition: tile in bounds
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @param path True if path (false implies fog)
     */
    public void setVisited(int x, int y, boolean path){
        if (!inBounds(x,y,path)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        Tile[][] tileGrid = path ? pathTileGrid : fogTileGrid;
        tileGrid[x][y].visited = true;
    }

    /**
     * Returns true if the tile is a goal.
     *
     * A tile position that is not on the board will return false.
     * Precondition: tile in bounds
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @param path True if path (false implies fog)
     *
     * @return true if the tile is a goal.
     */
    public boolean isGoal(int x, int y, boolean path){
        if (!inBounds(x,y,path)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return false;
        }
        Tile[][] tileGrid = path ? pathTileGrid : fogTileGrid;
        return tileGrid[x][y].goal;
    }

    /**
     * Marks a tile as a goal.
     *
     * A marked tile will return true for isGoal(), until a call to clearAllTiles().
     * A tile position that is not on the board will raise an error
     * Precondition: tile in bounds
     *
     * ONLY USED BY PATH
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setGoal(int x, int y){
        if (!inBounds(x,y,true)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        pathTileGrid[x][y].goal = true;
    }

    /**
     * Set the goal and visited of each tile to false only for PATHFINDING
     */
    public void clearAllTiles() {
        for (int x = 0; x < pathTileGrid.length; x++) {
            for (int y = 0; y < pathTileGrid[0].length; y++) {
                pathTileGrid[x][y].goal = false;
                pathTileGrid[x][y].visited = false;
            }
        }
    }

    public void update(PlayerModel p, Collection<EnemyModel> em) {
        updateGrid(pathTileGrid);
        updateGrid(fogTileGrid);
        placePlayer(p);
        for (EnemyModel e : em) {
            placeEnemy(e);
        }
    }

    public void updateGrid(Tile[][] tileGrid){
        for (int x = 0; x < tileGrid.length; x++) {
            for (int y = 0; y < tileGrid[0].length; y++) {
                tileGrid[x][y].enemy = false;
                tileGrid[x][y].player = false;
            }
        }
    }

    /** PATH CONSTANT DETERMINES WHICH DEBUG IS DRAWN - path or fog */
    private static boolean DEBUG_DRAW_PATH = true;
    public void drawDebug(GameCanvas canvas, Vector2 drawScale) {
        Tile[][] tileGrid = DEBUG_DRAW_PATH ? pathTileGrid : fogTileGrid;
        float tileSize = DEBUG_DRAW_PATH ? PATH_TILE_SIZE : FOG_TILE_SIZE;
        for (int x = 0; x < tileGrid.length; x++) {
            for (int y = 0; y < tileGrid[0].length; y++) {
                canvas.drawGrid(x, y, isSafe(x, y, DEBUG_DRAW_PATH), drawScale, tileSize);
            }
        }
    }
}
