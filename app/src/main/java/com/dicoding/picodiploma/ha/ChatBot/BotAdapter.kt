package com.dicoding.picodiploma.ha.ChatBot

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.ha.AddLocationActivity
import com.dicoding.picodiploma.ha.AddReportActivity
import com.dicoding.picodiploma.ha.R
import org.w3c.dom.Text

class BotAdapter(private var activity: Activity, private var messageList: List<Message>) : RecyclerView.Adapter<BotAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageReceive: TextView = itemView.findViewById(R.id.get)
        var messageSend: TextView = itemView.findViewById(R.id.send)
        var image : ImageView = itemView.findViewById(R.id.gambar)
        var namabot : TextView = itemView.findViewById(R.id.namabot)
        var anda : TextView = itemView.findViewById(R.id.anda)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.chatbot_layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message: String = messageList.get(position).message
        val isReceived: Boolean = messageList.get(position).isReceived
        setVisibility(isReceived,holder,message)

        holder.messageReceive.setOnClickListener{

            if(message.contains("bersedia")){
                val intent = Intent(holder.itemView.context , AddReportActivity::class.java )
                activity.startActivity(intent)
            }else{
                //DO NOTHING
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.count()
    }

    private fun setVisibility(isReceived : Boolean, holder:ChatViewHolder, message:String){
        if (isReceived) {
            holder.messageReceive.visibility = View.VISIBLE
            holder.messageSend.visibility = View.GONE
            holder.messageReceive.text = message
            holder.namabot.visibility = View.VISIBLE
            holder.image.visibility= View.VISIBLE
            holder.anda.visibility = View.GONE

        } else {

            holder.messageSend.visibility = View.VISIBLE
            holder.messageReceive.visibility = View.GONE
            holder.messageSend.text = message
            holder.namabot.visibility = View.GONE
            holder.image.visibility= View.GONE
            holder.anda.visibility = View.VISIBLE
        }
    }
}