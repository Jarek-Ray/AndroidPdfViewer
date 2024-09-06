/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.sample;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnScanTouchListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.Constants;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.util.SizeF;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.options)
public class PDFViewActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;

    public static final String SAMPLE_FILE = "sample.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    private static final String PDF_FILE_NETWORK = "http://172.16.106.60:9000/cloudsaas/upload/20210510/8ec608f4-7883-43f0-b052-556a7366c5aa.PDF";
    //"http://10.157.138.205/group1/M00/00/01/Cp2KzV7B9x2APQeaAADFFjPSGYM924.pdf";
    //"http://10.245.228.89:8888/2011_Test.pdf";

    private Handler handler;
    private long pdf_trun_time = 500000;

    @ViewById
    PDFView pdfView;

    @NonConfigurationInstance
    Uri uri;

    @NonConfigurationInstance
    Integer pageNumber = 0;

    String pdfFileName;

    @OptionsItem(R.id.pickFile)
    void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    @AfterViews
    void afterViews() {
        //解决加载模糊问题 1f，不显示缩略图
        Constants.THUMBNAIL_RATIO = 1f;
        Log.i("-------", "------afterViews" + "--");
        pdfView.setBackgroundColor(Color.LTGRAY);
        pdfView.setMidZoom(2f);
        pdfView.setMaxZoom(2f);
        pdfView.setMinZoom(1f);
        //pdfView.zoomTo(1.5f);
        //pdfView.disableDragScale();
        pdfView.enableDragScale();
        pdfView.setScanTouchListener(new OnScanTouchListener() {
            @Override
            public void onScanTouch(View v, MotionEvent event, float zoom) {
                Log.i("-------", "------onScanTouch--" + "-1->" + event.getAction() + "-->" + event.getPointerCount());
                Log.i("-------", "------onScanTouch--" + "-2->" + pdfView.getZoom() + "-->" + zoom);
            }
        });
        /*pdfView.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY, offsetX, offsetY;
            private double nLenStart, nLenEnd;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int nCnt = event.getPointerCount();
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && 2 == nCnt) {
                    for (int i = 0; i < nCnt; i++) {
                        float x = event.getX(i);
                        float y = event.getY(i);
                    }
                    int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
                    int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));
                    nLenStart = Math.sqrt((double) xlen * xlen + (double) ylen * ylen);
                    return false;
                }
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP && 2 == nCnt) {
                    for (int i = 0; i < nCnt; i++) {
                        float x = event.getX(i);
                        float y = event.getY(i);
                    }
                    int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
                    int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));
                    nLenEnd = Math.sqrt((double) xlen * xlen + (double) ylen * ylen);
                    if (nLenEnd > nLenStart)//通过两个手指开始距离和结束距离，来判断放大缩小
                    {
                        Toast.makeText(getApplicationContext(), "放大", 3000).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "缩小", 3000).show();
                    }
                    return false;
                }
                return false;
            }
        });*/
        pdfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("-------", "------onClick--" + "");
            }
        });
        if (uri != null) {
            displayFromUri(uri);
        } else {
            String fileName = SAMPLE_FILE;
            fileName = "sample1.pdf";
            fileName = "sample_2.pdf";
            fileName = "sample_3.pdf";
            displayFromAsset(fileName);
        }

        setTitle(pdfFileName);
    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;
        PDFView.Configurator pdfConfig = pdfView.fromAsset(assetFileName);
        //pdfView.fromUri(Uri.parse(PDF_FILE_NETWORK))
        PdfViewConfigUtils.PdfViewConfigListener configListener = new PdfViewConfigUtils.PdfViewConfigListener() {
            @Override
            public void onError(Throwable error) {

            }

            @Override
            public void onRender(int nbPages) {
                if (pdf_trun_time != 0) {
                    if (handler == null) {
                        handler = new Handler();
                    }
                    handler.postDelayed(goNextPageRunnable, pdf_trun_time);
                }
            }

            @Override
            public void onPageChange(int page, int pageCount) {
                SizeF pageSize = pdfView.getPageSize(page);
                float pageWidth = pageSize.getWidth();
                float pageHeight = pageSize.getHeight();
                Log.i("-------", "------onPageChange--" + pageSize.getWidth() + "->" + pageSize.getHeight());
                int width = pdfView.getWidth();
                int height = pdfView.getHeight();
                Log.i("-------", "------onPageChange--" + width + "->" + height);
                float ratioWidth = width / pageWidth;
                float ratioHeight = height / pageHeight;
                Log.i("-------", "------onPageChange--" + ratioWidth + "->" + ratioHeight);
                /*if(page > 0 && ratioWidth > 1.5){
                    pdfView.zoomTo(ratioWidth);
                } else {
                    pdfView.zoomTo(1f);
                }*/
            }

            @Override
            public void onDraw(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                Log.i("-------", "------onDraw--" + pageWidth + "->" + pageHeight + "->" + displayedPage);

            }

            @Override
            public void onLoad(int nbPages) {
              Log.i("-------", "------loadComplete--" + nbPages);
                SizeF pageSize = pdfView.getPageSize(pdfView.getCurrentPage());
                float pageWidth = pageSize.getWidth();
                float pageHeight = pageSize.getHeight();
                Log.i("-------", "------onPageChange--" + pageSize.getWidth() + "->" + pageSize.getHeight());
                int width = pdfView.getWidth();
                int height = pdfView.getHeight();
                Log.i("-------", "------onPageChange--" + width + "->" + height);
                float ratioWidth = width / pageWidth;
                float ratioHeight = height / pageHeight;
                if(ratioWidth > ratioHeight && pageWidth > 1.2){
                }
            }

            @Override
            public boolean onTap(MotionEvent event) {
                Log.i("-------", "------onTap--" + event.getAction());
                return false;
            }

            @Override
            public void onLongPress(MotionEvent event) {
                Log.i("-------", "------onLongPress--" + event.getAction());
            }
        };
        pdfConfig = PdfViewConfigUtils.doInitPdfViewConfig(pdfConfig, configListener);
        //设置播放页
        pdfConfig.pages(16, 17);
        pdfConfig.load();
        if (true) {
            return;
        }
        /*pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(0) // in dp
                .onPageError(this)
                .enableSwipe(true)
                .swipeHorizontal(true)
                .pageSnap(true)
                .autoSpacing(false)
                .pageFling(true)
                .pageFitPolicy(FitPolicy.HEIGHT)
                .load();*/
        //pdfView.fromFile(new File("/sdcard/Download/EB9-AMUT-067 REV.C.pdf"))//fromAsset(SAMPLE_FILE)
        pdfConfig//fromUri(Uri.parse(PDF_FILE_NETWORK))
                .defaultPage(pageNumber)
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(true)
                .enableDoubletap(true)
                .defaultPage(0)
                // allows to draw something on the current page, usually visible in the middle of the screen
                //.onDraw(onDrawListener)
                // allows to draw something on all pages, separately for every page. Called only for visible pages
                //.onDrawAll(this)
                .onLoad(this) // called after document is loaded and starts to be rendered
                .onPageChange(this)
                //.onPageScroll(this)
                //.onError(this)
                .onPageError(this)
                .onRender(new OnRenderListener() {
                    @Override
                    public void onInitiallyRendered(int nbPages) {
                        if (pdf_trun_time != 0) {
                            if (handler == null) {
                                handler = new Handler();
                            }
                            handler.postDelayed(goNextPageRunnable, pdf_trun_time);
                        }
                    }
                }) // called after document is rendered for the first time
                // called on single tap, return true if handled, false to toggle scroll handle visibility
                //.onTap(this)
                //.onLongPress(this)
                .enableAnnotationRendering(true) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                // spacing between pages in dp. To define spacing color, set view background
                .spacing(0)
                .autoSpacing(true) // add dynamic spacing to fit each page on its own on the screen
                //.linkHandler(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .pageSnap(true) // snap pages to screen boundaries
                .pageFling(true) // make a fling change only a single page like ViewPager
                .nightMode(false) // toggle night mode
                .load();

    }

    private void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(0) // in dp
                .onPageError(this)
                .onRender(new OnRenderListener() {
                    @Override
                    public void onInitiallyRendered(int nbPages) {
                        if (pdf_trun_time != 0) {
                            if (handler == null) {
                                handler = new Handler();
                            }
                            handler.postDelayed(goNextPageRunnable, pdf_trun_time);
                        }
                    }
                })
                .enableSwipe(true)
                .swipeHorizontal(true)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
    }

    private Runnable goNextPageRunnable = new Runnable() {
        @Override
        public void run() {
            if (pdf_trun_time != 0) {
                handler.postDelayed(this, pdf_trun_time);//设置循环时间，此处是5秒
                GoNextPage();
            }
        }
    };


    private void GoNextPage() {
        int totalPage = pdfView.getPageCount();
        int curPage = pdfView.getCurrentPage();
        int nextPage = 0;
        if (curPage < totalPage - 1) {
            nextPage = curPage + 1;
        } else {
            nextPage = 0;
        }

        pdfView.jumpTo(nextPage, true);
    }

    @OnActivityResult(REQUEST_CODE)
    public void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();

        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    /**
     * Listener for response to user permission request
     *
     * @param requestCode  Check that permission request code matches
     * @param permissions  Permissions that requested
     * @param grantResults Whether permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }
}
