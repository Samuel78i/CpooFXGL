package com.almasb.fxglgames.drop;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.multiplayer.NetworkComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxglgames.drop.components.SnakeComponent;
import com.almasb.fxglgames.drop.components.ai.AIMovementComponent;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import static com.almasb.fxgl.dsl.FXGL.*;

public class SlitherFactory implements EntityFactory {

    @Spawns("snake")
    public Entity spawnSnake(SpawnData data) {
        var t = texture("snake.png")
                .subTexture(new Rectangle2D(0, 0, 7, 14))
                .multiplyColor(Color.RED);


        return entityBuilder()
                .type(Type.SNAKEHEAD)
                .at(FXGLMath.random(0, getAppWidth()), FXGLMath.random(0, getAppHeight()))
                .bbox(new HitBox(BoundingShape.circle(5)))
                .view(new Circle(5, 5, 5, Color.GREEN))
                .collidable()
                .with(new SnakeComponent())
                //.with(userSnakeMovementComponents)
                .with(new NetworkComponent())
                .buildAndAttach();


    }

    @Spawns("food")
    public Entity spawnFood(SpawnData data) {
        return entityBuilder()
                .type(Type.FOOD)
                .at(FXGLMath.random(-getAppWidth()+20, getAppWidth() - 20), FXGLMath.random(-getAppHeight()+20, getAppHeight() - 20))
                .bbox(new HitBox(BoundingShape.circle(6)))
                .view(new Circle(6, 6, 6, Color.color(Math.random(), Math.random(), Math.random())))
                .collidable()
                .with(new NetworkComponent())
                .buildAndAttach();

    }


    @Spawns("ai")
    public Entity spawnAI(SpawnData data) {
        var t = texture("snake.png")
                .subTexture(new Rectangle2D(0, 0, 7, 14))
                .multiplyColor(Color.YELLOW);


        return entityBuilder()
                .type(Type.SNAKEHEAD)
                .at(FXGLMath.random(0, getAppWidth()), FXGLMath.random(0, getAppHeight()))
                .bbox(new HitBox(BoundingShape.circle(5)))
                .view(new Circle(5, 5, 5, Color.YELLOW))
                .collidable()
                .with(new AIMovementComponent())
                .with(new NetworkComponent())
                .buildAndAttach();
    }
}
