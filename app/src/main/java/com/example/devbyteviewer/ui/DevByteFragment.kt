package com.example.devbyteviewer.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.devbyteviewer.R
import com.example.devbyteviewer.databinding.ActivityDevByteViewerBinding
import com.example.devbyteviewer.databinding.DevbyteItemBinding
import com.example.devbyteviewer.databinding.FragmentDevByteBindingImpl
import com.example.devbyteviewer.domain.Video
import com.example.devbyteviewer.viewmodels.DevByteViewModel

/**
 * Show a list of DevBytes on screen.
 */
class DevByteFragment : Fragment() {

    /**
     * One way to delay creation of the viewModel until an appropriate lifecycle method is to use
     * lazy. This requires that viewModel not be referenced before onViewCreated(), which we
     * do in this Fragment.
     */
    private val viewModel: DevByteViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewmodel after onViewCreated()"
        }

        ViewModelProvider(
            this,
            DevByteViewModel.Factory(activity.application)
        ).get(DevByteViewModel::class.java)
    }

    /**
     * RecyclerView Adapter for converting a list of Video to cards.
     */
    private var viewModelAdapter: DevByteAdapter? = null

    /**
     * Called immediately after onCreateView() has returned, and fragment's
     * view hierarchy has been created.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.playlist.observe(viewLifecycleOwner, { videos ->
            videos.apply {
                videos?.apply {
                    viewModelAdapter?.videos = videos
                }
            }
        })
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentDevByteBindingImpl = DataBindingUtil.inflate(
            inflater,
        R.layout.fragment_dev_byte,
        container,
        false)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        viewModelAdapter = DevByteAdapter(VideoClick {
            // When a video is clicked this block or lambda will be called by DevByteAdapter

            // context is not around, we can safely discard this click since the Fragment is no
            // longer on the screen
            val packetManager = context?.packageManager ?: return@VideoClick

            var intent = Intent(Intent.ACTION_VIEW, it.launchUri)
            if (intent.resolveActivity(packetManager) == null) {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.url))
            }

            startActivity(intent)
        })

        binding.root.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

        return binding.root
    }

    /**
     * Helper method to generate YouTube app links
     */
    private val Video.launchUri: Uri
        get() {
            val httpUri = Uri.parse(url)
            return Uri.parse("vnd.youtube:" + httpUri.getQueryParameter("v"))
        }
}

    /**
     * Click listener for Videos. By giving the block a name it helps a reader understand what it does.
     *
     */
    class VideoClick(val block: (Video) -> Unit) {
        fun onClick(video: Video) = block(video)
    }

    /**
    * RecyclerView Adapter for setting up data binding on the items in the list.
    */
    class DevByteAdapter(val callback: VideoClick) : RecyclerView.Adapter<DevByteViewHolder>() {

        /**
         * The videos that our Adapter will show
         */
        var videos: List<Video> = emptyList()
            set(value) {
                field = value

                notifyDataSetChanged()
            }

        /**
         * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
         * an item.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevByteViewHolder {
            val withDataBinding: DevbyteItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                DevByteViewHolder.LAYOUT,
                parent, false)
            return  DevByteViewHolder(withDataBinding)
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
         * position.
         */
        override fun onBindViewHolder(holder: DevByteViewHolder, position: Int) {
            holder.viewerBinding.also {
                it.video = videos[position]
                it.videoCallback = callback
            }
        }

        override fun getItemCount(): Int {
            return videos.size
        }

    }

/**
 * ViewHolder for DevByte items. All work is done by data binding.
 */
class DevByteViewHolder(val viewerBinding: DevbyteItemBinding) : RecyclerView.ViewHolder(viewerBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.devbyte_item
    }
}
