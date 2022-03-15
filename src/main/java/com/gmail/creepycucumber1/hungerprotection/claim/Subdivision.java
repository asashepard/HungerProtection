package com.gmail.creepycucumber1.hungerprotection.claim;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@SerializableAs("Subdivision")
public class Subdivision implements Comparable, ConfigurationSerializable {

    private BoundingBox boundingBox;
    private boolean explosions;
    private boolean isPrivate;

    public Subdivision(BoundingBox boundingBox, boolean explosions, boolean isPrivate) {
        this.boundingBox = boundingBox;
        this.explosions = explosions;
        this.isPrivate = isPrivate;
    }

    public Map<String, Object> serialize() {
        LinkedHashMap result = new LinkedHashMap();
        result.put("boundingBox", this.getBoundingBox());
        result.put("explosions", this.getIsExplosions());
        result.put("isPrivate", this.getIsPrivate());
        return result;
    }

    public static Subdivision deserialize(Map<String, Object> map) {
        return new Subdivision(
                (BoundingBox) map.get("boundingBox"),
                (Boolean) map.get("explosions"),
                (Boolean) map.get("isPrivate")
        );
    }

    //setter
    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    //getter
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public BoundingBox getVisualBox() {
        BoundingBox box = getBoundingBox();
        return new BoundingBox(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX() - 1, box.getMaxY(), box.getMaxZ() - 1);
    }

    public boolean getIsExplosions() {
        return explosions;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

    //other
    @Override
    public int compareTo(@NotNull Object o) {
        Subdivision subdivision = (Subdivision) o;
        if(subdivision.getIsPrivate() == this.isPrivate && subdivision.getIsExplosions() == this.explosions &&
                subdivision.getBoundingBox().equals(this.getBoundingBox()))
            return 0;
        return -1;
    }

}
