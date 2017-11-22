package library.san.library_ui.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

/**
 * author : jichang
 * time   : 2017/06/22
 * desc   :
 * 自动检测权限的 Activity ，因为不是所有的 Activity 都有这个需求，所以独立出来
 * 重写 getPermission4Check() 方法 ，在 onResume 时会进行检测,
 * 或者设置 mNeedCheck 为 false ，在需要时自行调用 {@link #checkPermission(String...)}
 * 下面两个方法可以重写来改变提示的界面
 * {@link #createDialogForPermission(String[], PermissionHelper)}
 * {@link #createDialogForSetting(PermissionHelper)}
 * version: 1.0
 */
public class PermissionActivity extends BaseActivity {

  private static final int PERMISSION_REQUEST_CODE = 44;
  protected boolean mNeedCheck = true;//是否需要请求权限

  @Override
  protected void onResume() {
    super.onResume();
    if (mNeedCheck) {
      mNeedCheck = false;
      checkPermission(getPermission4Check());
    }
  }


  //抽象方法，由子类来决定哪些权限需要申请(取消抽象,按需求重写)
  protected String[] getPermission4Check() {
    return null;
  }


  //写在基类的申请权限的回调
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode != PERMISSION_REQUEST_CODE) {
      return;
    }
    // 当申请权限时，home 键返回桌面，再进入程序，申请弹窗消失，回调 permissions 的 size 为0
    if (permissions.length == 0) {
      dialogForPermission(permissions);
      return;
    }
    for (int i = 0; i < grantResults.length; i++) {
      //权限被用户拒绝
      if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
        //且勾选了不再显示对话框
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
          dialogForSetting();
        } else {
          dialogForPermission(permissions);
        }
        break;
      }
    }
  }

  /**
   * 动态申请权限
   *
   * @param permissions 需要申请的权限
   * @return 是否拥有权限
   */
  protected boolean checkPermission(String... permissions) {

    //没有权限需要申请时
    if (permissions == null || permissions.length == 0) {
      return true;
    }
    //检查权限是否拒绝
    Boolean isDenied = false;
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
        isDenied = true;
        break;
      }
    }
    //不需申请
    if (!isDenied) {
      return true;
    }
    // 进行申请
    ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    return false;
  }


  /**
   * 提醒用户进入设置界面设置权限
   */
  private void dialogForSetting() {
    createDialogForSetting(new PermissionHelper() {
      @Override
      public void onAgree() {
        // 返回应用时检查权限
        mNeedCheck = true;
        // 隐式意图打开界面
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
      }

      @Override
      public void onRefuse() {
        finish();
      }
    });
  }

  /**
   * 进入系统设置的提示界面
   *
   * @param helper 一个接口，定义了接受和拒绝时的默认操作，可以重写 createDialogForSetting
   *               自定义提示界面，使用 helper 进行操作
   */
  protected void createDialogForSetting(final PermissionHelper helper) {
    new AlertDialog.Builder(this)
        .setTitle("提示")
        .setMessage("程序没有权限将无法正常运行,\n"
            + "请在接下来的对话框中点击“确认”按钮\n手动设置权限\n"
            + "点击“取消”将退出程序")
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            helper.onAgree();
          }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            helper.onRefuse();
          }
        })
        .setCancelable(false)
        .create()
        .show();
  }

  /**
   * 提醒用户需要权限并重新请求
   *
   * @param permissions 权限列表
   */
  private void dialogForPermission(final String... permissions) {
    createDialogForPermission(permissions, new PermissionHelper() {
      @Override
      public void onAgree() {
        ActivityCompat.requestPermissions(PermissionActivity.this, permissions,
            PERMISSION_REQUEST_CODE);
      }

      @Override
      public void onRefuse() {
        finish();
      }
    });
  }

  /**
   * 创建请求 权限的提示界面
   *
   * @param permissions 权限列表
   * @param helper      一个接口，定义了接受和拒绝时的默认操作，可以重写 createDialogForPermission
   *                    自定义提示界面，使用 helper 进行操作
   */
  protected void createDialogForPermission(final String[] permissions,
                                           final PermissionHelper helper) {
    new AlertDialog.Builder(this)
        .setTitle("提示")
        .setMessage("程序没有权限将无法正常运行,\n"
            + "请在接下来的对话框中点击“允许”\n点击“取消”将退出程序")
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            helper.onAgree();

          }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            helper.onRefuse();
          }
        })
        .setCancelable(false)
        .create()
        .show();
  }

  public interface PermissionHelper {
    void onAgree();

    void onRefuse();
  }

}
