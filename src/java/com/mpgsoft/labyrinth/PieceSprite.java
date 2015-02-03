//
// $Id$

package com.mpgsoft.labyrinth;


/**
 * This class extends {@link LabyrinthSprite} and adds support for associating
 * a {@link Piece} with a <code>Sprite</code>.
 * 
 * @author Jeffrey D. Hoffman
 */
abstract class PieceSprite extends LabyrinthSprite {

    /**
     * This is the <code>Piece</code> this sprite represents.
     */
    protected Piece mtPiece;
    
    /**
     * Protected constructor which instantiates a new <code>Sprite</code>
     * for the designated <code>Piece</code>.
     */
    protected PieceSprite(final Piece tPiece) {
        
        // Update the piece's initial state.
        updatePiece(tPiece);
        
    }
    /**
     * Notifies the sprite that it should update it's corresponding 
     * {@link Piece} and aligns it to it's new location.
     */
    protected final void updatePiece(final Piece tPiece) {
        
        // Track the <code>Piece</code> managed by this class.
        mtPiece = tPiece;
        
        // Update the internal location.
        setLocation(tPiece.x * SIZE, tPiece.y * SIZE);
                
    }

}
