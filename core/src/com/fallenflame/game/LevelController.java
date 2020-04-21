package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.fallenflame.game.enemies.*;
import com.fallenflame.game.physics.obstacle.Obstacle;
import sun.awt.image.ImageWatched;
import com.fallenflame.game.util.JsonAssetManager;

import java.awt.*;
import java.util.*;
import java.util.List;

/** Credit to Walker White for some code reused from B2LightsDemo */
public class LevelController implements ContactListener {
    //  MAY NEED THESE:
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;

    // Sound constants
    /** Base volume for enemy movement sounds */
    public static final float ENEMY_MOV_BASE_VOL = .4f;
    /** Volume scaling for enemy movement sounds.
     * Must be >0. Lower numbers will lead to faster volume drop-off.
     * Value of 1 means drop-off rate is exactly equivalent to 1/distance */
    public static final float ENEMY_MOVE_VOL_SCL = 3f;
    /** Pitch for enemy movement sounds */
    public static final float ENEMY_MOV_PITCH = 1f;
    /** Base volume for enemy constant sounds */
    public static final float ENEMY_CONS_BASE_VOL = 1.7f;
    /** Volume scaling for enemy constant sounds.
     * Must be >0. Lower numbers will lead to faster volume drop-off.
     * Value of 1 means drop-off rate is exactly equivalent to 1/distance */
    public static final float ENEMY_CONS_VOL_SCL = 1f;
    /** Pitch for enemy constant sounds */
    public static final float ENEMY_CONS_PITCH = .5f;
    /** Volume scaling for panning
     * Must be in range [0,1]. 1 is maximum panning, 0 is no panning. */
    public static final float PAN_SCL = .4f;


    /** Whether or not the level has been populated */
    private boolean populated;

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
    /** Reference to all fireballs */
    private List<FireballModel> fireballs;
    /** Level Model for AI Pathfinding */
    private LevelModel levelModel;
    /** Flare JSONValue */
    private JsonValue flareJSON;
    /** Fireball JSONValue */
    private JsonValue fireballJSON;

    /** Whether or not the level is in debug mode (showing off physics) */
    private int debug;
    /** Whether or not the level is in debug 2 mode (unlit area only half dark) */
    private boolean debug2;

    // World Definitions
    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** The world background */
    protected TextureRegion background;

    // Controllers
    /** Light Controller */
    private final LightController lightController;
    /** AI Controllers */
    private final List<AIController> AIControllers;
    private final FogController fogController;

    /** Enum to specify level state */
    public enum LevelState {
        /** Player has reached the exit */
        WIN,
        /** Player has died */
        LOSS,
        /** Player is still playing */
        IN_PROGRESS
    }
    private LevelState levelState;

    // TO FIX THE TIMESTEP
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
    /** FPS of game */
    private float fps;
    /** Number of ticks sense we started this controller (used to limit number of fps updates) */
    private long ticks;

    /**
     * Returns the bounding rectangle for the physics world
     *
     * The size of the rectangle is in physics, coordinates, not screen coordinates
     *
     * @return the bounding rectangle for the physics world
     */
    public Rectangle getBounds() { return bounds; }

    /**
     * Sets the bounding rectangle for the physics world
     *
     * The size of the rectangle is in physics, coordinates, not screen coordinates
     *
     * @value the bounding rectangle for the physics world
     */
    public void setBounds(Rectangle value) { bounds = value; }

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
    public int getDebug() { return debug; }

    /**
     * Returns whether this level is currently in debug node
     *
     * If the level is in debug 2 mode, then unlit area will be half-dark
     *
     * @return whether this level is currently in debug node
     */
    public boolean getDebug2() { return debug2; }

    /**
     * Sets whether this level is currently in debug node
     *
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @param value	whether this level is currently in debug node
     */
    public void setDebug(int value) { debug = value % 3; }

    /**
     * Sets whether this level is currently in debug 2 node
     *
     * If the level is in debug 2 mode, then unlit area will be half-dark
     *
     * @param value	whether this level is currently in debug node
     */
    public void setDebug2(boolean value) { debug2 = value; }

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

    /**
     * Gets the current state of the level.
     *
     * This value is used by GameEngine to know when player has lost or won.
     */
    public LevelState getLevelState() { return levelState; }

