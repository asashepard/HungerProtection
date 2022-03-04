package com.gmail.creepycucumber1.hungerprotection.claim;

import org.bukkit.util.BoundingBox;

import java.util.ArrayList;

public class Subdivision {

    private BoundingBox boundingBox;
    private boolean explosions;
    private boolean isPrivate;

    public Subdivision(BoundingBox boundingBox, boolean explosions, boolean isPrivate) {
        this.boundingBox = boundingBox;
        this.explosions = explosions;
        this.isPrivate = isPrivate;
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

    public boolean getIsExplosions() {
        return explosions;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

}
