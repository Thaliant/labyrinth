//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import com.threerings.media.sprite.Sprite;
import com.threerings.util.DirectionCodes;

/**
 * This is the base class for all sprites in Labyrinth.  It provides a convenient way 
 * to paint an <code>Image</code> with a desired rotation at a specific location on 
 * the board.  All <code>Sprite</code>s should extend this base class.
 * 
 * @author Jeffrey D. Hoffman
 */
abstract class LabyrinthSprite extends Sprite {
    
    /**
     * Static constant defining the dimensions of all sprites on the Labyrinth board.
     */
    protected static final int SIZE = 64;
    
    /**
     * Static set of <code>AffineTransform</code>s used to rotate the sprites in 
     * 90-degree increments.  Labyrinth sprites are restricted to the primary
     * directions of the compass.  While it results in additional null entries, I 
     * found it less error-prone to use the Three Rings' defined {@link DirectionCodes}
     * rather than defining my own.
     * <p>
     * It is presumed that sprite images naturally point NORTH so images painted
     * with this orientation will not be transformed.  
     */
    protected static final AffineTransform[] ORIENTATION_TRANSFORMS = new AffineTransform[DirectionCodes.DIRECTION_COUNT];
    static {
        
        // The three non-NORTH directions will receive transforms.  The order is
        // important because the <code>AffineTransform</code>s below are built
        // in clockwise, 90-degree increments.
        final int[] aiPrimaryDirections = new int[] {
                DirectionCodes.EAST, DirectionCodes.SOUTH, DirectionCodes.WEST
        };
                
        // This is the <code>AffineTransform</code> that will be used to 
        // generate the child transforms.
        final AffineTransform tTransform = new AffineTransform();
        
        // Create the remaining transforms.
        for (int iOrientation : aiPrimaryDirections) {
            
            // Apply rotation in 90-degree increments.  This is how the Atlantis
            // example creates it's <code>AffineTransform</code>s.
            tTransform.translate(SIZE, 0);
            tTransform.rotate(Math.PI / 2);
            
            // Store the transform.
            ORIENTATION_TRANSFORMS[iOrientation] = (AffineTransform) tTransform.clone();
            
        }
        
    }
    
    /**
     * This class overrides <code>setLocation()</code> because during a particular UI
     * event, we want to allow drawing of sprites that do not span to grid boundaries.
     * This is accomplished by tracking the coordinate set by calls to <code>setLocation()</code>
     * and then using this last-known position when applying offsets.
     * @see LabyrinthSprite#setLocation(int, int)
     * @see LabyrinthSprite#setOffset(int, int)
     */
    private final Point mtLastSetPosition = new Point();
    
    /**
     * Protected constructor which instantiates a new <code>Sprite</code>
     * for the designated <code>Piece</code>.
     */
    protected LabyrinthSprite() {
        super(SIZE, SIZE);        
    }

    /**
     * The extending class must implement this method which should return the
     * <code>Image</code> to be rotated (if necessary) and painted to the screen
     * at the sprite's current location.
     * 
     * @return An <code>Image</code> or null if the sprite is not visible at 
     *         at this time.
     */
    protected abstract Image getImage();
    
    /* (non-Javadoc)
     * @see com.threerings.media.sprite.Sprite#paint(java.awt.Graphics2D)
     */
    public void paint(final Graphics2D tGraphics) {
                        
        // Retrieve the <code>Image</code> from the extending class.  A null value is
        // not considered an error - it indicates that the sprite should not paint 
        // at this time.
        final Image tImage = getImage();
        if (tImage == null)
            return;
        
        // Shift the origin of the graphics object to correspond to the x- and y-position
        // of the sprite being painted.
        tGraphics.translate(_bounds.x, _bounds.y);
                
        try {
            
            // Get the sprite's orientation.
            final int iOrientation = getOrientation();
        
            // For sprites with a non-NORTH orientation, paint with the corresponding transform.
            // I don't feel that additional validation of the orientation is necessary other than
            // ensuring it is within bounds.  Painting with a null <code>AffineTransform</code>
            // paints the image in it's natural orientation.
            if (iOrientation > DirectionCodes.NONE && iOrientation < DirectionCodes.DIRECTION_COUNT)
                tGraphics.drawImage(tImage, ORIENTATION_TRANSFORMS[iOrientation], null);
            
            // Otherwise, paint the default image at the temporary origin.
            else
                tGraphics.drawImage(tImage, 0, 0, null);
        
        } finally {
            
            // Translate back to the origin.
            tGraphics.translate(-_bounds.x, -_bounds.y);
            
        }
                    
    }
    
    /* (non-Javadoc)
     * @see com.threerings.media.sprite.Sprite#setLocation(int, int)
     */
    public void setLocation(final int iX, final int iY) {
        super.setLocation(iX, iY);
        
        // Store a copy of the last externally-set position.
        mtLastSetPosition.x = iX;
        mtLastSetPosition.y = iY;
        
    }

    /**
     * Package-private method allowing the sprite's intra-grid offset to be modified.
     * Calling this with <code>0, 0</code> returns the sprite to it's original position.
     */
    void setOffset(final int iDx, final int iDy) {
        
        // Directly call the <code>super</code> implementation of <code>setLocation()</code>
        // so that the sprite's bounds are updated to reflect the offset.  Use the last-known
        // position as the origin for the offset.
        super.setLocation(mtLastSetPosition.x + iDx, mtLastSetPosition.y + iDy);
                
    }
    
}
