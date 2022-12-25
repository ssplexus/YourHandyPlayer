package ru.ssnexus.yourhandyplayer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.domain.Interactor
import timber.log.Timber
import javax.inject.Inject

class ConnectionChecker : BroadcastReceiver() {

    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    override fun onReceive(context: Context?, intent: Intent?) {
        //Если интента нет, то выходим из метода
        if (intent == null) return

        //Проверяем, какой пришел action
        when (intent.action) {
            Intent.ACTION_HEADSET_PLUG -> {
                Timber.d("ConnectionChecker : Headset")
                when (intent.getIntExtra("state", -1)){
                    0 -> interactor.connectedDeviceTypeLiveData.postValue(R.drawable.speaker_anim)
                    else -> interactor.connectedDeviceTypeLiveData.postValue(R.drawable.headset_anim)
//                    0 -> Toast.makeText(context, "Headphones not plugged in", Toast.LENGTH_LONG).show()
//                    else -> Toast.makeText(context, "Headphones plugged in", Toast.LENGTH_LONG).show()
                }
            }

            Intent.ACTION_BATTERY_LOW -> Toast.makeText(context, "Батарея разряжена", Toast.LENGTH_SHORT).show()
            //Если пришло подключение к зарядке
            Intent.ACTION_POWER_CONNECTED -> Toast.makeText(context, "Зарядка подключена", Toast.LENGTH_SHORT).show()
            //Если батарея в норме
            Intent.ACTION_BATTERY_OKAY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


    }
}