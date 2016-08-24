package com.example.isaac.apoinventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.example.isaac.apoinvetory.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private ArrayList<Item> items;
    private String currentOwner;
    private boolean checkin;

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the main content layout of the Activity
        setContentView(R.layout.activity_main);
        this.items = new ArrayList<Item>();
        this.currentOwner = null;
        this.checkin = false;

        String ret = "";

        try {
            InputStream inputStream = openFileInput("ITEM_STORAGE.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        Log.e("testing",ret);

//        ArrayList<Item> newItems = new ArrayList<Item>();
        try {
            JSONArray jarray = new JSONArray(ret);

            String jOwner;
            String jName;
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject row = jarray.getJSONObject(i);
                jOwner = row.getString("owner");
                jName = row.getString("name");
                items.add(new Item(jName, jOwner));
            }
            for(Item i: items) {
                Log.e("testing", i.getName());
            }

        }catch(org.json.JSONException e){
            Log.e("Exception", "JSON conversion failed "+ e.toString());
        }


    }


    public void writeJsonStream(OutputStream out, List<Item> itemSaver) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writeItemsArray(writer, itemSaver);
        writer.close();
    }

    public void writeItemsArray(JsonWriter writer, List<Item> itemSaver) throws IOException {
        writer.beginArray();
        for (Item i : itemSaver) {
            writeItem(writer, i);
        }
        writer.endArray();
    }

    public void writeItem(JsonWriter writer, Item i) throws IOException {
        writer.beginObject();
        writer.name("name").value(i.getName());
        writer.name("owner").value(i.getOwner());
        writer.endObject();
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config.txt", this.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }



    //product barcode mode
    public void scanBar(View v) {

        try {
            FileOutputStream fos = openFileOutput("ITEM_STORAGE.txt", Context.MODE_PRIVATE);
            try {
                writeJsonStream(fos, this.items);
            }catch(IOException e){
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }catch(FileNotFoundException e){
            Log.e("Exception", "File write failed: " + e.toString());
        }





        final HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        ArrayList<String> owners = new ArrayList<>();




       if(!this.items.isEmpty()){
           for(Item i:items) {
               if(!owners.contains(i.getOwner())){
                   owners.add(i.getOwner());
               }
           }
           for(String owner: owners){
               expandableListDetail.put(owner,new ArrayList<String>());
               for(Item i: items){

                   if(i.getOwner().equals(owner)){
                       expandableListDetail.get(owner).add(i.getName());
                   }
               }
           }
       }

        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);

        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {


            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                return false;
            }
        });



}



    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public void scanQR2(View v){
        this.checkin = true;
        scanQR(v);
    }


    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {


        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
//                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

//                items.add(new Item(contents,contents));

                if(contents.substring(0,6).equals("Name: ")){
                    this.currentOwner = contents.substring(6);
                    Toast.makeText(this,"Now scanning for: "+this.currentOwner,Toast.LENGTH_LONG).show();

                }
                else {

                    if(this.checkin){
                        for(Item i:items){
                            if(i.getName().equals(contents.substring((6)))){
                                items.remove(i);
                                checkin=false;
                                return;
                            }

                        }
                        Toast.makeText(this,"This item was not checked out", Toast.LENGTH_LONG).show();
                        checkin=false;
                        return;
                    }

                    if(this.currentOwner!=null){
//                        if(this.checkin){
//                            for(Item i:items){
//                                if(i.getName().equals(contents.substring((6)))){
//                                    items.remove(i);
//                                    checkin=false;
//                                    return;
//                                }
//
//                            }
//                            Toast.makeText(this,"This item was not checked out", Toast.LENGTH_LONG).show();
//                            checkin=false;
//                            return;
//                        }
                        items.add(new Item(contents.substring(6 ),this.currentOwner));
                        Toast.makeText(this,contents.substring(6)+" checked out to "+ this.currentOwner,Toast.LENGTH_LONG).show();
                    }
                    else{

                        Toast.makeText(this,"Select an owner before checking out items", Toast.LENGTH_LONG).show();
                    }
                }


            }
        }
    }
}