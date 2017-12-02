package com.hku.yita.notelephonedeception;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.hku.yita.notelephonedeception.tools.WarningHandler;

import java.util.ArrayList;
import java.util.List;

import SlideView.SlideSwitchView;
import SlideView.SlideSwitchView.SlideListener;


public class MainActivity extends AppCompatActivity implements SlideListener{

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    public final static int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 11;
    private TextView txt;
    SlideSwitchView slide;
    private WarningHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt = (TextView) findViewById(R.id.txt);
        slide = (SlideSwitchView) findViewById(R.id.swit);
        slide.setState(false);
        slide.setSlideListener(this);
        Intent intent=new Intent(this,PhoneService.class);
        startService(intent);
        ShowContacts();
        handler = WarningHandler.getInstance();
        handler.setContext(this);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        }
    }

    @Override
    public void open() {
        txt.setText("open");
    }

    @Override
    public void close() {
        txt.setText("close");




    }


    private void ShowContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            readContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                ShowContacts();
            } else {
                Toast.makeText(this, "not granted at all", Toast.LENGTH_SHORT).show();

            }
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "not granted yet", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void readContacts() {
        List<String> contractname = new ArrayList<String>();
        List<String> contractnumber = new ArrayList<String>();
        //Button btn = (Button) findViewById(R.id.button);
        ContentResolver resolver = getContentResolver();
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Cursor cursor = resolver.query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));


            Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);

            while(phoneCursor.moveToNext()){

                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contractname.add(name);
                contractnumber.add(phoneNumber);
            }
            phoneCursor.close();
        }
        cursor.close();
    }
}