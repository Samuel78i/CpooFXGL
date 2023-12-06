package com.almasb.fxglgames.drop.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.getInput;

public class SnakeComponent extends Component {

//    @Override
//    public void onUpdate(double tpf) {
//        double lastX = snakes.get(0).getX();
//        double lastY = snakes.get(0).getY();
//
//        if(boost){
//            snakes.get(0).translate(getInput().getMousePositionWorld().subtract(snakes.get(0).getPosition()).normalize().multiply(1.2));
//        }else {
//            snakes.get(0).translate(getInput().getMousePositionWorld().subtract(snakes.get(0).getPosition()).normalize().multiply(0.8));
//        }
//        Point2D position = snakes.get(0).getPosition();
//        Point2D vectorToMouse = getInput().getMousePositionWorld().subtract(position);
//
//        snakes.get(0).rotateToVector(vectorToMouse);
//
//
//
//        for (int i = 1; i < snakes.size(); i++) {
//            double tempLastX = snakes.get(i).getX();
//            double tempLastY = snakes.get(i).getY();
//            //snakes.get(i).translate(lastX, lastY);
//            snakes.get(i).setX(lastX);
//            snakes.get(i).setY(lastY);
//
//            snakes.get(i).rotateToVector(lastVector);
//
//            lastX = tempLastX;
//            lastY = tempLastY;
//        }
//
//        if (count > 5) {
//            count = 0;
//            makeTheSnakeBigger(lastX, lastY, vectorToMouse);
//            bigger = 20;
//        }else if(bigger > 0){
//            makeTheSnakeBigger(lastX, lastY, vectorToMouse);
//            bigger--;
//        } else if (start> 0) {
//            makeTheSnakeBigger(lastX, lastY, vectorToMouse);
//            start--;
//        }
//
//        lastVector = vectorToMouse;
//    }
}
