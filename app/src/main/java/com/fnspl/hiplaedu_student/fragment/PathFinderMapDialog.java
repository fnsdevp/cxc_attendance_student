package com.fnspl.hiplaedu_student.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.service.MapNavigationService;
import com.fnspl.hiplaedu_student.service.MyNavigationService;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.fnspl.hiplaedu_student.widget.LocationView;
import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.DevicePath;
import com.navigine.naviginesdk.Location;
import com.navigine.naviginesdk.LocationPoint;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;
import com.navigine.naviginesdk.SubLocation;
import com.navigine.naviginesdk.Venue;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 8/22/2017.
 */

public class PathFinderMapDialog extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "NAVIGINE.Demo";
    private static final int UPDATE_TIMEOUT = 50;  // milliseconds
    private static final int ADJUST_TIMEOUT = 5000; // milliseconds
    private static final int ERROR_MESSAGE_TIMEOUT = 5000; // milliseconds
    private static final boolean ORIENTATION_ENABLED = true; // Show device orientation?

    // UI Parameters
    private LocationView mLocationView = null;
    private Button mPrevFloorButton = null;
    private Button mNextFloorButton = null;
    private ImageView img_choose_path;
    private ImageView img_back;
    private LinearLayout ll_drop_down;
    private boolean isDropDownShowing = false;
    private View mBackView = null;
    private View mPrevFloorView = null;
    private View mNextFloorView = null;
    private View mZoomInView = null;
    private View mZoomOutView = null;
    private View mAdjustModeView = null;
    private TextView mCurrentFloorLabel = null;
    private TextView mErrorMessageLabel = null;
    private TextView mCoordinateLabel = null;
    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    private Handler mHandler = new Handler();

    private boolean mAdjustMode = false;
    private long mErrorMessageTime = 0;

    // Map parameters
    private long mAdjustTime = 0;

    // Location parameters
    private Location mLocation = null;
    private int mCurrentSubLocationIndex = -1;

    // Device parameters
    private DeviceInfo mDeviceInfo = null; // Current device
    private LocationPoint mPinPoint = null; // Potential device target
    private LocationPoint mTargetPoint = null; // Current device target
    private RectF mPinPointRect = null;

    private Bitmap mVenueBitmap = null;
    private Venue mTargetVenue = null;
    private Venue mSelectedVenue = null;
    private RectF mSelectedVenueRect = null;

    private boolean runForFirstTime = false;
    private boolean preDefineLocationSet=true;
    private View mView;
    private String PointX = null, PointY=null;
    private BroadcastReceiver mErrorReceiver;
    private BroadcastReceiver mReceiver;
    private int zoomCount = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        if(getActivity()!=null){
            getActivity().startService(new Intent(getActivity(), MapNavigationService.class));
        }

        mView = inflater.inflate(R.layout.fragment_navigine_layout_new, container, false);


        if(getArguments()!=null){
            PointX = getArguments().getString(CONST.POINTX);
            PointY = getArguments().getString(CONST.POINTY);
        }

        initNavigine(mView);
        setDropDownView(mView);
        return mView;
    }

    private void setDropDownView(View root) {
        ll_drop_down = (LinearLayout) root.findViewById(R.id.ll_drop_down);
        img_choose_path.setVisibility(View.GONE);
        img_choose_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zoomCount==0) {
                    if (!isDropDownShowing) {
                        isDropDownShowing = true;
                        ll_drop_down.setVisibility(View.VISIBLE);
                    } else {
                        isDropDownShowing = false;
                        ll_drop_down.setVisibility(View.GONE);
                    }
                }else{
                    Toast.makeText(getActivity(), "Zoom out first to select a new destination.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        root.findViewById(R.id.tv_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(mLocationView.
                        getAbsCoordinates(getXPoint(202f),getYPoint(1018f)));
            }
        });

        root.findViewById(R.id.tv_bangalore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(mLocationView.
                        getAbsCoordinates(getXPoint(199f),getYPoint(766f)));
            }
        });

        root.findViewById(R.id.tv_dubai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(mLocationView.
                        getAbsCoordinates(getXPoint(884f), getYPoint(1453f)));
            }
        });

        root.findViewById(R.id.tv_sanghai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(mLocationView.
                        getAbsCoordinates(getXPoint(306f), getYPoint(1461f)));
            }
        });

        root.findViewById(R.id.tv_los_angelese).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(mLocationView.
                        getAbsCoordinates(getXPoint(967f), getYPoint(984f)));
            }
        });

        root.findViewById(R.id.tv_sao_paulo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(mLocationView.
                        getAbsCoordinates(getXPoint(969f), getYPoint(756f)));
            }
        });

        root.findViewById(R.id.tv_edc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(mLocationView.
                        getAbsCoordinates(getXPoint(563f), getYPoint(1458f)));
            }
        });

        root.findViewById(R.id.tv_center_stage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(mLocationView.
                        getAbsCoordinates(getXPoint(582f), getYPoint(415f)));
            }
        });

    }

    private void initNavigine(View root) {
        // Setting up GUI parameters
        mLocationView = (LocationView) root.findViewById(R.id.navigation__location_view);
        mBackView = (View) root.findViewById(R.id.navigation__back_view);
        mPrevFloorButton = (Button) root.findViewById(R.id.navigation__prev_floor_button);
        mNextFloorButton = (Button) root.findViewById(R.id.navigation__next_floor_button);
        mPrevFloorView = (View) root.findViewById(R.id.navigation__prev_floor_view);
        mNextFloorView = (View) root.findViewById(R.id.navigation__next_floor_view);
        mCurrentFloorLabel = (TextView) root.findViewById(R.id.navigation__current_floor_label);
        mZoomInView = (View) root.findViewById(R.id.navigation__zoom_in_button);
        mZoomOutView = (View) root.findViewById(R.id.navigation__zoom_out_view);
        mAdjustModeView = (View) root.findViewById(R.id.navigation__adjust_mode_view);
        mErrorMessageLabel = (TextView) root.findViewById(R.id.navigation__error_message_label);
        mCoordinateLabel = (TextView) root.findViewById(R.id.navigation__coordinates);
        img_choose_path = (ImageView) root.findViewById(R.id.img_choose_path);
        img_back = (ImageView) root.findViewById(R.id.img_back);
        mLocationView.setBackgroundColor(getResources().getColor(R.color.dialogBgColor));

        mBackView.setVisibility(View.INVISIBLE);
        mPrevFloorView.setVisibility(View.INVISIBLE);
        mNextFloorView.setVisibility(View.INVISIBLE);
        mCurrentFloorLabel.setVisibility(View.INVISIBLE);
        mZoomInView.setVisibility(View.INVISIBLE);
        mZoomOutView.setVisibility(View.INVISIBLE);
        mAdjustModeView.setVisibility(View.INVISIBLE);
        mErrorMessageLabel.setVisibility(View.GONE);

        mZoomInView.setOnClickListener(this);
        mZoomOutView.setOnClickListener(this);
        mAdjustModeView.setOnClickListener(this);
        mErrorMessageLabel.setOnClickListener(this);
        mBackView.setOnClickListener(this);
        img_back.setOnClickListener(this);

        mVenueBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.elm_venue);

        // Setting up listener
        mLocationView.setListener
                (
                        new LocationView.Listener() {
                            @Override
                            public void onClick(float x, float y) {
                                handleClick(x, y);
                            }

                            @Override
                            public void onLongClick(float x, float y) {
                                Toast.makeText(getActivity(), "Point: " + x + "  " + y, Toast.LENGTH_LONG).show();
                                handleLongClick(x, y);
                            }

                            @Override
                            public void onScroll(float x, float y) {
                                mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
                            }

                            @Override
                            public void onZoom(float ratio) {
                                mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
                            }

                            @Override
                            public void onDraw(Canvas canvas) {
                                drawPoints(canvas);
                                drawVenues(canvas);
                                drawDevice(canvas);

                                if (runForFirstTime && preDefineLocationSet) {
                                    if(PointX!=null && PointY!=null) {
                                        setPredefinePointAndPath(mLocationView.
                                                getAbsCoordinates(getXPoint(Float.parseFloat(PointX)), getYPoint(Float.parseFloat(PointY))));
                                    }
                                }
                            }

                            @Override
                            public void mapInitialized() {

                            }
                        }
                );

        loadMap();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //super.onCreateDialog(savedInstanceState);

        Dialog dialog = new Dialog(getActivity());
        final RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        onCancelRoute();
        mHandler.removeCallbacks(mRunnable);
    }

    public void toggleAdjustMode() {
        mAdjustMode = !mAdjustMode;
        mAdjustTime = 0;
        Button adjustModeButton = (Button) mView.findViewById(R.id.navigation__adjust_mode_button);
        adjustModeButton.setBackgroundResource(mAdjustMode ?
                R.drawable.btn_adjust_mode_on :
                R.drawable.btn_adjust_mode_off);
        mHandler.post(mRunnable);
    }

    public void onNextFloor(View v) {
        if (loadNextSubLocation())
            mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
    }

    public void onPrevFloor() {
        if (loadPrevSubLocation())
            mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
    }

    public void onZoomIn() {
        zoomCount++;

        mLocationView.zoomBy(1.25f);
    }

    public void onZoomOut() {
        if(zoomCount>0){
            zoomCount--;
        }

        mLocationView.zoomBy(0.8f);
    }

    public void onMakeRoute() {
        if (MainApplication.Navigation == null)
            return;

        if (mPinPoint == null)
            return;

        mTargetPoint = mPinPoint;
        mTargetVenue = null;
        mPinPoint = null;
        mPinPointRect = null;

        MainApplication.Navigation.setTarget(mTargetPoint);
        mBackView.setVisibility(View.GONE);
        mHandler.post(mRunnable);
    }

    public void onCancelRoute() {
        preDefineLocationSet = false;

        if (MainApplication.Navigation == null)
            return;

        mTargetPoint = null;
        mTargetVenue = null;
        mPinPoint = null;
        mPinPointRect = null;

        MainApplication.Navigation.cancelTargets();
        mBackView.setVisibility(View.GONE);
        mHandler.post(mRunnable);
    }

    public void onCloseMessage() {
        mErrorMessageLabel.setVisibility(View.GONE);
        mErrorMessageTime = 0;
    }

    private void setErrorMessage(String message) {
        mErrorMessageLabel.setText(message);
        mErrorMessageLabel.setVisibility(View.VISIBLE);
        mErrorMessageTime = NavigineSDK.currentTimeMillis();
    }

    private void handleClick(float x, float y) {
        Log.d(TAG, String.format(Locale.ENGLISH, "Click at (%.2f, %.2f)", x, y));

        if (mPinPoint != null) {
            if (mPinPointRect.contains(x, y)) {
                mTargetPoint = mPinPoint;
                mTargetVenue = null;
                mPinPoint = null;
                mPinPointRect = null;
                MainApplication.Navigation.setTarget(mTargetPoint);
                mBackView.setVisibility(View.GONE);
                return;
            }
            cancelPin();
            return;
        }

        if (mSelectedVenue != null) {
            if (mSelectedVenueRect != null && mSelectedVenueRect.contains(x, y)) {
                if (mLocation == null || mCurrentSubLocationIndex < 0)
                    return;

                SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
                if (subLoc == null)
                    return;

                mTargetVenue = mSelectedVenue;
                mTargetPoint = null;

                MainApplication.Navigation.setTarget(new LocationPoint(subLoc.id, mTargetVenue.kx * subLoc.width, mTargetVenue.ky * subLoc.height));
                mBackView.setVisibility(View.GONE);
            }
            cancelVenue();
            return;
        }

        // Check if we touched venue
        mSelectedVenue = getVenueAt(x, y);
        mSelectedVenueRect = new RectF();

        mHandler.post(mRunnable);
    }

    private void handleLongClick(float x, float y) {
        Log.d(TAG, String.format(Locale.ENGLISH, "Long click at (%.2f, %.2f)", x, y));
        makePin(mLocationView.getAbsCoordinates(x, y));
        cancelVenue();
    }

    private boolean mMapLoaded = false;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    private boolean loadMap() {
        if (mMapLoaded)
            return false;
        mMapLoaded = true;

        if (MainApplication.Navigation == null) {
            Log.e(TAG, "Can't load map!");
            return false;
        }

        mLocation = MainApplication.Navigation.getLocation();
        mCurrentSubLocationIndex = -1;

        if (mLocation == null) {
            Log.e(TAG, "Loading map failed: no location");
            return false;
        }

        if (mLocation.subLocations.size() == 0) {
            Log.e(TAG, "Loading map failed: no sublocations");
            mLocation = null;
            return false;
        }

        if (!loadSubLocation(0)) {
            Log.e(TAG, "Loading map failed: unable to load default sublocation");
            mLocation = null;
            return false;
        }

        if (mLocation.subLocations.size() >= 2) {
            mPrevFloorView.setVisibility(View.VISIBLE);
            mNextFloorView.setVisibility(View.VISIBLE);
            mCurrentFloorLabel.setVisibility(View.VISIBLE);
        }
        mZoomInView.setVisibility(View.VISIBLE);
        mZoomOutView.setVisibility(View.VISIBLE);
        mAdjustModeView.setVisibility(View.VISIBLE);

        mHandler.post(mRunnable);
        MainApplication.Navigation.setMode(NavigationThread.MODE_NORMAL);
        return true;
    }

    private boolean loadSubLocation(int index) {
        if (MainApplication.Navigation == null)
            return false;

        if (mLocation == null || index < 0 || index >= mLocation.subLocations.size())
            return false;

        SubLocation subLoc = mLocation.subLocations.get(index);
        Log.d(TAG, String.format(Locale.ENGLISH, "Loading sublocation %s (%.2f x %.2f)", subLoc.name, subLoc.width, subLoc.height));

        if (subLoc.width < 1.0f || subLoc.height < 1.0f) {
            Log.e(TAG, String.format(Locale.ENGLISH, "Loading sublocation failed: invalid size: %.2f x %.2f", subLoc.width, subLoc.height));
            return false;
        }

        if (!mLocationView.loadSubLocation(subLoc)) {
            Log.e(TAG, "Loading sublocation failed: invalid image");
            return false;
        }

        mAdjustTime = 0;

        mCurrentSubLocationIndex = index;
        mCurrentFloorLabel.setText(String.format(Locale.ENGLISH, "%d", mCurrentSubLocationIndex));

        if (mCurrentSubLocationIndex > 0) {
            mPrevFloorButton.setEnabled(true);
            mPrevFloorView.setBackgroundColor(Color.parseColor("#90aaaaaa"));
        } else {
            mPrevFloorButton.setEnabled(false);
            mPrevFloorView.setBackgroundColor(Color.parseColor("#90dddddd"));
        }

        if (mCurrentSubLocationIndex + 1 < mLocation.subLocations.size()) {
            mNextFloorButton.setEnabled(true);
            mNextFloorView.setBackgroundColor(Color.parseColor("#90aaaaaa"));
        } else {
            mNextFloorButton.setEnabled(false);
            mNextFloorView.setBackgroundColor(Color.parseColor("#90dddddd"));
        }

        cancelVenue();
        mHandler.post(mRunnable);
        return true;
    }

    private boolean loadNextSubLocation() {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return false;
        return loadSubLocation(mCurrentSubLocationIndex + 1);
    }

    private boolean loadPrevSubLocation() {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return false;
        return loadSubLocation(mCurrentSubLocationIndex - 1);
    }

    //My defined function for show pre defined functions
    private void setPredefinePointAndPath(PointF P) {
        if (MainApplication.Navigation.getMode() == NavigationThread.MODE_IDLE)
            MainApplication.Navigation.setMode(NavigationThread.MODE_NORMAL);

        // Get device info from NavigationThread
        mDeviceInfo = Paper.book().read(MyNavigationService.DEVICE_LOCATION);


        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
            return;

        if (P.x < 0.0f || P.x > subLoc.width ||
                P.y < 0.0f || P.y > subLoc.height) {
            // Missing the map
            return;
        }

        if (mTargetPoint != null || mTargetVenue != null) {
            //setErrorMessage("Unable to make route: you must cancel the previous route first!");
            return;
        }

        if (mDeviceInfo!=null && mDeviceInfo.errorCode != 0) {
            //setErrorMessage("Unable to make route: navigation is not available!");
            return;
        }

        mPinPoint = new LocationPoint(subLoc.id, P.x, P.y);
        mPinPointRect = new RectF();

        onMakeRoute();
    }

    private void makePin(PointF P) {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
            return;

        if (P.x < 0.0f || P.x > subLoc.width ||
                P.y < 0.0f || P.y > subLoc.height) {
            // Missing the map
            return;
        }

        if (mTargetPoint != null || mTargetVenue != null) {
            //setErrorMessage("Unable to make route: you must cancel the previous route first!");
            return;
        }

        if (mDeviceInfo!=null && mDeviceInfo.errorCode != 0) {
            //setErrorMessage("Unable to make route: navigation is not available!");
            return;
        }

        mPinPoint = new LocationPoint(subLoc.id, P.x, P.y);
        mPinPointRect = new RectF();
        mHandler.post(mRunnable);
    }

    private void cancelPin() {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
            return;

        if (mTargetPoint != null || mTargetVenue != null || mPinPoint == null)
            return;

        mPinPoint = null;
        mPinPointRect = null;
        mHandler.post(mRunnable);
    }

    private void cancelVenue() {
        mSelectedVenue = null;
        mHandler.post(mRunnable);
    }

    private Venue getVenueAt(float x, float y) {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return null;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
            return null;

        Venue v0 = null;
        float d0 = 1000.0f;

        for (int i = 0; i < subLoc.venues.size(); ++i) {
            Venue v = subLoc.venues.get(i);
            if (v.subLocation != subLoc.id)
                continue;
            PointF P = mLocationView.getScreenCoordinates(v.kx * subLoc.width, v.ky * subLoc.height);
            float d = Math.abs(x - P.x) + Math.abs(y - P.y);
            if (d < 30.0f * MainApplication.DisplayDensity && d < d0) {
                v0 = new Venue(v);
                d0 = d;
            }
        }

        return v0;
    }

    private void drawPoints(Canvas canvas) {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        // Get current sublocation displayed
        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

        if (subLoc == null)
            return;

        final int solidColor = Color.argb(255, 64, 163, 205);  // Light-blue color
        final int circleColor = Color.argb(127, 64, 163, 205);  // Semi-transparent light-blue color
        final int arrowColor = Color.argb(255, 255, 255, 255); // White color
        final float dp = MainApplication.DisplayDensity;
        final float textSize = 16 * dp;

        // Preparing paints
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Drawing pin point (if it exists and belongs to the current sublocation)
        if (mPinPoint != null && mPinPoint.subLocation == subLoc.id) {
            final PointF T = mLocationView.getScreenCoordinates(mPinPoint);
            final float tRadius = 10 * dp;

            paint.setARGB(255, 0, 0, 0);
            paint.setStrokeWidth(4 * dp);
            canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

            paint.setColor(solidColor);
            paint.setStrokeWidth(0);
            canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);

            final String text = "Make route";
            final float textWidth = paint.measureText(text);
            final float h = 50 * dp;
            final float w = Math.max(120 * dp, textWidth + h / 2);
            final float x0 = T.x;
            final float y0 = T.y - 75 * dp;

            mPinPointRect.set(x0 - w / 2, y0 - h / 2, x0 + w / 2, y0 + h / 2);

            paint.setColor(solidColor);
            canvas.drawRoundRect(mPinPointRect, h / 2, h / 2, paint);

            paint.setARGB(255, 255, 255, 255);
            canvas.drawText(text, x0 - textWidth / 2, y0 + textSize / 4, paint);
        }

        // Drawing target point (if it exists and belongs to the current sublocation)
        if (mTargetPoint != null && mTargetPoint.subLocation == subLoc.id) {
            final PointF T = mLocationView.getScreenCoordinates(mTargetPoint);
            final float tRadius = 10 * dp;

            paint.setARGB(255, 0, 0, 0);
            paint.setStrokeWidth(4 * dp);
            canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

            paint.setColor(solidColor);
            canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);
        }
    }

    private void drawVenues(Canvas canvas) {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

        final float dp = MainApplication.DisplayDensity;
        final float textSize = 16 * dp;
        final float venueSize = 30 * dp;
        final int venueColor = Color.argb(255, 0xCD, 0x88, 0x50); // Venue color

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(0);
        paint.setColor(venueColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        for (int i = 0; i < subLoc.venues.size(); ++i) {
            Venue v = subLoc.venues.get(i);
            if (v.subLocation != subLoc.id)
                continue;

            final PointF P = mLocationView.getScreenCoordinates(v.kx * subLoc.width, v.ky * subLoc.height);
            final float x0 = P.x - venueSize / 2;
            final float y0 = P.y - venueSize / 2;
            final float x1 = P.x + venueSize / 2;
            final float y1 = P.y + venueSize / 2;
            canvas.drawBitmap(mVenueBitmap, null, new RectF(x0, y0, x1, y1), paint);
        }

        if (mSelectedVenue != null) {
            final PointF T = mLocationView.getScreenCoordinates(mSelectedVenue.kx * subLoc.width, mSelectedVenue.ky * subLoc.height);
            final float textWidth = paint.measureText(mSelectedVenue.name);

            final float h = 50 * dp;
            final float w = Math.max(120 * dp, textWidth + h / 2);
            final float x0 = T.x;
            final float y0 = T.y - 50 * dp;
            mSelectedVenueRect.set(x0 - w / 2, y0 - h / 2, x0 + w / 2, y0 + h / 2);

            paint.setColor(venueColor);
            canvas.drawRoundRect(mSelectedVenueRect, h / 2, h / 2, paint);

            paint.setARGB(255, 255, 255, 255);
            canvas.drawText(mSelectedVenue.name, x0 - textWidth / 2, y0 + textSize / 4, paint);
        }
    }

    private void drawDevice(Canvas canvas) {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        // Check if navigation is available
        if (mDeviceInfo!=null && mDeviceInfo.errorCode != 0)
            return;

        // Check if device belongs to the location loaded
        if (mDeviceInfo!=null && mDeviceInfo.location != mLocation.id)
            return;

        // Get current sublocation displayed
        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

        if (subLoc == null)
            return;

        final int solidColor = Color.argb(255, 64, 163, 205); // Light-blue color
        final int circleColor = Color.argb(127, 64, 163, 205); // Semi-transparent light-blue color
        final int arrowColor = Color.argb(127, 64, 163, 205); // White color
        final float dp = MainApplication.DisplayDensity;

        // Preparing paints
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        /// Drawing device path (if it exists)
        if (mDeviceInfo!=null && mDeviceInfo.paths != null && mDeviceInfo.paths.size() > 0) {
            DevicePath p = mDeviceInfo.paths.get(0);
            if (p.path.length >= 2) {
                paint.setColor(solidColor);

                for (int j = 1; j < p.path.length; ++j) {
                    LocationPoint P = p.path[j - 1];
                    LocationPoint Q = p.path[j];
                    if (P.subLocation == subLoc.id && Q.subLocation == subLoc.id) {
                        paint.setStrokeWidth(3 * dp);
                        PointF P1 = mLocationView.getScreenCoordinates(P);
                        PointF Q1 = mLocationView.getScreenCoordinates(Q);
                        canvas.drawLine(P1.x, P1.y, Q1.x, Q1.y, paint);
                    }
                }
            }
        }

        paint.setStrokeCap(Paint.Cap.BUTT);

        // Check if device belongs to the current sublocation
        if (mDeviceInfo!=null && mDeviceInfo.subLocation != subLoc.id)
            return;

        if(mDeviceInfo!=null) {
            final float x = mDeviceInfo.x;
            final float y = mDeviceInfo.y;
            final float r = mDeviceInfo.r;
            final float angle = mDeviceInfo.azimuth;

            final float sinA = (float) Math.sin(angle);
            final float cosA = (float) Math.cos(angle);
            final float radius = mLocationView.getScreenLengthX(r);  // External radius: navigation-determined, transparent
            final float radius1 = 15 * dp;// Internal radius: fixed, solid

            PointF O = mLocationView.getScreenCoordinates(x, y);
            PointF P = new PointF(O.x - radius1 * sinA * 0.22f, O.y + radius1 * cosA * 0.22f);
            PointF Q = new PointF(O.x + radius1 * sinA * 0.55f, O.y - radius1 * cosA * 0.55f);
            PointF R = new PointF(O.x + radius1 * cosA * 0.44f - radius1 * sinA * 0.55f, O.y + radius1 * sinA * 0.44f + radius1 * cosA * 0.55f);
            PointF S = new PointF(O.x - radius1 * cosA * 0.44f - radius1 * sinA * 0.55f, O.y - radius1 * sinA * 0.44f + radius1 * cosA * 0.55f);

            // Drawing transparent circle
            paint.setStrokeWidth(0);
            paint.setColor(circleColor);
            canvas.drawCircle(O.x, O.y, radius, paint);

            // Drawing solid circle
            paint.setColor(solidColor);
            canvas.drawCircle(O.x, O.y, radius1, paint);

            if (ORIENTATION_ENABLED) {
                // Drawing arrow
                paint.setColor(arrowColor);
                Path path = new Path();
                path.moveTo(Q.x, Q.y);
                path.lineTo(R.x, R.y);
                path.lineTo(P.x, P.y);
                path.lineTo(S.x, S.y);
                path.lineTo(Q.x, Q.y);
                canvas.drawPath(path, paint);
            }
        }
    }

    private void adjustDevice() {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        // Check if navigation is available
        if (mDeviceInfo!=null && mDeviceInfo.errorCode != 0)
            return;

        // Check if device belongs to the location loaded
        if (mDeviceInfo!=null && mDeviceInfo.location != mLocation.id)
            return;

        long timeNow = NavigineSDK.currentTimeMillis();

        // Adjust map, if necessary
        if (timeNow >= mAdjustTime) {
            // Firstly, set the correct sublocation
            SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
            if (mDeviceInfo!=null && mDeviceInfo.subLocation != subLoc.id) {
                for (int i = 0; i < mLocation.subLocations.size(); ++i)
                    if (mLocation.subLocations.get(i).id == mDeviceInfo.subLocation)
                        loadSubLocation(i);
            }

            // Secondly, adjust device to the center of the screen
            if(mDeviceInfo!=null) {
                PointF center = mLocationView.getScreenCoordinates(mDeviceInfo.x, mDeviceInfo.y);
                float deltaX = mLocationView.getWidth() / 2 - center.x;
                float deltaY = mLocationView.getHeight() / 2 - center.y;
                mAdjustTime = timeNow;
                mLocationView.scrollBy(deltaX, deltaY);
            }
        }
    }

    final Runnable mRunnable =
            new Runnable() {
                public void run() {
                    redrawMapAgain();
                }
            };

    private void redrawMapAgain(){
        if (MainApplication.Navigation == null) {
            Log.d(TAG, "Sorry, navigation is not supported on your device!");
            return;
        }

        final long timeNow = NavigineSDK.currentTimeMillis();

        if (mErrorMessageTime > 0 && timeNow > mErrorMessageTime + ERROR_MESSAGE_TIMEOUT) {
            mErrorMessageTime = 0;
            mErrorMessageLabel.setVisibility(View.GONE);
        }

        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        // Get current sublocation displayed
        //SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

        // Start navigation if necessary
        if (MainApplication.Navigation.getMode() == NavigationThread.MODE_IDLE)
            MainApplication.Navigation.setMode(NavigationThread.MODE_NORMAL);

        // Get device info from NavigationThread
        mDeviceInfo = Paper.book().read(MyNavigationService.DEVICE_LOCATION, null);
        if(mDeviceInfo!=null) {
            mCoordinateLabel.setText("Location X: " + mDeviceInfo.x + " Y: " + mDeviceInfo.y);

            if (mDeviceInfo.errorCode == 0) {
                mErrorMessageTime = 0;
                mErrorMessageLabel.setVisibility(View.GONE);

                if (mAdjustMode)
                    adjustDevice();

                if (mTargetPoint != null || mTargetVenue != null)
                    mBackView.setVisibility(View.GONE);
                else
                    mBackView.setVisibility(View.GONE);

                runForFirstTime = true;
            }
            // This causes map redrawing
            mLocationView.redraw();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        IntentFilter intentFilter1 = new IntentFilter(
                "android.intent.action.SUCCESSLOCATION");

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                /*if(!runForFirstTime){
                    loadMap();
                }*/
                mHandler.post(mRunnable);
            }
        };

        mErrorReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                setErrorMessage(intent.getStringExtra("error"));
                mBackView.setVisibility(View.GONE);
            }
        };
        //registering our receiver
        getActivity().registerReceiver(mReceiver, intentFilter);
        getActivity().registerReceiver(mErrorReceiver, intentFilter1);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(mReceiver);
        getActivity().unregisterReceiver(mErrorReceiver);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.navigation__zoom_in_button:
                onZoomIn();
                break;
            case R.id.navigation__zoom_out_view:
                onZoomOut();
                break;
            case R.id.navigation__adjust_mode_view:
                toggleAdjustMode();
                break;
            case R.id.navigation__error_message_label:
                onCloseMessage();
                break;
            case R.id.navigation__back_view:
                onCancelRoute();
                break;
            case R.id.img_back:
                dismiss();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(getActivity()!=null){
            getActivity().stopService(new Intent(getActivity(), MapNavigationService.class));
        }
    }

    public Float getXPoint(Float x){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        float convertedPoint = (width/1080f)*x;

        return convertedPoint;
    }

    public Float getYPoint(Float y){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        float convertedPoint = (height/1920f)*y;
        return convertedPoint;
    }

}
