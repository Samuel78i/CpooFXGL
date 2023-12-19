package com.almasb.fxglgames.drop;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxglgames.drop.components.SnakeComponent;
import com.almasb.fxglgames.drop.components.UserSnakeMovementComponents;
import com.almasb.fxglgames.drop.components.ai.AIMovementComponent;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;


public class SlitherApp extends GameApplication {
    private List<Entity> snakes = new ArrayList<>();
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
        spawnSnake();
        spawnAI();

        // creates a timer that runs spawnFood() every second
        run(this::spawnFood, Duration.seconds(1));
    }


    @Override
    protected void initInput() {

/*        getInput().addAction(new UserAction("boost") {
            @Override
            protected void onActionBegin() {
                boost = true;
            }

            @Override
            protected void onActionEnd() {
                boost = false;
            }
        }, MouseButton.PRIMARY);*/
    }

    @Override
    protected void initPhysics() {
        onCollisionBegin(Type.SNAKEHEAD, Type.FOOD, (snake, food) -> {
            snake.getComponent(SnakeComponent.class).aFoodAsBeenEaten();

            // remove the collided food from the game
            food.removeFromWorld();

            // play a sound effect located in /resources/assets/sounds/
            play("drop.wav");
        });

    onCollisionBegin(Type.SNAKEHEAD, Type.SNAKEHEAD, (snake1, snake2) -> {
            snake1.getComponent(SnakeComponent.class).death();
            snake2.getComponent(SnakeComponent.class).death();

            // remove the collided food from the game
            snake2.removeFromWorld();
            snake1.removeFromWorld();

            // play a sound effect located in /resources/assets/sounds/
            play("drop.wav");
        });

        onCollisionBegin(Type.SNAKEHEAD, Type.SNAKEBODY, (snake, body) -> {
            if(!snake.getComponent(UserSnakeMovementComponents.class).getBodyParts().contains(body)) {
                snake.getComponent(SnakeComponent.class).death();

                // remove the collided food from the game
                snake.removeFromWorld();

                // play a sound effect located in /resources/assets/sounds/
                play("drop.wav");
            }});
    }


    private void spawnSnake() {
        var t = texture("snake.png")
                .subTexture(new Rectangle2D(0, 0, 7, 14))
                .multiplyColor(Color.RED);

        SnakeComponent snakeComponent = new SnakeComponent();
        UserSnakeMovementComponents userSnakeMovementComponents = new UserSnakeMovementComponents();
        snakeComponent.setUserSnakeMovementComponents(userSnakeMovementComponents);
        userSnakeMovementComponents.setSnakeComponent(snakeComponent);

        Entity snake = entityBuilder()
                .type(Type.SNAKEHEAD)
                .at(FXGLMath.random(0, getAppWidth()), FXGLMath.random(0, getAppHeight()))
                .viewWithBBox(t)
                .collidable()
                .with(snakeComponent)
                .with(userSnakeMovementComponents)
                .buildAndAttach();

        snakes.add(snake);
        Point2D position = snake.getPosition();
        Point2D vectorToMouse = getInput().getMousePositionWorld().subtract(position);

        snake.rotateToVector(vectorToMouse);
        snake.getComponent(UserSnakeMovementComponents.class).setLastVector(vectorToMouse);
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
        var t = texture("snake.png")
                .subTexture(new Rectangle2D(0, 0, 7, 14))
                .multiplyColor(Color.BLUE);

        Entity snake = entityBuilder()
                .type(Type.SNAKEHEAD)
                .at(FXGLMath.random(0, getAppWidth()), FXGLMath.random(0, getAppHeight()))
                .viewWithBBox(t)
                .collidable()
                .with(new AIMovementComponent())
                .buildAndAttach();

        snakes.add(snake);
    }
}
