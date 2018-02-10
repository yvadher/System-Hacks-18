package systems.lightspeed.love_a_thon_app;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    String timer[]={"Select time","5 sec","10 sec","15 sec","20 sec","30 sec"};
    String tim;
    Button mLocationBtn;
    TextView mText;
    GPS_Service gps;

    double partnerLongitude;
    double partnerLatitude;
    double mylongitude;
    double mylatitude;

    //Firebase Work
    DatabaseReference mDatabaseLocationDetails;
    DatabaseReference partnerLocationDetails;

    private void listenForPartner(String partnerName) {
        partnerLocationDetails = FirebaseDatabase.getInstance().getReference().child("Location_Details").child(partnerName);
        partnerLocationDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("longitude") && dataSnapshot.hasChild("latitude")) {
                    partnerLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                    partnerLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                    System.out.println("distance: "+ distPercentage(mylatitude, mylongitude, partnerLatitude, partnerLongitude));
                }else {
                    System.err.println("couldnt get partner location, keys were missing");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = (TextView) findViewById(R.id.location_tv);
        Spinner mSpinTime= (Spinner) findViewById(R.id.spinner_time);
        mLocationBtn= (Button) findViewById(R.id.location_btn);
        mDatabaseLocationDetails = FirebaseDatabase.getInstance().getReference().child("Location_Details").child("User 1");

//      permission check
        if(!runtime_permission())
         //   enable_button();
        runtime_permission();

        listenForPartner("MyPartner");

        mSpinTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                tim= adapterView.getItemAtPosition(i).toString();
                if(tim.equals("Select time")){
                    Toast.makeText(MainActivity.this, "Please Select time!", Toast.LENGTH_SHORT).show();
                }
                if(tim=="5 sec"){
                    tim= String.valueOf(tim.charAt(0));
                    Toast.makeText(MainActivity.this, tim+"", Toast.LENGTH_SHORT).show();
                }
                if(tim=="10 sec"){
                    tim= tim.substring(0,2);
                    Toast.makeText(MainActivity.this, tim+"", Toast.LENGTH_SHORT).show();
                }if(tim=="15 sec"){
                    tim= tim.substring(0,2);
                    Toast.makeText(MainActivity.this, tim+"", Toast.LENGTH_SHORT).show();
                }if(tim=="20 sec"){
                    tim= tim.substring(0,2);
                    Toast.makeText(MainActivity.this, tim+"", Toast.LENGTH_SHORT).show();
                }if(tim=="30 sec"){
                    tim= tim.substring(0,2);
                    Toast.makeText(MainActivity.this, tim+"", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                tim= String.valueOf(0);
            }
        });

        ArrayAdapter arrayAdapterCity = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,timer);
        arrayAdapterCity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinTime.setAdapter(arrayAdapterCity);
    //    updateDisplay();
    }
    private void updateDisplay() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                 gps = new GPS_Service(MainActivity.this,tim);
                startService(new Intent(MainActivity.this,GPS_Service.class));

                if(gps.canGetLocation()){
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    mylatitude = latitude;
                    mylongitude = longitude;
                    storeInDatabase(latitude,longitude);
                    mText.setText(latitude+" ::: "+longitude);
                    Toast.makeText(MainActivity.this, latitude+" ::: "+ longitude, Toast.LENGTH_SHORT).show();
                }else{
                    gps.showSettingsAlert();
                }
            }

        },0,1000);//Update text every second
    }
//                gps = new GPS_Service(MainActivity.this,tim);
//                startService(new Intent(MainActivity.this,GPS_Service.class));
//
//                if(gps.canGetLocation()){
//                    double latitude = gps.getLatitude();
//                    double longitude = gps.getLongitude();
//                    mylatitude = latitude;
//                    mylongitude = longitude;
//                    storeInDatabase(latitude,longitude);
//                    mText.setText(latitude+" ::: "+longitude);
//                    Toast.makeText(MainActivity.this, latitude+" ::: "+ longitude, Toast.LENGTH_SHORT).show();
//                }else{
//                    gps.showSettingsAlert();
//                }


    private double distPercentage(double latitude,  double longitude, double partnerlat, double partnerlong){
        double percentage;
        Location loc = new Location("");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);

        Location partnerloc = new Location("");
        partnerloc.setLatitude(partnerlat);
        partnerloc.setLongitude(partnerlong);

        percentage = loc.distanceTo(partnerloc);

        percentage/=2;
        percentage=1/percentage;
        percentage*=100;
        if(percentage >= 100) percentage = 100;
        percentage = Math.abs(percentage);
        return percentage;
    }

    private void storeInDatabase(double latitude, double longitude) {
        mDatabaseLocationDetails.child("longitude").setValue(longitude);
        mDatabaseLocationDetails.child("latitude").setValue(latitude);
    }

    private boolean runtime_permission() {
        if(Build.VERSION.SDK_INT>=23 && ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&& ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},123);
            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==123){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
              //  enable_button();
             //   updateDisplay();
            }else{
                runtime_permission();
            }
        }
    }
}