//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.threerings.presents.dobj.DSet;

/**
 * @author Jeffrey D. Hoffman
 */
final class LabyrinthAI {
    
    /**
     * Static constant defining the delay between moves the AI makes.  This is done
     * to ease the speed with which the AI solves the board.
     */
    private static final long TURN_THROTTLE = 3000l;
    
    /**
     * This class can not be instantiated.
     */
    private LabyrinthAI() { }
            
    /**
     * Restores the {@link Piece}s in the provided <code>DSet</code> to the positions
     * they held at the time the map of <code>Point</code>s was created.
     * @see LabyrinthAI#extract(DSet)
     */
    private static final void restore(final DSet<? extends Piece> tPieces, final Map<Comparable, Point> tPointsByID) {
        
        // Iterate through the {@link Piece}s and copy their original locations
        // into the set.
        for (final Piece tPiece : tPieces) {
            
            // Get it's original location.
            final Point tOriginalLocation = tPointsByID.get(tPiece.unique_id);
            if (tOriginalLocation == null)
                continue;
            
            // Copy the original location into the piece.
            tPiece.x = tOriginalLocation.x;
            tPiece.y = tOriginalLocation.y;
            
        }
        
    }
    
    /**
     * This is a private method which iterates across the provided 
     * {@Piece} <code>DSet</code> and extracts the current locations
     * into a set of <code>Point</code>s.
     * @see LabyrinthAI#restore(DSet, Map)
     */
    private static final Map<Comparable, Point> extract(final DSet<? extends Piece> tPieces) {
        
        // This map will hold the points by identity.
        final Map<Comparable, Point> tPointsByID = new HashMap<Comparable, Point>();
        
        // Iterate across the pieces and copy their locations into points.
        for (final Piece tPiece : tPieces)
            tPointsByID.put(tPiece.unique_id, new Point(tPiece.x, tPiece.y));

        return tPointsByID;
    }
    
