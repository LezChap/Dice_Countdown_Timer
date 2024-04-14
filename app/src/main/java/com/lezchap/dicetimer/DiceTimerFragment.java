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

import com.lezchap.dicetimer.databinding.FragmentDiceTimerBinding;

import java.util.Arrays;
import java.util.Random;

public class DiceTimerFragment extends Fragment {

    private FragmentDiceTimerBinding binding;
    private CountDownTimer timer;
    private final Random random = new java.util.Random();
    private SharedPreferences sharedPreferences;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDiceTimerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        view.setKeepScreenOn(true);


        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView timerView = view.getRootView().findViewById(R.id.textview_dice_timer);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String formula = sharedPreferences.getString("formula", "");
        String time_unit = sharedPreferences.getString("time_unit", "");
        int total_dice = sharedPreferences.getInt("total_dice", 1);
        int dice_size = Integer.parseInt(sharedPreferences.getString("dice_size", ""));
        String roll_type = sharedPreferences.getString("roll_type", ""); // "normal", "advantage", "disadvantage"
        long time_mult;

        boolean vibe_alert = sharedPreferences.getBoolean("vibrate_alert", true);
        boolean audio_alert = sharedPreferences.getBoolean("audible_alert", false);
        boolean flash_alert = sharedPreferences.getBoolean("flash_alert", false);

        switch (time_unit) {
            case "minute": time_mult = 1000 * 60; break;
            case "hour": time_mult = 1000 * 60 * 60; break;
            case "day": time_mult = 1000 * 60 * 60 * 24; break;
            case "week": time_mult = 1000 * 60 * 60 * 24 * 7; break;
            case "month": time_mult = 1000L * 60 * 60 * 24 * 30; break;  //assuming 30 day months
            case "year": time_mult = 1000L * 60 * 60 * 24 * 7 * 365; break;  //assuming no leap years
            default: time_mult = 1000;
        }
        int result;
        try {
            int roll = rollDice(roll_type, total_dice, dice_size);
            result = (int)Math.round(Utilities.eval(formula, roll));
            timer = new CountDownTimer(result * time_mult, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long secondsLeft = millisUntilFinished / 1000;
                    timerView.setText(Utilities.formatDuration(secondsLeft));
                }
                @Override
                public void onFinish() {
                    timerView.setText("done!");
                    if (vibe_alert) Utilities.startVibration(requireContext());
                    if (audio_alert) Utilities.startBeeping(requireContext());
                    if (flash_alert) Utilities.startFlashing(binding.getRoot());

                }
            }.start();
        } catch (Exception e) {
            timerView.setText("Equation Error: " + e.getMessage());
        }
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
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    private int[] rollMultipleDice(int qty, int size) {
        int[] rolls = new int[qty];
        for (int i = 0; i < qty; i ++) rolls[i] = random.nextInt(size) + 1;
        return rolls;
    }

    private int rollDice(String type, int qty, int size) {
        if (type.equals("disadvantage") || type.equals("advantage")) qty = qty + 1;
        int[] rolls = rollMultipleDice(qty, size);

        int total = 0;
        switch (type) {
            case "normal":
                for (int num : rolls) total += num;
                break;
            case "advantage":
                Arrays.sort(rolls);
                int[] adv = Arrays.copyOfRange(rolls, 1, qty);
                for (int num : adv) total += num;
                break;
            case "disadvantage":
                Arrays.sort(rolls);
                int[] dis = Arrays.copyOfRange(rolls, 0, qty - 1);
                for (int num : dis) total += num;
                break;
        }
        return total;
    }

}