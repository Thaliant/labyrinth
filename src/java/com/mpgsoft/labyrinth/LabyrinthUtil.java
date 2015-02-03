//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.samskivert.util.RandomUtil;
import com.threerings.presents.dobj.DSet;

/**
 * The obligitory collection of utility functions.
 * 
 * @author Jeffrey D. Hoffman
 */
final class LabyrinthUtil {
   
    /**
     * This class can not be instantiated.
     */
    private LabyrinthUtil() { }
    
    /**
     * Computes the location index from the provided x- and y-position.
     */
    static final int getLocation(final int iX, final int iY) {
        return iX + (iY * 5);
    }
    
    /**
     * Retrieves a list of the pieces that are located at the designated
     * location.
     */
    static final List<Piece> getPieces(final DSet<Piece> tAllPieces, final Point tLocation) {
        
        // This is the list that will be returned, if any. 
        final List<Piece> tPieces = new ArrayList<Piece>(2);
        
        // Iterate through the distributed set - if any of the pieces are
        // located at this point, add it to the list.
        for (Piece tPiece : tAllPieces) {
            if (tPiece.x == tLocation.x && tPiece.y == tLocation.y)
                tPieces.add(tPiece);
        }
        
        return tPieces;
    }
    
    /**
     * Returns an integer array representing the number of points each of
     * the players in the game has.
     */
    static final int[] getPoints(final LabyrinthObject tLabyrinthObject) {
        
        // Allocate an array based on the player list.
        final int[] aiPoints = new int[tLabyrinthObject.players.length];
        
        // Iterate through the treasure list and for each collected 
        // treasure, increment the points corresponding to the owner.
        for (Treasure tTreasure : tLabyrinthObject.treasures) {
            if (tTreasure.isCollected())
                aiPoints[tTreasure.owner]++;
        }
        
        return aiPoints;
    }
    
    /**
     * Generates a random <code>Point</code> anywhere on the playable board.
     */
    static final Point getRandomLocation() {
        return new Point(RandomUtil.getInt(6, 0), RandomUtil.getInt(6, 0));
    }
    
    /**
     * Generates a random <code>Point</code> on the playable board excluding
     * any points already in the provided <code>Set</code>.  This is not 
     * necessarily an efficient implementation if the exclusion set is
     * large.
     */
    static final Point getRandomLocation(final Set<Point> tExclusions, final boolean bAddPoint) {
        
        // We'll try a reasonable number of times to find an exclusive
        // random point - but we won't allow an infinite loop.
        int iTries = 0;
        
        // This will hold the random point.
        Point tPoint;
        
        do {
            
            // Randomly choose a point.
            tPoint = getRandomLocation();
            
        } while (++iTries < 50 && tExclusions.contains(tPoint)); 

        // If the caller desires the new point to be added to those that are
        // excluded from future calls, add it.
        if (bAddPoint)
            tExclusions.add(tPoint);
        
        return tPoint;
    }
    
    /**
     * Convenience method which returns true if the specified row or column is
     * a valid insertion point.
     */
    static final boolean isMoveable(final int iLocation) {
        return iLocation == 2 || iLocation == 4;
    }
    
    /**
     * Convenience method whcih returns true if the specified row and column is
     * a valid insertion point.
     */
    static final boolean isMoveable(final int iX, final int iY) {
        return (isSurrounding(iX) && isMoveable(iY)) || (isSurrounding(iY) && isMoveable(iX));
    }
        
    /**
     * Returns true if the specified location falls on the outside bounds of
     * the board.
     */
    private static final boolean isSurrounding(final int iRowOrColumn) {
        return iRowOrColumn == 0 || iRowOrColumn == 6;
    }
    
    
    /**
     * Returns true if the specified position falls on the outside bounds of 
     * the map.  This is the area reserved for wall insertion.
     */
    static final boolean isSurrounding(final int iX, final int iY) {
        return isSurrounding(iX) || isSurrounding(iY); 
    }
    
    
}
