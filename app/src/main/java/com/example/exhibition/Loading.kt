package com.example.exhibition

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import bluetoothsup.*
import java.util.*
import kotlin.collections.HashMap


class Loading : AppCompatActivity() {

    //Additional
    private var Clicked: Boolean = true
    private var TimesActed: Int = 0

    //UUID
    private val MatchService = Match_Service
    private val Mobile_Number_Characteristic = Mobile_number_characteristic

    //BLE
    private lateinit var BleScaner: BluetoothLeScanner
    private lateinit var BA: BluetoothAdapter
    private lateinit var BluetoothGattServer: BluetoothGattServer
    private lateinit var Advertiser:BluetoothLeAdvertiser
    private val CreateProfile = CreateBluetoothService()

    //Func
    private val FoundedDevices = HashMap<String, String>()
    private val FoundToGatt = HashMap<String, BluetoothDevice?>()
    private val MobileNumber = Numbers
    private val Connection_List = Connection_List_Creator(FoundedDevices)

    //UI/UX
    private lateinit var Status: TextView
    private lateinit var AdvertiseButton: Button
    private lateinit var BLESwitch: Button

    private val Bluetooth_Status_Check = object : BluEnabledBroadcast() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            super.onReceive(p0, intent)
            TimesActed += 1
            if (TimesActed == 2) {
                chechBluetoothEnabled()
                Toast.makeText(
                    p0, "Status bluetooth active: ${BA.isEnabled}", Toast.LENGTH_LONG
                ).show()
                TimesActed = 0
            }
        }
    }
    private val BluetoothScannerCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        @RequiresApi(Build.VERSION_CODES.R)
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.i("Scanning", "Devices")
            val DefaultValue = "Unsigned"

            FoundToGatt[result?.device?.address ?: DefaultValue] = result?.device
            FoundedDevices[result?.device?.address ?: DefaultValue] = MobileNumber

            Status.setText(Connection_List)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLE scan is failed", errorCode.toString())
        }
    }
    private val GattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            Log.i("Server Status", "Working")
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            if (characteristic?.uuid.toString() == Mobile_Number_Characteristic.toString()) {
                BluetoothGattServer.sendResponse(device, 1, 1, 0, NumInBits )
                Log.e("Asked characteristic", "Mobile Number")
            }
        }

    }
    private val BlutoothGattConnectionCallBack = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.e("Paired", (newState==BluetoothProfile.STATE_CONNECTED).toString())

            if (BluetoothProfile.STATE_CONNECTED == newState) {
                this@Loading.runOnUiThread({Toast.makeText(this@Loading, "Connected", Toast.LENGTH_LONG).show()})

                gatt?.discoverServices()
            }

            else if (BluetoothProfile.STATE_CONNECTING == newState) { this@Loading.runOnUiThread( { Toast.makeText(this@Loading, "Connecting", Toast.LENGTH_LONG).show() })}
            else { this@Loading.runOnUiThread( { Toast.makeText(this@Loading, "Error", Toast.LENGTH_LONG).show()})}
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i("Services Status", "Discovered")
            val Characteristic=Get_Characteristic(gatt,MatchService,Mobile_Number_Characteristic)
            Log.e("!!!Value is", Characteristic?.value.toString())
            gatt?.readCharacteristic(Characteristic)
            gatt?.setCharacteristicNotification(Characteristic, true)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            val info=characteristic?.value.toString()
            Log.e("Received from Client", info)
            Status.setText(info)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?){
            super.onCharacteristicChanged(gatt, characteristic)
            Status.setText(characteristic?.value.toString())
        }
    }
    private val AdvertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.i("Advertising state", "Success")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e("Advertising State", "Failed")
        }
    }

    //Главное
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        Status = findViewById(R.id.Status)

        AdvertiseButton = findViewById(R.id.Advertise_Button)
        BLESwitch = findViewById(R.id.Connect_or_disconnect_Buttton)
        ActivityCompat.requestPermissions(this, String_Permissions,123)

        AdvertiseButton.setOnClickListener { Advertise() }
        BLESwitch.setOnClickListener { Connect_Or_Disconnect() }
        initiate()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        chechBluetoothEnabled()

        IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED).also {
            registerReceiver(Bluetooth_Status_Check, it)
        }

        getDevices()
    }

    @SuppressLint("MissingPermission")
    override fun onStop() {
        super.onStop()
        BleScaner.stopScan(BluetoothScannerCallback)
        Advertiser.stopAdvertising(AdvertiseCallback)

        unregisterReceiver(Bluetooth_Status_Check)
        BA.disable()
    }

    //Дополнительные функции

    @SuppressLint("MissingPermission")
    private fun initiate() {
        val BM = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        BluetoothGattServer = BM.openGattServer(this, GattServerCallback)
        BA = BM.adapter
        BleScaner = BA.bluetoothLeScanner
        Advertiser = BA.bluetoothLeAdvertiser!!
        BluetoothGattServer.addService(CreateProfile)
    }

    private fun chechBluetoothEnabled() {
        if (BA.isEnabled == false) {
            AccessPermission(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), Reject)}
        else {
            TimesActed = 0 }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getDevices() {
        if (Clicked==true){
            BleScaner.stopScan(BluetoothScannerCallback)
            BLESwitch.setText("Start")
            FoundToGatt.values.forEach(){
                Device->
                Device?.connectGatt(this@Loading, false, BlutoothGattConnectionCallBack)?:Log.e("Devices","Not Found")
            }
            return
        }

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(MatchService))
                .build()
        )

        val settin = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build()

        BLESwitch.setText("Stop")
        BleScaner.startScan(filters, settin, BluetoothScannerCallback)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    private fun Connect_Or_Disconnect() {
        Clicked=(!Clicked)
        getDevices()
    }

    @SuppressLint("MissingPermission")
    private fun Advertise() {

        BleScaner.stopScan(BluetoothScannerCallback)

        val dataBuilder = AdvertiseData.Builder()
        dataBuilder.addServiceUuid(ParcelUuid(MatchService))
        BA.setName("A")
        dataBuilder.setIncludeTxPowerLevel(false)
        dataBuilder.setIncludeDeviceName(true)
        val settingsBuilder = AdvertiseSettings.Builder()
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        settingsBuilder.setTimeout(0)
        settingsBuilder.setConnectable(true)

        Toast.makeText(this, "Advertising", Toast.LENGTH_LONG).show()
        Advertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), AdvertiseCallback)
    }

    protected fun AccessPermission(intent: Intent, Action:Int) {
        val Permissionlauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != Activity.RESULT_OK) {
                    Action
                }
            }
        Permissionlauncher.launch(intent)
    }
}