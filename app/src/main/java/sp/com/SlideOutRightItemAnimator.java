package sp.com;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DefaultItemAnimator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

public class SlideOutRightItemAnimator extends DefaultItemAnimator {

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        holder.itemView.animate()
                .translationX(holder.itemView.getWidth())
                .setDuration(getRemoveDuration())
                .setListener(new DefaultRemoveAnimatorListener(holder))
                .start();
        return true;
    }

    private class DefaultRemoveAnimatorListener extends AnimatorListenerAdapter {
        private final RecyclerView.ViewHolder viewHolder;

        DefaultRemoveAnimatorListener(RecyclerView.ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            dispatchRemoveStarting(viewHolder);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animation.removeAllListeners();
            viewHolder.itemView.setTranslationX(0);
            dispatchRemoveFinished(viewHolder);
        }
    }
}

