package com.almasb.fxglgames.drop.components;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxglgames.drop.Type;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

public class SnakeComponent extends Component {

    List<Entity> bodyParts = new ArrayList<>();
    int countOfFoodEaten = 0;
    
    int countToMakeTheSnakeLonger = 0;
    int countToMakeTheSnakeLarger = 0;
    boolean boost = false;

    int start = 50;

    Point2D lastVector;


    @Override
    public void onUpdate(double tpf) {
        double lastX = this.getEntity().getX();
        double lastY = this.getEntity().getY();

        if(boost){
            this.getEntity().translate(getInput().getMousePositionWorld().subtract(
                    this.getEntity().getPosition()).normalize().multiply(1.2));
        }else {
            this.getEntity().translate(getInput().getMousePositionWorld().subtract(
                    this.getEntity().getPosition()).normalize().multiply(0.8));
        }
        Point2D position = this.getEntity().getPosition();
        Point2D vectorToMouse = getInput().getMousePositionWorld().subtract(position);

        this.getEntity().rotateToVector(vectorToMouse);
        lastVector = vectorToMouse;


        for (Entity bodyPart : bodyParts) {
            double tempLastX = bodyPart.getX();
            double tempLastY = bodyPart.getY();
            //snakes.get(i).translate(lastX, lastY);
            bodyPart.setX(lastX);
            bodyPart.setY(lastY);

            //Point2D temp = bodyPart.getRotation();

            bodyPart.rotateToVector(lastVector);

            lastX = tempLastX;
            lastY = tempLastY;
        }

        if (countOfFoodEaten > 5) {
            countOfFoodEaten = 0;
            makeTheSnakeLonger(lastX, lastY, vectorToMouse);
            countToMakeTheSnakeLonger = 20;
        }else if(countToMakeTheSnakeLonger > 0){
            makeTheSnakeLonger(lastX, lastY, vectorToMouse);
            countToMakeTheSnakeLonger--;
            countToMakeTheSnakeLarger+= 5;
        }else if(countToMakeTheSnakeLarger > 15){
            makeTheSnakeLarger(lastX, lastY, vectorToMouse);
            countToMakeTheSnakeLarger--;
        } else if (start> 0) {
            makeTheSnakeLonger(lastX, lastY, vectorToMouse);
            start--;
        }

    }

    private void makeTheSnakeLarger(double lastX, double lastY, Point2D vectorToMouse) {
        this.getEntity().setScaleY(2);
        for (Entity bodyPart : bodyParts) {
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

        bodyParts.add(snake);
        snake.rotateToVector(rotation);
    }

    public void aFoodAsBeenEaten(){
        countOfFoodEaten++;
        countToMakeTheSnakeLarger++;
    }

    public void setLastVector(Point2D lastVector) {
        this.lastVector = lastVector;
    }
}
