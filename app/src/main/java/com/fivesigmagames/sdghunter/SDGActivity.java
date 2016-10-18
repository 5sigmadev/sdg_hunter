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

import com.fivesigmagames.sdghunter.model.ShareItem;
import com.fivesigmagames.sdghunter.repository.aws.AWSShareItemRepository;
import com.fivesigmagames.sdghunter.repository.sqlite.SqliteShareItemRepository;
import com.fivesigmagames.sdghunter.services.LocationService;
import com.fivesigmagames.sdghunter.view.AboutFragment;
import com.fivesigmagames.sdghunter.view.HomeFragment;
import com.fivesigmagames.sdghunter.view.MapFragment;
import com.fivesigmagames.sdghunter.view.PreviewActivity;
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

import static android.os.Environment.getExternalStoragePublicDirectory;

public class SDGActivity extends AppCompatActivity implements HomeFragment.OnHomeFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener, ShareFragment.OnShareFragmentInteractionListener,
        AboutFragment.OnAboutFragmentInteractionListener {

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
        }

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
                        mViewPager.setCurrentItem(3);
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
            mAwsShareItemRepository = new AWSShareItemRepository(this);
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
        } catch (IOException e) {
            Log.e(TAG, "Error moving file. Unexpected IO error");
        }
        finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Unexpected error closing input stream");
                }
            }
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Unexpected error closing output stream");
                }
            }
        }

    }

    private boolean updateMapFragment(ShareItem item) {
        if(item != null) {
            MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(getFragementTag(1));
            if (fragment != null) {
                fragment.updateMap(item);
                Log.d(TAG, " MapFragment updated with item " + item.getTitle());
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
            Log.d(TAG, " ShareFragement updated with item " + picPath);
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

    // Share Fragment
    @Override
    public void sharePicture(int position) {
        if(mPermissionsGranted) {
            ShareFragment fragment = (ShareFragment) getSupportFragmentManager().findFragmentByTag(getFragementTag(2));
            ShareItem item = fragment.getShareItem(position);

            //Upload to AWS
            uploadPhotoEntryToAWS(item.getFullPath());
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
                    return AboutFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
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
                    return "ABOUT";
            }
            return null;
        }
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
                    shareItemList = getSDGImages();
                }
                Location auxLocation = intent.getParcelableExtra("LOCATION");
                if (distanceBetween(auxLocation) >= DISTANCE_THRESHOLD) {
                    shareItemList = getSDGImages();
                }
                mCurrentLocation = auxLocation;
                fragment.updateMap(shareItemList, mCurrentLocation);
                Log.d(TAG, "Current location: lat - " + mCurrentLocation.getLatitude() +
                        " long -" + mCurrentLocation.getLongitude());
            }
        }
    }

    private double distanceBetween(Location point) {
        return mCurrentLocation.distanceTo(point);
    }
}