    /**
     * Sets the current state of the level.
     *
     * This value is used by GameEngine to know when player has lost or won.
     *
     * @param state Desired levelState
     */
    public void setLevelState(LevelState state) { levelState = state; }

    /**
     * Creates a new LevelModel
     *
     * The level is empty and there is no active physics world.  You must read
     * the JSON file to initialize the level
     */
    public LevelController() {
        // World
        world  = null;
        bounds = new Rectangle(0,0,1,1);
        scale = new Vector2(1,1);
        debug  = 0;
        debug2 = false;
        levelState = LevelState.IN_PROGRESS;
        // Controllers
        lightController = new LightController();
        AIControllers = new LinkedList<>();
        fogController = new FogController();
        // Models
        walls = new LinkedList<>();
        enemies = new LinkedList<>();
        flares = new LinkedList<>();
        fireballs = new LinkedList<>();
        levelModel = new LevelModel();
        // Not yet populated
        populated = false;

    }

    /**
     * Lays out the game geography and enemies from the given JSON file
     *
     * @param levelJson	the JSON tree defining the level
     */
    public void populate(JsonValue levelJson, JsonValue globalJson, ParticleEffect fogTemplate) {
        populated = true;

        float[] pSize = levelJson.get("physicsSize").asFloatArray();
        int[] gSize = levelJson.get("graphicSize").asIntArray();

        world = new World(Vector2.Zero,false);
        bounds = new Rectangle(0,0,pSize[0],pSize[1]);
        scale.x = scale.y = 50;

        String key = globalJson.get("background").get("texture").asString();
        if (levelJson.get("background").has("texture"))
            levelJson.get("background").get("texture").asString(); // Get specific texture if available
        background = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);

        // Compute the FPS
        int[] fps = levelJson.get("fpsRange").asIntArray();
        maxFPS = fps[1]; minFPS = fps[0];
        timeStep = 1.0f/maxFPS;
        maxSteps = 1.0f + maxFPS/minFPS;
        maxTimePerFrame = timeStep*maxSteps;

        // Create player
        player = new PlayerModel();
        player.initialize(globalJson.get("player"), levelJson.get("playerpos").asFloatArray());
        player.setDrawScale(scale);
        player.activatePhysics(world);
        assert inBounds(player);
        // Create Exit
        exit = new ExitModel();
        exit.initialize(globalJson.get("exit"), levelJson.get("exitpos").asFloatArray());
        exit.setDrawScale(scale);
        exit.activatePhysics(world);
        assert inBounds(exit);
        // Create Walls
        for(JsonValue wallJSON : levelJson.get("walls")) {
            WallModel wall = new WallModel();

            if(wallJSON.get("texture").asString().equals("wall-side")) {
                wall.initialize(globalJson.get("wall-side"), wallJSON);
            } else {
                wall.initialize(globalJson.get("wall-top"), wallJSON);
            }

            wall.setDrawScale(scale);
            wall.activatePhysics(world);
            walls.add(wall);
            assert inBounds(wall);
        }
        // Create enemies
        int enemyID = 0;
        JsonValue globalEnemies = globalJson.get("enemies");
        for(JsonValue enemyJSON : levelJson.get("enemies")) {
            String enemyType = enemyJSON.get("enemytype").asString();
            // Initialize Enemy Model
            EnemyModel enemy;
            if(enemyType.equals("typeA")) {
                enemy = new EnemyTypeAModel();
            }
            else if(enemyType.equals("typeB")){
                enemy = new EnemyTypeBModel();
            }
            else {
                Gdx.app.error("LevelController", "Enemy type without model", new IllegalArgumentException());
                return;
            }
            enemy.initialize(globalEnemies.get(enemyType), enemyJSON.get("enemypos").asFloatArray());
            enemy.setConstantSoundID(enemy.getConstantSound().loop(0, ENEMY_CONS_PITCH, 0));
            enemy.setDrawScale(scale);
            enemy.activatePhysics(world);
            enemies.add(enemy);
            // Initialize AIController
            if(enemyType.equals("typeA")) {
                AIControllers.add(new AITypeAController(enemyID, levelModel, enemies, player, flares));
            }
            else if(enemyType.equals("typeB")) {
                AIControllers.add(new AITypeBController(enemyID, levelModel, enemies, player));
            }
            else{
                Gdx.app.error("LevelController", "Enemy type without AIController", new IllegalArgumentException());
                return;
            }

            enemyID++;
            assert inBounds(enemy);
        }
        // Prepare flare and fireball jsons
        flareJSON = globalJson.get("flare");
        fireballJSON = globalJson.get("fireball");

