package com.littlebencreations.roombcontrol;

/**
 * Created by austin on 11/22/15.
 *
 * Meant to be a simple controller for the Roomba
 */
public class RoombaController {
    private static final String LOG_TAG = RoombaController.class.getSimpleName();

    public static final String ACTION_DELIVER = "deliver";
    public static final String ACTION_STAY = "stay";
    public static final String ACTION_COMEBACK = "comeback";

    private String currentAction;

    public RoombaController() {
        currentAction = ACTION_STAY;
    }

    public void setCurrentAction(String action) {
        this.currentAction = action;
    }

    public String getCurrentAction() {
        return currentAction;
    }

    public void deliver() {
        setCurrentAction(ACTION_DELIVER);
    }

    public void stay() {
        setCurrentAction(ACTION_STAY);

    }

    public void comeback() {
        setCurrentAction(ACTION_COMEBACK);

    }
}
