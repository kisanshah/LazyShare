package com.kisan.lazyshare.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kisan.lazyshare.databinding.FragmentAppBinding
import com.kisan.lazyshare.databinding.FragmentVideoBinding
import com.kisan.lazyshare.model.ShareItem
import com.kisan.lazyshare.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppFragment : Fragment() {

    private var _binding: FragmentAppBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            loadInstalledApplication()
        }
    }

    private suspend fun loadInstalledApplication() {
        return withContext(Dispatchers.IO) {
            val packageManager = requireContext().packageManager
            val listOfApp = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps = listOf<ShareItem>()
            for (app in listOfApp) {
//                val appName = app.loadLabel(packageManager).toS
//
//                ShareItem(
//                    type = Constants.APP_TYPE,
//                    name = appName,
//                    id = app.uid,
//                    size = photoSize,
//                    contentUri =,
//                    width = photoWidth,
//                    height = photoHeight,
//                )
            }
        }
    }
}