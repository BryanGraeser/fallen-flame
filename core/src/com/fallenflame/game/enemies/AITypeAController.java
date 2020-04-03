package com.fallenflame.game.enemies;

import com.badlogic.gdx.math.Vector2;
import com.fallenflame.game.FlareModel;
import com.fallenflame.game.LevelModel;
import com.fallenflame.game.PlayerModel;
import com.fallenflame.game.enemies.EnemyModel;

import java.util.*;

public class AITypeAController extends AIController {
    /**
     * Enumeration to encode the finite state machine.
     */
    private enum FSMState {
        /** The enemy does not have a target */
        IDLE,
        /** The enemy is chasing the player */
        CHASE,
        /** The enemy is moving towards the player's last known location or a flare */
        INVESTIGATE,
    }

    // Constants
    /** The radius from which an enemy could have considered to have finished its investigation
     * of a flare or of a player's last-known location*/
    private static final int REACHED_INVESTIGATE = 1;

    // Instance Attributes
    /** The enemy's current state*/
    private FSMState state;
    /** The player*/
    private PlayerModel player;
    /** The enemy being controlled by this AIController */
    private EnemyTypeAModel enemy;

    /**
     * Creates an AIController for the enemy with the given id.
     *
     * @param id The unique enemy identifier
     * @param level The game level (for pathfinding)
     * @param enemies The list of enemies
     * @param player The player to target
     * @param flares The flares that may attract the enemy
     */
    public AITypeAController(int id, LevelModel level, List<EnemyModel> enemies, PlayerModel player,
                        List<FlareModel> flares) {
        super(id, level, enemies);
        this.player = player;
        assert(enemy.getClass() == EnemyTypeAModel.class);
        this.enemy = (EnemyTypeAModel)super.enemy;
        // this.flares = flares;
        state = FSMState.IDLE;
        // action = Action.NO_ACTION;
    }

    /**
     * Change the state of the enemy using a Finite State Machine.
     */
    protected void changeStateIfApplicable() {
        switch(state) {
            case IDLE:
                enemy.setActivated(false);
                if(withinChase()){
                    state = FSMState.CHASE;
                    break;
                }
                break;

            case CHASE:
                enemy.setActivated(true);

                if(!withinChase()){
                    state = FSMState.INVESTIGATE;
                    enemy.setInvestigatePosition(new Vector2(player.getX(), player.getY()));
                }
                break;

            case INVESTIGATE:
                assert enemy.getInvestigatePosition() != null;
                if(withinChase()){
                    state = FSMState.CHASE;
                    enemy.setInvestigatePosition(null);
                }

                else if(investigateReached()){
                    enemy.setInvestigatePosition(null);
                    enemy.setActivated(false);
                    state = FSMState.IDLE;
                }
                break;

            default:
                assert false;
        }
    }

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     */
    protected void markGoalTiles() {
        switch(state) {
            case IDLE:
                break; // no goal tile

            case CHASE:
                level.setGoal(level.screenToTile(player.getX()), level.screenToTile(player.getY()));
                //System.out.println("Goal chase: " + level.screenToTile(player.getX()) + ", " + level.screenToTile(player.getY()));
                break;

            case INVESTIGATE:
                level.setGoal(level.screenToTile(enemy.getInvestigatePositionX()),
                        level.screenToTile(enemy.getInvestigatePositionY()));
                //System.out.println("Goal inv: " + level.screenToTile(enemy.getInvestigatePositionX()) + "," + level.screenToTile(enemy.getInvestigatePositionY()));
                break;

            default:
                assert false;
        }
    }

//    /**
//     * Determines action based on enemy state and goal tiles and saves action to `action` variable
//     */
//    protected void chooseAction() {
//        if(state == FSMState.IDLE)
//            action = EnemyModel.Action.NO_ACTION;
//        else
//            action = getMoveAlongPathToGoalTile();
//    }

    /** Determines whether the player has reached the coordinates they are investigating */
    private boolean investigateReached(){
        double distance = cartesianDistance(level.screenToTile(enemy.getX()),
                level.screenToTile(enemy.getInvestigatePositionX()),
                level.screenToTile(enemy.getY()),
                level.screenToTile(enemy.getInvestigatePositionY()));
        return distance <= REACHED_INVESTIGATE;
    }

    /** Returns whether an enemy is in range to chase a player */
    private boolean withinChase(){
        double distance = cartesianDistance(enemy.getX(),player.getX(),enemy.getY(),player.getY());
        return distance <= player.getLightRadius();
    }
}