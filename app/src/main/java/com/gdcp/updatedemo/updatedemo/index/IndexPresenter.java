package com.gdcp.updatedemo.updatedemo.index;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.gdcp.updatedemo.updatedemo.utils.DownloadService;
import com.gdcp.updatedemo.updatedemo.utils.SpUtils;

import java.io.File;

/**
 * Created by asus- on 2018/1/5.
 */

public class IndexPresenter implements IndexContract.Presenter{
    private IndexContract.View view;
    private ServiceConnection connection;
    public IndexPresenter(IndexContract.View view) {
        this.view = view;
    }

    /**
     * 请求网络
     * 获取网络版本号
     * 获取成功后与本地版本号比对
     * 符合更新条件就控制view弹窗
     */
    @Override
    public void checkUpdate(String local) {
        //假设获取得到最新版本
        //一般还要和忽略的版本做比对。。这里就不累赘了
        //这里假设是网络获取的版本
        String version = "2.0";
        String ignore = SpUtils.getInstance().getString("ignore");
        if (!ignore.equals(version) && !ignore.equals(local)) {
            view.showUpdate(version);
        }
    }

    @Override
    public void setIgnore(String version) {
        SpUtils.getInstance().putString("ignore",version);
    }

    /**
     * 模拟网络下载
     */
    @Override
    public void downApk(Context context) {
        final String url = "https://dianfenqi.cn/data/ffmpeg/upload/images/android/20171206/dianfenqi.apk";
        if (connection == null){
             connection=new ServiceConnection() {
                 @Override
                 public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                     DownloadService.DownloadBinder binder= (DownloadService.DownloadBinder) iBinder;
                     DownloadService downloadService=binder.getService();
                     downloadService.downApk(url, new DownloadService.DownloadCallback() {
                         @Override
                         public void onPrepare() {

                         }

                         @Override
                         public void onProgress(int progress) {
                             view.showProgress(progress);
                         }

                         @Override
                         public void onComplete(File file) {
                             view.showComplete(file);
                         }

                         @Override
                         public void onFail(String msg) {
                             view.showFail(msg);
                         }
                     });
                 }

                 @Override
                 public void onServiceDisconnected(ComponentName componentName) {

                 }
             };
        }
        Intent intent = new Intent(context,DownloadService.class);
        context.bindService(intent, connection, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void unbind(Context context) {
        context.unbindService(connection);
    }




}
