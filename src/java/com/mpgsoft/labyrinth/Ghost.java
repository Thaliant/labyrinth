//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Color;
import java.awt.Point;

/**
 * This class describes the player's piece on the board.  Players are
 * represented by a color and an image which they can move around the
 * board once they have aligned a pathway through the maze.
 * 
 * @author Jeffrey D. Hoffman
 */
public final class Ghost extends Piece {
    
    /**
     * Static constants defining the indicies of the colors for players.
     */
    static final int RED    = 0;
    static final int BLUE   = 1;
    static final int GREEN  = 2;
    static final int YELLOW = 3;
    
    /**
     * Static constant defining the <code>Color</code>s for player pieces and 
     * which correspond to player indicies at the table.
     */
    static final Color[] COLORS = new Color[] {
        new Color(0xff3333), new Color(0x003399), new Color(0x009900), new Color(0xffcc33)
    };
        
    /**
     * Empty constructor per the <code>DSet.Entry</code> contract.
     */
    public Ghost() { }
        
    /**
     * Convenience constructor accepting the unique identity and location of 
     * the the <code>Ghost</code>.
     */
    Ghost(final int iUniqueID, final Point tLocation) {
        unique_id = iUniqueID;
        x = tLocation.x;
        y = tLocation.y;
    }

}