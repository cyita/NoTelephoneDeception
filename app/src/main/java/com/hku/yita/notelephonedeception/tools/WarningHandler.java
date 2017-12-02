package com.hku.yita.notelephonedeception.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.hku.yita.notelephonedeception.MainActivity;
import com.hku.yita.notelephonedeception.MyPopupWindow;
import com.hku.yita.notelephonedeception.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
    private static HashSet<String> contacts;

    private WarningHandler(){}

    public static synchronized WarningHandler getInstance(){
        if(handler == null){
            handler = new WarningHandler();
            warningMessage = "";
            contacts = new HashSet<>();
        }
        return handler;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void checkDeceptionCall(final String incomingCall){
        readContacts();
        if(contacts.contains(incomingCall)){
        } else{
            warningMessage = "The incoming call " + incomingCall;

            AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
                boolean success = true;
                String jsonString;

                @Override
                protected String doInBackground(String... strings) {
                    final String url = "http://i.cs.hku.hk/~ynchen/blacklist.php?" + "action=check&num=" + incomingCall;
                    jsonString = getJsonPage(url);
                    if (jsonString.equals("Fail to connect"))
                        success = false;
                    return null;
                }

                @Override
                protected void onPostExecute(String result) {
                    if (success) {
                        parse_JSON_String_and_Switch_Activity(jsonString);
                    } else {
                    }
                }
            }.execute("");

            getWarningPicture();

        }
    }

    public void markDeceptionCall(final String phoneNumber){
        warningMessage = "";
        if(contacts.contains(phoneNumber)){
        } else{
            AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
                boolean success = true;
                String jsonString;

                @Override
                protected String doInBackground(String... strings) {
                    final String url = "http://i.cs.hku.hk/~ynchen/blacklist.php?" + "action=check&num=" + phoneNumber;
                    jsonString = getJsonPage(url);
                    if (jsonString.equals("Fail to connect"))
                        success = false;
                    return null;
                }

                @Override
                protected void onPostExecute(String result) {
                    if (success) {
                        JSONObject rootJSONObj = null;
                        try {
                            rootJSONObj = new JSONObject(jsonString);
                            String deception = rootJSONObj.getString("deception");
                            int deception_int = Integer.valueOf(deception);
                            if(deception_int == 0){
                                final String items[]={"Skip","Advertisement","Crime", "Other Deception Kind"};
                                AlertDialog.Builder builder=new AlertDialog.Builder(context);  //先得到构造器
                                builder.setTitle("Mark Call");                                     //设置标题
//                                builder.setIcon(R.mipmap.ic_launcher);                      //设置图标，图片id即可

                                ButtonOnClick clickListener = new ButtonOnClick(0, phoneNumber);

                                //设置单选按钮
                                //  items   为列表项
                                //  0       为默认选中第一个
                                //  第三个参数是监听器
                                builder.setSingleChoiceItems(items,0, clickListener);

                                //  设置监听器
                                builder.setPositiveButton("Confirm", clickListener);
                                builder.create().show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                    }
                }
            }.execute("");
        }
    }

    private class ButtonOnClick implements DialogInterface.OnClickListener{
        private int index;
        private String num;

        public ButtonOnClick(int index, String num){
            this.index = index;
            this.num = num;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which >= 0){
                index = which;
            } else if(which == -1){


                if(index != 0){
                    Toast.makeText(context, "Thank you for your help!", Toast.LENGTH_SHORT).show();

                    AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
                        @Override
                        protected String doInBackground(String... strings) {
                            final String url = "http://i.cs.hku.hk/~ynchen/blacklist.php?action=insert&num=" + num + "&type=" + index;
                            String jsonString = getJsonPage(url);
                            return jsonString;
                        }
                    }.execute("");
                }
                dialog.dismiss();
            }
        }
    }

    private void readContacts() {
        if(context != null) {
            List<String> contractname = new ArrayList<String>();

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
                    contacts.add(phoneNumber);
                }
                phoneCursor.close();
            }
            cursor.close();
        }
    }

    private void getWarningPicture(){
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

                Toast.makeText(context, warningMessage , Toast.LENGTH_SHORT).show();
            }
        }.execute("");
    }

    private String getJsonPage(String url) {
        HttpURLConnection conn_object = null;
        final int HTML_BUFFER_SIZE = 2*1024*1024;
        char htmlBuffer[] = new char[HTML_BUFFER_SIZE];

        try {
            URL url_object = new URL(url);
            conn_object = (HttpURLConnection) url_object.openConnection();
            conn_object.setInstanceFollowRedirects(true);

            BufferedReader reader_list = new BufferedReader(new InputStreamReader(conn_object.getInputStream()));
            String HTMLSource = ReadBufferedHTML(reader_list, htmlBuffer, HTML_BUFFER_SIZE);
            reader_list.close();
            return HTMLSource;
        } catch (Exception e) {
            return "Fail to connect";
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            if (conn_object != null) {
                conn_object.disconnect();
            }
        }
    }

    private String ReadBufferedHTML(BufferedReader reader, char [] htmlBuffer, int bufSz) throws java.io.IOException
    {
        htmlBuffer[0] = '\0';
        int offset = 0;
        do {
            int cnt = reader.read(htmlBuffer, offset, bufSz - offset);
            if (cnt > 0) {
                offset += cnt;
            } else {
                break;
            }
        } while (true);
        return new String(htmlBuffer);
    }

    private void parse_JSON_String_and_Switch_Activity(String JSONString) {
        String deception = "";
        String type = "";
        try {
            JSONObject rootJSONObj = new JSONObject(JSONString);
            deception = rootJSONObj.getString("deception");
            int deception_int = Integer.valueOf(deception);
            type = rootJSONObj.getString("type");
            int type_int = Integer.valueOf(type);
            if(deception_int != 0 && deception_int != 2){
                if(type_int == 1){
                    warningMessage += " is marked as advertisement call !";
                } else if(type_int == 2){
                    warningMessage += " is marked as crime call !";
                } else{
                    warningMessage += " is marked as deception call !";
                }
            } else{
                warningMessage += " is not included in your phone books !";
            }

        } catch (JSONException e) {
            e.printStackTrace();
            warningMessage += " is not included in your phone books !";
        }
    }
}

