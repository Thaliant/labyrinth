//
// $Id$

package com.mpgsoft.labyrinth;

import com.samskivert.util.RandomUtil;
import com.threerings.util.DirectionCodes;

/**
 * This class defines a wall tile within the maze.  A wall is a shape 
 * (which describes the what directions can be traveled from a given square) 
 * with an orientation (which was selected by the player at the time of 
 * insertion into the maze).
 * 
 * @author Jeffrey D. Hoffman
 */
public final class Wall extends Piece {

    /**
     * Static constants defining the possible shapes for tiles.
     */
    public static final int STRAIGHT = 0;
    public static final int L_SHAPE  = 1;
    public static final int T_SHAPE  = 2;

    /**
     * This is a private array of directions used when randomly selecting
     * an orientation for this wall.
     * @see Wall#Wall(int, int)
     */
    private static final int[] ORIENTATIONS = new int[] {
        DirectionCodes.NORTH, DirectionCodes.EAST, DirectionCodes.SOUTH, DirectionCodes.WEST 
    };
    
    /**
     * This is a private array of exits that are possible from each of the
     * shape types.
     */
    private static final int[][] EXITS = new int[3][];
    /**
     * Private static constants used to improve the readability of the
     * paramters passed to <code>rotate(int, int)</code>.
     */
    private static final int DEGREES_90  = 1;
    
    private static final int DEGREES_180 = 2;
    static {
        EXITS[STRAIGHT] = new int[] { DirectionCodes.NORTH, DirectionCodes.SOUTH };
        EXITS[L_SHAPE]  = new int[] { DirectionCodes.NORTH, DirectionCodes.EAST };
        EXITS[T_SHAPE]  = new int[] { DirectionCodes.NORTH, DirectionCodes.EAST, DirectionCodes.WEST };        
    }
    
    /**
     * This is the index of the shape this tile corresponds to.
     */
    public int shape;

    /**
     * This is the tile's orientation.
     */
    public int orientation;

    /**
     * Empty constructor per <code>DSet.Entry</code> requirements.
     */
    public Wall() {}
    
    /**
     * Convenience constructor used during game setup that creates a new  
     * wall shape 
     */
    Wall(final int iUniqueID, final int iShape) {
        this(iUniqueID, 0, 0, iShape, ORIENTATIONS[RandomUtil.getInt(ORIENTATIONS.length)]);
    }
    
    /**
     * Convenience constructor used during game setup.
     */
    Wall(final int iUniqueID, final int iX, final int iY, final int iShape, final int iOrientation) {
        unique_id = iUniqueID;
        x = iX;
        y = iY;
        shape = iShape;
        orientation = iOrientation;
    }

    /**
     * Returns true if this wall segment can be entered from the designated 
     * direction based on it's shape and orientation.
     */
    final boolean canBeEnteredFrom(int iDirection) {
        
        // If we're entering from this direction, we need to look for an
        // exit in the opposite direction.
        iDirection = rotate(iDirection, DEGREES_180);
        
        // Get the list of exits.
        final int[] aiExits = getExits();
        
        // Step through the list of exits and see if this direction is among them.
        for (int iIndex = 0; iIndex < aiExits.length; ++iIndex) {
            if (iDirection == aiExits[iIndex])
                return true;
        }

        // You can not enter this tile from that direction.
        return false;
    }

    /**
     * Returns an array of the directions (e.g. <code>DirectionCodes.NORTH, 
     * DirectionCodes.EAST</code>) that are possible exits from this tile 
     * based on it's shape and orientation.  
     */
    final int[] getExits() {

        // Get the list of unrotated exists.
        final int[] aiExits = EXITS[shape];

        // This will hold the list of exits.
        final int[] aiRelativeExits;
        
        // If we're oriented NORTH, it is unnecessary to do any array manipulation.
        if (orientation == DirectionCodes.NORTH)
            aiRelativeExits = aiExits;
        
        // Otherwise, we need to rotate the exits based on the current orientation
        // of the tile.
        else {
            
            // Create an array to hold the rotated exits.
            aiRelativeExits = new int[aiExits.length];
            
            // Get the current orientation index.  This value represents the 
            // number of rotations that need to be performed to each direction.
            final int iOrientationIndex = getOrientationIndex(orientation);
            
            // Step through the list of exits and rotate them based on the 
            // orientation of this shape.
            for (int iIndex = 0; iIndex < aiRelativeExits.length; ++iIndex)
                aiRelativeExits[iIndex] = rotate(aiExits[iIndex], iOrientationIndex);
                        
        }
        
        return aiRelativeExits;        
    }
    
    /**
     * Rotates the wall 90-degrees clockwise.
     */
    final void rotate() {
        
        // Rotate the wall by one 90-degree increment.
        orientation = rotate(orientation, DEGREES_90);
        
    }
    
    /**
     * Resolves the index into the <code>ORIENTATIONS</code> array for the 
     * specified direction.
     */
    private static final int getOrientationIndex(final int iOrientation) {
        
        // This will hold the current orientation's index in the array.
        int iOrientationIndex = 0;
        
        // Step through the list of possible directions and find the one that 
        // matches the current orientation.
        for (int iIndex = 0; iIndex < ORIENTATIONS.length; ++iIndex) {
            if (ORIENTATIONS[iIndex] == iOrientation) {
                iOrientationIndex = iIndex;
                break;
            }
        }

        return iOrientationIndex;
    }
    
    /**
     * Convenience method which rotates the specified orientation by 90-degrees
     * increments and returns the new orientation.  For example, a rotation
     * of two (180-degrees) produces the opposite direction provided.
     */
    private static final int rotate(final int iOrientation, final int iRotation) {
        
        // This will hold the current orientation's index in the array.
        int iOrientationIndex = getOrientationIndex(iOrientation);
                                
        // Increment the orientation and MOD it by the possible orientations
        // to ensure that it wraps properly.
        iOrientationIndex = (iOrientationIndex + iRotation) % ORIENTATIONS.length;
        
        // Return the adjusted orientation.
        return ORIENTATIONS[iOrientationIndex];

    }
    
}