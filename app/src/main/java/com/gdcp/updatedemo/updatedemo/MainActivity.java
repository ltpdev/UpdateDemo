package com.gdcp.updatedemo.updatedemo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gdcp.updatedemo.updatedemo.index.IndexContract;
import com.gdcp.updatedemo.updatedemo.index.IndexPresenter;
import com.gdcp.updatedemo.updatedemo.utils.SpUtils;

import java.io.File;
import java.util.Locale;

import static android.os.Process.killProcess;

public class MainActivity extends AppCompatActivity implements IndexContract.View {
    private IndexPresenter mPresenter;
    private TextView mTextView;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPresenter = new IndexPresenter(this);
        mTextView = findViewById(R.id.main_textView);
        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            String local = pi.versionName;
            mPresenter.checkUpdate(local);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
       /* mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTextView.getText().toString().equals("下载进度")) {
                    SpUtils.getInstance().putString("ignore","-1");
                    MainActivityPermissionsDispatcher.needStorageWithPermissionCheck(MainActivity.this);
                }
            }
        });
        MainActivityPermissionsDispatcher.needStorageWithPermissionCheck(this);*/
    }

    @Override
    public void showUpdate(final String version) {
        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(this)
                    .setTitle("检测到有新版本")
                    .setMessage("当前版本:" + version)
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPresenter.downApk(MainActivity.this);
                        }
                    })
                    .setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPresenter.setIgnore(version);
                        }
                    })
                    .create();
            //重写这俩个方法，一般是强制更新不能取消弹窗
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_BACK && mDialog != null && mDialog.isShowing();
                }
            });

        }
        mDialog.show();
    }

    @Override
    public void showProgress(int progress) {
        /*%s: 字符串类型，如："ljq"
%b: 布尔类型，如：true
%d: 整数类型(十进制)，如：99
%f: 浮点类型，如：99.99
%%: 百分比类型，如：％
%n: 换行符
        *
        * */
        mTextView.setText(String.format(Locale.CHINESE, "%d%%", progress));
    }

    @Override
    public void showFail(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showComplete(File file) {
        try {
            String authority = getApplicationContext().getPackageName() + ".fileProvider";
            Uri fileUri = FileProvider.getUriForFile(this, authority, file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            //7.0以上需要添加临时读取权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            } else {
                Uri uri = Uri.fromFile(file);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            }
            startActivity(intent);

            //弹出安装窗口把原程序关闭。
            //避免安装完毕点击打开时没反应
            killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        mPresenter.unbind(this);
        super.onDestroy();
    }

   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, "这位爷，在下载呢，待会再退出吧", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

}
