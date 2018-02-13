package pfrison.me.polytime.android;

import android.animation.Animator;
import android.view.View;
import android.widget.Button;

public class Animations {
    private static final int WEEK_ANIM_DURATION = 150;

    public static void animateOpening(View view){
        view.setAlpha(0f);
        view.setTranslationY(20f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(100)
                .setDuration(500);
    }

    public static void animateReturnDefaultFadeOut(final View view, Animator.AnimatorListener listener){
        view.animate()
                .alpha(0f)
                .translationY(20f)
                .setDuration(WEEK_ANIM_DURATION)
                .setListener(listener);
    } public static void animateReturnDefaultFadeIn(View view, Animator.AnimatorListener listener){
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(WEEK_ANIM_DURATION)
                .setListener(listener);
    }

    public static void animateLeftFadeOut(final View view, Animator.AnimatorListener listener){
        view.animate()
                .alpha(0f)
                .translationX(50f)
                .setDuration(WEEK_ANIM_DURATION)
                .setListener(listener);
    } public static void animateLeftFadeIn(View view, Animator.AnimatorListener listener){
        view.setTranslationX(-50f);
        view.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(WEEK_ANIM_DURATION)
                .setListener(listener);
    }

    public static void animateRightFadeOut(final View view, Animator.AnimatorListener listener) {
        view.animate()
                .alpha(0f)
                .translationX(-50f)
                .setDuration(WEEK_ANIM_DURATION)
                .setListener(listener);
    } public static void animateRightFadeIn(View view, Animator.AnimatorListener listener){
        view.setTranslationX(50f);
        view.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(WEEK_ANIM_DURATION)
                .setListener(listener);
    }

    public static Animator.AnimatorListener enableButtonsAtEnd(final Button... buttons){
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                for (Button button : buttons)
                    button.setEnabled(true);
            }
        };
    }
}
