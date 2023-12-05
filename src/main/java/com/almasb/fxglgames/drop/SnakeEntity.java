package com.almasb.fxglgames.drop;

import com.almasb.fxgl.entity.Entity;

public class SnakeEntity extends Entity implements SnakeEntityInterface {
    private int size = 0;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public void onCollisionBegin(Entity other) {
        if (!(other instanceof SnakeEntity)) {
            other.removeFromWorld();
        }
    }
}
