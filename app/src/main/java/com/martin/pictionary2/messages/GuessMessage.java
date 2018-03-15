package com.martin.pictionary2.messages;

/**
 * Created by desireelenart on 3/5/18.
 */

/**
 * Message containing the information about a guess entered by a
 *   non-artist player.
 * guess - the word chosen by the guesser.
 * guesserId - the userID of the guesser.
 */

public class GuessMessage extends Message {
    private String guess;
    private String guesserId;
    private String displayName;

    public GuessMessage() {
    }

    public GuessMessage(String displayName, String guess, String guesserId) {
        this.guess = guess;
        this.guesserId = guesserId;
        this.displayName = displayName;
    }

    public String getGuess() {
        return guess;
    }

    public void setGuess(String guess) {
        this.guess = guess;
    }

    public String getGuesserId() {
        return guesserId;
    }

    public void setGuesserId(String guesserId) {
        this.guesserId = guesserId;
    }

    public String getDisplayName() {return displayName;}

    public void setDisplayName() { this.displayName = displayName; }
}
