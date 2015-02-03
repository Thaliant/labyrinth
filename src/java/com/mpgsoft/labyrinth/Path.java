//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import com.threerings.presents.dobj.DSet;
import com.threerings.util.DirectionCodes;

/**
 * This class provides pathfinding between two <code>Point</code>s within the
 * maze.  The factory method <code>findPath()</code> is the only way to construct
 * a new {@link Path} - if a path can be successfully resolved given the current
 * orientation of the walls in the maze.
 * <p>
 * It is fairly inexpensive to pathfind in the current maze so it is not necessary
 * to cache paths.  They can be requested as needed.
 * 
 * @author Jeffrey D. Hoffman
 */
final class Path {

    /**
     * This is a private array of <code>Point</code> representing grid offsets 
     * corresponding to the defined <code>DirectionCodes</code>.  Like  
     */
    private static final Point[] DIRECTION_OFFSETS = new Point[DirectionCodes.DIRECTION_COUNT];
    static {
        DIRECTION_OFFSETS[DirectionCodes.NORTH] = new Point(0, -1);
        DIRECTION_OFFSETS[DirectionCodes.SOUTH] = new Point(0, 1);
        DIRECTION_OFFSETS[DirectionCodes.WEST]  = new Point(-1, 0);
        DIRECTION_OFFSETS[DirectionCodes.EAST]  = new Point(1, 0);
    }                                                               
    
    /**
     * This is the list of <code>Point</code>s through which this path passes.
     * It does not include the origin.
     */
    private final LinkedList<Point> mtPoints = new LinkedList<Point>();
        
    /**
     * Private <code>Path</code> constructor called from the static factory method.
     */
    private Path(final Point tPoint) { 
        mtPoints.add(tPoint);
    }
            
    /**
     * Returns the first (origin) <code>Point</code> of this path.
     */
    final Point getFirst() {
        return mtPoints.getFirst();
    }
    
    /**
     * Returns the last (destination) <code>Point</code> of this path.
     */
    final Point getLast() {
        return mtPoints.getLast();
    }
    
    /**
     * Returns an <code>Iterator</code> of the <code>Point</code>s in the path.
     */
    final Iterator<Point> iterator() {
        return mtPoints.iterator();
    }
    
    /**
     * Static factory method that resolves a path between two <code>Point</code>s 
     * based on the orientation of the <code>Wall</code>s between the two locations.  
     * If passage is possible, a <code>Path</code> is created and returned.  Otherwise
     * null is returned indicating that it is not possible to reach that point.
     */
    static final Path findPath(final DSet<Wall> tWalls, final Point tStart, final Point tDestination) {
                
        // Recursively find a path if possible.
        return findPath(tWalls, tStart, tDestination, new HashSet<Point>());        
    }
    
    /**
     * This is the recursive pathfinding loop that, given a point, finds all possible
     * exits and explores those in an attempt to reach the destination.
     */
    private static Path findPath(final DSet<Wall> tWalls, final Point tWayPoint, final Point tDestination, final Set<Point> tVisitedPoints) {
        
        // Quick abort if we have already visited this point or if this point 
        // falls off of the board.
        if (tVisitedPoints.contains(tWayPoint) || LabyrinthUtil.isSurrounding(tWayPoint.x, tWayPoint.y))
            return null;
        
        // Add this point to the list of visited points.
        tVisitedPoints.add(tWayPoint);
        
        // If this is the destination point, we've reached the end and can create
        // a new path.
        if (tWayPoint.equals(tDestination))
            return new Path(tDestination);
            
        // Otherwise, get the wall segment that is located at this position.  This should
        // never return null seeing as we're legally on the board but it doesn't hurt to
        // check.
        final Wall tWall = getWall(tWalls, tWayPoint);
        if (tWall == null)
            return null;
        
        // This is the working point that will be used while processing directions.
        final Point tWorkingPoint = new Point();
        
        // Get the list of exit directions from this position.  Step through each exit
        // find the corresponding adject wall, check to see if it supports entry from
        // that direction and recursively check for a path.
        final int[] aiExits = tWall.getExits();
        for (int iExit = 0; iExit < aiExits.length; ++iExit) {
            final int iDirection = aiExits[iExit];
            
            // Update the working point based on the offset.
            tWorkingPoint.x = tWayPoint.x + DIRECTION_OFFSETS[iDirection].x;
            tWorkingPoint.y = tWayPoint.y + DIRECTION_OFFSETS[iDirection].y;
            
            // Get the wall that corresponds to this point.  It's possible for this
            // new point to be off of the board so <code>getWall()</code> may return
            // null.  In that case, skip this direction.  Alternatively, the wall
            // may be oriented such that you can not enter it from this direction.
            final Wall tNextWall = getWall(tWalls, tWorkingPoint);
            if (tNextWall == null || !tNextWall.canBeEnteredFrom(iDirection))
                continue;
            
            // Explore in this direction.  If this returns a <code>Path</code> (indicating
            // that the destination is reachable in this direction) insert <i>this</i> 
            // waypoint at the beginning of the path and return it.
            final Path tPath = findPath(tWalls, tWorkingPoint, tDestination, tVisitedPoints);
            if (tPath != null) {
                
                // Insert this waypoint.
                tPath.mtPoints.addFirst(tWayPoint);
                
                // Return the path.
                return tPath;
                
            }
            
        }        
        
        // The destination can not be reached through this waypoint.
        return null;
    }
    
    /**
     * This is a private method which scans the list of <code>Wall</code> objects to
     * find the one at a given point.
     */
    private static final Wall getWall(final DSet<Wall> tWalls, final Point tPoint) {
        
        // Verify that the point being requested is within bounds.
        if (!LabyrinthUtil.isSurrounding(tPoint.x, tPoint.y)) {
        
            // Iterate through the walls until we find the one that matches this point.
            for (Wall tWall : tWalls) {
                if (tWall.x == tPoint.x && tWall.y == tPoint.y)
                    return tWall;
            }
            
        }
        
        // No wall matches this point.
        return null;
    }
    
    /**
     * Returns the size of the path.
     */
    final int size() {
        return mtPoints.size();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        
        // This will buffer the path as it is created.
        final StringBuilder tBuffer = new StringBuilder();
        
        // Get the number of points on the path.
        final int iPoints = size();
        
        // Step through the list of points and display them with delimiters.
        for (int iIndex = 0; iIndex < iPoints; ++iIndex) {
            final Point tPoint = mtPoints.get(iIndex);
            
            // Append a delimiter if necessary.
            if (iIndex > 0)
                tBuffer.append(" - ");
            
            // Append the point.
            tBuffer.append(tPoint.x).append(',').append(tPoint.y);
            
        }
        
        return tBuffer.toString();
    }
    
}
