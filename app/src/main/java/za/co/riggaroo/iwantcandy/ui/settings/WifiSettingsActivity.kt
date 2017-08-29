package za.co.riggaroo.iwantcandy.ui.settings

import android.app.Activity
import android.os.Bundle
import za.co.riggaroo.iwantcandy.R

/**
 * @author rebeccafranks
 * @since 2017/08/29.
 */

class WifiSettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_settings)
    }
}
