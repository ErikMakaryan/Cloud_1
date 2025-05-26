package com.example.vohoportunitysconect.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class EmailUtils {
    public static void sendEmail(Context context, String recipientEmail, String subject, String body) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // Only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"erikmakaryan3@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error sending email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 