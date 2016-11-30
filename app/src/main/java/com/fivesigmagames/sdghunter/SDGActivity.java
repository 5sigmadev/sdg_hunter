package com.fivesigmagames.sdghunter;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.fivesigmagames.sdghunter.model.SDGItem;
import com.fivesigmagames.sdghunter.model.ShareItem;
import com.fivesigmagames.sdghunter.repository.aws.AWSQueryAsyncTask;
import com.fivesigmagames.sdghunter.repository.aws.AWSShareItemRepository;
import com.fivesigmagames.sdghunter.repository.sqlite.SqliteShareItemRepository;
import com.fivesigmagames.sdghunter.services.LocationService;
import com.fivesigmagames.sdghunter.view.AboutFragment;
import com.fivesigmagames.sdghunter.view.HomeFragment;
import com.fivesigmagames.sdghunter.view.MapFragment;
import com.fivesigmagames.sdghunter.view.PreviewActivity;
import com.fivesigmagames.sdghunter.view.SDGFragment;
import com.fivesigmagames.sdghunter.view.ShareActivity;
import com.fivesigmagames.sdghunter.view.ShareFragment;
import com.fivesigmagames.sdghunter.view.ar.UnityPlayerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class SDGActivity extends AppCompatActivity implements HomeFragment.OnHomeFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener, ShareFragment.OnShareFragmentInteractionListener,
        AboutFragment.OnAboutFragmentInteractionListener, SDGFragment.OnSDGFragmentInteractionListener,
        AWSQueryAsyncTask.QueryAsyncResponse {

    // CONSTANTS
    private static final String TAG = "SDG [Main Activity]";
    private static final String SAVING_PICTURE_ERROR_MESSAGE = "Unexpected error when saving picture";
    private static final String DIRECTORY_CREATION_ERROR_MESSAGE = "Unxpected error when creating directory";
    private static final String LOCATION_SERVICE_NOT_CONNECTED_ERROR_MESSAGE = "Unexpected error. Location services not connected yet. " +
            "Try again in a few seconds";
    private static final String CURRENT_PHOTO_PATH = "CURRENT_PHOTO_PATH";
    private static final String CURRENT_LOCATION = "CURRENT_LOCATION";
    private static final int SHOW_PREVIEW_CAPTURE = 10;
    private static final int RESQUEST_ACTIVATE_CAMERA = 20;
    private static final int RESULT_PHOTO_RETAKE = 0;
    private static final int RESULT_PHOTO_SHARE = 1;
    private static final int RESULT_PHOTO_SAVE = 2;
    private static final int RESULT_PHOTO_TAKEN = 4;
    private static final int TAKEN_DIR = 1;
    private static final int DOWNLOAD_DIR = 2;
    private static final int DISTANCE_THRESHOLD = 100;
    private static final String PICTURE = "PICTURE";

    // PERMISSIONS CONSTANTS
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 4;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 5;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 6;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET,
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int[] PERMISSIONS_RESULT = {
            MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION,
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION, MY_PERMISSIONS_REQUEST_INTERNET,
            MY_PERMISSIONS_REQUEST_CAMERA, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
    };

    private boolean[] GRANTED_PERMISSIONS = { true, true, true, true, true, true};

    // VARS
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private LocationReceiver locationReceiver;
    private String mCurrentPhotoPath;
    private Location mCurrentLocation;
    private boolean mLocationEnabled;
    private SqliteShareItemRepository mSqliteShareItemRepository;
    private AWSShareItemRepository mAwsShareItemRepository;
    private boolean mPermissionsGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdh);

        ((SDGApplication)this.getApplication()).getDefaultTracker();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        checkPermissions();
        mPermissionsGranted = updateOverallPermissionStatus();

        SharedPreferences prefs = getSharedPreferences("com.fivesigmagames.sdghunter", Context.MODE_PRIVATE);
        if(!prefs.contains("firstUsage") || prefs.getBoolean("firstUsage", false)) {
            this.buildHintAlertMessage();
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putBoolean("firstUsage", false);
            prefsEditor.apply();
            Log.d(TAG, "First usage hint shown");
            ((SDGApplication)getApplication()).sendEvent("DEBUG", "First usage hint shown", TAG);
        }
        ((SDGApplication)getApplication()).sendEvent("DEBUG", "Application started...", TAG);

    }

    private boolean updateOverallPermissionStatus() {
        for(boolean b : GRANTED_PERMISSIONS) if(!b) return false;
        return true;
    }

    private void checkPermissions() {
        int i = 0;
        for(String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                GRANTED_PERMISSIONS[i] = false;
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_RESULT[i]);
                Log.d(TAG, "Permission: "+permission+" needed. Requesting it...");
                ((SDGApplication)getApplication()).sendEvent("DEBUG",
                        "Permission: "+permission+" needed. Requesting it...", TAG);
            }
            ++i;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GRANTED_PERMISSIONS[MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION-1] = true;
                    checkPermissions();
                    updateOverallPermissionStatus();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GRANTED_PERMISSIONS[MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION-1] = true;
                    checkPermissions();
                    updateOverallPermissionStatus();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GRANTED_PERMISSIONS[MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE-1] = true;
                    checkPermissions();
                    updateOverallPermissionStatus();
                }
                break;
            }case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GRANTED_PERMISSIONS[MY_PERMISSIONS_REQUEST_CAMERA-1] = true;
                    checkPermissions();
                    updateOverallPermissionStatus();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GRANTED_PERMISSIONS[MY_PERMISSIONS_REQUEST_INTERNET-1] = true;
                    checkPermissions();
                    updateOverallPermissionStatus();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GRANTED_PERMISSIONS[MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE-1] = true;
                    checkPermissions();
                    updateOverallPermissionStatus();
                }
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sdh, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(mCurrentPhotoPath != null) {
            savedInstanceState.putString(CURRENT_PHOTO_PATH, mCurrentPhotoPath);
            Log.d(TAG, "Saving current photo: " + mCurrentPhotoPath + " path in state");
        }
        if(mCurrentLocation != null) {
            savedInstanceState.putParcelable(CURRENT_LOCATION, mCurrentLocation);
            Log.d(TAG, "Saving current location: Lat " + mCurrentLocation.getLatitude() + " Lng " +
                    mCurrentLocation.getLongitude() + " in state");
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_hint) {
            this.buildHintAlertMessage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void buildHintAlertMessage() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("HINT");
        builder.setMessage(getResources().getString(R.string.hint_text_val))
                .setCancelable(false)
                .setPositiveButton("Tell me more", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        mViewPager.setCurrentItem(4);
                    }
                })
                .setNegativeButton("Got it!", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStart() {
        //Register BroadcastReceiver
        //to receive event from our service
        super.onStart();

        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals("")) {
            mLocationEnabled = false;
            buildAlertMessageNoGps();
        }
        if(mPermissionsGranted) {
            mLocationEnabled = true;
            locationReceiver = new LocationReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(LocationService.LOCATION_UPDATE);
            registerReceiver(locationReceiver, intentFilter);

            //Start our own service
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);

            mSqliteShareItemRepository = new SqliteShareItemRepository(this);
            mAwsShareItemRepository = new AWSShareItemRepository(this, this);
        }
        else{
            checkPermissions();
            mPermissionsGranted = updateOverallPermissionStatus();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it? If you do not enable. " +
                "You will not be able to take pictures with the app")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        mLocationEnabled = false;
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            mCurrentPhotoPath = savedInstanceState.getString(CURRENT_PHOTO_PATH);
            if(mCurrentPhotoPath != null) {
                Log.d(TAG, "Restoring current photo: " + mCurrentPhotoPath + " path from state");
            }
            if(mCurrentLocation != null) {
                mCurrentLocation = savedInstanceState.getParcelable(CURRENT_LOCATION);
                Log.d(TAG, "Saving current location: Lat " + mCurrentLocation.getLatitude() + " Lng " +
                        mCurrentLocation.getLongitude() + " in state");
            }
        }
    }

    @Override
    protected void onStop() {
        if(mPermissionsGranted) {
            unregisterReceiver(locationReceiver);
            stopService(new Intent(this, LocationService.class));
        }
        super.onStop();
    }

    // Interface Methods
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // Home Fragment
    @Override
    public void activateCamera(){

        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals("")) {
            mLocationEnabled = false;
            buildAlertMessageNoGps();
        }
        if(mPermissionsGranted) {
            if (mLocationEnabled) { // Only take a picture if the location is activated
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Toast.makeText(this, SAVING_PICTURE_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                    ((SDGApplication)getApplication()).sendEvent("DEBUG",
                            ex.getMessage(), TAG);
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, "com.fivesigmagames.sdghunter.fileprovider",
                            photoFile);
                    Intent cameraIntent = new Intent(SDGActivity.this, UnityPlayerActivity.class);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(cameraIntent, RESQUEST_ACTIVATE_CAMERA);
                } else {
                    Toast.makeText(this, DIRECTORY_CREATION_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                }
            }
        }
        else{
            checkPermissions();
            mPermissionsGranted = updateOverallPermissionStatus();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "OnActivityResult requestCode: " + requestCode);
        Log.d(TAG, "OnActivityResult resultCode: " + resultCode);
        if(requestCode == RESQUEST_ACTIVATE_CAMERA && resultCode == RESULT_PHOTO_TAKEN){
            Log.d(TAG, "Starting preview...");
            ((SDGApplication)getApplication()).sendEvent("DEBUG",
                    "Starting preview...", TAG);
            String auxPath = data.getExtras().getString(PICTURE);
            if(auxPath != null) {
                movePicture(mCurrentPhotoPath, auxPath);
            }
            updateGallery(mCurrentPhotoPath);
            // Start preview
            Intent previewIntent = new Intent(SDGActivity.this, PreviewActivity.class);
            previewIntent.putExtra("pic_path", mCurrentPhotoPath);
            startActivityForResult(previewIntent, SHOW_PREVIEW_CAPTURE);
        }
        else if(requestCode == SHOW_PREVIEW_CAPTURE){
            if(resultCode == RESULT_PHOTO_RETAKE){
                Bundle extras = data.getExtras();
                String picPath = extras.getString("pic_path");
                deleteFileFromMediaStore(getContentResolver(), new File(picPath));
                this.activateCamera();
            }
            else if(resultCode == RESULT_PHOTO_SHARE){
                Bundle extras = data.getExtras();
                Intent intent = new Intent(SDGActivity.this, ShareActivity.class);
                String picPath = extras.getString("pic_path");
                intent.putExtra("pic_path", picPath);
                savePhotoEntryInDb(picPath);
                uploadPhotoEntryToAWS(picPath);
                updateShareFragment(picPath);
                updateMapFragment(getSDGImage(picPath));
                startActivity(intent);
            }
            else if(resultCode == RESULT_PHOTO_SAVE){
                String picPath = data.getExtras().getString("pic_path");
                savePhotoEntryInDb(picPath);
                updateShareFragment(picPath);
                updateMapFragment(getSDGImage(picPath));
                mViewPager.setCurrentItem(2);
            }
        }
    }

    private void movePicture(String dst, String src) {
        InputStream in = null;
        OutputStream out = null;
        try {
            Log.d(TAG, "Moving file... Src " + src + " to Dst " + dst);
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;
            Log.d(TAG, "Finished moving file file...");
            deleteFileFromMediaStore(getContentResolver(), new File(src));
        } catch (FileNotFoundException e) {
            Log.e(TAG,"Error moving file. Src " + src +" or Dst " + dst + " file not found");
            ((SDGApplication)getApplication()).sendEvent("DEBUG",
                    e.getMessage(), TAG);
        } catch (IOException e) {
            Log.e(TAG, "Error moving file. Unexpected IO error");
            ((SDGApplication)getApplication()).sendEvent("DEBUG",
                    e.getMessage(), TAG);
        }
        finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Unexpected error closing input stream");
                    ((SDGApplication)getApplication()).sendEvent("DEBUG",
                            e.getMessage(), TAG);
                }
            }
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Unexpected error closing output stream");
                    ((SDGApplication)getApplication()).sendEvent("DEBUG",
                            e.getMessage(), TAG);
                }
            }
        }

    }

    private boolean updateMapFragment(ShareItem item) {
        if(item != null) {
            MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(getFragementTag(1));
            if (fragment != null) {
                fragment.updateMap(item);
                Log.d(TAG, "MapFragment updated with item " + item.getTitle());
                return true;
            }
            else{
                Log.d(TAG, "Map fragment not created yet");
                return false;
            }
        }
        else{
            Log.d(TAG, "Item was null");
            return false;
        }
    }

    private boolean updateShareFragment(String picPath){
        ShareFragment fragment = (ShareFragment) getSupportFragmentManager().findFragmentByTag(getFragementTag(2));
        if(fragment != null) {
            String[] parts = picPath.split(File.separator);
            ShareItem item = mSqliteShareItemRepository.findByName(parts[parts.length - 1]);
            item.setFullPath(picPath);
            fragment.updateSharedGrid(item);
            Log.d(TAG, "ShareFragement updated with item " + picPath);
            return true;
        }
        else {
            Log.d(TAG, "ShareFragment not created yet. Impossible to update");
            return false;
        }
    }

    private static void deleteFileFromMediaStore(final ContentResolver contentResolver, final File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        Log.d(TAG, "Deleting file " + canonicalPath + " from media store");
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[] {canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }
        Log.d(TAG, "File deleted from media store");
    }

    private void savePhotoEntryInDb(String picPath) {
        if(mCurrentLocation == null){
            Toast.makeText(this, LOCATION_SERVICE_NOT_CONNECTED_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }
        else {
            String[] parts = picPath.split(File.separator);
            mSqliteShareItemRepository.insert(new ShareItem(parts[parts.length - 1], picPath,
                    mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }
    }

    private void updateGallery(String picPath){
        //update gallery
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File pic = new File(picPath);
        Uri contentUri = Uri.fromFile(pic);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        Log.d(TAG, "Gallery updated");
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = null, image = null;
        if ((storageDir = checkSDGHunterDirectory(TAKEN_DIR)) != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
            String imageFileName = "JPEG_" + timeStamp;
            image = File.createTempFile( // Using createTempFile to guarantee uniqueness in the filename
                    imageFileName,  /* prefix */
                    ".jpeg",         /* suffix */
                    storageDir      /* directory */
            );
            mCurrentPhotoPath = image.getAbsolutePath();
        }
        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    private File checkSDGHunterDirectory(int dir){
        String sdgPictures = getResources().getString(R.string.sdg_pictures_path);
        File sdgDir = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), sdgPictures);
        if (!sdgDir.exists()) {
            sdgDir.mkdirs();
        }
        String sdgSubDir = null;
        if(dir == TAKEN_DIR){
            sdgSubDir = getResources().getString(R.string.sdg_taken_pictures_path);
        }
        else if(dir == DOWNLOAD_DIR){
            sdgSubDir = getResources().getString(R.string.sdg_download_pictures_path);
        }
        File subDir = null;
        if(sdgSubDir != null) {
            subDir = new File(sdgDir, sdgSubDir);
            if (!subDir.exists()) {
                subDir.mkdirs();
            }
            return subDir;
        }
        return subDir;
    }

    // SDG Fragment
    @Override
    public void sdgPicDescription(int position) {
        SDGFragment fragment = (SDGFragment) getSupportFragmentManager().findFragmentByTag(getFragementTag(3));
        fragment.showSdgDescription(position);
    }

    // Share Fragment
    @Override
    public void sharePicture(int position) {
        if(mPermissionsGranted) {
            ShareFragment fragment = (ShareFragment) getSupportFragmentManager().findFragmentByTag(getFragementTag(2));
            ShareItem item = fragment.getShareItem(position);

            //Upload to AWS
            uploadPhotoEntryToAWS(item.getFullPath(), item.getLatitude(), item.getLongitude());
            //Create intent
            Intent intent = new Intent(SDGActivity.this, ShareActivity.class);
            intent.putExtra("pic_path", item.getFullPath());
            //Start details activity
            startActivity(intent);
        }
        else{
            checkPermissions();
            mPermissionsGranted = updateOverallPermissionStatus();
        }
    }

    private void uploadPhotoEntryToAWS(String picPath) {
        if(mCurrentLocation != null){
            String[] parts = picPath.split(File.separator);
            mAwsShareItemRepository.insert(new ShareItem(parts[parts.length - 1], picPath,
                    mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }
    }

    private void uploadPhotoEntryToAWS(String picPath, double lat, double lng) {
        if(mCurrentLocation != null){
            String[] parts = picPath.split(File.separator);
            mAwsShareItemRepository.insert(new ShareItem(parts[parts.length - 1], picPath,
                    lat, lng));
        }
    }

    private String getFragementTag(int position) {
        return "android:switcher:" + mViewPager.getId() + ":" + position;
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position){
                case 0:
                    return HomeFragment.newInstance();
                case 1:
                    return MapFragment.newInstance(getSDGImages(), mCurrentLocation);
                case 2:
                    return ShareFragment.newInstance(getSDGImages());
                case 3:
                    return SDGFragment.newInstance(getSDGListImages());
                case 4:
                    return AboutFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "HOME";
                case 1:
                    return "MAP";
                case 2:
                    return "SHARE";
                case 3:
                    return "SDG";
                case 4:
                    return "ABOUT";
            }
            return null;
        }
    }

    private ArrayList<SDGItem> getSDGListImages(){

        ArrayList<SDGItem> sdgList = new ArrayList<>();
        for(int i=0; i < SDG_TITLES.length && i < SDG_DESCRIPTIONS.length; ++i) {
            sdgList.add(new SDGItem(SDG_TITLES[i], SDG_DESCRIPTIONS[i]));
        }
        return sdgList;
    }

    private ArrayList<ShareItem> getSDGImages() {
        if(mPermissionsGranted) {
            ArrayList<ShareItem> files = new ArrayList();// list of file paths
            String sdgPictures = getResources().getString(R.string.sdg_pictures_path).concat(File.separator).concat(
                    getResources().getString(R.string.sdg_taken_pictures_path)
            );
            File file = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), sdgPictures);

            if (file.isDirectory()) {
                File[] listFile = file.listFiles();
                for (int i = 0; i < listFile.length; i++) {
                    ShareItem item = mSqliteShareItemRepository.findByName(listFile[i].getName());
                    if (item != null) {
                            item.setFullPath(listFile[i].getAbsolutePath());
                            files.add(item);
                    } else {
                        Log.e(TAG, "An entry in the db should exist for " + listFile[i].getName());
                    }
                }
            }
            return files;
        }
        else{
            checkPermissions();
            mPermissionsGranted = updateOverallPermissionStatus();
            return null;
        }
    }

    private ShareItem getSDGImage(String picPath) {
        if(mPermissionsGranted) {
            String[] parts = picPath.split(File.separator);
            ShareItem item = mSqliteShareItemRepository.findByName(parts[parts.length - 1]);
            if (item != null) {
                item.setFullPath(picPath);
                return item;
            } else {
                Log.e(TAG, "An entry in the db should exist for " + parts[parts.length - 1]);
                return null;
            }
        }
        else{
            checkPermissions();
            mPermissionsGranted = updateOverallPermissionStatus();
        }
        return null;
    }

    private class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(getFragementTag(1));
            if(fragment != null) {
                ArrayList<ShareItem> shareItemList = null;
                if (mCurrentLocation == null) {
                    mCurrentLocation = intent.getParcelableExtra("LOCATION");
                    if (checkSDGHunterDirectory(DOWNLOAD_DIR) != null) {
                        mAwsShareItemRepository.findSDGImages(mCurrentLocation);
                    }
                    shareItemList = getSDGImages();
                }
                Location auxLocation = intent.getParcelableExtra("LOCATION");
                if (distanceBetween(auxLocation) >= DISTANCE_THRESHOLD) {
                    if (checkSDGHunterDirectory(DOWNLOAD_DIR) != null) {
                        mAwsShareItemRepository.findSDGImages(mCurrentLocation);
                    }
                    shareItemList = getSDGImages();
                }
                mCurrentLocation = auxLocation;
                fragment.updateMap(shareItemList, mCurrentLocation, true);
                Log.d(TAG, "Current location: lat - " + mCurrentLocation.getLatitude() +
                        " long -" + mCurrentLocation.getLongitude());
            }
        }
    }

    @Override
    public void queryProcessFinish(ArrayList<ShareItem> output) {
        for(ShareItem item : output){
            updateGallery(item.getFullPath());
        }
        MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(getFragementTag(1));
        if(fragment != null) {
            fragment.updateMap(output, mCurrentLocation, false);
        }
    }

    private double distanceBetween(Location point) {
        return mCurrentLocation.distanceTo(point);
    }

    // LONG STRING CONSTANTS

    private String[] SDG_TITLES = new String[]{
            "No poverty", "Zero hunger", "Good health and well-being", "Quality education",
            "Gender equality", "Clean water and sanitation", "Affordable and clean energy",
            "Decent work and economic growth", "Industry, innovation and infrastructure",
            "Reduced inequalities", "Sustainable cities and communities", "Responsible consumption and production",
            "Climate change", "Life below water", "Life on land", "Peace, justice and strong institutions",
            "Partnerships for the goals", "Sustainable development goals"
    };

    private String[] SDG_DESCRIPTIONS = new String[]{
            "By 2030, eradicate extreme poverty for all people everywhere, currently measured as people living on less than $1.25 a day\n" +
            "By 2030, reduce at least by half the proportion of men, women and children of all ages living in poverty in all its dimensions according to national definitions\n" +
            "Implement nationally appropriate social protection systems and measures for all, including floors, and by 2030 achieve substantial coverage of the poor and the vulnerable\n" +
            "By 2030, ensure that all men and women, in particular the poor and the vulnerable, have equal rights to economic resources, as well as access to basic services, ownership and control over land and other forms of property, inheritance, natural resources, appropriate new technology and financial services, including microfinance\n" +
            "By 2030, build the resilience of the poor and those in vulnerable situations and reduce their exposure and vulnerability to climate-related extreme events and other economic, social and environmental shocks and disasters\n" +
            "Ensure significant mobilization of resources from a variety of sources, including through enhanced development cooperation, in order to provide adequate and predictable means for developing countries, in particular least developed countries, to implement programmes and policies to end poverty in all its dimensions\n" +
            "Create sound policy frameworks at the national, regional and international levels, based on pro-poor and gender-sensitive development strategies, to support accelerated investment in poverty eradication actions",

            "By 2030, end hunger and ensure access by all people, in particular the poor and people in vulnerable situations, including infants, to safe, nutritious and sufficient food all year round\n" +
            "By 2030, end all forms of malnutrition, including achieving, by 2025, the internationally agreed targets on stunting and wasting in children under 5 years of age, and address the nutritional needs of adolescent girls, pregnant and lactating women and older persons\n" +
            "By 2030, double the agricultural productivity and incomes of small-scale food producers, in particular women, indigenous peoples, family farmers, pastoralists and fishers, including through secure and equal access to land, other productive resources and inputs, knowledge, financial services, markets and opportunities for value addition and non-farm employment\n" +
            "By 2030, ensure sustainable food production systems and implement resilient agricultural practices that increase productivity and production, that help maintain ecosystems, that strengthen capacity for adaptation to climate change, extreme weather, drought, flooding and other disasters and that progressively improve land and soil quality\n" +
            "By 2020, maintain the genetic diversity of seeds, cultivated plants and farmed and domesticated animals and their related wild species, including through soundly managed and diversified seed and plant banks at the national, regional and international levels, and promote access to and fair and equitable sharing of benefits arising from the utilization of genetic resources and associated traditional knowledge, as internationally agreed\n" +
            "Increase investment, including through enhanced international cooperation, in rural infrastructure, agricultural research and extension services, technology development and plant and livestock gene banks in order to enhance agricultural productive capacity in developing countries, in particular least developed countries\n" +
            "Correct and prevent trade restrictions and distortions in world agricultural markets, including through the parallel elimination of all forms of agricultural export subsidies and all export measures with equivalent effect, in accordance with the mandate of the Doha Development Round\n" +
            "Adopt measures to ensure the proper functioning of food commodity markets and their derivatives and facilitate timely access to market information, including on food reserves, in order to help limit extreme food price volatility",

            "By 2030, reduce the global maternal mortality ratio to less than 70 per 100,000 live births\n" +
            "By 2030, end preventable deaths of newborns and children under 5 years of age, with all countries aiming to reduce neonatal mortality to at least as low as 12 per 1,000 live births and under-5 mortality to at least as low as 25 per 1,000 live births\n" +
            "By 2030, end the epidemics of AIDS, tuberculosis, malaria and neglected tropical diseases and combat hepatitis, water-borne diseases and other communicable diseases\n" +
            "By 2030, reduce by one third premature mortality from non-communicable diseases through prevention and treatment and promote mental health and well-being\n" +
            "Strengthen the prevention and treatment of substance abuse, including narcotic drug abuse and harmful use of alcohol\n" +
            "By 2020, halve the number of global deaths and injuries from road traffic accidents\n" +
            "By 2030, ensure universal access to sexual and reproductive health-care services, including for family planning, information and education, and the integration of reproductive health into national strategies and programmes\n" +
            "Achieve universal health coverage, including financial risk protection, access to quality essential health-care services and access to safe, effective, quality and affordable essential medicines and vaccines for all\n" +
            "By 2030, substantially reduce the number of deaths and illnesses from hazardous chemicals and air, water and soil pollution and contamination\n" +
            "Strengthen the implementation of the World Health Organization Framework Convention on Tobacco Control in all countries, as appropriate\n" +
            "Support the research and development of vaccines and medicines for the communicable and noncommunicable diseases that primarily affect developing countries, provide access to affordable essential medicines and vaccines, in accordance with the Doha Declaration on the TRIPS Agreement and Public Health, which affirms the right of developing countries to use to the full the provisions in the Agreement on Trade Related Aspects of Intellectual Property Rights regarding flexibilities to protect public health, and, in particular, provide access to medicines for all\n" +
            "Substantially increase health financing and the recruitment, development, training and retention of the health workforce in developing countries, especially in least developed countries and small island developing States\n" +
            "Strengthen the capacity of all countries, in particular developing countries, for early warning, risk reduction and management of national and global health risks",

            "By 2030, ensure that all girls and boys complete free, equitable and quality primary and secondary education leading to relevant and Goal-4 effective learning outcomes\n" +
            "By 2030, ensure that all girls and boys have access to quality early childhood development, care and preprimary education so that they are ready for primary education\n" +
            "By 2030, ensure equal access for all women and men to affordable and quality technical, vocational and tertiary education, including university\n" +
            "By 2030, substantially increase the number of youth and adults who have relevant skills, including technical and vocational skills, for employment, decent jobs and entrepreneurship\n" +
            "By 2030, eliminate gender disparities in education and ensure equal access to all levels of education and vocational training for the vulnerable, including persons with disabilities, indigenous peoples and children in vulnerable situations\n" +
            "By 2030, ensure that all youth and a substantial proportion of adults, both men and women, achieve literacy and numeracy\n" +
            "By 2030, ensure that all learners acquire the knowledge and skills needed to promote sustainable development, including, among others, through education for sustainable development and sustainable lifestyles, human rights, gender equality, promotion of a culture of peace and non-violence, global citizenship and appreciation of cultural diversity and of culture’s contribution to sustainable development\n" +
            "Build and upgrade education facilities that are child, disability and gender sensitive and provide safe, nonviolent, inclusive and effective learning environments for all\n" +
            "By 2020, substantially expand globally the number of scholarships available to developing countries, in particular least developed countries, small island developing States and African countries, for enrolment in higher education, including vocational training and information and communications technology, technical, engineering and scientific programmes, in developed countries and other developing countries\n" +
            "By 2030, substantially increase the supply of qualified teachers, including through international cooperation for teacher training in developing countries, especially least developed countries and small island developing states",

            "End all forms of discrimination against all women and girls everywhere\n" +
            "Eliminate all forms of violence against all women and girls in the public and private spheres, including trafficking and sexual and other types of exploitation\n" +
            "Eliminate all harmful practices, such as child, early and forced marriage and female genital mutilation\n" +
            "Recognize and value unpaid care and domestic work through the provision of public services, infrastructure and social protection policies and the promotion of shared responsibility within the household and the family as nationally appropriate\n" +
            "Ensure women’s full and effective participation and equal opportunities for leadership at all levels of decisionmaking in political, economic and public life\n" +
            "Ensure universal access to sexual and reproductive health and reproductive rights as agreed in accordance with the Programme of Action of the International Conference on Population and Development and the Beijing Platform for Action and the outcome documents of their review conferences\n" +
            "Undertake reforms to give women equal rights to economic resources, as well as access to ownership and control over land and other forms of property, financial services, inheritance and natural resources, in accordance with national laws\n" +
            "Enhance the use of enabling technology, in particular information and communications technology, to promote the empowerment of women\n" +
            "Adopt and strengthen sound policies and enforceable legislation for the promotion of gender equality and the empowerment of all women and girls at all levels",

            "By 2030, achieve universal and equitable access to safe and affordable drinking water for all\n" +
            "By 2030, achieve access to adequate and equitable sanitation and hygiene for all and end open defecation, paying special attention to the needs of women and girls and those in vulnerable situations\n" +
            "By 2030, improve water quality by reducing pollution, eliminating dumping and minimizing release of hazardous chemicals and materials, halving the proportion of untreated wastewater and substantially increasing recycling and safe reuse globally\n" +
            "By 2030, substantially increase water-use efficiency across all sectors and ensure sustainable withdrawals and supply of freshwater to address water scarcity and substantially reduce the number of people suffering from water scarcity\n" +
            "By 2030, implement integrated water resources management at all levels, including through transboundary cooperation as appropriate\n" +
            "By 2020, protect and restore water-related ecosystems, including mountains, forests, wetlands, rivers, aquifers and lakes\n" +
            "By 2030, expand international cooperation and capacity-building support to developing countries in water- and sanitation-related activities and programmes, including water harvesting, desalination, water efficiency, wastewater treatment, recycling and reuse technologies\n" +
            "Support and strengthen the participation of local communities in improving water and sanitation management",

            "By 2030, ensure universal access to affordable, reliable and modern energy services\n" +
            "By 2030, increase substantially the share of renewable energy in the global energy mix\n" +
            "By 2030, double the global rate of improvement in energy efficiency\n" +
            "By 2030, enhance international cooperation to facilitate access to clean energy research and technology, including renewable energy, energy efficiency and advanced and cleaner fossil-fuel technology, and promote investment in energy infrastructure and clean energy technology\n" +
            "By 2030, expand infrastructure and upgrade technology for supplying modern and sustainable energy services for all in developing countries, in particular least developed countries, small island developing States, and land-locked developing countries, in accordance with their respective programmes of support",

            "Sustain per capita economic growth in accordance with national circumstances and, in particular, at least 7 per cent gross domestic product growth per annum in the least developed countries\n" +
            "Achieve higher levels of economic productivity through diversification, technological upgrading and innovation, including through a focus on high-value added and labour-intensive sectors\n" +
            "Promote development-oriented policies that support productive activities, decent job creation, entrepreneurship, creativity and innovation, and encourage the formalization and growth of micro-, small- and medium-sized enterprises, including through access to financial services\n" +
            "Improve progressively, through 2030, global resource efficiency in consumption and production and endeavour to decouple economic growth from environmental degradation, in accordance with the 10-year framework of programmes on sustainable consumption and production, with developed countries taking the lead\n" +
            "By 2030, achieve full and productive employment and decent work for all women and men, including for young people and persons with disabilities, and equal pay for work of equal value\n" +
            "By 2020, substantially reduce the proportion of youth not in employment, education or training\n" +
            "Take immediate and effective measures to eradicate forced labour, end modern slavery and human trafficking and secure the prohibition and elimination of the worst forms of child labour, including recruitment and use of child soldiers, and by 2025 end child labour in all its forms\n" +
            "Protect labour rights and promote safe and secure working environments for all workers, including migrant workers, in particular women migrants, and those in precarious employment\n" +
            "By 2030, devise and implement policies to promote sustainable tourism that creates jobs and promotes local culture and products\n" +
            "Strengthen the capacity of domestic financial institutions to encourage and expand access to banking, insurance and financial services for all\n" +
            "Increase Aid for Trade support for developing countries, in particular least developed countries, including through the Enhanced Integrated Framework for Trade-Related Technical Assistance to Least Developed Countries\n" +
            "By 2020, develop and operationalize a global strategy for youth employment and implement the Global Jobs Pact of the International Labour Organization",

            "Develop quality, reliable, sustainable and resilient infrastructure, including regional and transborder infrastructure, to support economic development and human well-being, with a focus on affordable and equitable access for all\n" +
            "Promote inclusive and sustainable industrialization and, by 2030, significantly raise industry’s share of employment and gross domestic product, in line with national circumstances, and double its share in least developed countries\n" +
            "Increase the access of small-scale industrial and other enterprises, in particular in developing countries, to financial services, including affordable credit, and their integration into value chains and markets\n" +
            "By 2030, upgrade infrastructure and retrofit industries to make them sustainable, with increased resource-use efficiency and greater adoption of clean and environmentally sound technologies and industrial processes, with all countries taking action in accordance with their respective capabilities\n" +
            "Enhance scientific research, upgrade the technological capabilities of industrial sectors in all countries, in particular developing countries, including, by 2030, encouraging innovation and substantially increasing the number of research and development workers per 1 million people and public and private research and development spending\n" +
            "Facilitate sustainable and resilient infrastructure development in developing countries through enhanced financial, technological and technical support to African countries, least developed countries, landlocked developing countries and small island developing States 18\n" +
            "Support domestic technology development, research and innovation in developing countries, including by ensuring a conducive policy environment for, inter alia, industrial diversification and value addition to commodities\n" +
            "Significantly increase access to information and communications technology and strive to provide universal and affordable access to the Internet in least developed countries by 2020",

            "By 2030, progressively achieve and sustain income growth of the bottom 40 per cent of the population at a rate higher than the national average\n" +
            "By 2030, empower and promote the social, economic and political inclusion of all, irrespective of age, sex, disability, race, ethnicity, origin, religion or economic or other status\n" +
            "Ensure equal opportunity and reduce inequalities of outcome, including by eliminating discriminatory laws, policies and practices and promoting appropriate legislation, policies and action in this regard\n" +
            "Adopt policies, especially fiscal, wage and social protection policies, and progressively achieve greater equality\n" +
            "Improve the regulation and monitoring of global financial markets and institutions and strengthen the implementation of such regulations\n" +
            "Ensure enhanced representation and voice for developing countries in decision-making in global international economic and financial institutions in order to deliver more effective, credible, accountable and legitimate institutions\n" +
            "Facilitate orderly, safe, regular and responsible migration and mobility of people, including through the implementation of planned and well-managed migration policies\n" +
            "Implement the principle of special and differential treatment for developing countries, in particular least developed countries, in accordance with World Trade Organization agreements\n" +
            "Encourage official development assistance and financial flows, including foreign direct investment, to States where the need is greatest, in particular least developed countries, African countries, small island developing States and landlocked developing countries, in accordance with their national plans and programmes\n" +
            "By 2030, reduce to less than 3 per cent the transaction costs of migrant remittances and eliminate remittance corridors with costs higher than 5 per cent",

            "By 2030, ensure access for all to adequate, safe and affordable housing and basic services and upgrade slums\n" +
            "By 2030, provide access to safe, affordable, accessible and sustainable transport systems for all, improving road safety, notably by expanding public transport, with special attention to the needs of those in vulnerable situations, women, children, persons with disabilities and older persons\n" +
            "By 2030, enhance inclusive and sustainable urbanization and capacity for participatory, integrated and sustainable human settlement planning and management in all countries\n" +
            "Strengthen efforts to protect and safeguard the world’s cultural and natural heritage\n" +
            "By 2030, significantly reduce the number of deaths and the number of people affected and substantially decrease the direct economic losses relative to global gross domestic product caused by disasters, including water-related disasters, with a focus on protecting the poor and people in vulnerable situations\n" +
            "By 2030, reduce the adverse per capita environmental impact of cities, including by paying special attention to air quality and municipal and other waste management\n" +
            "By 2030, provide universal access to safe, inclusive and accessible, green and public spaces, in particular for women and children, older persons and persons with disabilities\n" +
            "Support positive economic, social and environmental links between urban, peri-urban and rural areas by strengthening national and regional development planning\n" +
            "By 2020, substantially increase the number of cities and human settlements adopting and implementing integrated policies and plans towards inclusion, resource efficiency, mitigation and adaptation to climate change, resilience to disasters, and develop and implement, in line with the Sendai Framework for Disaster Risk Reduction 2015-2030, holistic disaster risk management at all levels\n" +
            "Support least developed countries, including through financial and technical assistance, in building sustainable and resilient buildings utilizing local materials",

            "Implement the 10-year framework of programmes on sustainable consumption and production, all countries taking action, with developed countries taking the lead, taking into account the development and capabilities of developing countries\n" +
            "By 2030, achieve the sustainable management and efficient use of natural resources\n" +
            "By 2030, halve per capita global food waste at the retail and consumer levels and reduce food losses along production and supply chains, including post-harvest losses\n" +
            "By 2020, achieve the environmentally sound management of chemicals and all wastes throughout their life cycle, in accordance with agreed international frameworks, and significantly reduce their release to air, water and soil in order to minimize their adverse impacts on human health and the environment\n" +
            "By 2030, substantially reduce waste generation through prevention, reduction, recycling and reuse\n" +
            "Encourage companies, especially large and transnational companies, to adopt sustainable practices and to integrate sustainability information into their reporting cycle\n" +
            "Promote public procurement practices that are sustainable, in accordance with national policies and priorities\n" +
            "By 2030, ensure that people everywhere have the relevant information and awareness for sustainable development and lifestyles in harmony with nature\n" +
            "Support developing countries to strengthen their scientific and technological capacity to move towards more sustainable patterns of consumption and production\n" +
            "Develop and implement tools to monitor sustainable development impacts for sustainable tourism that creates jobs and promotes local culture and products\n" +
            "Rationalize inefficient fossil-fuel subsidies that encourage wasteful consumption by removing market distortions, in accordance with national circumstances, including by restructuring taxation and phasing out those harmful subsidies, where they exist, to reflect their environmental impacts, taking fully into account the specific needs and conditions of developing countries and minimizing the possible adverse impacts on their development in a manner that protects the poor and the affected communities",

            "Strengthen resilience and adaptive capacity to climate-related hazards and natural disasters in all countries\n" +
            "Integrate climate change measures into national policies, strategies and planning\n" +
            "Improve education, awareness-raising and human and institutional capacity on climate change mitigation, adaptation, impact reduction and early warning\n" +
            "Implement the commitment undertaken by developed-country parties to the United Nations Framework Convention on Climate Change to a goal of mobilizing jointly $100 billion annually by 2020 from all sources to address the needs of developing countries in the context of meaningful mitigation actions and transparency on implementation and fully operationalize the Green Climate Fund through its capitalization as soon as possible\n" +
            "Promote mechanisms for raising capacity for effective climate change-related planning and management in least developed countries and small island developing States, including focusing on women, youth and local and marginalized communities\n" +
            "* Acknowledging that the United Nations Framework Convention on Climate Change is the primary international, intergovernmental forum for negotiating the global response to climate change.",

            "By 2025, prevent and significantly reduce marine pollution of all kinds, in particular from land-based activities, including marine debris and nutrient pollution\n" +
            "By 2020, sustainably manage and protect marine and coastal ecosystems to avoid significant adverse impacts, including by strengthening their resilience, and take action for their restoration in order to achieve healthy and productive oceans\n" +
            "Minimize and address the impacts of ocean acidification, including through enhanced scientific cooperation at all levels\n" +
            "By 2020, effectively regulate harvesting and end overfishing, illegal, unreported and unregulated fishing and destructive fishing practices and implement science-based management plans, in order to restore fish stocks in the shortest time feasible, at least to levels that can produce maximum sustainable yield as determined by their biological characteristics\n" +
            "By 2020, conserve at least 10 per cent of coastal and marine areas, consistent with national and international law and based on the best available scientific information\n" +
            "By 2020, prohibit certain forms of fisheries subsidies which contribute to overcapacity and overfishing, eliminate subsidies that contribute to illegal, unreported and unregulated fishing and refrain from introducing new such subsidies, recognizing that appropriate and effective special and differential treatment for developing and least developed countries should be an integral part of the World Trade Organization fisheries subsidies negotiation\n" +
            "By 2030, increase the economic benefits to Small Island developing States and least developed countries from the sustainable use of marine resources, including through sustainable management of fisheries, aquaculture and tourism\n" +
            "Increase scientific knowledge, develop research capacity and transfer marine technology, taking into account the Intergovernmental Oceanographic Commission Criteria and Guidelines on the Transfer of Marine Technology, in order to improve ocean health and to enhance the contribution of marine biodiversity to the development of developing countries, in particular small island developing States and least developed countries\n" +
            "Provide access for small-scale artisanal fishers to marine resources and markets\n" +
            "Enhance the conservation and sustainable use of oceans and their resources by implementing international law as reflected in UNCLOS, which provides the legal framework for the conservation and sustainable use of oceans and their resources, as recalled in paragraph 158 of The Future We Want",

            "By 2020, ensure the conservation, restoration and sustainable use of terrestrial and inland freshwater ecosystems and their services, in particular forests, wetlands, mountains and drylands, in line with obligations under international agreements\n" +
            "By 2020, promote the implementation of sustainable management of all types of forests, halt deforestation, restore degraded forests and substantially increase afforestation and reforestation globally\n" +
            "By 2030, combat desertification, restore degraded land and soil, including land affected by desertification, drought and floods, and strive to achieve a land degradation-neutral world\n" +
            "By 2030, ensure the conservation of mountain ecosystems, including their biodiversity, in order to enhance their capacity to provide benefits that are essential for sustainable development\n" +
            "Take urgent and significant action to reduce the degradation of natural habitats, halt the loss of biodiversity and, by 2020, protect and prevent the extinction of threatened species\n" +
            "Promote fair and equitable sharing of the benefits arising from the utilization of genetic resources and promote appropriate access to such resources, as internationally agreed\n" +
            "Take urgent action to end poaching and trafficking of protected species of flora and fauna and address both demand and supply of illegal wildlife products\n" +
            "By 2020, introduce measures to prevent the introduction and significantly reduce the impact of invasive alien species on land and water ecosystems and control or eradicate the priority species\n" +
            "By 2020, integrate ecosystem and biodiversity values into national and local planning, development processes, poverty reduction strategies and accounts\n" +
            "Mobilize and significantly increase financial resources from all sources to conserve and sustainably use biodiversity and ecosystems\n" +
            "Mobilize significant resources from all sources and at all levels to finance sustainable forest management and provide adequate incentives to developing countries to advance such management, including for conservation and reforestation\n" +
            "Enhance global support for efforts to combat poaching and trafficking of protected species, including by increasing the capacity of local communities to pursue sustainable livelihood opportunities",

            "Significantly reduce all forms of violence and related death rates everywhere\n" +
            "End abuse, exploitation, trafficking and all forms of violence against and torture of children\n" +
            "Promote the rule of law at the national and international levels and ensure equal access to justice for all\n" +
            "By 2030, significantly reduce illicit financial and arms flows, strengthen the recovery and return of stolen assets and combat all forms of organized crime\n" +
            "Substantially reduce corruption and bribery in all their forms\n" +
            "Develop effective, accountable and transparent institutions at all levels\n" +
            "Ensure responsive, inclusive, participatory and representative decision-making at all levels\n" +
            "Broaden and strengthen the participation of developing countries in the institutions of global governance\n" +
            "By 2030, provide legal identity for all, including birth registration\n" +
            "Ensure public access to information and protect fundamental freedoms, in accordance with national legislation and international agreements\n" +
            "Strengthen relevant national institutions, including through international cooperation, for building capacity at all levels, in particular in developing countries, to prevent violence and combat terrorism and crime\n" +
            "Promote and enforce non-discriminatory laws and policies for sustainable development",

            "\t\tFinance\t" +
            "Strengthen domestic resource mobilization, including through international support to developing countries, to improve domestic capacity for tax and other revenue collection\n" +
            "Developed countries to implement fully their official development assistance commitments, including the commitment by many developed countries to achieve the target of 0.7 per cent of ODA/GNI to developing countries and 0.15 to 0.20 per cent of ODA/GNI to least developed countries ODA providers are encouraged to consider setting a target to provide at least 0.20 per cent of ODA/GNI to least developed countries\n" +
            "Mobilize additional financial resources for developing countries from multiple sources\n" +
            "Assist developing countries in attaining long-term debt sustainability through coordinated policies aimed at fostering debt financing, debt relief and debt restructuring, as appropriate, and address the external debt of highly indebted poor countries to reduce debt distress\n" +
            "Adopt and implement investment promotion regimes for least developed countries\n" +
            "\t\tTechnology\t"+
            "Enhance North-South, South-South and triangular regional and international cooperation on and access to science, technology and innovation and enhance knowledge sharing on mutually agreed terms, including through improved coordination among existing mechanisms, in particular at the United Nations level, and through a global technology facilitation mechanism\n" +
            "Promote the development, transfer, dissemination and diffusion of environmentally sound technologies to developing countries on favourable terms, including on concessional and preferential terms, as mutually agreed\n" +
            "Fully operationalize the technology bank and science, technology and innovation capacity-building mechanism for least developed countries by 2017 and enhance the use of enabling technology, in particular information and communications technology\n" +
            "\t\tCapacity building\t" +
            "Enhance international support for implementing effective and targeted capacity-building in developing countries to support national plans to implement all the sustainable development goals, including through North-South, South-South and triangular cooperation\n" +
            "\t\tTrade\t" +
            "Promote a universal, rules-based, open, non-discriminatory and equitable multilateral trading system under the World Trade Organization, including through the conclusion of negotiations under its Doha Development Agenda\n" +
            "Significantly increase the exports of developing countries, in particular with a view to doubling the least developed countries’ share of global exports by 2020\n" +
            "Realize timely implementation of duty-free and quota-free market access on a lasting basis for all least developed countries, consistent with World Trade Organization decisions, including by ensuring that preferential rules of origin applicable to imports from least developed countries are transparent and simple, and contribute to facilitating market access\n" +
            "\t\tSystemic issues"+
            "\t\tPolicy and institutional coherence\t"+
            "Enhance global macroeconomic stability, including through policy coordination and policy coherence\n" +
            "Enhance policy coherence for sustainable development\n" +
            "Respect each country’s policy space and leadership to establish and implement policies for poverty eradication and sustainable development\n" +
            "\t\tMulti-stakeholder partnerships\t" +
            "Enhance the global partnership for sustainable development, complemented by multi-stakeholder partnerships that mobilize and share knowledge, expertise, technology and financial resources, to support the achievement of the sustainable development goals in all countries, in particular developing countries\n" +
            "Encourage and promote effective public, public-private and civil society partnerships, building on the experience and resourcing strategies of partnerships\n" +
            "\t\tData, monitoring and accountability\t" +
            "By 2020, enhance capacity-building support to developing countries, including for least developed countries and small island developing States, to increase significantly the availability of high-quality, timely and reliable data disaggregated by income, gender, age, race, ethnicity, migratory status, disability, geographic location and other characteristics relevant in national contexts\n" +
            "By 2030, build on existing initiatives to develop measurements of progress on sustainable development that complement gross domestic product, and support statistical capacity-building in developing countries",

            "Sustainable Development Goals"

    };
}
