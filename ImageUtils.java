package com.iredfish.club.util;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Pair;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.iredfish.club.R;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.decoding.DecodeFormatManager;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.mob.tools.utils.Strings.getString;

public class ImageUtils {

    public static String analyzingShareResource(Pair<Long, String> pair, Context context) {
        final String[] url = new String[1];
        if (null != pair) {
            String path = pair.second;
            RedfishCodeUtils.analyzeBitmap(path,
                new CodeUtils.AnalyzeCallback() {
                    @Override
                    public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                        if (result.contains("commodity")) {
                            url[0] = result;
                        }
                    }

                    @Override
                    public void onAnalyzeFailed() {
                        Toast.makeText(context, getString(R.string.album_qrcode_error),
                            Toast.LENGTH_LONG)
                            .show();
                    }
                });
        }
        return url[0];
    }

    /**
     * 搜索剪切板 查询分享口令
     */
    public static String analyzeKeyStr(Context context) {
        String analyzeKeyStr = null;
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription()
            .hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            ClipData cdText = clipboard.getPrimaryClip();
            ClipData.Item item = cdText.getItemAt(0);
            if (item.getText() != null) {
                String str = item.getText().toString();
                if (str.contains("redfish?share")) {
                    //todo 从analyzedKeyUrl提取口令中Api请求所需requestUrl
                    String requestUrl = "提取口令方法";
                    analyzeKeyStr = requestUrl;
                }
            }
        }
        return analyzeKeyStr;
    }

    /**
     * 获取相册中最新一张图片
     * Pair<Long,String>
     * Long为图片日期所对应的毫秒，String为图片路径
     */
    public static Pair<Long, String> getLatestPhoto(Context context) {
        Pair<Long, String> picturePair = null;
        Cursor cursor = getCursor(context, null, null);
        if (cursor.moveToFirst()) {
            picturePair = getPair(cursor);
        }

        //查询某个或者某多个相册中最近的一张图片
        /*String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";

        String CAMERA_IMAGE_BUCKET_ID = getBucketId(getCameraPath());
        String SCREENSHOTS_IMAGE_BUCKET_ID = getBucketId(getScreenshotsPath());

        String[] selectionArgsForCamera = { CAMERA_IMAGE_BUCKET_ID };
        String[] selectionArgsForScreenshots = { SCREENSHOTS_IMAGE_BUCKET_ID };

        Pair<Long, String> screenshotsPair = null;
        Pair<Long, String> cameraPair = null;

        cursor = getCursor(selection, selectionArgsForCamera);

        if (cursor.moveToFirst()) {
            cameraPair = getPair(cursor);
        }

        cursor = getCursor(selection, selectionArgsForScreenshots);
        if (cursor.moveToFirst()) {
            screenshotsPair = getPair(cursor);
        }

        if (cameraPair != null && screenshotsPair != null) {
            if (cameraPair.first > screenshotsPair.first) {
                return cameraPair;
            } else {
                return screenshotsPair;
            }
        } else if (cameraPair != null && screenshotsPair == null) {
            return cameraPair;
        } else if (cameraPair == null && screenshotsPair != null) {
            return screenshotsPair;
        }*/

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return picturePair;
    }

    /**
     * @param selection 过滤条件
     * @param selectionArgs 过滤值
     *
     * 二者为空值时查询即为整个手机相册
     */
    public static Cursor getCursor(Context context, String selection, String[] selectionArgs) {
        String[] projection = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_MODIFIED
        };
        return context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs,
            MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC");
    }

    /**
     * @return 对应图片的日期(毫秒)及路径
     */
    public static Pair<Long, String> getPair(Cursor cursor) {
        return new Pair(
            cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)),
            cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
    }

    /**
     * @return 过滤所需的Id
     */
    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    /**
     * @return 截图路径
     */
    public static String getScreenshotsPath() {
        String path = Environment.getExternalStorageDirectory().toString() + "/DCIM/Screenshots";
        File file = new File(path);
        if (!file.exists()) {
            path = Environment.getExternalStorageDirectory().toString() + "/Pictures/Screenshots";
        }
        return path;
    }

    /**
     * @return 相机照片路径
     */
    public static String getCameraPath() {
        return Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
    }

    /**
     * 清空剪贴板内容
     */
    public static void clearClipboard(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setText(null);
            } catch (Exception e) {

            }
        }
    }

    /**
     * 解析从相册选取的图片(二维码解析)
     *
     * @param path 本地图片路径
     */
    public static String decodeQRCode(String path) {
        Bitmap srcBitmap = BitmapFactory.decodeFile(path);
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>(2);
        // 可以解析的编码类型
        Vector<BarcodeFormat> decodeFormats = new Vector<>();
        if (decodeFormats.isEmpty()) {
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
        }

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        Result result = null;

        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        int[] pixels = new int[width * height];
        srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            result = reader.decode(binaryBitmap, hints);//开始解析
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
        }
        if (result != null) {
            return result.getText();
        }
        return null;
    }
}
