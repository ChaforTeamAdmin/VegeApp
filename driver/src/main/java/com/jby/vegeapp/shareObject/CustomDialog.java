package com.jby.vegeapp.shareObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class CustomDialog {
    public static void CustomDialog(final Activity context, final String title, final String message, final String button) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setCancelable(true);

                builder.setPositiveButton(button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
}
