package com.lezchap.dicetimer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lezchap.dicetimer.databinding.FragmentStaticTimerBinding;

import java.util.Random;

public class StaticTimerFragment extends Fragment {

    private FragmentStaticTimerBinding binding;
    private CountDownTimer timer;
    private final Random random = new Random();
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStaticTimerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        view.setKeepScreenOn(true);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView timerView = view.getRootView().findViewById(R.id.textview_static_timer);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        boolean vibe_alert = sharedPreferences.getBoolean("vibrate_alert", true);
        boolean audio_alert = sharedPreferences.getBoolean("audible_alert", false);
        boolean flash_alert = sharedPreferences.getBoolean("flash_alert", false);

        int years = sharedPreferences.getInt("static_years", 0);
        int days = sharedPreferences.getInt("static_days", 0);
        int hours = sharedPreferences.getInt("static_hours", 0);
        int minutes = sharedPreferences.getInt("static_minutes", 5);
        int seconds = sharedPreferences.getInt("static_seconds", 0);
        long result = (years * 365L) + days; //total days
        result = (result * 24) + hours; //total hours
        result = (result * 60) + minutes; //total minutes
        result = (result * 60) + seconds; //total seconds
        result = result * 1000;

        timer = new CountDownTimer(result, 1000) {
                public void onTick(long millisUntilFinished) {
                    long secondsLeft = millisUntilFinished / 1000;
                    timerView.setText(Utilities.formatDuration(secondsLeft));
                }

                public void onFinish() {
                    timerView.setText("done!");
                    if (vibe_alert) Utilities.startVibration(requireContext());
                    if (audio_alert) Utilities.startBeeping(requireContext());
                    if (flash_alert) Utilities.startFlashing(binding.getRoot());
                }
            }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Utilities.stopVibration();
        Utilities.stopBeeping();
        Utilities.stopFlashing();
        timer.cancel();
    }

    @Override
    public void onPause() {
        super.onPause();
        //sharedPreferences = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
    }
    private Integer rollDice(int size) {
        int randomNumber = 0;
        randomNumber = random.nextInt(size) + 1;
        return randomNumber;
    }

}