//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import com.samskivert.swing.Label;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.media.VirtualMediaPanel;
import com.threerings.media.animation.Animation;
import com.threerings.media.animation.FloatingTextAnimation;
import com.threerings.media.sprite.Sprite;
import com.threerings.parlor.media.ScoreAnimation;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.ChangeListener;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Displays the main game interface (the board).
 */
public final class LabyrinthBoardView extends VirtualMediaPanel implements PlaceView {

    /**
     * Static constant defining the percentage of overlap necessary to constitute
     * a full shift of the maze.  This value, when multiplied by the size of a tile,
     * represents the minimum distance the player must move a wall segment before
     * it is considered a full shift.
     */
    private static final int MINIMUM_SHIFT = Math.round(LabyrinthSprite.SIZE / 3.0f);
    
    /**
     * Static constant defining the color of the background of the board.
     */
    static final Color BACKGROUND_COLOR = new Color(0x33333f);
    
    /**
     * Static constant defining the size of the board.  The standard labyrinth
     * world consists of a 5x5 grid with moving pieces.  An additional row/column
     * has been added to all sides to facilitate the player interface.
     * @TODO [jhoffman 07/06/08]: Having a configurable board size is desireable
     * and should be possible as I have tried to encapsulate all of the logic
     * related to moveable columns and what constitutes the "special" area 
     * surrounding the board in {@link LabyrinthUtil}.
     */
    static final Dimension SIZE = new Dimension(7, 7);
    
    /**
     * This is the map of <code>Wall</code> to <code>WallSprite</code> objects.
     */
    private final Map<Comparable, WallSprite> mtWallSpritesByID = new HashMap<Comparable, WallSprite>();
    
    /**
     * This is the map of <code>GhostSprite</code objects by the player's index.
     */
    private final Map<Comparable, GhostSprite> mtGhostSpritesByID = new HashMap<Comparable, GhostSprite>();
    
    /**
     * This is the map of the remaining <code>TreasureSprite</code> objects by their
     * unique identities.  As treasures are collected from the board, they are removed
     * from this map until the game is over.
     */
    private final Map<Comparable, TreasureSprite> mtTreasureSpritesByID = new HashMap<Comparable, TreasureSprite>();
    
    /**
     * This is a list of the <code>ArrowSprite</code>s that are added to the board
     * during intialization.  When the floating wall changes, these will be notified
     * that they need to repaint.
     */
    private final List<ArrowSprite> mtArrowSprites = new ArrayList<ArrowSprite>();
        
    /**
     * This is the <code>Sprite</code> that visualizes the floating wall piece
     * that can be placed by the active player.
     */
    private final WallCursor mtWallCursor;
            
    /**
     * A refence to the Labyrinth <code>GameController</code>.
     */
    private final LabyrinthController mtGameController;

    /** 
     * A reference to the Labyrinth game object. 
     */
    protected LabyrinthObject mtGameObject;
  
    /**
     * This is the <code>ShiftEvent</code> that is instantiated when the player
     * begins a legal drag from the floating wall piece into the maze.  This is null
     * unless the drag is in-progress.
     */
    private ShiftEvent mtShiftEvent = null;
    
    /**
     * Constructs a view which will initialize itself and prepare to display
     * the game board.
     */
    public LabyrinthBoardView(final ToyBoxContext tToyBoxContext, final LabyrinthController tGameController) {
        super(tToyBoxContext.getFrameManager());
        
        // Store the game controller.
        mtGameController = tGameController;
                                
        // Instantiate the wall cursor.
        mtWallCursor = new WallCursor(tGameController);
        mtWallCursor.setRenderOrder(10);
                
    }
    
