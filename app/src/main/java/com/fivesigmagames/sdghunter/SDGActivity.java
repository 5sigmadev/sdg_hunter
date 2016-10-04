package com.fivesigmagames.sdghunter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;

import com.fivesigmagames.sdghunter.adapter.ShareItem;
import com.fivesigmagames.sdghunter.view.AboutFragment;
import com.fivesigmagames.sdghunter.view.HomeFragment;
import com.fivesigmagames.sdghunter.view.MapFragment;
import com.fivesigmagames.sdghunter.view.ShareActivity;
import com.fivesigmagames.sdghunter.view.ShareFragment;

import java.io.File;
import java.util.ArrayList;

public class SDGActivity extends AppCompatActivity implements HomeFragment.OnHomeFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener, ShareFragment.OnShareFragmentInteractionListener,
        AboutFragment.OnAboutFragmentInteractionListener {



    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

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

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sdh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeFragment/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Interface Methods
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // Home Fragment
    @Override
    public void activateCamera(){
        final int REQUEST_IMAGE_CAPTURE = 1;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    // Share Fragment
    @Override
    public void sharePicture(int position) {
        ShareFragment fragment = (ShareFragment) getSupportFragmentManager().findFragmentByTag(getFragementTag(2));
        ShareItem item = fragment.getShareItem(position);

        //Create intent
        Intent intent = new Intent(SDGActivity.this, ShareActivity.class);
        intent.putExtra("pic_path", item.getFullPath());
        //Start details activity
        startActivity(intent);
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
                    return MapFragment.newInstance();
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

        ArrayList<ShareItem> files = new ArrayList();// list of file paths
        File file= new File(android.os.Environment.getExternalStorageDirectory(),"Pictures/Instagram");

        if (file.isDirectory()) {
            File[] listFile = file.listFiles();
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            for (int i = 0; i < listFile.length; i++) {
                Bitmap bitmap = BitmapFactory.decodeFile(listFile[i].getAbsolutePath(),bmOptions);
                files.add(new ShareItem(bitmap, listFile[i].getName(), listFile[i].getAbsolutePath()));
            }
        }
        return files;
    }
}
