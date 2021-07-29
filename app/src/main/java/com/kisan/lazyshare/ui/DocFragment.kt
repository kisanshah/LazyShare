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
import com.kisan.lazyshare.adapter.ShareItemAdapter
import com.kisan.lazyshare.databinding.FragmentDocBinding
import com.kisan.lazyshare.model.ShareItem
import com.kisan.lazyshare.utils.Constants
import com.kisan.lazyshare.utils.sdk29orUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocFragment : Fragment() {

    private var _binding: FragmentDocBinding? = null
    private val binding get() = _binding!!

    private lateinit var shareItemAdapter: ShareItemAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shareItemAdapter = ShareItemAdapter { }
        setUpRecyclerView()
        loadFilesToRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.documentRecyclerView.apply {
            adapter = shareItemAdapter
            layoutManager = GridLayoutManager(requireContext(), 4)
        }
    }

    private fun loadFilesToRecyclerView() {
        lifecycleScope.launch {
            val files = loadDocumentFromExternalStorage()
            shareItemAdapter.submitList(files)
        }
    }

    private suspend fun loadDocumentFromExternalStorage(): List<ShareItem> {
        return withContext(Dispatchers.IO) {
            val collection = sdk29orUp {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } ?: MediaStore.Files.getContentUri("external")

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.WIDTH,
                MediaStore.Files.FileColumns.HEIGHT,
                MediaStore.Files.FileColumns.SIZE,
            )

            val files = mutableListOf<ShareItem>()
            requireContext().contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val id = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val displayName = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val width = cursor.getColumnIndex(MediaStore.Files.FileColumns.WIDTH)
                val height = cursor.getColumnIndex(MediaStore.Files.FileColumns.HEIGHT)
                val size = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                while (cursor.moveToNext()) {
                    val docName = cursor.getString(displayName)
                    val docId = cursor.getLong(id)
                    val docWidth = cursor.getInt(width)
                    val docHeight = cursor.getInt(height)
                    val docSize = cursor.getDouble(size)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"),
                        docId
                    )
                    files.add(
                        ShareItem(
                            name = docName,
                            height = docHeight,
                            contentUri = contentUri,
                            id = docId,
                            size = docSize,
                            type = Constants.DOC_TYPE,
                            width = docWidth
                        )
                    )
                }
                files.toList()
            } ?: listOf()
        }
    }
}