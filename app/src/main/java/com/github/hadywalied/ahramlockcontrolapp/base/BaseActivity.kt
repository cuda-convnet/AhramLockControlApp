package com.github.hadywalied.ahramlockcontrolapp.base

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.REQUEST_CODE_REQUIRED_PERMISSIONS
import com.github.hadywalied.ahramlockcontrolapp.REQUEST_ENABLE_BT
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.disposables.CompositeDisposable as CompositeDisposable1


open class BaseActivity : AppCompatActivity() {

    protected val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    protected val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private lateinit var disposables: CompositeDisposable1

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        init()

    }

    override fun onStart() {
        super.onStart()
        if (!hasPermissions(this, requiredPermissions)) {
            requestPermissions(
                requiredPermissions,
                REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        }
        init()
    }

    override fun onResume() {
        super.onResume()
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(
                enableBtIntent,
                REQUEST_ENABLE_BT
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_CANCELED -> {
                Toast.makeText(this@BaseActivity, "Please Enable Bluetooth", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Returns `true` if the app was granted all the permissions. Otherwise, returns `false`.
     */
    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    protected val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private fun init() {
        initRx()
    }

    private fun initRx() {
        disposables = CompositeDisposable1()
    }

    @Synchronized
    protected fun addDisposable(disposable: @NonNull io.reactivex.rxjava3.disposables.Disposable) {
        if (disposable == null) return
        disposables.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!disposables.isDisposed) disposables.dispose()
    }

}