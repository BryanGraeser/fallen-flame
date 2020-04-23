package com.fallenflame.game;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import static com.fallenflame.game.LevelModel.TILE_SIZE;

public class FogController {
    private ParticleEffectPool fogPool;
    private fogParticle[][] fog;
    private LevelModel levelModel;
    private PlayerModel playerModel; //Needed for light radius
    private int tileGridW;
    private int tileGridH;

    public void initialize(ParticleEffect fogTemplate, LevelModel lm, PlayerModel pm) {
        /*Using a pool doesn't actually help much, as if the number of models is higher than the max it just makes a new
        object. However, it has a slight performance help in terms of reusing objects. 100 is a random value, can be changed*/
        fogPool = new ParticleEffectPool(fogTemplate, 0, 150);
        levelModel = lm;
        playerModel = pm;
        int[] n = levelModel.tileGridSize();
        tileGridW = n[0];
        tileGridH = n[1];
        /*Using a 2D array of an array (called fogParticle) to keep track of which fog particles are complete and need
        * new particles versus which ones do not. This fixes the initial issue of us creating 10,000 fog particles as
        * fog particles were created whether or not the particle around that tile was complete*/
        fog = new fogParticle[tileGridW][tileGridH];
    }

    public void updateFogAndDraw(GameCanvas canvas, Vector2 scale, float delta) {
        // Cache values locally so we don't have to do expensive calculations each loop.
        float px = playerModel.getX(), py = playerModel.getY();
        // Camera pos:
        Vector3 cameraPos = canvas.getCamera().position;
        // These are the ratio to translate camera pos to tile pos.
        float ratioX = scale.x * TILE_SIZE, ratioY = scale.y * TILE_SIZE;
        // Bounds of the camera in tile units. Could be out of bounds on tile map! (e.g. lowX could be -3)
        int lowX = (int) Math.floor((cameraPos.x - canvas.getWidth() / 2f) / ratioX),
                highX = (int) Math.floor((cameraPos.x + canvas.getWidth() / 2f) / ratioX),
                lowY = (int) Math.floor((cameraPos.y - canvas.getHeight() / 2f) / ratioY),
                highY = (int) Math.floor((cameraPos.y + canvas.getHeight() / 2f) / ratioY);
        for (int x = 0; x < tileGridW; x++) {
            for (int y = 0; y < tileGridH; y++) {
                // If this tile is not in camera, clear its content.
                if (x < lowX || x >= highX || y < lowY || y >= highY) {
                    if (fog[x][y] != null) {
                        for(ParticleEffectPool.PooledEffect effect: fog[x][y].fogParticles){
                            effect.free();
                        }
                        fog[x][y].fogParticles.clear();
                        fog[x][y] = null;
                    }
                    continue;
                }
                //To prevent drawing on tiles with the player or a wall as well as if its within the light radius
                if (levelModel.hasWall(x, y) || levelModel.hasPlayer(x, y)) continue;
                boolean withinLight = (Math.pow((Math.pow((x*TILE_SIZE) - (px), 2) +
                        Math.pow((y*TILE_SIZE) - (py), 2)), 0.5))
                        <= playerModel.getLightRadius();
                if(withinLight) continue;
                if(fog[x][y] == null) {
                    fog[x][y] = new fogParticle();
                }
                Array<ParticleEffectPool.PooledEffect> fogArr = fog[x][y].fogParticles;
                /*Free up complete fog particles so new ones can hopefully use more of the pool's resources*/
                for(ParticleEffectPool.PooledEffect effect: fogArr){
                    effect.free();
                    fogArr.removeValue(effect, true);
                }
                /*Only make a new fog particle if we do not have enough particles in the array for that tile*/
                if (fogArr.size < 2|| (levelModel.hasEnemy(x, y) && fogArr.size < 9)) {
                    ParticleEffectPool.PooledEffect effect = fogPool.obtain();
                    for (int i = 0; i < (1 + (levelModel.hasEnemy(x, y) ? 8 : 1)); i++) {
                        float randomVal = (float) (Math.random() * TILE_SIZE);
                        effect.setPosition((x * TILE_SIZE + randomVal) * scale.x, (y * TILE_SIZE + randomVal) * scale.y);
                        fog[x][y].fogParticles.add(effect);
                    }
                }
            }
        }
        draw(canvas, delta);
    }

    private void draw(GameCanvas canvas, float delta) {
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

