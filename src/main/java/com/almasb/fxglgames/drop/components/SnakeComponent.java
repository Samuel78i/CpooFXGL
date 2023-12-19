package com.almasb.fxglgames.drop.components;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxglgames.drop.Type;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.texture;

public class SnakeComponent extends Component {

    private int countOfFoodEaten = 0;

    private int countToMakeTheSnakeLonger = 0;
    private int countToMakeTheSnakeLarger = 0;

    private int start = 50;

    private double lastX;
    private double lastY;
    private Point2D vectorToMouse;
    private UserSnakeMovementComponents userSnakeMovementComponents;


    @Override
    public void onUpdate(double tpf) {
        if (countOfFoodEaten > 5) {
            countOfFoodEaten = 0;
            makeTheSnakeLonger(lastX, lastY, vectorToMouse);
            countToMakeTheSnakeLonger = 20;
        }else if(countToMakeTheSnakeLonger > 0){
            makeTheSnakeLonger(lastX, lastY, vectorToMouse);
            countToMakeTheSnakeLonger--;
            countToMakeTheSnakeLarger+= 5;
        }else if(countToMakeTheSnakeLarger > 15){
            makeTheSnakeLarger();
            countToMakeTheSnakeLarger--;
        } else if (start> 0) {
            makeTheSnakeLonger(lastX, lastY, vectorToMouse);
            start--;
        }

    }

    private void makeTheSnakeLarger() {
        this.getEntity().setScaleY(2);
        for (Entity bodyPart : userSnakeMovementComponents.getBodyParts()) {
            bodyPart.setScaleY(2);
        }
    }

    private void makeTheSnakeLonger(double x, double y, Point2D rotation) {
        var t = texture("snake.png")
                .subTexture(new Rectangle2D(0, 0, 7, 14))
                .multiplyColor(Color.GREEN);

        Entity snake = entityBuilder()
                .type(Type.SNAKEBODY)
                .at(x, y)
                .viewWithBBox(t)
                .collidable()
                .buildAndAttach();

        userSnakeMovementComponents.addABodyPart(snake);
        snake.rotateToVector(rotation);
    }

    public void aFoodAsBeenEaten(){
        countOfFoodEaten++;
        countToMakeTheSnakeLarger++;
    }

    public void setLastX(double lastX) {
        this.lastX = lastX;
    }

    public void setLastY(double lastY) {
        this.lastY = lastY;
    }

    public void setVectorToMouse(Point2D vectorToMouse) {
        this.vectorToMouse = vectorToMouse;
    }

    public void setUserSnakeMovementComponents(UserSnakeMovementComponents u){
        userSnakeMovementComponents = u;
    }

    public void death() {
    }
}
