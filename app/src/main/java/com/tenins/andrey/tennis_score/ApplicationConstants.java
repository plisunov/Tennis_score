package com.tenins.andrey.tennis_score;

import org.apache.commons.lang3.StringUtils;


public class ApplicationConstants {


    public static final String baseURl = "http://ec2-3-120-137-87.eu-central-1.compute.amazonaws.com:8080/ttennis";

    public static final String logTag = "TTennis";

    public static final String user = "AndroidClient";

    public static final String password = "87androidapplication78";

    public enum History_Points {
        PLAYER1INKSCORE, PLAYER1YELLOW, PLAYER2INKSCORE, PLAYER1RED, PLAYER2RED, PLAYER1REDAFTERYELLOW, PLAYER2REDAFTERYELLOW, PLAYER2YELLOW
    }

    public enum SCORE_CHANGE_TYPE {ADD_POINT, REVERSE_POINT, YELLOW_CARD, RED_CARD}


    public enum GAME_ACTION {
        GAME_START,
        GAME_REVERSED,
        REVERSE_BRAKER,
        SET_ENDED,
        MATCH_ENDED,
        MATCH_PAUSED,
        PLAYER1_GET_TIMEOUT,
        PLAYER2_GET_TIMEOUT,
        PLAYER1_GET_POINT,
        PLAYER2_GET_POINT,
        PLAYER1_GET_YELLOW_CARD,
        PLAYER2_GET_YELLOW_CARD,
        PLAYER1_GET_RED_CARD,
        PLAYER2_GET_RED_CARD;
    }

    public static GAME_ACTION getGameActionByName(String actionName) {
        for (GAME_ACTION action : GAME_ACTION.values()) {
            if (StringUtils.equals(actionName, action.name())) {
                return action;
            }
        }
        return null;
    }

}
