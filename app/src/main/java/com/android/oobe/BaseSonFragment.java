package com.android.oobe;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.lang.reflect.Field;


/**
 * BaseSonFragment is a custom Fragment class designed to ensure smoother transitions,
 * particularly when the parent Fragment is being removed, preventing the premature disappearance
 * of child Fragments.
 *
 * This class overrides the onCreateAnimator method to selectively apply a default "do-nothing"
 * animation or return the standard Fragment animation based on specific conditions.
 *
 * - onCreateAnimator: Determines if a "do-nothing" animation should be used when the parent Fragment
 *   is being removed, avoiding unwanted animations during Fragment transitions.
 *
 * - getNextAnimationDuration: Reflectively retrieves the next animation resource of a Fragment and
 *   obtains its duration. If unavailable, it returns a default duration.
 */

public class BaseSonFragment extends Fragment {
    private static final int DEFAULT_CHILD_ANIMATION_DURATION = 500;

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        final Fragment parent = getParentFragment();
        if (!enter && parent != null && parent.isRemoving()) {
            @SuppressLint("ObjectAnimatorBinding") ObjectAnimator doNothingAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 1f);
            doNothingAnimator.setDuration(getNextAnimationDuration(parent, DEFAULT_CHILD_ANIMATION_DURATION));
            return doNothingAnimator;
        } else {
            return super.onCreateAnimator(transit, enter, nextAnim);
        }
    }

    private static long getNextAnimationDuration(Fragment fragment, long defValue) {
        try {
            Field nextAnimField = Fragment.class.getDeclaredField("mNextAnim");
            nextAnimField.setAccessible(true);
            int nextAnimResource = nextAnimField.getInt(fragment);
            Animation nextAnim = AnimationUtils.loadAnimation(fragment.getActivity(), nextAnimResource);
            return (nextAnim == null) ? defValue : nextAnim.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
            return defValue;
        }
    }
}
