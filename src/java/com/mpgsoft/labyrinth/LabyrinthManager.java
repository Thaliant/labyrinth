//
// $Id: SampleManager.java,v 1.21 2004/08/27 18:51:26 mdb Exp $

package com.mpgsoft.labyrinth;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.samskivert.util.RandomUtil;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.parlor.game.data.GameAI;
import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.turn.server.TurnGameManager;
import com.threerings.parlor.turn.server.TurnGameManagerDelegate;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.util.DirectionCodes;

/**
 * Handles the server side of the game.
 */
public final class LabyrinthManager extends GameManager implements TurnGameManager {

    /** 
     * This is the <code>TurnGameManagerDelegate</code> which is responsible for
     * managing the turn-based game play on our behalf.
     *  
     * @TODO [jhoffman 07/09/08]: This delegate was created based on the Reversi
     * tutorial.  The <code>TurnGameManagerDelegate(TurnGameManager)</code> 
     * constructor has been deprecated, however.
     */
    private final TurnGameManagerDelegate mtTurnDelegate = new TurnGameManagerDelegate(this);

    /** 
     * The Labyrinth <code>GameObject</code> shared between players on the board.
     */
    protected LabyrinthObject mtGameObject;

    /** 
     * The Labyrinth game configuration.
     */
    protected ToyBoxGameConfig mtGameConfiguration;
    
    /**
     * Base constructor for the Labyrinth server manager.
     */
    public LabyrinthManager() {
        
        // Register the turn-based game delegate.
        addDelegate(mtTurnDelegate);
                
    }
    
    /* (non-Javadoc)
     * @see com.threerings.parlor.game.server.GameManager#assignWinners(boolean[])
     */
    protected final void assignWinners(final boolean[] abWinners) {
        super.assignWinners(abWinners);
        
        // Get the point distribution.
        final int[] aiPoints = LabyrinthUtil.getPoints(mtGameObject);
        
        // This will hold the maximum points.  Any player that matches
        // this value will be considered a winner.
        int iMaximumPoints = 0;
        
        // Step through the list of points and find the maximum.
        for (int iPoints : aiPoints) {
            if (iPoints > iMaximumPoints)
                iMaximumPoints = iPoints;
        }
            
        // Now step through the list of players and determine if they're the winner.
        // The game can end in a tie if multiple players have the same number of 
        // points at the end of the game.
        for (int iPlayer = 0; iPlayer < abWinners.length; ++iPlayer)
            abWinners[iPlayer] = aiPoints[iPlayer] == iMaximumPoints;
        
    }

