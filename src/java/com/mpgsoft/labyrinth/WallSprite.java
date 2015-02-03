//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Graphics2D;
import java.awt.Image;

import com.samskivert.Log;
import com.threerings.media.tile.UniformTileSet;

/**
 * This class is a {@link PieceSprite} capable of displaying the 
 * image corresponding to the shape and orientation of the {@lilnk Wall}
 * it wraps.
 * 
 * @author Jeffrey D. Hoffman
 */
class WallSprite extends PieceSprite {

    /**
     * This is a static reference to the <code>UniformTileSet</code> which holds the
     * tiles for the walls.  It is package-private because it is initialized from 
     * the {@link LabyrinthPanel} class.
     */
    static UniformTileSet WALL_TILES;
        
    /**
     * Package-private constructor for the class accepting the <code>Wall</code>
     * to be rendered as a <code>Sprite</code>.
     */
    WallSprite(final Wall tWall) {
        super(tWall);
    }

    /* (non-Javadoc)
     * @see com.mpgsoft.labyrinth.LabyrinthSprite#getImage()
     */
    protected Image getImage() {

        // Get a local handle on the <code>Wall</code> being painted.
        final Wall tWall = (Wall) mtPiece;

        // Get the <code>Tile</code> corresponding to this shape.  If properly initialized,
        // this should never return null - but if it does, log a warning.
        final Image tTile = WALL_TILES.getRawTileImage(tWall.shape);
        if (tTile == null)
            Log.warning("Tile for wall shape " + tWall.shape + " is missing.");

        return tTile;
    }

    /* (non-Javadoc)
     * @see com.threerings.media.sprite.Sprite#getOrientation()
     */
    public final int getOrientation() {
        return ((Wall) mtPiece).orientation;
    }

    /* (non-Javadoc)
     * @see com.mpgsoft.labyrinth.LabyrinthSprite#paint(java.awt.Graphics2D)
     */
    public final void paint(final Graphics2D tGraphics) {
        super.paint(tGraphics);
        
        // Check to see if an image was painted - if not, do not display the border.
        if (getImage() == null)
            return;
        
        // Paint the wall beveling (the 4th tile in the set) over the previously 
        // painted wall graphic.
        tGraphics.drawImage(WALL_TILES.getRawTileImage(3), _bounds.x, _bounds.y, null);
        
    }

}
