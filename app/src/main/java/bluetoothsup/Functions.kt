package bluetoothsup

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.util.Log
import android.widget.TextView
import java.util.*
import kotlin.collections.ArrayList

val Match_Service:UUID = UUID.fromString("0000fe84-0000-1000-8000-00805f9b34fb")
val Mobile_number_characteristic:UUID = UUID.fromString("00002A1C-0000-1000-8000-00805F9B34FB")
val Mobile_number_descriptor:UUID = UUID.fromString("63fbff4a-27f8-4034-a148-448206fbc9cc")
const val Numbers="9164009726"
val NumInBits= Numbers.toByteArray()
val Reject: Int = Log.e("Status","Rejected")

 fun CreateBluetoothService(): BluetoothGattService {

    val service = BluetoothGattService(
        Match_Service,
        BluetoothGattService.SERVICE_TYPE_PRIMARY,

        )

    val mobile_number = BluetoothGattCharacteristic(
        Mobile_number_characteristic,
        BluetoothGattCharacteristic.PROPERTY_READ,
        BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED or BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM
    )
    mobile_number.setValue(NumInBits)
    Log.i(mobile_number.getStringValue(0),"Setted in Creator")

    val mobile_number_des = BluetoothGattDescriptor(
        Mobile_number_descriptor,
        BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
    )
    mobile_number.addDescriptor(mobile_number_des)
    service.addCharacteristic(mobile_number)
    return service
}

 fun Connection_List_Creator(hm: HashMap<String, String>): String {
    var Value= ""
    for (Key: String? in hm.keys) {
        if (hm[Key] != "false") {
            Value += Key + " " + hm[Key] + "\n"
        }
    }
     return Value
}

//Получение сервиса/характеристики/дескриптора по UUID

fun Get_Decriptor(gatt: BluetoothGatt?,My_Service: UUID,My_Characteristic:UUID,My_Descriptor:UUID): BluetoothGattDescriptor? {
    val Des=Get_Characteristic(gatt,My_Service,My_Characteristic)?.descriptors?.find { rand_descriptors -> rand_descriptors.uuid.toString()== My_Descriptor.toString() }
    return Des
}

fun Get_Characteristic(gatt:BluetoothGatt?,My_Service: UUID,My_Characteristic:UUID): BluetoothGattCharacteristic? {
    val Char=Get_Service(gatt,My_Service)?.characteristics?.find { Rand_characteristic -> Rand_characteristic.uuid.toString() == My_Characteristic.toString() }
    return Char
}

fun Get_Service(gatt:BluetoothGatt?,My_Service:UUID): BluetoothGattService? {
   val Service= gatt?.services?.find { Rand_service -> Rand_service.uuid.toString() == My_Service.toString()}
    return Service
}
val String_Permissions= arrayOf<String>(Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.READ_CONTACTS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BLUETOOTH_ADVERTISE,
Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_PRIVILEGED,Manifest.permission.BLUETOOTH_ADMIN,
Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET)








