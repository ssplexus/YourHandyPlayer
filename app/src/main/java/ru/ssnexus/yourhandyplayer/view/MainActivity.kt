package ru.ssnexus.yourhandyplayer.view

import android.content.BroadcastReceiver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.databinding.ActivityMainBinding
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.view.fragments.HomeFragment
import ru.ssnexus.yourhandyplayer.view.fragments.PListFragment
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
//    @Inject
//    lateinit var interactor: Interactor

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        //Передаем его в метод
        setContentView(binding.root)

        //App.instance.dagger.inject(this)
        //Инициализируем объект


        //Запускаем фрагмент при старте
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_placeholder, PListFragment())
            .addToBackStack(null)
            .commit()
    }
}