package com.almasb.fxglgames.drop.components.ai;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxglgames.drop.Type;
import com.almasb.fxglgames.drop.components.SnakeComponent;
import javafx.geometry.Point2D;


import java.util.Optional;

public class AIMovementComponent extends SnakeComponent {
    private Optional<Entity> closestFood = Optional.empty();

    @Override
    public void onUpdate(double tpf) {
        move();
    }

    private void move() {

        Point2D oldPosition = this.getEntity().getPosition();
        FXGL.getGameWorld().getEntitiesByType(Type.FOOD).forEach(this::findTheClosestOne);
        if (closestFood.isPresent() && closestFood.get().isActive()) {
            this.getEntity().translate(closestFood.get().getPosition().subtract(this.getEntity().getPosition()).normalize().multiply(0.8));

            moveBodyParts(oldPosition.getX(), oldPosition.getY());
        }
    }

    @Override
    protected boolean shouldChangeSize() {
        return closestFood.isPresent();
    }

    private void findTheClosestOne(Entity food) {
        if (closestFood.isPresent() && closestFood.get().isActive()) {
            if (this.getEntity().getPosition().distance(closestFood.get().getPosition()) > this.getEntity().getPosition().distance(food.getPosition())) {
                closestFood = Optional.of(food);
            }
        } else {
            closestFood = Optional.of(food);
        }
    }

    public void aFoodAsBeenEaten() {
        countOfFoodEaten++;
        closestFood = Optional.empty();
    }

}
