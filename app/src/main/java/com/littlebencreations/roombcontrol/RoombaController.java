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

    private void setCurrentAction(String action) {
        this.currentAction = action;
    }

    public String getCurrentAction() {
        return currentAction;
    }

    /**
     * Needs to interface with the arduino to launch the roomba's
     * clean movement. It will get to you. Eventually.
     */
    public void deliver() {
        if (currentAction.equals(ACTION_DELIVER)) {
            // Don't do anything if we're already doing it
            return;
        }
        setCurrentAction(ACTION_DELIVER);
    }

    /** Really don't need this, but would be nice if we shifted to a listen/respond model */
    public void stay() {
        if (currentAction.equals(ACTION_STAY)) {
            // Don't do anything if we're already doing it
            return;
        }
        setCurrentAction(ACTION_STAY);

    }

    public void comeback() {
        if (currentAction.equals(ACTION_COMEBACK)) {
            // Don't do anything if we're already doing it
            return;
        }
        setCurrentAction(ACTION_COMEBACK);

    }
}
