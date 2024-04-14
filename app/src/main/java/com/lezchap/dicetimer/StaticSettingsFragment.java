package com.lezchap.dicetimer;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;

import java.util.Objects;

public class StaticSettingsFragment extends Fragment {
    private NumberPicker year_numberPicker;
    private NumberPicker day_numberPicker;
    private NumberPicker hour_numberPicker;
    private NumberPicker min_numberPicker;
    private NumberPicker sec_numberPicker;
    private Switch vibrate_alert;
    private Switch audible_alert;
    private Switch flash_alert;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_static_settings, container, false);

        year_numberPicker = view.findViewById(R.id.numberPickerYears);
        day_numberPicker = view.findViewById(R.id.numberPickerDays);
        hour_numberPicker = view.findViewById(R.id.numberPickerHours);
        min_numberPicker = view.findViewById(R.id.numberPickerMinutes);
        sec_numberPicker = view.findViewById(R.id.numberPickerSeconds);

        year_numberPicker.setMinValue(0);
        day_numberPicker.setMinValue(0);
        hour_numberPicker.setMinValue(0);
        min_numberPicker.setMinValue(0);
        sec_numberPicker.setMinValue(0);
        year_numberPicker.setMaxValue(100);
        day_numberPicker.setMaxValue(364);
        hour_numberPicker.setMaxValue(23);
        min_numberPicker.setMaxValue(59);
        sec_numberPicker.setMaxValue(59);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        int years = preferences.getInt("static_years", 0);
        int days =  preferences.getInt("static_days", 0);
        int hours =  preferences.getInt("static_hours", 0);
        int minutes =  preferences.getInt("static_minutes", 5);
        int seconds =  preferences.getInt("static_seconds", 0);

        //init current values
        year_numberPicker.setValue(years);
        day_numberPicker.setValue(days);
        hour_numberPicker.setValue(hours);
        min_numberPicker.setValue(minutes);
        sec_numberPicker.setValue(seconds);

        // Create a shared pickerListener
        NumberPickerListener pickerListener = new NumberPickerListener();

        // Set the pickerListener for both NumberPickers
        year_numberPicker.setOnValueChangedListener(pickerListener);
        day_numberPicker.setOnValueChangedListener(pickerListener);
        hour_numberPicker.setOnValueChangedListener(pickerListener);
        min_numberPicker.setOnValueChangedListener(pickerListener);
        sec_numberPicker.setOnValueChangedListener(pickerListener);

        vibrate_alert = view.findViewById(R.id.Vibrate_Alert);
        audible_alert = view.findViewById(R.id.Audible_Alert);
        flash_alert = view.findViewById(R.id.Flash_Alert);

        boolean vibrate_pref = preferences.getBoolean("vibrate_alert", true);
        boolean audible_pref = preferences.getBoolean("audible_alert", false);
        boolean flash_pref = preferences.getBoolean("flash_alert", false);
        vibrate_alert.setChecked(vibrate_pref);
        audible_alert.setChecked(audible_pref);
        flash_alert.setChecked(flash_pref);

        SwitchListener switchListener = new SwitchListener();
        vibrate_alert.setOnCheckedChangeListener(switchListener);
        audible_alert.setOnCheckedChangeListener(switchListener);
        flash_alert.setOnCheckedChangeListener(switchListener);

        return view;
    }

    private class NumberPickerListener implements NumberPicker.OnValueChangeListener {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            if (Objects.requireNonNull(picker) == year_numberPicker) {
                preferences.edit().putInt("static_years", newVal).apply();
            } else if (picker == day_numberPicker) {
                preferences.edit().putInt("static_days", newVal).apply();
            } else if (picker == hour_numberPicker) {
                preferences.edit().putInt("static_hours", newVal).apply();
            } else if (picker == min_numberPicker) {
                preferences.edit().putInt("static_minutes", newVal).apply();
            } else if (picker == sec_numberPicker) {
                preferences.edit().putInt("static_seconds", newVal).apply();
            }
        }
    }

    private class SwitchListener implements CompoundButton.OnCheckedChangeListener {
         @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
             Switch switchId = (Switch) buttonView;
             if (Objects.requireNonNull(switchId) == vibrate_alert) {
                 preferences.edit().putBoolean("vibrate_alert", isChecked).apply();
             } else if (switchId == audible_alert) {
                 preferences.edit().putBoolean("audible_alert", isChecked).apply();
             } else if (switchId == flash_alert) {
                 preferences.edit().putBoolean("flash_alert", isChecked).apply();
             }
         }
    }
}