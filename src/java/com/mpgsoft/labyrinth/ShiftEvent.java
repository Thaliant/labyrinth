//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Map;

import com.threerings.presents.dobj.DSet;

/**
 * This class pairs the pixel origin of a player-generated shift event
 * with the context detailing the orientation and direction of the shift.
 * It provides convenience methods for non-destructive, visible offsets
 * to sprites on the board allowing the player to preview their shift.
 * 
 * @author Jeffrey D. Hoffman
 */
final class ShiftEvent {

    /**
     * This is an origin <code>Point</code> from which this shift event 
     * was initiated.
     */
    final Point origin;
    
    /** 
     * This is the <code>ShiftContext</code> that details the shift itself.
     */
    final ShiftContext shift_context;

    /**
     * Shift events can only be created through their factory methods.
     */
    private ShiftEvent(final Point tOrigin, final ShiftContext tContext) {
        origin = tOrigin;
        shift_context = tContext;
    }

    /**
     * Package-private factory method which instantiates creates a new
     * <code>ShiftEvent</code> from the provided <code>MouseEvent</code>
     * assuming the mouse is positioned over a legally shiftable row or
     * column.
     * 
     * @return A <code>ShiftEvent</code> corresponding to the position
     *         of the mouse or null if the position is invalid.
     *         
     * @see ShiftContext#createContext(LabyrinthObject, int, int)
     */
    static final ShiftEvent createEvent(final LabyrinthObject tGameObject, final MouseEvent tMouseEvent) {

        // This will hold the event to be returned.
        ShiftEvent tEvent = null;
        
        // Get the x- and y-position of the mouse.
        final int iX = tMouseEvent.getX();
        final int iY = tMouseEvent.getY();
        
        // Convert the mouse coordinates to a tile position and try to 
        // create a <code>ShiftContext</code> from it.
        final ShiftContext tContext = ShiftContext.createContext(tGameObject, iX / LabyrinthSprite.SIZE, iY / LabyrinthSprite.SIZE); 
        if (tContext != null)
            tEvent = new ShiftEvent(new Point(iX, iY), tContext);

        // Return the event that was created, if any.
        return tEvent;                
    }
    
    /**
     * Provides package-private access to the magnitude (or displacement in pixels)
     * of the shift relative to the origin of the event and the provided
     * <code>MouseEvent</code>.
     */
    final int getMagnitude(final MouseEvent tEvent) {
        
        // This will hold the magnitude of the shift.
        int iMagnitude = 0;

        // Shift based on the 
        if (shift_context.orientation == ShiftContext.HORIZONTAL) {
            iMagnitude = tEvent.getX() - origin.x;
        
            // If the shift occurs from the right side of the board, shifting LEFT, the 
            // mouse event will result in a natural negative value.  Flip the magnitude
            // to achieve expected behavior.
            if (shift_context.direction == ShiftContext.LEFT)
                iMagnitude = 0 - iMagnitude;
            
        } else {
            iMagnitude = tEvent.getY() - origin.y;
            
            // If the shift occurs from the bottom side of the board, shifting UP, the 
            // mouse event will result in a natural negative value.  Flip the magnitude
            // to achieve expected behavior.
            if (shift_context.direction == ShiftContext.UP)
                iMagnitude = 0 - iMagnitude;
            
        }
        
        // Ensure that the magnitude is within the allowed values.  It can never shift a 
        // piece negatively from it's starting position and it can not move a piece 
        // beyond one tile.
        if (iMagnitude < 0)
            iMagnitude = 0;
        else if (iMagnitude > LabyrinthSprite.SIZE)
            iMagnitude = LabyrinthSprite.SIZE;

        return iMagnitude;
    }
    
    /**
     * Private convenience method which sets the offset for all of the <code>Sprite</code>s
     * that correspond to the <code>Piece</code>s in the designated list.
     */
    final void setOffset(final DSet<? extends Piece> tPieces, Map<Comparable, ? extends LabyrinthSprite> tSpritesByID, final MouseEvent tEvent) {
     
        // Proxy the call to the internal context after calculating the magnitude of the event.
        shift_context.setOffset(tPieces, tSpritesByID, getMagnitude(tEvent));

    }
    
    /**
     * This method shifts the provided <code>LabyrinthSprite</code> based on the 
     * magnitude provided.
     */
    final void setOffset(final LabyrinthSprite tSprite, final MouseEvent tEvent) {
        
        // Proxy the call to the internal context after calculating the magnitude of the event.
        shift_context.setOffset(tSprite, getMagnitude(tEvent));
        
    }
    
}
