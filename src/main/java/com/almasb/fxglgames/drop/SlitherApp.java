package com.almasb.fxglgames.drop;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Connection;
import com.almasb.fxglgames.drop.components.SnakeComponent;
import com.almasb.fxglgames.drop.components.ai.AIMovementComponent;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

import java.util.UUID;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;


public class SlitherApp extends GameApplication {

    private Entity player1;
    private Entity player2;
    private Viewport viewport;
    private boolean isServer;
    private boolean online;
    private Connection<Bundle> connection;

    private Input clientInput;


    public static void main(String[] args) {
        launch(args);
    }


    @Override
    protected void initSettings(GameSettings settings) {
        // initialize common game / window settings.
        settings.setTitle("Snake.io");
        settings.setVersion("1.0");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.addEngineService(MultiplayerService.class);

    }

    @Override
    protected void initGame() {
        runOnce(() -> getDialogService().showConfirmationBox("Online", yes -> {
            if (yes) {
                runOnce(() -> getDialogService().showConfirmationBox("Are you the host?", oui -> {
                    isServer = oui;

                    getGameWorld().addEntityFactory(new SlitherFactory());
                    viewport = getGameScene().getViewport();

                    if (oui) {
                        var server = getNetService().newTCPServer(444);
                        server.setOnConnected(conn -> {
                            connection = conn;

                            getExecutor().startAsyncFX(this::onServer);
                        });

                        server.startAsync();

                    } else {
                        var client = getNetService().newTCPClient("localhost", 444);
                        client.setOnConnected(conn -> {
                            connection = conn;

                            getExecutor().startAsyncFX(this::onClient);
                        });

                        client.connectAsync();
                    }
                }), Duration.seconds(0.5));
            } else {
                online = false;
                getGameWorld().addEntityFactory(new SlitherFactory());
                viewport = getGameScene().getViewport();
                offline();
            }

        }), Duration.seconds(0.5));

    }

    private void onClient() {
        System.out.println("CLIENT");
        online = true;
        player1 = new Entity();

        viewport.setBounds(-getAppWidth(), -getAppHeight(), getAppWidth(), getAppHeight());
        viewport.setX(player1.getX());
        viewport.setY(player1.getY());
        viewport.setLazy(true);

        getService(MultiplayerService.class).addEntityReplicationReceiver(connection, getGameWorld());
        getService(MultiplayerService.class).addInputReplicationSender(connection, getInput());
    }

    private void onServer() {
        System.out.println("SERVER");
        online = true;
        var ai1 = spawn("ai");
        getService(MultiplayerService.class).spawn(connection, ai1, "ai");

        var ai2 = spawn("ai");
        getService(MultiplayerService.class).spawn(connection, ai2, "ai");

        var ai3 = spawn("ai");
        getService(MultiplayerService.class).spawn(connection, ai3, "ai");


        player1 = spawn("snake");
        getService(MultiplayerService.class).spawn(connection, player1, "snake");

        player1.getComponent(SnakeComponent.class).setInput(clientInput);

        player2 = spawn("snake");
        getService(MultiplayerService.class).spawn(connection, player2, "snake");


        viewport.setBounds(-getAppWidth(), -getAppHeight(), getAppWidth(), getAppHeight());
        viewport.setX(player2.getX());
        viewport.setY(player2.getY());
        viewport.setLazy(true);

        run(() -> {
            var food = spawn("food", FXGLMath.random(-getAppWidth() + 10, getAppWidth() - 10), FXGLMath.random(-getAppHeight() + 10, getAppHeight() - 10));
            getService(MultiplayerService.class).spawn(connection, food, "food");
        }, Duration.seconds(0.3));

        getService(MultiplayerService.class).addInputReplicationReceiver(connection, clientInput);

        initPhysics();
        initInput();
    }


    private void offline() {
        player1 = spawn("snake");
//        spawn("ai");
//        spawn("ai");
//        spawn("ai");

        viewport.setBounds(-getAppWidth(), -getAppHeight(), getAppWidth(), getAppHeight());
        viewport.bindToEntity(player1, (double) getAppWidth() / 2, (double) getAppHeight() / 2);
        viewport.setLazy(true);

        run(() -> {
            spawn("food", FXGLMath.random(-getAppWidth() + 10, getAppWidth() - 10), FXGLMath.random(-getAppHeight() + 10, getAppHeight() - 10));
        }, Duration.seconds(0.3));

        initPhysics();
        initInput();
    }

    protected void initInput() {

        clientInput = new Input();

        //onKeyBuilder(clientInput, KeyCode.W).onAction(() -> player1.getComponent(SnakeComponent.class).setMouse(clientInput.getMousePositionWorld()));


//        if (!online) {
//            getInput().addAction(new UserAction("boost") {
//                @Override
//                protected void onActionBegin() {
//                    player1.getComponent(SnakeComponent.class).setBoost(true);
//                }
//
//                @Override
//                protected void onActionEnd() {
//                    player1.getComponent(SnakeComponent.class).setBoost(false);
//                }
//            }, MouseButton.PRIMARY);
//        }
    }


    @Override
    protected void onUpdate(double tpf) {
        if (online) {
            if (isServer) {
                clientInput.update(tpf);
//                if (player2 != null) {
//                    viewport.setX(player2.getX());
//                    viewport.setY(player2.getY());
//                }
            } else {
//                if (player1 != null) {
//                    viewport.setX(player1.getX());
//                    viewport.setY(player1.getY());
//                }
//
            }
        }
    }


    protected void initPhysics() {

        onCollisionBegin(Type.SNAKEHEAD, Type.FOOD, (entitySnake, food) -> {

            SnakeComponent snakeComponent = entitySnake.hasComponent(AIMovementComponent.class) ?
                    entitySnake.getComponent(AIMovementComponent.class) : entitySnake.getComponent(SnakeComponent.class);

            snakeComponent.aFoodAsBeenEaten();

            // remove the collided food from the game
            food.removeFromWorld();

            // play a sound effect located in /resources/assets/sounds/
            play("drop.wav");


        });

        onCollisionBegin(Type.SNAKEHEAD, Type.SNAKEHEAD, (snake1, snake2) -> {
            SnakeComponent snakeComponent1 = snake1.hasComponent(AIMovementComponent.class) ?
                    snake1.getComponent(AIMovementComponent.class) : snake1.getComponent(SnakeComponent.class);


            SnakeComponent snakeComponent2 = snake2.hasComponent(AIMovementComponent.class) ?
                    snake2.getComponent(AIMovementComponent.class) : snake2.getComponent(SnakeComponent.class);

            snakeComponent1.death();
            snakeComponent2.death();

            // remove the collided food from the game
            snake2.removeFromWorld();
            snake1.removeFromWorld();
        });

        onCollisionBegin(Type.SNAKEHEAD, Type.SNAKEBODY, (entitySnake, body) -> {
            SnakeComponent snakeComponent = entitySnake.hasComponent(AIMovementComponent.class) ?
                    entitySnake.getComponent(AIMovementComponent.class) : entitySnake.getComponent(SnakeComponent.class);
            UUID snakeId = snakeComponent.getId();
            UUID bodySnakeId = body.getProperties().getObject("Id");
            if (!snakeId.equals(bodySnakeId)) {
                int count = 0;
                for (Entity bodyParts : snakeComponent.getBodyPart()) {
                    if (count > 3) {
                        spawn("food", bodyParts.getPosition());
                    }
                    count++;
                }

                snakeComponent.death();

                // remove the collided food from the game
                entitySnake.removeFromWorld();
            }
        });
    }
}
