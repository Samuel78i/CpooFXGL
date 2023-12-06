package com.almasb.fxglgames.drop;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;


public class DropApp extends GameApplication {

    /**
     * Types of entities in this game.
     */

    private List<Entity> snakes = new ArrayList<>();

    private int count = 0;
    private int bigger = 0;
    private int start = 0;
    private Point2D lastVector;

    private boolean boost;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initSettings(GameSettings settings) {
        // initialize common game / window settings.
        settings.setTitle("Snake.io");
        settings.setVersion("1.0");
        settings.setWidth(1500);
        settings.setHeight(800);
    }

    @Override
    protected void initGame() {
        start = 50;
        spawnSnake();
        spawnAI();

        // creates a timer that runs spawnFood() every second
        run(this::spawnFood, Duration.seconds(1));

    }


    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("boost") {
            @Override
            protected void onActionBegin() {
                boost = true;
            }

            @Override
            protected void onActionEnd() {
                boost = false;
            }
        }, MouseButton.PRIMARY);
    }

    @Override
    protected void initPhysics() {

        onCollisionBegin(Type.SNAKEHEAD, Type.FOOD, (snake, food) -> {

            count++;


            // remove the collided droplet from the game
            food.removeFromWorld();

            // play a sound effect located in /resources/assets/sounds/
            play("drop.wav");
        });
    }

    private void makeTheSnakeBigger(double x, double y, Point2D rotation) {

        var t = texture("snake.png")
                .subTexture(new Rectangle2D(0, 0, 7, 14))
                .multiplyColor(Color.GREEN);

        Entity snake = entityBuilder()
                .type(Type.SNAKEBODY)
                .at(x, y)
                .viewWithBBox(t)
                .collidable()
                .buildAndAttach();

        snakes.add(snake);
        snake.rotateToVector(rotation);
    }

    @Override
    protected void onUpdate(double tpf) {

        double lastX = snakes.get(0).getX();
        double lastY = snakes.get(0).getY();

        if(boost){
            snakes.get(0).translate(getInput().getMousePositionWorld().subtract(snakes.get(0).getPosition()).normalize().multiply(1.2));
        }else {
            snakes.get(0).translate(getInput().getMousePositionWorld().subtract(snakes.get(0).getPosition()).normalize().multiply(0.8));
        }
        Point2D position = snakes.get(0).getPosition();
        Point2D vectorToMouse = getInput().getMousePositionWorld().subtract(position);

        snakes.get(0).rotateToVector(vectorToMouse);



        for (int i = 1; i < snakes.size(); i++) {
            double tempLastX = snakes.get(i).getX();
            double tempLastY = snakes.get(i).getY();
            //snakes.get(i).translate(lastX, lastY);
            snakes.get(i).setX(lastX);
            snakes.get(i).setY(lastY);

            snakes.get(i).rotateToVector(lastVector);

            lastX = tempLastX;
            lastY = tempLastY;
        }

        if (count > 5) {
            count = 0;
            makeTheSnakeBigger(lastX, lastY, vectorToMouse);
            bigger = 20;
        }else if(bigger > 0){
            makeTheSnakeBigger(lastX, lastY, vectorToMouse);
            bigger--;
        } else if (start> 0) {
            makeTheSnakeBigger(lastX, lastY, vectorToMouse);
            start--;
        }

        lastVector = vectorToMouse;
    }


    private void spawnSnake() {
        var t = texture("snake.png")
                .subTexture(new Rectangle2D(0, 0, 7, 14))
                .multiplyColor(Color.RED);

        Entity snake = entityBuilder()
                .type(Type.SNAKEHEAD)
                .at(FXGLMath.random(0, getAppWidth()), FXGLMath.random(0, getAppHeight()))
                .viewWithBBox(t)
                .collidable()
                .buildAndAttach();

        snakes.add(snake);

        Point2D position = snake.getPosition();
        Point2D vectorToMouse = getInput().getMousePositionWorld().subtract(position);

        snake.rotateToVector(vectorToMouse);
        lastVector = vectorToMouse;
    }

    private void spawnFood() {
        entityBuilder()
                .type(Type.FOOD)
                .at(FXGLMath.random(0, getAppWidth() - 20), FXGLMath.random(0, getAppHeight() - 20))
                .viewWithBBox("droplet.png")
                .collidable()
                .buildAndAttach();
    }


    private void spawnAI() {

    }
}
