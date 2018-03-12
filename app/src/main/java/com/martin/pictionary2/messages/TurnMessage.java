package com.martin.pictionary2.messages;

/**
 * Created by desireelenart on 3/5/18.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * Message containing the data relevant to one turn of a match.
 * int turnNumber - the absolute turn number in this match,
 * beginning at 0 and increasing.
 * String correctWord - the correct word choice.
 */

public class TurnMessage extends Message {
    private int turnNumber;
    private String guesserId;
    private String prevWord;
    private boolean newGame;
    private int maxScore;
    private Map<String, Integer> displayNamesToScores = new HashMap<String, Integer>();

    public TurnMessage(int turnNumber, String guesserId, String prevWord, boolean isNewGame, int maxScore, Map<String, Integer> displayNamesToScores) {
        this.turnNumber = turnNumber;
        this.guesserId = guesserId;
        this.prevWord = prevWord;
        this.newGame = isNewGame;
        this.maxScore = maxScore;
        this.displayNamesToScores = displayNamesToScores;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public String getGuesserId() {
        return guesserId;
    }

    public void setGuesserId(String correctWord) {
        this.guesserId = correctWord;
    }

    public boolean getNewGame() {
        return newGame;
    }

    public void setNewGame(boolean newGame) {
        this.newGame = newGame;
    }

    public String getPrevWord() {
        return this.prevWord;
    }

    public void setPrevWord(String prevWord) {
        this.prevWord = prevWord;
    }

    // Score Info

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

    public Map<String, Integer> getScores() {
        return displayNamesToScores;
    }

    public void setScores(String guesserName, int guesserScore) {
        this.displayNamesToScores.put(guesserName, guesserScore);
    }
}

