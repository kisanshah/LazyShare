package com.kisan.lazyshare.ui

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kisan.lazyshare.adapter.ShareItemAdapter
import com.kisan.lazyshare.databinding.FragmentVideoBinding
import com.kisan.lazyshare.model.ShareItem
import com.kisan.lazyshare.utils.Constants
import com.kisan.lazyshare.utils.sdk29orUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoFragment : Fragment() {

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!

    private lateinit var shareItemAdapter: ShareItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shareItemAdapter = ShareItemAdapter { }
        setUpRecyclerView()
        loadVideoToRecyclerView()
    }

    private fun loadVideoToRecyclerView() {
        lifecycleScope.launch {
            val videos = loadVideosFromExternalStorage()
            shareItemAdapter.submitList(videos)
        }
    }

    private fun setUpRecyclerView() {
        binding.videoRecyclerView.apply {
            adapter = shareItemAdapter
            layoutManager = GridLayoutManager(requireContext(), 4)
            setHasFixedSize(true)
        }
    }


    private suspend fun loadVideosFromExternalStorage(): List<ShareItem> {
        return withContext(Dispatchers.IO) {
            val collection = sdk29orUp {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } ?: MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.SIZE,
            )

            val videos = mutableListOf<ShareItem>()

            context?.contentResolver?.query(
                collection,
                projection,
                null,
                null,
                "",
            )?.use { cursor ->
                val id = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                val displayName = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val width = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val height = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val size = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(displayName)
                    val videoId = cursor.getLong(id)
                    val videoWidth = cursor.getInt(width)
                    val videoHeight = cursor.getInt(height)
                    val videoSize = cursor.getDouble(size)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        videoId
                    )
                    videos.add(
                        ShareItem(
                            type = Constants.VIDEO_TYPE,
                            name = name,
                            id = videoId,
                            size = videoSize,
                            contentUri = contentUri,
                            width = videoWidth,
                            height = videoHeight,
                        )
                    )
                }
                videos.toList()
            } ?: listOf()
        }
    }
}