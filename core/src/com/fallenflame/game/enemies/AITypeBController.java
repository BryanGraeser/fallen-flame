package com.fallenflame.game.enemies;

import com.fallenflame.game.LevelModel;
import com.fallenflame.game.PlayerModel;

import java.util.List;

public class AITypeBController extends AIController {

    /**
     * Enumeration to encode the finite state machine.
     */
    private enum FSMState {
        /** The enemy does not have a target */
        IDLE,
        /** The enemy is firing at the player (or player's last known position)
         * TODO: may want to split into attack and sustained fire*/
        FIRING,
    }

    // Constants
    private static final int FIRING_TIME = 2000;

    // Instance Attributes
    /** The enemy's current state*/
    private FSMState state;
    /** The player*/
    private PlayerModel player;
    /** The enemy being controlled by this AIController */
    private EnemyTypeBModel enemy;

    /**
     * Creates an AIController for the enemy with the given id.
     *
     * @param id The unique enemy identifier
     * @param level The game level (for pathfinding)
     * @param enemies The list of enemies
     * @param player The player to target
     */
    public AITypeBController(int id, LevelModel level, List<EnemyModel> enemies, PlayerModel player) {
        super(id, level, enemies);
        this.player = player;
        assert(enemy.getClass() == EnemyTypeBModel.class);
        this.enemy = (EnemyTypeBModel)super.enemy;
        state = FSMState.IDLE;
    }

    /**
     * Change the state of the enemy using a Finite State Machine.
     */
    protected void changeStateIfApplicable() {

    }

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     */
    protected void markGoalTiles() {
        level.setGoal(level.screenToTile(enemy.getX()), level.screenToTile(enemy.getY()));
    }
}
