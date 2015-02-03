//
// $Id$

package com.mpgsoft.labyrinth;

import com.samskivert.util.ListUtil;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;
import com.threerings.parlor.game.client.GameController;
import com.threerings.parlor.turn.client.TurnGameController;
import com.threerings.parlor.turn.client.TurnGameControllerDelegate;
import com.threerings.toybox.util.ToyBoxContext;
import com.threerings.util.Name;

/**
 * Manages the client side mechanics of the game.  The controller is 
 * responsible for keeping track of the state of a player's turn -
 * whether they must first place the floating wall or if it is time
 * to move their piece around the board.
 */
public final class LabyrinthController extends GameController implements TurnGameController {

    /**
     * This is the <code>TurnGameControllerDelegate which handles turn-based 
     * game related stuff.
     */
    private final TurnGameControllerDelegate mtTurnController = new TurnGameControllerDelegate(this);
    
    /** 
     * This is a reference to the interface <code>Panel</code> for the game.
     */
    private LabyrinthPanel mtLabyrinthPanel;

    /** 
     * This is a reference to the distributed game state.
     */
    private LabyrinthObject mtLabyrinthObject;
    
    /**
     * This is our player index.  It defaults to an uninitialized state until
     * the place is entered.
     */
    private int miPlayerIndex = -1;

    /**
     * Base constructor for the Labyrinth controller.
     */
    public LabyrinthController() {
        
        // Register the client-side turn controller.
        addDelegate(mtTurnController);
        
    }

    /**
     * Requests that we leave the game and return to the lobby.
     */
    public final void backToLobby() {
        _ctx.getLocationDirector().moveBack();
    }

    /**
     * Returns true if it is currently our turn and the part of the turn where the
     * player can move their ghost to another location in the maze.
     */
    final boolean canGhostBeMoved() {
        return isOurTurn() && mtLabyrinthObject.wall_placed;
    }
    
    /**
     * Returns true if it is currently our turn and the part of the turn where the
     * player must insert the floating wall into the maze.
     */
    final boolean canWallBePlaced() {
        return isOurTurn() && !mtLabyrinthObject.wall_placed;
    }

    /* (non-Javadoc)
     * @see com.threerings.crowd.client.PlaceController#createPlaceView(com.threerings.crowd.util.CrowdContext)
     */
    protected final PlaceView createPlaceView(final CrowdContext tCrowdContext) {
        
        // Instantiate a new panel for the game with the crowd context and a reference
        // to this controller.
        mtLabyrinthPanel = new LabyrinthPanel((ToyBoxContext) tCrowdContext, this);
        
        // Return the panel.
        return mtLabyrinthPanel;
    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.game.client.GameController#didLeavePlace(com.threerings.crowd.data.PlaceObject)
     */
    public final void didLeavePlace(final PlaceObject tPlaceObject) {
        super.didLeavePlace(tPlaceObject);

        // We no longer need a reference to the game object.
        mtLabyrinthObject = null;
        
    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.game.client.GameController#gameDidEnd()
     */
    protected final void gameDidEnd() {
        super.gameDidEnd();

        // Determine the message to be displayed.
        final String sMessage = !mtLabyrinthObject.isDraw() && mtLabyrinthObject.isWinner(getPlayerIndex()) ? "m.you_win" : "m.game_over";

        // Display the floating message.
        mtLabyrinthPanel.getBoard().displayFloatingText(mtLabyrinthPanel.getMessageBundle().xlate(sMessage));
        
    }
    
    /**
     * Provides package-private access to our player index.
     */
    final int getPlayerIndex() {
        return miPlayerIndex;
    }
    
    /**
     * Package-private method which notifies the server that the provided
     * <code>Ghost</code> has been placed by the client.
     */
    final void ghostPlaced(final Ghost tGhost) {
        mtLabyrinthObject.manager.invoke("placeGhost", tGhost);
    }
    
    /**
     * Returns true if it is currently our turn.
     */
    final boolean isOurTurn() {
        return mtTurnController.isOurTurn();
    }
    
    /* (non-Javadoc)
     * @see com.threerings.parlor.turn.client.TurnGameController#turnDidChange(com.threerings.util.Name)
     */
    public final void turnDidChange(final Name tTurnHolder) { }

    /**
     * Package-private callback indicating the designated <code>Wall</code> piece
     * has been placed by the player.
     */
    final void wallPlaced(final Wall tWall) {
        mtLabyrinthObject.manager.invoke("placeWall", tWall);
    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.game.client.GameController#willEnterPlace(com.threerings.crowd.data.PlaceObject)
     */
    public final void willEnterPlace(final PlaceObject tPlaceObject) {
        super.willEnterPlace(tPlaceObject);

        // Cache a casted reference to the Labyrinth distributed state.
        mtLabyrinthObject = (LabyrinthObject) tPlaceObject;
        
        // Get our player index.
        miPlayerIndex = ListUtil.indexOf(mtLabyrinthObject.players, ((ToyBoxContext) _ctx).getUsername());
        
    }
}
