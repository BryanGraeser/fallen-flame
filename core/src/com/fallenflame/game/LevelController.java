package com.fallenflame.game;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;

import java.util.*;

public class LevelController {
    //  MAY NEED THESE:
//    /** Number of velocity iterations for the constrain solvers */
//    public static final int WORLD_VELOC = 6;
//    /** Number of position iterations for the constrain solvers */
//    public static final int WORLD_POSIT = 2;

    // Physics objects for the level
    /** Reference to the player character */
    private PlayerModel player;
    /** Reference to the exit (for collision detection) */
    private ExitModel exit;
    /** Reference to all enemies */
    private List<EnemyModel> enemies;
    /** Reference to all walls */
    private List<WallModel> walls;
    /** Reference to all flares */
    private List<FlareModel> flares;

    /** Whether or not the level is in debug more (showing off physics) */
    private boolean debug;

    // World Definitions
    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;

    // Controllers
    /** Light Controller */
    private LightController lightController;
    /** AI Controllers */
    private List<AIController> AIControllers;

    // TODO #2: TO FIX THE TIMESTEP? May not need
    /** The maximum frames per second setting for this level */
    protected int maxFPS;
    /** The minimum frames per second setting for this level */
    protected int minFPS;
    /** The amount of time in to cover a single animation frame */
    protected float timeStep;
    /** The maximum number of steps allowed before moving physics forward */
    protected float maxSteps;
    /** The maximum amount of time allowed in a frame */
    protected float maxTimePerFrame;
    /** The amount of time that has passed without updating the frame */
    protected float physicsTimeLeft;
    // TODO #2 End

    /**
     * Returns the bounding rectangle for the physics world
     *
     * The size of the rectangle is in physics, coordinates, not screen coordinates
     *
     * @return the bounding rectangle for the physics world
     */
    public Rectangle getBounds() { return bounds; }

    /**
     * Returns the scaling factor to convert physics coordinates to screen coordinates
     *
     * @return the scaling factor to convert physics coordinates to screen coordinates
     */
    public Vector2 getScale() { return scale; }

    /**
     * Returns a reference to the Box2D World
     *
     * @return a reference to the Box2D World
     */
    public World getWorld() { return world; }

    /**
     * Returns a reference to the player
     *
     * @return a reference to the player
     */
    public PlayerModel getPlayer() { return player; }

    /**
     * Returns a reference to the exit
     *
     * @return a reference to the exit
     */
    public ExitModel getExit() { return exit; }

    /**
     * Returns a reference to the enemies
     *
     * @return a reference to the enemies
     */
    public List<EnemyModel> getEnemies() { return enemies; }

    /**
     * Returns a reference to the walls
     *
     * @return a reference to the walls
     */
    public List<WallModel> getWalls() { return walls; }

    /**
     * Returns a reference to the flares
     *
     * @return a reference to the flares
     */
    public List<FlareModel> getFlares() { return flares; }

    /**
     * Returns whether this level is currently in debug node
     *
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @return whether this level is currently in debug node
     */
    public boolean getDebug() { return debug; }

    /**
     * Sets whether this level is currently in debug node
     *
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @param value	whether this level is currently in debug node
     */
    public void setDebug(boolean value) { debug = value; }

    // TODO #2
    /**
     * Returns the maximum FPS supported by this level
     *
     * This value is used by the rayhandler to fix the physics timestep.
     *
     * @return the maximum FPS supported by this level
     */
    public int getMaxFPS() { return maxFPS; }

    /**
     * Sets the maximum FPS supported by this level
     *
     * This value is used by the rayhandler to fix the physics timestep.
     *
     * @param value the maximum FPS supported by this level
     */
    public void setMaxFPS(int value) { maxFPS = value; }

    /**
     * Returns the minimum FPS supported by this level
     *
     * This value is used by the rayhandler to fix the physics timestep.
     *
     * @return the minimum FPS supported by this level
     */
    public int getMinFPS() { return minFPS; }

    /**
     * Sets the minimum FPS supported by this level
     *
     * This value is used by the rayhandler to fix the physics timestep.
     *
     * @param value the minimum FPS supported by this level
     */
    public void setMinFPS(int value) { minFPS = value; }
    // TODO #2 End

    /**
     * Creates a new LevelModel
     *
     * The level is empty and there is no active physics world.  You must read
     * the JSON file to initialize the level
     */
    public LevelController() {
        world  = null;
        // TODO #4: Fix once light controller constructor is finished
        lightController = new LightController();
        // TODO #4 End
        AIControllers = new LinkedList<>();
        // TODO #3: Check correctness
        bounds = new Rectangle(0,0,1,1);
        scale = new Vector2(1,1);
        // TODO #3 End
        walls = new LinkedList<>();
        enemies = new LinkedList<>();
        flares = new LinkedList<>();
        debug  = false;
    }

    /**
     * Lays out the game geography and enemies from the given JSON file
     *
     * @param levelFormat	the JSON tree defining the level
     */
    public void populate(JsonValue levelFormat) {
        //TODO #8 INIT Player and exit
        player = new PlayerModel();
        exit = new ExitModel();
        //TODO #8 End
        for(JsonValue wallJSON : levelFormat.get("walls")) {
            //TODO #5 INIT walls
            walls.add(new WallModel());
            // TODO #5 End
        }
        for(JsonValue enemyJSON : levelFormat.get("enemies")) {
            //TODO #6 INIT enemies
            EnemyModel enemy = new EnemyModel();
            enemies.add(enemy);
            AIControllers.add(new AIController());
            //TODO #6
        }
        for(JsonValue flareJSON : levelFormat.get("flares")) {
            //TODO #7 INIT flares
            flares.add(new FlareModel());
            //TODO #7
        }
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We handle
     * most collisions here
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {}
    /** Unused ContactListener method */
    public void endContact(Contact contact) {}
    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

}
