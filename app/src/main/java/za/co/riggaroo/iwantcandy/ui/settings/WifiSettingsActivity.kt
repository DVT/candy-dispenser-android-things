package za.co.riggaroo.iwantcandy.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import za.co.riggaroo.iwantcandy.R
import java.net.NetworkInterface
import java.util.*


/**
 * @author rebeccafranks
 * @since 2017/08/29.
 */

class WifiSettingsActivity : Activity() {

    private lateinit var textViewConnectionInfoNetworkName: TextView
    private lateinit var textViewConnectionInfoIp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_settings)

        textViewConnectionInfoIp = findViewById(R.id.text_view_ip_address)
        textViewConnectionInfoNetworkName = findViewById(R.id.text_view_wifi_connected_status)
        val backButton = findViewById<ImageButton>(R.id.button_back)
        backButton.setOnClickListener { finish() }

        textViewConnectionInfoNetworkName.text = getCurrentSsid(this)
        textViewConnectionInfoIp.text = getIPAddress()

    }

    private fun getCurrentSsid(context: Context): String? {
        var ssid: String? = null
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo ?: return null

        if (networkInfo.isConnected) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectionInfo = wifiManager.connectionInfo
            connectionInfo?.let {
                if (!TextUtils.isEmpty(connectionInfo.ssid)) {
                    ssid = connectionInfo.ssid
                }
            }

        }

        return ssid
    }


    private val TAG: String? = "WifiSettings"

    private fun getIPAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addresses = Collections.list(intf.inetAddresses)
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4)
                            return sAddr

                    }
                }
            }
        } catch (ex: Exception) {
            Log.d(TAG, "Exception getting IP Address:", ex);
        }
        return ""
    }

    fun isOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, WifiSettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
