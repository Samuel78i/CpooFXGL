package com.almasb.fxglgames.drop.components;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.ViewComponent;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxglgames.drop.Type;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getInput;

public class SnakeComponent extends Component {
    private int countOfFoodEaten = 0;
    private int countToMakeTheSnakeLonger = 0;
    private int countToMakeTheSnakeLarger = 0;
    private int currentRadius = 5;
    private int start = 50;
    private List<Entity> bodyParts = new ArrayList<>();
    private boolean boost = false;
    private Input clientInput;
    private Point2D mouse;


    @Override
    public void onUpdate(double tpf) {

        double lastX = this.getEntity().getX();
        double lastY = this.getEntity().getY();

        if(clientInput == null) {
            if (boost) {
                this.getEntity().translate(getInput().getMousePositionWorld().subtract(
                        this.getEntity().getPosition()).normalize().multiply(1.2));
            } else {
                this.getEntity().translate(getInput().getMousePositionWorld().subtract(
                        this.getEntity().getPosition()).normalize().multiply(0.8));
            }
        }else {
            System.out.println("sal la zone");
            if(mouse != null) {
                if (boost) {
                    this.getEntity().translate(mouse.subtract(
                            this.getEntity().getPosition()).normalize().multiply(1.2));
                } else {
                    this.getEntity().translate(mouse.subtract(
                            this.getEntity().getPosition()).normalize().multiply(0.8));
                }
            }
        }


        for (Entity bodyPart : bodyParts) {
            double tempLastX = bodyPart.getX();
            double tempLastY = bodyPart.getY();

            bodyPart.setX(lastX);
            bodyPart.setY(lastY);


            lastX = tempLastX;
            lastY = tempLastY;
        }


        if (countOfFoodEaten > 5) {
            countOfFoodEaten = 0;
            makeTheSnakeLonger(lastX, lastY);
            countToMakeTheSnakeLonger = 20;
            countToMakeTheSnakeLarger++;
        }else if(countToMakeTheSnakeLonger > 0){
            makeTheSnakeLonger(lastX, lastY);
            countToMakeTheSnakeLonger--;
        }else if(countToMakeTheSnakeLarger > 6){
            currentRadius++;
            makeTheSnakeLarger();
            countToMakeTheSnakeLarger = 0;
        } else if (start> 0) {
            makeTheSnakeLonger(lastX, lastY);
            start--;
        }

    }

    private void makeTheSnakeLarger() {
        ViewComponent viewComponent = this.getEntity().getViewComponent();
        this.getEntity().getBoundingBoxComponent().clearHitBoxes();
        this.getEntity().getBoundingBoxComponent().addHitBox(new HitBox(BoundingShape.circle(currentRadius)));
        for(Node n :viewComponent.getChildren()){
            if(n instanceof Circle){
                ((Circle) n).setRadius(currentRadius);
            }
        }
        for (Entity bodyPart : bodyParts) {
            viewComponent = bodyPart.getViewComponent();
            for(Node n : viewComponent.getChildren()){
                if(n instanceof Circle){
                    ((Circle) n).setRadius(currentRadius);
                }
            }
        }
    }

    private void makeTheSnakeLonger(double x, double y) {
        Entity snake = entityBuilder()
                .type(Type.SNAKEBODY)
                .at(x, y)
                .bbox(new HitBox(BoundingShape.circle(currentRadius)))
                .view(new Circle(5, 5, currentRadius, Color.GREEN))
                .collidable()
                .buildAndAttach();


        bodyParts.add(snake);
    }

    public void aFoodAsBeenEaten(){
        countOfFoodEaten++;
    }

    public void death() {
        for (Entity bodyPart : bodyParts) {
            bodyPart.removeFromWorld();
        }
    }

    public List<Entity> getBodyParts() {
        return bodyParts;
    }

    public void setInput(Input clientInput) {
        this.clientInput = clientInput;
    }

    public void setMouse(Point2D mousePositionWorld) {

    }
}
