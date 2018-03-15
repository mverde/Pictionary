package com.martin.pictionary2.messages;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by desireelenart on 3/11/18.
 */

public class EndGameMessage extends Message {
    private Map<String, Integer> displayNamesToScores = new HashMap<String, Integer>();
    private String winner;

    public EndGameMessage(Map<String, Integer> displayNamesToScores, String winner) {
        this.displayNamesToScores = displayNamesToScores;
        this.winner = winner;
    }

    public Map<String, Integer> getScores() {
        return displayNamesToScores;
    }

    public String getWinner(){ return winner; }
}
