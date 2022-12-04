package ru.ssnexus.yourhandyplayer.view.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import ru.ssnexus.yourhandyplayer.databinding.FragmentTagsSetBinding
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.view.MainActivity
import ru.ssnexus.yourhandyplayer.viewmodel.TagsSetViewModel

class TagsSetFragment : Fragment() {

    companion object {
        fun newInstance() = TagsSetFragment()

        private const val ROCK_TAG = "Rock"
        private const val POP_TAG = "Pop"
        private const val RNB_TAG = "R&ampB"
        private const val HIPHOP_TAG = "Hiphop"
        private const val LOUNGE_TAG = "Lounge"
        private const val ELECTRONIC_TAG = "Electronic"
        private const val RELAXATION_TAG = "Relaxation"
        private const val METAL_TAG = "Metal"
        private const val CLASSICAL_TAG = "Classical"
        private const val JAZZ_TAG = "Jazz"
        private const val WORLD_TAG = "World"
        private const val SOUNDTRACK_TAG = "Soundtrack"
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
        binding.loungeTag.setOnClickListener { setTags() }
        binding.electronicTag.setOnClickListener { setTags() }
        binding.relaxationTag.setOnClickListener { setTags() }
        binding.metalTag.setOnClickListener { setTags() }
        binding.classicalTag.setOnClickListener { setTags() }
        binding.jazzTag.setOnClickListener { setTags() }
        binding.worldTag.setOnClickListener { setTags() }
        binding.soundtrackTag.setOnClickListener { setTags() }

        binding.acceptButton.setOnClickListener {
            viewModel.saveTagsProperty(tagsProperty)
            (requireActivity() as MainActivity).launchFragment(HomeFragment())
        }

        viewModel.tagsPropertyLifeData.observe(viewLifecycleOwner, Observer<String> {
            tagsProperty = it
        })

        viewModel.tagsPropertyLifeData.observe(viewLifecycleOwner, Observer<String> {
            it.split("+").forEach {tag ->
                when(tag.trim()) {
                    ROCK_TAG -> binding.rockTag.isChecked = true
                    POP_TAG -> binding.popTag.isChecked = true
                    RNB_TAG -> binding.rnbTag.isChecked = true
                    HIPHOP_TAG -> binding.hiphopTag.isChecked = true
                    LOUNGE_TAG -> binding.loungeTag.isChecked = true
                    ELECTRONIC_TAG -> binding.electronicTag.isChecked = true
                    RELAXATION_TAG -> binding.relaxationTag.isChecked = true
                    METAL_TAG -> binding.metalTag.isChecked = true
                    CLASSICAL_TAG -> binding.classicalTag.isChecked = true
                    JAZZ_TAG -> binding.jazzTag.isChecked = true
                    WORLD_TAG -> binding.worldTag.isChecked = true
                    SOUNDTRACK_TAG -> binding.soundtrackTag.isChecked = true
                }
            }
        })

        (requireActivity() as MainActivity).title = "Choose tags"
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).isHomeFragment(false)
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).isHomeFragment(false)
    }

    fun setTags(){
        var result = ""
        val delim = " + "
        if (binding.rockTag.isChecked) result += binding.rockTag.text.toString() + delim
        if (binding.popTag.isChecked) result += binding.popTag.text.toString() + delim
        if (binding.rnbTag.isChecked) result += binding.rnbTag.text.toString() + delim
        if (binding.hiphopTag.isChecked) result += binding.hiphopTag.text.toString() + delim
        if (binding.loungeTag.isChecked) result += binding.loungeTag.text.toString() + delim
        if (binding.electronicTag.isChecked) result += binding.electronicTag.text.toString() + delim
        if (binding.relaxationTag.isChecked) result += binding.relaxationTag.text.toString() + delim
        if (binding.metalTag.isChecked) result += binding.metalTag.text.toString() + delim
        if (binding.classicalTag.isChecked) result += binding.classicalTag.text.toString() + delim
        if (binding.jazzTag.isChecked) result += binding.jazzTag.text.toString() + delim
        if (binding.worldTag.isChecked) result += binding.worldTag.text.toString() + delim
        if (binding.soundtrackTag.isChecked) result += binding.soundtrackTag.text.toString() + delim
        if (result.isEmpty()) result = "Select your tags"
        result = result.removeSuffix(delim)

        tagsProperty = result
    }



}