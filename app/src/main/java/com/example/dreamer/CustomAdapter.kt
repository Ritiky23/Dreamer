package com.example.dreamer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dreamer.R

class CustomAdapter(private val mList: List<DreamResponse>, private val onItemClick: (Int, DreamResponse) -> Unit) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_response, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(position,item) }
    }

    override fun getItemCount() = mList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dreamContentTextView: TextView = itemView.findViewById(R.id.dream_content_text_view)
        private val emotionContentTextView: TextView = itemView.findViewById(R.id.emotion_text_view)
        private val timeTextView: TextView = itemView.findViewById(R.id.timestamp_text_view)

        fun bind(item: DreamResponse) {
            dreamContentTextView.text = item.dreamContent
            emotionContentTextView.text = item.emotion
            timeTextView.text = item.timestamp
        }
    }
}
