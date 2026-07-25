package com.example.cardtodo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cardtodo.databinding.ItemTaskCardBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskPagerAdapter(
    private var tasks: List<Task>,
    private val onComplete: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskPagerAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(private val binding: ItemTaskCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.tvDescription.text = task.description.ifBlank { "暂无描述" }

            val sdf = SimpleDateFormat("MM月dd日 yyyy", Locale.CHINA)
            binding.tvDate.text = sdf.format(Date(task.createdAt))

            // 优先级标签
            val priority = try { Priority.valueOf(task.priority) } catch (e: Exception) { Priority.MEDIUM }
            binding.tvPriority.text = when (priority) {
                Priority.HIGH -> "🔴 高优先级"
                Priority.MEDIUM -> "🟡 中优先级"
                Priority.LOW -> "🟢 低优先级"
            }

            // 完成状态
            if (task.isCompleted) {
                binding.ivStatus.setImageResource(R.drawable.ic_check_circle)
                binding.tvTitle.alpha = 0.55f
            } else {
                binding.ivStatus.setImageResource(R.drawable.ic_circle_outline)
                binding.tvTitle.alpha = 1.0f
            }

            binding.btnComplete.text = if (task.isCompleted) "撤销" else "完成"
            binding.btnComplete.setOnClickListener { onComplete(task) }
            binding.btnDelete.setOnClickListener { onDelete(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
