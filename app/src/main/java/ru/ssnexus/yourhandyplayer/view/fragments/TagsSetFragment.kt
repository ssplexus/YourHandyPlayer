package ru.ssnexus.yourhandyplayer.view.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import ru.ssnexus.yourhandyplayer.data.preferences.PreferenceProvider
import ru.ssnexus.yourhandyplayer.databinding.FragmentTagsSetBinding
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.view.MainActivity
import ru.ssnexus.yourhandyplayer.viewmodel.TagsSetViewModel
import timber.log.Timber

class TagsSetFragment : Fragment() {

    companion object {

        private const val ROCK_TAG = "Rock"
        private const val POP_TAG = "Pop"
        private const val RNB_TAG = "RnB"
        private const val HIPHOP_TAG = "Hiphop"
        private const val CHILLOUT_TAG = "Chillout"
        private const val ELECTRONIC_TAG = "Electronic"
        private const val RELAXATION_TAG = "Relaxation"
        private const val METAL_TAG = "Metal"
        private const val CLASSICAL_TAG = "Classical"
        private const val JAZZ_TAG = "Jazz"
        private const val HOUSE_TAG = "House"
        private const val BLUES_TAG = "Blues"
        private const val COUNTRY_TAG = "Country"
        private const val REGGAE_TAG = "Reggae"
        private const val PUNK_TAG = "Punk"
        private const val LATIN_TAG = "Latin"
    }

    private lateinit var binding: FragmentTagsSetBinding
    private val autoDisposable = AutoDisposable()

    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(TagsSetViewModel::class.java)
    }

    private var tagsProperty: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTagsSetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rockTag.setOnClickListener { setTags() }
        binding.popTag.setOnClickListener { setTags() }
        binding.rnbTag.setOnClickListener { setTags() }
        binding.hiphopTag.setOnClickListener { setTags() }
        binding.chilloutTag.setOnClickListener { setTags() }
        binding.electronicTag.setOnClickListener { setTags() }
        binding.relaxationTag.setOnClickListener { setTags() }
        binding.metalTag.setOnClickListener { setTags() }
        binding.classicalTag.setOnClickListener { setTags() }
        binding.jazzTag.setOnClickListener { setTags() }
        binding.houseTag.setOnClickListener{ setTags() }
        binding.bluesTag.setOnClickListener { setTags() }
        binding.countryTag.setOnClickListener { setTags() }
        binding.reggaeTag.setOnClickListener { setTags() }
        binding.punkTag.setOnClickListener { setTags() }
        binding.latinTag.setOnClickListener { setTags() }

        binding.acceptButton.setOnClickListener {
            if(tagsProperty.isBlank()) tagsProperty = PreferenceProvider.DEFAULT_TAGS
            viewModel.saveTagsProperty(tagsProperty)
            (requireActivity() as MainActivity).launchFragment(HomeFragment(), tagsProperty)
        }

        viewModel.tagsPropertyLiveData.observe(viewLifecycleOwner, Observer<String> {
            tagsProperty = it
            it.split("+").forEach {tag ->
                when(tag.trim()) {
                    ROCK_TAG -> binding.rockTag.isChecked = true
                    POP_TAG -> binding.popTag.isChecked = true
                    RNB_TAG -> binding.rnbTag.isChecked = true
                    HIPHOP_TAG -> binding.hiphopTag.isChecked = true
                    CHILLOUT_TAG -> binding.chilloutTag.isChecked = true
                    ELECTRONIC_TAG -> binding.electronicTag.isChecked = true
                    RELAXATION_TAG -> binding.relaxationTag.isChecked = true
                    METAL_TAG -> binding.metalTag.isChecked = true
                    CLASSICAL_TAG -> binding.classicalTag.isChecked = true
                    JAZZ_TAG -> binding.jazzTag.isChecked = true
                    HOUSE_TAG -> binding.houseTag.isChecked = true
                    BLUES_TAG -> binding.bluesTag.isChecked = true
                    COUNTRY_TAG -> binding.countryTag.isChecked = true
                    REGGAE_TAG -> binding.reggaeTag.isChecked = true
                    PUNK_TAG -> binding.punkTag.isChecked = true
                    LATIN_TAG -> binding.latinTag.isChecked = true
                }
            }
        })

        binding.backPress.setOnClickListener {
            (requireActivity() as MainActivity).onBackPressed()
        }
        binding.actionBarTitle.text = "Tags"
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    fun setTags(){
        var result = ""
        val delim = " + "
        if (binding.rockTag.isChecked) result += binding.rockTag.text.toString() + delim
        if (binding.popTag.isChecked) result += binding.popTag.text.toString() + delim
        if (binding.rnbTag.isChecked) result += binding.rnbTag.text.toString() + delim
        if (binding.hiphopTag.isChecked) result += binding.hiphopTag.text.toString() + delim
        if (binding.chilloutTag.isChecked) result += binding.chilloutTag.text.toString() + delim
        if (binding.electronicTag.isChecked) result += binding.electronicTag.text.toString() + delim
        if (binding.relaxationTag.isChecked) result += binding.relaxationTag.text.toString() + delim
        if (binding.metalTag.isChecked) result += binding.metalTag.text.toString() + delim
        if (binding.classicalTag.isChecked) result += binding.classicalTag.text.toString() + delim
        if (binding.jazzTag.isChecked) result += binding.jazzTag.text.toString() + delim
        if (binding.houseTag.isChecked) result += binding.houseTag.text.toString() + delim
        if (binding.countryTag.isChecked) result += binding.countryTag.text.toString() + delim
        if (binding.reggaeTag.isChecked) result += binding.reggaeTag.text.toString() + delim
        if (binding.punkTag.isChecked) result += binding.punkTag.text.toString() + delim
        if (binding.latinTag.isChecked) result += binding.latinTag.text.toString() + delim

        if (result.isEmpty()) result = "Select your tags"
        result = result.removeSuffix(delim)

        tagsProperty = result
    }



}