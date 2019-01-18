package com.jby.vegeapp.others;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardHelper {

    public static void hideSoftKeyboard(Context context, EditText editText) {

        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    public static void openSoftKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

}