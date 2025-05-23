package com.example.vohoportunitysconect.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.vohoportunitysconect.fragments.ApplicationListFragment;
import com.example.vohoportunitysconect.models.Application;

public class ApplicationsPagerAdapter extends FragmentStateAdapter {
    private final boolean isOrganizer;

    public ApplicationsPagerAdapter(@NonNull FragmentActivity fragmentActivity, boolean isOrganizer) {
        super(fragmentActivity);
        this.isOrganizer = isOrganizer;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Map position to Application.Status
        Application.Status status;
        switch (position) {
            case 0:
                status = Application.Status.PENDING;
                break;
            case 1:
                status = Application.Status.ACCEPTED;
                break;
            case 2:
                status = Application.Status.REJECTED;
                break;
            default:
                status = Application.Status.PENDING;
        }
        return ApplicationListFragment.newInstance(status, isOrganizer);
    }

    @Override
    public int getItemCount() {
        return 3; // Pending, Accepted, Rejected
    }
} 