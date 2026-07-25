package com.example.cardtodo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.cardtodo.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdapter()
        setupViewPager()
        observeTasks()
        setupFab()
        setupSettings()
    }

    private fun setupAdapter() {
        adapter = TaskPagerAdapter(
            tasks = emptyList(),
            onComplete = { task -> viewModel.toggleComplete(task) },
            onDelete = { task -> viewModel.deleteTask(task) }
        )
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1

        val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.page_margin)
        binding.viewPager.setPageTransformer { page, position ->
            val scaleFactor = 1 - 0.12f * abs(position)
            page.scaleY = scaleFactor
            page.alpha = 0.5f + (1 - abs(position)) * 0.5f
            page.translationX = -position * pageMarginPx * 2
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateCounter(position)
            }
        })
    }

    private fun observeTasks() {
        viewModel.allTasks.observe(this) { tasks ->
            adapter.updateTasks(tasks)
            updateCounter(binding.viewPager.currentItem)
            if (tasks.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvCounter.visibility = View.GONE
                binding.viewPager.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.tvCounter.visibility = View.VISIBLE
                binding.viewPager.visibility = View.VISIBLE
            }
        }
    }

    private fun updateCounter(position: Int) {
        val total = adapter.itemCount
        if (total > 0) {
            binding.tvCounter.text = "${position + 1} / $total"
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            val dialog = AddTaskDialogFragment { title, description, priority ->
                viewModel.addTask(title, description, priority)
            }
            dialog.show(supportFragmentManager, "AddTaskDialog")
        }
    }

    private fun setupSettings() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