    /* (non-Javadoc)
     * @see com.threerings.crowd.server.PlaceManager#createPlaceObject()
     */
    protected final PlaceObject createPlaceObject() {
        return new LabyrinthObject();
    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.game.server.GameManager#didInit()
     */
    public final void didInit() {
        super.didInit();

        // get a casted reference to our game configuration
        mtGameConfiguration = (ToyBoxGameConfig) _config;

    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.game.server.GameManager#didStartup()
     */
    public final void didStartup() {
        super.didStartup();

        // grab our own casted game object reference
        mtGameObject = (LabyrinthObject) super._gameobj;
        
        // The Reversi tutorial suggests board initialization should occur in the
        // <code>gameWillStart()</code> method.  I did this initially but found that
        // the game starts (the board is painted) and then <code>gameWillStart()</code>
        // runs and creates the board.  This resulted in a large amount of client-server
        // traffic since all of the wall pieces are created and updated during this step.
        // It also meant that the code designed to handle pieces already in the game's
        // distributed state at startup was never firing.
        // 
        // Instead, I moved the board initialization code here.  When the manager is 
        // started, the board for this game instance is immediately initialized.  The
        // client doesn't enter the board view until after this initialization is
        // completed - which is the desired effect and network traffic is minimized.
        // After a bunch of testing, it appears that this is a safe change.
        
        // This is the number of walls placed - and is used to calculate each 
        // wall's unique identity.
        int iWalls = 0;
        
        // The corners and edge centers of the maze are fixed shapes
        // and orientation.  This code orients those pieces properly.
        // The center square is also a fixed shape (straight) but
        // a random direction is added for each new game.
        mtGameObject.addToWalls(new Wall(++iWalls, 1, 1, Wall.L_SHAPE, DirectionCodes.EAST));
        mtGameObject.addToWalls(new Wall(++iWalls, 3, 1, Wall.T_SHAPE, DirectionCodes.SOUTH));
        mtGameObject.addToWalls(new Wall(++iWalls, 5, 1, Wall.L_SHAPE, DirectionCodes.SOUTH));
        mtGameObject.addToWalls(new Wall(++iWalls, 1, 3, Wall.T_SHAPE, DirectionCodes.EAST));
        mtGameObject.addToWalls(new Wall(++iWalls, 3, 3, Wall.STRAIGHT, Math.random() < 0.5d ? DirectionCodes.NORTH : DirectionCodes.EAST));
        mtGameObject.addToWalls(new Wall(++iWalls, 5, 3, Wall.T_SHAPE, DirectionCodes.WEST));
        mtGameObject.addToWalls(new Wall(++iWalls, 1, 5, Wall.L_SHAPE, DirectionCodes.NORTH));
        mtGameObject.addToWalls(new Wall(++iWalls, 3, 5, Wall.T_SHAPE, DirectionCodes.NORTH));
        mtGameObject.addToWalls(new Wall(++iWalls, 5, 5, Wall.L_SHAPE, DirectionCodes.WEST));
        
        // Now we're going to build the list of wall segments that are moveable.
        // This list will hold the <code>Wall</code>s that remain to be placed.
        final List<Wall> tWalls = new ArrayList<Wall>();
        
        // Initialize the list with the default wall segments.  The constructor
        // being used randomly selects an orientation so we don't need to do
        // that ourselves.
        for (int iIndex = 0; iIndex < 5; ++iIndex)
            tWalls.add(new Wall(++iWalls, Wall.STRAIGHT));
        for (int iIndex = 0; iIndex < 6; ++iIndex)
            tWalls.add(new Wall(++iWalls, Wall.L_SHAPE));
        for (int iIndex = 0; iIndex < 6; ++iIndex)
            tWalls.add(new Wall(++iWalls, Wall.T_SHAPE));

        // The next step of maze initialization is to randomly distribute the 
        // selection of wall pieces across the remaining empty squares on the
        // board.  This is done by 
        for (int iY = 1; iY < LabyrinthBoardView.SIZE.height - 1; ++iY) {
            for (int iX = 1; iX < LabyrinthBoardView.SIZE.width - 1; ++iX) {

                // Skip the special fixed squares.
                if ((iX == 1 || iX == 3 || iX == 5) && (iY == 1 || iY == 3 || iY == 5))
                    continue;
                
                // Randomly select from the remaining wall segments.  Since the
                // wall received a random orientation at construction, we only
                // need to set it's location.
                final Wall tWall = tWalls.remove(RandomUtil.getInt(tWalls.size()));
                
                // Set the wall's location on the grid.
                tWall.x = iX;
                tWall.y = iY;
                
                // Add the newly created wall to the game.
                mtGameObject.addToWalls(tWall);
                
            }
        }

        // The last remaining piece in the walls <code>List</code> is the tile
        // that can be shifted into the maze by the turn holder.  The wall is
        // also positioned, by default, into a valid moveable location.
        mtGameObject.floating_wall = tWalls.remove(0);
        mtGameObject.floating_wall.y = 2;
                
        // Get the number of players in this game.
        final int iPlayers = mtGameObject.getPlayerCount();
        
        // This set of <code>Point</code>s will be used to prevent players and treasures
        // from starting on-top of one another.  As points are randomly selected, they
        // are added to this list which prevents those points from being selected in a
        // future call to <code>getRandomLocation()</code>.
        final Set<Point> tUsedPoints = new HashSet<Point>();
        
        // Randomly distribute <code>Treasure</code> pieces around the board.
        for (int iTreasure = 0; iTreasure < Treasure.TREASURE_COUNT; ++iTreasure)
            mtGameObject.addToTreasures(new Treasure(iTreasure, LabyrinthUtil.getRandomLocation(tUsedPoints, true)));
        
        // Create <code>Ghost</code> pieces for each of the players.
        for (int iPlayer = 0; iPlayer < iPlayers; ++iPlayer)
            mtGameObject.addToGhosts(new Ghost(iPlayer, LabyrinthUtil.getRandomLocation(tUsedPoints, true)));
                
    }

    /**
     * Callback from the client when the provided <code>Ghost</code> piece has
     * been placed by the turn holder.
     */
    public final void placeGhost(final BodyObject tPlayer, final Ghost tGhost) {

        // First, verify that the player placing the wall is actually the
        // active player.
        final int iPlayerIndex = mtTurnDelegate.getTurnHolderIndex();
        if (tPlayer.getOid() != _playerOids[iPlayerIndex])
            System.err.println("Request to place ghost received from non-turn holder [who=" + tPlayer.who() + ", turnHolder=" + mtGameObject.turnHolder + "].");

        // Second, verify that the active player has already placed a wall 
        // in the maze.
        else if (!mtGameObject.wall_placed)
            System.err.println("Request to place ghost received before wall placed.");

        // Verify that the ghost provided corresponds to the ghost the player controls. 
        else if (!tGhost.getKey().equals(iPlayerIndex))
            System.err.println("Invalid ghost placement request [key=" + tGhost.getKey() + ", pid=" + iPlayerIndex + "].");

        // Otherwise, move the ghost if a valid path is found.
        else
            placeGhost(iPlayerIndex, tGhost);
                    
    }
    
    /**
     * Package-private method which places the ghost controlled by the designated player
     * index at it's new location.
     */
    final void placeGhost(final int iPlayerIndex, final Ghost tGhost) {

        // Get the ghost out of the shared object.  Record an error if the ghost provided
        // doesn't match the 
        final Ghost tOriginalGhost = mtGameObject.ghosts.get(iPlayerIndex);
                   
        // Get the current location for the specified ghost.
        final Path tPath = Path.findPath(mtGameObject.walls, new Point(tOriginalGhost.x, tOriginalGhost.y), new Point(tGhost.x, tGhost.y));
        if (tPath == null) { 
            System.err.println("Ghost can not reach [from=" + tOriginalGhost.x + "," + tOriginalGhost.y + "; to=" + tGhost.x + "," + tGhost.y + "].");
            return;
        }

        try {
            
            // Start a transaction event.
            mtGameObject.startTransaction();

            // Move the original ghost.
            tOriginalGhost.x = tGhost.x;
            tOriginalGhost.y = tGhost.y;

            // Notify the game state that the player has moved.
            mtGameObject.updateGhosts(tGhost);

            // Check to see if the player has moved onto the square holding the 
            // active treasure.  If so, the player picks it up.
            final Treasure tTreasure = mtGameObject.treasures.get(mtGameObject.active_treasure);
            if (tTreasure != null && tTreasure.x == tGhost.x && tTreasure.y == tGhost.y) {

                // Assign this treasure to the player.
                tTreasure.owner = iPlayerIndex;

                // Remove the treasure from the board so that it doesn't continue to be
                // shifted behind the scenes even though it is no longer visible.
                tTreasure.x = -1;
                tTreasure.y = -1;

                // Temporarily set the game object's treasure index to zero.  This will be
                // immediately rectified in <code>turnWillStart()</code>.
                mtGameObject.active_treasure = -1;

                // Update the shared treasure state.
                mtGameObject.updateTreasures(tTreasure);

            }
            
        } finally {
            
            // Send the transaction.
            mtGameObject.commitTransaction();
            
        }

        // After the player moves, end the turn.
        mtTurnDelegate.endTurn();
            
    }
    
    /**
     * Callback from the client when the provided <code>Wall</code> piece has 
     * been placed by the turn holder.
     */
    public final void placeWall(final BodyObject tPlayer, final Wall tWall) {

        // First, verify that the player placing the wall is actually the
        // active player.
        final int iPlayerIndex = mtTurnDelegate.getTurnHolderIndex();
        if (tPlayer.getOid() != _playerOids[iPlayerIndex])
            System.err.println("Request to place wall received from non-turn holder [who=" + tPlayer.who() + ", turnHolder=" + mtGameObject.turnHolder + "].");

        // Second, verify that the active player has not already placed a 
        // wall in the maze.
        else if (mtGameObject.wall_placed)
            System.err.println("Request to place wall received after wall already placed.");

        else
            placeWall(iPlayerIndex, tWall);
        
    }
    
    /**
     * Package-private wall placement method which is used for both the real player
     * callback from the client and for virtual AI players.
     */
    final void placeWall(final int iPlayerIndex, final Wall tWall) {

        // Verify that the wall is currently floating in the area 
        // surrounding the maze.  Create a <code>ShiftContext</code> 
        // based on the floating wall's position.
        final ShiftContext tShiftContext = ShiftContext.createContext(mtGameObject, tWall.x, tWall.y);
        if (tShiftContext == null) {
            System.err.println("Request to place wall in an illegal position [x=" + tWall.x + ", y=" + tWall.y + "] received.");
            return;
        }

        try {
        
            // Start a transaction.
            mtGameObject.startTransaction();

            // Record that the player has placed a wall which prevents the active
            // player from doing so a second time.
            mtGameObject.setWall_placed(true);

            // Add the newly configured wall to the game.
            mtGameObject.addToWalls(tWall);

            // Perform the physical shift on the walls.
            final List<Piece> tShiftedWalls = tShiftContext.shift(mtGameObject.walls, false);

            // We need to iterate through the walls and notify the distributed set
            // that they have been updated.  One of the walls will have been shifted
            // out of the maze - that's the new floating wall.
            for (Piece tWallPiece : tShiftedWalls) {
                final Wall tShiftedWall = (Wall) tWallPiece;

                // If the wall has been shifted out of play, then make this the 
                // active piece.  Otherwise, notify the game context that the wall
                // has been updated.
                if (LabyrinthUtil.isSurrounding(tShiftedWall.x, tShiftedWall.y)) {
                    mtGameObject.removeFromWalls(tShiftedWall.getKey());
                    mtGameObject.setFloating_wall((Wall) tWallPiece);
                    mtGameObject.setFloating_wall_origin(LabyrinthUtil.getLocation(tShiftedWall.x, tShiftedWall.y));

                } else
                    mtGameObject.updateWalls(tShiftedWall);

            }

            // Shift ghosts that are affected by the newly inserted piece.  Ghosts wrap
            // to the other side.
            final List<Piece> tShiftedGhosts = tShiftContext.shift(mtGameObject.ghosts, true);
            for (Piece tGhostPiece : tShiftedGhosts)
                mtGameObject.updateGhosts((Ghost) tGhostPiece);

            // Shift treasures that are affected by the newly inserted piece.  Like ghosts,
            // treasures also wrap to the other side of the board.
            final List<Piece> tShiftedTreasures = tShiftContext.shift(mtGameObject.treasures, true);
            for (Piece tTreasurePiece : tShiftedTreasures)
                mtGameObject.updateTreasures((Treasure) tTreasurePiece);
            
        } finally {
            
            // Commit all of the changes that were just made.
            mtGameObject.commitTransaction();
            
        }
        
    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.turn.server.TurnGameManager#turnDidEnd()
     */
    public final void turnDidEnd() {
        
        // There is nothing to do unless the active treasure has been cleared.
        if (mtGameObject.active_treasure >= 0)
            return;
                
        // Iterate through the treasures and see if any are left to be collected.
        // If so, there is nothing left to do.
        for (Treasure tTreasure : mtGameObject.treasures) {
            if (!tTreasure.isCollected())
                return;
        }
                    
        // If we reach this point, there are no remaining treasures to pickup so
        // the game ends.
        endGame();
                    
    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.turn.server.TurnGameManager#turnDidStart()
     */
    public final void turnDidStart() { 
                
        // Get the active player index.
        final int iPlayerIndex = mtTurnDelegate.getTurnHolderIndex();
        
        // Check to see if the active player is an AI player - if not, there is
        // nothing left to do on this turn.
        if (!isAI(iPlayerIndex))
            return;

        // Get a local handle on the corresponding AI.  This should never return
        // null since we have already verified that the current player is an AI.
        final GameAI tAI = _AIs[iPlayerIndex];
        if (tAI == null)
            return;
        
        // Provide the AI with a chance to process this turn.
        LabyrinthAI.processTurn(this, mtGameObject, iPlayerIndex);
        
    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.turn.server.TurnGameManager#turnWillStart()
     */
    public final void turnWillStart() {
        
        try {
            
            // Start a new transaction.
            mtGameObject.startTransaction();
        
            // Reset the wall placement flag ensuring the player has to 
            // shift the maze before they can move.
            mtGameObject.setWall_placed(false);

            // If there is no active treasure, we need to randomly select one from
            // the those remaining to be collected.
            if (mtGameObject.active_treasure < 0) {

                // If there is no active treasure, we need to randomly pick a new one.
                // This list will hold the remaining treasures.
                final List<Treasure> tRemainingTreasures = new ArrayList<Treasure>(Treasure.TREASURE_COUNT);

                // Iterate through the treasures and find any that are not already 
                // picked up.
                for (Treasure tTreasure : mtGameObject.treasures) {
                    if (!tTreasure.isCollected())
                        tRemainingTreasures.add(tTreasure);
                }

                // We should never reach this point if there are no remaining treasures -
                // the game should already have ended - but it doesn't hurt to check.
                final int iRemainingTreasures = tRemainingTreasures.size();
                if (iRemainingTreasures > 0)
                    mtGameObject.setActive_treasure(tRemainingTreasures.get(RandomUtil.getInt(iRemainingTreasures)).unique_id);

            }
            
        } finally {
            
            // Conclude the transaction.
            mtGameObject.commitTransaction();
            
        }
                        
    }

}
