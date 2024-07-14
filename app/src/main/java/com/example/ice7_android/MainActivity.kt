package com.example.ice7_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ice7_android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MovieViewModel by viewModels()
    private lateinit var firstAdapter: FirstAdapter
    private lateinit var movieList: MutableList<Movie>

    private val detailsActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.getAllMovies() // Refresh the movie list
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the adapter with an empty list
        firstAdapter = FirstAdapter(emptyList())
        binding.FirstRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = firstAdapter
        }

        viewModel.movies.observe(this) { movies ->
            movieList = movies.toMutableList()
            firstAdapter.updateMovies(movies)
        }

        firstAdapter.onMovieClick = { movie ->
            val intent = Intent(this, DetailsActivity::class.java).apply {
                putExtra("MOVIE_ID", movie.id)
                putExtra("IS_UPDATE", true)
            }
            detailsActivityResultLauncher.launch(intent)
        }

        binding.addButton.setOnClickListener {
            // go to the Details Activity for adding a new movie
            val intent = Intent(this, DetailsActivity::class.java).apply {
                putExtra("IS_UPDATE", false)
            }
            detailsActivityResultLauncher.launch(intent)
        }

        // Setup swipe to delete
        val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
        {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false // not used
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                AlertDialog.Builder(this@MainActivity).apply {
                    setTitle(getString(R.string.delete_movie))
                    setMessage(getString(R.string.are_you_sure))
                    setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                        val position = viewHolder.adapterPosition
                        val movieId = movieList[position].id
                        viewModel.deleteMovie(movieId) // Trigger deletion in ViewModel
                    }
                    setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                        firstAdapter.notifyItemChanged(viewHolder.adapterPosition) // Reverts the swipe action visually
                    }
                    setCancelable(false)
                }.create().show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.FirstRecyclerView)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllMovies() // Refresh the movie list
    }
}