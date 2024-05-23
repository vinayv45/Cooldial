package com.droideve.apps.nearbystores.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;

import com.droideve.apps.nearbystores.utils.NSLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.droideve.apps.nearbystores.location.GPStracker;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.SplashActivity;
import com.droideve.apps.nearbystores.animation.ImageLoaderAnimation;
import com.droideve.apps.nearbystores.appconfig.AppContext;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.databinding.V2FragmentProfileEditBinding;
import com.droideve.apps.nearbystores.helper.CommunFunctions;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.api_parser.UserParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.ImageUtils;
import com.droideve.apps.nearbystores.utils.MessageDialog;
import com.droideve.apps.nearbystores.utils.Translator;
import com.droideve.apps.nearbystores.utils.Utils;
import com.droideve.apps.nearbystores.views.CustomDialog;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment implements ImageUtils.PrepareImagesData.OnCompressListner {


    @BindView(R.id.login)
    TextInputEditText loginTxt;

    @BindView(R.id.full_name)
    TextInputEditText fullNameTxt;

    @BindView(R.id.email)
    TextInputEditText emailTxt;

    @BindView(R.id.saveBtn)
    MaterialRippleLayout saveBtn;


    @BindView(R.id.disableAccountBtn)
    MaterialRippleLayout disableAccountBtn;

    @BindView(R.id.logoutAccountBtn)
    MaterialRippleLayout logoutAccountBtn;


    @BindView(R.id.takePicture)
    ImageView takePicture;

    @BindView(R.id.userimage)
    CircularImageView userimage;

    private User mUser;
    private ProgressDialog mPdialog;
    private GPStracker gps;
    private RequestQueue queue;
    private CustomDialog mDialogError;
    private int GALLERY_REQUEST = 103;
    private Uri imageToUpload = null;
    private String loadedImageId = "";

    // newInstance constructor for creating fragment with arguments
    public static EditProfileFragment newInstance(int page, String title) {
        EditProfileFragment fragmentFirst = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putInt("id", page);
        args.putString("title", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        V2FragmentProfileEditBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.v2_fragment_profile_edit, container, false);
        View view = binding.getRoot();
        ButterKnife.bind(this, view);


        //button listners
        userimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFromGallery();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSave();
            }
        });

        disableAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDisableAccount();
            }
        });

        logoutAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SessionsController.logOut();

                //restart the app
                ActivityCompat.finishAffinity(getActivity());
                startActivity(new Intent(getActivity(), SplashActivity.class));

            }
        });

        //Load data from profile user to the view presentation
        loadProfileData();

        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        gps = new GPStracker(this.getActivity());
        queue = VolleySingleton.getInstance(getActivity()).getRequestQueue();


    }

    protected void getFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }


    private void loadProfileData() {

        //Make sure that the user is connected before executing the api
        mUser = SessionsController.getSession().getUser();
        if (mUser != null) {
            if (mUser.getImages() != null) {
                Glide.with(this).load(mUser.getImages().getUrl200_200()).centerCrop().into(userimage);
            }
            loginTxt.setText(mUser.getUsername());
            emailTxt.setText(mUser.getEmail());
            fullNameTxt.setText(mUser.getName());
        }

    }

    private void doDisableAccount() {

        if (SessionsController.getSession() != null) {

            //Make sure that the user is connected before executing the api
            mUser = SessionsController.getSession().getUser();
            if (mUser != null) {

                final int user_id = mUser.getId();


                //Display Popup : Loading
                mPdialog = new ProgressDialog(getActivity());
                mPdialog.setMessage("Loading ...");
                mPdialog.setCancelable(false);
                mPdialog.show();


                SimpleRequest request = new SimpleRequest(Request.Method.POST,
                        Constances.API.API_DISABLE_ACCOUNT, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        mPdialog.dismiss();

                        try {

                            if (APP_DEBUG) {
                                NSLog.i("DisableAccountResponse", response);
                            }

                            JSONObject js = new JSONObject(response);

                            Parser mUserParser = new Parser(js);

                            int success = Integer.parseInt(mUserParser.getStringAttr(Tags.SUCCESS));

                            if (success == 1) {

                                //log out and restart the app
                                //logout
                                SessionsController.logOut();

                                //restart the app
                                ActivityCompat.finishAffinity(getActivity());
                                startActivity(new Intent(getActivity(), SplashActivity.class));

                            } else {

                                Map<String, String> errors = mUserParser.getErrors();
                                MessageDialog.newDialog(getActivity()).onCancelClick(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        MessageDialog.getInstance().hide();
                                    }
                                }).onOkClick(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        MessageDialog.getInstance().hide();
                                    }
                                }).setContent(Translator.print(CommunFunctions.convertMessages(errors), "Message showError")).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                            Map<String, String> errors = new HashMap<String, String>();
                            errors.put("JSONException:", "Try later \"Json parser\"");

                            MessageDialog.newDialog(getActivity()).onCancelClick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MessageDialog.getInstance().hide();
                                }
                            }).onOkClick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    MessageDialog.getInstance().hide();
                                }
                            }).setContent(Translator.print(CommunFunctions.convertMessages(errors), "Message showError")).show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (APP_DEBUG) {
                            NSLog.e("ERROR", error.toString());
                        }

                        mPdialog.dismiss();
                        Map<String, String> errors = new HashMap<String, String>();

                        errors.put("NetworkException:", getString(R.string.check_nework));
                        mDialogError = CommunFunctions.showErrors(errors, getContext());
                        mDialogError.setTitle(R.string.network_error);

                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();

                        //params.put("password", password.getText().toString().trim());
                        params.put("user_id", String.valueOf(user_id));
                        params.put("user_token", SessionsController.getSession().getToken());


                        if (APP_DEBUG) {
                            NSLog.e("Disable account", " params :" + params.toString());
                        }


                        return params;

                    }

                };


                request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                queue.add(request);

            }

        }


    }


    private void doSave() {


        if (SessionsController.getSession() != null) {

            //Make sure that the user is connected before executing the api
            mUser = SessionsController.getSession().getUser();
            if (mUser != null) {

                final String oldUsername = mUser.getUsername();
                final String idUser = String.valueOf(mUser.getId());


                //Display Popup : Loading
                mPdialog = new ProgressDialog(getActivity());
                mPdialog.setMessage("Loading ...");
                mPdialog.setCancelable(false);
                mPdialog.show();


                SimpleRequest request = new SimpleRequest(Request.Method.POST,
                        Constances.API.API_UPDATE_ACCOUNT, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        mPdialog.dismiss();

                        try {

                            if (APP_DEBUG) {
                                NSLog.i("response", response);
                            }

                            JSONObject js = new JSONObject(response);

                            UserParser mUserParser = new UserParser(js);

                            int success = Integer.parseInt(mUserParser.getStringAttr(Tags.SUCCESS));

                            if (success == 1) {

                                final List<User> list = mUserParser.getUser();
                                if (list.size() > 0) {

                                    if (imageToUpload != null)
                                        uploadImage(list.get(0).getId());

                                    if (APP_DEBUG)
                                        NSLog.i("__", "logged " + list.get(0).getUsername());

                                    SessionsController.logOut();
                                    (new Handler()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            SessionsController.createSession(list.get(0),list.get(0).getToken());
                                            //Toast.makeText(getContext(), "Profile detail has successfully updated", Toast.LENGTH_LONG).show();
                                        }
                                    }, 2000);


                                }


                            } else {


                                Map<String, String> errors = mUserParser.getErrors();


                                MessageDialog.newDialog(getActivity()).onCancelClick(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        MessageDialog.getInstance().hide();
                                    }
                                }).onOkClick(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        MessageDialog.getInstance().hide();
                                    }
                                }).setContent(Translator.print(CommunFunctions.convertMessages(errors), "Message showError")).show();

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();

                            Map<String, String> errors = new HashMap<String, String>();
                            errors.put("JSONException:", "Try later \"Json parser\"");

                            MessageDialog.newDialog(getActivity()).onCancelClick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MessageDialog.getInstance().hide();
                                }
                            }).onOkClick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    MessageDialog.getInstance().hide();
                                }
                            }).setContent(Translator.print(CommunFunctions.convertMessages(errors), "Message showError")).show();


                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (APP_DEBUG) {
                            NSLog.e("ERROR", error.toString());
                        }

                        mPdialog.dismiss();
                        Map<String, String> errors = new HashMap<String, String>();

                        errors.put("NetworkException:", getString(R.string.check_nework));
                        mDialogError = CommunFunctions.showErrors(errors, getContext());
                        mDialogError.setTitle(R.string.network_error);

                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();

                        //params.put("password", password.getText().toString().trim());
                        params.put("username", loginTxt.getText().toString().trim());
                        params.put("email", emailTxt.getText().toString().trim());
                        params.put("oldUsername", oldUsername);
                        params.put("name", fullNameTxt.getText().toString());
                        //params.put("phone", codeCountryString + "-" + phone.getText().toString().trim());
                        params.put("user_id", idUser);
                        params.put("image", loadedImageId);
                        params.put("lat", String.valueOf(gps.getLatitude()));
                        params.put("lng", String.valueOf(gps.getLongitude()));
                        params.put("token", Utils.getToken(getActivity().getApplicationContext()));
                        params.put("mac_adr", ServiceHandler.getMacAddr());
                        params.put("auth_type", "mobile");


                        if (APP_DEBUG) {
                            NSLog.e("EditProfile", " params :" + params.toString());
                        }


                        return params;

                    }

                };


                request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                queue.add(request);

            }

        }


    }


    private void uploadImage(final int uid) {

        Toast.makeText(getActivity(), getString(R.string.fileUploading), Toast.LENGTH_LONG).show();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_USER_UPLOAD64, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                // pdialog.dismiss();
                try {

                    if (APP_DEBUG)
                        NSLog.e("EditProfile", response);
                    JSONObject js = new JSONObject(response);

                    UserParser mUserParser = new UserParser(js);
                    int success = Integer.parseInt(mUserParser.getStringAttr(Tags.SUCCESS));
                    if (success == 1) {

                        final List<User> list = mUserParser.getUser();
                        if (list.size() > 0) {
                            SessionsController.updateSession(list.get(0));

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
                }
                //pdialog.dismiss();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                Bitmap bm = BitmapFactory.decodeFile(imageToUpload.getPath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                byte[] b = baos.toByteArray();
                String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                params.put("image", encodedImage);

                params.put("int_id", String.valueOf(uid));
                params.put("type", "user");


                //do else
                params.put("module_id", String.valueOf(uid));
                params.put("module", "user");

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST) {

            try {

                if (resultCode == Activity.RESULT_OK) {
                    try {
                        final Uri imageUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getActivity().getContentResolver().query(imageUri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();

                        String createNewFileDest = CommunFunctions.createImageFile(getActivity());
                        new ImageUtils.PrepareImagesData(
                                getActivity(),
                                picturePath,
                                bitmap,
                                createNewFileDest,
                                this).execute();

                        userimage.setImageBitmap(bitmap);
                    } catch (IOException e) {

                        if (AppContext.DEBUG)
                            e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getActivity(), "You haven't picked Image", Toast.LENGTH_LONG).show();
                }


            } catch (Exception e) {
                if (AppContext.DEBUG)
                    e.printStackTrace();
            }


        }
    }


    @Override
    public void onCompressed(String newPath, String oldPath) {

        if (APP_DEBUG)
            Toast.makeText(getContext(), "PATH:" + newPath, Toast.LENGTH_SHORT).show();

        File mFile = new File(newPath);

        Glide.with(this).load(mFile).centerCrop()
                .placeholder(R.drawable.profile_placeholder)
                .placeholder(ImageLoaderAnimation.glideLoader(getContext()))
                .into(userimage);

        imageToUpload = Uri.parse(newPath);
    }


}