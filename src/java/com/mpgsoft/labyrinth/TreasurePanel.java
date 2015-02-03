//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetAdapter;
import com.threerings.presents.dobj.SetListener;
import com.threerings.toybox.client.ToyBoxUI;

/**
 * @author Jeffrey D. Hoffman
 */
final class TreasurePanel extends JPanel implements PlaceView {
    
    /**
     * This static constant defines the dimension of an individual icon on 
     * the panel.  Treasure images are painted in a cropped fashion because
     * most of the image is transparent.
     */
    private static final Dimension IMAGE_DIMENSION = new Dimension(LabyrinthSprite.SIZE / 2, LabyrinthSprite.SIZE / 2);
    
    /**
     * This defines the desired default size of the treasure panel.  It will
     * be at least this size.
     */
    private static final Dimension PANEL_DIMENSION = new Dimension(LabyrinthSprite.SIZE, Math.round(LabyrinthSprite.SIZE * 2.5f));

    /**
     * This is the set of the identities of the treasures that have been picked-up
     * by the current player.  When an identity is not already in this set a new
     * {@link TreasureLabel} is added to this panel.
     */
    private final Set<Comparable> mtTreasureIDs = new HashSet<Comparable>();
    
    /**
     * This is a reference to the <code>GameController</code> that provides
     * information about the state of the game.
     */
    private final LabyrinthController mtLabyrinthController;

    /**
     * This is a reference to the distributed game object that this class
     * listens to for changes in treasure ownership.
     */
    private LabyrinthObject mtLabyrinthObject = null;
    
    /**
     * Instantiates the panel that displays the list of treasures acquired
     * by the current player.
     */
    TreasurePanel(final LabyrinthController tGameController) {
        mtLabyrinthController = tGameController;
                
        // We need to set it in a bunch of spots to get the desired effect.
        setMinimumSize(PANEL_DIMENSION);
        setPreferredSize(PANEL_DIMENSION);        
        setSize(PANEL_DIMENSION);
        
        // Install a new flow layout.
        setLayout(new FlowLayout(FlowLayout.LEFT, 3, 3));
        
    }
    
    /* (non-Javadoc)
     * @see com.threerings.crowd.client.PlaceView#didLeavePlace(com.threerings.crowd.data.PlaceObject)
     */
    public final void didLeavePlace(final PlaceObject tPlaceObject) {

        // Stop listening and dispose the reference.
        mtLabyrinthObject.removeListener(mtSetListener);
        mtLabyrinthObject = null;
        
    }

    /* (non-Javadoc)
     * @see com.threerings.crowd.client.PlaceView#willEnterPlace(com.threerings.crowd.data.PlaceObject)
     */
    public final void willEnterPlace(final PlaceObject tPlaceObject) {

        // Store a reference to the distributed object and start listening for set changes.
        mtLabyrinthObject = (LabyrinthObject) tPlaceObject;
        mtLabyrinthObject.addListener(mtSetListener);
        
    }

    /**
     * This is the private <code>SetListener</code> that responds to changes
     * in the {@link Treasure} distributed set.
     */
    private final SetListener mtSetListener = new SetAdapter() {
        public final void entryUpdated(final EntryUpdatedEvent tEvent) {
            
            // We're only interested in updates to treasure.
            if (!LabyrinthObject.TREASURES.equals(tEvent.getName()))
                return;
                
            // Get the current player index.
            final int iPlayerIndex = mtLabyrinthController.getPlayerIndex();
            
            // True if new treasure was found.
            boolean bTreasureCollected = false;
            
            // Iterate through the treasures and see if there are any new
            // treasures that have been acquired by the player.  Technically
            // there should never be more than one treasure acquired in any
            // given turn but we'll iterate through them all just to be safe.
            for (Treasure tTreasure : mtLabyrinthObject.treasures) {
                if (tTreasure.owner == iPlayerIndex && !mtTreasureIDs.contains(tTreasure.unique_id)) {
                    
                    // Remember that we've created this treasure.
                    mtTreasureIDs.add(tTreasure.unique_id);
                    
                    // Add a new label to the panel.
                    add(new TreasureLabel(tTreasure.unique_id));
                    
                    // Remember that we need a repaint.
                    bTreasureCollected = true;
                    
                }

            }
            
            // If new treasure was found, we need to repaint.
            if (bTreasureCollected)
                repaint();
            
        }
    };

    /**
     * This is the private <code>JLabel</code> that will display an individual
     * treasure's image.
     */
    private final class TreasureLabel extends JLabel {
        
        /**
         * Static constant defining the insets of the background.
         */
        private static final int INSETS = 2;
        
        /**
         * This is the unique identity of the treasure being displayed.
         */
        private final int miTreasureID;
        
        /**
         * Instantiates a new label for this treasure.
         */
        private TreasureLabel(final int iTreasureID) {
            miTreasureID = iTreasureID;
            
            // Install the desired size for this label.
            setMaximumSize(IMAGE_DIMENSION);
            setMinimumSize(IMAGE_DIMENSION);
            setPreferredSize(IMAGE_DIMENSION);
            setSize(IMAGE_DIMENSION);
            
            // Set the background color.
            setBorder(BorderFactory.createEtchedBorder());
                        
        }

        /* (non-Javadoc)
         * @see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        public final void paint(final Graphics tGraphics) {
            super.paint(tGraphics);
                                    
            // Get the dimensions of the label.
            final Dimension tSize = getSize();
                        
            // Fill the background of the treasure box.
            tGraphics.setColor(ToyBoxUI.LIGHT_BLUE);
            tGraphics.fillRect(INSETS, INSETS, tSize.width - INSETS * 2, tSize.height - INSETS * 2);
            
            // Get the image for this treasure.
            final Image tImage = TreasureSprite.TREASURE_TILES.getRawTileImage(miTreasureID);

            // Draw the image at a negative offset so the treasure is the
            // focus of the label.
            tGraphics.drawImage(tImage, -IMAGE_DIMENSION.width / 2, -IMAGE_DIMENSION.height / 2, null);

        }
        
    }
    
}
