package com.ripenapps.adoreandroid.stackview;

import android.view.View;

public interface CardStackListener {
    void onCardDragging(Direction direction, float ratio, int position);
    void onCardSwiped(Direction direction, int position);
    void onCardRewound();
    void onCardCanceled(int position);
    void onCardAppeared(View view, int position);
    void onCardDisappeared(View view, int position);

    CardStackListener DEFAULT = new CardStackListener() {
        @Override
        public void onCardDragging(Direction direction, float ratio, int position) {}
        @Override
        public void onCardSwiped(Direction direction, int position) {}
        @Override
        public void onCardRewound() {}
        @Override
        public void onCardCanceled(int position) {}
        @Override
        public void onCardAppeared(View view, int position) {}
        @Override
        public void onCardDisappeared(View view, int position) {}
    };
}
