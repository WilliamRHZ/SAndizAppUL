package com.example.distrisandi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.Toolbar;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class registros extends AppCompatActivity {

    TabLayout mTablayout;
    TabItem itemPagos;
    TabItem itemGastos;
    TabItem itemDevoluciones;
    ViewPager mviewPager;
    PagerControllerFragments pagerControllerFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registros);

        mTablayout = findViewById(R.id.tablaOpciones);
        itemPagos = findViewById(R.id.item1);
        itemGastos = findViewById(R.id.item2);
        itemDevoluciones = findViewById(R.id.item3);
        mviewPager = findViewById(R.id.ViewPagerFragments);

        pagerControllerFragments = new PagerControllerFragments(getSupportFragmentManager(),mTablayout.getTabCount());
        mviewPager.setAdapter(pagerControllerFragments);

        mTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mviewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mviewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTablayout));
    }
}
