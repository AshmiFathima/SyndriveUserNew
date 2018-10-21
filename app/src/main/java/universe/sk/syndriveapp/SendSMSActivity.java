package universe.sk.syndriveapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SendSMSActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private GPSTracker mGPSTracker;
    private ContactUsFragment contactUsFragment;
    private String message;
    GoogleApiClient googleApiClient;
    //private List<String> hospitals;
    //private String location;

    String etName, etName1, etNum1, etName2, etNum2, etName3, etNum3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.sms);
        actionBar.setTitle(" SOS Sent");
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mGPSTracker = new GPSTracker(this);
        contactUsFragment = new ContactUsFragment();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        storageReference = firebaseStorage.getReference();
        //hospitals = mGPSTracker.getHospitalAddress();
        //location = mGPSTracker.getCurrentAddress();

        final DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(firebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Userinfo userinfo;
                userinfo = dataSnapshot.getValue(Userinfo.class);

                etName = userinfo.getUsername();
//                etName1 = userinfo.getCname1();
//                etName2 = userinfo.getCname2();
//                etName3 = userinfo.getCname3();
//                etNum1 = userinfo.getCnum1();
//                etNum2 = userinfo.getCnum2();
//                etNum3 = userinfo.getCnum3();
                etName1 = "Srividya";
                etName2 = "Megha";
                etName3 = "Ashmi";
                etNum1 = "+917736497532";
                etNum2 = "+918078906366";
                etNum3 = "+919074976560";
                sendSMSMessage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SendSMSActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendSMSMessage() {
        String message = constructMessage();
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(etNum1, null, message, null, null);
        smsManager.sendTextMessage(etNum2, null, message, null, null);
        smsManager.sendTextMessage(etNum3, null, message, null, null);
//         smsManager.sendTextMessage("+918848041089", null, message, null, null);
//         smsManager.sendTextMessage(etNum2, null, message, null, null);
//         smsManager.sendTextMessage(etNum3, null, message, null, null);
//         smsManager.sendTextMessage("+918594014280", null, message, null, null);
    }

    private String constructMessage() {
        GPSTracker gpsTracker = new GPSTracker(this);
        //ContactUsFragment contactUsFragment = new ContactUsFragment();
        //LocationRequest locationRequest = new LocationRequest();
        Log.e("LOCATION", "Latitude : " + gpsTracker.getLatitude() + ", Longitude : " + gpsTracker.getLongitude());
        String latitude = String.valueOf(gpsTracker.getLatitude());
        String longitude = String.valueOf(gpsTracker.getLongitude());
        //Location location = mGPSTracker.getLocation();
        //double latitude = mGPSTracker.getLatitude();
        //double longitude = mGPSTracker.getLongitude();
        //String message = "Alert! It appears that " + etName + " may have been in an accident. " +
        //      etName + " has chosen you as their emergency contact. " + etName
        //    + "'s current location is: " +"Latitude : "+ gpsTracker.getLatitude() + ", Longitude : "+ gpsTracker.getLongitude();/*+location+
        //  ". Nearby hospitals include ";
        //message+="https://www.google.co.id/maps/@"+location;
        /*for (String hospital : hospitals) {
            message += hospital + "; ";
        }*/
        // String message = "https://www.google.co.id/maps/@https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&radius=10000&type=hospital&sensor=true&key=AIzaSyAwXCzIE9533_qHW8PdfWRamdVTqi6vrJg";
        message = "latitude:" + latitude + "\tlongitude" + longitude;
        Log.d("message", message);
        LocationAddress.getAddressFromLocation(gpsTracker.getLatitude(),gpsTracker.getLongitude(),getApplicationContext(),new GeocoderHandler());
        Log.d("message",message);
        return message;
    }


    private static class LocationAddress {
        private static final String TAG = "LocationAddress";

        public static void getAddressFromLocation(final double latitude, final double longitude,
                                                  final Context context, final Handler handler) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    String result = null;
                    try {
                        List<Address> addressList = geocoder.getFromLocation(
                                latitude, longitude, 1);
                        if (addressList != null && addressList.size() > 0) {
                            Address address = addressList.get(0);
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                                sb.append(address.getAddressLine(i)).append("\n");
                            }
                            sb.append(address.getLocality()).append("\n");
                            sb.append(address.getPostalCode()).append("\n");
                            sb.append(address.getCountryName());
                            result = sb.toString();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Unable connect to Geocoder", e);
                    } finally {
                        Message message = Message.obtain();
                        message.setTarget(handler);
                        if (result != null) {
                            message.what = 1;
                            Bundle bundle = new Bundle();
                            result = "Latitude: " + latitude + " Longitude: " + longitude +
                                    "\n\nAddress:\n" + result;
                            bundle.putString("address", result);
                            message.setData(bundle);
                        } else {
                            message.what = 1;
                            Bundle bundle = new Bundle();
                            result = "Latitude: " + latitude + " Longitude: " + longitude +
                                    "\n Unable to get address for this lat-long.";
                            bundle.putString("address", result);
                            message.setData(bundle);
                        }
                        message.sendToTarget();
                    }
                }
            };
            thread.start();
        }
    }
    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message1) {
            String locationAddress;
            switch (message1.what) {
                case 1:
                    Bundle bundle = message1.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            message = locationAddress;
            Log.d("message1",message);
        }
    }

}