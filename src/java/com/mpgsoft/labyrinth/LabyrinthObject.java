//
// $Id$

package com.mpgsoft.labyrinth;

import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.turn.data.TurnGameObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.util.Name;

/**
 * Maintains the shared state of the game.
 */
public class LabyrinthObject extends GameObject implements TurnGameObject {

    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>turnHolder</code> field. */
    public static final String TURN_HOLDER = "turnHolder";

    /** The field name of the <code>active_treasure</code> field. */
    public static final String ACTIVE_TREASURE = "active_treasure";

    /** The field name of the <code>wall_placed</code> field. */
    public static final String WALL_PLACED = "wall_placed";

    /** The field name of the <code>floating_wall</code> field. */
    public static final String FLOATING_WALL = "floating_wall";

    /** The field name of the <code>floating_wall_origin</code> field. */
    public static final String FLOATING_WALL_ORIGIN = "floating_wall_origin";

    /** The field name of the <code>walls</code> field. */
    public static final String WALLS = "walls";

    /** The field name of the <code>treasures</code> field. */
    public static final String TREASURES = "treasures";

    /** The field name of the <code>ghosts</code> field. */
    public static final String GHOSTS = "ghosts";
    // AUTO-GENERATED: FIELDS END

    /**
     * This is the <code>Name</code> of the current player.
     */
    public Name turnHolder;

    /**
     * This is the index of the active treasure that is currently being sought
     * by all players. The first player to move their piece onto the same 
     * square as this treasure acquires it and it leaves the board.  When there 
     * are no more treasures left to acquire, the game is over.
     */
    public int active_treasure = -1;
    
    /**
     * Each turn of Labyrinth consists of two parts: 1) shift the floating wall
     * piece into the maze at one of the possible locations and then 2) move the
     * corresponding player piece to a new position in the maze.  When true,
     * this boolean indicates that the player has completed the first step and
     * modified the maze.
     */
    public boolean wall_placed;
    
    /**
     * This is the moveable <code>Wall</code> that can be shifted into the maze
     * by the current turn holder.  After the game is initialized, this will 
     * never be null.
     */
    public Wall floating_wall;
    
    /**
     * This is the position from which the floating wall piece originated.  Per
     * the rules, the floating wall can not be inserted at this position.  To
     * prevent multiple updates, this integer value is calculated as 
     * <code>y * columns + x</code>.
     */
    public int floating_wall_origin = 0;

    /**
     * This is the distributed set of <code>Wall</code> objects representing the
     * labyrinth layout at any given moment.
     */
    public DSet<Wall> walls = new DSet<Wall>();

    /**
     * This is the distributed set of <code>Treasure</code> objects left 
     * in play at the current moment.
     */
    public DSet<Treasure> treasures = new DSet<Treasure>();

    /**
     * This is the distributed set of <code>Piece</code> objects representing
     * the players' positions in the labyrinth.
     */
    public DSet<Ghost> ghosts = new DSet<Ghost>();

    /* (non-Javadoc)
     * @see com.threerings.parlor.turn.data.TurnGameObject#getPlayers()
     */
    public final Name[] getPlayers() {
        return players;
    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.turn.data.TurnGameObject#getTurnHolder()
     */
    public final Name getTurnHolder() {
        return turnHolder;
    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.turn.data.TurnGameObject#getTurnHolderFieldName()
     */
    public final String getTurnHolderFieldName() {
        return TURN_HOLDER;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>turnHolder</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTurnHolder (Name value)
    {
        Name ovalue = this.turnHolder;
        requestAttributeChange(
            TURN_HOLDER, value, ovalue);
        this.turnHolder = value;
    }

    /**
     * Requests that the <code>active_treasure</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setActive_treasure (int value)
    {
        int ovalue = this.active_treasure;
        requestAttributeChange(
            ACTIVE_TREASURE, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.active_treasure = value;
    }

    /**
     * Requests that the <code>wall_placed</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setWall_placed (boolean value)
    {
        boolean ovalue = this.wall_placed;
        requestAttributeChange(
            WALL_PLACED, Boolean.valueOf(value), Boolean.valueOf(ovalue));
        this.wall_placed = value;
    }

    /**
     * Requests that the <code>floating_wall</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setFloating_wall (Wall value)
    {
        Wall ovalue = this.floating_wall;
        requestAttributeChange(
            FLOATING_WALL, value, ovalue);
        this.floating_wall = value;
    }

    /**
     * Requests that the <code>floating_wall_origin</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setFloating_wall_origin (int value)
    {
        int ovalue = this.floating_wall_origin;
        requestAttributeChange(
            FLOATING_WALL_ORIGIN, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.floating_wall_origin = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>walls</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToWalls (Wall elem)
    {
        requestEntryAdd(WALLS, walls, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>walls</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromWalls (Comparable key)
    {
        requestEntryRemove(WALLS, walls, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>walls</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateWalls (Wall elem)
    {
        requestEntryUpdate(WALLS, walls, elem);
    }

    /**
     * Requests that the <code>walls</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setWalls (DSet<com.mpgsoft.labyrinth.Wall> value)
    {
        requestAttributeChange(WALLS, value, this.walls);
        @SuppressWarnings("unchecked") DSet<com.mpgsoft.labyrinth.Wall> clone =
            (value == null) ? null : value.typedClone();
        this.walls = clone;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>treasures</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToTreasures (Treasure elem)
    {
        requestEntryAdd(TREASURES, treasures, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>treasures</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromTreasures (Comparable key)
    {
        requestEntryRemove(TREASURES, treasures, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>treasures</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateTreasures (Treasure elem)
    {
        requestEntryUpdate(TREASURES, treasures, elem);
    }

    /**
     * Requests that the <code>treasures</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setTreasures (DSet<com.mpgsoft.labyrinth.Treasure> value)
    {
        requestAttributeChange(TREASURES, value, this.treasures);
        @SuppressWarnings("unchecked") DSet<com.mpgsoft.labyrinth.Treasure> clone =
            (value == null) ? null : value.typedClone();
        this.treasures = clone;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>ghosts</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToGhosts (Ghost elem)
    {
        requestEntryAdd(GHOSTS, ghosts, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>ghosts</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromGhosts (Comparable key)
    {
        requestEntryRemove(GHOSTS, ghosts, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>ghosts</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateGhosts (Ghost elem)
    {
        requestEntryUpdate(GHOSTS, ghosts, elem);
    }

    /**
     * Requests that the <code>ghosts</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setGhosts (DSet<com.mpgsoft.labyrinth.Ghost> value)
    {
        requestAttributeChange(GHOSTS, value, this.ghosts);
        @SuppressWarnings("unchecked") DSet<com.mpgsoft.labyrinth.Ghost> clone =
            (value == null) ? null : value.typedClone();
        this.ghosts = clone;
    }
    // AUTO-GENERATED: METHODS END
}
