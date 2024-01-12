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
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.UUID;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;


/**
 * Main Class of our own Slither.io game
 */
public class SlitherApp extends GameApplication {

    private Entity player1;
    private Entity player2;
    private Viewport viewport;
    private boolean isServer;
    private boolean online = true;
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


    /**
     *  Init the game with a dialogue box to enable the online mode if needed
     */
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
                        client.setOnConnected(con -> {
                            connection = con;

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


    /**
     * If the online mode is enabled and we're on the client side
     */
    private void onClient() {
        System.out.println("CLIENT");
        player1 = new Entity();

        viewport.setBounds(-getAppWidth(), -getAppHeight(), getAppWidth(), getAppHeight());
        viewport.setX(-getAppWidth());
        viewport.setY(-getAppHeight());
        viewport.setLazy(true);

        getService(MultiplayerService.class).addEntityReplicationReceiver(connection, getGameWorld());
        getService(MultiplayerService.class).addInputReplicationSender(connection, getInput());

        connection.addMessageHandler((conn, message) -> {
            if(message != null) {
                Double cameraX = message.get("cameraX");
                Double cameraY = message.get("cameraY");

                if (cameraX != null) {
                    viewport.setX(cameraX);
                }

                if (cameraY != null) {
                    viewport.setY(cameraY);
                }
            }
        });

    }


    /**
     *  If the online mode is enabled and we're on the client side
     */
    private void onServer() {

        //All the getServices() calls here are to replicate the entity to the client

        System.out.println("SERVER");
        var ai1 = spawn("ai");
        getService(MultiplayerService.class).spawn(connection, ai1, "ai");

        var ai2 = spawn("ai");
        getService(MultiplayerService.class).spawn(connection, ai2, "ai");

        var ai3 = spawn("ai");
        getService(MultiplayerService.class).spawn(connection, ai3, "ai");

        spawnWalls();


        player1 = spawn("snake", 200, 200);
        getService(MultiplayerService.class).spawn(connection, player1, "snake");

        player2 = spawn("snake", -200, -200);
        getService(MultiplayerService.class).spawn(connection, player2, "snake");

        player2.getComponent(SnakeComponent.class).setKeyboard();


        viewport.setBounds(-getAppWidth(), -getAppHeight(), getAppWidth(), getAppHeight());
        viewport.setX(0);
        viewport.setY(0);
        viewport.setLazy(true);

        //Spawn food every 0.3 seconds
        run(() -> {
            var food = spawn("food", FXGLMath.random(-getAppWidth() + 10, getAppWidth() - 10), FXGLMath.random(-getAppHeight() + 10, getAppHeight() - 10));
            getService(MultiplayerService.class).spawn(connection, food, "food");
        }, Duration.seconds(0.3));

        getService(MultiplayerService.class).addInputReplicationReceiver(connection, clientInput);

        initPhysics();
    }

    /**
     *  Offline init of game
     */
    private void offline() {
        player1 = spawn("snake");
        online = false;
        spawn("ai");
        spawn("ai");
        spawn("ai");
        spawnWalls();


        viewport.setBounds(-getAppWidth(), -getAppHeight(), getAppWidth(), getAppHeight());
        viewport.bindToEntity(player1, (double) getAppWidth() / 2, (double) getAppHeight() / 2);
        viewport.setLazy(true);

        //Spawn food every 0.3 seconds
        run(() -> spawn("food", FXGLMath.random(-getAppWidth() + 10, getAppWidth() - 10), FXGLMath.random(-getAppHeight() + 10, getAppHeight() - 10)), Duration.seconds(0.3));

        initPhysics();


        //Boost handling on click
        getInput().addAction(new UserAction("boost") {
            @Override
            protected void onActionBegin() {
                player1.getComponent(SnakeComponent.class).setBoost(true);
            }

            @Override
            protected void onActionEnd() {
                player1.getComponent(SnakeComponent.class).setBoost(false);
            }
        }, MouseButton.PRIMARY);

    }

    private void spawnWalls() {
        Entity wallWidth7 = spawn("wallWidth", -getAppWidth(), -getAppHeight());
        Entity wallWidth6 = spawn("wallWidth", getAppWidth() - 5, -getAppHeight());
        Entity wallWidth5 = spawn("wallWidth", -getAppWidth(), -((double) getAppHeight() / 2));
        Entity wallWidth4 = spawn("wallWidth", getAppWidth() - 5, -((double) getAppHeight() / 2));

        Entity wallWidth3 = spawn("wallWidth", -getAppWidth(), 0);
        Entity wallWidth2 = spawn("wallWidth", getAppWidth() - 5, 0);
        Entity wallWidth1 = spawn("wallWidth", -getAppWidth(), (double) getAppHeight() / 2);
        Entity wallWidth = spawn("wallWidth", getAppWidth() - 5, (double) getAppHeight() / 2);


        Entity wallHeight7 = spawn("wallHeight", -getAppWidth(), -getAppHeight());
        Entity wallHeight6 = spawn("wallHeight", -getAppWidth(), getAppHeight() - 5);
        Entity wallHeight5 = spawn("wallHeight", -(double) getAppWidth() / 2, -getAppHeight());
        Entity wallHeight4 = spawn("wallHeight", -(double) getAppWidth() / 2, getAppHeight() - 5);

        Entity wallHeight3 = spawn("wallHeight", 0, -getAppHeight());
        Entity wallHeight2 = spawn("wallHeight", 0, getAppHeight() - 5);
        Entity wallHeight1 = spawn("wallHeight", (double) getAppWidth() / 2, -getAppHeight());
        Entity wallHeight = spawn("wallHeight", (double) getAppWidth() / 2, getAppHeight() - 5);

        //replicate all the walls on the client side
        if(online){
            for (Entity entity : Arrays.asList(wallWidth, wallWidth1, wallWidth2, wallWidth3, wallWidth4, wallWidth5, wallWidth6, wallWidth7)) {
                getService(MultiplayerService.class).spawn(connection, entity, "wallWidth");
            }
            for (Entity entity : Arrays.asList(wallHeight, wallHeight1, wallHeight2, wallHeight3, wallHeight4, wallHeight5, wallHeight6, wallHeight7)) {
                getService(MultiplayerService.class).spawn(connection, entity, "wallHeight");
            }
        }
    }


    /**
     *  Init input for the second player
     */
    @Override
    protected void initInput() {

        clientInput = new Input();

        onKeyBuilder(clientInput, KeyCode.Z)
                .onAction(() -> player2.getComponent(SnakeComponent.class).up());
        onKeyBuilder(clientInput, KeyCode.S)
                .onAction(() -> player2.getComponent(SnakeComponent.class).down());
        onKeyBuilder(clientInput, KeyCode.Q)
                .onAction(() -> player2.getComponent(SnakeComponent.class).left());
        onKeyBuilder(clientInput, KeyCode.D)
                .onAction(() -> player2.getComponent(SnakeComponent.class).right());
    }


    /**
     *  On every frame we verify if the camera needs to change, it's only useful for the online mode
     */
    @Override
    protected void onUpdate(double tpf) {
        if (online) {
            if (isServer) {
                clientInput.update(tpf);
                if (player1 != null && player1.isActive()) {
                    handleCameraChange(player1);
                }
                if (player2 != null && player2.isActive()) {
                    handleCameraChange(player2);
                }
            } else {
                if (player2 != null && player2.isActive()) {
                    handleCameraChange(player2);
                }
            }
        }
    }


    /**
     *  All collisions init
     */
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
            if(!player1.isActive()){
                createDialogueBoxToReplay();
            }
        });

