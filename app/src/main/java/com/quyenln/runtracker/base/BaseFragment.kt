package com.quyenln.runtracker.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    private var _binding: T? = null
    abstract val methodInflate: (LayoutInflater, ViewGroup?, Boolean) -> T
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = methodInflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(savedInstanceState)
        initListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    abstract fun initViews(savedInstanceState: Bundle?)

    abstract fun initListeners()

}