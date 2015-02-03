//
// $Id$

package com.mpgsoft.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.threerings.presents.dobj.DSet;
import com.threerings.util.DirectionCodes;


/**
 * This class encapsulates the data corresponding to a horizontal or vertical
 * shift in the maze initiated by the user.  It is capable of applying the
 * shift or offset to {@link Piece}s on the board in it's area of influence. 
 * 
 * @author Jeffrey D. Hoffman
 */
final class ShiftContext {

    /**
     * Static constants defining the possible scopes of the shift - either a 
     * horizontal row or a vertical column.
     */
    static final int HORIZONTAL = 0;
    static final int VERTICAL   = 1;
    
    /**
     * Static constants defining the possible directions of the shift.  A
     * vertical shift upwards, for example, results in a negative offset 
     * to both the grid position and the pixel location.
     */
    static final int UP = -1;
    static final int DOWN = 1;
    static final int RIGHT  = 1;
    static final int LEFT  = -1;
    
    /**
     * This is the orientation for this particular shift -either 
     * <code>HORIZONTAL</code> or <code>VERTICAL</code>.
     */
    final int orientation;
    
    /**
     * This is the direction of the shift - one of <code>UP</code>,
     * <code>DOWN</code>, <code>LEFT</code> or <code>RIGHT</code>.
     */
    final int direction;
    
    /**
     * This represents the row or column index that is being shifted.
     */
    final int location;
        
    /**
     * Package-private constructor accepting the orientation, direction and location
     * for shifting pieces on the board.
     */
    ShiftContext(final int iOrientation, final int iDirection, final int iLocation) {
        orientation = iOrientation;
        direction = iDirection;
        location = iLocation;
    }
    
    /**
     * Package-private factory method which instantiates creates a new
     * <code>ShiftContext</code> assuming the x- and y- tile coordinates
     * provided correspond to a real, shiftable position on the board.
     * 
     * @return A <code>ShiftContext</code> corresponding to the position
     *         on the board from which the shift originates or null if
     *         the position is invalid.
     */
    static final ShiftContext createContext(final LabyrinthObject tGameObject, final int iTileX, final int iTileY) {
        
        // This will hold the <code>ShiftContext</code> to be returned, if any.
        ShiftContext tContext = null;

        // Verify that the tile position does not match the location of the 
        // previous shift.  The rules of Labyrinth dictate that a player can
        // not shift the walls back into the same location the piece came from.
        if (LabyrinthUtil.getLocation(iTileX, iTileY) != tGameObject.floating_wall_origin) {

            // Check to see if the origin is on the left or right-side of the board.
            // If so, create a horizontal shift.
            if (iTileX < 1 || iTileX > 5) {

                // Verify that the y-position corresponds to a moveable row.
                if (LabyrinthUtil.isMoveable(iTileY)) {

                    // Create a horizontal shift for the designated row.
                    tContext = new ShiftContext(
                            HORIZONTAL, 
                            (iTileX <= 1) ? RIGHT : LEFT,
                                    iTileY
                    );

                }

            } else if (iTileY < 1 || iTileY > 5) {

                // Verify that the x-position corresponds to a moveable column.
                if (LabyrinthUtil.isMoveable(iTileX)) {

                    // Create a vertical shift for the designated column.
                    tContext = new ShiftContext(
                            VERTICAL,
                            (iTileY <= 1) ? DOWN : UP,
                                    iTileX
                    );

                }

            }
            
        }

        // Return whatever was created, if anything.
        return tContext;
        
    }
    
    /**
     * Returns the <code>DirectionCode</code> corresponding to this shift.  This
     * is a convenience method that maps the internal offset directions (i.e. UP) 
     * to the corresponding direction. 
     */
    final int getDirectionCode() {
                
        // If this is a vertical shift, return either NORTH or SOUTH.
        if (orientation == VERTICAL)
            return direction == UP ? DirectionCodes.NORTH : DirectionCodes.SOUTH;
            
        // Otherwise, return a horizontal WEST or EAST direction.
        return direction == LEFT ? DirectionCodes.WEST : DirectionCodes.EAST;
    }
    
    /**
     * Convenience method which retrieves the list of pieces that are in the same
     * row or column based on the designated position.
     * @TODO [jhoffman 07/07/08]: I don't know enough about generics to know how
     * to properly declare this method.  I would like to be able to pass a DSet<Wall>
     * and have the compiler know it's going to get back a List<Wall> rather than
     * a List<Piece>.
     */
    final List<Piece> getPieces(final DSet<? extends Piece> tAllPieces) {
        
        // This will hold the list of <code>Pieces</code>.
        final List<Piece> tSomePieces = new ArrayList<Piece>(5);
        
        // Iterate through the <code>Piece</code>s in the set and find those that
        // match either the x- or y- coordinate.
        for (Piece tPiece : tAllPieces) {
            if ((orientation == HORIZONTAL && tPiece.y == location) ||
                    (orientation == VERTICAL && tPiece.x == location))
                tSomePieces.add(tPiece);
        }

        return tSomePieces;
    }
    
    /**
     * Package-private method which updates the offsets for the <code>Sprite</code>s that correspond
     * to the <code>Piece</code>s affected by this shift.
     */
    final void setOffset(final DSet<? extends Piece> tPieces, Map<Comparable, ? extends LabyrinthSprite> tSpritesByID, final int iMagnitude) {
     
        // Get the list of pieces that are affected by this shift.
        final List<Piece> tPiecesAffected = getPieces(tPieces);
        for (Piece tPiece : tPiecesAffected) {

            // Get the wall sprite that corresponds to this piece and 
            final LabyrinthSprite tSprite = tSpritesByID.get(tPiece.getKey());
            if (tSprite != null)
                setOffset(tSprite, iMagnitude);
            
        }

    }
    
    /**
     * This method shifts the provided <code>LabyrinthSprite</code> based on the 
     * magnitude provided.
     */
    final void setOffset(final LabyrinthSprite tSprite, int iMagnitude) {
        
        // Apply the direction to the magnitude.
        iMagnitude *= direction;
        
        // These will hold the x- or y- offsets that will be applied to the sprite.
        if (orientation == HORIZONTAL)
            tSprite.setOffset(iMagnitude, 0);
        else
            tSprite.setOffset(0, iMagnitude);
        
    }

    /**
     * Package-private method which physically shifts the <code>Piece</code> (along
     * grid boundaries) based on this context.  The <code>List</code> of pieces that
     * were affected by this shift are returned.
     */
    final List<Piece> shift(final DSet<? extends Piece> tPieces, final boolean bWrap) {
             
        // Get the list of pieces that are affected by this shift.
        final List<Piece> tPiecesAffected = getPieces(tPieces);
        for (Piece tPiece : tPiecesAffected) {

            // Apply the shift direction to either the x- or y-position of the
            // piece based on the orientation of this context.
            if (orientation == HORIZONTAL) {
                tPiece.x += direction;
            
                // If wrapping is desired, ensure that the piece does not leave
                // the playable area of the maze.
                if (bWrap) {
                    if (tPiece.x < 1)
                        tPiece.x = 5;
                    else if (tPiece.x > 5)
                        tPiece.x = 1;
                }
                
            } else {
                tPiece.y += direction;
                
                // If wrapping is desired, ensure that the piece does not leave
                // the playable area of the maze.
                if (bWrap) {
                    if (tPiece.y < 1)
                        tPiece.y = 5;
                    else if (tPiece.y > 5)
                        tPiece.y = 1;
                }
                
            }
                        
        }
        
        return tPiecesAffected;
    }
        
}
