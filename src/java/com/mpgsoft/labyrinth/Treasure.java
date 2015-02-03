//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Point;

/**
 * This class represents a treasure that players pickup during gameplay.  
 * There are a specific number of treasures in all games.  Each treasure
 * is either on the board or has an owner who has acquired it during 
 * gameplay.
 * 
 * @author Jeffrey D. Hoffman
 */
public final class Treasure extends Piece {
    
    /**
     * Static constant defining the maximum number of treasures.  This corresponds
     * to the number of sprites that will be created.
     */
    public static final int TREASURE_COUNT = 12;
    
    /**
     * This is the index of the player who has picked-up this treasure 
     * or -1 if the treasure is still available on the board.  By default
     * all treasures are available on the board.
     */
    public int owner = -1;

    /**
     * Empty constructor per the <code>DSet.Entry</code> contract.
     */
    public Treasure() { }
    
    /**
     * Convenience constructor accepting the unique identity and location of 
     * the the <code>Treasure</code>.
     */
    Treasure(final int iUniqueID, final Point tLocation) {
        unique_id = iUniqueID;
        x = tLocation.x;
        y = tLocation.y;
    }

    /**
     * Convenience method which returns true if this treasure has been 
     * picked up by any player.
     */
    final boolean isCollected() {
        return owner >= 0;
    }
    
}
