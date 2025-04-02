package com.example.vohoportunitysconect.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.ApplicationsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ApplicationsFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ApplicationsPagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applications, container, false);
        initializeViews(view);
        setupViewPager();
        return view;
    }

    private void initializeViews(View view) {
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);
    }

    private void setupViewPager() {
        pagerAdapter = new ApplicationsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Pending");
                    break;
                case 1:
                    tab.setText("Accepted");
                    break;
                case 2:
                    tab.setText("Rejected");
                    break;
            }
        }).attach();
    }
} 