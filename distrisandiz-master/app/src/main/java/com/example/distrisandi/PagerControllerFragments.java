package com.example.distrisandi;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerControllerFragments extends FragmentPagerAdapter {

    int tabCounts;

    public PagerControllerFragments(FragmentManager fm, int tabCounts) {
        super(fm);
        this.tabCounts = tabCounts;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return  new pagosFragment();
            case 1:
                return new gastosFragment();
            case 2:
                return new devolucionesFragment();
                default:
                    return null;
        }

    }

    @Override
    public int getCount() {
        return tabCounts;
    }
}
