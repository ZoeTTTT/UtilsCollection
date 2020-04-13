package com.iredfish.club;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;
import com.iredfish.club.util.ImageUtils;
import org.apache.commons.lang3.StringUtils;

public class DemoApplication extends Application {
    private static Context mContext;
    private Activity currentActivity;
    private long pic_pick;

    @Override public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private int activityStartCount = 0;
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                activityStartCount++;
                if (StringUtils.isNotEmpty(SessionUtils.getToken()) && activityStartCount == 1) {//判断是否在登录状态
                    currentActivity = activity;
                    String analyzedKeyUrl = ImageUtil.analyzeKeyStr(activity);
                    if (StringUtils.isNotEmpty(analyzedKeyUrl)) {
                        createShareDialog(analyzedKeyUrl);
                    } else {
                        Pair<Long, String> pair = ImageUtil.getLatestPhoto(mContext);
                        String analyzedImageUrl = ImageUtil.analyzingShareResource(pair, activity);
                        if (pic_pick != pair.first && null != analyzedImageUrl) {
                            pic_pick = pair.first;
                            createShareDialog(analyzedImageUrl);
                        }
                    }
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
               
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                activityStartCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public void createShareDialog(final String url) {
        //todo 通过url请求对应商品(或其他想要搜索的)信息，并显示商品信息弹框
        Toast.makeText(currentActivity, url, Toast.LENGTH_LONG).show();
    }
}
