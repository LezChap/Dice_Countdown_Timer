package com.lezchap.dicetimer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;

public class Utilities {
    private static Vibrator vibrator;
    private static boolean isVibrating = false;
    private static MediaPlayer mediaPlayer;
    private static Handler beepHandler;
    private static Runnable beepRunnable;
    private static boolean isBeeping = false;
    private static final int FLASH_INTERVAL = 500; // Flash interval in milliseconds
    private static Handler flashHandler;
    private static boolean isFlashing = false;

    public static double eval(final String str, int diceRoll) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName `(` expression `)` | functionName factor
            //        | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (func.equals("x"))  return diceRoll;
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    switch (func) {
                        case "sqrt": x = Math.sqrt(x); break;
                        case "sin":  x = Math.sin(Math.toRadians(x)); break;
                        case "cos":  x = Math.cos(Math.toRadians(x)); break;
                        case "tan":  x = Math.tan(Math.toRadians(x)); break;
                        default: throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    public static String formatDuration(long durationInSeconds) {
        // Constants for time units conversion
        final int SECONDS_IN_MINUTE = 60;
        final int MINUTES_IN_HOUR = 60;
        final int HOURS_IN_DAY = 24;
        final int DAYS_IN_YEAR = 365;

        // Calculate the time components
        long seconds = durationInSeconds % SECONDS_IN_MINUTE;
        long totalMinutes = durationInSeconds / SECONDS_IN_MINUTE;
        long minutes =  totalMinutes % MINUTES_IN_HOUR;
        long totalHours = totalMinutes / MINUTES_IN_HOUR;
        long hours = totalHours % HOURS_IN_DAY;
        long totalDays = totalHours / HOURS_IN_DAY;
        long days = totalDays % DAYS_IN_YEAR;
        long years = totalDays / DAYS_IN_YEAR;

        // Build the formatted string
        StringBuilder formattedDuration = new StringBuilder();
        // Always include minutes and seconds
        formattedDuration.append(String.format("%02d", minutes)).append(":").append(String.format("%02d", seconds));

        // Include hours if non-zero
        if (hours > 0 || days > 0 || years > 0) {
            formattedDuration.insert(0, String.format("%02d", hours) + ":");
        }

        // Include days if non-zero
        if (days > 0 || years > 0) {
            formattedDuration.insert(0, String.format("%d", days) + "d ");
        }

        // Include years if non-zero
        if (years > 0) {
            formattedDuration.insert(0, String.format("%d", years) + "y ");
        }

        return formattedDuration.toString();
    }

    public static void startVibration(Context context) {
        if (vibrator == null) vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator() && !isVibrating) {
            isVibrating = true;
            long[] pattern = {0, 500, 500};
            vibrator.vibrate(pattern, 0);
        }
    }

    public static void stopVibration() {
        if (vibrator != null && isVibrating) {
            vibrator.cancel();
            isVibrating = false;
        }
    }

    public static void startBeeping(Context context) {
        if (!isBeeping) {
            isBeeping = true;
            Uri defaultAlarmSoundUri = Settings.System.DEFAULT_ALARM_ALERT_URI;

            mediaPlayer = MediaPlayer.create(context, defaultAlarmSoundUri); // Load beep sound
            mediaPlayer.setLooping(true); // Loop the sound
            mediaPlayer.start(); // Start playing the sound
            beepHandler = new Handler();
            beepRunnable = new Runnable() {
                @Override
                public void run() {
                    mediaPlayer.start(); // Start playing the sound again
                    beepHandler.postDelayed(this, 1000); // Schedule next beep after 1 second
                }
            };
            beepHandler.post(beepRunnable); // Start beeping immediately
        }
    }

    public static void stopBeeping() {
        if (beepHandler != null && beepRunnable != null) {
            beepHandler.removeCallbacks(beepRunnable); // Remove the beep task
            beepHandler = null;
            beepRunnable = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // Stop playing the sound
            mediaPlayer.release(); // Release MediaPlayer resources
            mediaPlayer = null;
        }
        isBeeping = false;
    }

    public static void startFlashing(View view) {
        if (!isFlashing) {
            isFlashing = true;
            flashHandler = new Handler();
            flashHandler.post(flashRunnable(view));
        }
    }

    public static void stopFlashing() {
        if (flashHandler != null) {
            flashHandler.removeCallbacksAndMessages(null);
            flashHandler = null;
        }
        isFlashing = false;
    }

    private static Runnable flashRunnable(final View view) {
        return new Runnable() {
            @Override
            public void run() {
                int currentColor = view.getBackground() != null ?
                        ((ColorDrawable) view.getBackground()).getColor() :
                        Color.TRANSPARENT;

                // Toggle between white and transparent background
                int newColor = currentColor == Color.WHITE ? Color.BLACK : Color.WHITE;
                view.setBackgroundColor(newColor);

                // Schedule the next flash
                flashHandler.postDelayed(this, FLASH_INTERVAL);
            }
        };
    }
}
