package com.fnspl.hiplaedu_student.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.drawable.PictureDrawable;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.navigine.naviginesdk.LocationPoint;
import com.navigine.naviginesdk.NativeUtils;
import com.navigine.naviginesdk.NavigineSDK;
import com.navigine.naviginesdk.SubLocation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class LocationView extends FrameLayout {
    public static String TAG = "NAVIGINE_SDK.LocationView";
    private boolean mWebViewLoading = true;
    private WebView mWebView = null;
    private ImageView mImageView = null;
    private PictureDrawable mPicDrawable = null;
    private Handler mHandler = new Handler();
    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    private float mDisplayDensity = 1.0F;
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private int mImageWidth = 0;
    private int mImageHeight = 0;
    private float mMapWidth = 0.0F;
    private float mMapHeight = 0.0F;
    private float mCenterX = 0.0F;
    private float mCenterY = 0.0F;
    private float mRatio = 1.0F;
    private float mMinRatio = 1.0F;
    private float mMaxRatio = 30.0F;
    private static final int TOUCH_MODE_SCROLL = 1;
    private static final int TOUCH_MODE_ZOOM = 2;
    private static final int TOUCH_MODE_ROTATE = 3;
    private static final int TOUCH_SENSITIVITY = 20;
    private static final int TOUCH_SHORT_TIMEOUT = 200;
    private static final int TOUCH_LONG_TIMEOUT = 600;
    private long mTouchTime = 0L;
    private int mTouchMode = 0;
    private int mTouchLength = 0;
    private PointF[] mTouchPoints = new PointF[]{new PointF(0.0F, 0.0F), new PointF(0.0F, 0.0F), new PointF(0.0F, 0.0F)};
    LocationView.Listener mListener = null;
    private float mRatio1 = 1.0F;
    final Runnable mRunnable = new Runnable() {
        public void run() {
            if(!LocationView.this.mWebViewLoading) {

                if(Math.abs(LocationView.this.mRatio - LocationView.this.mRatio1) >= 1.0E-6F) {
                    if(VERSION.SDK_INT >= 21) {
                        LocationView.this.mWebView.zoomBy(LocationView.this.mRatio / LocationView.this.mRatio1);
                    } else {
                        LocationView.this.mWebView.setInitialScale(Math.round(LocationView.this.mRatio * 100.0F));
                   }

                    LocationView.this.mRatio1 = LocationView.this.mRatio;
                }
                int scrollX = (int)(LocationView.this.mCenterX - (float)(LocationView.this.mViewWidth / 2));
                int scrollY = (int)(LocationView.this.mCenterY - (float)(LocationView.this.mViewHeight / 2));
                int offsetX = 0;
                if(scrollX < 0) {
                    offsetX = -scrollX;
                    scrollX = 0;
                }
                int offsetY = 0;
                if(scrollY < 0) {
                    offsetY = -scrollY;
                    scrollY = 0;
                }
                if(LocationView.this.mListener != null && LocationView.this.mPicDrawable != null) {
                    LocationView.this.mWebView.setTranslationX((float)offsetX);
                    LocationView.this.mWebView.setTranslationY((float)offsetY);
                    LocationView.this.mWebView.scrollTo(scrollX, scrollY);

                    Picture pic = LocationView.this.mPicDrawable.getPicture();
                    Canvas canvas = pic.beginRecording(LocationView.this.mViewWidth, LocationView.this.mViewHeight);
                    LocationView.this.mListener.onDraw(canvas);
                    pic.endRecording();
                    LocationView.this.mImageView.invalidate();

                    //Log.d("Test", "Offset: "+offsetX+" "+offsetY+" |");
                }

            }
        }
    };
    final Runnable mRefreshRunnable = new Runnable() {
        public void run() {

            long timeNow = NavigineSDK.currentTimeMillis();
            if(LocationView.this.mTouchTime > 0L && LocationView.this.mTouchTime + 600L < timeNow && (float) LocationView.this.mTouchLength < 20.0F * LocationView.this.mDisplayDensity) {
                if(LocationView.this.mListener != null) {
                    LocationView.this.mListener.onLongClick(LocationView.this.mTouchPoints[0].x, LocationView.this.mTouchPoints[0].y);
                }

                LocationView.this.mTouchLength = 0;
                LocationView.this.mTouchTime = 0L;
            }

        }
    };

    public LocationView(Context context) {
        super(context);
        this.mWebView = new WebView(context);
        this.mImageView = new ImageView(context);
        this.addView(this.mWebView);
        this.addView(this.mImageView);
        this.init(context);
    }

    public LocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mWebView = new WebView(context, attrs);
        this.mImageView = new ImageView(context, attrs);
        LayoutParams layoutParams = this.generateLayoutParams(attrs);
        this.addView(this.mWebView, layoutParams);
        this.addView(this.mImageView, layoutParams);
        this.init(context);
    }

    public LocationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mWebView = new WebView(context, attrs, defStyleAttr);
        this.mImageView = new ImageView(context, attrs, defStyleAttr);
        LayoutParams layoutParams = this.generateLayoutParams(attrs);
        this.addView(this.mWebView, layoutParams);
        this.addView(this.mImageView, layoutParams);
        this.init(context);
    }

    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        this.mWebView.setBackgroundColor(color);
    }

    public void setZoomRatio(float ratio) {
        this.mRatio = ratio;
    }

    private void init(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        this.mDisplayDensity = displayMetrics.density;
        this.mImageView.setBackgroundColor(Color.argb(0, 0, 0, 0));
        this.mImageView.setLayerType(1, (Paint)null);
        this.mImageView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                LocationView.this.doTouch(event);
                return true;
            }
        });
        this.mWebView.setEnabled(false);
        this.mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {

                //This method is not called before version 23. So the logic is moved to onPageStarted

                /*LocationView.this.mWebViewLoading = false;
                if(LocationView.this.mImageWidth > 0 && LocationView.this.mImageHeight > 0 && LocationView.this.mViewWidth > 0 && LocationView.this.mViewHeight > 0) {
                    LocationView.this.initMap();
                }*/
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //Toast.makeText(mContext, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                LocationView.this.mWebViewLoading = false;
                if(LocationView.this.mImageWidth > 0 && LocationView.this.mImageHeight > 0 && LocationView.this.mViewWidth > 0 && LocationView.this.mViewHeight > 0) {
                    LocationView.this.initMap();
                }

            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);

            }

        });
        MarginLayoutParams layoutParams = (MarginLayoutParams)this.mWebView.getLayoutParams();
        layoutParams.leftMargin = 0;
        layoutParams.topMargin = 0;
        layoutParams.rightMargin = 0;
        layoutParams.bottomMargin = 0;
        this.mWebView.setLayoutParams(layoutParams);
        this.mTimerTask = new TimerTask() {
            public void run() {
                LocationView.this.mHandler.post(LocationView.this.mRefreshRunnable);
            }
        };
        this.mTimer.schedule(this.mTimerTask, 100L, 100L);
    }

    private void initMap() {
        this.mMinRatio = Math.min((float)this.mViewWidth / (float)this.mImageWidth, (float)this.mViewHeight / (float)this.mImageHeight);
        this.mMaxRatio = Math.max((float)this.mViewWidth / (float)this.mImageWidth * this.mMapWidth, (float)this.mViewHeight / (float)this.mImageHeight * this.mMapHeight);
//        this.mRatio = this.mRatio1 = this.mMinRatio=0.9f;
        this.mRatio = this.mRatio1 = this.mMinRatio;
        this.mCenterX = (float)this.mImageWidth * this.mRatio / 2.0F;//414f;//
        this.mCenterY = (float)this.mImageHeight * this.mRatio / 2.0F;//852f;//
        this.mWebView.setInitialScale(Math.round(this.mMinRatio * 100.0F));
        Picture pic = new Picture();
        pic.beginRecording(this.mViewWidth, this.mViewHeight);
        pic.endRecording();
        this.mPicDrawable = new PictureDrawable(pic);
        this.mImageView.setImageDrawable(this.mPicDrawable);
        this.mImageView.bringToFront();
        this.mHandler.post(this.mRunnable);

        if(mListener!=null){
            mListener.mapInitialized();
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mViewWidth = w;
        this.mViewHeight = h;
        if(this.mImageWidth > 0 && this.mImageHeight > 0 && !this.mWebViewLoading) {
            this.initMap();
        }
    }

    private void doTouch(MotionEvent event) {
        if(this.mPicDrawable != null) {
            long timeNow = NavigineSDK.currentTimeMillis();
            int actionMask = event.getActionMasked();
            int pointerIndex = event.getActionIndex();
            int pointerCount = event.getPointerCount();
            PointF[] points = new PointF[pointerCount];
            for(int oldDist = 0; oldDist < pointerCount; ++oldDist) {
                points[oldDist] = new PointF(event.getX(oldDist), event.getY(oldDist));
            }
            switch(actionMask) {
                case 0:
                    this.mTouchPoints[0].set(points[0]);
                    this.mTouchTime = timeNow;
                    this.mTouchMode = 0;
                    this.mTouchLength = 0;
                    return;
                case 1:
                    if(this.mTouchTime > 0L && this.mTouchTime + 200L > timeNow && (float)this.mTouchLength < 20.0F * this.mDisplayDensity && this.mListener != null) {
                        this.mListener.onClick(this.mTouchPoints[0].x, this.mTouchPoints[0].y);
                    }
                    this.mTouchTime = 0L;
                    this.mTouchMode = 0;
                    this.mTouchLength = 0;
                    return;
                case 2:
                    float newDist;
                    float var11;
                    if(pointerCount == 1) {
                        if(this.mTouchMode == 1) {
                            var11 = points[0].x - this.mTouchPoints[0].x;
                            newDist = points[0].y - this.mTouchPoints[0].y;
                            this.mTouchLength = (int)((float)this.mTouchLength + Math.abs(var11));
                            this.mTouchLength = (int)((float)this.mTouchLength + Math.abs(newDist));
                            if((float)this.mTouchLength > 20.0F * this.mDisplayDensity) {
                                this.mTouchTime = 0L;
                            }
                            this.scrollBy(var11, newDist);
                        }
                        this.mTouchMode = 1;
                        this.mTouchPoints[0].set(points[0]);
                    } else if(pointerCount == 2) {
                        if(this.mTouchMode == 2 && VERSION.SDK_INT >= 21) {
                            var11 = PointF.length(this.mTouchPoints[0].x - this.mTouchPoints[1].x, this.mTouchPoints[0].y - this.mTouchPoints[1].y);
                            newDist = PointF.length(points[0].x - points[1].x, points[0].y - points[1].y);
                            var11 = Math.max(var11, 1.0F);
                            newDist = Math.max(newDist, 1.0F);
                            float ratio = newDist / var11;
                            this.zoomBy(ratio);
                        }
                        this.mTouchMode = 2;
                        this.mTouchPoints[0].set(points[0]);
                        this.mTouchPoints[1].set(points[1]);
                    }
                    return;
                default:
                    this.mTouchTime = 0L;
                    this.mTouchMode = 0;
                    this.mTouchLength = 0;
            }
        }
    }

    private void printState() {
      //  Log.d(TAG, String.format(Locale.ENGLISH, "Center", new Object[]{Float.valueOf(this.mCenterX), Float.valueOf(this.mCenterY), Float.valueOf((float)this.mImageWidth * this.mRatio), Float.valueOf((float)this.mImageHeight * this.mRatio), Float.valueOf(this.mRatio), Float.valueOf(this.mWebView.getTranslationX()), Float.valueOf(this.mWebView.getTranslationY()), Integer.valueOf(this.mWebView.getScrollX()), Integer.valueOf(this.mWebView.getScrollY())}));
    }

    public void scrollBy(float deltaX, float deltaY) {
        if(this.mPicDrawable != null) {
            float minDeltaX = 0.0F;
            float maxDeltaX = 0.0F;
            if((float)this.mImageWidth * this.mRatio > (float)this.mViewWidth) {
                minDeltaX = this.mCenterX - ((float)this.mImageWidth * this.mRatio - (float)(this.mViewWidth / 2));
                maxDeltaX = this.mCenterX - (float)(this.mViewWidth / 2);
            }
            float minDeltaY = 0.0F;
            float maxDeltaY = 0.0F;
            if((float)this.mImageHeight * this.mRatio > (float)this.mViewHeight) {
                minDeltaY = this.mCenterY - ((float)this.mImageHeight * this.mRatio - (float)(this.mViewHeight / 2));
                maxDeltaY = this.mCenterY - (float)(this.mViewHeight / 2);
            }

            deltaX = Math.max(Math.min(deltaX, maxDeltaX), minDeltaX);
            deltaY = Math.max(Math.min(deltaY, maxDeltaY), minDeltaY);
            this.mCenterX -= deltaX;
            this.mCenterY -= deltaY;
            this.mHandler.post(this.mRunnable);
            if(this.mListener != null) {
                this.mListener.onScroll(this.mCenterX, this.mCenterY);
            }
        }
    }

    public void zoomBy(float ratio) {
        if(this.mPicDrawable != null) {
            float r = Math.max(Math.min(ratio, this.mMaxRatio / this.mRatio), this.mMinRatio / this.mRatio);
            this.mRatio *= r;
            this.mCenterX *= r;
            this.mCenterY *= r;
            if((float)this.mImageWidth * this.mRatio > (float)this.mViewWidth) {
                this.mCenterX = Math.min(this.mCenterX, (float)this.mImageWidth * this.mRatio - (float)(this.mViewWidth / 2));
                this.mCenterX = Math.max(this.mCenterX, (float)(this.mViewWidth / 2));
            } else {
                this.mCenterX = (float)this.mImageWidth * this.mRatio / 2.0F;
            }

            if((float)this.mImageHeight * this.mRatio > (float)this.mViewHeight) {
                this.mCenterY = Math.min(this.mCenterY, (float)this.mImageHeight * this.mRatio - (float)(this.mViewHeight / 2));
                this.mCenterY = Math.max(this.mCenterY, (float)(this.mViewHeight / 2));
            } else {
                this.mCenterY = (float)this.mImageHeight * this.mRatio / 2.0F;
            }

            this.mHandler.post(this.mRunnable);
            if(this.mListener != null) {
                this.mListener.onZoom(this.mRatio);
            }
        }
    }

    public PointF getScreenCenter() {
        return new PointF((float)(this.mViewWidth / 2), (float)(this.mViewHeight / 2));
    }

    public PointF getScreenCoordinates(LocationPoint P) {
        return this.getScreenCoordinates(P.x, P.y);
    }

    public PointF getScreenCoordinates(PointF P) {
        return this.getScreenCoordinates(P.x, P.y);
    }

    public PointF getScreenCoordinates(float x, float y) {
        float dx = x * (float)this.mImageWidth * this.mRatio / this.mMapWidth;
        float dy = (this.mMapHeight - y) / this.mMapHeight * (float)this.mImageHeight * this.mRatio;
        return new PointF(dx - this.mCenterX + (float)(this.mViewWidth / 2), dy - this.mCenterY + (float)(this.mViewHeight / 2));
    }

    public float getScreenLengthX(float d) {
        return d * (float)this.mImageWidth * this.mRatio / this.mMapWidth;
    }

    public float getScreenLengthY(float d) {
        return d * (float)this.mImageHeight * this.mRatio / this.mMapHeight;
    }

    public PointF getAbsCoordinates(PointF P) {
        return this.getAbsCoordinates(P.x, P.y);
    }

    public PointF getAbsCoordinates(LocationPoint P) {
        return this.getAbsCoordinates(P.x, P.y);
    }

    public PointF getAbsCoordinates(float x, float y) {
        float dx = x + this.mCenterX - (float)(this.mViewWidth / 2);
        float dy = y + this.mCenterY - (float)(this.mViewHeight / 2);
        return new PointF(this.mMapWidth * dx / this.mRatio / (float)this.mImageWidth, this.mMapHeight * (1.0F - dy / this.mRatio / (float)this.mImageHeight));
    }

    public float getAbsLengthX(float d) {
        return d * this.mMapWidth / (float)this.mImageWidth / this.mRatio;
    }

    public float getAbsLengthY(float d) {
        return d * this.mMapHeight / (float)this.mImageHeight / this.mRatio;
    }

    public boolean loadSubLocation(SubLocation subLoc) {
        if(subLoc == null) {
            return false;
        } else if(subLoc.width >= 1.0F && subLoc.height >= 1.0F) {
            this.mPicDrawable = null;
            this.mMapWidth = subLoc.width;
            this.mMapHeight = subLoc.height;
            this.mImageWidth = 0;
            this.mImageHeight = 0;
            this.mCenterX = 0.0F;
            this.mCenterY = 0.0F;
            this.mRatio = 1.0F;
            this.mRatio1 = 1.0F;
            this.mMinRatio = 1.0F;
            this.mMaxRatio = 1.0F;
            this.mWebView.loadUrl("about:blank");
            this.mWebView.clearView();
            return subLoc.svgFile.length() > 0?this.loadSvgMap(subLoc.archiveFile, subLoc.svgFile):(subLoc.pngFile.length() > 0?this.loadPngMap(subLoc.archiveFile, subLoc.pngFile):(subLoc.jpgFile.length() > 0?this.loadJpgMap(subLoc.archiveFile, subLoc.jpgFile):false));
        } else {
            return false;
        }
    }

    private boolean loadSvgMap(String archiveFile, String svgFile) {
        byte[] data = NativeUtils.jniZipReadFile(archiveFile, svgFile);
        if(data == null) {
            return false;
        } else {
            try {
                DocumentBuilderFactory e = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = e.newDocumentBuilder();
                Document document = builder.parse(new ByteArrayInputStream(data));
                Element rootElem = document.getDocumentElement();
                if(!rootElem.getNodeName().equals("svg")) {
                   // Log.e(TAG, "Invalid or corrupted SVG file (no root \'svg\' element)!");
                    return false;
                }
                String widthStr = rootElem.getAttribute("width");
                String heightStr = rootElem.getAttribute("height");
                float width = 1.0F;
                float height = 1.0F;
                if(widthStr.endsWith("em")) {
                    widthStr = widthStr.substring(0, widthStr.length() - 2);
                    width = 12.0F;
                } else if(widthStr.endsWith("ex")) {
                    widthStr = widthStr.substring(0, widthStr.length() - 2);
                    width = 6.0F;
                } else if(widthStr.endsWith("px")) {
                    widthStr = widthStr.substring(0, widthStr.length() - 2);
                    width = 1.0F;
                } else if(widthStr.endsWith("in")) {
                    widthStr = widthStr.substring(0, widthStr.length() - 2);
                    width = 90.0F;
                } else if(widthStr.endsWith("cm")) {
                    widthStr = widthStr.substring(0, widthStr.length() - 2);
                    width = 35.44F;
                } else if(widthStr.endsWith("mm")) {
                    widthStr = widthStr.substring(0, widthStr.length() - 2);
                    width = 3.544F;
                } else if(widthStr.endsWith("pt")) {
                    widthStr = widthStr.substring(0, widthStr.length() - 2);
                    width = 1.25F;
                } else if(widthStr.endsWith("pc")) {
                    widthStr = widthStr.substring(0, widthStr.length() - 2);
                    width = 15.0F;
                }

                try {
                    width *= Float.parseFloat(widthStr);
                } catch (Throwable var14) {
                    width = 0.0F;
                }

                if(heightStr.endsWith("em")) {
                    heightStr = heightStr.substring(0, heightStr.length() - 2);
                    height = 12.0F;
                } else if(heightStr.endsWith("ex")) {
                    heightStr = heightStr.substring(0, heightStr.length() - 2);
                    height = 6.0F;
                } else if(heightStr.endsWith("px")) {
                    heightStr = heightStr.substring(0, heightStr.length() - 2);
                    height = 1.0F;
                } else if(heightStr.endsWith("in")) {
                    heightStr = heightStr.substring(0, heightStr.length() - 2);
                    height = 90.0F;
                } else if(heightStr.endsWith("cm")) {
                    heightStr = heightStr.substring(0, heightStr.length() - 2);
                    height = 35.44F;
                } else if(heightStr.endsWith("mm")) {
                    heightStr = heightStr.substring(0, heightStr.length() - 2);
                    height = 3.544F;
                } else if(heightStr.endsWith("pt")) {
                    heightStr = heightStr.substring(0, heightStr.length() - 2);
                    height = 1.25F;
                } else if(heightStr.endsWith("pc")) {
                    heightStr = heightStr.substring(0, heightStr.length() - 2);
                    height = 15.0F;
                }

                try {
                    height *= Float.parseFloat(heightStr);
                } catch (Throwable var13) {
                    height = 0.0F;
                }

                if(width <= 1.0F) {
                    width = 500.0F;
                }

                if(height <= 1.0F) {
                    height = 500.0F;
                }

                this.mImageWidth = Math.round(width);
                this.mImageHeight = Math.round(height);
            } catch (Throwable var15) {
               // Log.e(TAG, Log.getStackTraceString(var15));
                return false;
            }

            this.mWebViewLoading = true;
            this.mWebView.loadData(new String(data), "image/svg+xml", (String)null);
            return true;
        }
    }

    private boolean loadPngMap(String archiveFile, String pngFile) {
        byte[] data = NativeUtils.jniZipReadFile(archiveFile, pngFile);
        if(data == null) {
            return false;
        } else {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            this.mImageWidth = options.outWidth;
            this.mImageHeight = options.outHeight;
            if(this.mImageWidth != 0 && this.mImageHeight != 0) {
                File f = new File(archiveFile);

                try {
                    String e = f.getParent() + "/" + pngFile;
                    File f1 = new File(e);
                    if(!f1.exists() || f1.lastModified() < f.lastModified()) {
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(e));
                        bos.write(data);
                        bos.close();
                    }
                } catch (Throwable var9) {
                   /// Log.d(TAG, Log.getStackTraceString(var9));
                    return false;
                }

                this.mWebViewLoading = true;
                this.mWebView.loadDataWithBaseURL("file://" + f.getParent() + "/", "<html><body><div><img src=\"" + pngFile + "\"/></div></body></html>", "text/html", (String)null, (String)null);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean loadJpgMap(String archiveFile, String jpgFile) {
        return this.loadPngMap(archiveFile, jpgFile);
    }

    public void setListener(LocationView.Listener listener) {
        this.mListener = listener;
    }

    public void setMapSize(float width, float height) {
        this.mMapWidth = width;
        this.mMapHeight = height;
    }

    public float getMapWidth() {
        return this.mMapWidth;
    }

    public float getMapHeight() {
        return this.mMapHeight;
    }

    public void redraw() {
        this.mHandler.post(this.mRunnable);
    }

    public interface Listener {
        void onClick(float var1, float var2);

        void onLongClick(float var1, float var2);

        void onScroll(float var1, float var2);

        void onZoom(float var1);

        void onDraw(Canvas var1);

        void mapInitialized();
    }
}
