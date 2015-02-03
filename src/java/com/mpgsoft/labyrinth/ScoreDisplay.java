//
// $Id$

package com.mpgsoft.labyrinth;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;

import com.samskivert.swing.util.SwingUtil;
import com.threerings.parlor.turn.client.TurnDisplay;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.util.Name;

/**
 * This is a hastily implemented extension of the <code>TurnDisplay</code>
 * object with the addition of player score.
 * 
 * @author Jeffrey D. Hoffman
 */
final class ScoreDisplay extends TurnDisplay {

    /* (non-Javadoc)
     * @see com.threerings.parlor.turn.client.TurnDisplay#attributeChanged(com.threerings.presents.dobj.AttributeChangedEvent)
     */
    public final void attributeChanged(final AttributeChangedEvent tEvent) {
        if (LabyrinthObject.ACTIVE_TREASURE.equals(tEvent.getName()))
            createList();
        else
            super.attributeChanged(tEvent);
    }

    /* (non-Javadoc)
     * @see com.threerings.parlor.turn.client.TurnDisplay#createList()
     */
    protected final void createList() {
        
        // @TODO [jhoffman 07/08/07]: In the spirit of the 20 hour challenge, I 
        // am running out of time and overrode <code>TurnDisplay</code>'s 
        // list creation method to add scoring.  This code needs cleanup
        // and documentation...
        
        removeAll();
        _labels.clear();

        final GridBagLayout tLayout = new GridBagLayout();
        setLayout(tLayout);

        final GridBagConstraints tLayoutConstraints = new GridBagConstraints();

        final Name[] atNames = _turnObj.getPlayers();
        
        final LabyrinthObject tGameObject = (LabyrinthObject)_turnObj;
        
        // Get the points for the players.
        final int[] aiPoints = LabyrinthUtil.getPoints(tGameObject);
        
        final boolean[] abWinners = tGameObject.winners;
        
        final Name tTurnHolder = _turnObj.getTurnHolder();
        
        for (int ii=0, jj=0; ii < atNames.length; ii++, jj++) {
            if (atNames[ii] == null) 
                continue;
                        
            final JLabel tIconLabel = new JLabel();
            if (abWinners == null) {
                if (atNames[ii].equals(tTurnHolder)) {
                    tIconLabel.setIcon(_turnIcon);
                }
            } else if (tGameObject.isDraw()) {
                tIconLabel.setText(_drawText);
            } else if (abWinners[ii]) {
                tIconLabel.setText(_winnerText);
            }
            
            tIconLabel.setForeground(Color.BLACK);
            _labels.put(atNames[ii], tIconLabel);
            
            tLayoutConstraints.gridy++;
            tLayoutConstraints.gridx  = 0;
            tLayoutConstraints.anchor = GridBagConstraints.CENTER;

            add(tIconLabel, tLayoutConstraints);

            JLabel label = new JLabel(atNames[ii].toString());
            if (_playerIcons != null) {
                label.setIcon(_playerIcons[jj]);
            }
                        
            tLayoutConstraints.fill         = GridBagConstraints.HORIZONTAL;
            tLayoutConstraints.weightx      = 1.0f;
            tLayoutConstraints.insets.left  = 10;
            tLayoutConstraints.gridx++;

            add(label, tLayoutConstraints);
            
            JLabel tScoreLabel = new JLabel(String.valueOf(aiPoints[ii]));
            
            tLayoutConstraints.fill         = GridBagConstraints.NONE;
            tLayoutConstraints.anchor       = GridBagConstraints.EAST;
            tLayoutConstraints.weightx      = 0.0f;
            tLayoutConstraints.insets.left  = 0;
            tLayoutConstraints.insets.right = 10;
            tLayoutConstraints.gridx++;

            add(tScoreLabel, tLayoutConstraints);
            
            tLayoutConstraints.insets.right = 0;
        }

        SwingUtil.refresh(this);

    }

}
