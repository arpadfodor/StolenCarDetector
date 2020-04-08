package com.arpadfodor.android.stolencardetector.view

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.arpadfodor.android.stolencardetector.BuildConfig
import com.arpadfodor.android.stolencardetector.R
import com.arpadfodor.android.stolencardetector.view.utils.AppDialog
import com.arpadfodor.android.stolencardetector.view.utils.GalleryFragmentArgs
import com.arpadfodor.android.stolencardetector.view.utils.padWithDisplayCutout
import java.io.File
import java.util.Locale

val EXTENSION_WHITELIST = arrayOf("JPG")

/**
 * Fragment used to present the user the gallery
 */
class GalleryFragment internal constructor() : Fragment() {

    /**
     * AndroidX navigation arguments
     */
    private val args: GalleryFragmentArgs by navArgs()

    private lateinit var mediaList: MutableList<File>

    inner class MediaPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        override fun getCount(): Int = mediaList.size
        override fun getItem(position: Int): Fragment =
            PhotoFragment.create(
                mediaList[position]
            )
        override fun getItemPosition(obj: Any): Int = POSITION_NONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Mark this as a retain fragment, so the lifecycle does not get restarted on config change
        retainInstance = true

        // Get root directory of media from navigation arguments
        val rootDirectory = File(args.rootDirectory)

        // Walk through all files in the root directory
        // Reverse the order of the list to present the last photos first
        mediaList = rootDirectory.listFiles { file ->
            EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
        }?.sortedDescending()?.toMutableList() ?: mutableListOf()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        //Checking media files list
        if (mediaList.isEmpty()) {
            view.findViewById<ImageButton>(R.id.delete_button).isEnabled = false
            view.findViewById<ImageButton>(R.id.share_button).isEnabled = false
        }

        // Populate the ViewPager and implement a cache of two media items
        val mediaViewPager = view.findViewById<ViewPager>(R.id.photo_view_pager).apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager)
        }

        // Make sure that the cutout "safe area" avoids the screen notch if any
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Use extension method to pad "inside" view containing UI using display cutout's bounds
            view.findViewById<ConstraintLayout>(R.id.cutout_safe_area).padWithDisplayCutout()
        }

        // Handle back button press
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            Navigation.findNavController(requireActivity(),
                R.id.camera_container
            ).navigateUp()
        }

        // Handle share button press
        view.findViewById<ImageButton>(R.id.share_button).setOnClickListener {

            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                // Create a sharing intent
                val intent = Intent().apply {
                    // Infer media type from file extension
                    val mediaType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(mediaFile.extension)
                    // Get URI from the FileProvider implementation
                    val uri = FileProvider.getUriForFile(
                        view.context, BuildConfig.APPLICATION_ID + ".provider", mediaFile)
                    // Set the appropriate intent extra, type, action and flags
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = mediaType
                    action = Intent.ACTION_SEND
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                // Launch the intent letting the user choose which app to share with
                startActivity(Intent.createChooser(intent, getString(R.string.share_hint)))
            }
        }

        // Handle delete button press
        view.findViewById<ImageButton>(R.id.delete_button).setOnClickListener {

            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                val deleteDialog = AppDialog(this.requireContext(), getString(R.string.delete_title), getString(R.string.delete_dialog),
                    resources.getDrawable(R.drawable.warning))
                deleteDialog.setPositiveButton {

                    // Delete current photo
                    mediaFile.delete()

                    // Send relevant broadcast to notify other apps of deletion
                    MediaScannerConnection.scanFile(
                        view.context, arrayOf(mediaFile.absolutePath), null, null)

                    // Notify our view pager
                    mediaList.removeAt(mediaViewPager.currentItem)
                    mediaViewPager.adapter?.notifyDataSetChanged()

                    // If all photos have been deleted, return to camera
                    if (mediaList.isEmpty()) {
                        Navigation.findNavController(requireActivity(),
                            R.id.camera_container
                        ).navigateUp()
                    }

                }
                deleteDialog.show()

            }

        }

    }

}