        // Initialize levelModel, lightController, and fogController
        levelModel.initialize(bounds, walls, enemies);

        lightController.initialize(player, levelJson.get("lighting"), world, scale);
        fogController.initialize(fogTemplate, levelModel, player);
    }

    /**
     * Disposes of all resources for this model.
     *
     * Because of all the heavy weight physics stuff, this method is absolutely
     * necessary whenever we reset a level.
     */
    public void dispose() {
        if(!populated)
            return;

        lightController.dispose();

        for(WallModel wall : walls) {
            wall.deactivatePhysics(world);
            wall.dispose();
        }
        walls.clear();
        for(EnemyModel enemy : enemies) {
            enemy.getConstantSound().stop();
            enemy.deactivatePhysics(world);
            enemy.dispose();
        }
        enemies.clear();
        for(FlareModel flare : flares) {
            flare.deactivatePhysics(world);
            flare.dispose();
        }
        flares.clear();
        for(FireballModel fireball : fireballs) {
            fireball.deactivatePhysics(world);
            fireball.dispose();
        }
        fireballs.clear();
        exit.deactivatePhysics(world);
        exit.dispose();
        player.deactivatePhysics(world);
        player.dispose();

        if (world != null) {
            world.dispose();
            world = null;
        }
        populated = false;
    }

    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    private boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    /**
     * Updates all of the models in the level.
     *
     * This is borderline controller functionality.  However, we have to do this because
     * of how tightly coupled everything is.
     *
     * @param dt the time passed since the last frame
     */
    public void update(float dt) {
        if(fixedStep(dt)){
            // Update player and exit
            player.update(dt);
            assert inBounds(player);

            // TODO: handle enemy placement in levelmodel

            // Get Enemy Actions
            Iterator<AIController> ctrlI = AIControllers.iterator();
            LinkedList<Integer> ctrlCodes = new LinkedList();
            while(ctrlI.hasNext()){
                AIController ctrl = ctrlI.next();
                ctrlCodes.add(ctrl.getAction());
            }
            // Execute Enemy Actions
            Iterator<EnemyModel> enemyI = enemies.iterator();
            Iterator<Integer> actionI = ctrlCodes.iterator();
            while(enemyI.hasNext()){
                EnemyModel enemy = enemyI.next();
                int action = actionI.next();
                enemy.executeMovementAction(action);
                // Check if enemy is firing, for now only supports EnemyTypeBModel. TODO: Will need to rework if more firing enemies
                boolean firing = (action & EnemyModel.CONTROL_FIRE) != 0;
                if (enemy.getClass() == EnemyTypeBModel.class && firing) {
                    if(((EnemyTypeBModel)enemy).canFire())
                        fireWeapon((EnemyTypeBModel)enemy);
                    else
                        ((EnemyTypeBModel)enemy).coolDown(true);
                }
                // Play enemy sounds
                float pan = (enemy.getX() - player.getX()) * PAN_SCL;
                if (enemy.isActivated() && (enemy.getMoveSoundID() == -1)) {
                    //start sound
                    enemy.setMoveSoundID(enemy.getMoveSound().loop(ENEMY_MOV_BASE_VOL, ENEMY_MOV_PITCH, pan));
                } else if (!enemy.isActivated()) {
                    //end sound
                    enemy.getMoveSound().stop();
                    enemy.setMoveSoundID(-1);
                } else {
                    //modify sound
                    enemy.getMoveSound().setPan(enemy.getMoveSoundID(), pan, ENEMY_MOV_BASE_VOL * ((1/enemy.getDistanceBetween(player) * ENEMY_MOVE_VOL_SCL)));
                }
                enemy.getConstantSound().setPan(enemy.getConstantSoundID(), pan, ENEMY_CONS_BASE_VOL * ((1/enemy.getDistanceBetween(player) * ENEMY_CONS_VOL_SCL)));
                assert inBounds(enemy);
            }

            // Update flares
            Iterator<FlareModel> i = flares.iterator();
            while(i.hasNext()){
                FlareModel flare = i.next();
                if(!(Float.compare(flare.timeToBurnout(), 0.0f) > 0)){
                    flare.deactivatePhysics(world);
                    flare.getBurnoutSound().play();
                    flare.dispose();
                    i.remove();
                }
                else {
                    flare.update(dt);
                }
            }
            // Remove old fireballs
            Iterator<FireballModel> ii = fireballs.iterator();
            while(ii.hasNext()){
                FireballModel f = ii.next();
                if(!f.isActive()){
                    f.deactivatePhysics(world);
                    f.dispose();
                    ii.remove();
                }
            }

            // Update level model.
            levelModel.update(player, enemies);

            // Update lights
            lightController.updateLights(flares, enemies, fireballs);

            // Update fog.
            fogController.updateFog(scale);
        }
    }

    /**
     * Fixes the physics frame rate to be in sync with the animation framerate
     *
     * http://gafferongames.com/game-physics/fix-your-timestep/
     *
     * @param dt the time passed since the last frame
     */
    private boolean fixedStep(float dt) {
        if (world == null) return false;

        physicsTimeLeft += dt;
        if (physicsTimeLeft > maxTimePerFrame) {
            physicsTimeLeft = maxTimePerFrame;
        }

        boolean stepped = false;
        while (physicsTimeLeft >= timeStep) {
            world.step(timeStep, WORLD_VELOC, WORLD_POSIT);
            physicsTimeLeft -= timeStep;
            stepped = true;
        }
        return stepped;
    }


    /**
     * Launch a flare from the player towards the mouse position based on preset flareJSON data, or does nothing if the
     * player has already created the max number of flares.
     * (Called by GameEngine)
     *
     * @param mousePosition Position of mouse when flare launched
     */
    public void createFlare(Vector2 mousePosition){
        if (flares.size() < player.getFlareCount()) {
            FlareModel flare = new FlareModel(player.getPosition());
            flare.setDrawScale(scale);
            flare.initialize(flareJSON);
            flare.activatePhysics(world);
            Vector2 centerScreenPosition = new Vector2((bounds.width * scale.x) / 2, (bounds.height * scale.y) / 2);
            Vector2 posDif = new Vector2(mousePosition.x - centerScreenPosition.x, mousePosition.y - centerScreenPosition.y);
            float angleRad = posDif.angleRad(new Vector2(1, 0));
            Vector2 force = (new Vector2(flare.getInitialForce(), 0)).rotateRad(angleRad);
            flare.applyInitialForce(angleRad, force);
            flare.getShotSound().play();
            flares.add(flare);
            assert inBounds(flare);
        }
    }

    /**
     * Fires a bullet from an enemy
     */
    public void fireWeapon(EnemyTypeBModel enemy) {
        Vector2 enemyPos = enemy.getPosition();
        FireballModel fireball = new FireballModel(enemyPos);
        fireball.setDrawScale(scale);
        fireball.initialize(fireballJSON);
        fireball.activatePhysics(world);
        Vector2 posDif = new Vector2(enemy.getFiringTarget().x - enemyPos.x, enemy.getFiringTarget().y- enemyPos.y);
        posDif.nor();  // Normalize vector
        fireball.applyInitialForce(posDif);
        fireballs.add(fireball);
        enemy.coolDown(false);
        assert inBounds(fireball);
    }

    /**
     * Change the player's movement to sprint
     * Store current light radius in lightRadiusSaved and change light radius to lightRadiusSprint
     * (Called by GameEngine)
     */
    public void makeSprint(){
        player.setLightRadiusSaved(player.getLightRadius());
        player.setLightRadius(player.getLightRadiusSprint());
        player.setWalking(false);
        player.setForce(player.getForceSprint());
    }

    /**
     * Change the player's movement to walk
     * Set to walk and restore light radius to what it was before sprinting, which is in lightRadiusSaved
     * (Called by GameEngine)
     */
    public void makeWalk(){
        player.setLightRadius(player.getLightRadiusSaved());
        player.setWalking(true);
        player.setForce(player.getForceWalk());
    }

    /**
     * Change the player's movement to sneak
     * Store current light radius in lightRadiusSaved and change light radius to lightRadiusSneak
     * (Called by GameEngine)
     */
    public void makeSneak(){
        player.setLightRadiusSaved(player.getLightRadius());
        player.setLightRadiusSneak();
        player.setWalking(false);
        player.setForce(player.getForceSneak());
    }

    /**
     * Moves the player. (Called by GameEngine)
     * @param angle angle player is facing
     * @param tempAngle movement angle of player (to be scaled by player force)
     */
    public void movePlayer(float angle, Vector2 tempAngle) {
        tempAngle.scl(player.getForce());
        player.setMovement(tempAngle.x, tempAngle.y);
        if (!tempAngle.isZero()) player.setAngle(angle);
        player.applyForce();
    }

    /**
     * Change lightRadius generated from player. (Called by GameEngine)
     * @param lightRadius radius of light around player
     */
    public void lightFromPlayer(float lightRadius) {
        if(player.isWalking())
            player.incrementLightRadius(lightRadius);
    }

    /**
     * Draws the level to the given game canvas
     *
     * If debug mode is true, it will outline all physics bodies as wireframes. Otherwise
     * it will only draw the sprite representations.
     *
     * @param canvas	the drawing context
     */
    public void draw(GameCanvas canvas, float delta, BitmapFont displayFont) {
        canvas.clear();
        canvas.setCameraPosition(player.getPosition().x * scale.x, player.getPosition().y * scale.y);

        canvas.begin();
        //draw background
        if (background != null) {
            canvas.draw(background, Color.WHITE, 0,0, (float) canvas.getWidth(), (float) canvas.getHeight());
        }

        // Draw all objects
        exit.draw(canvas);
        for(WallModel wall : walls) {
            wall.draw(canvas);
        }
        for(EnemyModel enemy : enemies) {
            enemy.draw(canvas);
        }
        for(FlareModel flare : flares) {
            flare.draw(canvas);
        }
        for(FireballModel fireball : fireballs){
            fireball.draw(canvas);
        }
        player.draw(canvas);
        canvas.end();

        lightController.setDebug(debug2);
        lightController.draw();
        fogController.draw(canvas, delta);

        // Draw debugging on top of everything.
        if (debug == 1) {
            canvas.beginDebug();
            player.drawDebug(canvas);
            exit.drawDebug(canvas);
            for(WallModel wall : walls) {
                wall.drawDebug(canvas);
            }
            for(FlareModel flare : flares) {
                flare.drawDebug(canvas);
            }
            for(EnemyModel enemy : enemies) {
                enemy.drawDebug(canvas);
            }
            for(FireballModel fireball: fireballs){
                fireball.drawDebug(canvas);
            }
            canvas.endDebug();
            if(ticks % 10 == 0){
                fps = 1/delta;
            }
            displayFont.setColor(Color.YELLOW);
            canvas.begin();
            canvas.drawText(Float.toString(fps), displayFont, 0, canvas.getHeight()/2);
            canvas.end();
            ticks++;
        } else if (debug == 2) {
            canvas.beginDebugFilled();
            levelModel.drawDebug(canvas, scale);
            canvas.endDebug();
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
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            // Check for win condition
            if ((bd1 == player && bd2 == exit  )
                    || (bd1 == exit && bd2 == player)) {
            setLevelState(levelState.WIN);
                return;
            }
            // Check for loss condition 1 (player runs into enemy)
            if((bd1 == player && bd2 instanceof EnemyModel)
                    || (bd1 instanceof  EnemyModel && bd2 == player)){
                setLevelState(LevelState.LOSS);
                return;
            }
            // Check if flare collides with wall and if so stop it
            if((bd1 instanceof FlareModel && bd2 instanceof WallModel
                    || bd1 instanceof  WallModel && bd2 instanceof FlareModel)) {
                if(bd1 instanceof FlareModel)
                    ((FlareModel) bd1).stopMovement();
                else
                    ((FlareModel) bd2).stopMovement();
            }
            // Check for loss condition 2 (fireball hits player)
            if((bd1 instanceof FireballModel && bd2 instanceof PlayerModel
                    || bd1 instanceof  PlayerModel && bd2 instanceof FireballModel)) {
                setLevelState(LevelState.LOSS);
                return;
            }
            // Check for fireball-wall collision and if so remove fireball
            if((bd1 instanceof FireballModel && bd2 instanceof WallModel
                    || bd1 instanceof  WallModel && bd2 instanceof FireballModel)) {
                if(bd1 instanceof FireballModel){
                    ((FireballModel) bd1).deactivate();
                }
                else{
                    ((FireballModel) bd2).deactivate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** Unused ContactListener method */
    public void endContact(Contact contact) {}
    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

}

