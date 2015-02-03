//
// $Id$

package com.mpgsoft.labyrinth;

import com.threerings.presents.dobj.DSet;

/**
 * This class represents the base of all distributed set <code>Entry</code>
 * objects used in the game.  It is responsible for holding the piece's
 * unique key (relative to it's set) and the grid location on the board.
 * 
 * @autor Jeffrey D. Hoffman
 */
public abstract class Piece implements DSet.Entry {

    /**
     * This is the piece's unique identity or key.
     */
    public int unique_id;

    /**
     * This is the piece's x-location relative to the board's grid (squares, not pixels). 
     */
    public int x;

    /**
     * This is the piece's y-location relative to the board's grid (squares, not pixels).
     */
    public int y;

    /**
     * Empty constructor per the <code>DSet.Entry</code> contract.
     */
    public Piece() { }
    
    /* (non-Javadoc)
     * @see com.threerings.presents.dobj.DSet.Entry#getKey()
     */
    public final Comparable getKey() {
        return unique_id;
    }

}