    /**
     * Private method responsible for adding a new <code>GhostSprite</code> to
     * the game board.
     */
    private final void addGhostSprite(final Ghost tGhost) {
        
        // Create a new sprite to display the ghost.
        final GhostSprite tGhostSprite = new GhostSprite(tGhost);
        
        // Ghost sprites always paint above most of the board.  <i>This</i> player's
        // ghost is always on top of the other ghosts too, for clarity. 
        tGhostSprite.setRenderOrder((tGhost.unique_id == mtGameController.getPlayerIndex()) ? 125 : 100);
        
        // Index the sprite by the ghost's unique identity.
        mtGhostSpritesByID.put(tGhost.getKey(), tGhostSprite);
        
        // Add the sprite to the board.
        addSprite(tGhostSprite);
                
    }
    
    /**
     * Private method responsible for adding a new <code>TreasureSprite</code>
     * to the game board.
     */
    private final void addTreasureSprite(final Treasure tTreasure) {
        
        // Create a new sprite to display the treasure.
        final TreasureSprite tTreasureSprite = new TreasureSprite(tTreasure);
        
        // Ghost sprites always paint on top.
        tTreasureSprite.setRenderOrder(50);
        
        // Index the sprite by the treasure's unique identity.
        mtTreasureSpritesByID.put(tTreasure.getKey(), tTreasureSprite);
        
        // Add the sprite to the board.
        addSprite(tTreasureSprite);
                
    }

    /**
     * Private method responsible for adding a new <code>WallSprite</code> to 
     * the game board.
     */
    private final void addWallSprite(final Wall tWall) {

        // Create a new sprite to display this wall. 
        final WallSprite tWallSprite = new WallSprite(tWall);
        
        // Index the sprite by the wall's unique identity.
        mtWallSpritesByID.put(tWall.getKey(), tWallSprite);
        
        // Add the sprite to those that will be rendered each frame.
        addSprite(tWallSprite);
        
    }
    
    /* (non-Javadoc)
     * @see com.threerings.crowd.client.PlaceView#didLeavePlace(com.threerings.crowd.data.PlaceObject)
     */
    public final void didLeavePlace(final PlaceObject plobj) {

        // Remove the mouse listener.
        removeMouseListener(mtMouseListener);
        removeMouseMotionListener(mtMouseListener);

        // Remove the listener we added in <code>willEnterPlace()</code> and
        // clear our reference to the game.
        mtGameObject.removeListener(mtSetListener);
        mtGameObject.removeListener(mtAttributeListener);
        mtGameObject = null;        
        
    }