    /**
     * Package-private static method which is called to process a
     * turn of the AI for the provided <code>GameManager</code> and
     * distributed <code>GameObject</code>.
     */
    static final void processTurn(final LabyrinthManager tGameManager, final LabyrinthObject tGameObject, final int iPlayerIndex) {
        
        // The AI needs to run on a background thread in order to emulate the decision
        // time that a real player would require to evaluate the maze.  In order to 
        // sleep, we need a secondary thread.  I looked through the APIs and forums and
        // found no immediate information about the recommendations for threading on 
        // the server so I'm going ahead and creating a new short-lived temporary thread.
        //
        // Really, this could be easily handled by a thread pool and should be better
        // managed than simply spawning new threads.
        new Thread(new Runnable() {
            public final void run() {
        
                // These maps will be used to remember the original locations of the
                // objects in each of the distributed sets.  Because we're destructively
                // shifting the maze in order to evaluate a position, we need to be able
                // to put it back to it's normal position after each attempt.
                final Map<Comparable, Point> tWallPositionsByID     = extract(tGameObject.walls);
                final Map<Comparable, Point> tTreasurePositionsByID = extract(tGameObject.treasures);
                final Map<Comparable, Point> tGhostPositionsByID    = extract(tGameObject.ghosts);

                // This will hold a list of possible shift positions that
                // result in the player reaching the treasure.  If multiple
                // shifts are possible, one will be randomly selected.
                final List<Wall> tPreferredShifts = new ArrayList<Wall>();

                // This will hold a list of possible shift positions which
                // are used if a viable location can not be resolved.
                final List<Wall> tPossibleShifts = new ArrayList<Wall>();

                // We're going to iterate across the board and determine which 
                // squares we can insert the floating wall piece.
                for (int iY = 0; iY < LabyrinthBoardView.SIZE.height; ++iY) {
                    for (int iX = 0; iX < LabyrinthBoardView.SIZE.width; ++iX) {

                        // Skip non-moveable columns.
                        if (!LabyrinthUtil.isMoveable(iX, iY))
                            continue;

                        // Move the floating wall.
                        tGameObject.floating_wall.x = iX;
                        tGameObject.floating_wall.y = iY;

                        // For each of the possible rotations for the wall, try shifting the maze
                        // based on that orientation and if a path opens to the treasure, use it.
                        for (int iD = 0; iD < 4; ++ iD) {

                            // Rotate the wall.
                            tGameObject.floating_wall.rotate();

                            // Check to see if we can initiate a shift from this position.
                            final ShiftContext tShiftContext = ShiftContext.createContext(tGameObject, iX, iY);
                            if (tShiftContext == null)
                                continue;

                            // This will hold the <code>Path</code> that is resolved from the ghost's
                            // position to the treasure.
                            final Path tPath;

                            try {

                                // Shift the objects in the maze.
                                tShiftContext.shift(tGameObject.walls, false);
                                tShiftContext.shift(tGameObject.ghosts, true);
                                tShiftContext.shift(tGameObject.ghosts, true);

                                // Check to see if there is a path to the location of the
                                // active treasure.  We get the location of the treasure
                                // with each test because it may have been shifted by the
                                // insertion of this wall piece.
                                final Treasure tTreasure = tGameObject.treasures.get(tGameObject.active_treasure);

                                // Get the active ghost.
                                final Ghost tGhost = tGameObject.ghosts.get(iPlayerIndex);

                                // Try to find a path to the treasure.
                                tPath = Path.findPath(
                                        tGameObject.walls, 
                                        new Point(tGhost.x, tGhost.y), 
                                        new Point(tTreasure.x, tTreasure.y)
                                );

                            } finally {

                                // Restore the walls, treasures and ghosts to their pre-shifted state.
                                restore(tGameObject.walls,     tWallPositionsByID);
                                restore(tGameObject.treasures, tTreasurePositionsByID);
                                restore(tGameObject.ghosts,    tGhostPositionsByID);

                            }

                            // Copy the current floating wall into a new one.
                            final Wall tWorkingWall = new Wall(
                                    tGameObject.floating_wall.unique_id,
                                    tGameObject.floating_wall.x,
                                    tGameObject.floating_wall.y,
                                    tGameObject.floating_wall.shape,
                                    tGameObject.floating_wall.orientation
                            );

                            // If a path was not found, put this position on the list of
                            // possible shifts that will be used if no other is found.
                            if (tPath == null)
                                tPossibleShifts.add(tWorkingWall);
                            else
                                tPreferredShifts.add(tWorkingWall);

                        }

                    }

                }

                // This will hold the {@link Wall} that will be selected from either the 
                // preferred or possible lists.
                final Wall tFloatingWall;

                // Get the number of preferred positions.
                final int iPreferredShifts = tPreferredShifts.size();
                if (iPreferredShifts > 0)
                    tFloatingWall = tPreferredShifts.get((int) (Math.random() * iPreferredShifts));

                else    
                    tFloatingWall = tPossibleShifts.get((int) (Math.random() * tPossibleShifts.size()));

                // Delay a short time before making a move to give the impression of
                // thought.  Without this, the board shifts almost immediately after
                // the player makes their move which is disorienting.
                try {
                    Thread.sleep(TURN_THROTTLE);
                } catch (InterruptedException tEx) {
                    // Safely ignored.
                }
                
                // Place the wall.
                tGameManager.placeWall(iPlayerIndex, tFloatingWall);

                // Now delay a short time to allow real players participating in this game
                // the opportunity to comprehend what just happened.
                try {
                    Thread.sleep(TURN_THROTTLE);
                } catch (InterruptedException tEx) {
                    // Safely ignored.
                }

                // Check to see if there is a path to the location of the
                // active treasure.  We get the location of the treasure
                // with each test because it may have been shifted by the
                // insertion of this wall piece.
                final Treasure tTreasure = tGameObject.treasures.get(tGameObject.active_treasure);

                // Get the active ghost.
                final Ghost tGhost = tGameObject.ghosts.get(iPlayerIndex);

                // Try to find a path to the treasure.
                final Path tPath = Path.findPath(
                        tGameObject.walls, 
                        new Point(tGhost.x, tGhost.y), 
                        new Point(tTreasure.x, tTreasure.y)
                );

                // If a path was found, move the ghost along the path.
                if (tPath != null) {
                    tGhost.x = tTreasure.x;
                    tGhost.y = tTreasure.y;
                }

                // Now move the ghost into the desired position.
                tGameManager.placeGhost(iPlayerIndex, tGhost);
                
            }
        }, "AI Processing Thread").start();
        
    }
    
}
