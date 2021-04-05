package com.example.letschat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val DELETED_VIEW_TYPE = 1
private const val NORMAL_VIEW_TYPE = 2
class PeopleFragment : Fragment() {

    lateinit var madapter : FirestorePagingAdapter<com.example.letschat.User, RecyclerView.ViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerView: RecyclerView
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.DESCENDING)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
            setupAdapter()

        viewManager = LinearLayoutManager(requireContext())
         val view =  inflater.inflate(R.layout.fragment_chats,container,false)

         recyclerView = view.findViewById(R.id.recyclerView)

        return view ;
    }

    // Init Paging Configuration
    private fun setupAdapter() {
        val config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(2)
                .setPageSize(10)
                .build()
        // Init Adapter Configuration
        val options = FirestorePagingOptions.Builder<com.example.letschat.User>()
                .setLifecycleOwner(this)
                .setQuery(database, config,User::class.java)
                .build()
        // Instantiate Paging Adapter

        madapter = object : FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int ): RecyclerView.ViewHolder {
                val inflater = layoutInflater
                return when (viewType) {
                    NORMAL_VIEW_TYPE -> {
                        UserViewHolder(inflater.inflate(R.layout.list_item, parent, false))
                    }
                    else -> EmptyViewHolder(inflater.inflate(R.layout.empty_view, parent, false))

                }
            }



            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
                // Bind to ViewHolder
               if(holder is UserViewHolder){
                   holder.bind(user = model){name: String, photo: String, id: String ->  
                       val intent = Intent(requireContext(),ChatActivity::class.java)
                       intent.putExtra(UID,id)
                       intent.putExtra(NAME ,name)
                       intent.putExtra( IMAGE,photo)
                       startActivity(intent)
                   }
               }
            }

            override fun onError(e: Exception) {
                super.onError(e)
                e.message?.let { Log.e("MainActivity", it) }
            }


            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(com.example.letschat.User::class.java)
                return if (auth.uid == item!!.uid) {
                    DELETED_VIEW_TYPE
                } else {
                    NORMAL_VIEW_TYPE
                }
            }


            override fun onLoadingStateChanged(state: LoadingState) {
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                    }

                    LoadingState.LOADING_MORE -> {
                    }

                    LoadingState.LOADED -> {
                    }

                    LoadingState.ERROR -> {
                        Toast.makeText(
                                requireContext(),
                                "Error Occurred!",
                                Toast.LENGTH_SHORT
                        ).show()
                    }

                    LoadingState.FINISHED -> {
                    }
                }
            }

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            layoutManager = viewManager
            adapter = madapter
        }
    }

}