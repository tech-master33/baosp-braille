package org.baosp.braille

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BrailleActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var serviceStatusText: TextView
    private lateinit var displayNameText: TextView
    private lateinit var openAccessibilityBtn: Button
    private lateinit var scanBtn: Button
    private lateinit var gradeToggleBtn: Button

    private var useGrade2 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_braille)

        statusText = findViewById(R.id.text_status)
        serviceStatusText = findViewById(R.id.text_service_status)
        displayNameText = findViewById(R.id.text_display_name)
        openAccessibilityBtn = findViewById(R.id.btn_open_accessibility)
        scanBtn = findViewById(R.id.btn_scan)
        gradeToggleBtn = findViewById(R.id.btn_grade_toggle)

        openAccessibilityBtn.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            announceForAccessibility(getString(R.string.opening_accessibility))
        }

        scanBtn.setOnClickListener {
            requestBluetoothAndScan()
        }

        gradeToggleBtn.setOnClickListener {
            useGrade2 = !useGrade2
            val label = if (useGrade2) getString(R.string.grade2) else getString(R.string.grade1)
            gradeToggleBtn.text = label
            gradeToggleBtn.contentDescription = getString(R.string.braille_grade_desc, label)
            announceForAccessibility(getString(R.string.grade_changed, label))
        }

        requestBluetoothPermissions()
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
        updateBluetoothStatus()
    }

    private fun updateServiceStatus() {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabled = am.isEnabled && am.isTouchExplorationEnabled
        if (enabled) {
            serviceStatusText.text = getString(R.string.service_active)
            serviceStatusText.contentDescription = getString(R.string.service_active)
        } else {
            serviceStatusText.text = getString(R.string.service_inactive)
            serviceStatusText.contentDescription = getString(R.string.service_inactive_desc)
        }
    }

    private fun updateBluetoothStatus() {
        val bm = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bm.adapter
        if (adapter == null || !adapter.isEnabled) {
            statusText.text = getString(R.string.bluetooth_off)
            statusText.contentDescription = getString(R.string.bluetooth_off_desc)
            scanBtn.isEnabled = false
        } else {
            statusText.text = getString(R.string.bluetooth_on)
            statusText.contentDescription = getString(R.string.bluetooth_on)
            scanBtn.isEnabled = true
            checkConnectedDisplay(adapter)
        }
    }

    private fun checkConnectedDisplay(adapter: BluetoothAdapter) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            Manifest.permission.BLUETOOTH_CONNECT else Manifest.permission.BLUETOOTH

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            displayNameText.text = getString(R.string.no_permission)
            return
        }

        val bonded = adapter.bondedDevices
        val brailleDisplay = bonded.firstOrNull { device ->
            BrailleDisplayService.knownDisplayNames.any { name ->
                device.name?.contains(name, ignoreCase = true) == true
            }
        }

        if (brailleDisplay != null) {
            val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
                brailleDisplay.name ?: getString(R.string.unknown_display)
            } else {
                getString(R.string.display_found)
            }
            displayNameText.text = getString(R.string.display_connected, name)
            displayNameText.contentDescription = getString(R.string.display_connected, name)
        } else {
            displayNameText.text = getString(R.string.no_display_paired)
            displayNameText.contentDescription = getString(R.string.no_display_paired)
        }
    }

    private fun requestBluetoothPermissions() {
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        if (perms.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, perms.toTypedArray(), REQUEST_BT)
        }
    }

    private fun requestBluetoothAndScan() {
        requestBluetoothPermissions()
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        startActivity(intent)
        announceForAccessibility(getString(R.string.opening_bluetooth))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BT) {
            updateBluetoothStatus()
        }
    }

    companion object {
        private const val REQUEST_BT = 1001
    }
}
