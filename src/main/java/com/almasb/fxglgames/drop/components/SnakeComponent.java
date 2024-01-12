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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getInput;

/**
 *  Class that handles the snake movement and size growing
 */
public class SnakeComponent extends Component {
    private final UUID id;
    protected int countOfFoodEaten = 0;
    private int countToMakeTheSnakeLonger = 0;
    private int countToMakeTheSnakeLarger = 0;
    private int countToMakeTheSnakeShorter = 0;
    private int currentRadius = 5;
    private int start = 50;
    private List<Entity> bodyParts = new ArrayList<>();
    private boolean boost = false;
    private boolean keyboard = false;
    private boolean up, left, right, down;
    private boolean cameraXHasBeenChanged;
    private boolean cameraYHasBeenChanged;
    private int countForCameraX = 0;
    private int countForCameraY = 0;



    /**
     *  The UUID id is used to compare different snake on collision
     */
    public SnakeComponent() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }


    /**
     *  On every frame, we move the snake towards the mouse or in direction of the key pressed,
     *  and move every body parts to were the one in front previously was
     */
    @Override
    public void onUpdate(double tpf) {

        double lastX = this.getEntity().getX();
        double lastY = this.getEntity().getY();

        if(keyboard) {
            if (up) {
                this.getEntity().translate(new Point2D(this.getEntity().getX(), this.getEntity().getY() - 50).subtract(
                        this.getEntity().getPosition()).normalize().multiply(0.8));
            } else if (left) {
                this.getEntity().translate(new Point2D(this.getEntity().getX() - 50, this.getEntity().getY()).subtract(
                        this.getEntity().getPosition()).normalize().multiply(0.8));
            } else if (right) {
                this.getEntity().translate(new Point2D(this.getEntity().getX() + 50, this.getEntity().getY()).subtract(
                        this.getEntity().getPosition()).normalize().multiply(0.8));
            } else if (down) {
                this.getEntity().translate(new Point2D(this.getEntity().getX(), this.getEntity().getY() + 50).subtract(
                        this.getEntity().getPosition()).normalize().multiply(0.8));
            }
        }else {
            if (boost) {
                //Remove body parts while boost
                countToMakeTheSnakeShorter++;
                if (countToMakeTheSnakeShorter > 20) {
                    removeLastBodyPart();
                    countToMakeTheSnakeShorter = 0;
                }
                //Going towards the mouse
                this.getEntity().translate(getInput().getMousePositionWorld().subtract(
                        this.getEntity().getPosition()).normalize().multiply(1.2));
            } else {
                //Going towards the mouse
                this.getEntity().translate(getInput().getMousePositionWorld().subtract(
                        this.getEntity().getPosition()).normalize().multiply(0.8));
            }
        }

        moveBodyParts(lastX, lastY);
    }

    private void removeLastBodyPart() {
        bodyParts.get(bodyParts.size() - 1).removeFromWorld();
        bodyParts.remove(bodyParts.size() - 1);
    }

    protected boolean shouldChangeSize() {
        return true;
    }


    /**
     *  Move every body parts to were the one in front previously was
     */
    protected void moveBodyParts(double lastX, double lastY) {
        for (Entity bodyPart : bodyParts) {
            double tempLastX = bodyPart.getX();
            double tempLastY = bodyPart.getY();

            bodyPart.setX(lastX);
            bodyPart.setY(lastY);


            lastX = tempLastX;
            lastY = tempLastY;
        }
        if (shouldChangeSize())
            handleSizeChange(lastX, lastY);

    }


    /**
     *  Check every counter to handle size change and init the snake size with the start int
     */
    protected void handleSizeChange(double pointInitial, double pointNew) {
        if (countOfFoodEaten > 5) {
            countOfFoodEaten = 0;
            makeTheSnakeLonger(pointInitial, pointNew);
            countToMakeTheSnakeLonger = 20;
            countToMakeTheSnakeLarger++;
        } else if (countToMakeTheSnakeLonger > 0) {
            makeTheSnakeLonger(pointInitial, pointNew);
            countToMakeTheSnakeLonger--;
        } else if (countToMakeTheSnakeLarger > 6) {
            currentRadius++;
            changeSnakeRadius();
            countToMakeTheSnakeLarger = 0;
        } else if (start > 0) {
            //use to init the snake size
            makeTheSnakeLonger(pointInitial, pointNew);
            start--;
        }
    }


    /**
     * Change the radius of every body parts of the snake to make him larger
     */
    private void changeSnakeRadius() {
        ViewComponent viewComponent = this.getEntity().getViewComponent();
        this.getEntity().getBoundingBoxComponent().clearHitBoxes();
        this.getEntity().getBoundingBoxComponent().addHitBox(new HitBox(BoundingShape.circle(currentRadius)));
        for (Node n : viewComponent.getChildren()) {
            if (n instanceof Circle) {
                ((Circle) n).setRadius(currentRadius);
            }
        }
        for (Entity bodyPart : bodyParts) {
            viewComponent = bodyPart.getViewComponent();
            for (Node n : viewComponent.getChildren()) {
                if (n instanceof Circle) {
                    ((Circle) n).setRadius(currentRadius);
                }
            }
        }
    }


    /**
     *  Adds body parts to the snake to make him longer
     */
    private void makeTheSnakeLonger(double x, double y) {
        Entity snake = entityBuilder()
                .type(Type.SNAKEBODY)
                .at(x, y)
                .bbox(new HitBox(BoundingShape.circle(currentRadius)))
                .view(new Circle(5, 5, currentRadius, Color.GREEN))
                .collidable()
                .with("Id", this.id)
                .buildAndAttach();

        bodyParts.add(snake);
    }

    public void aFoodAsBeenEaten() {
        countOfFoodEaten++;
    }

    public void death() {
        for (Entity bodyPart : bodyParts) {
            bodyPart.removeFromWorld();
        }
    }

    public void setBoost(boolean b) {
        boost = b;
        currentRadius = boost ? (int) (currentRadius / 1.25) : (int) (currentRadius * 1.25);
        changeSnakeRadius();
    }

    public List<Entity> getBodyPart() {
        return bodyParts;
    }

    public void up() {
        up = true;
        down = false;
        left = false;
        right = false;
    }

    public void down() {
        up = false;
        down = true;
        left = false;
        right = false;
    }

    public void left() {
        up = false;
        down = false;
        left = true;
        right = false;
    }

    public void right() {
        up = false;
        down = false;
        left = false;
        right = true;
    }

    public boolean isCameraXHasBeenChanged() {
        return cameraXHasBeenChanged;
    }

    public void setCameraXHasBeenChanged(boolean cameraXHasBeenChanged) {
        this.cameraXHasBeenChanged = cameraXHasBeenChanged;
    }

    public boolean isCameraYHasBeenChanged() {
        return cameraYHasBeenChanged;
    }

    public void setCameraYHasBeenChanged(boolean cameraYHasBeenChanged) {
        this.cameraYHasBeenChanged = cameraYHasBeenChanged;
    }

    public int getCountForCameraX() {
        return countForCameraX;
    }

    public void setCountForCameraX(int countForCameraX) {
        this.countForCameraX = countForCameraX;
    }

    public int getCountForCameraY() {
        return countForCameraY;
    }

    public void setCountForCameraY(int countForCameraY) {
        this.countForCameraY = countForCameraY;
    }

    public void setKeyboard() {
        keyboard = true;
    }
}
