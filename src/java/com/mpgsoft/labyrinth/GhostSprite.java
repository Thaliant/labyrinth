//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Image;

import com.samskivert.Log;
import com.threerings.media.tile.TileMultiFrameImage;
import com.threerings.media.tile.UniformTileSet;
import com.threerings.media.util.MultiFrameImage;

/**
 * A {@link PieceSprite} that displays the appropriate image according to
 * what player controls the internal {@link Ghost}.
 * 
 * @author Jeffrey D. Hoffman
 */
public final class GhostSprite extends FocusablePieceSprite {
    
    /**
     * This is a static reference to the <code>UniformTileSet</code> which holds the
     * tiles for the players.  It is package-private because it is initialized from 
     * the {@link LabyrinthPanel} class.
     */
    static UniformTileSet GHOST_TILES;
    
    /**
     * This is a static reference to the <code>UniformTileSet</code> which holds the
     * tiles for the movement cursor that appears when it is the player's turn to 
     * move their own ghost.  It is package-private because it is initialized from 
     * the {@link LabyrinthPanel} class.
     */
    static UniformTileSet CURSOR_TILES;
        
    /**
     * This is the private <code>TileMultiFrameImage</code> that wraps the CURSOR_TILES
     * and is used to create the animation for the active image.  It is instantiated
     * on first use to ensure that the tileset is properly initialized.
     */
    private static TileMultiFrameImage MULTIFRAME_CURSOR = null;

    /**
     * Package-private constructor for the class accepting the <code>Ghost</code>
     * to be rendered as a <code>Sprite</code>.
     */
    GhostSprite(final Ghost tGhost) {
        super(tGhost, 1.75d);
    }

    /* (non-Javadoc)
     * @see com.mpgsoft.labyrinth.LabyrinthSprite#getImage()
     */
    protected final Image getImage() {

        // Get a local handle on the <code>Player</code> being painted.
        final Ghost tGhost = (Ghost) mtPiece;

        // Get the <code>Tile</code> corresponding to this shape.  If properly initialized,
        // this should never return null - but if it does, log a warning.
        final Image tTile = GHOST_TILES.getRawTileImage(tGhost.unique_id);
        if (tTile == null)
            Log.warning("Tile for player shape " + tGhost.unique_id + " is missing.");

        return tTile;
    }

    /* (non-Javadoc)
     * @see com.mpgsoft.labyrinth.FocusablePieceSprite#getMultiFrameImage()
     */
    protected final MultiFrameImage getMultiFrameImage() {

        // Instantiate the multiframe image if it has not already been created.
        if (MULTIFRAME_CURSOR == null)
            MULTIFRAME_CURSOR = new TileMultiFrameImage(CURSOR_TILES);

        return MULTIFRAME_CURSOR;
    }

}
