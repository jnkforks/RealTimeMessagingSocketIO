package com.polatechno.realtimemessagingsocetio.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.polatechno.realtimemessagingsocetio.data.model.MessageItem
import com.polatechno.realtimemessagingsocetio.R
import kotlinx.android.synthetic.main.message_item.view.*


class MessageAdapter(private var messageList: ArrayList<MessageItem>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.message_item,
            parent, false
        )

        return MessageViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentItem = messageList[position]

        holder.tvUsername.text = currentItem.username
        holder.tvMessage.text =  ": " + currentItem.message
    }

    override fun getItemCount() = messageList.size

    fun addItem(newItem: MessageItem) {

        messageList.add(newItem)
        notifyItemInserted(messageList.size - 1)

    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.tvUsername
        val tvMessage: TextView = itemView.tvMessage
    }
}