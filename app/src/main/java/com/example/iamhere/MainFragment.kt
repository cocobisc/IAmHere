package com.example.iamhere

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.iamhere.databinding.FragmentMainBinding
import com.example.iamhere.utils.Permissions
import com.example.iamhere.utils.checkPermissions
import com.example.iamhere.utils.showToast


@RequiresApi(Build.VERSION_CODES.S)
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    private val MIN_RSSI = -55

    private val bluetoothManager by lazy {
        requireContext().getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private val leScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private lateinit var br: BroadcastReceiver

    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @Override
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requireContext().showToast("No Permissions")
                        return
                    }
                    requireActivity().runOnUiThread {
                        setFindDeviceState(false)

                        binding.deviceInfo.text = "Connected device : ${gatt.device.name}"
                        binding.iconBluetooth.visibility = View.GONE
                        binding.iconDevice.visibility = View.VISIBLE
                        leScanner.stopScan(scanCallback)
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    requireContext().showToast("Device disconnected")
                }
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result?.device == null || result.rssi < MIN_RSSI) {
                return
            }

            connectDevice(result.device)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        initView()
        checkPermission()

        return binding.root
    }

    private fun checkPermission() {
        if (!requireContext().checkPermissions(Permissions.bluetooth)) {
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {
                if (!it.all { permission -> permission.value }) {
                    requireContext().showToast("Permission denied")
                }
            }.launch(Permissions.bluetooth)
        }
    }

    private fun connectDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (bluetoothAdapter.bondedDevices.contains(device)) {
            device.connectGatt(requireContext(), false, gattCallback)
        }
    }

    private fun initView() {
        binding.deviceInfo.text = "No device has been paired."

        binding.discoverableButton.setOnClickListener {
            val textView = it as TextView
            if (textView.text == "STOP") {
                setFindDeviceState(false)
            } else {
                setFindDeviceState(true)
            }
        }

        binding.bluetoothSettingButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }
    }

    private fun setFindDeviceState(enable: Boolean) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requireContext().showToast("Scan permission has been denied.")
            return
        }
        binding.iconBluetooth.visibility = View.VISIBLE
        binding.iconDevice.visibility = View.GONE
        binding.deviceInfo.text = "Finding connectable bluetooth device.."

        if (enable) {
            binding.discoverableButton.text = "STOP"
            binding.discoverableButton.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.red400,
                    null
                )
            )
            binding.pulse.start()
            leScanner.startScan(scanCallback)
        } else {
            binding.discoverableButton.text = "Find device"
            binding.discoverableButton.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.green400, null))
            binding.pulse.stop()

            leScanner.stopScan(scanCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        requireContext().unregisterReceiver(br)
    }
}