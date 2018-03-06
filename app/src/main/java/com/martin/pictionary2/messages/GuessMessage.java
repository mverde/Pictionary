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

    public GuessMessage() {
    }

    public GuessMessage(String guess, String guesserId) {
        this.guess = guess;
        this.guesserId = guesserId;
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
}
