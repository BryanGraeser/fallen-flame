package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.obstacle.BoxObstacle;
import com.fallenflame.game.util.JsonAssetManager;

public class ItemModel extends BoxObstacle implements ILight  {
    /** Item type. For now only items are pick-up flares*/
    private enum ItemType {
        FLARE,
    }
    /** Item type */
    private ItemType type;
    /**Tint of item light */
    private Color tint;
    /** Radius of item light */
    private float lightRadius;
    /** Active status (picked up or not) */
    private boolean active;

    /**
     * Gets item color tint
     * @return light color
     */
    public Color getLightColor() {
        return tint;
    }

    /**
     * Gets item light radius
     * @return light radius
     */
    public float getLightRadius() {
        return lightRadius;
    }

    /**
     * Returns true if item is a flare item
     */
    public boolean isFlare() { return type == ItemType.FLARE; }

    /**
     * Set active status to false
     */
    public void deactivate() { active = false; }

    /**
     * Create a new ItemModel with degenerate settings
     */
    public ItemModel() {
        super(0,0,1,1);
        active = true;
        setSensor(true);
    }

    /**
     * Initializes the item door via the given JSON value, position, itemType
     *
     * @param globalJson	the JSON subtree defining global item data
     * @param levelItemJson the JSON subtree defining level-specific item data (type and pos)
     */
    public void initialize(JsonValue globalJson, JsonValue levelItemJson){
        float[] pos = levelItemJson.get("itemPos").asFloatArray();
        setPosition(pos[0],pos[1]);
        String typeString = levelItemJson.get("itemType").asString();
        switch (typeString){
            case "flare":
                type = ItemType.FLARE;
                break;
            default:
                Gdx.app.error("ItemModel", "Illegal item type", new IllegalArgumentException());
        }
        // Get global JSON for specific type
        globalJson = globalJson.get(typeString);
        float[] size = globalJson.get("size").asFloatArray();
        setDimension(size[0],size[1]);
        setBodyType(globalJson.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(globalJson.get("density").asFloat());
        setFriction(globalJson.get("friction").asFloat());
        setRestitution(globalJson.get("restitution").asFloat());

        // Light data
        lightRadius = globalJson.get("lightradius").asInt();
        float[] tintValues = globalJson.get("tint").asFloatArray();//RGBA
        tint = new Color(tintValues[0], tintValues[1], tintValues[2], tintValues[3]);

        // Now get the texture from the AssetManager singleton
        String key = globalJson.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        setTexture(texture);
    }
}
