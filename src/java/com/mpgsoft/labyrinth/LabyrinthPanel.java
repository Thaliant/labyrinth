//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import com.samskivert.swing.Controller;
import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.MultiLineLabel;
import com.samskivert.swing.ShapeIcon;
import com.threerings.crowd.client.PlacePanel;
import com.threerings.media.image.ImageManager;
import com.threerings.media.tile.TileManager;
import com.threerings.parlor.turn.client.TurnDisplay;
import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.client.ToyBoxUI;
import com.threerings.toybox.util.ToyBoxContext;
import com.threerings.util.MessageBundle;

/**
 * Contains the primary client interface for the game.
 */
public final class LabyrinthPanel extends PlacePanel {

    /** 
     * Provides access to various client services. 
     */
    private final ToyBoxContext mtToyBoxContext;

    /** 
     * This is the Labyrinth board view which renders the current state of the
     * board for the player.
     */
    private final LabyrinthBoardView mtLabyrinthBoard;
    
    /**
     * This is a reference to the <code>MessageBundle</code> that is used
     * to retrieve messages for this game.
     */
    private final MessageBundle mtMessageBundle;

    /**
     * Creates a Labyrinth panel and its associated interface components.
     */
    public LabyrinthPanel(final ToyBoxContext tToyBoxContext, final LabyrinthController tGameController) {
        super(tGameController);
        
        // Store the context.
        mtToyBoxContext = tToyBoxContext;
        
        // Instantiate the board view.
        mtLabyrinthBoard = new LabyrinthBoardView(tToyBoxContext, tGameController);

        // Instantiate a panel that will hold the gameboard and put a nice 
        // border around it.
        final JPanel tBoardBorder = new JPanel();
        tBoardBorder.setOpaque(false);
        tBoardBorder.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        tBoardBorder.setLayout(new BorderLayout());
        tBoardBorder.add(mtLabyrinthBoard, BorderLayout.CENTER);
        
        // this is used to look up localized strings
        mtMessageBundle = mtToyBoxContext.getMessageManager().getBundle("labyrinth");

        // give ourselves a wee bit of a border
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());

        // This panel will hold the board, centered within it.
        final JPanel tBoardBox = GroupLayout.makeHBox();
        tBoardBox.add(tBoardBorder);
        tBoardBox.setOpaque(false);
        add(tBoardBox, BorderLayout.CENTER);
        
        // create a side panel to hold our chat and other extra interfaces
        final JPanel tSidePanel = GroupLayout.makeVStretchBox(5);
        tSidePanel.setOpaque(false);

        // Add the game's name as a fancy label.
        final MultiLineLabel tTitleLabel = new MultiLineLabel(mtMessageBundle.xlate("m.title"));
        tTitleLabel.setAntiAliased(true);
        tTitleLabel.setFont(ToyBoxUI.fancyFont);
        tTitleLabel.setForeground(Color.black);
        tSidePanel.add(tTitleLabel, GroupLayout.FIXED);        

        // These shapes will be used by the turn display.
        final Ellipse2D tElipse = new Ellipse2D.Float(0, 0, 12, 12);
        final Polygon tTriangle = new Polygon(new int[] { 0, 12, 0 }, new int[] { 0, 6, 12 }, 3);

        // Build the array of player icons.
        final Icon[] atPlayerIcons = new Icon[Ghost.COLORS.length];
        for (int iIndex = 0; iIndex < Ghost.COLORS.length; ++iIndex)
            atPlayerIcons[iIndex] = new ShapeIcon(tElipse, Ghost.COLORS[iIndex], null);        
        
        // Create a <code>TurnDisplay</code>.
        final TurnDisplay tTurnDisplay = new ScoreDisplay();
        tTurnDisplay.setOpaque(false);
        tTurnDisplay.setTurnIcon(new ShapeIcon(tTriangle, Color.orange, null));
        tTurnDisplay.setWinnerText(mtMessageBundle.xlate("m.winner"));
        tTurnDisplay.setDrawText(mtMessageBundle.get("m.draw"));        
        tTurnDisplay.setPlayerIcons(atPlayerIcons);
        
        // Add the turn display.
        tSidePanel.add(tTurnDisplay, GroupLayout.FIXED);
                       
        // Create a panel to display the player's loot.
        final TreasurePanel tTreasurePanel = new TreasurePanel(tGameController);
        tTreasurePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                " " + mtMessageBundle.xlate("m.loot") + " "
        ));
        
        // Add a panel that displays the player's loot.
        tSidePanel.add(tTreasurePanel, GroupLayout.FIXED);
        
        // add a chat box
        tSidePanel.add(new ChatPanel(tToyBoxContext));

        // add a "back to lobby" button
        final JButton back = Controller.createActionButton(mtMessageBundle.get("m.back_to_lobby"), "backToLobby");
        tSidePanel.add(back, GroupLayout.FIXED);

        // add our side panel to the main display
        add(tSidePanel, BorderLayout.EAST);
        
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#addNotify()
     */
    public final void addNotify() {
        super.addNotify();

        // An <code>ImageManager</code> will be responsible for loading the graphics
        // necessary for the walls, treasures and players.  Apparently the root
        // pane is necessary for the image manager - so this is done after the
        // panel has been added to a root frame.
        final ImageManager tImageManager = new ImageManager(mtToyBoxContext.getResourceManager(), getRootPane());
        
        // Now we can create the <code>TileManager</code>
        final TileManager tTileManager = new TileManager(tImageManager);
        
        // Initialize the wall, player and treasure tiles by way of the tile manager.
        WallSprite.WALL_TILES         = tTileManager.loadTileSet("media/walls.png", LabyrinthSprite.SIZE, LabyrinthSprite.SIZE);
        GhostSprite.GHOST_TILES       = tTileManager.loadTileSet("media/ghosts.png", LabyrinthSprite.SIZE, LabyrinthSprite.SIZE);
        GhostSprite.CURSOR_TILES      = tTileManager.loadTileSet("media/cursor.png", LabyrinthSprite.SIZE, LabyrinthSprite.SIZE);
        TreasureSprite.TREASURE_TILES = tTileManager.loadTileSet("media/treasures.png", LabyrinthSprite.SIZE, LabyrinthSprite.SIZE);
        TreasureSprite.SPARKLE_TILES  = tTileManager.loadTileSet("media/sparkle.png", LabyrinthSprite.SIZE, LabyrinthSprite.SIZE);
                
    }
    
    /**
     * Provides package-private access to the board view.
     */
    final LabyrinthBoardView getBoard() {
        return mtLabyrinthBoard;
    }
    
    /**
     * Provides package-private access to the <code>MessageBundle</code>.
     */
    final MessageBundle getMessageBundle() {
        return mtMessageBundle;
    }
    
}
