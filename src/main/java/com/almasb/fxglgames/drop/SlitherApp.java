package com.almasb.fxglgames.drop;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Connection;
import com.almasb.fxglgames.drop.components.SnakeComponent;
import com.almasb.fxglgames.drop.components.ai.AIMovementComponent;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;


public class SlitherApp extends GameApplication {

    private Entity player1;
    private Entity player2;
    private  Viewport viewport;
    private boolean isServer;
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
            if(yes) {
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
            }else{
                getGameWorld().addEntityFactory(new SlitherFactory());
                viewport = getGameScene().getViewport();
                offline();
            }

    }), Duration.seconds(0.5));

    }

    private void onClient() {
        player1 = new Entity();

        viewport.setBounds(-getAppWidth(), -getAppHeight(), getAppWidth(), getAppHeight());
        //viewport.bindToEntity(player1, (double) getAppWidth() / 2, (double) getAppHeight() / 2);
        viewport.setLazy(true);

        getService(MultiplayerService.class).addEntityReplicationReceiver(connection, getGameWorld());
        getService(MultiplayerService.class).addInputReplicationSender(connection, getInput());
    }

    private void onServer() {
        var ai1 = spawn("ai");
        getService(MultiplayerService.class).spawn(connection, ai1, "ai");

        var ai2 = spawn("ai");
        getService(MultiplayerService.class).spawn(connection, ai2, "ai");

        var ai3 = spawn("ai");
        getService(MultiplayerService.class).spawn(connection, ai3, "ai");


        player1 = spawn("snake");
        getService(MultiplayerService.class).spawn(connection, player1, "snake");


        player2 = spawn("snake");
        getService(MultiplayerService.class).spawn(connection, player2, "snake");

        player2.getComponent(SnakeComponent.class).setInput(clientInput);


        viewport.setBounds(-getAppWidth(), -getAppHeight(), getAppWidth(), getAppHeight());
        //viewport.bindToEntity(player2, (double) getAppWidth() / 2, (double) getAppHeight() / 2);
        viewport.setLazy(true);

        run(() -> {
            var food = spawn("food");
            getService(MultiplayerService.class).spawn(connection, food, "food");
        }, Duration.seconds(0.3));

        getService(MultiplayerService.class).addInputReplicationReceiver(connection, clientInput);

        initPhysics();
    }


    private void offline(){
        player1 = spawn("snake");
        spawn("ai");
        spawn("ai");
        spawn("ai");

        viewport.setBounds(-getAppWidth(), -getAppHeight(), getAppWidth(), getAppHeight());
        viewport.bindToEntity(player1, (double) getAppWidth() / 2, (double) getAppHeight() / 2);
        viewport.setLazy(true);

        run(() -> {
            spawn("food");
        }, Duration.seconds(0.3));

        initPhysics();
    }

    @Override
    protected void initInput() {

        clientInput = new Input();

        onKeyBuilder(clientInput, KeyCode.W).onAction(() -> player1.getComponent(SnakeComponent.class).setMouse(clientInput.getMousePositionWorld()));

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
    protected void onUpdate(double tpf) {
        if (isServer) {
            clientInput.update(tpf);
        }
    }


    protected void initPhysics() {
        onCollisionBegin(Type.SNAKEHEAD, Type.FOOD, (snake, food) -> {
            if(snake.hasComponent(AIMovementComponent.class)){
                AIMovementComponent component = snake.getComponent(AIMovementComponent.class);
                component.aFoodAsBeenEaten();

            }else {
                SnakeComponent component = snake.getComponent(SnakeComponent.class);
                component.aFoodAsBeenEaten();
            }
            // remove the collided food from the game
            food.removeFromWorld();

            // play a sound effect located in /resources/assets/sounds/
            play("drop.wav");


        });



//    onCollisionBegin(Type.SNAKEHEAD, Type.SNAKEHEAD, (snake1, snake2) -> {
//            snake1.getComponent(SnakeComponent.class).death();
//            snake2.getComponent(SnakeComponent.class).death();
//
//            // remove the collided food from the game
//            snake2.removeFromWorld();
//            snake1.removeFromWorld();
//
//            // play a sound effect located in /resources/assets/sounds/
//            play("drop.wav");
//        });


        onCollisionBegin(Type.SNAKEHEAD, Type.SNAKEBODY, (snake, body) -> {
            if(snake.hasComponent(AIMovementComponent.class)){
                AIMovementComponent component = snake.getComponent(AIMovementComponent.class);
                if(!(component.getBodyParts().contains(body))) {
                    component.death();

                    // remove the collided food from the game
                    snake.removeFromWorld();

                    // play a sound effect located in /resources/assets/sounds/
                    play("drop.wav");
                }
            }else {
                SnakeComponent component = snake.getComponent(SnakeComponent.class);
                if(!(component.getBodyParts().contains(body))) {
                    component.death();

                    // remove the collided food from the game
                    snake.removeFromWorld();

                    // play a sound effect located in /resources/assets/sounds/
                    play("drop.wav");
                }
            }
            });
    }



}
