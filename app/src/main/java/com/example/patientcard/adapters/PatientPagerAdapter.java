package com.example.patientcard.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.patientcard.fragment.PatientDataFragment;
import com.example.patientcard.domain.webservice.PatientResourcesFragment;
import com.example.patientcard.domain.control.PatientDataHandler;

public class PatientPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 2;
    private final PatientDataHandler patientDataHandler;

    public PatientPagerAdapter(@NonNull FragmentActivity fragmentActivity, PatientDataHandler patientDataHandler) {
        super(fragmentActivity);
        this.patientDataHandler = patientDataHandler;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PatientDataFragment(patientDataHandler);
        }
        return new PatientResourcesFragment(patientDataHandler);
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