    /**
     * Floats the supplied text over the board.
     */
    final void displayFloatingText(final String sText) {

        // Create an animated label.
        final Label tLabel = ScoreAnimation.createLabel(sText, Color.white, new Font("Helvetica", Font.BOLD, 48), (Component) this);
        
        // Center it on the screen.
        final Dimension tSize = tLabel.getSize();
        final int iX = (getWidth() - tSize.width) / 2;
        final int iY = (getHeight() - tSize.height) / 2;
        
        // Add the animation.
        addAnimation(new FloatingTextAnimation(tLabel, iX, iY));
        
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public final Dimension getPreferredSize() {
        return new Dimension(SIZE.width * LabyrinthSprite.SIZE, SIZE.height * LabyrinthSprite.SIZE);
    }

    /* (non-Javadoc)
     * @see com.threerings.media.VirtualMediaPanel#paintBehind(java.awt.Graphics2D, java.awt.Rectangle)
     */
    protected final void paintBehind(final Graphics2D tGraphics, final Rectangle tDirtyGeometry) {
        super.paintBehind(tGraphics, tDirtyGeometry);
        
        // Fill the background of the board.
        tGraphics.setColor(BACKGROUND_COLOR);
        tGraphics.fill(tDirtyGeometry);
        
    }
    
    /* (non-Javadoc)
     * @see com.threerings.crowd.client.PlaceView#willEnterPlace(com.threerings.crowd.data.PlaceObject)
     */
    public final void willEnterPlace(final PlaceObject tPlaceObject) {
                
        // Store the game object reference.
        mtGameObject = (LabyrinthObject) tPlaceObject;
                
        // Step along the edges of the board and create <code>ArrowSprite</code>s that 
        // will be visible during the player's turn when it is time to shift the board.
        for (int iY = 0; iY < SIZE.height; iY += 2) {
            for (int iX = 0; iX < SIZE.width; iX += 2) {
                
                // Skip points that are not in the area surrounding the board.
                if (!LabyrinthUtil.isSurrounding(iX, iY))
                    continue;
                
                // Create a shift context for this position.  Skip positions that are not
                // shiftable.
                final ShiftContext tShiftContext = ShiftContext.createContext(mtGameObject, iX, iY);
                if (tShiftContext == null)
                    continue;
                
                // Create a new <code>ArrowSprite</code>.
                final ArrowSprite tArrowSprite = new ArrowSprite();
                
                // Set the arrow's orientation to the shift orientation.  We want the
                // arrow to point in the same direction as the shift would occur.
                tArrowSprite.setOrientation(tShiftContext.getDirectionCode());
                
                // Align the sprite's location with the board.
                tArrowSprite.setLocation(iX * LabyrinthSprite.SIZE, iY * LabyrinthSprite.SIZE);
                
                // Add the arrow sprite to the list.
                mtArrowSprites.add(tArrowSprite);
                
                // Add the arrow sprite to the board.
                addSprite(tArrowSprite);
                
            }
        }
        
        // At the start of the game, all walls will be created and randomly distributed.
        // We need to step through the list of walls and make sure that sprites are
        // created for each one.
        for (Wall tWall : mtGameObject.walls)
            addWallSprite(tWall);
        
        // Update the <code>Wall</code> that will be configured by the player.
        mtWallCursor.updatePiece(mtGameObject.floating_wall);
        
        // Add the wall cursor to the game.
        addSprite(mtWallCursor);
        
        // Next we need to iterate through the treasures and create sprites for each
        // of those as well.
        for (Treasure tTreasure : mtGameObject.treasures)
            addTreasureSprite(tTreasure);
        
        // Next we need to iterate through the ghosts (a.k.a. players) and create sprites 
        // for each of those as well.
        for (Ghost tGhost : mtGameObject.ghosts)
            addGhostSprite(tGhost);
        
        // Start listening for shared state changes.
        mtGameObject.addListener(mtAttributeListener);
        mtGameObject.addListener(mtSetListener);
        
        // Start listening for mouse events.
        addMouseListener(mtMouseListener);
        addMouseMotionListener(mtMouseListener);
        
    }
    
    /**
     * This is the private <code>MouseListener</code> that responds to mouse
     * press, click, drag and release events.  It is responsible for translating
     * those events into specific game actions such as shifting a row or column
     * in the maze and moving the player's piece to a new location within 
     * the maze.
     */
    private final MouseInputListener mtMouseListener = new MouseInputAdapter() {
        public final void mouseClicked(final MouseEvent tEvent) {

            // Get the mouse position.
            final int iX = tEvent.getX();
            final int iY = tEvent.getY();
            
            // If the mouse is currently over the floating, moveable wall tile, 
            // clicking on it causes it to rotate.
            if (mtWallCursor.hitTest(iX, iY))
                mtWallCursor.rotate();
                        
            // Otherwise, check to see if we're at the point in the game where the player
            // can move their piece through the maze.
            else if (mtGameController.canGhostBeMoved()) {
                       

                // Get the current player's index.
                final int iPlayerIndex = mtGameController.getPlayerIndex();

                // Get the player's ghost sprite.
                final GhostSprite tGhostCursor = mtGhostSpritesByID.get(iPlayerIndex);

                // Verify that the mouse is over the player's piece.
                if (tGhostCursor.hitTest(iX, iY)) {
                    
                    try {
                        
                        // Notify the controller that the player has moved their ghost.
                        mtGameController.ghostPlaced(mtGameObject.ghosts.get(iPlayerIndex));
                        
                    } finally {
                        
                        // Get the animation for this player's ghost.
                        abortAnimation(tGhostCursor.getAnimation());

                    }
                    
                }
                    
            }
            
        }

        public final void mouseDragged(final MouseEvent tEvent) {

            // If a legal shift event is in-progress, proxy the drag to the event.
            if (mtShiftEvent != null) { 
                                    
                // Shift the wall curor and all affected pieces by the distance
                // the mouse has moved from the origin.
                mtShiftEvent.setOffset(mtWallCursor, tEvent);
                mtShiftEvent.setOffset(mtGameObject.walls, mtWallSpritesByID, tEvent);
                mtShiftEvent.setOffset(mtGameObject.ghosts, mtGhostSpritesByID, tEvent);
                mtShiftEvent.setOffset(mtGameObject.treasures, mtTreasureSpritesByID, tEvent);
                                                                 
            } 
                        
        }

        public final void mouseMoved(final MouseEvent tEvent) {

            // Determine which tile the mouse is over.
            final int iX = tEvent.getX() / LabyrinthSprite.SIZE;
            final int iY = tEvent.getY() / LabyrinthSprite.SIZE;
            
            // If the player is allowed to move the floating wall, then handle this mouse 
            // position as a change to the floating wall's position.
            if (mtGameController.canWallBePlaced()) {
               
                // Quick abort unless the tile is part of the area that surrounds the board. 
                if (!LabyrinthUtil.isSurrounding(iX, iY))
                    return;

                // Make sure that the mouse is over a moveable column.
                if (!LabyrinthUtil.isMoveable(iX) && !LabyrinthUtil.isMoveable(iY))
                    return;

                // Make sure that the wall cursor is not in it's previous location.  The
                // wall can not be inserted at the point it just was shifted out of.
                final int iLocation = LabyrinthUtil.getLocation(iX, iY);
                if (mtGameObject.floating_wall_origin != iLocation)
                    mtWallCursor.setPosition(iX, iY);
                
            } else if (mtGameController.canGhostBeMoved()) {
                
                // Wrap the coordinates in a point.
                final Point tMouseOverPoint = new Point(iX, iY);
                                 
                // Get the player index.
                final int iPlayerIndex = mtGameController.getPlayerIndex();
                
                // Attempt to resolve a path from the player's sprite to the
                // mouse-over tile.
                final Ghost tGhost = mtGameObject.ghosts.get(iPlayerIndex);

                // Try to resolve a path from the player's ghost to the destination.
                final Path tPath = Path.findPath(mtGameObject.walls, new Point(tGhost.x, tGhost.y), tMouseOverPoint);
                if (tPath != null) {
                    
                    // Get the sprite that corresponds to this ghost.
                    final GhostSprite tGhostSprite = mtGhostSpritesByID.get(iPlayerIndex);
                    if (tGhostSprite != null) {
                        
                        // Update the ghost's location.
                        tGhost.x = iX;
                        tGhost.y = iY;
                        
                        // Update the sprite.
                        tGhostSprite.updatePiece(tGhost);
                        
                    }
                                            
                }
                    
            }
                        
        }

        public final void mousePressed(final MouseEvent tEvent) {

            // Get the position of the event.
            final int iX = tEvent.getX();
            final int iY = tEvent.getY();

            // If we're at the part of the player's turn where they can place the wall,
            // check to see if the user has initiated a drag from that location.
            if (mtGameController.canWallBePlaced()) {

                // Check to see if the drag event has been initiated over the floating wall piece.
                // If so, create a new <code>ShiftEvent</code> holding the context of the drag.  This
                // should never return null since we're already enforcing that the wall cursor is 
                // restricted to valid shifting positions.
                if (mtWallCursor.hitTest(iX, iY))
                    mtShiftEvent = ShiftEvent.createEvent(mtGameObject, tEvent);

                // Otherwise, clear out any existing shift event.  This should already be null
                // but for sanity sake, we'll clear it anyway.
                else
                    mtShiftEvent = null;
                
            }
                        
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
         */
        public final void mouseReleased(final MouseEvent tEvent) {

            // If a valid shift was started, we are either going to commit the change
            // to the server or we're going to undo the shift.
            if (mtShiftEvent != null) {
                
                // This will hold the offset that will be applied to the wall cursor
                // and affected pieces.  Initially it's zero but if a move is requested
                // from the server, we'll try to set it to the full shift amount.
                int iOffset = 0;
                
                try {

                    // Get the magnitude of the shift.  If the player has shifted it at 
                    // least the minimum number of pixels, commit the move by passing the
                    // reconfigured wall to the controller.
                    final int iMagnitude = mtShiftEvent.getMagnitude(tEvent);
                    if (iMagnitude >= MINIMUM_SHIFT) {
                        
                        try {
                            
                            // Try to place the wall via the controller.
                            mtGameController.wallPlaced(mtGameObject.floating_wall);
                            
                            // If this succeeds, we'll paint the next couple of frames with
                            // an offset applied to each sprite.
                            iOffset = LabyrinthSprite.SIZE;
                            
                        } catch (final Exception tEx) {
                            
                            // Gracefully reset the maze.
                            iOffset = 0;
                            
                        }
                        
                    }

                } finally {

                    // Reset the position of the floating cursor and all of the pieces affected
                    // by the shift.
                    mtShiftEvent.shift_context.setOffset(mtWallCursor, iOffset);
                    mtShiftEvent.shift_context.setOffset(mtGameObject.walls, mtWallSpritesByID, iOffset);
                    mtShiftEvent.shift_context.setOffset(mtGameObject.ghosts, mtGhostSpritesByID, iOffset);
                    mtShiftEvent.shift_context.setOffset(mtGameObject.treasures, mtTreasureSpritesByID, iOffset);

                    // Clear the event.
                    mtShiftEvent = null;
                    
                }
                
            }
            
        }
    };
    
    /**
     * This is the private <code>ChangeListener</code> that responds to changes
     * in the shared game object state.
     */
    private final ChangeListener mtAttributeListener = new AttributeChangeListener() {
        public final void attributeChanged(final AttributeChangedEvent tEvent) {
            
            // Get the name of the attribute that changed.
            final String sAttribute = tEvent.getName();
            
            // When the active treasure changes, we need to add or reset the sparkle
            // to show which treasure is being sought.
            if (LabyrinthObject.ACTIVE_TREASURE.equals(sAttribute)) {

                // Get the identity of the new active treasure.
                final int iNewTreasure = tEvent.getIntValue();
                if (iNewTreasure >= 0)
                    addAnimation(mtTreasureSpritesByID.get(iNewTreasure).getAnimation());
                        
            }
                       
            // When the floating <code>Wall</code> changes, the wall cursor needs to
            // update with the new shape, orientation and location.
            else if (LabyrinthObject.FLOATING_WALL.equals(sAttribute))
                mtWallCursor.updatePiece((Wall) tEvent.getValue());
            
            // When the flag indicating whether or not the wall can be placed changes
            // we need to invalidate each of the wall sprites to ensure they repaint.
            else if (LabyrinthObject.WALL_PLACED.equals(sAttribute)) {
                for (ArrowSprite tArrowSprite : mtArrowSprites)
                    tArrowSprite.invalidate();
                
                // Get the animation for this player's ghost.
                final Animation tAnimation = mtGhostSpritesByID.get(mtGameController.getPlayerIndex()).getAnimation();
                
                // If it is now time for the player to move their character, we 
                // need to add the animation that indicates such.
                if (mtGameController.canGhostBeMoved())
                    addAnimation(tAnimation);
                
            } 
                        
        }
    };
    
    /**
     * This is the private <code>SetListener</code> that responds to changes in the 
     * distributed sets of the shared game state.
     */
    private final SetListener mtSetListener = new SetListener() {
        public final void entryAdded(final EntryAddedEvent tEvent) {
            if (LabyrinthObject.WALLS.equals(tEvent.getName()))
                addWallSprite((Wall) tEvent.getEntry());
        }

        public final void entryRemoved(final EntryRemovedEvent tEvent) {
            if (LabyrinthObject.WALLS.equals(tEvent.getName())) {
                final Sprite tSprite = mtWallSpritesByID.remove(tEvent.getKey());
                if (tSprite != null)
                    removeSprite(tSprite);
            }
        }

        public final void entryUpdated(final EntryUpdatedEvent tEvent) {
            
            // This will hold the <code>Piece</code> to be updated.
            Piece tPiece = null;
            
            // This holds the <code>Map</code> that tracks sprites for this
            // type of piece.
            Map<Comparable, ? extends PieceSprite> tSpritesByID = null;
            
            final String sEventName = tEvent.getName();
            if (LabyrinthObject.GHOSTS.equals(sEventName)) {
                tPiece = (Ghost) tEvent.getEntry();
                tSpritesByID = mtGhostSpritesByID;
                
            } else if (LabyrinthObject.TREASURES.equals(sEventName)) {
                final Treasure tTreasure = (Treasure) tEvent.getEntry();
                
                // Treasures disappear from the map when they are picked up.  In the
                // event that this treasure is now picked-up, we're going to remove
                // the sprite and skip the update process.
                if (tTreasure.isCollected()) {
                    
                    // Remove the sprite from the map.  We will never need it again
                    // now that the treasure has been collected.
                    final TreasureSprite tTreasureSprite = mtTreasureSpritesByID.remove(tTreasure.getKey());
                                        
                    // Stop painting the the sprite.
                    removeSprite(tTreasureSprite);
                    
                    // Get the sparkle animation and remove that as well.
                    abortAnimation(tTreasureSprite.getAnimation());

                    // No further work necessary.
                    return;
                    
                }
                    
                // Otherwise, this treasure was shifted so we need to do the
                // normal update.
                tPiece = tTreasure; 
                tSpritesByID = mtTreasureSpritesByID;
                                        
            } else if (LabyrinthObject.WALLS.equals(sEventName)) {
                tPiece = (Wall) tEvent.getEntry();
                tSpritesByID = mtWallSpritesByID;
                
            }

            // Quick abort unless a piece was found.
            if (tPiece == null || tSpritesByID == null)
                return;
            
            // Get the sprite that corresponds to the updated object.  This should
            // never return null but it never hurts to check.
            final PieceSprite tSprite = tSpritesByID.get(tPiece.getKey());
            if (tSprite != null)
                tSprite.updatePiece(tPiece);

        }
    };
    
    /**
     * This is the private <code>LabyrinthSprite</code> class responsible for painting
     * the insert-here arrows on the board during the wall shifting process.
     */
    private final class ArrowSprite extends LabyrinthSprite {

        /* (non-Javadoc)
         * @see com.mpgsoft.labyrinth.LabyrinthSprite#getImage()
         */
        protected final Image getImage() {

            // If the wall pieces are not moveable, this should always return null.
            if (!mtGameController.canWallBePlaced())
                return null;
            
            // Check to see if this sprite appears in the same location as the
            // previous wall's origin - if so, it should also return null.
            final int iLocation = LabyrinthUtil.getLocation(_bounds.x / LabyrinthSprite.SIZE, _bounds.y / LabyrinthSprite.SIZE);
            if (iLocation == mtGameObject.floating_wall_origin)
                return null;
            
            // Otherwise, return the north-facing arrow image.
            return WallSprite.WALL_TILES.getRawTileImage(4);  
        }
        
    }
    
}
