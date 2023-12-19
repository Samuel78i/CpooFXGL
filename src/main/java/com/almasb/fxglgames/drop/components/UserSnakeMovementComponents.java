package com.almasb.fxglgames.drop.components;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

public class UserSnakeMovementComponents extends Component{
    private List<Entity> bodyParts = new ArrayList<>();

    private boolean boost = false;
    private Point2D lastVector;
    private SnakeComponent snakeComponent;

    @Override
    public void onUpdate(double tpf) {
        double lastX = this.getEntity().getX();
        double lastY = this.getEntity().getY();
        Point2D entityPosition = this.getEntity().getPosition();

        if(boost){
            this.getEntity().translate(getInput().getMousePositionWorld().subtract(
                    entityPosition).normalize().multiply(1.2));
        }else {
            this.getEntity().translate(getInput().getMousePositionWorld().subtract(
                    entityPosition).normalize().multiply(0.8));
        }
        Point2D vectorToMouse = getInput().getMousePositionWorld().subtract(entityPosition);

        this.getEntity().rotateToVector(vectorToMouse);
        lastVector = vectorToMouse;


        for (Entity bodyPart : bodyParts) {
            double tempLastX = bodyPart.getX();
            double tempLastY = bodyPart.getY();
            bodyPart.setX(lastX);
            bodyPart.setY(lastY);

            bodyPart.rotateToVector(lastVector);

            lastX = tempLastX;
            lastY = tempLastY;
        }
        snakeComponent.setLastX(lastX);
        snakeComponent.setLastY(lastY);
        snakeComponent.setVectorToMouse(lastVector);
    }

    public List<Entity> getBodyParts(){
        return bodyParts;
    }

    public void addABodyPart(Entity body){
        bodyParts.add(body);
    }

    public void setSnakeComponent(SnakeComponent snakeComponent){
        this.snakeComponent = snakeComponent;
    }
}
