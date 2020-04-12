package com.fallenflame.game;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import static com.fallenflame.game.LevelModel.TILE_SIZE;

public class FogController {
    private ParticleEffectPool fogPool;
    private fogParticle[][] fog;
    private LevelModel levelModel;
    private int tileGridW;
    private int tileGridH;

    public void initialize(ParticleEffect fogTemplate, LevelModel lm) {
        fogPool = new ParticleEffectPool(fogTemplate, 0, 100);
        levelModel = lm;
        int[] n = levelModel.tileGridSize();
        tileGridW = n[0];
        tileGridH = n[1];
        fog = new fogParticle[tileGridW][tileGridH];
    }

    public void updateFog(Vector2 scale) {
//        for(ParticleEffectPool.PooledEffect effect: fog){
//            if(effect.isComplete()){
//                effect.free();
//                fog.removeValue(effect, true);
//            }
//        }
        Array<Integer> f;
        for (int x = 0; x < tileGridW; x++) {
            for (int y = 0; y < tileGridH; y++) {
                if (levelModel.hasWall(x, y) || levelModel.hasPlayer(x, y)) continue;
                if(fog[x][y] == null){
                    fog[x][y] = new fogParticle();
                }
                Array<ParticleEffectPool.PooledEffect> fogArr = fog[x][y].fogParticles;
                for(ParticleEffectPool.PooledEffect effect: fogArr){
                    effect.isComplete();
                    effect.free();
                    fogArr.removeValue(effect, true);
                }
                if (fogArr.size < 3|| (levelModel.hasEnemy(x, y) && fogArr.size < 5)) {
                    ParticleEffectPool.PooledEffect effect = fogPool.obtain();
                    for (int i = 0; i < (1 + (levelModel.hasEnemy(x, y) ? 3 : 1)); i++) {
                        float randomVal = (float) (Math.random() * TILE_SIZE);
                        effect.setPosition((x * TILE_SIZE + randomVal) * scale.x, (y * TILE_SIZE + randomVal) * scale.y);
                        fog[x][y].fogParticles.add(effect);
                    }
                }
            }
        }
//        for(EnemyModel enemy : enemies) {
//            if(!enemy.isActivated()) {
//                ParticleEffectPool.PooledEffect effect = fogPool.obtain();
//                float randomVal = (float)(Math.random());
//                effect.setPosition((enemy.getX()+randomVal)*scale.x,
//                        (enemy.getY()+randomVal)*scale.y);
//                fog.add(effect);
//            }
//        }
    }

    public void draw(GameCanvas canvas, float delta) {
        canvas.begin();
        canvas.drawFog(fog, delta);
        canvas.end();
    }

    protected class fogParticle {
        public Array<ParticleEffectPool.PooledEffect> fogParticles;

        public fogParticle(){
            fogParticles = new Array<>();
        }

    }
}

