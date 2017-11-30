package com.hku.yita.notelephonedeception.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hku.yita.notelephonedeception.MainActivity;
import com.hku.yita.notelephonedeception.MyPopupWindow;
import com.hku.yita.notelephonedeception.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Inflater;

/**
 * Created by Yita on 2017/11/28.
 */

public class WarningHandler {
    private static WarningHandler handler;
    private static Context context;
    private static String warningMessage;

    private WarningHandler(){}

    public static synchronized WarningHandler getInstance(){
        if(handler == null){
            handler = new WarningHandler();
            warningMessage = "";
        }
        return handler;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void checkDeceptionCall(String incomingCall){
        HashSet<String> contacts = readContacts();
        if(contacts.contains(incomingCall)){
        } else{
            warningMessage = "The incoming call " + incomingCall + " is not included in your phone books!";
            getWarningPicture();
        }
    }

    private HashSet<String> readContacts() {
        HashSet<String> contractNumSet = new HashSet<>();
        if(context != null) {
            List<String> contractname = new ArrayList<String>();
            List<String> contractnumber = new ArrayList<String>();

            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
            Cursor cursor = resolver.query(uri, null, null, null, null);
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));


                Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);

                while (phoneCursor.moveToNext()) {

                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumber = phoneNumber.replaceAll("[() +-]", "");
                    contractname.add(name);
                    contractnumber.add(phoneNumber);
                }
                phoneCursor.close();
            }
            cursor.close();
            contractNumSet.addAll(contractnumber);
        }

        return contractNumSet;
    }

    public void getWarningPicture(){
        AsyncTask<String, Void, Drawable> task = new AsyncTask<String, Void, Drawable>() {

            @Override
            protected Drawable doInBackground(String... strings) {
                final String url = "http://i.cs.hku.hk/~twchim/police/warning.jpg";
                InputStream is = null;
                try {
                    is = (InputStream) new URL(url).getContent();
                    Drawable d = Drawable.createFromStream(is, "warning");
                    return d;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Drawable warning) {
                PopupWindow popupWindow = new MyPopupWindow(context);

                LayoutInflater inflater = LayoutInflater.from(context);
                View popupView = inflater.inflate(R.layout.layout_popupwindow, null);
                ImageView warningPic = (ImageView) popupView.findViewById(R.id.warning);
                if(warning != null){
                    warningPic.setImageDrawable(warning);
                }
                TextView warning_message = (TextView) popupView.findViewById(R.id.message);
                warning_message.setText(warningMessage);

                Resources resources = context.getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                int popWidth = dm.widthPixels - 40;


                popupWindow.setWidth(popWidth);
                popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

                popupWindow.setContentView(popupView);

                popupWindow.setBackgroundDrawable(new ColorDrawable(-00000));
                popupWindow.setOutsideTouchable(false);
                popupWindow.setFocusable(true);

                View rootView = inflater.inflate(R.layout.activity_main, null);
                popupWindow.showAtLocation(rootView, Gravity.CENTER,0,10);
            }
        }.execute("");
    }
}

