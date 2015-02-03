//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Image;

import com.samskivert.Log;
import com.threerings.media.tile.TileMultiFrameImage;
import com.threerings.media.tile.UniformTileSet;
import com.threerings.media.util.MultiFrameImage;

/**
 * This sprite is responsible for painting the image that corresponds to
 * an item of {@link Treasure}.  It also managers the <code>Animation</code>
 * that illuminates said treasure for use when it is the current object 
 * of player desire.
 * <p>
 * On a related note, part of (or all) the graphic tiles used in this program 
 * is the public domain roguelike tileset "RLTiles".  I have modified some of 
 * the tiles and am displaying this notice as part of that agreement.
 * <p>
 * You can find the original tileset at: 
 * @url http://rltiles.sf.net
 * 
 * @author Jeffrey D. Hoffman
 */
final class TreasureSprite extends FocusablePieceSprite {

    /**
     * This is a static reference to the <code>UniformTileSet</code> which holds the
     * tiles for the treasures.  It is package-private because it is initialized from 
     * the {@link LabyrinthPanel} class.
     */
    static UniformTileSet TREASURE_TILES;
    
    /**
     * This is a static reference to the <code>UniformTileSet</code> which holds the
     * sparkle images that appear over treasures.  It is package-private because it 
     * is initialized from the {@link LabyrinthPanel} class.
     */
    static UniformTileSet SPARKLE_TILES;
    
    /**
     * This is the private <code>TileMultiFrameImage</code> that wraps the SPARKLE_TILES
     * and is used to create the animation for the active image.  It is instantiated
     * on first use to ensure that the tileset is properly initialized.
     */
    private static TileMultiFrameImage MULTIFRAME_SPARKLE = null;
        
    /**
     * Package-private constructor for the class accepting the <code>Wall</code>
     * to be rendered as a <code>Sprite</code>.
     */
    TreasureSprite(final Treasure tTreasure) {
        super(tTreasure, 2.25d);
    }
    
    /* (non-Javadoc)
     * @see com.mpgsoft.labyrinth.LabyrinthSprite#getImage()
     */
    protected final Image getImage() {

        // Get a local handle on the <code>Treasure</code> being painted.
        final Treasure tTreasure = (Treasure) mtPiece;

        // Get the <code>Tile</code> corresponding to this treasure's index.
        final Image tTile = TREASURE_TILES.getRawTileImage(tTreasure.unique_id);
        if (tTile == null)
            Log.warning("Tile for treasure #" + tTreasure.unique_id + " is missing.");

        return tTile;
    }

    /* (non-Javadoc)
     * @see com.mpgsoft.labyrinth.HighlightedPieceSprite#getMultiFrameImage()
     */
    protected final MultiFrameImage getMultiFrameImage() {

        // Instantiate the multiframe image if it has not already been created.
        if (MULTIFRAME_SPARKLE == null)
            MULTIFRAME_SPARKLE = new TileMultiFrameImage(SPARKLE_TILES);

        return MULTIFRAME_SPARKLE;
    }
        
}
