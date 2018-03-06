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
    private String correctWord;

    public TurnMessage() {
    }

    public TurnMessage(int turnNumber, String correctWord) {
        this.turnNumber = turnNumber;
        this.correctWord = correctWord;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public String getCorrectWord() {
        return correctWord;
    }

    public void setCorrectWord(String correctWord) {
        this.correctWord = correctWord;
    }
}

