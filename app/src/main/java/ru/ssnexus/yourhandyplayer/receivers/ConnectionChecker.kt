package ru.ssnexus.yourhandyplayer.receivers

import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.domain.Interactor
import timber.log.Timber
import javax.inject.Inject

class ConnectionChecker : BroadcastReceiver() {

    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor
    init {
        App.instance.dagger.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        //Если интента нет, то выходим из метода
        if (intent == null) return

        //Проверяем, какой пришел action
        when (intent.action) {
            Intent.ACTION_HEADSET_PLUG -> {
                when (intent.getIntExtra("state", -1)){
                    0 -> interactor.connectedDeviceTypeLiveData.postValue(R.drawable.speaker_anim)
                    else -> interactor.connectedDeviceTypeLiveData.postValue(R.drawable.headset_anim)
                }
            }
            Intent.ACTION_BATTERY_LOW -> Toast.makeText(context, "Батарея разряжена", Toast.LENGTH_SHORT).show()
            //Если пришло подключение к зарядке
            Intent.ACTION_POWER_CONNECTED -> Toast.makeText(context, "Зарядка подключена", Toast.LENGTH_SHORT).show()
            //Если батарея в норме
            Intent.ACTION_BATTERY_OKAY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                val extraData = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED)
                Timber.d("ACTION_CONNECTION_STATE_CHANGED " + extraData)

                when (extraData){
                    BluetoothHeadset.STATE_CONNECTED -> interactor.connectedDeviceTypeLiveData.postValue(R.drawable.headset_anim)
                    BluetoothHeadset.STATE_DISCONNECTED -> interactor.connectedDeviceTypeLiveData.postValue(R.drawable.speaker_anim)
                }
            }
//            Intent.ACTION_MEDIA_BUTTON -> {
//                Timber.d("KeyEvent.ACTION_DOWN")
//                val extraData = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
//                if (extraData != null){
//                    val action = extraData.action
//                    if (action == KeyEvent.ACTION_DOWN) {
//                        Timber.d("KeyEvent.ACTION_DOWN")
//                    }
//                }
//            }
        }
    }
}