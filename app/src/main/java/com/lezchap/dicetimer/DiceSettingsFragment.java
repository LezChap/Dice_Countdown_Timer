package com.lezchap.dicetimer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class DiceSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFormulaSummary();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // React to preference changes here
        if (key.equals("formula")) {
            setFormulaSummary();
            }
    }

    @Override
    public void onDestroy() {
        // Unregister the OnSharedPreferenceChangeListener to avoid memory leaks
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    private void setFormulaSummary() {
        // Handle the change of example_key preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        Preference result_preference = findPreference("evaluated_formula");
        if (result_preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) result_preference;
            String resultString;
            try {
                String formula = sharedPreferences.getString("formula", "");
                int exOne = (int) Utilities.eval(formula, 1);
                int exTwo = (int) Utilities.eval(formula, 2);
                int exSix = (int) Utilities.eval(formula, 6);
                int exTen = (int) Utilities.eval(formula, 10);
                resultString = "Roll 1: " + exOne + ", Roll 2: " + exTwo + ", Roll 6: " + exSix + ", Roll 10: " + exTen;
                //sharedPreferences.edit().putString("evaluated_formula", resultString).apply();
            } catch (Exception e) {
                resultString = "Invalid Equation: " + e.getMessage();
            }
            editTextPreference.setSummary(resultString);
        }
    }
}