package com.fivesigmagames.sdghunter.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fivesigmagames.sdghunter.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnHomeFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    //CONSTANTS
    private static final String SDG_HUNTER = "SDG Hunter";
    private static final float IMAGE_SCALE_RATIO = 0.2f;
    private static final float YELLOW_MON_ROTATION = 30;
    private static final float RED_MON_ROTATION = 25f;
    private static final float GREEN_MON_ROTATION = 0f;
    private static final float BTN_IMAGE_RATIO = 0.10f;
    private static final float BIG_IMAGE_SCALE_RATIO = 0.3f;

    //VARS
    private ImageView iv_yellow_mon_img;
    private ImageView iv_green_mon_img;
    private ImageView iv_red_mon_img;
    private ImageView iv_red_mon_bg;
    private TextView tv_sdg_hunter;
    private Button btn_cam;

    //INTERFACES
    private OnHomeFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Map vars with UI elements
        /*iv_yellow_mon_img = (ImageView) rootView.findViewById(R.id.yellow_mon_img);
        iv_red_mon_img = (ImageView) rootView.findViewById(R.id.red_mon_img);
        iv_green_mon_img = (ImageView) rootView.findViewById(R.id.green_mon_img);*/
        tv_sdg_hunter = (TextView) rootView.findViewById(R.id.sdh_hunter_text);
        btn_cam = (Button) rootView.findViewById(R.id.home_cam_btn);

        // Calculate image sizes
        int[] screenSize = getScreenSize();
        int screenWidth = screenSize[0];
        int screenHeight = screenSize[1];

        /*
        // Yellow monster top image
        Bitmap yellow_monster_on_bg = BitmapFactory.decodeResource(getContext().getResources(),
                                                                    R.drawable.yellow_monster_on_bg);
        Matrix matrix = getMatrix(IMAGE_SCALE_RATIO, IMAGE_SCALE_RATIO, YELLOW_MON_ROTATION);
        BitmapDrawable bmd = getBitmapDrawable(screenWidth, yellow_monster_on_bg, matrix);
        // set the Drawable on the ImageView
        iv_yellow_mon_img.setImageDrawable(bmd);
        iv_yellow_mon_img.setScaleType(ImageView.ScaleType.CENTER);

        // Green monster top image
        Bitmap green_monster_on_bg = BitmapFactory.decodeResource(getContext().getResources(),
                                                                    R.drawable.green_monster_on_bg);
        matrix = getMatrix(IMAGE_SCALE_RATIO, IMAGE_SCALE_RATIO, GREEN_MON_ROTATION);
        bmd = getBitmapDrawable(screenWidth, green_monster_on_bg, matrix);
        // set the Drawable on the ImageView
        iv_green_mon_img.setImageDrawable(bmd);
        iv_green_mon_img.setScaleType(ImageView.ScaleType.CENTER);

        // Red monster top image
        Bitmap red_monster_on_bg = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.red_monster_on_bg);
        matrix = getMatrix(IMAGE_SCALE_RATIO, IMAGE_SCALE_RATIO, RED_MON_ROTATION);
        bmd = getBitmapDrawable(screenWidth, red_monster_on_bg, matrix);
        // set the Drawable on the ImageView
        iv_red_mon_img.setImageDrawable(bmd);
        iv_red_mon_img.setScaleType(ImageView.ScaleType.CENTER);
        */

        // Cam button

        // Red monster top image
        Bitmap cam_button = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cam);
        Bitmap resized_cam_button = Bitmap.createScaledBitmap(cam_button, (int)(cam_button.getWidth()*0.1), (int)(cam_button.getHeight()*0.08), true);
        // set the Drawable on the ImageView
        btn_cam.setBackground(new BitmapDrawable(getResources(),resized_cam_button));
        btn_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.activateCamera();
            }
        });

        return rootView;
    }

    private int[] getScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }

    @NonNull
    private Matrix getMatrix(float width_scale, float height_scale, float rotation_angle) {
        Matrix matrix = new Matrix();
        matrix.postScale(width_scale, height_scale);
        matrix.postRotate(rotation_angle);
        return matrix;
    }

    @NonNull
    private BitmapDrawable getBitmapDrawable(int screen_width, Bitmap yellow_monster_on_bg, Matrix matrix) {

        int[] new_size = this.resizeImg(yellow_monster_on_bg, screen_width);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(yellow_monster_on_bg, 0, 0, new_size[0], new_size[1], matrix, true);
        return new BitmapDrawable(getResources(), resizedBitmap);
    }

    /**
     * Calculate the new sizes of the images to suit the scree width in according to the
     * IMAGE_SCALE_RATIO proportion.
     * @param d the drawable image
     * @param screen_width the full screen width
     * @return int [] being [0] the new width and [1] the new height
     */
    private int[] resizeImg(Bitmap d, double screen_width){
        int img_width = d.getWidth();
        int img_height = d.getHeight();
        int new_img_width = (int)(screen_width / IMAGE_SCALE_RATIO);
        double ratio = img_width / new_img_width;
        int new_img_height = (int) (img_height / ratio);
        if(ratio == 0) {
            ratio = new_img_width / img_width; //if instead of scaling down, it has to scale up
            new_img_height = (int) (img_height * ratio);
        }
        return new int[]{new_img_width, new_img_height};
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHomeFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnHomeFragmentInteractionListener {

        void activateCamera();
    }
}
