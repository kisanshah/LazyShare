package com.kisan.lazyshare.ui

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kisan.lazyshare.adapter.SelectionItemDetailLookup
import com.kisan.lazyshare.adapter.SelectionKeyProvider
import com.kisan.lazyshare.adapter.ShareItemAdapter
import com.kisan.lazyshare.databinding.FragmentImageBinding
import com.kisan.lazyshare.model.ShareItem
import com.kisan.lazyshare.utils.Constants
import com.kisan.lazyshare.utils.sdk29orUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageFragment : Fragment() {

    private var _binding: FragmentImageBinding? = null
    private val binding get() = _binding!!

    private lateinit var shareItemAdapter: ShareItemAdapter
    private lateinit var contentObserver: ContentObserver

    private var readPermissionGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private var tracker: SelectionTracker<String>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        shareItemAdapter = ShareItemAdapter {

        }
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
                readPermissionGranted =
                    permission[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted

            }


        setUpRecyclerView()
        checkAndRequestPermission()
        initContentObserver()
        loadPhotosToRecyclerView()


        tracker = SelectionTracker.Builder(
            "mySelection",
            binding.photosRV,
            SelectionKeyProvider(shareItemAdapter),
            SelectionItemDetailLookup(binding.photosRV),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        shareItemAdapter.tracker = tracker

        tracker?.addObserver(
            object : SelectionTracker.SelectionObserver<String>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val items = tracker?.selection!!.size()
                    Log.d("SELECTED_ITEMS", tracker?.selection.toString() + items)
                }
            })
    }


    private fun initContentObserver() {
        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                if (readPermissionGranted) {
                    loadPhotosToRecyclerView()
                }
            }
        }
        context?.contentResolver?.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            false,
            contentObserver
        )
    }

    private fun checkAndRequestPermission() {
        val hasReadPermission =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

        readPermissionGranted = hasReadPermission

        val requirePermission = mutableListOf<String>()
        if (!hasReadPermission) {
            requirePermission.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (requirePermission.isNotEmpty()) {
            permissionLauncher.launch(requirePermission.toTypedArray())
        }
    }

    private fun setUpRecyclerView() {

        binding.photosRV.apply {
            adapter = shareItemAdapter
            layoutManager = GridLayoutManager(requireContext(), RecyclerView.VERTICAL).apply {
                spanCount = 4
            }
            setHasFixedSize(true)

        }
    }

    private fun loadPhotosToRecyclerView() {
        lifecycleScope.launch {
            val photos = getPhotosFromExternalStorage()
            shareItemAdapter.submitList(photos)
        }
    }

    private suspend fun getPhotosFromExternalStorage(): List<ShareItem> {
        return withContext(Dispatchers.IO) {
            val collection = sdk29orUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE,
            )

            val photos = mutableListOf<ShareItem>()
            context?.contentResolver?.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val id = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val displayName = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val width = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val height = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                val size = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(displayName)
                    val photoId = cursor.getLong(id)
                    val photoWidth = cursor.getInt(width)
                    val photoHeight = cursor.getInt(height)
                    val photoSize = cursor.getDouble(size)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        photoId
                    )
                    photos.add(
                        ShareItem(
                            type = Constants.IMAGE_TYPE,
                            name = name,
                            id = photoId,
                            size = photoSize,
                            contentUri = contentUri,
                            width = photoWidth,
                            height = photoHeight,
                        )
                    )
                }
                photos.toList()
            } ?: listOf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.contentResolver?.unregisterContentObserver(contentObserver)
    }
}