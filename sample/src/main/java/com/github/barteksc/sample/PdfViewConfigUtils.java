package com.github.barteksc.sample;

import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.ScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;

/**
 * This is PdfViewConfigUtils file.
 *
 * @author Jarek
 * @date 2023/12/20 14:17.
 * @fileName PdfViewConfigUtils
 * @package com.mts.smart.factory.sop.utils
 * @org www.maxnerva.com {云智汇(重庆)高新科技服务有限公司}
 * @copyright © 2023 Maxnerva
 * @email bob.qb.ran@maxnerva.com
 * @describe TODO
 * 1.$
 * TODO
 */
public class PdfViewConfigUtils {

    public abstract static class PdfViewConfigListener {
        /**
         * Load
         *
         * @param nbPages
         */
        public void onLoad(int nbPages) {
            //Log.d("--", "--onLoad>" + nbPages);
        }

        /**
         * Error
         *
         * @param error
         */
        public abstract void onError(Throwable error);

        /**
         * PageError
         *
         * @param page
         * @param error
         */
        public void onPageError(int page, Throwable error) {
            //Log.d("--", "--onPageError>" + page + "->" + error);
        }

        /**
         * Render
         *
         * @param nbPages
         */
        public abstract void onRender(int nbPages);

        /**
         * PageChange
         *
         * @param page
         * @param pageCount
         */
        public void onPageChange(int page, int pageCount) {
            //Log.d("--", "--onPageChange>" + page + "->" + pageCount);
        }

        /**
         * PageScroll
         *
         * @param page
         * @param positionOffset
         */
        public void onPageScroll(int page, float positionOffset) {
            //Log.d("--", "--onPageScroll>" + page + "->" + positionOffset);
        }

        /**
         * Draw
         *
         * @param canvas
         * @param canvas
         * @param pageWidth
         * @param pageHeight
         * @param displayedPage
         */
        public void onDraw(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
            //Log.d("--", "--onDraw>");
        }

        /**
         * DrawAll
         *
         * @param canvas
         * @param pageWidth
         * @param pageHeight
         * @param displayedPage
         */
        public void onDrawAll(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
            //Log.d("--", "--onDrawAll>");
        }

        /**
         * Tap
         *
         * @param e
         * @return
         */
        public boolean onTap(MotionEvent e) {
            //Log.d("--", "--onTap>" + e);
            return false;
        }

        /**
         * LongPress
         *
         * @param e
         */
        public void onLongPress(MotionEvent e) {
            //Log.d("--", "--onLongPress>" + e);
        }
    }

    /**
     * 初始化pdfView Config
     *
     * @param pdfConfig
     * @param configListener
     * @return
     */
    public static PDFView.Configurator doInitPdfViewConfig(PDFView.Configurator pdfConfig, PdfViewConfigListener configListener) {
        if (pdfConfig == null) {
            return null;
        }
        // allows to block changing pages using swipe
        pdfConfig.enableSwipe(true)
                //pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                .swipeHorizontal(true)
                //启动双击放大缩小
                .enableDoubletap(true)
                //设置默认显示第0页
                .defaultPage(0)
                //渲染风格（就像注释，颜色或表单）
                .enableAnnotationRendering(true)
                //密码
                .password(null)
                //滚动Handle
                .scrollHandle(null)
                // 改善低分辨率屏幕上的渲染
                .enableAntialiasing(true)
                // 页面间的间距。定义间距颜色，设置背景视图
                .spacing(0)
                // add dynamic spacing to fit each page on its own on the screen
                .autoSpacing(true)
                // 页面fit类型
                .pageFitPolicy(FitPolicy.BOTH)
                // snap pages to screen boundaries
                .pageSnap(true)
                // make a fling change only a single page like ViewPager
                .pageFling(true)
                // toggle night mode
                .nightMode(false)
                // fit screen each page
                .fitEachPage(true);
        if (configListener == null) {
            return pdfConfig;
        }
        //设置监听事件
        pdfConfig.onDraw((canvas, x, y, page) -> configListener.onDraw(canvas, x, y, page))
                //设置Draw监听
                .onDrawAll((canvas, x, y, pages) -> configListener.onDrawAll(canvas, x, y, pages))
                //设置Render监听
                .onRender(nbPages -> configListener.onRender(nbPages))
                //设置异常监听
                .onError(throwable -> configListener.onError(throwable))
                //设置加载监听
                .onLoad(nbPages -> configListener.onLoad(nbPages))
                //设置翻页监听
                .onPageChange((page, pageCount) -> configListener.onPageChange(page, pageCount))
                //设置页面滑动监听
                .onPageScroll((page, positionOffset) -> configListener.onPageScroll(page, positionOffset))
                //设置点击监听
                .onTap(motionEvent -> configListener.onTap(motionEvent))
                //设置页面错误监听
                .onPageError((page, throwable) -> configListener.onPageError(page, throwable))
                //设置长按监听
                .onLongPress(motionEvent -> configListener.onLongPress(motionEvent));

        return pdfConfig;
    }

    /**
     * 初始化pdfView Config
     *
     * @param pdfConfig
     * @param configListener
     * @param scrollHandle
     * @return
     */
    public static PDFView.Configurator doInitPdfViewConfig(PDFView.Configurator pdfConfig, PdfViewConfigListener configListener, ScrollHandle scrollHandle) {
        if (pdfConfig == null) {
            return null;
        }
        // allows to block changing pages using swipe
        pdfConfig.enableSwipe(true)
                //pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                .swipeHorizontal(true)
                //启动双击放大缩小
                .enableDoubletap(false)
                //设置默认显示第0页
                .defaultPage(0)
                //渲染风格（就像注释，颜色或表单）
                .enableAnnotationRendering(false)
                //密码
                .password(null)
                //滚动Handle
                .scrollHandle(scrollHandle)
                // 改善低分辨率屏幕上的渲染
                .enableAntialiasing(true)
                // 页面间的间距。定义间距颜色，设置背景视图
                .spacing(0)
                // add dynamic spacing to fit each page on its own on the screen
                .autoSpacing(true)
                // 页面fit类型
                .pageFitPolicy(FitPolicy.BOTH)
                // snap pages to screen boundaries
                .pageSnap(true)
                // make a fling change only a single page like ViewPager
                .pageFling(true)
                // toggle night mode
                .nightMode(false)
                // fit screen each page
                .fitEachPage(true);
        if (configListener == null) {
            return pdfConfig;
        }
        //设置监听事件
        pdfConfig.onDraw((canvas, x, y, page) -> configListener.onDraw(canvas, x, y, page))
                //设置Draw监听
                .onDrawAll((canvas, x, y, pages) -> configListener.onDrawAll(canvas, x, y, pages))
                //设置Render监听
                .onRender(nbPages -> configListener.onRender(nbPages))
                //设置异常监听
                .onError(throwable -> configListener.onError(throwable))
                //设置加载监听
                .onLoad(nbPages -> configListener.onLoad(nbPages))
                //设置翻页监听
                .onPageChange((page, pageCount) -> configListener.onPageChange(page, pageCount))
                //设置页面滑动监听
                .onPageScroll((page, positionOffset) -> configListener.onPageScroll(page, positionOffset))
                //设置点击监听
                .onTap(motionEvent -> configListener.onTap(motionEvent))
                //设置页面错误监听
                .onPageError((page, throwable) -> configListener.onPageError(page, throwable))
                //设置长按监听
                .onLongPress(motionEvent -> configListener.onLongPress(motionEvent));

        return pdfConfig;
    }

}
