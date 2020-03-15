package com.trangdv.orderfood.adapters;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.trangdv.orderfood.ui.main.CartFragment;
import com.trangdv.orderfood.ui.main.FavoriteFragment;
import com.trangdv.orderfood.ui.main.HomeFragment;
import com.trangdv.orderfood.ui.main.OrderFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

        private final int mCount;

        public ViewPagerAdapter(final AppCompatActivity activity, int count) {
            super(activity.getSupportFragmentManager());
            this.mCount = count;
        }

        @Override
        public Fragment getItem(final int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new OrderFragment();
                case 2:
                    return new CartFragment();
                case 3:
                    return new FavoriteFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return mCount;
        }
    }