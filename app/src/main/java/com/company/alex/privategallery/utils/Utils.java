package com.company.alex.privategallery.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Alex on 08/05/2017.
 */

public class Utils {
    public Utils() {
    }

    public static void loge(String className, String message) {
        Log.e(className, message);
    }

    public static void showShortSnack(View parent, String message) {
        Snackbar snackbar = Snackbar.make(parent, message, -1);
        TextView textView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(10);
        snackbar.show();
    }

    public static void showLongSnack(View parent, String message) {
        Snackbar snackbar = Snackbar.make(parent, message, 0);
        TextView textView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(10);
        snackbar.show();
    }

    public static void initToolBar(AppCompatActivity activity, Toolbar toolbar, boolean homeUpIndicator) {
        activity.setSupportActionBar(toolbar);
        ActionBar ab = activity.getSupportActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayHomeAsUpEnabled(homeUpIndicator);
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
    }

    public static boolean hasCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.camera");
    }

    public static boolean hasCameraFlashHardware(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.camera.flash");
    }

    public static void setViewBackgroundColor(Activity activity, View view, int color) {
        if(color != 0) {
            GradientDrawable window;
            if(view instanceof ImageButton) {
                AppCompatImageButton toolbar = (AppCompatImageButton)view;
                window = (GradientDrawable)toolbar.getBackground();
                window.setColor(color);
                if(Build.VERSION.SDK_INT >= 16) {
                    toolbar.setBackground(window);
                } else {
                    toolbar.setBackgroundDrawable(window);
                }
            } else if(view instanceof ImageView) {
                ImageView toolbar1 = (ImageView)view;
                window = (GradientDrawable)toolbar1.getBackground();
                window.setColor(color);
                if(Build.VERSION.SDK_INT >= 16) {
                    toolbar1.setBackground(window);
                } else {
                    toolbar1.setBackground(window);
                }
            } else if(view instanceof Toolbar) {
                Toolbar toolbar2 = (Toolbar)view;
                toolbar2.setBackgroundColor(color);
                if(Build.VERSION.SDK_INT >= 21) {
                    Window window1 = activity.getWindow();
                    window1.clearFlags(67108864);
                    window1.addFlags(-2147483648);
                    window1.setStatusBarColor(getDarkColor(color));
                }
            }
        }
    }

    public static void setButtonTextColor(View view, int color) {
        if(color != 0) {
            if(view instanceof Button) {
                ((Button)view).setTextColor(color);
            }
        }
    }

    public static void setViewsColorStateList(View view, int normalColor, int darkenedColor) {
        int[][] states = new int[][]{{16842910}, {16842919}};
        int[] colors = new int[]{normalColor, darkenedColor};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        if(view instanceof AppCompatButton) {
            ((AppCompatButton)view).setSupportBackgroundTintList(colorStateList);
        }
    }

    public static void setViewsColorStateList(int normalColor, int darkenedColor, View... views) {
        int[][] states = new int[][]{new int[0], {16842919}};
        int[] colors = new int[]{normalColor, darkenedColor};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        View[] var6 = views;
        int var7 = views.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            View view = var6[var8];
            if(view instanceof AppCompatButton) {
                if(Build.VERSION.SDK_INT >= 21) {
                    ((AppCompatButton)view).setBackgroundTintList(colorStateList);
                } else {
                    ((AppCompatButton)view).setSupportBackgroundTintList(colorStateList);
                }
            }

            if(view instanceof AppCompatImageView) {
                ((AppCompatImageView)view).setColorFilter(normalColor);
            }

            if(view instanceof AppCompatTextView) {
                ((AppCompatTextView)view).setTextColor(normalColor);
            }
        }
    }

    public static int getDarkColor(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.rgb((int)((double)red * 0.8D), (int)((double)green * 0.8D), (int)((double)blue * 0.8D));
    }

    public static int getLightColor(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(127, red, green, blue);
    }
}
