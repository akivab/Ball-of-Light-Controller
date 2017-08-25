package com.example.akivabamberger.balloflightcontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImageUploadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImageUploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageUploadFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ImageUploadFragment";
    private static final int PICK_IMAGE = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ImageUploadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImageUploadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImageUploadFragment newInstance(String param1, String param2) {
        ImageUploadFragment fragment = new ImageUploadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image_upload, container, false);
        Button uploadButton = (Button) view.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) getView().findViewById(R.id.imageView);
                ImageManipulator.uploadImage(imageView, getActivity());
            }
        });
        Button drawButton = (Button) view.findViewById(R.id.drawButton);
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getContext().getPackageManager().getLaunchIntentForPackage("com.wacom.bamboopapertab");
                if (intent != null) {
                    // We found the activity now start the activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Couldn't find Bamboo paper!");
                }
            }
        });
        Button galleryButton = (Button) view.findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent();
                 intent.setType("image/*");
                 intent.setAction(Intent.ACTION_GET_CONTENT);
                 startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

             }
         });
        Button rotateButton = (Button) view.findViewById(R.id.rotateButton);
        Button flipButton = (Button) view.findViewById(R.id.flipButton);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) getView().findViewById(R.id.imageView);
                ImageManipulator.rotateImage(imageView, getActivity());
            }
        });
        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) getView().findViewById(R.id.imageView);
                ImageManipulator.mirrorImage(imageView, getActivity());
            }
        });
        Button resetButton = (Button) view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) getView().findViewById(R.id.imageView);
                Resources resources = view.getContext().getResources();
                final int resourceId = resources.getIdentifier("dickbutt", "drawable",
                        view.getContext().getPackageName());
                Bitmap bp = ((BitmapDrawable)resources.getDrawable(resourceId)).getBitmap();
                ImageManipulator.setImageBitmap(bp, imageView, getActivity());

            }
        });
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

        ImageManipulator.updateImageViewFromSavedFile(imageView, getActivity());
        WiFiController.getInstance().updateTextView(view);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
                ImageView imageView = (ImageView) getView().findViewById(R.id.imageView);
                Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
                if (bitmap != null && bitmap.getWidth() != 0) {
                    ImageManipulator.setImageBitmap(bitmap, imageView, getActivity());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "FOUND IMAGE");
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
    }
}
