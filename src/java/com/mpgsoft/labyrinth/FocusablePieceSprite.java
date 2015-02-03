//
// $Id$

package com.mpgsoft.labyrinth;

import com.threerings.media.animation.Animation;
import com.threerings.media.animation.MultiFrameAnimation;
import com.threerings.media.util.MultiFrameImage;


/**
 * Adds support for an animated visual distinction that tracks the 
 * underlying {@link PieceSprite}. 
 * 
 * @author Jeffrey D. Hoffman
 */
abstract class FocusablePieceSprite extends PieceSprite {

    /**
     * This is the desired framerate of the animation.
     */
    private final double mdFrameRate;

    /**
     * This is the private <code>MultiFrameAnimation</code> that is created on first
     * request that animates over this sprite.
     */
    private MultiFrameAnimation mtAnimation = null;

    /**
     * Protected constructor accepting the <code>Piece</code> to be animated and
     * the frame rate for the <code>Animation</code> that will be overlayed.
     */
    protected FocusablePieceSprite(final Piece tPiece, final double dFrameRate) {
        super(tPiece);
        
        // Store the frame rate which will be used in <code>getAnimation()</code>.
        mdFrameRate = dFrameRate;
        
    }
    
    /**
     * Package-private method which returns the <code>Animation</code> that tracks
     * this treasure and causes it to sparkle.
     */
    final Animation getAnimation() {
                
        // Check to see if the animation has been instantiated yet.  This anonymously
        // overrides the animation's <code>getBounds()</code> to ensure that it is always
        // in sync with this sprite.
        if (mtAnimation == null)
            mtAnimation = new MultiFrameAnimation(getMultiFrameImage(), mdFrameRate, true);
        
        // Otherwise, reset the animation so it's ready to go.
        else
            mtAnimation.reset();
            
        // Make sure the animation is at our location.
        mtAnimation.setLocation(getX(), getY());
        
        // Make sure the animation always appears above this image.
        mtAnimation.setRenderOrder(1000);
        
        return mtAnimation;
    }

    /**
     * The extending class must implement this method which must return the
     * <code>MultiFrameImage</code> that will be animated over the sprite.
     */
    protected abstract MultiFrameImage getMultiFrameImage();

    /* (non-Javadoc)
     * @see com.mpgsoft.labyrinth.LabyrinthSprite#setLocation(int, int)
     */
    public final void setLocation(final int iX, final int iY) {
        super.setLocation(iX, iY);
        
        // If our animation has been created, we need to update it's position.
        if (mtAnimation != null)
            mtAnimation.setLocation(iX, iY);
        
    }

    /* (non-Javadoc)
     * @see com.mpgsoft.labyrinth.LabyrinthSprite#setOffset(int, int)
     */
    final void setOffset(final int iDx, final int iDy) {
        super.setOffset(iDx, iDy);

        // If our animation has been created, we need to update it's position.
        if (mtAnimation != null)
            mtAnimation.setLocation(getX(), getY());

    }

}
