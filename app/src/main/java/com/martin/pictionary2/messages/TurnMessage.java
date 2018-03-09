package com.martin.pictionary2.messages;

/**
 * Created by desireelenart on 3/5/18.
 */

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

    public TurnMessage() {
    }

    public TurnMessage(int turnNumber, String guesserId, String prevWord, boolean isNewGame) {
        this.turnNumber = turnNumber;
        this.guesserId = guesserId;
        this.prevWord = prevWord;
        this.newGame = isNewGame;
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
}

