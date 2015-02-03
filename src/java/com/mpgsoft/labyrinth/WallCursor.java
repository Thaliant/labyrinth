//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Image;


/**
 * This class repurposes the {@link WallSprite} to provide an 
 * interface mechanism allowing the user to control the floating 
 * wall piece that they must place at the beginning of their turn. 
 * 
 * @author Jeffrey D. Hoffman
 */
final class WallCursor extends WallSprite {
    
    /**
     * Static constant defining the uninitialized <code>Wall</code> that is used to
     * create all <code>WallCursor</code>s initially.  This will quickly be replaced
     * with a real wall when the game is setup.
     */
    private static final Wall INVISIBLE_WALL = new Wall(0, -2, -2, Wall.STRAIGHT, NORTH);
    
    /**
     * This is a reference to the <code>LabyrinthController</code> which dictates 
     * whether or not this cursor will be visible.
     */
    private final LabyrinthController mtGameController;
    
    /**
     * Package-private constructor accepting a reference to the <code>GameObject</code>
     * that will influence the cursor sprite's reaction to user input.
     */
    WallCursor(final LabyrinthController tGameController) {
        super(INVISIBLE_WALL);
        
        // Store a reference to the controller.
        mtGameController = tGameController;
        
    }
        
    /* (non-Javadoc)
     * @see com.mpgsoft.labyrinth.WallSprite#getImage()
     */
    protected final Image getImage() {
        
        // We'll always return null if it is not currently time for the player
        // to insert the wall segment into the maze.
        return (mtGameController.canWallBePlaced()) ? super.getImage() : null;
    }

    /**
     * Package-private method which rotates the floating wall tile 90-degrees clockwise.
     * @see Wall#rotate()
     */
    final void rotate() {
        
        // Rotate the wall piece.
        ((Wall) mtPiece).rotate();
        
        // Force a redraw since location is not affected by the rotation but a 
        // repaint is still desired.
        invalidate();
                
    }
    
    /**
     * Package-private method that updates the position of the internal <code>Wall</code>
     * based on the position of the mouse.
     */
    final void setPosition(final int iX, final int iY) {
        
        // Get a local handle on the wall.
        final Wall tWall = (Wall) mtPiece;
        
        // Update the position of the internal wall.
        tWall.x = iX;
        tWall.y = iY;
        
        // Trigger an update of the state.
        updatePiece(tWall);
        
    }
        
}
