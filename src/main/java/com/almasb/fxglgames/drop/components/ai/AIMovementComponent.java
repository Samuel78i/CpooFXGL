package com.almasb.fxglgames.drop.components.ai;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxglgames.drop.Type;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.getInput;

public class AIMovementComponent extends Component {
    List<Entity> bodyParts = new ArrayList<>();
    int countOfFoodEaten = 0;

    int countToMakeTheSnakeLonger = 0;
    int countToMakeTheSnakeLarger = 0;
    private Entity closest;
    int start = 50;


    @Override
    public void onUpdate(double tpf) {
        move();
    }

    private void move() {
        FXGL.getGameWorld().getEntitiesByType(Type.FOOD).forEach(this::isThisTheClosestOne);

        this.getEntity().translate(getInput().getMousePositionWorld().subtract(this.getEntity().getPosition()).normalize().multiply(0.8));
    }



    private void isThisTheClosestOne(Entity food) {
        if(closest != null) {
            if (this.getEntity().getPosition().distance(closest.getPosition()) > this.getEntity().getPosition().distance(food.getPosition())) {
                closest = food;
            }
        }else {
            closest = food;
        }
    }

    public void aFoodAsBeenEaten(){
        countOfFoodEaten++;
        countToMakeTheSnakeLarger++;
    }
}
