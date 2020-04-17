package com.fallenflame.game;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;
import java.util.List;

import static com.fallenflame.game.LevelModel.TILE_SIZE;

public class FogController {
    private ParticleEffectPool fogPool;
    private fogParticle[][] fog;
    private LevelModel levelModel;
    private PlayerModel playerModel; //Needed for light radius
    private List<FlareModel> flareModels;//Needed for flare light radius
    private int tileGridW;
    private int tileGridH;

    private final int[] DIRECTIONS = {1, -1};
    public void initialize(ParticleEffect fogTemplate, LevelModel lm, PlayerModel pm, List<FlareModel> fm) {
        /*Using a pool doesn't actually help much, as if the number of models is higher than the max it just makes a new
        object. However, it has a slight performance help in terms of reusing objects. 100 is a random value, can be changed*/
        fogPool = new ParticleEffectPool(fogTemplate, 0, 150);
        levelModel = lm;
        playerModel = pm;
        flareModels = fm;
        int[] n = levelModel.tileGridSize();
        tileGridW = n[0];
        tileGridH = n[1];
        /*Using a 2D array of an array (called fogParticle) to keep track of which fog particles are complete and need
        * new particles versus which ones do not. This fixes the initial issue of us creating 10,000 fog particles as
        * fog particles were created whether or not the particle around that tile was complete*/
        fog = new fogParticle[tileGridW][tileGridH];
    }

    public void updateFog(Vector2 scale) {
        for (int x = 0; x < tileGridW; x++) {
            for (int y = 0; y < tileGridH; y++) {
                //To prevent drawing on tiles with the player or a wall as well as if its within the light radius
                if (levelModel.hasWall(x, y) || levelModel.hasPlayer(x, y)) continue;
                if(y%5 == 0 && !levelModel.hasEnemy(x,y)) continue;
                boolean withinLight = (Math.pow((Math.pow((x*TILE_SIZE) - (playerModel.getX()), 2) +
                        Math.pow((y*TILE_SIZE) - (playerModel.getY()), 2)), 0.5))
                        <= playerModel.getLightRadius();
                if(withinLight) continue;
                Iterator<FlareModel> iterator = flareModels.iterator();
                while(iterator.hasNext() && !withinLight){
                    FlareModel flare = iterator.next();
                    withinLight = (Math.pow((Math.pow((x*TILE_SIZE) - (flare.getX()), 2) +
                            Math.pow((y*TILE_SIZE) - (flare.getY()), 2)), 0.5))
                            <= flare.getLightRadius();
                }
                if(withinLight) continue;
                if(fog[x][y] == null) {
                    fog[x][y] = new fogParticle();
                }
                Array<ParticleEffectPool.PooledEffect> fogArr = fog[x][y].fogParticles;
                /*Free up complete fog particles so new ones can hopefully use more of the pool's resources*/
                for(ParticleEffectPool.PooledEffect effect: fogArr){
                    effect.isComplete();
                    effect.free();
                    fogArr.removeValue(effect, true);
                }
                /*Only make a new fog particle if we do not have enough particles in the array for that tile*/
                if (fogArr.size < 2|| (levelModel.hasEnemy(x, y) && fogArr.size < 12)) {
                    int dir = 0;
                    int toDir;
                    float incX = 0;
                    float incY = 0;
                    if(levelModel.hasEnemy(x,y)){
                        dir = DIRECTIONS[(int)(Math.random()+0.5)];
                       toDir = (int)((Math.random())*4);
                        incX = toDir%2 != 0 ? dir*TILE_SIZE: 0;
                        incY = toDir %2 == 0 ? dir*TILE_SIZE: 0;
                    }
                    ParticleEffectPool.PooledEffect effect = fogPool.obtain();
                    for (int i = 0; i < (1 + (levelModel.hasEnemy(x, y) ? 11 : 1)); i++) {
                        float randomVal = levelModel.hasEnemy(x,y) ? 2.0f : 1.0f;
                        float randomX = (float) (((Math.random()*randomVal)-0.5f) * TILE_SIZE);
                        float randomY = (float) (((Math.random()*randomVal)-0.5f) * TILE_SIZE);
                        effect.setPosition(((x+incX) * TILE_SIZE + randomX) * scale.x, ((y+incY) * TILE_SIZE + randomY) * scale.y);
                        fog[x][y].fogParticles.add(effect);
                    }
                }
            }
        }
    }

    public void draw(GameCanvas canvas, float delta) {
        canvas.begin();
        canvas.drawFog(fog, delta);
        canvas.end();
    }
/**Inner class to represent the fog on one tile
 *fogparticles: An Array of Pooled Effects. Need to use Array as this list's length will vary as things are added/removed
 * */
    protected class fogParticle {
        //TODO: Figure out a system that doesn't involve making an inner class for the sole purpose of having a 2D array of Arrays
        public Array<ParticleEffectPool.PooledEffect> fogParticles;

        /**Creates a new fogParticle with an empty array*/
        public fogParticle(){
            fogParticles = new Array<>();

        }

    }
}

