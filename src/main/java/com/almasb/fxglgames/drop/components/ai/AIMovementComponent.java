package com.almasb.fxglgames.drop.components.ai;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.ViewComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxglgames.drop.Type;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.texture;

public class AIMovementComponent extends Component {
    private int countOfFoodEaten = 0;

    private int countToMakeTheSnakeLonger = 0;
    private int countToMakeTheSnakeLarger = 0;

    private int start = 50;

    private List<Entity> bodyParts = new ArrayList<>();

    private double lastX;
    private double lastY;

    private Entity closest;
    private int currentRadius = 5;



    @Override
    public void onUpdate(double tpf) {
        move();

        if(closest != null) {
            if (countOfFoodEaten > 5) {
                countOfFoodEaten = 0;
                makeTheSnakeLonger(lastX, lastY);
                countToMakeTheSnakeLonger = 20;
                countToMakeTheSnakeLarger++;

            } else if (countToMakeTheSnakeLonger > 0) {
                makeTheSnakeLonger(lastX, lastY);
                countToMakeTheSnakeLonger--;

            } else if (countToMakeTheSnakeLarger > 6) {
                currentRadius++;
                makeTheSnakeLarger();
                countToMakeTheSnakeLarger=0;

            } else if (start > 0) {
                makeTheSnakeLonger(lastX, lastY);
                start--;
            }
        }
    }

    private void move() {
        FXGL.getGameWorld().getEntitiesByType(Type.FOOD).forEach(this::isThisTheClosestOne);
        if(closest != null && closest.isActive()) {
            lastX = this.getEntity().getX();
            lastY = this.getEntity().getY();

            this.getEntity().translate(closest.getPosition().subtract(this.getEntity().getPosition()).normalize().multiply(0.8));

//            Point2D position = this.getEntity().getPosition();
//            Point2D vectorToClosest = closest.getPosition().subtract(position);
//
//            this.getEntity().rotateToVector(vectorToClosest);
//            lastVector = vectorToClosest;


            for (Entity bodyPart : bodyParts) {
                double tempLastX = bodyPart.getX();
                double tempLastY = bodyPart.getY();

                bodyPart.setX(lastX);
                bodyPart.setY(lastY);

                lastX = tempLastX;
                lastY = tempLastY;
            }
        }
    }



    private void isThisTheClosestOne(Entity food) {
        if(closest != null && closest.isActive()) {
            if (this.getEntity().getPosition().distance(closest.getPosition()) > this.getEntity().getPosition().distance(food.getPosition())) {
                closest = food;
            }
        }else {
            closest = food;
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
        var t = texture("snake.png")
                .subTexture(new Rectangle2D(0, 0, 7, 14))
                .multiplyColor(Color.YELLOW);

        Entity snake = entityBuilder()
                .type(Type.SNAKEBODY)
                .at(x, y)
                .bbox(new HitBox(BoundingShape.circle(currentRadius)))
                .view(new Circle(5, 5, currentRadius, Color.YELLOW))
                .collidable()
                .buildAndAttach();

        bodyParts.add(snake);
    }

    public void aFoodAsBeenEaten(){
        countOfFoodEaten++;
        closest= null;
    }

    public List<Entity> getBodyParts() {
        return bodyParts;
    }

    public void death() {
        for (Entity bodyPart : bodyParts) {
            bodyPart.removeFromWorld();
        }
    }
}