        onCollisionBegin(Type.SNAKEHEAD, Type.SNAKEBODY, (entitySnake, body) -> {
            SnakeComponent snakeComponent = entitySnake.hasComponent(AIMovementComponent.class) ?
                    entitySnake.getComponent(AIMovementComponent.class) : entitySnake.getComponent(SnakeComponent.class);
            UUID snakeId = snakeComponent.getId();
            UUID bodySnakeId = body.getProperties().getObject("Id");
            if (!snakeId.equals(bodySnakeId)) {
                int count = 0;
                for (Entity bodyParts : snakeComponent.getBodyPart()) {
                    if (count > 10) {
                        var food = spawn("food", bodyParts.getPosition());
                        if(online){
                            //getService(MultiplayerService.class).spawn(connection, food, "food");
                        }
                        count=0;
                    }
                    count++;
                }

                snakeComponent.death();

                // remove the collided food from the game
                entitySnake.removeFromWorld();
                if(!player1.isActive()){
                    createDialogueBoxToReplay();
                }
            }
        });
    }

    private void createDialogueBoxToReplay() {
        runOnce(() -> getDialogService().showConfirmationBox("PlayAgain ?", oui -> {
            player1 = spawn("snake");
            viewport.bindToEntity(player1, (double) getAppWidth() / 2, (double) getAppHeight() / 2);
        }), Duration.seconds(0.5));
    }


    /**
     *  Camera needs to change if the player is going of the screen. Only in the online mode
     */
    private void handleCameraChange(Entity player) {
        SnakeComponent component = player.getComponent(SnakeComponent.class);
        if (component.isCameraXHasBeenChanged()) {
            component.setCountForCameraX(component.getCountForCameraX() + 1);
            if (component.getCountForCameraX() > 400) {
                component.setCountForCameraX(0);
                component.setCameraXHasBeenChanged(false);
            }
        } else if (component.isCameraYHasBeenChanged()) {
            component.setCountForCameraY(component.getCountForCameraY() + 1);
            if (component.getCountForCameraY() > 400) {
                component.setCountForCameraY(0);
                component.setCameraYHasBeenChanged(false);
            }
        } else {
            handleOutsideTheCamera(player);
        }
    }


    /**
     *  Camera needs to change if the player is going of the screen. Only in the online mode
     */
    private void handleOutsideTheCamera(Entity player) {
        //the player is going outside the camera on the left or right
        if (!(player.getX() > getAppWidth() - 20 || player.getX() < -getAppWidth() + 20)) {
            if (player.getX() < 10 && player.getX() > -10) {
                if (player.getX() < 0) {
                    if (player == player2) {
                        var data = new Bundle("");
                        data.put("cameraX", 0.0);
                        connection.send(data);
                    } else {
                        viewport.setX(0);
                    }
                } else {
                    if (player == player2) {
                        var data = new Bundle("");
                        data.put("cameraX", (double) -getAppWidth());
                        connection.send(data);
                    } else {
                        viewport.setX(-getAppWidth());
                    }
                }
                player.getComponent(SnakeComponent.class).setCameraXHasBeenChanged(true);
            }
        }

        //the player is going outside the camera on the top or bottom
        if (!(player.getY() > getAppHeight() - 20 || player.getY() < -getAppHeight() + 20)) {
            if (player.getY() < 10 && player.getY() > -10) {
                if (player.getY() < 0) {
                    if (player == player2) {
                        var data = new Bundle("");
                        data.put("cameraY", 0.0);
                        connection.send(data);
                    } else {
                        viewport.setY(0);
                    }
                } else {
                    if (player == player2) {
                        var data = new Bundle("");
                        data.put("cameraY", (double) -getAppHeight());
                        connection.send(data);
                    } else {
                        viewport.setY(-getAppHeight());
                    }
                }
                player.getComponent(SnakeComponent.class).setCameraYHasBeenChanged(true);
            }
        }
    }

}


