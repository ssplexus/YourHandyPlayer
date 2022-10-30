package ru.ssnexus.yourhandyplayer.view.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.databinding.FragmentPListBinding
import ru.ssnexus.yourhandyplayer.databinding.FragmentTagsSetBinding
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.viewmodel.PListFragmentViewModel
import ru.ssnexus.yourhandyplayer.viewmodel.TagsSetViewModel

class TagsSetFragment : Fragment() {

    companion object {
        fun newInstance() = TagsSetFragment()
    }

    private lateinit var binding: FragmentTagsSetBinding
    private val autoDisposable = AutoDisposable()

    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(TagsSetViewModel::class.java)
    }

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


}