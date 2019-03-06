package com.tenins.andrey.tennis_score;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrey on 08.07.2018.
 */

public class SetSnapshot {

    private int setNumber;

    private int player1Score;

    private int player2Score;

    private int player1SetScore;

    private int player2SetScore;

    private int player1Brake;

    private int player2Brake;

    public int getPlayer1Brake() {
        return player1Brake;
    }

    public void setPlayer1Brake(int player1Brake) {
        this.player1Brake = player1Brake;
    }

    public int getPlayer2Brake() {
        return player2Brake;
    }

    public void setPlayer2Brake(int player2Brake) {
        this.player2Brake = player2Brake;
    }

    private Map<Integer, ApplicationConstants.History_Points> history = new HashMap<>();

    public int getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public int getPlayer1SetScore() {
        return player1SetScore;
    }

    public void setPlayer1SetScore(int player1SetScore) {
        this.player1SetScore = player1SetScore;
    }

    public int getPlayer2SetScore() {
        return player2SetScore;
    }

    public void setPlayer2SetScore(int player2SetScore) {
        this.player2SetScore = player2SetScore;
    }

    public Map<Integer, ApplicationConstants.History_Points> getHistory() {
        return history;
    }

    public void setHistory(Map<Integer, ApplicationConstants.History_Points> history) {
        this.history = history;
    }
}
